package com.nprotech.moneytracker.repositories;

import androidx.lifecycle.LiveData;

import com.nprotech.moneytracker.db.MoneyTrackerDatabase;
import com.nprotech.moneytracker.db.dao.AccountDao;
import com.nprotech.moneytracker.db.dao.TransactionAttachmentDao;
import com.nprotech.moneytracker.db.dao.TransactionDao;
import com.nprotech.moneytracker.db.dao.WalletDao;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.TransactionAttachmentEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.models.BalanceSummaryModel;
import com.nprotech.moneytracker.models.CalendarSummaryModel;
import com.nprotech.moneytracker.models.CategoryExpenseModel;
import com.nprotech.moneytracker.models.TransactionCategoryModel;
import com.nprotech.moneytracker.models.TransactionTypeAmountModel;
import com.nprotech.moneytracker.models.TransactionWithDetails;

import java.util.List;

public class TransactionRepository {

    private final AccountDao accountDao;
    private final WalletDao walletDao;
    private final TransactionDao transactionDao;
    private final TransactionAttachmentDao transactionAttachmentDao;
    private final MoneyTrackerDatabase database;

    public TransactionRepository(MoneyTrackerDatabase database, AccountDao accountDao, WalletDao walletDao, TransactionDao transactionDao,
                                 TransactionAttachmentDao transactionAttachmentDao) {
        this.database = database;
        this.accountDao = accountDao;
        this.walletDao = walletDao;
        this.transactionDao = transactionDao;
        this.transactionAttachmentDao = transactionAttachmentDao;
    }

    public LiveData<List<TransactionWithDetails>> getTransactions(int accountId, long startDate, long endDate) {
        if(startDate > 0 || endDate > 0) {
            return transactionDao.getTransactions(accountId, startDate, endDate);
        }
        return transactionDao.getTransactions(accountId);
    }

    public TransactionWithDetails getTransactions(String tempTransactionServerId) {
        return transactionDao.getTransactions(tempTransactionServerId);
    }

    public long saveTransaction(TransactionEntity transaction) {
        return transactionDao.insert(transaction);
    }

    public int updateTransaction(TransactionEntity transaction) {
        return transactionDao.update(transaction);
    }

    public TransactionEntity getTransactionById(String tempTransactionServerId) {
        return transactionDao.getTransactionById(tempTransactionServerId);
    }

    public void saveTransactionAttachment(List<TransactionAttachmentEntity> transactionAttachments) {
        transactionAttachmentDao.insert(transactionAttachments);
    }

    public void updateWallet(WalletEntity wallet) {
        walletDao.updateWalletById(wallet.id, wallet.amount);
    }

    public void updateAccount(AccountEntity account) {
        accountDao.updateAccountById(account.id, account.balance);
    }

    public List<TransactionAttachmentEntity> getTransactionAttachments(String tempTransactionServerId) {
        return transactionAttachmentDao.getAttachments(tempTransactionServerId);
    }

    public void deleteAttachment(String attachmentPath, String tempTransactionServerId) {
        transactionAttachmentDao.deleteAttachment(attachmentPath, tempTransactionServerId);
    }

    public List<TransactionTypeAmountModel> getTransactionAmountByType(int walletId) {
        return transactionDao.getTransactionAmountByType(walletId);
    }

    public LiveData<List<TransactionCategoryModel>> getTransactionAmountByCategory(int walletId) {
        return transactionDao.getTransactionAmountByCategory(walletId);
    }

    public boolean deleteTransaction(TransactionEntity transaction, WalletEntity wallet, AccountEntity account) {
        walletDao.updateWallet(wallet);
        accountDao.updateAccount(account);
        int rows = transactionDao.deleteTransaction(transaction.tempTransactionServerId, System.currentTimeMillis());
        return rows > 0;
    }

    public void saveTransferTransaction(TransactionEntity transaction, TransactionEntity feeTransactionActivity, WalletEntity fromWallet, WalletEntity toWallet, AccountEntity account) {

        database.runInTransaction(() -> {
            long transactionInserted = transactionDao.insert(transaction);
            if (transactionInserted > 0) {

                if (feeTransactionActivity != null) {
                    transactionDao.insert(feeTransactionActivity);
                }

                walletDao.updateWallet(fromWallet);
                walletDao.updateWallet(toWallet);
                accountDao.updateAccount(account);
            }
        });
    }

    public void updateTransferTransaction(TransactionEntity transferTransaction, TransactionEntity feeTransaction, TransactionEntity oldFeeTransaction,
                                          WalletEntity oldFromWallet, WalletEntity oldToWallet, WalletEntity newFromWallet, WalletEntity newToWallet,
                                          AccountEntity account) {

        database.runInTransaction(() -> {

            // Update transfer
            transactionDao.update(transferTransaction);

            // Fee transaction
            if (oldFeeTransaction == null && feeTransaction != null) {

                // New fee added
                transactionDao.insert(feeTransaction);

            } else if (oldFeeTransaction != null && feeTransaction == null) {

                // Fee removed
                transactionDao.delete(oldFeeTransaction);

            } else if (oldFeeTransaction != null) {

                // Fee updated
                transactionDao.update(feeTransaction);
            }

            // Restore old wallets
            walletDao.updateWallet(oldFromWallet);
            walletDao.updateWallet(oldToWallet);

            // Apply new wallets
            if (oldFromWallet.id != newFromWallet.id) {
                walletDao.updateWallet(newFromWallet);
            }

            if (oldToWallet.id != newToWallet.id) {
                walletDao.updateWallet(newToWallet);
            }

            accountDao.updateAccount(account);
        });
    }

    public TransactionEntity getFeeTransaction(String parentTransactionId) {
        return transactionDao.getFeeTransaction(parentTransactionId);
    }

    public void deleteFeeTransaction(TransactionEntity transaction) {
        database.runInTransaction(() -> {
            transaction.isDeleted = true;
            transaction.updatedAt = System.currentTimeMillis();
            transactionDao.update(transaction);
        });
    }

    public LiveData<List<CalendarSummaryModel>> getCalendarSummary(int accountId, long startDate, long endDate) {
        return transactionDao.getCalendarSummary(accountId, startDate, endDate);
    }

    public LiveData<CalendarSummaryModel> getCalendarHeader(int accountId, long startDate, long endDate) {
        return transactionDao.getCalendarHeader(accountId, startDate, endDate);
    }

    public LiveData<List<TransactionWithDetails>> getTransactionsForDay(int accountId, long start, long end) {
        return transactionDao.getTransactionsForDay(accountId, start, end);
    }

    public LiveData<BalanceSummaryModel> getBalanceSummary(int accountId, long start, long end) {
        return transactionDao.getBalanceSummary(accountId, start, end);
    }

    public LiveData<List<CategoryExpenseModel>> getExpenseByCategory(int accountId, long start, long end) {
        return transactionDao.getExpenseByCategory(accountId, start, end);
    }

    public LiveData<List<CategoryExpenseModel>> getIncomeByCategory(int accountId, long start, long end) {
        return transactionDao.getIncomeByCategory(accountId, start, end);
    }
}