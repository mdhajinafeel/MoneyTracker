package com.nprotech.moneytracker.models;

import com.nprotech.moneytracker.enums.CalendarFilterType;

import java.util.Date;

public class BreakdownFilter {

    public int accountId;
    public CalendarFilterType filter;
    public Date date;
    public long startDate;
    public long endDate;

    public BreakdownFilter(int accountId,
                           CalendarFilterType filter,
                           Date date,
                           long startDate,
                           long endDate) {

        this.accountId = accountId;
        this.filter = filter;
        this.date = date;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}