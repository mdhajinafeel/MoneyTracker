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

    public LiveData<List<WalletEntity>> getAllWallets() {
        return walletDao.getAllWallets();
    }

    public long saveWallet(WalletEntity wallet) {
        return walletDao.insert(wallet);
    }

    public int getLastWalletOrder() {
        return walletDao.getLastWalletOrder();
    }

    public List<WalletEntity> getWalletsByAccountId(int accountId) {
        return walletDao.getWalletsByAccountId(accountId);
    }

    public WalletEntity getWalletByWalletId(int walletId) {
        return walletDao.getWalletByWalletId(walletId);
    }
}