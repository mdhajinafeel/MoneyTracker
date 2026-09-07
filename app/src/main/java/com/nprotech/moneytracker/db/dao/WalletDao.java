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

    @Query("SELECT * FROM wallets WHERE accountId = :accountId AND isDeleted = 0 AND isArchived = 0 ORDER BY isDefault DESC")
    LiveData<List<WalletEntity>> getAllWallets(int accountId);

    @Query("SELECT * FROM wallets WHERE accountId = :accountId AND isDeleted = 0 AND isArchived = 1 ORDER BY archivedAt DESC")
    LiveData<List<WalletEntity>> getArchivedWallets(int accountId);

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

    @Query("SELECT * FROM wallets WHERE accountId = :accountId AND currencyCode = :currencyCode AND isActive = 1 AND isDeleted = 0")
    List<WalletEntity> getWalletsByAccountAndCurrency(int accountId, String currencyCode);

    @Query("SELECT IFNULL(SUM(amount * exchangeRate), 0) FROM wallets WHERE accountId = :accountId AND isDeleted = 0 AND isActive = 1")
    double getAccountBalance(int accountId);

    @Query("UPDATE wallets SET isDefault = 0 WHERE accountId = :accountId")
    void clearDefaultWallet(int accountId);

    @Query("UPDATE wallets SET isDefault = 1 WHERE id = :walletId AND accountId = :accountId")
    void setDefaultWallet(int walletId, int accountId);

    @Query("UPDATE wallets SET archivedAt = :archivedAt, isArchived = :isArchived WHERE id = :walletId AND accountId = :accountId")
    void archiveWallet(int walletId, int accountId, boolean isArchived, long archivedAt);

    @Query("UPDATE wallets SET isDeleted = 1 WHERE id = :walletId AND accountId = :accountId AND isDefault = 0")
    void deleteWallet(int walletId, int accountId);
}