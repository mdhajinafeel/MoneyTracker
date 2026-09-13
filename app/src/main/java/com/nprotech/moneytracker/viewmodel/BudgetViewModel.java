package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.BudgetCategoryAmountEntity;
import com.nprotech.moneytracker.db.entites.BudgetCategoryEntity;
import com.nprotech.moneytracker.db.entites.BudgetEntity;
import com.nprotech.moneytracker.db.entites.BudgetWalletEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.models.BudgetWithDetails;
import com.nprotech.moneytracker.models.DailyTransModel;
import com.nprotech.moneytracker.models.TransactionWithDetails;
import com.nprotech.moneytracker.repositories.BudgetRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BudgetViewModel extends ViewModel {

    private final BudgetRepository budgetRepository;
    private final MutableLiveData<List<TransactionWithDetails>> transactions = new MutableLiveData<>();
    private int accountId, currentPage = 0;
    private long startDate, endDate;
    private List<Integer> categoryIds, walletIds;
    private boolean allCategories, allWallets, isRecent, loading = false, hasMore = true;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<DailyTransModel>> dailyTransactions = new MutableLiveData<>(new ArrayList<>());
    private static final int PAGE_SIZE = 100;

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

    public LiveData<List<BudgetWithDetails>> getBudgets(int accountId, boolean isArchived, boolean isPaused, boolean isCompleted) {
        return budgetRepository.getBudgets(accountId, isArchived, isPaused, isCompleted);
    }

    public LiveData<BudgetWithDetails> getBudgetDetailById(int budgetId) {
        return budgetRepository.getBudgetDetailById(budgetId);
    }

    public boolean deleteBudget(int budgetId) {
        return budgetRepository.deleteBudget(budgetId);
    }

    public boolean archiveRestoreBudget(int budgetId, boolean isArchive, boolean isPause) {
        return budgetRepository.archiveRestoreBudget(budgetId, isArchive, isPause);
    }

    public boolean pauseBudget(int budgetId, boolean isPause) {
        return budgetRepository.pauseBudget(budgetId, isPause);
    }

    public void loadTransactions(int accountId, long startDate, long endDate, List<Integer> categoryIds, List<Integer> walletIds,
                                 boolean allCategories, boolean allWallets, boolean isRecent) {
        List<TransactionWithDetails> list = budgetRepository.getTransactionsForBudget(accountId, startDate, endDate, categoryIds,
                walletIds, allCategories, allWallets, isRecent, currentPage, PAGE_SIZE);
        transactions.setValue(list);
    }

    public void loadAllTransactions(int accountId, long startDate, long endDate, List<Integer> categoryIds, List<Integer> walletIds,
                                 boolean allCategories, boolean allWallets, boolean isRecent) {

        this.accountId = accountId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.categoryIds = categoryIds;
        this.walletIds = walletIds;
        this.allCategories = allCategories;
        this.allWallets = allWallets;
        currentPage = 0;
        hasMore = true;
        dailyTransactions.setValue(new ArrayList<>());
        loadNextPage();
    }

    public void loadNextPage() {

        if (loading || !hasMore)
            return;

        loading = true;

        executor.execute(() -> {
            List<TransactionWithDetails> list = budgetRepository.getTransactionsForBudget(accountId, startDate, endDate, categoryIds,
                    walletIds, allCategories, allWallets, isRecent, currentPage, PAGE_SIZE);

            if (list.size() < PAGE_SIZE) {
                hasMore = false;
            }

            List<DailyTransModel> grouped = groupTransactions(list);
            List<DailyTransModel> current = dailyTransactions.getValue();

            if (current == null) {
                current = new ArrayList<>();
            }

            merge(current, grouped);
            dailyTransactions.postValue(current);
            currentPage++;
            loading = false;
        });
    }

    public LiveData<List<TransactionWithDetails>> getTransactions() {
        return transactions;
    }

    public List<Integer> getCategoryIdsByBudgetId(int budgetId) {
        return budgetRepository.getCategoryIdsByBudgetId(budgetId);
    }

    public List<Integer> getWalletIdsByBudgetId(int budgetId) {
        return budgetRepository.getWalletIdsByBudgetId(budgetId);
    }

    public LiveData<List<DailyTransModel>> getDailyTransactions() {
        return dailyTransactions;
    }

    private void merge(List<DailyTransModel> current, List<DailyTransModel> newItems) {

        if (newItems == null || newItems.isEmpty()) {
            return;
        }

        // First load
        if (current.isEmpty()) {
            current.addAll(newItems);
            return;
        }

        DailyTransModel lastCurrent = current.get(current.size() - 1);
        DailyTransModel firstNew = newItems.get(0);

        // Same day? Merge them.
        if (lastCurrent.getYear() == firstNew.getYear()
                && lastCurrent.getMonth() == firstNew.getMonth()
                && lastCurrent.getDay() == firstNew.getDay()) {

            lastCurrent.setAmount(lastCurrent.getAmount() + firstNew.getAmount());
            lastCurrent.getTransactions().addAll(firstNew.getTransactions());

            // Remove merged header
            newItems.remove(0);
        }

        current.addAll(newItems);
    }

    private List<DailyTransModel> groupTransactions(List<TransactionWithDetails> list) {
        Map<String, DailyTransModel> map = new LinkedHashMap<>();
        for (TransactionWithDetails item : list) {

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(item.transaction.transactionDate);

            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);

            String key = year + "-" + month + "-" + day;

            DailyTransModel model = map.get(key);

            if (model == null) {
                model = new DailyTransModel();
                model.setDay(day);
                model.setMonth(month);
                model.setYear(year);
                model.setCurrencySymbol(item.currencySymbol);
                model.setAmount(0);
                map.put(key, model);
            }

            // Calculate daily total
            switch (item.transaction.type) {

                case TransactionEntity.TYPE_INCOME:
                    model.setAmount(model.getAmount() + (item.transaction.amount * item.exchangeRate));
                    break;

                case TransactionEntity.TYPE_EXPENSE:
                    model.setAmount(model.getAmount() - (item.transaction.amount * item.exchangeRate));
                    break;

                case TransactionEntity.TYPE_TRANSFER:
                    // Transfer does not affect total wealth
                    break;
            }

            // Deduct transfer fee
            if (item.feeTransaction != null) {
                model.setAmount(model.getAmount() - item.feeTransaction.amount);
            }

            model.getTransactions().add(item);
        }
        return new ArrayList<>(map.values());
    }
}