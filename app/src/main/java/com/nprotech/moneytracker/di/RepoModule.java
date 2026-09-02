package com.nprotech.moneytracker.di;

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
import com.nprotech.moneytracker.repositories.BackupHistoryRepository;
import com.nprotech.moneytracker.repositories.CategoryRepository;
import com.nprotech.moneytracker.repositories.CommonDataRepository;
import com.nprotech.moneytracker.repositories.GoalRepository;
import com.nprotech.moneytracker.repositories.MasterRepository;
import com.nprotech.moneytracker.repositories.AccountRepository;
import com.nprotech.moneytracker.repositories.TransactionRepository;
import com.nprotech.moneytracker.repositories.WalletRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module(includes = {ApiModule.class, DBModule.class})
@InstallIn(SingletonComponent.class)
public class RepoModule {

    @Provides
    @Singleton
    MasterRepository provideMasterRepository(CurrencyDao currencyDao) {
        return new MasterRepository(currencyDao);
    }

    @Provides
    @Singleton
    CommonDataRepository provideCommonDataRepository(CommonDataDao commonDataDao) {
        return new CommonDataRepository(commonDataDao);
    }

    @Provides
    @Singleton
    AccountRepository provideAccountRepository(AccountDao accountDao, AccountCurrencyMappingDao accountCurrencyMappingDao) {
        return new AccountRepository(accountDao, accountCurrencyMappingDao);
    }

    @Provides
    @Singleton
    WalletRepository provideWalletRepository(WalletDao walletDao) {
        return new WalletRepository(walletDao);
    }

    @Provides
    @Singleton
    CategoryRepository provideCategoryRepository(CategoryDao categoryDao) {
        return new CategoryRepository(categoryDao);
    }

    @Provides
    @Singleton
    TransactionRepository provideTransactionRepository(MoneyTrackerDatabase database, AccountDao accountDao, WalletDao walletDao,TransactionDao transactionDao,
                                                       TransactionAttachmentDao transactionAttachmentDao, CategoryDao categoryDao) {
        return new TransactionRepository(database, accountDao, walletDao, transactionDao, transactionAttachmentDao, categoryDao);
    }

    @Provides
    @Singleton
    GoalRepository provideGoalRepository(GoalDao goalDao) {
        return new GoalRepository(goalDao);
    }

    @Provides
    @Singleton
    BackupHistoryRepository provideBackupHistoryRepository(BackupHistoryDao backupHistoryDao) {
        return new BackupHistoryRepository(backupHistoryDao);
    }
}