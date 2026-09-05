package com.nprotech.moneytracker.repositories;

import androidx.lifecycle.LiveData;

import com.nprotech.moneytracker.db.dao.CategoryDao;
import com.nprotech.moneytracker.db.entites.CategoryEntity;

import java.util.List;

public class CategoryRepository {

    private final CategoryDao categoryDao;

    public CategoryRepository(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    public LiveData<List<CategoryEntity>> fetchCategoriesByType(int type, boolean activeCategory) {
        if(activeCategory) {
            return categoryDao.fetchCategoriesByType(type, true);
        }
        return categoryDao.fetchCategoriesByType(type);
    }

    public CategoryEntity getCategoryById(int categoryId, boolean isDefault) {
        if(isDefault) {
            return categoryDao.getDefaultCategoryById(categoryId);
        }
        return categoryDao.getCategoryById(categoryId);
    }

    public CategoryEntity getDefaultCategoryByType(int categoryId, List<Integer> type) {
        return categoryDao.getDefaultCategoryByType(categoryId, type);
    }

    public void saveCategory(CategoryEntity category) {
        categoryDao.insert(category);
    }

    public void updateCategory(CategoryEntity category) {
        categoryDao.updateCategory(category);
    }

    public int getMaxOrder(int type) {
        return categoryDao.getMaxOrder(type);
    }

    public boolean deleteCategory(int categoryId) {
        return categoryDao.deleteCategory(categoryId, System.currentTimeMillis()) > 0;
    }

    public List<CategoryEntity> getCategoriesForMove(int type, int excludeCategoryId) {
        return categoryDao.getCategoriesForMove(type, excludeCategoryId);
    }
}