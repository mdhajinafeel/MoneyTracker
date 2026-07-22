package com.nprotech.moneytracker.models;

public class CalendarRangeModel {

    public int accountId;
    public long startDate, endDate;
    public String title;

    public CalendarRangeModel() {
    }

    public CalendarRangeModel(int accountId, long startDate, long endDate, String title) {
        this.accountId = accountId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = title;
    }
}