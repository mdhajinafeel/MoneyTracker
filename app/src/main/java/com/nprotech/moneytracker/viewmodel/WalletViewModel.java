package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.repositories.WalletRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class WalletViewModel extends ViewModel {

    private final WalletRepository walletRepository;
    private final MutableLiveData<Integer> accountId = new MutableLiveData<>();
    private final LiveData<List<WalletEntity>> wallets;

    @Inject
    public WalletViewModel(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
        wallets = Transformations.switchMap(accountId, walletRepository::getAllWallets);
    }

    public void selectAccount(int id) {
        accountId.setValue(id);
    }

    public LiveData<List<WalletEntity>> getWallets() {
        return wallets;
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
}