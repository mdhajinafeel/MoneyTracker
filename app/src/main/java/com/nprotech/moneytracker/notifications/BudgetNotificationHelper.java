package com.nprotech.moneytracker.notifications;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.nprotech.moneytracker.R;

public class BudgetNotificationHelper {

    private static final String CHANNEL_ID = "budget_alerts";

    private BudgetNotificationHelper() {
    }

    public static boolean showBudgetAlert(Context context, int budgetId, String budgetName, double spentAmount, double budgetAmount, int alertPercentage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        createNotificationChannel(context);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) {
            return false;
        }

        String title;
        String message;

        if (spentAmount > budgetAmount) {
            title = context.getString(R.string.overspent_budget);
            message = context.getString(R.string.budget_overspent_message, spentAmount, spentAmount - budgetAmount, budgetAmount, budgetName);
        } else if (spentAmount == budgetAmount) {
            title = context.getString(R.string.budget_limit_reached);
            message = context.getString(R.string.budget_limit_reached_message, spentAmount, budgetAmount, budgetName);
        } else {
            title = context.getString(R.string.text_budget_alert);
            message = context.getString(R.string.budget_alert_message, alertPercentage, budgetAmount, budgetName);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_app_logo_small)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        manager.notify(budgetId, builder.build());
        return true;
    }

    private static void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Budget Alerts", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Notifications when a budget reaches its alert percentage.");
        manager.createNotificationChannel(channel);
    }
}