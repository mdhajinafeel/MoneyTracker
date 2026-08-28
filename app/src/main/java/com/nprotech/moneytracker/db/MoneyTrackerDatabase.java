package com.nprotech.moneytracker.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.nprotech.moneytracker.BuildConfig;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.db.dao.AccountCurrencyMappingDao;
import com.nprotech.moneytracker.db.dao.AccountDao;
import com.nprotech.moneytracker.db.dao.BackupHistoryDao;
import com.nprotech.moneytracker.db.dao.CategoryDao;
import com.nprotech.moneytracker.db.dao.CommonDataDao;
import com.nprotech.moneytracker.db.dao.CurrencyDao;
import com.nprotech.moneytracker.db.dao.GoalDao;
import com.nprotech.moneytracker.db.dao.TransactionAttachmentDao;
import com.nprotech.moneytracker.db.dao.TransactionDao;
import com.nprotech.moneytracker.db.dao.WalletDao;
import com.nprotech.moneytracker.db.entites.AccountCurrencyMappingEntity;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.BackupHistoryEntity;
import com.nprotech.moneytracker.db.entites.CategoryEntity;
import com.nprotech.moneytracker.db.entites.CommonDataEntity;
import com.nprotech.moneytracker.db.entites.CurrencyEntity;
import com.nprotech.moneytracker.db.entites.GoalContributionEntity;
import com.nprotech.moneytracker.db.entites.GoalEntity;
import com.nprotech.moneytracker.db.entites.TransactionAttachmentEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;

@Database(entities = {CurrencyEntity.class, CategoryEntity.class, CommonDataEntity.class, AccountCurrencyMappingEntity.class, AccountEntity.class, WalletEntity.class, TransactionEntity.class,
        TransactionAttachmentEntity.class, GoalEntity.class, GoalContributionEntity.class, BackupHistoryEntity.class},
        version = Constants.DATABASE_VERSION)
public abstract class MoneyTrackerDatabase extends RoomDatabase {

    private static volatile MoneyTrackerDatabase INSTANCE;

    public abstract CurrencyDao currencyDao();

    public abstract CategoryDao categoryDao();

    public abstract CommonDataDao commonDataDao();

    public abstract AccountCurrencyMappingDao accountCurrencyMappingDao();

    public abstract AccountDao accountDao();

    public abstract WalletDao walletDao();

    public abstract TransactionDao transactionDao();

    public abstract TransactionAttachmentDao transactionAttachmentDao();

    public abstract GoalDao goalDao();

    public abstract BackupHistoryDao backupHistoryDao();

    public static MoneyTrackerDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (MoneyTrackerDatabase.class) {
                if (INSTANCE == null) {
                    // ✅ Build Room database
                    Builder<MoneyTrackerDatabase> builder = Room.databaseBuilder(context.getApplicationContext(), MoneyTrackerDatabase.class,
                                    context.getString(R.string.app_name) + "_db".toLowerCase())
                            .fallbackToDestructiveMigration(true);

                    // ✅ Allow main thread queries in DEBUG only
                    if (BuildConfig.DEBUG) {
                        builder.allowMainThreadQueries();
                    }

                    INSTANCE = builder.build();
                }
            }
        }
        return INSTANCE;
    }

    public static void closeDatabase() {
        if (INSTANCE != null) {
            if (INSTANCE.isOpen()) {
                INSTANCE.close();
            }
            INSTANCE = null;
        }
    }
}