package com.nprotech.moneytracker.models;

public class DayRangeModel {

    public int accountId;
    public long start, end;

    public DayRangeModel(int accountId, long start, long end) {
        this.accountId = accountId;
        this.start = start;
        this.end = end;
    }
}