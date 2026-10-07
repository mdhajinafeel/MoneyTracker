package com.nprotech.moneytracker.notifications;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.PreferenceManager;

import java.util.Calendar;

public class SmartReminderManager {

    public static final String CHANNEL_ID = "smart_reminder_channel";
    private static final int ALARM_REQUEST_CODE = 5001;
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
            channel.setDescription(context.getString(R.string.daily_reminder));
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(
                        channel
                );
            }
        }
    }

    // =========================================================
    // SCHEDULE REMINDER
    // =========================================================
    public void scheduleReminder(int reminderValue) {
        // 1 = Not set
        if (reminderValue <= 1 || reminderValue > 25) {
            Log.d("SmartReminder", "Invalid reminder value: " + reminderValue);
            cancelReminder();
            return;
        }

        int hour = reminderValue - 2;
        Log.d("SmartReminder",
                "Scheduling reminder. value=" + reminderValue +
                        ", hour=" + hour);

        createNotificationChannel();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e("SmartReminder", "AlarmManager is null");
            return;
        }

        Intent intent = new Intent(context, SmartReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        /*
         * If today's reminder time has already passed,
         * schedule it for tomorrow.
         */
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        long triggerAtMillis = calendar.getTimeInMillis();
        Log.d("SmartReminder",
                "Alarm scheduled for = " +
                        calendar.getTime());

        /*
         * Android 12+
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            boolean canScheduleExact = alarmManager.canScheduleExactAlarms();
            Log.d("SmartReminder",
                    "Can schedule exact alarm = " + canScheduleExact);
            if (canScheduleExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }

        Log.d("SmartReminder",
                "Alarm successfully registered.");
    }

    // =========================================================
    // CANCEL REMINDER
    // =========================================================
    public void cancelReminder() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, SmartReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    // =========================================================
    // RESCHEDULE SAVED REMINDER
    // =========================================================

    public void rescheduleSavedReminder() {
        int reminderValue = PreferenceManager.INSTANCE.getSmartReminder();

        // 1 = Not set
        if (reminderValue <= 1 || reminderValue > 25) {
            cancelReminder();
            return;
        }

        scheduleReminder(reminderValue);
    }

    // =========================================================
    // GET SAVED REMINDER
    // =========================================================
    public int getSavedReminder() {
        return PreferenceManager.INSTANCE.getSmartReminder();
    }

    // =========================================================
    // IS ENABLED
    // =========================================================
    public boolean isReminderEnabled() {
        int reminderValue = PreferenceManager.INSTANCE.getSmartReminder();
        return reminderValue > 1 && reminderValue <= 25;
    }
}