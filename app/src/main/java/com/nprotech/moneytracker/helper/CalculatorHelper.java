package com.nprotech.moneytracker.helper;

import android.content.Context;

import com.udojava.evalex.Expression;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalculatorHelper {

    public static String getDisplayAmount(String s) {
        String replaceAll = s.replaceAll(",", ".");
        BigDecimal bigDecimal = new BigDecimal(replaceAll);
        if (replaceAll.contains(".")) {
            if (replaceAll.indexOf(".") != replaceAll.length() - 1) {
                return replaceAll.indexOf(".") == replaceAll.length() + (-2) ? String.format(Locale.getDefault(), "%,.1f", bigDecimal) : String.format(Locale.getDefault(), "%,.2f", bigDecimal);
            }
            String format = String.format(Locale.getDefault(), "%,.1f", bigDecimal);
            return format.substring(0, format.length() - 1);
        }
        String format2 = String.format(Locale.getDefault(), "%,.1f", bigDecimal);
        return format2.substring(0, format2.length() - 2);
    }

    public static String getPlainAmount(BigDecimal digit) {
        return digit.remainder(new BigDecimal(1)).compareTo(new BigDecimal(0)) == 0 ? String.valueOf(digit.longValue()) : String.format(Locale.ENGLISH, "%.2f", digit);
    }

    public static String getFormattedNumber(String s) {
        String[] split = s.split("[+\\-*/]", -1);
        List<Object> arrayList = new ArrayList<>();
        int i = 0;
        while (i < s.length()) {
            int i2 = i + 1;
            String substring = s.substring(i, i2);
            if (isSymbol(substring)) {
                arrayList.add(substring);
            }
            i = i2;
        }
        StringBuilder sb = new StringBuilder();
        int i3 = 0;
        for (String str : split) {
            if (!str.isEmpty()) {
                sb.append(getDisplayAmount(str));
                sb.append(arrayList.size() >= i3 + 1 ? (String) arrayList.get(i3) : "");
            } else {
                sb.append(arrayList.size() >= i3 + 1 ? (String) arrayList.get(i3) : "");
            }
            i3++;
        }
        return sb.toString();
    }

    public static String validateDivide(String amount) {
        if (isSymbol(amount.substring(amount.length() - 1))) {
            amount = amount.substring(0, amount.length() - 1);
        }
        return amount + "/";
    }

    public static String validateMultiply(String amount) {
        if (isSymbol(amount.substring(amount.length() - 1))) {
            amount = amount.substring(0, amount.length() - 1);
        }
        return amount + "*";
    }

    public static String validatePlus(String amount) {
        if (isSymbol(amount.substring(amount.length() - 1))) {
            amount = amount.substring(0, amount.length() - 1);
        }
        return amount + "+";
    }

    public static String validateMinus(String amount) {
        if (isSymbol(amount.substring(amount.length() - 1))) {
            amount = amount.substring(0, amount.length() - 1);
        }
        return amount + "-";
    }

    public static String validateEscape(String amount) {
        String escapeCharacter = escapeCharacter(amount);
        return escapeCharacter.isEmpty() ? "0" : escapeCharacter;
    }

    public static String validateEqual(Context context, String amount) {
        Expression expression = new Expression(cleanUp(amount));
        expression.setPrecision(128);
        expression.setRoundingMode(RoundingMode.DOWN);
        try {
            return expression.eval().toPlainString();
        } catch (Exception e) {
            AppLogger.e(context.getClass(), "validateEqual", e);
            return "0";
        }
    }

    public static String validateDot(String amount) {
        int length = amount.length();
        for (int i = 0; i < length; i++) {
            String substring = amount.substring((length - i) - 1, amount.length() - i);
            if (isSymbol(substring)) {
                if (i == 0) {
                    return amount + "0.";
                }
                return amount + ".";
            } else if (isDot(substring)) {
                return amount;
            } else {
                if (i == length - 1) {
                    amount = amount + ".";
                }
            }
        }
        return amount;
    }

    public static String validateDigit(String digit, String amount, boolean isRate) {
        if (amount.equals("0")) {
            return digit;
        }
        int length = amount.length();
        int i = 0;
        int i2 = 0;
        while (true) {
            if (i >= length) {
                i = i2;
                break;
            } else if (isSymbol(amount.substring((length - i) - 1, amount.length() - i))) {
                break;
            } else {
                if (i == length - 1) {
                    i2 = i;
                }
                i++;
            }
        }
        String substring = amount.substring(length - i, length);
        if (!substring.contains(".")) {
            return amount + digit;
        }
        if (isRate) {
            if (amount.indexOf(".") > amount.length() - 6) {
                return amount + digit;
            }
        } else if (substring.indexOf(".") > substring.length() - 3) {
            if (digit.equals("00")) {
                if (substring.indexOf(".") > substring.length() - 2) {
                    return amount + digit;
                }
                return amount + "0";
            }
            return amount + digit;
        }
        return amount;
    }

    private static String cleanUp(String amount) {
        return isSymbol(amount.substring(amount.length() + (-1))) ? escapeCharacter(amount) : amount;
    }

    private static boolean isSymbol(String s) {
        return s.equals("/") || s.equals("*") || s.equals("+") || s.equals("-");
    }

    private static boolean isDot(String s) {
        return s.equals(".") || s.equals(",");
    }

    private static String escapeCharacter(String s) {
        return s.substring(0, s.length() - 1);
    }
}