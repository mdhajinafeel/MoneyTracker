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

    @Query("SELECT * FROM transactions WHERE tempTransactionServerId = :tempTransactionServerId")
    TransactionEntity getTransactionById(String tempTransactionServerId);

    @Query("SELECT t.*, w.currencySymbol AS currencySymbol, c.color, c1.name AS categoryName, CASE WHEN c.icon IS NULL THEN c1.icon ELSE c.icon END AS icon, " +
            "w.name AS walletName " +
            "FROM transactions t " +
            "JOIN wallets w ON w.id=t.walletId JOIN categories c ON c.defaultCategory = t.defaultCategoryId AND c.type = t.type " +
            "LEFT JOIN categories c1 ON c1.id = t.categoryId AND c1.type = t.type " +
            "WHERE t.isDeleted = 0 AND t.accountId= :accountId AND w.accountId= :accountId AND t.type IN (1, 2) " +
            "ORDER BY t.transactionDate DESC")
    LiveData<List<TransactionWithDetails>> getTransactions(int accountId);

    @Query("SELECT t.*, w.currencySymbol AS currencySymbol, c.color, c1.name AS categoryName, CASE WHEN c.icon IS NULL THEN c1.icon ELSE c.icon END AS icon, " +
            "w.name AS walletName " +
            "FROM transactions t " +
            "JOIN wallets w ON w.id=t.walletId JOIN categories c ON c.defaultCategory = t.defaultCategoryId AND c.type = t.type " +
            "LEFT JOIN categories c1 ON c1.id = t.categoryId AND c1.type = t.type " +
            "WHERE t.tempTransactionServerId = :tempTransactionServerId AND t.type IN (1, 2) " +
            "ORDER BY t.transactionDate DESC")
    TransactionWithDetails getTransactions(String tempTransactionServerId);

    @Query("SELECT type, SUM(amount) as amount FROM transactions WHERE walletId = :walletId AND isDeleted = 0 GROUP BY type")
    List<TransactionTypeAmountModel> getTransactionAmountByType(int walletId);

    @Query("SELECT t.defaultCategoryId, COUNT(*) AS transactionCount, SUM(t.amount) AS amount, c.color, c.icon, w.currencySymbol, t.type, " +
            "t.categoryId, c1.name AS categoryName " +
            "FROM transactions t " +
            "LEFT JOIN categories c ON c.defaultCategory = t.defaultCategoryId AND c.type = t.type " +
            "LEFT JOIN categories c1 ON c1.id = t.categoryId AND c1.type = t.type " +
            "INNER JOIN wallets w ON w.id = t.walletId " +
            "WHERE t.walletId = :walletId AND t.isDeleted = 0 " +
            "GROUP BY t.defaultCategoryId, t.categoryId, t.type, c.color, c.icon, w.currencySymbol, c1.name " +
            "ORDER BY t.defaultCategoryId, c1.name")
    LiveData<List<TransactionCategoryModel>> getTransactionAmountByCategory(int walletId);

    @Query("UPDATE transactions SET isDeleted = 1, updatedAt = :updatedAt, isSynced = 0 WHERE tempTransactionServerId = :tempTransactionServerId")
    int deleteTransaction(String tempTransactionServerId, long updatedAt);
}