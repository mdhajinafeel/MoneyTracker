package com.nprotech.moneytracker.models;

public class BalanceRangeModel {

    public int accountId;
    public long startDate, endDate;

    public BalanceRangeModel(int accountId, long startDate, long endDate) {
        this.accountId = accountId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}