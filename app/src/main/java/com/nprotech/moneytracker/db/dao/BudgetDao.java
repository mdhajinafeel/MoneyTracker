package com.nprotech.moneytracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.nprotech.moneytracker.db.entites.BudgetCategoryAmountEntity;
import com.nprotech.moneytracker.db.entites.BudgetCategoryEntity;
import com.nprotech.moneytracker.db.entites.BudgetEntity;
import com.nprotech.moneytracker.db.entites.BudgetWalletEntity;
import com.nprotech.moneytracker.models.BudgetWithDetails;
import com.nprotech.moneytracker.models.CategoryBudgetProgress;

import java.util.List;

@Dao
public interface BudgetDao {

    // =========================================================
    // BUDGET
    // =========================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BudgetEntity budget);

    @Query("SELECT * FROM budget WHERE id = :budgetId AND isDeleted = 0 LIMIT 1")
    BudgetEntity getBudgetById(int budgetId);

    @Query("SELECT b.*, COALESCE(( " +
            "    SELECT SUM(t.accountAmount) " +
            "    FROM transactions t " +
            "    INNER JOIN budget_category bc ON bc.categoryId = t.categoryId " +
            "    INNER JOIN budget_wallet bw ON bw.walletId = t.walletId " +
            "    WHERE bc.budgetId = b.id " +
            "    AND bw.budgetId = b.id " +
            "    AND bc.isDeleted = 0 " +
            "    AND bw.isDeleted = 0 " +
            "    AND t.isDeleted = 0 " +
            "    AND t.type = 2 " +
            "    AND t.transactionDate >= b.startDate " +
            "    AND t.transactionDate <= b.endDate " +
            "), 0) AS spentAmount " +
            "FROM budget b " +
            "WHERE b.isDeleted = 0 " +
            "AND b.accountId = :accountId " +
            "AND ( " +

            // Archived
            "    (:isArchived = 1 AND b.isArchived = 1) " +

            "    OR " +

            // Paused
            "    (:isArchived = 0 AND :isPaused = 1 " +
            "        AND b.isArchived = 0 " +
            "        AND b.isPaused = 1) " +

            "    OR " +

            // Active - In Progress / Completed
            "    (:isArchived = 0 AND :isPaused = 0 " +
            "        AND b.isArchived = 0 " +
            "        AND b.isPaused = 0 " +
            "        AND ( " +

            // Completed
            "            (:isCompleted = 1 AND COALESCE(( " +
            "                SELECT SUM(t2.accountAmount) " +
            "                FROM transactions t2 " +
            "                INNER JOIN budget_category bc2 " +
            "                    ON bc2.categoryId = t2.categoryId " +
            "                INNER JOIN budget_wallet bw2 " +
            "                    ON bw2.walletId = t2.walletId " +
            "                WHERE bc2.budgetId = b.id " +
            "                AND bw2.budgetId = b.id " +
            "                AND bc2.isDeleted = 0 " +
            "                AND bw2.isDeleted = 0 " +
            "                AND t2.isDeleted = 0 " +
            "                AND t2.type = 2 " +
            "                AND t2.transactionDate >= b.startDate " +
            "                AND t2.transactionDate <= b.endDate " +
            "            ), 0) >= b.amount) " +

            "            OR " +

            // In Progress
            "            (:isCompleted = 0 AND COALESCE(( " +
            "                SELECT SUM(t3.accountAmount) " +
            "                FROM transactions t3 " +
            "                INNER JOIN budget_category bc3 " +
            "                    ON bc3.categoryId = t3.categoryId " +
            "                INNER JOIN budget_wallet bw3 " +
            "                    ON bw3.walletId = t3.walletId " +
            "                WHERE bc3.budgetId = b.id " +
            "                AND bw3.budgetId = b.id " +
            "                AND bc3.isDeleted = 0 " +
            "                AND bw3.isDeleted = 0 " +
            "                AND t3.isDeleted = 0 " +
            "                AND t3.type = 2 " +
            "                AND t3.transactionDate >= b.startDate " +
            "                AND t3.transactionDate <= b.endDate " +
            "            ), 0) < b.amount) " +

            "        ) " +
            "    ) " +

            ") " +
            "ORDER BY b.createdAt DESC")
    LiveData<List<BudgetWithDetails>> getBudgets(int accountId, boolean isArchived, boolean isPaused, boolean isCompleted);

