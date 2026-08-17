package com.nprotech.moneytracker.repositories;

import com.nprotech.moneytracker.db.dao.CurrencyDao;
import com.nprotech.moneytracker.db.entites.CurrencyEntity;

import java.util.List;

public class MasterRepository {

    private final CurrencyDao currencyDao;

    public MasterRepository(CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
    }

    public List<CurrencyEntity> getAllCurrencies() {
        return currencyDao.getAllCurrencies();
    }

    public CurrencyEntity getDefaultCurrency() {
        return currencyDao.getDefaultCurrency();
    }

    public CurrencyEntity getFirstCurrencyForWallet(int accountId) {
        return currencyDao.getFirstCurrencyForWallet(accountId);
    }

    public CurrencyEntity getFirstCurrencyForAccount(int accountId) {
        return currencyDao.getFirstCurrencyForAccount(accountId);
    }

    public List<CurrencyEntity> getCurrenciesForWallet(int accountId) {
        return currencyDao.getCurrenciesForWallet(accountId);
    }

    public CurrencyEntity getCurrencyByCode(String code) {
        return currencyDao.getCurrencyByCode(code);
    }
}