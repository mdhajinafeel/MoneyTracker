package com.nprotech.moneytracker.repositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.nprotech.moneytracker.constants.DebtLoanType;
import com.nprotech.moneytracker.db.dao.AccountDao;
import com.nprotech.moneytracker.db.dao.DebtLoanDao;
import com.nprotech.moneytracker.db.dao.DebtLoanPaymentDao;
import com.nprotech.moneytracker.db.dao.DebtLoanPaymentTransactionDao;
import com.nprotech.moneytracker.db.dao.DebtLoanTransactionAttachmentDao;
import com.nprotech.moneytracker.db.dao.WalletDao;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanPaymentEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanPaymentTransactionEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanTransactionAttachmentEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DebtLoanWorkManagerHelper;
import com.nprotech.moneytracker.models.DebtLoanPaymentWithDetails;
import com.nprotech.moneytracker.models.DebtLoanTransactionWithDetails;
import com.nprotech.moneytracker.models.DebtLoanWithDetails;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

public class DebtLoanRepository {

    private final DebtLoanDao debtLoanDao;
    private final DebtLoanPaymentDao debtLoanPaymentDao;
    private final WalletDao walletDao;
    private final AccountDao accountDao;
    private final DebtLoanPaymentTransactionDao debtLoanPaymentTransactionDao;
    private final DebtLoanTransactionAttachmentDao debtLoanTransactionAttachmentDao;

    public DebtLoanRepository(DebtLoanDao debtLoanDao, DebtLoanPaymentDao debtLoanPaymentDao, WalletDao walletDao, AccountDao accountDao,
                              DebtLoanPaymentTransactionDao debtLoanPaymentTransactionDao, DebtLoanTransactionAttachmentDao debtLoanTransactionAttachmentDao) {
        this.debtLoanDao = debtLoanDao;
        this.debtLoanPaymentDao = debtLoanPaymentDao;
        this.walletDao = walletDao;
        this.accountDao = accountDao;
        this.debtLoanPaymentTransactionDao = debtLoanPaymentTransactionDao;
        this.debtLoanTransactionAttachmentDao = debtLoanTransactionAttachmentDao;
    }

    public long saveDebtLoan(DebtLoanEntity entity, List<DebtLoanPaymentEntity> payments, Context context) {
        long debtLoanId = debtLoanDao.insert(entity);
        if (debtLoanId > 0 && payments != null && !payments.isEmpty()) {
            for (DebtLoanPaymentEntity payment : payments) {
                payment.debtLoanId = (int) debtLoanId;
                payment.tempDebtLoanServerId = entity.tempDebtLoanServerId;
            }
            debtLoanPaymentDao.insertAll(payments);
        }

        updateMoneyBalance(entity, entity.isMoneyReceivedLent);

        // =====================================================
        // DEBT & LOAN REMINDER
        // =====================================================

        if (debtLoanId > 0 && entity.reminderEnabled) {
            Long nextReminderDate = getEarliestReminderDate();
            if (nextReminderDate != null) {
                DebtLoanWorkManagerHelper.scheduleNextReminder(context, nextReminderDate);
            }
        }

        return debtLoanId;
    }

    public int saveScheduledPaymentTransaction(DebtLoanPaymentTransactionEntity transaction, DebtLoanPaymentEntity scheduledPayment, DebtLoanEntity debtLoan) {
        try {
            long transactionId = debtLoanPaymentTransactionDao.insert(transaction);
            if (transactionId <= 0) {
                return 0;
            }

            int paymentUpdated = debtLoanPaymentDao.update(scheduledPayment);
            if (paymentUpdated <= 0) {
                return 0;
            }

            int debtLoanUpdated = debtLoanDao.update(debtLoan);
            if (debtLoanUpdated <= 0) {
                return 0;
            }

            // 4. Update wallet + account
            int balanceUpdated = updatePaymentMoneyBalance(debtLoan, transaction.walletId, transaction.amount, transaction.convertedAmount);
            if (balanceUpdated <= 0) {
                return 0;
            }

            return (int) transactionId;
        } catch (Exception e) {
            AppLogger.e(getClass(), "saveScheduledPaymentTransaction", e);
            return 0;
        }
    }

