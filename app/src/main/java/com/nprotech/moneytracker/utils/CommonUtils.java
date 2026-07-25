package com.nprotech.moneytracker.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.nprotech.moneytracker.R;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class CommonUtils {

    public static String getPlainAmount(BigDecimal digit) {
        return digit.remainder(new BigDecimal(1)).compareTo(new BigDecimal(0)) == 0 ? String.valueOf(digit.longValue()) : String.format(Locale.ENGLISH, "%.2f", digit);
    }

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

    public static long getLongFromString(String s) {
        return new BigDecimal(s).multiply(new BigDecimal(100)).longValue();
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
}