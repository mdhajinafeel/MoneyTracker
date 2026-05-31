package com.nprotech.moneytracker.models;

public class SettingItemModel {

    public int settingId;
    public String title;
    public boolean switchVisible, isEnabled;

    public SettingItemModel(int settingId, String title, boolean switchVisible, boolean isEnabled) {
        this.settingId = settingId;
        this.title = title;
        this.switchVisible = switchVisible;
        this.isEnabled = isEnabled;
    }
}