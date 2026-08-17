package com.nprotech.moneytracker.models;

public class GoalWithDetails {

    public String name, currencySymbol, currencyCode, currencyName, categoryName, color, description, notes;
    public int id, icon, categoryId, currencyId, autoSaveFrequency, autoSaveWeekDay, autoSaveDayOfMonth, autoSaveMonth, autoSaveDay;
    public double targetAmount, savedAmount, goalAmount, autoSaveAmount, initialAmount;
    public long startedDate, targetDate, moneyDate, nextAutoSaveDate, autoSaveStartDate, createdAt, completedOn, archivedOn;
    public boolean autoSaveEnabled, isCompleted, isArchived;
}