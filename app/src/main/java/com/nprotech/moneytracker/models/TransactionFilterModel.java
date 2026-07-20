package com.nprotech.moneytracker.models;

public class TransactionFilterModel {

    public int accountId;
    public long startDate, endDate;

    public TransactionFilterModel(int accountId, long startDate, long endDate) {
        this.accountId = accountId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}