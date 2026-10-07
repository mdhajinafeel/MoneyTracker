package com.nprotech.moneytracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.nprotech.moneytracker.db.entites.RecurringTransactionEntity;
import com.nprotech.moneytracker.models.RecurringTransactionWithDetails;

import java.util.List;

@Dao
public interface RecurringTransactionDao {

    @Insert
    long insert(RecurringTransactionEntity recurringTransaction);

    @Update
    int update(RecurringTransactionEntity recurringTransaction);

    @Delete
    void delete(RecurringTransactionEntity recurringTransaction);

    @Query("SELECT * FROM recurring_transactions ORDER BY createdAt DESC")
    List<RecurringTransactionEntity> getAll();

    @Query("""
           SELECT rt.*, w.currencySymbol AS currencySymbol, c.color, c.name AS categoryName,
           c.icon AS icon, w.name AS walletName, fw.name AS fromWalletName, w.exchangeRate
           FROM recurring_transactions rt
           JOIN wallets w ON w.id = rt.walletId
           LEFT JOIN wallets fw ON fw.id = rt.fromWalletId
           JOIN categories c ON c.id = rt.categoryId AND c.type = rt.type
           WHERE rt.isDeleted = 0
           AND rt.accountId = :accountId
           AND (
           (:isPaused = 1 AND rt.status = 1)
           OR
           (:isPaused = 0 AND :isCompleted = 1 AND rt.status = 2)
           OR
           (:isPaused = 0 AND :isCompleted = 0 AND rt.status = 0))
           ORDER BY rt.createdAt DESC
        """)
    LiveData<List<RecurringTransactionWithDetails>> getRecurringTransactions(int accountId, boolean isPaused, boolean isCompleted);

    @Query("""
           SELECT rt.*, w.currencySymbol AS currencySymbol, c.color, c.name AS categoryName,
           c.icon AS icon, w.name AS walletName, fw.name AS fromWalletName, w.exchangeRate
           FROM recurring_transactions rt
           JOIN wallets w ON w.id = rt.walletId
           LEFT JOIN wallets fw ON fw.id = rt.fromWalletId
           JOIN categories c ON c.id = rt.categoryId AND c.type = rt.type
           WHERE rt.isDeleted = 0
           AND rt.tempRecurringServerId = :tempRecurringServerId
        """)
    LiveData<RecurringTransactionWithDetails> getRecurringTransactionDetails(String tempRecurringServerId);

    @Query("""
           SELECT rt.*, w.currencySymbol AS currencySymbol, c.color, c.name AS categoryName,
           c.icon AS icon, w.name AS walletName, fw.name AS fromWalletName, w.exchangeRate
           FROM recurring_transactions rt
           JOIN wallets w ON w.id = rt.walletId
           LEFT JOIN wallets fw ON fw.id = rt.fromWalletId
           JOIN categories c ON c.id = rt.categoryId AND c.type = rt.type
           WHERE rt.isDeleted = 0
           AND rt.tempRecurringServerId = :tempRecurringServerId
        """)
    RecurringTransactionWithDetails getRecurringTransactionDetail(String tempRecurringServerId);

    @Query("""
            SELECT * FROM recurring_transactions
            WHERE isActive = 1 AND status = 0
            AND nextRunDate <= :currentTime
            ORDER BY nextRunDate ASC
            """)
    List<RecurringTransactionEntity> getDueRecurringTransactions(long currentTime);

    @Query("""
        SELECT COUNT(*)
        FROM recurring_transactions
        WHERE accountId = :accountId AND isActive = 1 AND status = 0
        AND isDeleted = 0
        """)
    LiveData<Integer> getActiveRecurringCount(int accountId);

    @Query("UPDATE recurring_transactions SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :recurringId")
    int deleteRecurring(int recurringId, long updatedAt);

    @Query("UPDATE recurring_transactions SET status = :status, updatedAt = :updatedAt WHERE id = :recurringId")
    int activateRecurring(int recurringId, int status, long updatedAt);

    @Query("""
            SELECT * FROM recurring_transactions
            WHERE isActive = 1
            AND isDeleted = 0
            AND status != 1
            AND status != 2
            AND reminder = 1
            AND reminderShown = 0
            AND nextRunDate > :currentTime
            AND nextRunDate <= :reminderLimit
            ORDER BY nextRunDate ASC
    """)
    List<RecurringTransactionEntity> getUpcomingReminderTransactions(long currentTime, long reminderLimit);
}