package com.nprotech.moneytracker.models;

import android.content.Context;

import com.nprotech.moneytracker.helper.DataHelper;

public class CategoryBudgetProgress {

    public int categoryId;
    public int defaultCategory;
    public String categoryName;
    public String currencySymbol;
    public int icon;
    public String color;
    public double budgetAmount;
    public double spentAmount;

    public double getRemainingAmount() {
        return budgetAmount - spentAmount;
    }

    public double getPercentage() {
        if (budgetAmount <= 0) {
            return 0;
        }

        return (spentAmount / budgetAmount) * 100.0;
    }

    public String getCategoryName(Context context) {
        if (categoryName == null || categoryName.isEmpty()) {
            return defaultCategory != 0
                    ? DataHelper.getDefaultCategory(context, defaultCategory)
                    : "";
        }

        return categoryName;
    }
}