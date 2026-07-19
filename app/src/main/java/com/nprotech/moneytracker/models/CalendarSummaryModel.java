package com.nprotech.moneytracker.models;

import java.io.Serializable;

public class CalendarSummaryModel implements Serializable {

    public long dayTimestamp;
    public double income, expense, total;

    public CalendarSummaryModel() {
    }

    public double getNet() {
        return income - expense;
    }
}