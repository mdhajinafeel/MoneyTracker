package com.nprotech.moneytracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nprotech.moneytracker.db.entites.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CategoryEntity> list);

    @Query("SELECT COUNT(*) FROM categories")
    int getCategoriesCount();

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY ordering")
    LiveData<List<CategoryEntity>> fetchCategoriesByType(int type);

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    CategoryEntity getCategoryById(int categoryId);

    @Query("SELECT * FROM categories WHERE defaultCategory = :categoryId LIMIT 1")
    CategoryEntity getDefaultCategoryById(int categoryId);
}