    @Query("SELECT b.*, COALESCE(( SELECT SUM(t.accountAmount) FROM transactions t " +
            "INNER JOIN budget_category bc ON bc.categoryId = t.categoryId " +
            "INNER JOIN budget_wallet bw ON bw.walletId = t.walletId " +
            "WHERE bc.budgetId = b.id " +
            "AND bw.budgetId = b.id " +
            "AND bc.isDeleted = 0 " +
            "AND bw.isDeleted = 0 " +
            "AND t.isDeleted = 0 " +
            "AND t.transactionDate >= b.startDate " +
            "AND t.transactionDate <= b.endDate " +
            "AND t.type = 2), 0) AS spentAmount " +
            "FROM budget b " +
            "WHERE b.id = :budgetId " +
            "AND b.isDeleted = 0")
    LiveData<BudgetWithDetails> getBudgetDetailById(int budgetId);

    @Query("SELECT COUNT(*) " +
            "FROM budget b " +
            "WHERE b.isDeleted = 0 " +
            "AND b.accountId = :accountId " +
            "AND b.isArchived = 0 " +
            "AND b.isPaused = 0 " +
            "AND COALESCE(( " +
            "    SELECT SUM(t.accountAmount) " +
            "    FROM transactions t " +
            "    INNER JOIN budget_category bc " +
            "        ON bc.categoryId = t.categoryId " +
            "    INNER JOIN budget_wallet bw " +
            "        ON bw.walletId = t.walletId " +
            "    WHERE bc.budgetId = b.id " +
            "    AND bw.budgetId = b.id " +
            "    AND bc.isDeleted = 0 " +
            "    AND bw.isDeleted = 0 " +
            "    AND t.isDeleted = 0 " +
            "    AND t.type = 2 " +
            "    AND t.transactionDate >= b.startDate " +
            "    AND t.transactionDate <= b.endDate " +
            "), 0) < b.amount")
    LiveData<Integer> getActiveBudgetCount(int accountId);

    @Update
    void update(BudgetEntity budget);

    @Query("UPDATE budget SET updatedAt = :updatedAt, isDeleted = 1 WHERE id = :budgetId AND isDeleted = 0")
    int delete(int budgetId, long updatedAt);

    // =========================================================
    // BUDGET WALLETS
    // =========================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWallet(BudgetWalletEntity budgetWallet);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWallets(List<BudgetWalletEntity> budgetWallets);

    @Query("SELECT * FROM budget_wallet WHERE budgetId = :budgetId")
    List<BudgetWalletEntity> getWallets(int budgetId);

    @Update
    void updateWallet(BudgetWalletEntity budgetWallet);

    @Query("UPDATE budget_wallet SET updatedAt = :updatedAt, isDeleted = 1 WHERE budgetId = :budgetId AND isDeleted = 0")
    void deleteWallets(int budgetId, long updatedAt);

    // =========================================================
    // BUDGET CATEGORIES
    // =========================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategory(BudgetCategoryEntity budgetCategory);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategories(List<BudgetCategoryEntity> budgetCategories);

    @Query("SELECT * FROM budget_category WHERE budgetId = :budgetId")
    List<BudgetCategoryEntity> getCategories(int budgetId);

    @Update
    void updateCategory(BudgetCategoryEntity budgetCategory);

    @Query("UPDATE budget_category set updatedAt = :updatedAt, isDeleted = 1 WHERE budgetId = :budgetId AND isDeleted = 0")
    void deleteCategories(int budgetId, long updatedAt);

    // =========================================================
    // CATEGORY AMOUNTS
    // =========================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategoryAmount(BudgetCategoryAmountEntity categoryAmount);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategoryAmounts(List<BudgetCategoryAmountEntity> categoryAmounts);

    @Query("SELECT * FROM budget_category_amount WHERE budgetId = :budgetId")
    List<BudgetCategoryAmountEntity> getCategoryAmounts(int budgetId);

    @Update
    void updateCategoryAmount(BudgetCategoryAmountEntity categoryAmount);

    @Query("UPDATE budget_category_amount SET updatedAt = :updatedAt, isDeleted = 1 WHERE budgetId = :budgetId AND isDeleted = 0")
    void deleteCategoryAmounts(int budgetId, long updatedAt);

    @Query("""
        SELECT * FROM budget
        WHERE repeatEnabled = 1
        AND isDeleted = 0 AND isArchived = 0 AND isPaused = 0
        AND nextRepeatDate > 0
        AND nextRepeatDate <= :date
        """)
    List<BudgetEntity> getBudgetsDueForRepeat(long date);

