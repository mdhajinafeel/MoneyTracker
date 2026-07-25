package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "wallets")
public class WalletEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int accountId;
    public String name;
    public String walletColor;
    public int walletType;
    public String currencyName;
    public String currencyCode;
    public String currencySymbol;
    public int categoryIcon;
    public double initialAmount;
    public double amount;
    public double exchangeRate;
    public long dueDate;
    public long statementDate;
    public int ordering;
    public boolean isHidden;
    public boolean isExclude;
    public boolean isActive;
    public boolean isSynced = false;
    public boolean isDeleted = false;
}