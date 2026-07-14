package com.nprotech.moneytracker.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nprotech.moneytracker.db.entites.CurrencyEntity;

import java.util.List;

@Dao
public interface CurrencyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CurrencyEntity> list);

    @Query("SELECT * FROM currencies ORDER BY code")
    List<CurrencyEntity> getAllCurrencies();

    @Query("SELECT COUNT(*) FROM currencies")
    int getCurrencyCount();

    @Query("SELECT * FROM currencies WHERE isDefault = 1")
    CurrencyEntity getDefaultCurrency();

    @Query("SELECT * FROM currencies WHERE code = :currencyCode")
    CurrencyEntity getCurrencyByCode(String currencyCode);

    @Query("SELECT * FROM currencies WHERE id NOT IN (SELECT currencyId FROM account_currency_mapping WHERE accountId = :accountId) LIMIT 1")
    CurrencyEntity getFirstCurrencyForWallet(int accountId);

    @Query("SELECT * FROM currencies WHERE id NOT IN (SELECT currencyId FROM account_currency_mapping WHERE accountId = :accountId)")
    List<CurrencyEntity> getCurrenciesForWallet(int accountId);
}