    public int saveFlexiblePaymentTransaction(DebtLoanPaymentTransactionEntity transaction, DebtLoanEntity debtLoan) {
        try {
            long transactionId = debtLoanPaymentTransactionDao.insert(transaction);
            if (transactionId <= 0) {
                return 0;
            }

            int updated = debtLoanDao.update(debtLoan);
            if (updated <= 0) {
                return 0;
            }

            int balanceUpdated = updatePaymentMoneyBalance(debtLoan, transaction.walletId, transaction.amount, transaction.convertedAmount);
            if (balanceUpdated <= 0) {
                return 0;
            }

            return (int) transactionId;
        } catch (Exception e) {
            AppLogger.e(getClass(), "saveFlexiblePaymentTransaction", e);
            return 0;
        }
    }

    public boolean deleteDebtLoan(int debtLoanId) {
        try {
            // 1. Get the existing Debt / Loan
            DebtLoanEntity debtLoan = debtLoanDao.getDebtLoanByIdSync(debtLoanId);

            if (debtLoan == null) {
                return false;
            }

            // 2. Get all payment transactions BEFORE deleting anything
            List<DebtLoanPaymentTransactionEntity> transactions = debtLoanPaymentTransactionDao.getTransactionsByDebtLoanId(debtLoanId);

            // 3. Reverse every payment transaction
            if (transactions != null && !transactions.isEmpty()) {
                for (DebtLoanPaymentTransactionEntity transaction : transactions) {
                    int reverseResult = reversePaymentMoneyBalance(debtLoan, transaction.walletId, transaction.amount, transaction.convertedAmount);
                    if (reverseResult <= 0) {
                        return false;
                    }
                }
            }

            // 4. Reverse the original Debt / Loan amount
            if (debtLoan.isMoneyReceivedLent) {
                adjustInitialMoneyBalance(debtLoan, false);
            }

            // 5. Delete transaction attachments
            deleteDebtLoanAttachments(debtLoanId);

            // 6. Delete payment transactions
            debtLoanPaymentTransactionDao.deleteByDebtLoanId(debtLoanId);

            // 7. Delete scheduled payments
            debtLoanPaymentDao.deletePaymentsByDebtLoanId(debtLoanId, System.currentTimeMillis());

            // 8. Delete the Debt / Loan
            int deleted = debtLoanDao.delete(debtLoanId, System.currentTimeMillis());

            return deleted > 0;
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteDebtLoan", e);
            return false;
        }
    }

    public void updateMoneyBalance(DebtLoanEntity entity, boolean moneyReceived) {

        if (entity.walletId <= 0) {
            return;
        }

        WalletEntity wallet = walletDao.getWalletByWalletId(entity.walletId);
        if (wallet == null) {
            return;
        }

        double principal = entity.principalAmount;
        double walletAmount = wallet.amount;

        if (entity.type == DebtLoanType.BORROW) {
            if (moneyReceived) {
                walletAmount += principal;
            }
        } else if (entity.type == DebtLoanType.LENT) {
            if (moneyReceived) {
                walletAmount -= principal;
            }
        }

        walletDao.updateWalletById(wallet.id, walletAmount);

        AccountEntity account = accountDao.getAccountDetailById(wallet.accountId);
        if (account == null) {
            return;
        }

        double accountBalance = account.balance;
        if (entity.type == DebtLoanType.BORROW) {
            if (moneyReceived) {
                accountBalance += principal;
            }
        } else if (entity.type == DebtLoanType.LENT) {
            if (moneyReceived) {
                accountBalance -= principal;
            }
        }

        accountDao.updateAccountById(account.id, accountBalance);
    }

    public LiveData<DebtLoanWithDetails> getDebtLoanById(int id) {
        return debtLoanDao.getDebtLoanById(id, System.currentTimeMillis());
    }

    public List<DebtLoanWithDetails> getAllDebtLoans(int type, int currentSortType, int currentStatusType, int page, int pageSize) {
        int offset = page * pageSize;
        return debtLoanDao.getAllDebtLoans(type, currentSortType, currentStatusType, System.currentTimeMillis(), pageSize, offset);
    }

