package com.nprotech.moneytracker.helper;

import android.content.Context;

import com.nprotech.moneytracker.R;

import java.util.ArrayList;

public class DataHelper {

    public static ArrayList<String> getColorList() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("#34BFFF");
        arrayList.add("#0097E6");
        arrayList.add("#0077C5");
        arrayList.add("#055393");
        arrayList.add("#008481");
        arrayList.add("#00A6A4");
        arrayList.add("#00C1BF");
        arrayList.add("#29B473");
        arrayList.add("#7FD000");
        arrayList.add("#FFCA00");
        arrayList.add("#FFBB00");
        arrayList.add("#FFAD00");
        arrayList.add("#FF8000");
        arrayList.add("#F95700");
        arrayList.add("#EE4036");
        arrayList.add("#D52B1E");
        arrayList.add("#B80000");
        arrayList.add("#9C005E");
        arrayList.add("#90278E");
        arrayList.add("#652D90");
        arrayList.add("#4E2B8F");
        arrayList.add("#6436AF");
        arrayList.add("#7A3DD8");
        arrayList.add("#9457FA");
        arrayList.add("#FF59CC");
        arrayList.add("#E31C9E");
        arrayList.add("#C9007A");
        arrayList.add("#810035");
        arrayList.add("#A52A2A");
        arrayList.add("#8B4513");
        arrayList.add("#7A5649");
        arrayList.add("#5E4138");
        arrayList.add("#424243");
        arrayList.add("#455A64");
        arrayList.add("#66757f");
        return arrayList;
    }

    public static String getDefaultCategory(Context context, int i) {
        return new String[]{
                getResourceString(context, R.string.category_bills),
                getResourceString(context, R.string.category_clothing),
                getResourceString(context, R.string.category_education),
                getResourceString(context, R.string.category_entertainment),
                getResourceString(context, R.string.category_fitness),
                getResourceString(context, R.string.category_food_and_beverages),
                getResourceString(context, R.string.category_gifts),
                getResourceString(context, R.string.category_health_and_beauty),
                getResourceString(context, R.string.category_furniture),
                getResourceString(context, R.string.category_pet),
                getResourceString(context, R.string.category_shopping),
                getResourceString(context, R.string.category_transportation),
                getResourceString(context, R.string.category_travel),
                getResourceString(context, R.string.category_others),
                getResourceString(context, R.string.category_allowance),
                getResourceString(context, R.string.category_award),
                getResourceString(context, R.string.category_bonus),
                getResourceString(context, R.string.category_dividend),
                getResourceString(context, R.string.category_investment),
                getResourceString(context, R.string.category_lottery),
                getResourceString(context, R.string.category_salary),
                getResourceString(context, R.string.category_tips),
                getResourceString(context, R.string.category_others),
                getResourceString(context, R.string.adjustment),
                getResourceString(context, R.string.loan),
                getResourceString(context, R.string.repay),
                getResourceString(context, R.string.debt),
                getResourceString(context, R.string.collect)}[i - 1];
    }

    private static String getResourceString(Context context, int id) {
        return context.getResources().getString(id);
    }
}