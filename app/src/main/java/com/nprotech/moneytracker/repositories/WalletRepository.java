package com.nprotech.moneytracker.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.nprotech.moneytracker.db.MoneyTrackerDatabase;
import com.nprotech.moneytracker.db.dao.AccountDao;
import com.nprotech.moneytracker.db.dao.TransactionDao;
import com.nprotech.moneytracker.db.dao.WalletDao;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WalletRepository {

    private final WalletDao walletDao;
    private final TransactionDao transactionDao;
    private final AccountDao accountDao;
    private final MoneyTrackerDatabase database;

    public WalletRepository(MoneyTrackerDatabase database, WalletDao walletDao, TransactionDao transactionDao, AccountDao accountDao) {
        this.database = database;
        this.walletDao = walletDao;
        this.transactionDao = transactionDao;
        this.accountDao = accountDao;
    }

    public LiveData<List<WalletEntity>> getFilteredWallets(int accountId, int sortType) {
        return walletDao.getFilteredWallets(accountId, sortType);
    }

    public LiveData<List<WalletEntity>> getFilteredArchivedWallets(int accountId, int sortType) {
        return walletDao.getFilteredArchivedWallets(accountId, sortType);
    }

    public long saveWallet(WalletEntity wallet) {
        return walletDao.insert(wallet);
    }

    public List<WalletEntity> getWalletsByAccountId(int accountId) {
        return walletDao.getWalletsByAccountId(accountId);
    }

    public WalletEntity getWalletByWalletId(int walletId) {
        return walletDao.getWalletByWalletId(walletId);
    }

    public void updateWallet(WalletEntity wallet) {
        walletDao.updateWallet(wallet);
    }

    public int getMaxWalletOrdering(int accountId) {
        return walletDao.getMaxWalletOrdering(accountId);
    }

    public List<WalletEntity> getWalletsByAccountAndCurrency(int accountId, String currencyCode) {
        return walletDao.getWalletsByAccountAndCurrency(accountId, currencyCode);
    }

    public double getAccountBalance(int accountId) {
        return walletDao.getAccountBalance(accountId);
    }

    public void setDefaultWallet(int walletId, int accountId) {
        database.runInTransaction(() -> {
            walletDao.clearDefaultWallet(accountId);
            walletDao.setDefaultWallet(walletId, accountId);
        });
    }

    public void archiveWallet(int walletId, int accountId, boolean isArchived) {
        database.runInTransaction(() -> walletDao.archiveWallet(walletId, accountId, isArchived, isArchived ? System.currentTimeMillis() : 0));
    }

    public void deleteWallet(int walletId, int accountId) {
        database.runInTransaction(() -> {
            deleteWalletTransactionsInternal(walletId, accountId);
            walletDao.deleteWallet(walletId, accountId);
        });
    }

    public void deleteWalletTransactions(int walletId, int accountId) {
        database.runInTransaction(() -> deleteWalletTransactionsInternal(walletId, accountId));
    }

    private void deleteWalletTransactionsInternal(int walletId, int accountId) {

        long updatedAt = System.currentTimeMillis();

        List<TransactionEntity> transactions = transactionDao.getTransactionsForWallet(walletId, accountId);

        Set<Integer> affectedWalletIds = getWalletIds(walletId, transactions);

        // Delete all transactions
        for (TransactionEntity transaction : transactions) {
            transactionDao.deleteTransaction(transaction.tempTransactionServerId, updatedAt);
        }

        // Recalculate affected wallets
        for (Integer affectedWalletId : affectedWalletIds) {
            recalculateWalletBalance(affectedWalletId, accountId);
        }

        // Recalculate account
        recalculateAccountBalance(accountId);
    }

    @NonNull
    private static Set<Integer> getWalletIds(int walletId, List<TransactionEntity> transactions) {
        Set<Integer> affectedWalletIds = new HashSet<>();

        affectedWalletIds.add(walletId);

        // Find other wallets involved in transfers
        for (TransactionEntity transaction : transactions) {

            if (transaction.type == TransactionEntity.TYPE_TRANSFER) {

                // Destination wallet
                if (transaction.walletId != walletId) {
                    affectedWalletIds.add(transaction.walletId);
                }

                // Source wallet
                if (transaction.fromWalletId != null && transaction.fromWalletId != walletId) {
                    affectedWalletIds.add(transaction.fromWalletId);
                }
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
}