    public Long getEarliestReminderDate() {

        List<DebtLoanEntity> loans = debtLoanDao.getLoansWithReminders();

        if (loans == null || loans.isEmpty()) {
            return null;
        }

        long now = System.currentTimeMillis();
        Long earliestReminderDate = null;

        for (DebtLoanEntity loan : loans) {
            try {
                if (loan.repaymentMethod == DebtLoanType.REPAYMENT_FLEXIBLE) {
                    if (loan.dueDate == null) {
                        continue;
                    }

                    long reminderDate = calculateReminderDate(loan.dueDate, loan.reminderDays, loan.reminderHour, loan.reminderMinute);

                    if (reminderDate < now) {
                        continue;
                    }

                    if (earliestReminderDate == null || reminderDate < earliestReminderDate) {
                        earliestReminderDate = reminderDate;
                    }
                } else {
                    List<DebtLoanPaymentEntity> payments = debtLoanPaymentDao.getPendingPayments(loan.id);

                    if (payments == null || payments.isEmpty()) {
                        continue;
                    }

                    for (DebtLoanPaymentEntity payment : payments) {
                        long reminderDate = calculateReminderDate(payment.paymentDate, loan.reminderDays, loan.reminderHour, loan.reminderMinute);

                        if (reminderDate < now) {
                            continue;
                        }

                        if (earliestReminderDate == null || reminderDate < earliestReminderDate) {

                            earliestReminderDate = reminderDate;
                        }
                    }
                }
            } catch (Exception e) {
                AppLogger.e(getClass(), "getEarliestReminderDate", e);
            }
        }

        return earliestReminderDate;
    }

