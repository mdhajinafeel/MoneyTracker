package com.nprotech.moneytracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.models.DailyTransModel;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TransactionEntity transactionEntity);

    @Query("SELECT * FROM transactions")
    LiveData<List<TransactionEntity>> getAllTransactions();

    @Query("SELECT * FROM transactions WHERE tempTransactionServerId = :tempTransactionServerId")
    TransactionEntity getTransactionById(String tempTransactionServerId);

    @Query("SELECT SUM(t.amount) AS amount," +
            "CAST(strftime('%d', datetime(t.transactionDate/1000, 'unixepoch', 'localtime')) AS INTEGER) AS day," +
            "CAST(strftime('%m', datetime(t.transactionDate/1000, 'unixepoch', 'localtime')) AS INTEGER) AS month," +
            "CAST(strftime('%Y', datetime(t.transactionDate/1000, 'unixepoch', 'localtime')) AS INTEGER) AS year, " +
            "w.currencySymbol " +
            "FROM transactions t " +
            "JOIN wallets w ON t.walletId = w.id " +
            "WHERE t.accountId = :accountId " +
            "AND w.accountId = :accountId " +
            "AND t.type = 2 " +
            "GROUP BY year, month, day " +
            "ORDER BY year DESC, month DESC, day DESC;")
    LiveData<List<DailyTransModel>> getDailyTransactionData(int accountId);
}