package com.nprotech.moneytracker.repositories;

import androidx.lifecycle.LiveData;

import com.nprotech.moneytracker.db.dao.AccountCurrencyMappingDao;
import com.nprotech.moneytracker.db.dao.AccountDao;
import com.nprotech.moneytracker.db.entites.AccountCurrencyMappingEntity;
import com.nprotech.moneytracker.db.entites.AccountEntity;

import java.util.List;

public class AccountRepository {

    private final AccountDao accountDao;
    private final AccountCurrencyMappingDao accountCurrencyMappingDao;

    public AccountRepository(AccountDao accountDao, AccountCurrencyMappingDao accountCurrencyMappingDao) {
        this.accountDao = accountDao;
        this.accountCurrencyMappingDao = accountCurrencyMappingDao;
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

    public void saveAccountCurrencyMapping(AccountCurrencyMappingEntity accountCurrencyMapping) {
        accountCurrencyMappingDao.insert(accountCurrencyMapping);
    }

    public void updateMapping(AccountCurrencyMappingEntity accountCurrencyMapping) {
        accountCurrencyMappingDao.update(accountCurrencyMapping);
    }

    public LiveData<List<AccountCurrencyMappingEntity>> getAccountCurrencyByAccountId(int accountId) {
        return accountCurrencyMappingDao.getAccountCurrencyByAccountId(accountId);
    }

    public int getLastAccountOrder() {
        return accountDao.getLastAccountOrder();
    }

    public void updateAccount(AccountEntity account) {
        accountDao.updateAccount(account);
    }

    public List<AccountCurrencyMappingEntity> fetchAccountCurrencyByAccountId(int accountId, int page, int pageSize) {
        int offset = page * pageSize;
        return accountCurrencyMappingDao.fetchAccountCurrencyByAccountId(accountId, pageSize, offset);
    }

    public AccountCurrencyMappingEntity fetchAccountBaseCurrencyByAccountId(int accountId) {
        return accountCurrencyMappingDao.fetchAccountBaseCurrencyByAccountId(accountId);
    }

    public AccountCurrencyMappingEntity fetchAccountCurrencyByMappingId(int currencyId, int accountId) {
        return accountCurrencyMappingDao.fetchAccountCurrencyByMappingId(currencyId, accountId);
    }

    public boolean updateAccountCurrencyMapping(int accountId, int currencyId, String currencyCode) {
        return accountCurrencyMappingDao.updateAccountCurrencyMapping(accountId, currencyId, currencyCode) > 0;
    }
}