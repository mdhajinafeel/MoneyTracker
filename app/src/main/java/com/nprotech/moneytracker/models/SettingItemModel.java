package com.nprotech.moneytracker.models;

public class SettingItemModel {

    public int settingId;
    public String title, subTitle;
    public boolean switchVisible, isEnabled, enabledSubTitle;

    public SettingItemModel(int settingId, String title, boolean switchVisible, boolean isEnabled, boolean enabledSubTitle, String subTitle) {
        this.settingId = settingId;
        this.title = title;
        this.switchVisible = switchVisible;
        this.isEnabled = isEnabled;
        this.enabledSubTitle = enabledSubTitle;
        this.subTitle = subTitle;
    }
}