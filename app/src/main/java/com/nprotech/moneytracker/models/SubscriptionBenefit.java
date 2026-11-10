package com.nprotech.moneytracker.models;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

public class SubscriptionBenefit {

    public String title, description;
    public int iconRes, backgroundColor, foregroundColor;

    public SubscriptionBenefit(String title, String description, @DrawableRes int iconRes, @ColorRes int foregroundColor, @ColorRes int backgroundColor) {
        this.title = title;
        this.description = description;
        this.iconRes = iconRes;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
    }
}