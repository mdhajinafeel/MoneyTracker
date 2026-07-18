package com.nprotech.moneytracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.nprotech.moneytracker.db.entites.AccountEntity;

import java.util.List;

@Dao
public interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AccountEntity accountEntity);

    @Query("SELECT * FROM accounts ORDER BY ordering")
    LiveData<List<AccountEntity>> getAllAccounts();

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    LiveData<AccountEntity> getAccountById(int accountId);

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    AccountEntity getAccountDetailById(int accountId);

    @Query("SELECT COALESCE(MAX(ordering), 0) FROM accounts")
    int getLastAccountOrder();

    @Query("UPDATE accounts SET balance = :amount WHERE id = :accountId")
    void updateAccountById(int accountId, double amount);

    @Update
    void updateAccount(AccountEntity account);
}