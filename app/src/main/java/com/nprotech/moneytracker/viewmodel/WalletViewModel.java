package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.repositories.WalletRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class WalletViewModel extends ViewModel {

    private final WalletRepository walletRepository;

    @Inject
    public WalletViewModel(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public LiveData<List<WalletEntity>> getAllWallets() {
        return walletRepository.getAllWallets();
    }

    public long saveWallet(WalletEntity wallet) {
        return walletRepository.saveWallet(wallet);
    }

    public int getLastWalletOrder() {
        return walletRepository.getLastWalletOrder();
    }
}