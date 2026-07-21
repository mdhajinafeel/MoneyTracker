package com.nprotech.moneytracker.models;

public class CategoryExpenseModel {

    public int categoryId, defaultCategoryId;
    public String categoryName, color;
    public double amount, percentage;

    public CategoryExpenseModel(int categoryId, int defaultCategoryId, String categoryName, String color, double amount) {
        this.categoryId = categoryId;
        this.defaultCategoryId = defaultCategoryId;
        this.categoryName = categoryName;
        this.color = color;
        this.amount = amount;
    }
}