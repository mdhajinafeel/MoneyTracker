package com.nprotech.moneytracker.repositories;

import androidx.lifecycle.LiveData;

import com.nprotech.moneytracker.db.dao.AccountDao;
import com.nprotech.moneytracker.db.dao.TransactionAttachmentDao;
import com.nprotech.moneytracker.db.dao.TransactionDao;
import com.nprotech.moneytracker.db.dao.WalletDao;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.TransactionAttachmentEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.models.DailyTransModel;

import java.util.List;

public class TransactionRepository {

    private final AccountDao accountDao;
    private final WalletDao walletDao;
    private final TransactionDao transactionDao;
    private final TransactionAttachmentDao transactionAttachmentDao;

    public TransactionRepository(AccountDao accountDao, WalletDao walletDao, TransactionDao transactionDao, TransactionAttachmentDao transactionAttachmentDao) {
        this.accountDao = accountDao;
        this.walletDao = walletDao;
        this.transactionDao = transactionDao;
        this.transactionAttachmentDao = transactionAttachmentDao;
    }

    public LiveData<List<DailyTransModel>> getDailyTransactionData(int accountId) {
        return transactionDao.getDailyTransactionData(accountId);
    }

    public long saveTransaction(TransactionEntity transaction) {
        return transactionDao.insert(transaction);
    }

    public TransactionEntity getTransactionById(String tempTransactionServerId){
        return transactionDao.getTransactionById(tempTransactionServerId);
    }

    public void saveTransactionAttachment(List<TransactionAttachmentEntity> transactionAttachments) {
        transactionAttachmentDao.insert(transactionAttachments);
    }

    public void updateWallet(WalletEntity wallet) {
        walletDao.updateWalletById(wallet.id, wallet.initialAmount);
    }

    public void updateAccount(AccountEntity account) {
        accountDao.updateAccountById(account.id, account.balance);
    }


}