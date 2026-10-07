package com.nprotech.moneytracker.worker;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class RecurringTransactionScheduler {

    private static final String WORK_NAME = "recurring_transaction_worker";

    public static void scheduleRecurringTransactions(Context context) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(RecurringTransactionWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(context.getApplicationContext()).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);
    }
}