package com.nprotech.moneytracker.models;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

import com.nprotech.moneytracker.enums.SettingType;

import java.io.Serializable;

public class SettingItemModel implements Serializable {

    public SettingType settingType;
    public String title, subTitle;
    public boolean isEnabled, enabledSubTitle, navigationVisible, isPremium;
    public int icon, fgColor, bgColor, navigationColor;

    public SettingItemModel(SettingType settingType, @DrawableRes int icon, @ColorRes int fgColor, @ColorRes int bgColor, String title, boolean isEnabled,
                            boolean enabledSubTitle, String subTitle, boolean navigationVisible, boolean isPremium, @ColorRes int navigationColor) {
        this.settingType = settingType;
        this.title = title;
        this.isEnabled = isEnabled;
        this.enabledSubTitle = enabledSubTitle;
        this.subTitle = subTitle;
        this.navigationVisible = navigationVisible;
        this.isPremium = isPremium;
        this.icon = icon;
        this.fgColor = fgColor;
        this.bgColor = bgColor;
        this.navigationColor = navigationColor;
    }
}