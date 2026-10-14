package com.nprotech.moneytracker.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.nprotech.moneytracker.db.MoneyTrackerDatabase;
import com.nprotech.moneytracker.db.dao.AccountCurrencyMappingDao;
import com.nprotech.moneytracker.db.dao.AccountDao;
import com.nprotech.moneytracker.db.dao.TransactionDao;
import com.nprotech.moneytracker.db.dao.WalletDao;
import com.nprotech.moneytracker.db.entites.AccountCurrencyMappingEntity;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccountRepository {

    private final AccountDao accountDao;
    private final AccountCurrencyMappingDao accountCurrencyMappingDao;
    private final TransactionDao transactionDao;
    private final WalletDao walletDao;
    private final BudgetRepository budgetRepository;
    private final MoneyTrackerDatabase database;

    public AccountRepository(MoneyTrackerDatabase database, AccountDao accountDao, AccountCurrencyMappingDao accountCurrencyMappingDao,
                             TransactionDao transactionDao, WalletDao walletDao, BudgetRepository budgetRepository) {
        this.database = database;
        this.accountDao = accountDao;
        this.accountCurrencyMappingDao = accountCurrencyMappingDao;
        this.transactionDao = transactionDao;
        this.walletDao = walletDao;
        this.budgetRepository = budgetRepository;
    }

    public List<AccountEntity> getAllAccounts() {
        return accountDao.getAllAccounts();
    }

    public LiveData<AccountEntity> getAccountById(int accountId) {
        return accountDao.getAccountById(accountId);
    }

    public AccountEntity getAccountDetailById(int accountId) {
        return accountDao.getAccountDetailById(accountId);
    }

    public long saveAccount(AccountEntity account) {
        return accountDao.insert(account);
    }

    public void saveAccountCurrencyMapping(AccountCurrencyMappingEntity accountCurrencyMapping) {
        accountCurrencyMappingDao.insert(accountCurrencyMapping);
    }

    public void updateMapping(AccountCurrencyMappingEntity accountCurrencyMapping) {
        accountCurrencyMappingDao.update(accountCurrencyMapping);
    }

    public LiveData<List<AccountCurrencyMappingEntity>> getAccountCurrencyByAccountId(int accountId) {
        return accountCurrencyMappingDao.getAccountCurrencyByAccountId(accountId);
    }

    public int getLastAccountOrder() {
        return accountDao.getLastAccountOrder();
    }

    public void updateAccount(AccountEntity account) {
        accountDao.updateAccount(account);
    }

    public List<AccountCurrencyMappingEntity> fetchAccountCurrencyByAccountId(int accountId, int page, int pageSize) {
        int offset = page * pageSize;
        return accountCurrencyMappingDao.fetchAccountCurrencyByAccountId(accountId, pageSize, offset);
    }

    public AccountCurrencyMappingEntity fetchAccountBaseCurrencyByAccountId(int accountId) {
        return accountCurrencyMappingDao.fetchAccountBaseCurrencyByAccountId(accountId);
    }

    public AccountCurrencyMappingEntity fetchAccountCurrencyByMappingId(int currencyId, int accountId) {
        return accountCurrencyMappingDao.fetchAccountCurrencyByMappingId(currencyId, accountId);
    }

    public boolean updateAccountCurrencyMapping(int accountId, int currencyId, String currencyCode) {
        return accountCurrencyMappingDao.updateAccountCurrencyMapping(accountId, currencyId, currencyCode) > 0;
    }

    public LiveData<List<AccountEntity>> getFilteredAccounts(int sortType) {
        return accountDao.getFilteredAccounts(sortType);
    }

    public LiveData<List<AccountEntity>> getFilteredArchivedAccounts(int sortType) {
        return accountDao.getFilteredArchivedAccounts(sortType);
    }

    public void archiveAccount(int accountId, boolean isArchived) {
        database.runInTransaction(() -> accountDao.archiveAccount(accountId, isArchived, isArchived ? System.currentTimeMillis() : 0));
    }

    public void setDefaultAccount(int accountId) {
        database.runInTransaction(() -> {
            accountDao.clearDefaultAccount();
            accountDao.setDefaultAccount(accountId);
        });
    }

    public boolean deleteAccount(int accountId) {

        final boolean[] deleted = {false};

        database.runInTransaction(() -> {

            // Validate before modifying any associated records.
            if (accountDao.canDeleteAccount(accountId) == 0) {
                return;
            }

            // Soft-delete associated transactions and recalculate
            // any affected wallet balances before deleting the wallets.
            deleteAccountTransactionsInternal(accountId);

            // Deactivate the account's currency mappings.
            accountCurrencyMappingDao.deactivateAccountCurrencyMappings(accountId);

            // Soft-delete all wallets belonging to this account,
            // including default and archived wallets.
            walletDao.deleteAllWalletsForAccount(accountId);

            // Soft-delete the account.
            deleted[0] = accountDao.deleteAccount(accountId) > 0;
        });

        // Run alert checks once, after the transaction has committed.
        if (deleted[0]) {
            budgetRepository.checkBudgetAlerts();
        }

        return deleted[0];
    }

    private void deleteAccountTransactionsInternal(int accountId) {

        long updatedAt = System.currentTimeMillis();

        List<TransactionEntity> transactions = transactionDao.getTransactionsForAccount(accountId);

        Set<Integer> affectedWalletIds = getWalletIds(transactions);

        // Delete all transactions
        for (TransactionEntity transaction : transactions) {
            int rows = transactionDao.deleteTransaction(transaction.tempTransactionServerId, updatedAt);

            if(rows > 0) {
                budgetRepository.checkBudgetAlerts();
            }
        }

        // Recalculate affected wallets
        for (Integer affectedWalletId : affectedWalletIds) {
            recalculateWalletBalance(affectedWalletId, accountId);
        }

        // Recalculate account
        recalculateAccountBalance(accountId);
    }

    @NonNull
    private static Set<Integer> getWalletIds(List<TransactionEntity> transactions) {
        Set<Integer> affectedWalletIds = new HashSet<>();
        for (TransactionEntity transaction : transactions) {
            affectedWalletIds.add(transaction.walletId);
            if(transaction.fromWalletId > 0) {
                affectedWalletIds.add(transaction.fromWalletId);
            }
        }
        return affectedWalletIds;
    }

    private void recalculateWalletBalance(int walletId, int accountId) {

        WalletEntity wallet = walletDao.getWalletByWalletId(walletId);

        if (wallet == null) {
            return;
        }

        List<TransactionEntity> transactions = transactionDao.getTransactionsForWallet(walletId, accountId);

        double balance = calculateWalletBalance(walletId, wallet.initialAmount, transactions);

        walletDao.updateWalletById(walletId, balance);
    }

    private double calculateWalletBalance(int walletId, double openingBalance, List<TransactionEntity> transactions) {

        double balance = openingBalance;
        for (TransactionEntity transaction : transactions) {

            if (transaction.isDeleted) {
                continue;
            }

            if (transaction.type == TransactionEntity.TYPE_INCOME) {
                if (transaction.walletId == walletId) {
                    balance += transaction.amount;
                }
            } else if (transaction.type == TransactionEntity.TYPE_EXPENSE) {
                if (transaction.walletId == walletId) {
                    balance -= transaction.amount;
                }
            } else if (transaction.type == TransactionEntity.TYPE_TRANSFER) {

                // Money received
                if (transaction.walletId == walletId) {
                    balance += transaction.convertedAmount;
                }

                // Money sent
                if (transaction.fromWalletId != null && transaction.fromWalletId == walletId) {
                    balance -= transaction.amount;
                }
            }
        }

        return balance;
    }

    private void recalculateAccountBalance(int accountId) {
        double accountBalance = walletDao.getAccountBalance(accountId);
        accountDao.updateAccountById(accountId, accountBalance);
    }

    public void deleteWalletTransactions(int accountId) {
        database.runInTransaction(() -> deleteAccountTransactionsInternal(accountId));
    }

    public int getDefaultAccountId() {
        return accountDao.getDefaultAccountId();
    }

    public boolean isAccountAvailable(int accountId) {
        return accountDao.isAccountAvailable(accountId) > 0;
    }
}