package com.nprotech.moneytracker.helper;

import android.content.Context;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.nprotech.moneytracker.worker.DebtLoanReminderWorker;

import java.util.concurrent.TimeUnit;

public class DebtLoanWorkManagerHelper {

    private static final String WORK_NAME = "debt_loan_reminder";

    private DebtLoanWorkManagerHelper() {
        // Utility class
    }

    public static void scheduleReminder(Context context, long delay) {

        if (delay < 0) {
            delay = 0;
        }

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DebtLoanReminderWorker.class).setInitialDelay(delay, TimeUnit.MILLISECONDS).build();
        WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request);
    }

    public static void scheduleNextReminder(Context context, Long reminderDate) {
        if (reminderDate == null) {
            return;
        }
        long delay = reminderDate - System.currentTimeMillis();
        scheduleReminder(context, Math.max(delay, 0));
    }
}