package com.nprotech.moneytracker.repositories;

import androidx.lifecycle.LiveData;

import com.nprotech.moneytracker.db.dao.AccountDao;
import com.nprotech.moneytracker.db.entites.AccountEntity;

import java.util.List;

public class AccountRepository {

    private final AccountDao accountDao;

    public AccountRepository(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public LiveData<List<AccountEntity>> getAllAccounts() {
        return accountDao.getAllAccounts();
    }

    public LiveData<AccountEntity> getAccountById(int accountId) {
        return accountDao.getAccountById(accountId);
    }

    public AccountEntity getAccountDetailById(int accountId) {
        return accountDao.getAccountDetailById(accountId);
    }

    public long saveAccount(AccountEntity account) {
        return accountDao.insert(account);
    }

    public int getLastAccountOrder() {
        return accountDao.getLastAccountOrder();
    }
}