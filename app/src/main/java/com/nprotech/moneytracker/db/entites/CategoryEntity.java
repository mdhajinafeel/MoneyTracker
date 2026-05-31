package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "categories")
public class CategoryEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String color;
    public int type;
    public boolean isActive;
    public int ordering;
    public int icon;
    public int categoryNameId;
}