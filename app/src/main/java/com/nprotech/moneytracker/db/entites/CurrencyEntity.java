package com.nprotech.moneytracker.db.entites;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "currencies")
public class CurrencyEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;
    @NonNull
    public String code;
    public String name;
    public String symbol;
    public boolean isDefault;

    public CurrencyEntity(@NonNull String code, String name, String symbol, boolean isDefault) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
        this.isDefault = isDefault;
    }
}