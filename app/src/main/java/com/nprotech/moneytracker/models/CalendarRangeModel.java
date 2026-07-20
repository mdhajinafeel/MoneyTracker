package com.nprotech.moneytracker.models;

public class CalendarRangeModel {

    public int accountId;
    public long startDate, endDate;

    public CalendarRangeModel(int accountId, long startDate, long endDate) {
        this.accountId = accountId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}