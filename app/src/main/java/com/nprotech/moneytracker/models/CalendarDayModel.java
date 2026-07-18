package com.nprotech.moneytracker.models;

import java.io.Serializable;
import java.util.Date;

public class CalendarDayModel implements Serializable {

    public Date date;
    public int day;
    public boolean currentMonth,today,selected;
    public double income,expense,total;

    public boolean hasTransaction;

    public CalendarDayModel(Date date, int day, boolean currentMonth, boolean today, boolean selected) {
        this.date = date;
        this.day = day;
        this.currentMonth = currentMonth;
        this.today = today;
        this.selected = selected;
    }
}