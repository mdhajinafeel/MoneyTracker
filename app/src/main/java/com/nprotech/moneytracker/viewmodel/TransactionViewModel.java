package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.TransactionAttachmentEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.models.CalendarSummaryModel;
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
    private final MutableLiveData<Integer> selectedAccountId = new MutableLiveData<>();
    private final LiveData<List<DailyTransModel>> dailyTransactions;
    private final MutableLiveData<CalendarRange> calendarRange = new MutableLiveData<>();
    private final LiveData<List<CalendarSummaryModel>> calendarSummary;
    private final LiveData<CalendarSummaryModel> calendarHeader;

    @Inject
    public TransactionViewModel(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;

        dailyTransactions = Transformations.switchMap(selectedAccountId,
                accountId -> Transformations.map(transactionRepository.getTransactions(accountId), this::groupTransactions));

        calendarSummary = Transformations.switchMap(calendarRange, range -> transactionRepository.getCalendarSummary(range.accountId, range.startDate, range.endDate));
        calendarHeader = Transformations.switchMap(calendarRange, range -> transactionRepository.getCalendarHeader(range.accountId, range.startDate, range.endDate));
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
                    model.setAmount(model.getAmount() + item.transaction.amount);
                    break;

                case TransactionEntity.TYPE_EXPENSE:
                    model.setAmount(model.getAmount() - item.transaction.amount);
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

    public void selectAccount(int accountId) {
        selectedAccountId.setValue(accountId);
    }

    public LiveData<List<DailyTransModel>> getDailyTransactions() {
        return dailyTransactions;
    }

    public static class CalendarRange {
        public int accountId;
        public long startDate, endDate;

        public CalendarRange(int accountId, long startDate, long endDate) {
            this.accountId = accountId;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    public void loadCalendar(int accountId, long startDate, long endDate) {
        calendarRange.setValue(new CalendarRange(accountId, startDate, endDate));
    }

    public LiveData<List<CalendarSummaryModel>> getCalendarSummary() {
        return calendarSummary;
    }

    public LiveData<CalendarSummaryModel> getCalendarHeader() {
        return calendarHeader;
    }
}