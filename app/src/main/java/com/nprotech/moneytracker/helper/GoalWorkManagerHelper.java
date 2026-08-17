package com.nprotech.moneytracker.helper;

import android.content.Context;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.nprotech.moneytracker.worker.GoalAutoSaveWorker;

import java.util.concurrent.TimeUnit;

public class GoalWorkManagerHelper {

    private static final String WORK_NAME = "goal_auto_save";

    private GoalWorkManagerHelper() {
        // Utility class
    }

    public static void scheduleAutoSave(Context context, long delay) {

        if (delay < 0) {
            delay = 0;
        }

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(GoalAutoSaveWorker.class).setInitialDelay(delay, TimeUnit.MILLISECONDS).build();
        WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request);
    }
}