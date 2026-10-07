package com.nprotech.moneytracker.notifications;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.nprotech.moneytracker.MoneyTrackerApp;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.ui.activities.MainActivity;
import com.nprotech.moneytracker.ui.activities.SplashActivity;

public class SmartReminderReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 5001;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("SmartReminder", ">>> SmartReminderReceiver RECEIVED <<<");

        SmartReminderManager manager = new SmartReminderManager(context);

        int reminderValue = manager.getSavedReminder();
        Log.d("SmartReminder",
                "Saved reminder value = " + reminderValue);

        if (!manager.isReminderEnabled()) {
            Log.d("SmartReminder",
                    "Reminder is disabled. Receiver exiting.");
            return;
        }

        Log.d("SmartReminder", "Posting notification...");

        showNotification(context);

        Log.d("SmartReminder", "Notification posted");
        manager.scheduleReminder(reminderValue);

        Log.d("SmartReminder",
                "Next day's reminder scheduled.");
    }

    private void showNotification(Context context) {

        Log.d("SmartReminder", "Creating notification...");

        Intent openIntent;
        if (MoneyTrackerApp.isAppProcessAlive()) {
            // Existing process
            openIntent = new Intent(context, MainActivity.class);
        } else {
            // Fresh process after app was killed
            openIntent = new Intent(context, SplashActivity.class);
        }

        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 5002, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // =====================================================
        // Notification
        // =====================================================
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, SmartReminderManager.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_app_logo_small)
                .setContentTitle(context.getString(R.string.smart_reminder_notification_title))
                .setContentText(context.getString(R.string.smart_reminder_notification_text))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.smart_reminder_notification_big_text)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // =====================================================
        // Android 13+
        // =====================================================
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        Log.d("SmartReminder", ">>> NOTIFICATION SENT <<<");
    }
}