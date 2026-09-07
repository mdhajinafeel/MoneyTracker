package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.models.DailyTransModel;
import com.nprotech.moneytracker.models.TransactionWithDetails;
import com.nprotech.moneytracker.repositories.TransactionRepository;
import com.nprotech.moneytracker.repositories.WalletRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class WalletViewModel extends ViewModel {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final MutableLiveData<Integer> accountId = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final int PAGE_SIZE = 100;
    private int currentPage = 0, categoryId, selectAccountId, walletId;
    private boolean loading = false, hasMore = true;
    private final MutableLiveData<List<DailyTransModel>> categoryTransactions = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<WalletEntity> walletLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> walletSortType =  new MutableLiveData<>(1);
    private final MutableLiveData<Integer> walletArchivedSortType =  new MutableLiveData<>(1);
    private final MediatorLiveData<List<WalletEntity>> wallets = new MediatorLiveData<>();
    private final MediatorLiveData<List<WalletEntity>> archivedWallets = new MediatorLiveData<>();
    private LiveData<List<WalletEntity>> walletSource;
    private LiveData<List<WalletEntity>> walletArchivedSource;

    @Inject
    public WalletViewModel(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;

        wallets.addSource(accountId, id -> loadWallets());
        wallets.addSource(walletSortType, sortType -> loadWallets());

        archivedWallets.addSource(accountId, id -> loadArchivedWallets());
        archivedWallets.addSource(walletArchivedSortType, sortType -> loadArchivedWallets());
    }

    public void selectAccount(int id) {
        accountId.setValue(id);
    }

    public LiveData<List<WalletEntity>> getWallets() {
        return wallets;
    }

    public LiveData<List<WalletEntity>> getArchivedWallets() {
        return archivedWallets;
    }

    public long saveWallet(WalletEntity wallet) {
        return walletRepository.saveWallet(wallet);
    }

    public void updateWallet(WalletEntity wallet) {
        walletRepository.updateWallet(wallet);
    }

    public int getMaxWalletOrdering(int accountId) {
        return walletRepository.getMaxWalletOrdering(accountId);
    }

    public WalletEntity getWalletByWalletId(int walletId) {
        return walletRepository.getWalletByWalletId(walletId);
    }

    public LiveData<WalletEntity> getWalletLiveData() {
        return walletLiveData;
    }

    public void loadWallet(int walletId) {
        executor.execute(() -> {
            WalletEntity wallet = walletRepository.getWalletByWalletId(walletId);
            walletLiveData.postValue(wallet);
        });
    }

    public List<WalletEntity> getWalletsByAccountAndCurrency(int accountId, String currencyCode) {
        return walletRepository.getWalletsByAccountAndCurrency(accountId, currencyCode);
    }

    public double getAccountBalance(int accountId) {
        return walletRepository.getAccountBalance(accountId);
    }

    public void loadTransactions(int selectAccountId, int walletId, int categoryId) {
        this.selectAccountId = selectAccountId;
        this.walletId = walletId;
        this.categoryId = categoryId;
        currentPage = 0;
        hasMore = true;
        categoryTransactions.setValue(new ArrayList<>());
        loadNextPage();
    }

    public void loadNextPage() {

        if (loading || !hasMore)
            return;

        loading = true;

        executor.execute(() -> {
            List<TransactionWithDetails> page = transactionRepository.getTransactionsByCategory(selectAccountId, walletId, categoryId, currentPage, PAGE_SIZE);

            if (page.size() < PAGE_SIZE) {
                hasMore = false;
            }

            List<DailyTransModel> grouped = groupTransactions(page);
            List<DailyTransModel> current = categoryTransactions.getValue();

            if (current == null) {
                current = new ArrayList<>();
            }

            merge(current, grouped);
            categoryTransactions.postValue(current);
            currentPage++;
            loading = false;
        });
    }

    public LiveData<List<DailyTransModel>> getCategoryTransactions() {
        return categoryTransactions;
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

    public void setDefaultWallet(int walletId, int accountId) {
        walletRepository.setDefaultWallet(walletId, accountId);
    }

    public void archiveWallet(int walletId, int accountId, boolean isArchived) {
        walletRepository.archiveWallet(walletId, accountId, isArchived);
    }

    public void deleteWallet(int walletId, int accountId) {
        walletRepository.deleteWallet(walletId, accountId);
    }

    public void deleteWalletTransactions(int walletId, int accountId) {
        walletRepository.deleteWalletTransactions(walletId, accountId);
    }

    private void loadWallets() {

        Integer id = accountId.getValue();
        Integer sortType = walletSortType.getValue();

        if (id == null || sortType == null) {
            return;
        }

        if (walletSource != null) {
            wallets.removeSource(walletSource);
        }

        walletSource = walletRepository.getFilteredWallets(id, sortType);

        wallets.addSource(walletSource, wallets::setValue);
    }

    private void loadArchivedWallets() {

        Integer id = accountId.getValue();
        Integer sortType = walletArchivedSortType.getValue();

        if (id == null || sortType == null) {
            return;
        }

        if (walletArchivedSource != null) {
            archivedWallets.removeSource(walletArchivedSource);
        }

        walletArchivedSource = walletRepository.getFilteredArchivedWallets(id, sortType);

        archivedWallets.addSource(walletArchivedSource, archivedWallets::setValue);
    }

    public void setWalletSort(int sortType) {
        walletSortType.setValue(sortType);
    }

    public void setWalletArchivedSort(int sortType) {
        walletArchivedSortType.setValue(sortType);
    }
}