    private long calculateReminderDate(long paymentDate, int reminderDays, int reminderHour, int reminderMinute) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(paymentDate);
        calendar.add(Calendar.DAY_OF_YEAR, -reminderDays);
        calendar.set(Calendar.HOUR_OF_DAY, reminderHour);
        calendar.set(Calendar.MINUTE, reminderMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public List<DebtLoanPaymentEntity> getDueReminderPayments(long now) {

        List<DebtLoanEntity> loans = debtLoanDao.getLoansWithReminders();

        List<DebtLoanPaymentEntity> duePayments = new ArrayList<>();

        if (loans == null || loans.isEmpty()) {
            return duePayments;
        }

        for (DebtLoanEntity loan : loans) {
            try {
                if (loan.repaymentMethod == DebtLoanType.REPAYMENT_FLEXIBLE) {
                    if (loan.dueDate == null) {
                        continue;
                    }

                    long reminderDate = calculateReminderDate(loan.dueDate, loan.reminderDays, loan.reminderHour, loan.reminderMinute);

                    if (isReminderDue(reminderDate, now)) {
                        DebtLoanPaymentEntity payment = new DebtLoanPaymentEntity();
                        payment.debtLoanId = loan.id;
                        payment.paymentNumber = 0;
                        payment.paymentDate = loan.dueDate;
                        duePayments.add(payment);
                    }

                } else {
                    List<DebtLoanPaymentEntity> payments = debtLoanPaymentDao.getPendingPayments(loan.id);
                    if (payments == null || payments.isEmpty()) {
                        continue;
                    }

                    for (DebtLoanPaymentEntity payment : payments) {
                        long reminderDate = calculateReminderDate(payment.paymentDate, loan.reminderDays, loan.reminderHour, loan.reminderMinute);
                        if (isReminderDue(reminderDate, now)) {
                            duePayments.add(payment);
                        }
                    }
                }
            } catch (Exception e) {
                AppLogger.e(getClass(), "getDueReminderPayments", e);
            }
        }

        return duePayments;
    }

    public int getTotalInstallments(int debtLoanId) {
        return debtLoanPaymentDao.getTotalInstallments(debtLoanId);
    }

    public DebtLoanPaymentEntity getNextPendingPayment(int debtLoanId) {
        return debtLoanPaymentDao.getNextPendingPayment(debtLoanId, DebtLoanPaymentEntity.PAYMENT_PENDING);
    }

    public LiveData<List<DebtLoanTransactionWithDetails>> getPaymentTransactionByDebtLoanId(int debtLoanId) {
        return debtLoanPaymentTransactionDao.getPaymentsByDebtLoanId(debtLoanId);
    }

    public LiveData<List<DebtLoanTransactionWithDetails>> getPaymentTransactionByDebtLoanId(int debtLoanId, int debtLoanPaymentId) {
        return debtLoanPaymentTransactionDao.getPaymentsByDebtLoanId(debtLoanId, debtLoanPaymentId);
    }

    public LiveData<List<DebtLoanPaymentWithDetails>> getPaymentsByDebtLoanId(int debtLoanId) {
        return debtLoanPaymentDao.getPaymentsByDebtLoanId(debtLoanId);
    }

    private boolean isReminderDue(long reminderDate, long now) {
        return reminderDate <= now;
    }

    private int updatePaymentMoneyBalance(DebtLoanEntity debtLoan, int walletId, double paymentAmount, double convertedAmount) {
        try {
            if (walletId <= 0 || paymentAmount <= 0) {
                return 0;
            }

            WalletEntity wallet = walletDao.getWalletByWalletId(walletId);
            if (wallet == null) {
                return 0;
            }

            double walletAmount = wallet.amount;
            if (debtLoan.type == DebtLoanType.BORROW) {
                walletAmount -= paymentAmount;
            } else if (debtLoan.type == DebtLoanType.LENT) {
                walletAmount += paymentAmount;
            } else {
                return 0;
            }

            int walletUpdated = walletDao.updateWalletBalanceById(wallet.id, walletAmount);
            if (walletUpdated <= 0) {
                return 0;
            }

            AccountEntity account = accountDao.getAccountDetailById(wallet.accountId);
            if (account == null) {
                return 0;
            }

            double accountBalance = account.balance;
            if (debtLoan.type == DebtLoanType.BORROW) {
                accountBalance -= convertedAmount;
            } else if (debtLoan.type == DebtLoanType.LENT) {
                accountBalance += convertedAmount;
            }

            int accountUpdated = accountDao.updateAccountBalanceById(account.id, accountBalance);
            return accountUpdated > 0 ? 1 : 0;
        } catch (Exception e) {
            AppLogger.e(getClass(), "updatePaymentMoneyBalance", e);
            return 0;
        }
    }

    public void saveTransactionAttachment(List<DebtLoanTransactionAttachmentEntity> transactionAttachments) {
        debtLoanTransactionAttachmentDao.insert(transactionAttachments);
    }

    public LiveData<DebtLoanTransactionWithDetails> getPaymentDetailById(int debtLoanTransactionId, int debtLoanId, int debtLoanPaymentId) {
        return debtLoanPaymentTransactionDao.getPaymentDetailById(debtLoanTransactionId, debtLoanId, debtLoanPaymentId);
    }

    public List<DebtLoanTransactionAttachmentEntity> getAttachments(int debtLoanId, int debtLoanPaymentId, int debtLoanTransactionId) {
        return debtLoanTransactionAttachmentDao.getAttachments(debtLoanId, debtLoanPaymentId, debtLoanTransactionId);
    }

    public LiveData<DebtLoanPaymentWithDetails> getPaymentDetailByDebtLoanId(int debtLoanId, int debtLoanPaymentId) {
        return debtLoanPaymentDao.getPaymentDetailByDebtLoanId(debtLoanId, debtLoanPaymentId);
    }

    public int updateDebtLoanAndPayments(DebtLoanEntity entity, List<DebtLoanPaymentEntity> newPayments) {
        try {

            // Get the original loan before updating it
            DebtLoanEntity existingDebtLoan = debtLoanDao.getDebtLoanByIdSync(entity.id);

            if (existingDebtLoan == null) {
                return 0;
            }

            if (existingDebtLoan.isMoneyReceivedLent) {
                adjustInitialMoneyBalance(existingDebtLoan, false);
            }

            if (entity.isMoneyReceivedLent) {
                adjustInitialMoneyBalance(entity, true);
            }

            // Update main Debt/Loan
            int loanUpdated = debtLoanDao.update(entity);

            if (loanUpdated <= 0) {
                return 0;
            }

            // Flexible repayment has no scheduled payments
            if (entity.repaymentMethod == DebtLoanType.REPAYMENT_FLEXIBLE) {
                return 1;
            }

            List<DebtLoanPaymentEntity> existingPayments =
                    debtLoanPaymentDao.getPaymentsByDebtLoanIdSync(entity.id);

            if (existingPayments == null || existingPayments.isEmpty()) {

                if (newPayments != null && !newPayments.isEmpty()) {
                    for (DebtLoanPaymentEntity payment : newPayments) {
                        payment.debtLoanId = entity.id;
                    }

                    debtLoanPaymentDao.insertAll(newPayments);
                }

                return 1;
            }

            if (newPayments == null) {
                newPayments = new ArrayList<>();
            }

            int count = Math.min(existingPayments.size(), newPayments.size());

            for (int i = 0; i < count; i++) {

                DebtLoanPaymentEntity existing = existingPayments.get(i);
                DebtLoanPaymentEntity updated = newPayments.get(i);

                updated.id = existing.id;
                updated.debtLoanId = entity.id;
                updated.createdAt = existing.createdAt;

                // Preserve actual payment information
                updated.paidAmount = existing.paidAmount;
                updated.paidDate = existing.paidDate;

                // Recalculate status
                if (updated.paidAmount >= updated.paymentAmount) {
                    updated.paidAmount = updated.paymentAmount;
                    updated.status = DebtLoanPaymentEntity.PAYMENT_PAID;
                } else {
                    updated.status = DebtLoanPaymentEntity.PAYMENT_PENDING;
                    updated.paidDate = null;
                }

                updated.updatedAt = System.currentTimeMillis();
                debtLoanPaymentDao.update(updated);
            }

            // New installments added
            if (newPayments.size() > existingPayments.size()) {
                for (int i = existingPayments.size();
                     i < newPayments.size();
                     i++) {

                    DebtLoanPaymentEntity payment = newPayments.get(i);
                    payment.debtLoanId = entity.id;
                    payment.updatedAt = System.currentTimeMillis();

                    debtLoanPaymentDao.insert(payment);
                }
            }

            // Old installments removed
            if (newPayments.size() < existingPayments.size()) {
                for (int i = newPayments.size(); i < existingPayments.size(); i++) {
                    DebtLoanPaymentEntity existing = existingPayments.get(i);

                    if (existing.paidAmount <= 0) {
                        existing.isDeleted = true;
                        existing.updatedAt = System.currentTimeMillis();
                        debtLoanPaymentDao.update(existing);
                    }
                }
            }
            return 1;
        } catch (Exception e) {
            AppLogger.e(getClass(), "updateDebtLoanAndPayments", e);
            return 0;
        }
    }

    private void adjustInitialMoneyBalance(DebtLoanEntity entity, boolean apply) {
        try {
            if (entity.walletId <= 0) {
                return;
            }

            WalletEntity wallet = walletDao.getWalletByWalletId(entity.walletId);

            if (wallet == null) {
                return;
            }

            double principal = entity.principalAmount;

            double walletDelta;
            if (entity.type == DebtLoanType.BORROW) {
                walletDelta = apply ? principal : -principal;
            } else if (entity.type == DebtLoanType.LENT) {
                walletDelta = apply ? -principal : principal;
            } else {
                return;
            }

            double walletAmount = wallet.amount + walletDelta;
            int walletUpdated = walletDao.updateWalletBalanceById(wallet.id, walletAmount);
            if (walletUpdated <= 0) {
                return;
            }

            // Update account using the same movement
            AccountEntity account = accountDao.getAccountDetailById(wallet.accountId);
            if (account == null) {
                return;
            }

            double accountBalance = account.balance + walletDelta;
            accountDao.updateAccountBalanceById(account.id, accountBalance);
        } catch (Exception e) {
            AppLogger.e(getClass(), "adjustInitialMoneyBalance", e);
        }
    }

    public int updatePaymentTransaction(DebtLoanPaymentTransactionEntity transaction) {
        try {
            // =====================================================
            // 1. GET EXISTING TRANSACTION
            // =====================================================
            DebtLoanPaymentTransactionEntity existingTransaction = debtLoanPaymentTransactionDao.getTransactionById(transaction.id, transaction.debtLoanId);
            if (existingTransaction == null) {
                return 0;
            }

            // =====================================================
            // 2. GET DEBT / LOAN
            // =====================================================
            DebtLoanEntity debtLoan = debtLoanDao.getDebtLoanByIdSync(transaction.debtLoanId);
            if (debtLoan == null) {
                return 0;
            }

            // =====================================================
            // 3. REVERSE OLD WALLET / ACCOUNT BALANCE
            // =====================================================
            int reverseResult = reversePaymentMoneyBalance(debtLoan, existingTransaction.walletId, existingTransaction.amount, existingTransaction.convertedAmount);
            if (reverseResult <= 0) {
                return 0;
            }

            // =====================================================
            // 4. UPDATE TRANSACTION
            // =====================================================
            transaction.updatedAt = System.currentTimeMillis();
            transaction.isDeleted = false;

            int transactionUpdated = debtLoanPaymentTransactionDao.update(transaction);
            if (transactionUpdated <= 0) {
                return 0;
            }

            // =====================================================
            // 5. RECALCULATE INSTALLMENT
            // =====================================================
            if (transaction.debtLoanPaymentId > 0) {
                DebtLoanPaymentEntity payment = debtLoanPaymentDao.getPaymentById(transaction.debtLoanPaymentId);

                if (payment == null) {
                    return 0;
                }

                double paidAmount = debtLoanPaymentTransactionDao.getTotalPaidForPayment(payment.id);
                payment.paidAmount = paidAmount;

                if (paidAmount >= payment.paymentAmount) {
                    payment.paidAmount = payment.paymentAmount;
                    payment.status = DebtLoanPaymentEntity.PAYMENT_PAID;
                    payment.paidDate = transaction.paymentDate;
                } else {
                    payment.status = DebtLoanPaymentEntity.PAYMENT_PENDING;
                    payment.paidDate = null;
                }

                payment.updatedAt = System.currentTimeMillis();

                int paymentUpdated = debtLoanPaymentDao.update(payment);
                if (paymentUpdated <= 0) {
                    return 0;
                }
            }

            // =====================================================
            // 6. RECALCULATE DEBT / LOAN PAID AMOUNT
            // =====================================================
            double totalPaid = debtLoanPaymentTransactionDao.getTotalPaidForDebtLoan(debtLoan.id);
            debtLoan.paidAmount = totalPaid;
            debtLoan.remainingAmount = Math.max(0, debtLoan.totalAmount - totalPaid);
            debtLoan.updatedAt = System.currentTimeMillis();

            int debtLoanUpdated = debtLoanDao.update(debtLoan);
            if (debtLoanUpdated <= 0) {
                return 0;
            }

            // =====================================================
            // 7. APPLY NEW WALLET / ACCOUNT BALANCE
            // =====================================================
            int balanceResult = updatePaymentMoneyBalance(debtLoan, transaction.walletId, transaction.amount, transaction.convertedAmount);
            if (balanceResult <= 0) {
                return 0;
            }

            return 1;
        } catch (Exception e) {
            AppLogger.e(getClass(), "updatePaymentTransaction", e);
            return 0;
        }
    }

    private int reversePaymentMoneyBalance(DebtLoanEntity debtLoan, int walletId, double paymentAmount, double convertedAmount) {
        try {
            if (walletId <= 0 || paymentAmount <= 0) {
                return 0;
            }

            WalletEntity wallet = walletDao.getWalletByWalletId(walletId);
            if (wallet == null) {
                return 0;
            }

            // =====================================================
            // REVERSE WALLET BALANCE
            // =====================================================
            double walletAmount = wallet.amount;
            if (debtLoan.type == DebtLoanType.BORROW) {
                walletAmount += paymentAmount;
            } else if (debtLoan.type == DebtLoanType.LENT) {
                walletAmount -= paymentAmount;
            } else {
                return 0;
            }

            int walletUpdated = walletDao.updateWalletBalanceById(wallet.id, walletAmount);
            if (walletUpdated <= 0) {
                return 0;
            }

            // =====================================================
            // REVERSE ACCOUNT BALANCE
            // =====================================================
            AccountEntity account = accountDao.getAccountDetailById(wallet.accountId);
            if (account == null) {
                return 0;
            }

            double accountBalance = account.balance;
            if (debtLoan.type == DebtLoanType.BORROW) {
                accountBalance += convertedAmount;
            } else if (debtLoan.type == DebtLoanType.LENT) {
                accountBalance -= convertedAmount;
            }

            int accountUpdated = accountDao.updateAccountBalanceById(account.id, accountBalance);
            return accountUpdated > 0 ? 1 : 0;
        } catch (Exception e) {
            AppLogger.e(getClass(), "reversePaymentMoneyBalance", e);
            return 0;
        }
    }

    public LiveData<DebtLoanPaymentTransactionEntity> getTransactionById(int transactionId, int debtLoanId) {
        return debtLoanPaymentTransactionDao.getTransactionDetailById(transactionId, debtLoanId);
    }

    public int deletePaymentTransaction(int transactionId, int debtLoanId, int debeLoanPaymentId) {
        try {
            // -------------------------------------------------
            // 1. Get existing transaction
            // -------------------------------------------------
            DebtLoanPaymentTransactionEntity transaction = debtLoanPaymentTransactionDao.getTransactionByIdSync(transactionId, debtLoanId, debeLoanPaymentId);
            if (transaction == null) {
                return 0;
            }

            // -------------------------------------------------
            // 2. Get parent Debt / Loan
            // -------------------------------------------------
            DebtLoanEntity debtLoan = debtLoanDao.getDebtLoanByIdSync(debtLoanId);
            if (debtLoan == null) {
                return 0;
            }

            // -------------------------------------------------
            // 3. Get attachments BEFORE deleting DB records
            // -------------------------------------------------
            List<DebtLoanTransactionAttachmentEntity> attachments = debtLoanTransactionAttachmentDao.getAttachments(debtLoanId, transaction.debtLoanPaymentId,
                    transaction.id);

            // -------------------------------------------------
            // 4. Reverse wallet + account balance
            // -------------------------------------------------
            int reverseResult = reversePaymentMoneyBalance(debtLoan, transaction.walletId, transaction.amount, transaction.convertedAmount);
            if (reverseResult <= 0) {
                return 0;
            }

            // -------------------------------------------------
            // 5. Soft-delete payment transaction
            // -------------------------------------------------
            int deleted = debtLoanPaymentTransactionDao.softDelete(debtLoanId, debeLoanPaymentId, transactionId, System.currentTimeMillis());
            if (deleted <= 0) {
                return 0;
            }

            // -------------------------------------------------
            // 6. Delete physical attachment files
            // -------------------------------------------------
            if (attachments != null && !attachments.isEmpty()) {
                for (DebtLoanTransactionAttachmentEntity attachment : attachments) {
                    if (attachment == null) {
                        continue;
                    }

                    if (attachment.attachmentPath == null || attachment.attachmentPath.trim().isEmpty()) {
                        continue;
                    }

                    try {
                        File file = new File(attachment.attachmentPath);
                        if (file.exists() && file.isFile()) {
                            if (!file.delete()) {
                                AppLogger.d(getClass(), "deleteDebtLoanAttachments - failed to delete file: " + file.getAbsolutePath());
                            }
                        }
                    } catch (Exception e) {
                        AppLogger.e(getClass(), "deletePaymentTransaction - delete attachment file", e);
                    }
                }
            }

            // -------------------------------------------------
            // 7. Delete attachment DB records
            // -------------------------------------------------
            debtLoanTransactionAttachmentDao.deleteByTransaction(debtLoanId, transaction.debtLoanPaymentId, transaction.id);

            // -------------------------------------------------
            // 8. Delete empty attachment folder
            // -------------------------------------------------
            deleteEmptyAttachmentFolder(attachments);

            // -------------------------------------------------
            // 9. Recalculate installment
            // -------------------------------------------------
            if (transaction.debtLoanPaymentId > 0) {
                DebtLoanPaymentEntity payment = debtLoanPaymentDao.getPaymentById(transaction.debtLoanPaymentId);

                if (payment != null) {
                    double paidAmount = debtLoanPaymentTransactionDao.getTotalPaidForPayment(payment.id);
                    payment.paidAmount = paidAmount;

                    if (paidAmount >= payment.paymentAmount) {
                        payment.paidAmount = payment.paymentAmount;
                        payment.status = DebtLoanPaymentEntity.PAYMENT_PAID;

                        // Find latest remaining transaction
                        List<DebtLoanPaymentTransactionEntity> remainingTransactions = debtLoanPaymentTransactionDao.getTransactionsByPaymentId(payment.id);
                        if (remainingTransactions != null && !remainingTransactions.isEmpty()) {
                            payment.paidDate = remainingTransactions.get(remainingTransactions.size() - 1).paymentDate;
                        } else {
                            payment.paidDate = null;
                        }
                    } else {
                        payment.status = DebtLoanPaymentEntity.PAYMENT_PENDING;
                        payment.paidDate = null;
                    }
                    payment.updatedAt = System.currentTimeMillis();

                    int paymentUpdated = debtLoanPaymentDao.update(payment);
                    if (paymentUpdated <= 0) {
                        return 0;
                    }
                }
            }

            // -------------------------------------------------
            // 10. Recalculate parent Debt / Loan
            // -------------------------------------------------
            double totalPaid = debtLoanPaymentTransactionDao.getTotalPaidForDebtLoan(debtLoanId);
            debtLoan.paidAmount = totalPaid;
            debtLoan.remainingAmount = Math.max(0, debtLoan.totalAmount - totalPaid);
            debtLoan.updatedAt = System.currentTimeMillis();

            int debtLoanUpdated = debtLoanDao.update(debtLoan);
            if (debtLoanUpdated <= 0) {
                return 0;
            }
            return 1;
        } catch (Exception e) {
            AppLogger.e(getClass(), "deletePaymentTransaction", e);
            return 0;
        }
    }

    private void deleteEmptyAttachmentFolder(List<DebtLoanTransactionAttachmentEntity> attachments) {
        try {
            if (attachments == null || attachments.isEmpty()) {
                return;
            }

            HashSet<String> parentPaths = getParentPaths(attachments);

            for (String parentPath : parentPaths) {
                File folder = new File(parentPath);
                if (!folder.exists() || !folder.isDirectory()) {
                    continue;
                }
                File[] remainingFiles = folder.listFiles();
                if (remainingFiles == null || remainingFiles.length == 0) {
                    if (!folder.delete()) {
                        AppLogger.d(getClass(), "deleteDebtLoanAttachments - failed to delete folder: " + folder.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteEmptyAttachmentFolder", e);
        }
    }

    @NonNull
    private static HashSet<String> getParentPaths(List<DebtLoanTransactionAttachmentEntity> attachments) {
        HashSet<String> parentPaths = new HashSet<>();
        for (DebtLoanTransactionAttachmentEntity attachment : attachments) {

            if (attachment == null || attachment.attachmentPath == null || attachment.attachmentPath.trim().isEmpty()) {
                continue;
            }

            File file = new File(attachment.attachmentPath);
            File parent = file.getParentFile();
            if (parent != null) {
                parentPaths.add(parent.getAbsolutePath());
            }
        }
        return parentPaths;
    }

    public void deleteAttachment(String attachmentPath, String tempDebtLoanServerId, int debtLoanId, int debtLoanPaymentId, int debtLoanPaymentTransactionId) {
        debtLoanTransactionAttachmentDao.deleteAttachment(attachmentPath, tempDebtLoanServerId, debtLoanId, debtLoanPaymentId, debtLoanPaymentTransactionId);
    }

    public LiveData<DebtLoanPaymentEntity> getPaymentDataById(int paymentId) {
        return debtLoanPaymentDao.getPaymentDataById(paymentId);
    }

    private void deleteDebtLoanAttachments(int debtLoanId) {
        try {
            List<DebtLoanTransactionAttachmentEntity> attachments = debtLoanTransactionAttachmentDao.getAttachmentsByDebtLoanId(debtLoanId);
            if (attachments == null || attachments.isEmpty()) {
                return;
            }

            HashSet<String> parentPaths = new HashSet<>();

            for (DebtLoanTransactionAttachmentEntity attachment : attachments) {
                if (attachment == null || attachment.attachmentPath == null || attachment.attachmentPath.trim().isEmpty()) {
                    continue;
                }

                File file = new File(attachment.attachmentPath);

                File parent = file.getParentFile();
                if (parent != null) {
                    parentPaths.add(parent.getAbsolutePath());
                }

                // Delete physical file
                try {
                    if (file.exists() && file.isFile()) {
                        if (!file.delete()) {
                            AppLogger.d(getClass(), "deleteDebtLoanAttachments - failed to delete file: " + file.getAbsolutePath());
                        }
                    }
                } catch (Exception e) {
                    AppLogger.e(getClass(), "deleteDebtLoanAttachments - delete file", e);
                }
            }

            // Delete empty payment folders
            for (String parentPath : parentPaths) {
                try {
                    File folder = new File(parentPath);
                    if (!folder.exists() || !folder.isDirectory()) {
                        continue;
                    }
                    File[] remainingFiles = folder.listFiles();
                    if (remainingFiles == null || remainingFiles.length == 0) {
                        if (!folder.delete()) {
                            AppLogger.d(getClass(), "deleteDebtLoanAttachments - failed to delete folder: " + folder.getAbsolutePath());
                        }
                    }
                } catch (Exception e) {
                    AppLogger.e(getClass(), "deleteDebtLoanAttachments - delete folder", e);
                }
            }

            // Delete attachment database records
            debtLoanTransactionAttachmentDao.deleteByDebtLoanId(debtLoanId);
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteDebtLoanAttachments", e);
        }
    }
}