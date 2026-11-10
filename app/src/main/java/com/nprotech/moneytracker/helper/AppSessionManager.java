package com.nprotech.moneytracker.helper;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.atomic.AtomicBoolean;

public final class AppSessionManager {

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final AtomicBoolean SESSION_REVOKED = new AtomicBoolean(false);
    private static Runnable revocationAction;

    private AppSessionManager() {
    }

    public static void setRevocationAction(Runnable action) {
        synchronized (AppSessionManager.class) {
            revocationAction = action;
        }
    }

    public static void notifySessionRevoked() {

        if (!SESSION_REVOKED.compareAndSet(false, true)) {
            return;
        }

        MAIN_HANDLER.post(() -> {

            Runnable action;

            synchronized (AppSessionManager.class) {
                action = revocationAction;
            }

            if (action != null) {
                action.run();
            }
        });
    }

    public static void reset() {
        SESSION_REVOKED.set(false);
    }
}