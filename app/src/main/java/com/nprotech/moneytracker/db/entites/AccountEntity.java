package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "accounts")
public class AccountEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String currencyCode;
    public String currencyName;
    public String currencySymbol;
    public double balance;
    public int ordering;
    public boolean isDeleted = false;
    public boolean isSynced = false;
}