package com.nprotech.moneytracker.models;

import com.nprotech.moneytracker.enums.SettingType;

public class SettingItemModel {

    public SettingType settingType;
    public String title, subTitle;
    public boolean switchVisible, isEnabled, enabledSubTitle, navigationVisible;

    public SettingItemModel(SettingType settingType, String title, boolean switchVisible, boolean isEnabled, boolean enabledSubTitle, String subTitle, boolean navigationVisible) {
        this.settingType = settingType;
        this.title = title;
        this.switchVisible = switchVisible;
        this.isEnabled = isEnabled;
        this.enabledSubTitle = enabledSubTitle;
        this.subTitle = subTitle;
        this.navigationVisible = navigationVisible;
    }
}