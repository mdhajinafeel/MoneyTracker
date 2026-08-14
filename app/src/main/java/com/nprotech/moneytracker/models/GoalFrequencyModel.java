package com.nprotech.moneytracker.models;

public class GoalFrequencyModel {

    public int frequency, icon;
    public String frequencyName;

    public GoalFrequencyModel(int frequency, int icon, String frequencyName) {
        this.frequency = frequency;
        this.icon = icon;
        this.frequencyName = frequencyName;
    }
}
