package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.BudgetCategoryAmountEntity;
import com.nprotech.moneytracker.db.entites.BudgetCategoryEntity;
import com.nprotech.moneytracker.db.entites.BudgetEntity;
import com.nprotech.moneytracker.db.entites.BudgetWalletEntity;
import com.nprotech.moneytracker.repositories.BudgetRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BudgetViewModel extends ViewModel {

    private final BudgetRepository budgetRepository;

    @Inject
    public BudgetViewModel(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    public long saveBudget(BudgetEntity budget, Set<Integer> walletIds, Set<Integer> categoryIds, Map<Integer, Double> categoryAmounts) {
        return budgetRepository.saveBudget(budget, walletIds, categoryIds, categoryAmounts);
    }

    public void updateBudget(BudgetEntity budget, Set<Integer> selectedWalletIds, Set<Integer> selectedCategoryIds, Map<Integer, Double> categoryAmounts) {
        budgetRepository.updateBudget(budget, selectedWalletIds, selectedCategoryIds, categoryAmounts);
    }

    public BudgetEntity getBudgetById(int budgetId) {
        return budgetRepository.getBudgetById(budgetId);
    }

    public List<BudgetWalletEntity> getWallets(int budgetId) {
        return budgetRepository.getWallets(budgetId);
    }

    public List<BudgetCategoryEntity> getCategories(int budgetId) {
        return budgetRepository.getCategories(budgetId);
    }

    public List<BudgetCategoryAmountEntity> getCategoryAmounts(int budgetId) {
        return budgetRepository.getCategoryAmounts(budgetId);
    }
}