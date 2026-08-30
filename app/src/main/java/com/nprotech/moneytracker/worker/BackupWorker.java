package com.nprotech.moneytracker.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.utils.BackupManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class BackupWorker extends Worker {

    private static final String KEY_FREQUENCY = "backup_frequency";
    public static final String UNIQUE_BACKUP_WORK = "automatic_backup";

    public BackupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // -----------------------------------------
            // Check whether automatic backup is enabled
            // -----------------------------------------
            if (!PreferenceManager.INSTANCE.isAutoBackupEnabled()) {
                return Result.success();
            }

            // -----------------------------------------
            // Get frequency
            // -----------------------------------------
            int frequency = getInputData().getInt(KEY_FREQUENCY, PreferenceManager.INSTANCE.getBackupFrequency());

            // -----------------------------------------
            // Create backup
            // -----------------------------------------
            boolean success = createBackup();

            if (!success) {
                return Result.retry();
            }

            // -----------------------------------------
            // Schedule next backup
            // -----------------------------------------
            schedule(getApplicationContext(), frequency);
            return Result.success();
        } catch (Exception e) {
            AppLogger.e(getClass(), "doWork", e);
            return Result.retry();
        }
    }

    /**
     * Schedule the next automatic backup.
     */
    public static void schedule(Context context, int frequency) {
        try {

            Calendar calendar = Calendar.getInstance();
            switch (frequency) {
                case Constants.BACKUP_FREQUENCY_WEEKLY:
                    // Next Sunday at 12:00 AM
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    break;
                case Constants.BACKUP_FREQUENCY_MONTHLY:
                    // First day of next month at 12:00 AM
                    calendar.add(Calendar.MONTH, 1);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    break;
                case Constants.BACKUP_FREQUENCY_DAILY:
                default:
                    // Tomorrow at 12:00 AM
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    break;
            }

            long delay = Math.max(0, calendar.getTimeInMillis() - System.currentTimeMillis());
            Data data = new Data.Builder().putInt(KEY_FREQUENCY, frequency).build();
            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(BackupWorker.class)
                            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                            .setInputData(data)
                            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                            .build();

            WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork(UNIQUE_BACKUP_WORK, ExistingWorkPolicy.REPLACE, request);

        } catch (Exception e) {
            AppLogger.e(BackupWorker.class, "schedule", e);
        }
    }

    /**
     * Cancel automatic backup.
     */
    public static void cancel(Context context) {
        WorkManager.getInstance(context.getApplicationContext()).cancelUniqueWork(UNIQUE_BACKUP_WORK);
    }

    /**
     * Create backup using the existing BackupManager.
     */
    private boolean createBackup() {

        try {
            BackupManager backupManager = new BackupManager(getApplicationContext());
            BackupManager.BackupResult result = backupManager.createBackup(true, null);
            return result != null && result.uri() != null;
        } catch (Exception e) {
            AppLogger.e(getClass(), "createBackup", e);
            return false;
        }
    }
}