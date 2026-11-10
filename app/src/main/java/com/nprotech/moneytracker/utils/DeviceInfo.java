package com.nprotech.moneytracker.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public final class DeviceInfo {

    private DeviceInfo() {
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getDeviceName() {
        return Build.MANUFACTURER + " " + Build.MODEL;
    }

    public static String getPlatform() {
        return "android";
    }

    public static String getPlatformVersion() {
        return "Android " + Build.VERSION.RELEASE;
    }
}