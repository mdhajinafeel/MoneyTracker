package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.TransactionAttachmentEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.models.DailyTransModel;
import com.nprotech.moneytracker.models.TransactionWithDetails;
import com.nprotech.moneytracker.repositories.TransactionRepository;
import com.nprotech.moneytracker.repositories.WalletRepository;
import com.nprotech.moneytracker.wrapper.SingleLiveEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class TransactionViewModel extends ViewModel {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final SingleLiveEvent<Boolean> dataSavedStatus = new SingleLiveEvent<>();
    private final SingleLiveEvent<Boolean> dataUpdatedStatus = new SingleLiveEvent<>();

    @Inject
    public TransactionViewModel(TransactionRepository transactionRepository, WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }


    public LiveData<List<DailyTransModel>> getDailyTransactionData(int accountId) {
        return Transformations.map(
                transactionRepository.getTransactions(accountId),
                this::groupTransactions
        );
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
                model.setType(item.transaction.type);
                map.put(key, model);
            }
            model.setAmount(model.getAmount() + item.transaction.amount);
            model.getTransactions().add(item);
        }
        return new ArrayList<>(map.values());
    }

    public void saveTransactionAttachment(List<TransactionAttachmentEntity> transactionAttachments) {
        transactionRepository.saveTransactionAttachment(transactionAttachments);
    }

    public void saveTransaction(TransactionEntity transaction, WalletEntity wallet, AccountEntity account) {
        long transId = transactionRepository.saveTransaction(transaction);
        AppLogger.d(getClass(), "Returned transId = " + transId);
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

    public void updateTransaction(TransactionEntity transaction, WalletEntity wallet, AccountEntity account) {

        int rows = transactionRepository.updateTransaction(transaction);
        AppLogger.d(getClass(), "Updated rows = " + rows);

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

    public WalletEntity getWalletByWalletId(int walletId) {
        return walletRepository.getWalletByWalletId(walletId);
    }
}