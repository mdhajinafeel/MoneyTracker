package com.nprotech.moneytracker.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SmartReminderBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            SmartReminderManager manager = new SmartReminderManager(context);
            manager.rescheduleSavedReminder();
        }
    }
}