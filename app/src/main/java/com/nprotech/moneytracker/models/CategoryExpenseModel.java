package com.nprotech.moneytracker.models;

import android.content.Context;

import com.nprotech.moneytracker.helper.DataHelper;

public class CategoryExpenseModel {

    public int categoryId, defaultCategoryId, transactionCount, icon, walletId, type, goalId;
    public String categoryName, color;
    public double amount, percentage;

    public CategoryExpenseModel(int categoryId, int defaultCategoryId, String categoryName, String color, double amount) {
        this.categoryId = categoryId;
        this.defaultCategoryId = defaultCategoryId;
        this.categoryName = categoryName;
        this.color = color;
        this.amount = amount;
    }

    public String getCategoryName(Context context) {
        return getCategory(context);
    }

    public String getCategory(Context context) {
        if (this.defaultCategoryId != 0) {
            return DataHelper.getDefaultCategory(context, this.defaultCategoryId);
        }
        return "";
    }
}