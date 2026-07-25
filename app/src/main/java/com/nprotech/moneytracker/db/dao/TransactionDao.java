package com.nprotech.moneytracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.models.BalanceSummaryModel;
import com.nprotech.moneytracker.models.BreakdownChartModel;
import com.nprotech.moneytracker.models.CalendarSummaryModel;
import com.nprotech.moneytracker.models.CategoryExpenseModel;
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

    @Transaction
    @Query("SELECT t.*, w.currencySymbol AS currencySymbol, c.color, c1.name AS categoryName, " +
            "CASE WHEN c.icon IS NULL THEN c1.icon ELSE c.icon END AS icon, " +
            "w.name AS walletName, fw.name AS fromWalletName, w.exchangeRate " +
            "FROM transactions t " +
            "JOIN wallets w ON w.id = t.walletId " +
            "LEFT JOIN wallets fw ON fw.id = t.fromWalletId " +
            "JOIN categories c ON c.defaultCategory = t.defaultCategoryId " +
            "LEFT JOIN categories c1 ON c1.id = t.categoryId AND c1.type = t.type " +
            "WHERE t.isDeleted = 0 " +
            "AND t.accountId = :accountId " +
            "AND w.accountId = :accountId " +
            "AND t.type IN (1,2,3) " +
            "AND (t.parentTransactionId IS NULL OR t.parentTransactionId = '') " +
            "ORDER BY t.transactionDate DESC " +
            "LIMIT :limit OFFSET :offset")
    List<TransactionWithDetails> getTransactionsPaged(int accountId, int limit, int offset);

    @Transaction
    @Query("SELECT t.*, w.currencySymbol AS currencySymbol, c.color, c1.name AS categoryName, " +
            "CASE WHEN c.icon IS NULL THEN c1.icon ELSE c.icon END AS icon, " +
            "w.name AS walletName, fw.name AS fromWalletName, w.exchangeRate " +
            "FROM transactions t " +
            "JOIN wallets w ON w.id=t.walletId " +
            "LEFT JOIN wallets fw ON fw.id=t.fromWalletId " +
            "JOIN categories c ON c.defaultCategory=t.defaultCategoryId " +
            "LEFT JOIN categories c1 ON c1.id=t.categoryId AND c1.type=t.type " +
            "WHERE t.isDeleted=0 " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "AND t.accountId=:accountId " +
            "AND w.accountId=:accountId " +
            "AND t.type IN(1,2,3) " +
            "AND (t.parentTransactionId IS NULL OR t.parentTransactionId='') " +
            "ORDER BY t.transactionDate DESC " +
            "LIMIT :limit OFFSET :offset")
    List<TransactionWithDetails> getTransactionsPaged(int accountId, long startDate, long endDate, int limit, int offset);

    @Transaction
    @Query("SELECT t.*, w.currencySymbol AS currencySymbol, c.color, c1.name AS categoryName, CASE WHEN c.icon IS NULL THEN c1.icon ELSE c.icon END AS icon, " +
            "w.name AS walletName, fw.name AS fromWalletName, w.exchangeRate " +
            "FROM transactions t " +
            "JOIN wallets w ON w.id = t.walletId " +
            "LEFT JOIN wallets fw ON fw.id = t.fromWalletId " +
            "JOIN categories c ON c.defaultCategory = t.defaultCategoryId " +
            "LEFT JOIN categories c1 ON c1.id = t.categoryId AND c1.type = t.type " +
            "WHERE t.tempTransactionServerId = :tempTransactionServerId AND t.type IN (1, 2, 3) " +
            "ORDER BY t.transactionDate DESC")
    TransactionWithDetails getTransactions(String tempTransactionServerId);

    @Query("SELECT type, SUM(amount) as amount FROM transactions WHERE walletId = :walletId AND isDeleted = 0 GROUP BY type")
    List<TransactionTypeAmountModel> getTransactionAmountByType(int walletId);

    @Query("SELECT t.defaultCategoryId, COUNT(*) AS transactionCount, SUM(t.amount) AS amount, c.color, c.icon, w.currencySymbol, " +
            "CASE WHEN t.type = 3 AND t.fromWalletId = :walletId THEN 2 WHEN t.type = 3 AND t.walletId = :walletId THEN 1 ELSE t.type END AS type, " +
            "t.categoryId, c1.name AS categoryName FROM transactions t " +
            "LEFT JOIN categories c ON c.defaultCategory = t.defaultCategoryId " +
            "LEFT JOIN categories c1 ON c1.id = t.categoryId AND c1.type = t.type " +
            "INNER JOIN wallets w ON w.id = t.walletId " +
            "WHERE (t.walletId = :walletId OR t.fromWalletId = :walletId) " +
            "AND t.isDeleted = 0 GROUP BY t.defaultCategoryId, t.categoryId, " +
            "CASE WHEN t.type = 3 AND t.fromWalletId = :walletId THEN 2 WHEN t.type = 3 AND t.walletId = :walletId THEN 1 ELSE t.type END, " +
            "c.color, c.icon, w.currencySymbol, c1.name " +
            "ORDER BY t.defaultCategoryId, c1.name")
    LiveData<List<TransactionCategoryModel>> getTransactionAmountByCategory(int walletId);

    @Query("UPDATE transactions SET isDeleted = 1, updatedAt = :updatedAt, isSynced = 0 WHERE tempTransactionServerId = :tempTransactionServerId")
    int deleteTransaction(String tempTransactionServerId, long updatedAt);

    @Delete
    void delete(TransactionEntity transaction);

    @Query("SELECT * FROM transactions WHERE parentTransactionId = :parentTransactionId LIMIT 1")
    TransactionEntity getFeeTransaction(String parentTransactionId);

    @Query("SELECT CAST(strftime('%s', date(t.transactionDate/1000, 'unixepoch', 'localtime')) AS INTEGER) * 1000 AS dayTimestamp, " +
            "SUM(CASE WHEN type = 1 THEN (t.amount * w.exchangeRate) ELSE 0 END) AS income, " +
            "SUM(CASE WHEN type = 2 THEN (t.amount * w.exchangeRate) ELSE 0 END) AS expense, " +
            "SUM(CASE WHEN type = 1 THEN (t.amount * w.exchangeRate) WHEN type = 2 THEN (-t.amount * w.exchangeRate) ELSE 0 END) AS total " +
            "FROM transactions t " +
            "JOIN wallets w ON w.id = t.walletId " +
            "WHERE t.accountId = :accountId " +
            "AND t.isDeleted = 0 " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY date(t.transactionDate/1000, 'unixepoch', 'localtime') " +
            "ORDER BY dayTimestamp")
    LiveData<List<CalendarSummaryModel>> getCalendarSummary(int accountId, long startDate, long endDate);

    @Query("SELECT 0 AS dayTimestamp, " +
            "SUM(CASE WHEN type = 1 THEN (t.amount * w.exchangeRate) ELSE 0 END) AS income, " +
            "SUM(CASE WHEN type = 2 THEN (t.amount * w.exchangeRate) ELSE 0 END) AS expense, " +
            "SUM(CASE WHEN type = 1 THEN (t.amount * w.exchangeRate) WHEN type = 2 THEN (-t.amount * w.exchangeRate) ELSE 0 END) AS total " +
            "FROM transactions t " +
            "JOIN wallets w ON w.id = t.walletId " +
            "WHERE t.accountId = :accountId " +
            "AND t.isDeleted = 0 " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate")
    LiveData<CalendarSummaryModel> getCalendarHeader(int accountId, long startDate, long endDate);

    @Transaction
    @Query("SELECT t.*, w.currencySymbol AS currencySymbol, c.color, c1.name AS categoryName, CASE WHEN c.icon IS NULL THEN c1.icon ELSE c.icon END AS icon, " +
            "w.name AS walletName, fw.name AS fromWalletName, w.exchangeRate " +
            "FROM transactions t " +
            "JOIN wallets w ON w.id = t.walletId " +
            "LEFT JOIN wallets fw ON fw.id = t.fromWalletId " +
            "JOIN categories c ON c.defaultCategory = t.defaultCategoryId " +
            "LEFT JOIN categories c1 ON c1.id = t.categoryId AND c1.type = t.type " +
            "WHERE t.accountId = :accountId AND t.transactionDate BETWEEN :start AND :end " +
            "AND t.isDeleted = 0 " +
            "ORDER BY t.transactionDate DESC")
    LiveData<List<TransactionWithDetails>> getTransactionsForDay(int accountId, long start, long end);

    @Query("SELECT " +
            "SUM(w.initialAmount * w.exchangeRate) + " +
            "IFNULL((" +
            "SELECT SUM(" +
            "CASE " +
            "WHEN t.type = 1 THEN t.amount * tw.exchangeRate " +
            "WHEN t.type = 2 THEN -t.amount * tw.exchangeRate " +
            "ELSE 0 END) " +
            "FROM transactions t " +
            "INNER JOIN wallets tw ON tw.id = t.walletId " +
            "WHERE t.accountId = w.accountId " +
            "AND t.isDeleted = 0 " +
            "AND t.transactionDate < :startDate" +
            "), 0) AS openingBalance, " +

            "SUM(w.initialAmount * w.exchangeRate) + " +
            "IFNULL((" +
            "SELECT SUM(" +
            "CASE " +
            "WHEN t.type = 1 THEN t.amount * tw.exchangeRate " +
            "WHEN t.type = 2 THEN -t.amount * tw.exchangeRate " +
            "ELSE 0 END) " +
            "FROM transactions t " +
            "INNER JOIN wallets tw ON tw.id = t.walletId " +
            "WHERE t.accountId = w.accountId " +
            "AND t.isDeleted = 0 " +
            "AND t.transactionDate <= :endDate" +
            "), 0) AS closingBalance " +
            "FROM wallets w WHERE w.accountId = :accountId AND w.isDeleted = 0")
    LiveData<BalanceSummaryModel> getBalanceSummary(int accountId, long startDate, long endDate);

    @Query("SELECT c.id AS categoryId, c.name AS categoryName, c.defaultCategory AS defaultCategoryId, " +
            "c.color AS color, SUM(t.amount * w.exchangeRate) AS amount, 0 AS percentage, 0 AS transactionCount, 0 AS icon " +
            "FROM transactions t " +
            "INNER JOIN wallets w ON w.id = t.walletId " +
            "INNER JOIN categories c ON c.id = t.categoryId " +
            "WHERE t.accountId = :accountId AND t.type = 2 " +
            "AND t.isDeleted = 0 AND t.transactionDate BETWEEN :startDate AND :endDate GROUP BY c.id ORDER BY amount DESC")
    LiveData<List<CategoryExpenseModel>> getExpenseByCategory(int accountId, long startDate, long endDate);

    @Query("SELECT c.id AS categoryId, c.name AS categoryName, c.defaultCategory AS defaultCategoryId, " +
            "c.color AS color, SUM(t.amount * w.exchangeRate) AS amount, 0 AS percentage, 0 AS transactionCount, 0 AS icon " +
            "FROM transactions t " +
            "INNER JOIN categories c ON c.id = t.categoryId " +
            "INNER JOIN wallets w ON w.id = t.walletId " +
            "WHERE t.accountId = :accountId AND t.type = 1 " +
            "AND t.isDeleted = 0 AND t.transactionDate BETWEEN :startDate AND :endDate GROUP BY c.id ORDER BY amount DESC")
    LiveData<List<CategoryExpenseModel>> getIncomeByCategory(int accountId, long startDate, long endDate);

    @Query("SELECT CASE WHEN :transactionType = 1 THEN SUM(t.amount * w.exchangeRate) ELSE SUM((t.amount * w.exchangeRate) * -1) END AS amount, COUNT(*) AS transactionCount, c.name AS categoryName, t.categoryId, t.defaultCategoryId, c.color, c.icon, 0 AS percentage, " +
            "c.icon AS icon " +
            "FROM transactions t " +
            "INNER JOIN categories c ON c.id = t.categoryId " +
            "INNER JOIN wallets w ON w.id = t.walletId " +
            "WHERE t.isDeleted = 0 AND t.transactionDate BETWEEN :startDate AND :endDate AND t.type = :transactionType AND t.accountId = :accountId " +
            "GROUP BY t.categoryId ORDER BY categoryId")
    LiveData<List<CategoryExpenseModel>> getTransactionListByCategory(int transactionType, int accountId, long startDate, long endDate);

    @Query("SELECT CAST(strftime('%H', t.transactionDate / 1000, 'unixepoch', 'localtime') AS INTEGER) AS period, SUM(t.amount * w.exchangeRate) AS amount, " +
            "COUNT(*) AS transactionCount " +
            "FROM transactions t " +
            "INNER JOIN wallets w ON w.id = t.walletId " +
            "WHERE t.accountId = :accountId AND type = :transactionType " +
            "AND t.isDeleted = 0 AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY period ORDER BY period")
    LiveData<List<BreakdownChartModel>> getHourlyBreakdown(int accountId, int transactionType, long startDate, long endDate);

    @Query("SELECT (transactionDate / 86400000) * 86400000 AS period, SUM(t.amount * w.exchangeRate) AS amount, COUNT(*) AS transactionCount " +
            "FROM transactions t " +
            "INNER JOIN wallets w ON w.id = t.walletId " +
            "WHERE t.accountId = :accountId AND t.type = :transactionType AND t.isDeleted = 0 " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY period ORDER BY period")
    LiveData<List<BreakdownChartModel>> getDailyBreakdown(int accountId, int transactionType, long startDate, long endDate);

    @Query("SELECT MIN(transactionDate) AS period, SUM(t.amount * w.exchangeRate) AS amount, COUNT(*) AS transactionCount " +
            "FROM transactions t " +
            "INNER JOIN wallets w ON w.id = t.walletId " +
            "WHERE t.accountId = :accountId AND t.type = :transactionType AND t.isDeleted = 0 " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY strftime('%Y-%m-%d', transactionDate / 1000, 'unixepoch', 'localtime') " +
            "ORDER BY MIN(transactionDate)")
    LiveData<List<BreakdownChartModel>> getWeeklyBreakdown(int accountId, int transactionType, long startDate, long endDate);

    @Query("SELECT MIN(transactionDate) AS period, SUM(t.amount * w.exchangeRate) AS amount, COUNT(*) AS transactionCount " +
            "FROM transactions t " +
            "INNER JOIN wallets w ON w.id = t.walletId " +
            "WHERE t.accountId = :accountId AND t.type = :transactionType AND t.isDeleted = 0 " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY strftime('%Y-%m-%d', transactionDate / 1000, 'unixepoch', 'localtime') " +
            "ORDER BY MIN(transactionDate)")
    LiveData<List<BreakdownChartModel>> getMonthlyBreakdown(int accountId, int transactionType, long startDate, long endDate);

    @Query("SELECT CAST(strftime('%Y', transactionDate / 1000, 'unixepoch', 'localtime') AS INTEGER) AS period, SUM(t.amount * w.exchangeRate) AS amount, " +
            "COUNT(*) AS transactionCount " +
            "FROM transactions t " +
            "INNER JOIN wallets w ON w.id = t.walletId " +
            "WHERE t.accountId = :accountId AND t.type = :transactionType AND t.isDeleted = 0 " +
            "GROUP BY period ORDER BY period")
    LiveData<List<BreakdownChartModel>> getYearlyBreakdown(int accountId, int transactionType);

    @Transaction
    @Query("SELECT t.*, w.currencySymbol AS currencySymbol, c.color, c1.name AS categoryName, " +
            "CASE WHEN c.icon IS NULL THEN c1.icon ELSE c.icon END AS icon, " +
            "w.name AS walletName, fw.name AS fromWalletName, w.exchangeRate " +
            "FROM transactions t " +
            "JOIN wallets w ON w.id=t.walletId " +
            "LEFT JOIN wallets fw ON fw.id=t.fromWalletId " +
            "JOIN categories c ON c.defaultCategory=t.defaultCategoryId " +
            "LEFT JOIN categories c1 ON c1.id=t.categoryId AND c1.type=t.type " +
            "WHERE t.isDeleted=0 AND t.type = :transactionType " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "AND t.accountId=:accountId " +
            "AND w.accountId=:accountId " +
            "AND (t.parentTransactionId IS NULL OR t.parentTransactionId='') " +
            "ORDER BY t.transactionDate DESC " +
            "LIMIT :limit OFFSET :offset")
    List<TransactionWithDetails> getTransactionsForPeriod(int accountId, int transactionType, long startDate, long endDate, int limit, int offset);
}