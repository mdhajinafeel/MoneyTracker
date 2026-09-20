package com.nprotech.moneytracker.repositories;

import androidx.lifecycle.LiveData;

import com.nprotech.moneytracker.constants.DebtLoanType;
import com.nprotech.moneytracker.db.dao.AccountDao;
import com.nprotech.moneytracker.db.dao.DebtLoanDao;
import com.nprotech.moneytracker.db.dao.DebtLoanPaymentDao;
import com.nprotech.moneytracker.db.dao.WalletDao;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanPaymentEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DebtLoanRepository {

    private final DebtLoanDao debtLoanDao;
    private final DebtLoanPaymentDao debtLoanPaymentDao;
    private final WalletDao walletDao;
    private final AccountDao accountDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DebtLoanRepository(DebtLoanDao debtLoanDao, DebtLoanPaymentDao debtLoanPaymentDao, WalletDao walletDao, AccountDao accountDao) {
        this.debtLoanDao = debtLoanDao;
        this.debtLoanPaymentDao = debtLoanPaymentDao;
        this.walletDao = walletDao;
        this.accountDao = accountDao;
    }

    public long saveDebtLoan(DebtLoanEntity entity, List<DebtLoanPaymentEntity> payments) {
        long debtLoanId = debtLoanDao.insert(entity);
        if (debtLoanId > 0 && payments != null && !payments.isEmpty()) {
            for (DebtLoanPaymentEntity payment : payments) {
                payment.debtLoanId = (int) debtLoanId;
                payment.tempDebtLoanServerId = entity.tempDebtLoanServerId;
            }
            debtLoanPaymentDao.insertAll(payments);
        }

        updateMoneyBalance(entity, entity.isMoneyReceivedLent);

        return debtLoanId;
    }

    public void updateDebtLoan(DebtLoanEntity entity) {
        executor.execute(() -> debtLoanDao.update(entity));
    }

    public void deleteDebtLoan(DebtLoanEntity entity) {
        executor.execute(() -> debtLoanDao.delete(entity.id, System.currentTimeMillis()));
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

    public LiveData<DebtLoanEntity> getDebtLoanById(long id) {
        return debtLoanDao.getDebtLoanById(id);
    }

    public LiveData<List<DebtLoanEntity>> getAllDebtLoans() {
        return debtLoanDao.getAllDebtLoans();
    }

    public void shutdown() {
        executor.shutdown();
    }
}