package com.nprotech.moneytracker.helper;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.nprotech.moneytracker.worker.GoalAutoSaveWorker;

import java.util.concurrent.TimeUnit;

public class GoalWorkManagerHelper {

    private static final String WORK_NAME = "goal_auto_save";

    private GoalWorkManagerHelper() {
        // Utility class
    }

    public static void scheduleAutoSave(Context context) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(GoalAutoSaveWorker.class, 1, TimeUnit.DAYS).build();
        WorkManager.getInstance(context.getApplicationContext()).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);
    }
}