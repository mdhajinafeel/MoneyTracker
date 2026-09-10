package com.nprotech.moneytracker.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.nprotech.moneytracker.db.entites.BudgetCategoryAmountEntity;
import com.nprotech.moneytracker.db.entites.BudgetCategoryEntity;
import com.nprotech.moneytracker.db.entites.BudgetEntity;
import com.nprotech.moneytracker.db.entites.BudgetWalletEntity;

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

    @Update
    void update(BudgetEntity budget);

    @Query("UPDATE budget SET updatedAt = :updatedAt, isDeleted = 1 WHERE id = :budgetId AND isDeleted = 0")
    void delete(int budgetId, long updatedAt);

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
        AND isDeleted = 0
        AND nextRepeatDate > 0
        AND nextRepeatDate <= :date
        """)
    List<BudgetEntity> getBudgetsDueForRepeat(long date);
}