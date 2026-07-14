package com.nprotech.moneytracker.repositories;

import androidx.lifecycle.LiveData;

import com.nprotech.moneytracker.db.dao.WalletDao;
import com.nprotech.moneytracker.db.entites.WalletEntity;

import java.util.List;

public class WalletRepository {

    private final WalletDao walletDao;

    public WalletRepository(WalletDao walletDao) {
        this.walletDao = walletDao;
    }

    public LiveData<List<WalletEntity>> getAllWallets(int accountId) {
        return walletDao.getAllWallets(accountId);
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
}