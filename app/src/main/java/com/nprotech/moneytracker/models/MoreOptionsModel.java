package com.nprotech.moneytracker.models;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

public class MoreOptionsModel {

    public String title, desc;
    public int id, icon, fgColor, bgColor, count;

    public MoreOptionsModel(int id, String title, String desc, @DrawableRes int icon, @ColorRes int fgColor, @ColorRes int bgColor, int count) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.icon = icon;
        this.fgColor = fgColor;
        this.bgColor = bgColor;
        this.count = count;
    }
}