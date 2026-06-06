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

    public LiveData<List<CategoryEntity>> fetchCategoriesByType(int type) {
        return categoryDao.fetchCategoriesByType(type);
    }
}