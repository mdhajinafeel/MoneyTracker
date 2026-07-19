package com.nprotech.moneytracker.utils;

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

        if (value >= 1000000)
            return String.format(Locale.getDefault(), "%.1fM", value / 1000000);

        if (value >= 1000)
            return String.format(Locale.getDefault(), "%.1fK", value / 1000);

        return String.format(Locale.getDefault(), "%.0f", value);
    }

    public static long getLongFromString(String s) {
        return new BigDecimal(s).multiply(new BigDecimal(100)).longValue();
    }
}