package com.nprotech.moneytracker.models;

import java.util.Date;

public class BreakdownFilter {

    public int accountId;
    public Date transactionDate;

    public BreakdownFilter(int accountId, Date transactionDate) {
        this.accountId = accountId;
        this.transactionDate = transactionDate;
    }
}