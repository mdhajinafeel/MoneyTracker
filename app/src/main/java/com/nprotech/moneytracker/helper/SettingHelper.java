package com.nprotech.moneytracker.helper;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.nprotech.moneytracker.R;

public class SettingHelper {

    // -----------------------
    // RATE APP
    // -----------------------
    public static void rateApp(@NonNull Activity activity) {

        // TODO Change to true after Expenixo is published on Google Play
        boolean isPublishedOnPlayStore = false;
        if (!isPublishedOnPlayStore) {
            Toast.makeText(activity, activity.getString(R.string.app_google_play_yet, activity.getString(R.string.app_name)), Toast.LENGTH_SHORT).show();
            return;
        }

        ReviewManager manager = ReviewManagerFactory.create(activity);
        manager.requestReviewFlow().addOnCompleteListener(task -> {
            if (activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
            if (task.isSuccessful()) {
                ReviewInfo reviewInfo = task.getResult();
                manager.launchReviewFlow(activity, reviewInfo).addOnCompleteListener(launchTask ->
                                AppLogger.d(SettingHelper.class, "Review flow completed"));
            } else {
                AppLogger.e(SettingHelper.class, "rateApp", task.getException());
                openPlayStore(activity);
            }
        });
    }

    // -----------------------
    // OPEN PLAY STORE
    // -----------------------
    private static void openPlayStore(@NonNull Activity activity) {
        // TODO Change to true after Expenixo is published on Google Play
        boolean isPublishedOnPlayStore = false;
        if (!isPublishedOnPlayStore) {
            Toast.makeText(activity, activity.getString(R.string.app_google_play_yet, activity.getString(R.string.app_name)), Toast.LENGTH_SHORT).show();
            return;
        }
        String packageName = activity.getPackageName();
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            activity.startActivity(intent);
        }
    }

    //-------------------------
    // SHARE APP
    //-------------------------
    public static void shareApp(Activity activity) {
        String appName = activity.getString(R.string.app_name);
        String shareMessage = "I've been using " + appName + " to manage my finances. "
                + "Check it out on Google Play:\n\n"
                + "https://play.google.com/store/apps/details?id=" + activity.getPackageName();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, appName);
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        activity.startActivity(
                Intent.createChooser(intent, "Share " + appName)
        );
    }

    //-------------------------
    // LEGAL PAGE
    //-------------------------
    public static void openLegalPage(Context context, String url) {
        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().setShowTitle(true).build();
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }
}