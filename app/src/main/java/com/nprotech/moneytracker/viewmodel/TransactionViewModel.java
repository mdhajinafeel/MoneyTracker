package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.TransactionAttachmentEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.models.BalanceSummaryModel;
import com.nprotech.moneytracker.models.DailyTransModel;
import com.nprotech.moneytracker.models.TransactionCategoryModel;
import com.nprotech.moneytracker.models.TransactionTypeAmountModel;
import com.nprotech.moneytracker.models.TransactionWithDetails;
import com.nprotech.moneytracker.repositories.TransactionRepository;
import com.nprotech.moneytracker.wrapper.SingleLiveEvent;

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
public class TransactionViewModel extends ViewModel {

    private final TransactionRepository transactionRepository;
    private final SingleLiveEvent<Boolean> dataSavedStatus = new SingleLiveEvent<>();
    private final SingleLiveEvent<Boolean> dataUpdatedStatus = new SingleLiveEvent<>();
    private final SingleLiveEvent<Boolean> dataDeletedStatus = new SingleLiveEvent<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<DailyTransModel>> dailyTransactions = new MutableLiveData<>(new ArrayList<>());
    private static final int PAGE_SIZE = 100;
    private int currentPage = 0, accountId;
    private boolean loading = false, hasMore = true;
    private long startDate, endDate;

    @Inject
    public TransactionViewModel(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
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

    public void saveTransactionAttachment(List<TransactionAttachmentEntity> transactionAttachments) {
        transactionRepository.saveTransactionAttachment(transactionAttachments);
    }

    public void saveTransaction(TransactionEntity transaction, WalletEntity wallet, AccountEntity account) {

        long transId = transactionRepository.saveTransaction(transaction);
        if (transId > 0) {

            // UPDATE WALLET
            if (wallet != null) {
                transactionRepository.updateWallet(wallet);
            }

            // UPDATE ACCOUNT
            if (account != null) {
                transactionRepository.updateAccount(account);
            }

            dataSavedStatus.postValue(true);
        } else {
            dataSavedStatus.postValue(false);
        }
    }

    public void saveTransaction(TransactionEntity transaction) {

        long transId = transactionRepository.saveTransaction(transaction);
        if (transId > 0) {
            dataSavedStatus.postValue(true);
        } else {
            dataSavedStatus.postValue(false);
        }
    }

    public void updateTransaction(TransactionEntity transaction, WalletEntity wallet, AccountEntity account) {

        int rows = transactionRepository.updateTransaction(transaction);
        if (rows > 0) {
            // UPDATE WALLET
            if (wallet != null) {
                transactionRepository.updateWallet(wallet);
            }

            // UPDATE ACCOUNT
            if (account != null) {
                transactionRepository.updateAccount(account);
            }

            dataUpdatedStatus.postValue(true);
        } else {
            dataUpdatedStatus.postValue(false);
        }
    }

    public List<TransactionAttachmentEntity> getTransactionAttachments(String tempTransactionServerId) {
        return transactionRepository.getTransactionAttachments(tempTransactionServerId);
    }

    public void deleteAttachment(String attachmentPath, String tempTransactionServerId) {
        transactionRepository.deleteAttachment(attachmentPath, tempTransactionServerId);
    }

    public LiveData<Boolean> getDataSavedStatus() {
        return dataSavedStatus;
    }

    public LiveData<Boolean> getDataUpdatedStatus() {
        return dataUpdatedStatus;
    }

    public TransactionEntity getTransactionById(String tempTransactionServerId) {
        return transactionRepository.getTransactionById(tempTransactionServerId);
    }

    public List<TransactionTypeAmountModel> getTransactionAmountByType(int walletId) {
        return transactionRepository.getTransactionAmountByType(walletId);
    }

    public LiveData<List<TransactionCategoryModel>> getTransactionAmountByCategory(int walletId) {
        return transactionRepository.getTransactionAmountByCategory(walletId);
    }

    public TransactionWithDetails getTransactions(String tempTransactionServerId) {
        return transactionRepository.getTransactions(tempTransactionServerId);
    }

    public LiveData<Boolean> getDeleteStatus() {
        return dataDeletedStatus;
    }

    public void deleteTransaction(TransactionEntity transaction, WalletEntity wallet, AccountEntity account) {
        executor.execute(() -> {
            boolean success = transactionRepository.deleteTransaction(transaction, wallet, account);
            dataDeletedStatus.postValue(success);
        });
    }

    public void saveTransferTransaction(TransactionEntity transaction, TransactionEntity feeTransactionActivity,
                                        WalletEntity fromWallet, WalletEntity toWallet, AccountEntity account) {
        executor.execute(() -> {
            transactionRepository.saveTransferTransaction(transaction, feeTransactionActivity, fromWallet, toWallet, account);
            dataSavedStatus.postValue(true);
        });
    }

    public void updateTransferTransaction(TransactionEntity transferTransaction, TransactionEntity feeTransaction, TransactionEntity oldFeeTransaction,
                                          WalletEntity oldFromWallet, WalletEntity oldToWallet, WalletEntity newFromWallet, WalletEntity newToWallet,
                                          AccountEntity account) {
        executor.execute(() -> {
            transactionRepository.updateTransferTransaction(transferTransaction, feeTransaction, oldFeeTransaction, oldFromWallet, oldToWallet, newFromWallet, newToWallet, account);
            dataUpdatedStatus.postValue(true);
        });
    }

    public TransactionEntity getFeeTransaction(String parentTransactionId) {
        return transactionRepository.getFeeTransaction(parentTransactionId);
    }

    public void deleteFeeTransaction(TransactionEntity transaction) {
        transactionRepository.deleteFeeTransaction(transaction);
    }

    public void loadTransactions(int accountId, long startDate, long endDate) {
        this.accountId = accountId;
        this.startDate = startDate;
        this.endDate = endDate;
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
            List<TransactionWithDetails> page = transactionRepository.getTransactionsPaged(accountId, startDate, endDate, currentPage, PAGE_SIZE);

            if (page.size() < PAGE_SIZE) {
                hasMore = false;
            }

            List<DailyTransModel> grouped = groupTransactions(page);
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

    public LiveData<BalanceSummaryModel> accountSummaryById(int accountId) {
        return transactionRepository.accountSummaryById(accountId);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }
}