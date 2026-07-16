package com.nprotech.moneytracker.models;

import com.nprotech.moneytracker.enums.SettingType;

public class SettingItemModel {

    public SettingType settingType;
    public String title, subTitle;
    public boolean isEnabled, enabledSubTitle, navigationVisible, isPremium;

    public SettingItemModel(SettingType settingType, String title, boolean isEnabled,
                            boolean enabledSubTitle, String subTitle, boolean navigationVisible, boolean isPremium) {
        this.settingType = settingType;
        this.title = title;
        this.isEnabled = isEnabled;
        this.enabledSubTitle = enabledSubTitle;
        this.subTitle = subTitle;
        this.navigationVisible = navigationVisible;
        this.isPremium = isPremium;
    }
}