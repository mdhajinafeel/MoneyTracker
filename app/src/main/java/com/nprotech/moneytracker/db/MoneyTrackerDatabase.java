package com.nprotech.moneytracker.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.nprotech.moneytracker.BuildConfig;
import com.nprotech.moneytracker.db.dao.CurrencyDao;
import com.nprotech.moneytracker.db.dao.AccountDao;
import com.nprotech.moneytracker.db.dao.WalletDao;
import com.nprotech.moneytracker.db.entites.CurrencyEntity;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;

@Database(entities = {CurrencyEntity.class, AccountEntity.class, WalletEntity.class},
        version = 1)
public abstract class MoneyTrackerDatabase extends RoomDatabase {

    private static volatile MoneyTrackerDatabase INSTANCE;

    public abstract CurrencyDao currencyDao();

    public abstract AccountDao accountDao();

    public abstract WalletDao walletDao();

    public static MoneyTrackerDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (MoneyTrackerDatabase.class) {
                if (INSTANCE == null) {
                    // ✅ Build Room database
                    Builder<MoneyTrackerDatabase> builder = Room.databaseBuilder(context.getApplicationContext(), MoneyTrackerDatabase.class,
                                    "money_tracker")
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
}