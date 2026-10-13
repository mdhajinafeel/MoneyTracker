package com.nprotech.moneytracker.security;

import android.os.SystemClock;

import com.nprotech.moneytracker.constants.Constants;

public final class AppLockManager {

    private static boolean lockScreenShowing = false;
    private static boolean authenticated = false;
    private static long backgroundTime = 0L;

    private AppLockManager() {
    }

    public static synchronized boolean isAuthenticated() {
        return authenticated;
    }

    public static synchronized void setAuthenticated(boolean value) {
        authenticated = value;
    }

    public static synchronized void onAppBackgrounded() {
        backgroundTime = SystemClock.elapsedRealtime();
    }

    public static synchronized void resetBackgroundTime() {
        backgroundTime = 0L;
    }

    public static synchronized boolean shouldLock(int autoLockSeconds) {
        if (!authenticated) {
            return true;
        }

        if (backgroundTime == 0L) {
            return false;
        }

        if (autoLockSeconds == Constants.AUTO_LOCK_IMMEDIATELY) {
            return true;
        }

        if (autoLockSeconds == Constants.AUTO_LOCK_NEVER) {
            return false;
        }

        long elapsedSeconds =
                (SystemClock.elapsedRealtime() - backgroundTime) / 1000L;

        return elapsedSeconds >= autoLockSeconds;
    }

    public static synchronized boolean tryShowLockScreen() {
        if (lockScreenShowing) {
            return false;
        }

        lockScreenShowing = true;
        return true;
    }

    public static synchronized void onLockScreenClosed() {
        lockScreenShowing = false;
    }

    public static synchronized void requireAuthentication() {
        authenticated = false;
        backgroundTime = 0L;
    }
}