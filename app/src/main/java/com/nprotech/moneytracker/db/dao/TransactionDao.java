package com.nprotech.moneytracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.models.TransactionWithCurrency;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TransactionEntity transactionEntity);

    @Query("SELECT * FROM transactions")
    LiveData<List<TransactionEntity>> getAllTransactions();

    @Query("SELECT * FROM transactions WHERE tempTransactionServerId = :tempTransactionServerId")
    TransactionEntity getTransactionById(String tempTransactionServerId);

    @Query("SELECT t.*, w.currencySymbol AS currencySymbol, c.color, c1.name AS categoryName, CASE WHEN c.icon IS NULL THEN c1.icon ELSE c.icon END AS icon " +
            "FROM transactions t " +
            "JOIN wallets w ON w.id=t.walletId JOIN categories c ON c.defaultCategory = t.defaultCategoryId " +
            "LEFT JOIN categories c1 ON c1.id = t.categoryId " +
            "WHERE t.accountId= :accountId AND w.accountId= :accountId AND t.type= :type " +
            "ORDER BY t.transactionDate DESC")
    LiveData<List<TransactionWithCurrency>> getTransactions(int accountId, int type);
}