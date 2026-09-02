package com.nprotech.moneytracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.nprotech.moneytracker.db.entites.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CategoryEntity> list);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CategoryEntity category);

    @Update
    void updateCategory(CategoryEntity category);

    @Query("SELECT COUNT(*) FROM categories")
    int getCategoriesCount();

    @Query("SELECT * FROM categories WHERE type = :type  AND (:type = 5 OR defaultCategory < 26) AND isDeleted = 0 AND active = :activeCategory ORDER BY ordering")
    LiveData<List<CategoryEntity>> fetchCategoriesByType(int type, boolean activeCategory);

    @Query("SELECT * FROM categories WHERE type = :type  AND (:type = 5 OR defaultCategory < 26) AND isDeleted = 0 ORDER BY ordering")
    LiveData<List<CategoryEntity>> fetchCategoriesByType(int type);

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    CategoryEntity getCategoryById(int categoryId);

    @Query("SELECT * FROM categories WHERE defaultCategory = :categoryId LIMIT 1")
    CategoryEntity getDefaultCategoryById(int categoryId);

    @Query("SELECT * FROM categories WHERE defaultCategory = :categoryId AND type = :type LIMIT 1")
    CategoryEntity getDefaultCategoryByType(int categoryId, int type);

    @Query("SELECT MAX(ordering) FROM categories WHERE type = :type")
    int getMaxOrder(int type);

    @Query("UPDATE categories SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :categoryId")
    int deleteCategory(int categoryId, long updatedAt);

    @Query("SELECT * FROM categories " +
            "WHERE type = :type " +
            "AND id != :excludeCategoryId AND (:type = 5 OR defaultCategory < 26) " +
            "AND isDeleted = 0 AND active = 1 " +
            "ORDER BY name ASC")
    List<CategoryEntity> getCategoriesForMove(int type, int excludeCategoryId);
}