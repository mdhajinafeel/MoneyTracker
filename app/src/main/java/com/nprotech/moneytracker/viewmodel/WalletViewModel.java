package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.repositories.AccountRepository;
import com.nprotech.moneytracker.repositories.TransactionRepository;
import com.nprotech.moneytracker.repositories.WalletRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class WalletViewModel extends ViewModel {

    private final WalletRepository walletRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final MutableLiveData<Integer> accountId = new MutableLiveData<>();
    private final LiveData<List<WalletEntity>> wallets;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public WalletViewModel(WalletRepository walletRepository, AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
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

    public void updateWalletAndAccount(WalletEntity wallet, AccountEntity account) {
        walletRepository.updateWallet(wallet);
        transactionRepository.updateAccount(account);
    }

    public int getMaxWalletOrdering(int accountId) {
        return walletRepository.getMaxWalletOrdering(accountId);
    }

    public WalletEntity getWalletByWalletId(int walletId) {
        return walletRepository.getWalletByWalletId(walletId);
    }

    public List<WalletEntity> getWalletsByAccountAndCurrency(int accountId, String currencyCode) {
        return walletRepository.getWalletsByAccountAndCurrency(accountId, currencyCode);
    }

    public double getAccountBalance(int accountId) {
        return walletRepository.getAccountBalance(accountId);
    }
}