package com.nprotech.moneytracker.di;

import android.content.Context;

import com.nprotech.moneytracker.db.MoneyTrackerDatabase;
import com.nprotech.moneytracker.db.dao.AccountCurrencyMappingDao;
import com.nprotech.moneytracker.db.dao.BackupHistoryDao;
import com.nprotech.moneytracker.db.dao.CategoryDao;
import com.nprotech.moneytracker.db.dao.CommonDataDao;
import com.nprotech.moneytracker.db.dao.CurrencyDao;
import com.nprotech.moneytracker.db.dao.AccountDao;
import com.nprotech.moneytracker.db.dao.GoalDao;
import com.nprotech.moneytracker.db.dao.TransactionAttachmentDao;
import com.nprotech.moneytracker.db.dao.TransactionDao;
import com.nprotech.moneytracker.db.dao.WalletDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DBModule {

    @Provides
    @Singleton
    public MoneyTrackerDatabase provideDatabase(@ApplicationContext Context context) {
        return MoneyTrackerDatabase.getInstance(context);
    }

    @Provides
    @Singleton
    public CategoryDao provideCategoryDao(MoneyTrackerDatabase db) {
        return db.categoryDao();
    }

    @Provides
    @Singleton
    public CurrencyDao provideCurrencyDao(MoneyTrackerDatabase db) {
        return db.currencyDao();
    }

    @Provides
    @Singleton
    public CommonDataDao provideCommonDataDao(MoneyTrackerDatabase db) {
        return db.commonDataDao();
    }

    @Provides
    @Singleton
    public AccountDao provideAccountDao(MoneyTrackerDatabase db) {
        return db.accountDao();
    }

    @Provides
    @Singleton
    public AccountCurrencyMappingDao provideAccountCurrencyMappingDao(MoneyTrackerDatabase db) {
        return db.accountCurrencyMappingDao();
    }

    @Provides
    @Singleton
    public WalletDao provideWalletDao(MoneyTrackerDatabase db) {
        return db.walletDao();
    }

    @Provides
    @Singleton
    public TransactionDao provideTransactionDao(MoneyTrackerDatabase db) {
        return db.transactionDao();
    }

    @Provides
    @Singleton
    public TransactionAttachmentDao provideTransactionAttachmentDao(MoneyTrackerDatabase db) {
        return db.transactionAttachmentDao();
    }

    @Provides
    @Singleton
    public GoalDao provideGoalDao(MoneyTrackerDatabase db) {
        return db.goalDao();
    }

    @Provides
    @Singleton
    public BackupHistoryDao provideBackupHistoryDao(MoneyTrackerDatabase db) {
        return db.backupHistoryDao();
    }
}