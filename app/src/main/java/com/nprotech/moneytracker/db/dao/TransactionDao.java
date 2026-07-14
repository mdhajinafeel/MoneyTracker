package com.nprotech.moneytracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.models.TransactionCategoryModel;
import com.nprotech.moneytracker.models.TransactionTypeAmountModel;
import com.nprotech.moneytracker.models.TransactionWithDetails;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TransactionEntity transactionEntity);

    @Update
    int update(TransactionEntity transactionEntity);

    @Query("SELECT * FROM transactions")
    LiveData<List<TransactionEntity>> getAllTransactions();

    @Query("SELECT * FROM transactions WHERE tempTransactionServerId = :tempTransactionServerId")
    TransactionEntity getTransactionById(String tempTransactionServerId);

    @Query("SELECT t.*, w.currencySymbol AS currencySymbol, c.color, c1.name AS categoryName, CASE WHEN c.icon IS NULL THEN c1.icon ELSE c.icon END AS icon, " +
            "w.name AS walletName " +
            "FROM transactions t " +
            "JOIN wallets w ON w.id=t.walletId JOIN categories c ON c.defaultCategory = t.defaultCategoryId " +
            "LEFT JOIN categories c1 ON c1.id = t.categoryId " +
            "WHERE t.accountId= :accountId AND w.accountId= :accountId AND t.type IN (1, 2) " +
            "ORDER BY t.transactionDate DESC")
    LiveData<List<TransactionWithDetails>> getTransactions(int accountId);

    @Query("SELECT type, SUM(amount) as amount FROM transactions WHERE walletId = :walletId AND isDeleted = 0 GROUP BY type")
    List<TransactionTypeAmountModel> getTransactionAmountByType(int walletId);

    @Query("SELECT t.defaultCategoryId, COUNT(t.tempTransactionServerId) AS transactionCount, SUM(amount) as amount, c.color, c.icon " +
            "FROM transactions t " +
            "LEFT JOIN categories c ON c.defaultCategory = t.defaultCategoryId " +
            "WHERE walletId = :walletId AND isDeleted = 0 GROUP BY t.defaultCategoryId")
    LiveData<List<TransactionCategoryModel>> getTransactionAmountByCategory(int walletId);
}