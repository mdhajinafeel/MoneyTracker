package com.nprotech.moneytracker.db.entites;

import android.content.Context;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.nprotech.moneytracker.helper.DataHelper;

import java.io.Serializable;

@Entity(tableName = "categories")
public class CategoryEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String color;
    public int type;
    public boolean active;
    public int ordering;
    public int icon;
    public int defaultCategory;
    public boolean isIncludeReport;

    public CategoryEntity(String name, String color, int icon, int type, boolean active, int ordering, int defaultCategory, boolean isIncludeReport) {
        this.name = name;
        this.color = color;
        this.type = type;
        this.active = active;
        this.icon = icon;
        this.ordering = ordering;
        this.defaultCategory = defaultCategory;
        this.isIncludeReport = isIncludeReport;

    }

    public String getName(Context context) {
        String str = name;
        if (str == null || str.isEmpty()) {
            int i = defaultCategory;
            return i != 0 ? DataHelper.getDefaultCategory(context, i) : "";
        }
        return name;
    }
}