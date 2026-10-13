package com.nprotech.moneytracker.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.worker.SmartReminderWorker;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class SmartReminderManager {

    public static final String CHANNEL_ID = "smart_reminder_channel";
    private static final String SMART_REMINDER_WORK = "smart_reminder_work";
    private final Context context;

    public SmartReminderManager(Context context) {
        this.context = context.getApplicationContext();
    }

    // =========================================================
    // CREATE NOTIFICATION CHANNEL
    // =========================================================
    public void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Smart Reminder", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(context.getString(R.string.smart_reminder));
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // =========================================================
    // SCHEDULE NEXT REMINDER
    // =========================================================
    public void scheduleNextReminderWithWorker(int reminderValue) {

        // 1 = Not set
        if (reminderValue <= 1 || reminderValue > 25) {
            cancelWorker();
            return;
        }

        int hour = reminderValue - 2;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If today's time has already passed,
        // schedule for tomorrow.
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        long delay = calendar.getTimeInMillis() - System.currentTimeMillis();

        Log.d("SmartReminder", "Worker scheduled for = " + calendar.getTime() + ", delay = " + delay);

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SmartReminderWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .build();
        WorkManager.getInstance(context).enqueueUniqueWork(SMART_REMINDER_WORK, ExistingWorkPolicy.REPLACE, request);
    }

    // =========================================================
    // CANCEL WORKER
    // =========================================================
    public void cancelWorker() {
        WorkManager.getInstance(context).cancelUniqueWork(SMART_REMINDER_WORK);
    }
}