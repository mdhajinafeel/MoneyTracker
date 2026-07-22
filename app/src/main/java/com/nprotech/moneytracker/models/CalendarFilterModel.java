package com.nprotech.moneytracker.models;

import com.nprotech.moneytracker.enums.CalendarFilterType;

public class CalendarFilterModel {

    public CalendarFilterType type;
    public int icon, id;
    public String filterName;
    public boolean isSelected;

    public CalendarFilterModel(CalendarFilterType type, int id, int icon, String filterName, boolean isSelected) {
        this.type = type;
        this.id = id;
        this.icon = icon;
        this.filterName = filterName;
        this.isSelected = isSelected;
    }
}