package com.nprotech.moneytracker.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.widget.TextViewCompat;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CommonUtils {

    public static String getBeautifyAmount(String symbol, double amount) {
        boolean z = 0 > amount;
        StringBuilder sb = new StringBuilder();
        sb.append(z ? "-" : "");
        sb.append(symbol);
        sb.append(StringUtils.SPACE);
        if (z) {
            amount = -amount;
        }

        sb.append(getFormattedAmount(amount));
        return sb.toString();
    }

    public static String getBeautifyAmount(double amount) {
        boolean z = 0 > amount;
        StringBuilder sb = new StringBuilder();
        sb.append(z ? "-" : "");
        sb.append(StringUtils.SPACE);
        if (z) {
            amount = -amount;
        }

        sb.append(getFormattedAmount(amount));
        return sb.toString();
    }

    private static String getFormattedAmount(double amount) {
        if (amount == 0) {
            return "0";
        }

        BigDecimal value = BigDecimal.valueOf(amount).stripTrailingZeros();
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.getDefault());
        decimalFormat.setMaximumFractionDigits(2);
        decimalFormat.setMinimumFractionDigits(0);
        decimalFormat.setGroupingUsed(false);

        String format = decimalFormat.format(value);
        if (format.contains(".") || format.contains(",")) {
            return String.format(Locale.getDefault(), "%,.2f", value);
        }

        decimalFormat.setGroupingUsed(true);
        return decimalFormat.format(value);
    }

    public static String formatCompact(double value) {

        double abs = Math.abs(value);
        DecimalFormat df = new DecimalFormat("#.##");

        if (abs >= 1_000_000) {
            return df.format(value / 1_000_000) + "M";
        }

        if (abs >= 1_000) {
            return df.format(value / 1_000) + "K";
        }

        return df.format(value);
    }

    public static int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    public static double parseAmount(String amount) {
        if (amount == null || amount.trim().isEmpty()) {
            return 0;
        }

        return Double.parseDouble(amount.replace(",", "").trim());
    }

    public static float convertDpToPixel(Context context, float dp) {
        return dp * (context.getResources().getDisplayMetrics().densityDpi / 160.0f);
    }

    public static void applyFont(Context context, View view) {
        Typeface tf = ResourcesCompat.getFont(context, R.font.exo2_medium);

        if (view instanceof TextView) {
            ((TextView) view).setTypeface(tf);
        } else if (view instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                applyFont(context, group.getChildAt(i));
            }
        }
    }

    public static Drawable createIconBackground(Context context, String colorHex, int shape, int cornerRadius) {

        int color = Color.parseColor(colorHex);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(shape);
        drawable.setCornerRadius(dpToPx(context, cornerRadius));

        drawable.setColor(lightenColor(color, 0.35f));

        drawable.setStroke(
                CommonUtils.dpToPx(context, 1),
                Color.argb(50, 255, 255, 255));

        return drawable;
    }

    public static Drawable createGradient(Context context, String colorHex, int cornerRadius) {

        int color = Color.parseColor(colorHex);

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TR_BL,
                new int[]{
                        lightenColor(color, 0.18f),
                        color,
                        darkenColor(color)
                });

        gradient.setCornerRadius(dpToPx(context, cornerRadius));
        // Soft white border
        gradient.setStroke(dpToPx(context, 1), Color.argb(40, 255, 255, 255));
        return gradient;
    }

    private static int lightenColor(int color, float factor) {

        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        r += (int) ((255 - r) * factor);
        g += (int) ((255 - g) * factor);
        b += (int) ((255 - b) * factor);

        return Color.rgb(r, g, b);
    }

    private static int darkenColor(int color) {

        return Color.rgb(
                (int) (Color.red(color) * (1 - (float) 0.08)),
                (int) (Color.green(color) * (1 - (float) 0.08)),
                (int) (Color.blue(color) * (1 - (float) 0.08))
        );
    }

    public static Drawable createGoalProgressDrawable(Context context, int progressColor) {

        int backgroundColor = adjustAlpha(progressColor);

        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setColor(backgroundColor);
        background.setCornerRadius(dpToPx(context, 5));

        GradientDrawable progress = new GradientDrawable();
        progress.setShape(GradientDrawable.RECTANGLE);
        progress.setColor(progressColor);
        progress.setCornerRadius(dpToPx(context, 5));

        ClipDrawable clipDrawable = new ClipDrawable(progress, Gravity.START, ClipDrawable.HORIZONTAL);
        Drawable[] layers = {background, clipDrawable};

        return new LayerDrawable(layers) {{
            setId(0, android.R.id.background);
            setId(1, android.R.id.progress);
        }};
    }

    public static Drawable createGoalRingDrawable(Context context, int progressColor) {
        int strokeWidth = dpToPx(context, 6);
        int dimProgressColor = ColorUtils.setAlphaComponent(progressColor, 60);
        return new GoalRingDrawable(dimProgressColor, progressColor, strokeWidth);
    }

    private static int adjustAlpha(int color) {
        int alpha = Math.round(Color.alpha(color) * (float) 0.2);
        return ColorUtils.setAlphaComponent(color, alpha);
    }

    public static int calculateGoalProgress(double savedAmount, double targetAmount) {
        if (targetAmount <= 0) {
            return 0;
        }

        int progress = (int) Math.round((savedAmount / targetAmount) * 100);

        return Math.max(0, Math.min(progress, 100));
    }

    public static long calculateDaysLeft(long targetDate) {

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(targetDate);
        target.set(Calendar.HOUR_OF_DAY, 0);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        long difference = target.getTimeInMillis() - today.getTimeInMillis();

        return TimeUnit.MILLISECONDS.toDays(difference);
    }

    public static String getDisplayId(int goalId, String type) {
        return String.format(Locale.US, "#" + type + "%05d", goalId);
    }

    // =========================================================
    // DRAWABLE
    // =========================================================
    public static void setDrawable(Context context, AppCompatTextView textView, @DrawableRes int resId, @DimenRes int dimen, @ColorRes int colorId, int gravity) {
        try {
            Drawable drawable = AppCompatResources.getDrawable(context, resId);
            if (drawable != null) {
                int size = context.getResources().getDimensionPixelSize(dimen);
                drawable.setBounds(0, 0, size, size);
                if (gravity == Gravity.START) {
                    textView.setCompoundDrawablesRelative(drawable, null, null, null);
                } else if (gravity == Gravity.END) {
                    textView.setCompoundDrawablesRelative(null, null, drawable, null);
                }
                TextViewCompat.setCompoundDrawableTintList(textView, ColorStateList.valueOf(ContextCompat.getColor(context, colorId)));
            }
        } catch (Exception e) {
            AppLogger.e(context.getClass(), "setDrawable", e);
        }
    }

    public static void setDrawables(Context context, TextView textView, int startDrawable, int endDrawable, int sizeRes, int tintColor) {
        Drawable start = ContextCompat.getDrawable(context, startDrawable);
        Drawable end = ContextCompat.getDrawable(context, endDrawable);

        int size = context.getResources().getDimensionPixelSize(sizeRes);

        if (start != null) {
            start = start.mutate();
            start.setTint(ContextCompat.getColor(context, tintColor));
            start.setBounds(0, 0, size, size);
        }

        if (end != null) {
            end = end.mutate();
            end.setTint(ContextCompat.getColor(context, tintColor));
            end.setBounds(0, 0, size, size);
        }

        textView.setCompoundDrawables(start, null, end, null);
    }

    // =========================================================
    // FILE NAME & EXTENSION
    // =========================================================

    public static String getFileName(Uri uri, Context context) {
        String fileName = null;

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME},
                    null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        fileName = cursor.getString(index);
                    }
                }
            }
        }

        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }

        return fileName != null ? fileName : "attachment";
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }

        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex <= 0 || dotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    public static long getFileSize(Uri uri, Context context) {
        try {
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                String path = uri.getPath();
                if (path != null) {
                    File file = new File(path);
                    if (file.exists()) {
                        return file.length();
                    }
                }
            }

            if ("content".equalsIgnoreCase(uri.getScheme())) {
                try (Cursor cursor = context.getContentResolver().query(uri, new String[]{OpenableColumns.SIZE}, null, null, null)) {

                    if (cursor != null && cursor.moveToFirst()) {
                        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                        if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                            return cursor.getLong(sizeIndex);
                        }
                    }
                }
            }
        } catch (Exception e) {
            AppLogger.e(context.getClass(), "getFileSize", e);
        }
        return 0;
    }
}