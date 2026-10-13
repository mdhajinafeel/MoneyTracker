package com.nprotech.moneytracker.worker;

import android.Manifest;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.notifications.SmartReminderManager;
import com.nprotech.moneytracker.ui.activities.MainActivity;
import com.nprotech.moneytracker.ui.activities.SplashActivity;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class SmartReminderWorker extends Worker {

    private static final int NOTIFICATION_ID = 9999;

    @AssistedInject
    public SmartReminderWorker(@Assisted @NonNull Context context, @Assisted @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {

            Context context = getApplicationContext();

            // =============================
            // BATTERY CHECK
            // =============================
            if (isPowerSaveModeEnabled()) {
                return Result.success();
            }

            int reminderValue = PreferenceManager.INSTANCE.getSmartReminder();
            if (reminderValue <= 1 || reminderValue > 25) {
                new SmartReminderManager(context).cancelWorker();
                return Result.success();
            }

            showNotification(context);

            // Schedule next day's reminder
            new SmartReminderManager(context).scheduleNextReminderWithWorker(reminderValue);
            return Result.success();
        } catch (Exception e) {
            AppLogger.e(getClass(), "smartReminder", e);
            return Result.failure();
        }
    }

    private boolean isPowerSaveModeEnabled() {
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        return pm != null && pm.isPowerSaveMode();
    }

    private void showNotification(Context context) {

        // Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        SmartReminderManager reminderManager = new SmartReminderManager(context);
        reminderManager.createNotificationChannel();

        // =====================================
        // INTENT
        // =====================================
        Intent intent;
        if (isAppInForeground()) {
            // APP OPEN
            intent = new Intent(getApplicationContext(), MainActivity.class);
        } else {
            // APP CLOSED/KILLED
            intent = new Intent(getApplicationContext(), SplashActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("IsSmartReminderClicked", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT |  PendingIntent.FLAG_IMMUTABLE);

        // =============================
        // NOTIFICATION
        // =============================
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), SmartReminderManager.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_app_logo_small)
                .setContentTitle(context.getString(R.string.smart_reminder_notification_title))
                .setContentText(context.getString(R.string.smart_reminder_notification_text))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.smart_reminder_notification_big_text)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSilent(false)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private boolean isAppInForeground() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                || appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
    }
}