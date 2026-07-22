package com.nprotech.moneytracker;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.nprotech.moneytracker.crashlytics.CrashlyticsExceptionHandler;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.initializer.CategoryInitializer;
import com.nprotech.moneytracker.initializer.CommonInitializer;
import com.nprotech.moneytracker.initializer.CurrencyInitializer;

import java.security.KeyStore;
import java.util.List;

import dagger.hilt.android.HiltAndroidApp;
import devliving.online.securedpreferencestore.DefaultRecoveryHandler;
import devliving.online.securedpreferencestore.SecuredPreferenceStore;

@HiltAndroidApp
public class MoneyTrackerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize preferences
        initSecureSharedPref();

        // Apply theme
        boolean darkMode = PreferenceManager.INSTANCE.getDarkMode();
        AppCompatDelegate.setDefaultNightMode(darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        // Currency load
        CurrencyInitializer.loadCurrencies(this);

        // Category load
        CategoryInitializer.loadCategories(this);

        // Common Data load
        CommonInitializer.loadCommonData(this);

        // Preference
        if (PreferenceManager.INSTANCE.getWeekStartOn() == 0) {
            PreferenceManager.INSTANCE.setWeekStartOn(1);
        }

        if (PreferenceManager.INSTANCE.getLanguage() == 0) {
            PreferenceManager.INSTANCE.setLanguage(1);
        }

        if (PreferenceManager.INSTANCE.getStartUpScreen() == 0) {
            PreferenceManager.INSTANCE.setStartUpScreen(1);
        }

        if (PreferenceManager.INSTANCE.getSmartReminder() == 0) {
            PreferenceManager.INSTANCE.setSmartReminder(1);
        }

        if (PreferenceManager.INSTANCE.getStatisticsFilter() == 0) {
            PreferenceManager.INSTANCE.setStatisticsFilter(3);
        }

        // Firebase crashlytics
        firebaseCrashlytics();
    }

    private void initSecureSharedPref() {
        try {
            String prefName = "NPROTECH_PREF";
            String prefix = "nprotech_money_tracker";
            byte[] seed = "nprotech_money_tracker".getBytes();
            SecuredPreferenceStore.init(getApplicationContext(), prefName, prefix, seed, new DefaultRecoveryHandler());
            SecuredPreferenceStore.setRecoveryHandler(new DefaultRecoveryHandler() {
                @Override
                protected boolean recover(Exception e, KeyStore keyStore, List<String> keyAliases, SharedPreferences preferences) {
                    return super.recover(e, keyStore, keyAliases, preferences);
                }
            });

        } catch (Exception e) {
            AppLogger.e(getClass(), "initSecureSharedPref", e);
        }
    }

    private void firebaseCrashlytics() {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        // Global crash handler
        Thread.setDefaultUncaughtExceptionHandler(new CrashlyticsExceptionHandler());
    }
}