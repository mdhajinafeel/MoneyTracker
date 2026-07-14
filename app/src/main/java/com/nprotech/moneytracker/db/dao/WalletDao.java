package com.nprotech.moneytracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.nprotech.moneytracker.db.entites.WalletEntity;

import java.util.List;

@Dao
public interface WalletDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(WalletEntity walletAccount);

    @Query("SELECT * FROM wallets WHERE accountId = :accountId AND isDeleted = 0 ORDER BY ordering")
    LiveData<List<WalletEntity>> getAllWallets(int accountId);

    @Query("SELECT * FROM wallets WHERE accountId = :accountId AND isDeleted = 0 ORDER BY ordering")
    List<WalletEntity> getWalletsByAccountId(int accountId);

    @Query("SELECT * FROM wallets WHERE id = :walletId")
    WalletEntity getWalletByWalletId(int walletId);

    @Query("UPDATE wallets SET amount = :amount WHERE id = :walletId")
    void updateWalletById(int walletId, double amount);

    @Update
    void updateWallet(WalletEntity wallet);

    @Query("SELECT IFNULL(MAX(ordering), 0) FROM wallets WHERE accountId = :accountId")
    int getMaxWalletOrdering(int accountId);
}