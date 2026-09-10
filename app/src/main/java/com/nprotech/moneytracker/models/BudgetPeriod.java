package com.nprotech.moneytracker.models;

public class BudgetPeriod {

    public final long startDate;
    public final long endDate;

    public BudgetPeriod(long startDate, long endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
}