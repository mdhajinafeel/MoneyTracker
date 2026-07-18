package com.nprotech.moneytracker.helper;

import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class AppLogger {

    private static final String TAG = "MoneyTracker";

    // ---------------------------------------------------
    // Debug Log
    // ---------------------------------------------------
    public static void d(Class<?> cls, String message) {
        String logMessage = cls.getSimpleName() + " : " + message;
        Log.d(TAG, logMessage);
        FirebaseCrashlytics.getInstance().log(logMessage);
    }

    // ---------------------------------------------------
    // Error Log
    // ---------------------------------------------------
    public static void e(Class<?> cls, String message, Throwable throwable) {
        String logMessage = cls.getSimpleName() + " : " + message;
        Log.e(TAG, logMessage, throwable);
        FirebaseCrashlytics.getInstance().log(logMessage);
        FirebaseCrashlytics.getInstance().recordException(throwable);
    }

    public static void w(Class<?> cls, String message) {
        String logMessage = cls.getSimpleName() + " : " + message;
        Log.e(TAG, logMessage);
        FirebaseCrashlytics.getInstance().log(logMessage);
    }
}