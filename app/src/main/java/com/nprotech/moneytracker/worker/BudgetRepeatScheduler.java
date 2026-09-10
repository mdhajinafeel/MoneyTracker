package com.nprotech.moneytracker.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class BudgetRepeatScheduler {

    private static final String WORK_NAME = "budget_repeat_worker";

    public static void scheduleBudgetRepeat(@NonNull Context context) {

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(BudgetRepeatWorker.class, 1, TimeUnit.DAYS).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);
    }
}