package com.nprotech.moneytracker.di;

import android.content.Context;

import com.nprotech.moneytracker.db.MoneyTrackerDatabase;
import com.nprotech.moneytracker.db.dao.CategoryDao;
import com.nprotech.moneytracker.db.dao.CurrencyDao;
import com.nprotech.moneytracker.db.dao.AccountDao;
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
    public AccountDao provideAccountDao(MoneyTrackerDatabase db) {
        return db.accountDao();
    }

    @Provides
    @Singleton
    public WalletDao provideWalletDao(MoneyTrackerDatabase db) {
        return db.walletDao();
    }
}