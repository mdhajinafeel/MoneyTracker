package com.nprotech.moneytracker.crashlytics;

import android.os.Build;

import androidx.annotation.NonNull;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class CrashlyticsExceptionHandler implements Thread.UncaughtExceptionHandler{

    private final Thread.UncaughtExceptionHandler defaultHandler;

    public CrashlyticsExceptionHandler() {
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, @NonNull Throwable throwable) {

        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

        // Log Exception
        crashlytics.recordException(throwable);

        // Custom Keys
        crashlytics.setCustomKey("thread_name", thread.getName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            crashlytics.setCustomKey("thread_id", thread.threadId());
        }

        // Optional log
        crashlytics.log("Uncaught exception captured");

        // Pass to default handler
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, throwable);
        }
    }
}