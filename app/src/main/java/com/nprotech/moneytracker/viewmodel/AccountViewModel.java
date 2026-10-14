package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.AccountCurrencyMappingEntity;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.PreferenceManager;
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
    private final MutableLiveData<List<AccountEntity>> accountList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> accountSortType = new MutableLiveData<>(1);
    private final MutableLiveData<Integer> accountArchivedSortType = new MutableLiveData<>(1);
    private final MediatorLiveData<List<AccountEntity>> accounts = new MediatorLiveData<>();
    private LiveData<List<AccountEntity>> accountSource;
    private LiveData<List<AccountEntity>> accountArchivedSource;
    private final MutableLiveData<AccountEntity> accountLiveData = new MutableLiveData<>();
    private final MediatorLiveData<List<AccountEntity>> archivedAccounts = new MediatorLiveData<>();

    @Inject
    public AccountViewModel(AccountRepository accountRepository, WalletRepository walletRepository) {
        this.accountRepository = accountRepository;
        this.walletRepository = walletRepository;

        accounts.addSource(accountSortType, id -> loadAccounts());
        selectedAccount = Transformations.switchMap(selectedAccountId, accountRepository::getAccountById);
        archivedAccounts.addSource(accountArchivedSortType, sortType -> loadArchivedAccounts());
    }

    public void getAllAccounts() {
        accountList.setValue(accountRepository.getAllAccounts());
    }

    public LiveData<List<AccountEntity>> getAccountList() {
        return accountList;
    }

    public long saveAccount(AccountEntity account) {
        return accountRepository.saveAccount(account);
    }

    public void saveAccountCurrencyMapping(AccountCurrencyMappingEntity accountCurrencyMapping) {
        accountRepository.saveAccountCurrencyMapping(accountCurrencyMapping);
    }

    public void updateMapping(AccountCurrencyMappingEntity accountCurrencyMapping) {
        accountRepository.updateMapping(accountCurrencyMapping);
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

        if (loading || !hasMore) return;

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

    public AccountCurrencyMappingEntity fetchAccountCurrencyByMappingId(int currencyId, int accountId) {
        return accountRepository.fetchAccountCurrencyByMappingId(currencyId, accountId);
    }

    public void setAccountSort(int sortType) {
        accountSortType.setValue(sortType);
    }

    private void loadAccounts() {
        Integer sortType = accountSortType.getValue();
        if (sortType == null) {
            return;
        }
        if (accountSource != null) {
            accounts.removeSource(accountSource);
        }
        accountSource = accountRepository.getFilteredAccounts(sortType);
        accounts.addSource(accountSource, accounts::setValue);
    }

    public LiveData<List<AccountEntity>> getAccounts() {
        return accounts;
    }

    public void setDefaultAccount(int accountId) {
        accountRepository.setDefaultAccount(accountId);
    }

    public void archiveAccount(int accountId, boolean isArchived) {
        accountRepository.archiveAccount(accountId, isArchived);
    }

    public boolean deleteAccount(int accountId) {
        return accountRepository.deleteAccount(accountId);
    }

    public void deleteWalletTransactions(int accountId) {
        accountRepository.deleteWalletTransactions(accountId);
    }

    public LiveData<AccountEntity> getAccountLiveData() {
        return accountLiveData;
    }

    public void loadAccount(int accountId) {
        executor.execute(() -> {
            AccountEntity account = accountRepository.getAccountDetailById(accountId);
            accountLiveData.postValue(account);
        });
    }

    private void loadArchivedAccounts() {

        Integer sortType = accountArchivedSortType.getValue();

        if (sortType == null) {
            return;
        }

        if (accountArchivedSource != null) {
            archivedAccounts.removeSource(accountArchivedSource);
        }

        accountArchivedSource = accountRepository.getFilteredArchivedAccounts(sortType);

        archivedAccounts.addSource(accountArchivedSource, archivedAccounts::setValue);
    }

    public LiveData<List<AccountEntity>> getArchivedAccounts() {
        return archivedAccounts;
    }

    public int getDefaultAccountId() {
        return accountRepository.getDefaultAccountId();
    }

    public void validateSelectedAccount() {
        executor.execute(() -> {
            int currentAccountId = PreferenceManager.INSTANCE.getAccountId();
            boolean isValid = currentAccountId > 0 && accountRepository.isAccountAvailable(currentAccountId);

            if (isValid) {
                selectedAccountId.postValue(currentAccountId);
                return;
            }

            int fallbackAccountId = accountRepository.getDefaultAccountId();

            if (fallbackAccountId <= 0) {
                List<AccountEntity> availableAccounts = accountRepository.getAllAccounts();
                if (availableAccounts != null && !availableAccounts.isEmpty()) {
                    fallbackAccountId = availableAccounts.get(0).id;
                }
            }

            PreferenceManager.INSTANCE.setAccountId(fallbackAccountId);
            selectedAccountId.postValue(fallbackAccountId);
        });
    }
}