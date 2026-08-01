package com.nprotech.moneytracker.helper;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

public class SettingHelper {

    //-----------------------
    // RATE APP
    //-----------------------
    public static void rateApp(@NonNull Activity activity) {

        // TODO: Remove this before publishing to Play Store
        openPlayStore(activity);

//        ReviewManager manager = ReviewManagerFactory.create(activity);
//
//        Task<ReviewInfo> request = manager.requestReviewFlow();
//
//        request.addOnCompleteListener(task -> {
//
//            if (task.isSuccessful()) {
//                ReviewInfo reviewInfo = task.getResult();
//                manager.launchReviewFlow(activity, reviewInfo)
//                        .addOnCompleteListener(launchTask -> {
//                            // Google may or may not show the dialog.
//                            // Flow has finished.
//                            AppLogger.d(SettingHelper.class, "Review flow completed");
//                        });
//            } else {
//
//                Exception exception = task.getException();
//
//                if (exception instanceof ReviewException reviewException) {
//                    AppLogger.e(SettingHelper.class, "rateApp", reviewException);
//                } else {
//                    AppLogger.e(SettingHelper.class, "rateApp", exception);
//                }
//
//                openPlayStore(activity);
//            }
//        });
    }

    //-----------------------
    // OPEN PLAY STORE
    //-----------------------
    private static void openPlayStore(Activity activity) {
        // WhatsApp package name for testing
        String packageName = "com.whatsapp";
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        }
    }

    //-------------------------
    // SHARE APP
    //-------------------------
    public static void shareApp(Activity activity) {

        String appName = activity.getString(com.nprotech.moneytracker.R.string.app_name);

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
}