package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.AccountCurrencyMappingEntity;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.repositories.AccountRepository;
import com.nprotech.moneytracker.repositories.WalletRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AccountViewModel extends ViewModel {

    private final AccountRepository accountRepository;
    private final WalletRepository walletRepository;
    private final MutableLiveData<Integer> selectedAccountId = new MutableLiveData<>();
    private final LiveData<AccountEntity> selectedAccount;
    private boolean loading = false, hasMore = true;
    private int currentPage = 0, accountId;
    private static final int PAGE_SIZE = 100;
    private final MutableLiveData<List<AccountCurrencyMappingEntity>> accountCurrencyMapping = new MutableLiveData<>(new ArrayList<>());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public AccountViewModel(AccountRepository accountRepository, WalletRepository walletRepository) {
        this.accountRepository = accountRepository;
        this.walletRepository = walletRepository;

        selectedAccount = Transformations.switchMap(
                selectedAccountId,
                accountRepository::getAccountById
        );
    }

    public LiveData<List<AccountEntity>> getAllAccounts() {
        return accountRepository.getAllAccounts();
    }

    public long saveAccount(AccountEntity account) {
        return accountRepository.saveAccount(account);
    }

    public void saveAccountCurrencyMapping(AccountCurrencyMappingEntity accountCurrencyMapping) {
        accountRepository.saveAccountCurrencyMapping(accountCurrencyMapping);
    }

    public LiveData<List<AccountCurrencyMappingEntity>> getAccountCurrencyByAccountId(int accountId) {
        return accountRepository.getAccountCurrencyByAccountId(accountId);
    }

    public int getLastAccountOrder() {
        return accountRepository.getLastAccountOrder();
    }

    public LiveData<AccountEntity> getSelectedAccount() {
        return selectedAccount;
    }

    public void selectAccount(int accountId) {
        selectedAccountId.setValue(accountId);
    }

    public AccountEntity getAccountDetailById(int accountId) {
        return accountRepository.getAccountDetailById(accountId);
    }

    public List<WalletEntity> getWalletsByAccountId(int accountId) {
        return walletRepository.getWalletsByAccountId(accountId);
    }

    public void updateAccount(AccountEntity account) {
        accountRepository.updateAccount(account);
    }

    public void loadAccountCurrencies(int accountId) {
        this.accountId = accountId;
        currentPage = 0;
        hasMore = true;
        accountCurrencyMapping.setValue(new ArrayList<>());
        loadNextPage();
    }

    public void loadNextPage() {

        if (loading || !hasMore)
            return;

        loading = true;

        executor.execute(() -> {
            List<AccountCurrencyMappingEntity> page = accountRepository.fetchAccountCurrencyByAccountId(accountId, currentPage, PAGE_SIZE);

            if (page.size() < PAGE_SIZE) {
                hasMore = false;
            }

            accountCurrencyMapping.postValue(page);
            currentPage++;
            loading = false;
        });
    }

    public LiveData<List<AccountCurrencyMappingEntity>> getAccountCurrencyMapping() {
        return accountCurrencyMapping;
    }

    public boolean updateAccountCurrencyMapping(int accountId, int currencyId, String currencyCode) {
        return accountRepository.updateAccountCurrencyMapping(accountId, currencyId, currencyCode);
    }

    public void updateAccountBalance(int accountId, double balance) {
        executor.execute(() -> {
            AccountEntity account = accountRepository.getAccountDetailById(accountId);
            if (account != null) {
                account.balance = balance;
                accountRepository.updateAccount(account);
            }
        });
    }

    public AccountCurrencyMappingEntity fetchAccountBaseCurrencyByAccountId(int accountId) {
        return accountRepository.fetchAccountBaseCurrencyByAccountId(accountId);
    }
}