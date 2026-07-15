package com.nprotech.moneytracker.models;

import android.content.Context;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.DataHelper;

import java.io.Serializable;

public class TransactionCategoryModel implements Serializable {

    private int defaultCategoryId, categoryId, icon, transactionCount, type;
    private double amount;
    private String color, currencySymbol, categoryName;

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public int getDefaultCategoryId() {
        return defaultCategoryId;
    }

    public void setDefaultCategoryId(int defaultCategoryId) {
        this.defaultCategoryId = defaultCategoryId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategory(Context context) {
        return this.type == 3 ? context.getString(R.string.transfer) : getDefaultCategory(context);
    }

    public String getDefaultCategory(Context context) {
        if (this.defaultCategoryId != 0) {
            return DataHelper.getDefaultCategory(context, this.defaultCategoryId);
        }
        return "";
    }
}