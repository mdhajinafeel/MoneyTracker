package com.nprotech.moneytracker.utils;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.AnimRes;

public final class ActivityUtils {

    private ActivityUtils() {
        throw new AssertionError("No instances.");
    }

    /**
     * Applies the activity open transition.
     */
    public static void overrideOpenTransition(
            Activity activity,
            @AnimRes int enterAnim,
            @AnimRes int exitAnim
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(
                    Activity.OVERRIDE_TRANSITION_OPEN,
                    enterAnim,
                    exitAnim
            );
        } else {
            overridePendingTransitionCompat(activity, enterAnim, exitAnim);
        }
    }

    /**
     * Applies the activity close transition.
     */
    public static void overrideCloseTransition(
            Activity activity,
            @AnimRes int enterAnim,
            @AnimRes int exitAnim
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(
                    Activity.OVERRIDE_TRANSITION_CLOSE,
                    enterAnim,
                    exitAnim
            );
        } else {
            overridePendingTransitionCompat(activity, enterAnim, exitAnim);
        }
    }

    /**
     * Compatibility method for Android 13 and below.
     */
    @SuppressWarnings("deprecation")
    private static void overridePendingTransitionCompat(
            Activity activity,
            @AnimRes int enterAnim,
            @AnimRes int exitAnim
    ) {
        activity.overridePendingTransition(enterAnim, exitAnim);
    }
}