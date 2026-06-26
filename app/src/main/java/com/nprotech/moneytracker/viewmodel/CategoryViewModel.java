package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.CategoryEntity;
import com.nprotech.moneytracker.repositories.CategoryRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CategoryViewModel extends ViewModel {

    private CategoryRepository categoryRepository;
    private final LiveData<List<CategoryEntity>> incomeCategories;
    private final LiveData<List<CategoryEntity>> expenseCategories;
    private final MutableLiveData<Integer> incomeCategoryId = new MutableLiveData<>();
    private final MutableLiveData<Integer> expenseCategoryId = new MutableLiveData<>();

    @Inject
    public CategoryViewModel(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;

        incomeCategories = Transformations.switchMap(incomeCategoryId, categoryRepository::fetchCategoriesByType);
        expenseCategories = Transformations.switchMap(expenseCategoryId, categoryRepository::fetchCategoriesByType);
    }

    public LiveData<List<CategoryEntity>> getIncomeCategories() {
        return incomeCategories;
    }

    public LiveData<List<CategoryEntity>> getExpenseCategories() {
        return expenseCategories;
    }

    public void incomeCategory(int categoryId) {
        incomeCategoryId.setValue(categoryId);
    }

    public void expenseCategory(int categoryId) {
        expenseCategoryId.setValue(categoryId);
    }
}