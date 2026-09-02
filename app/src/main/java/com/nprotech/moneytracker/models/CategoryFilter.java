package com.nprotech.moneytracker.models;

public class CategoryFilter {

    private final int type;
    private final boolean active;

    public CategoryFilter(int type, boolean active) {
        this.type = type;
        this.active = active;
    }

    public int getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }
}