package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.TransactionAttachmentEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.models.DailyTransModel;
import com.nprotech.moneytracker.repositories.TransactionRepository;
import com.nprotech.moneytracker.repositories.WalletRepository;
import com.nprotech.moneytracker.wrapper.SingleLiveEvent;

import java.util.List;

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
        return transactionRepository.getDailyTransactionData(accountId);
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