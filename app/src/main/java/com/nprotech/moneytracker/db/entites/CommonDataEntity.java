package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "common_data")
public class CommonDataEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int type;
    public int value;
    public String languageCode;
    public int nameResId;
    public boolean active;

    public CommonDataEntity(int type, int value, String languageCode, int nameResId, boolean active) {
        this.type = type;
        this.value = value;
        this.languageCode = languageCode;
        this.nameResId = nameResId;
        this.active = active;
    }
}