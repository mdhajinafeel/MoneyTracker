package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.CategoryEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.models.CategoryFilter;
import com.nprotech.moneytracker.repositories.CategoryRepository;
import com.nprotech.moneytracker.repositories.TransactionRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CategoryViewModel extends ViewModel {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final LiveData<List<CategoryEntity>> incomeCategories;
    private final LiveData<List<CategoryEntity>> expenseCategories;
    private final MutableLiveData<CategoryFilter> incomeFilter = new MutableLiveData<>();
    private final MutableLiveData<CategoryFilter> expenseFilter = new MutableLiveData<>();
    private final MutableLiveData<Integer> transactionCount = new MutableLiveData<>();
    private final MutableLiveData<List<CategoryEntity>> categoryOptions = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Inject
    public CategoryViewModel(CategoryRepository categoryRepository, TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;

        incomeCategories = Transformations.switchMap(incomeFilter, filter -> categoryRepository.fetchCategoriesByType(filter.getType(),
                filter.isActive()));
        expenseCategories = Transformations.switchMap(expenseFilter, filter -> categoryRepository.fetchCategoriesByType(filter.getType(),
                filter.isActive()));
    }

    // ---------------------------------------------------------
    // Income Categories
    // ---------------------------------------------------------

    public LiveData<List<CategoryEntity>> getIncomeCategories() {
        return incomeCategories;
    }

    public void incomeCategory(int typeId, boolean active) {
        incomeFilter.setValue(new CategoryFilter(typeId, active));
    }

    // ---------------------------------------------------------
    // Expense Categories
    // ---------------------------------------------------------

    public LiveData<List<CategoryEntity>> getExpenseCategories() {
        return expenseCategories;
    }

    public void expenseCategory(boolean active) {
        expenseFilter.setValue(new CategoryFilter(TransactionEntity.TYPE_EXPENSE, active));
    }

    public CategoryEntity getCategoryById(int categoryId, boolean isDefault) {
        return categoryRepository.getCategoryById(categoryId, isDefault);
    }

    public CategoryEntity getDefaultCategoryByType(int categoryId, List<Integer> type) {
        return categoryRepository.getDefaultCategoryByType(categoryId, type);
    }

    public void saveCategory(CategoryEntity category) {
        categoryRepository.saveCategory(category);
    }

    public void updateCategory(CategoryEntity category) {
        categoryRepository.updateCategory(category);
    }

    public int getMaxOrder(int type) {
        return categoryRepository.getMaxOrder(type);
    }

    public void checkCategoryForDelete(int categoryId) {
        executorService.execute(() -> {
            try {
                int count = transactionRepository.getTransactionCountByCategory(categoryId);
                transactionCount.postValue(count);
            } catch (Exception e) {
                AppLogger.e(getClass(), "checkCategoryForDelete", e);
            }
        });
    }

    public LiveData<Integer> getTransactionCount() {
        return transactionCount;
    }

    public void getCategoriesForMove(int type, int categoryId) {
        executorService.execute(() -> {
            try {
                List<CategoryEntity> categories = categoryRepository.getCategoriesForMove(type, categoryId);
                categoryOptions.postValue(categories);
            } catch (Exception e) {
                AppLogger.e(getClass(), "getCategoriesForMove", e);
            }
        });
    }

    public LiveData<List<CategoryEntity>> getCategoryOptions() {
        return categoryOptions;
    }

    public boolean deleteCategory(int categoryId) {
        return categoryRepository.deleteCategory(categoryId);
    }

    public boolean moveTransactionsAndDeleteCategory(int oldCategoryId, int newCategoryId, int defaultCategoryId) {
        return transactionRepository.moveTransactionsAndDeleteCategory(oldCategoryId, newCategoryId, defaultCategoryId);
    }
}