    @Query("UPDATE budget SET isPaused = :isPause, isArchived = :isArchive, updatedAt = :updatedAt, archivedOn = :archivedOn WHERE id = :budgetId")
    int archiveRestoreBudget(int budgetId, long updatedAt, boolean isArchive, long archivedOn, boolean isPause);

    @Query("UPDATE budget SET isPaused = :isPause, updatedAt = :updatedAt, archivedOn = :pausedOn WHERE id = :budgetId")
    int pauseBudget(int budgetId, long updatedAt, boolean isPause, long pausedOn);

    @Query("""
        SELECT * FROM budget
        WHERE isDeleted = 0
        AND isArchived = 0
        AND isPaused = 0
        AND alertEnabled = 1
        """)
    List<BudgetEntity> getActiveAlertBudgets();

    @Query("""
        UPDATE budget
        SET alertTriggered = :alertTriggered,
            updatedAt = :updatedAt
            WHERE id = :budgetId
        """)
    void updateAlertTriggered(int budgetId, boolean alertTriggered, long updatedAt);

    @Query("""
        SELECT COALESCE(SUM(t.accountAmount), 0)
        FROM transactions t
        WHERE t.isDeleted = 0
        AND t.type = 2
        AND t.transactionDate >= :startDate
        AND t.transactionDate <= :endDate
        AND EXISTS (
            SELECT 1
            FROM budget_category bc
            WHERE bc.budgetId = :budgetId
            AND bc.categoryId = t.categoryId
            AND bc.isDeleted = 0
        )
        AND EXISTS (
            SELECT 1
            FROM budget_wallet bw
            WHERE bw.budgetId = :budgetId
            AND bw.walletId = t.walletId
            AND bw.isDeleted = 0
        )
        """)
    double getBudgetSpentAmount(int budgetId, long startDate, long endDate);

    @Query("""
        SELECT categoryId
        FROM budget_category
        WHERE budgetId = :budgetId
        AND isDeleted = 0
        """)
    List<Integer> getCategoryIdsByBudgetId(int budgetId);

    @Query("""
        SELECT walletId
        FROM budget_wallet
        WHERE budgetId = :budgetId
        AND isDeleted = 0
        """)
    List<Integer> getWalletIdsByBudgetId(int budgetId);

    @Transaction
    @Query("""
        SELECT
            bc.categoryId AS categoryId,
            c.name AS categoryName,
            c.icon AS icon,
            c.color AS color,
            bca.amount AS budgetAmount,
            COALESCE((
                SELECT SUM(t.accountAmount)
                FROM transactions t
                WHERE t.isDeleted = 0
                AND t.type = 2
                AND t.categoryId = bc.categoryId
                AND t.transactionDate >= :startDate
                AND t.transactionDate <= :endDate
                AND EXISTS (
                    SELECT 1
                    FROM budget_wallet bw
                    WHERE bw.budgetId = b.id
                    AND bw.walletId = t.walletId
                    AND bw.isDeleted = 0
                )
            ), 0) AS spentAmount,
            b.currencySymbol, c.defaultCategory
        FROM budget b
        INNER JOIN budget_category bc
            ON bc.budgetId = b.id
            AND bc.isDeleted = 0
        INNER JOIN categories c
            ON c.id = bc.categoryId
            AND c.type = 2
            AND c.isDeleted = 0
        INNER JOIN budget_category_amount bca
            ON bca.budgetId = b.id
            AND bca.categoryId = bc.categoryId
            AND bca.isDeleted = 0
        WHERE b.id = :budgetId AND b.accountId = :accountId
        AND b.isDeleted = 0
                    ORDER BY CASE WHEN :sortType = 1
                                         THEN spentAmount * 1.0 / NULLIF(budgetAmount, 0)
                                     END DESC,
                                     CASE WHEN :sortType = 2
                                         THEN spentAmount * 1.0 / NULLIF(budgetAmount, 0)
                                     END ASC,
                                     CASE WHEN :sortType = 3
                                         THEN spentAmount
                                     END DESC,
                                     CASE WHEN :sortType = 4
                                         THEN spentAmount
                                     END ASC,
                                     CASE WHEN :sortType = 5
                                         THEN budgetAmount
                                     END DESC,
                                     CASE WHEN :sortType = 6
                                         THEN budgetAmount
                                     END ASC,
                                     categoryName ASC
        """)
    List<CategoryBudgetProgress> getCategoryBudgetProgress(int accountId, int budgetId, long startDate, long endDate, int sortType);
}