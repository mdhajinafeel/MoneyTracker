package com.nprotech.moneytracker.enums;

public enum CalendarFilterType {

    DAILY(1),
    WEEKLY(2),
    MONTHLY(3),
    QUARTERLY(4),
    YEARLY(5),
    ALL(6),
    CUSTOM(7);

    private final int id;

    CalendarFilterType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CalendarFilterType fromId(int id) {
        for (CalendarFilterType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return MONTHLY;
    }
}