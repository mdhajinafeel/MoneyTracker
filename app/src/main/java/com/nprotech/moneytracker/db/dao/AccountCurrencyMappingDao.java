package com.nprotech.moneytracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nprotech.moneytracker.db.entites.AccountCurrencyMappingEntity;

import java.util.List;

@Dao
public interface AccountCurrencyMappingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AccountCurrencyMappingEntity accountCurrencyMappingEntity);

    @Query("SELECT * FROM account_currency_mapping WHERE accountId = :accountId")
    LiveData<List<AccountCurrencyMappingEntity>> getAccountCurrencyByAccountId(int accountId);
}