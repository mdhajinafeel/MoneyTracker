package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "account_currency_mapping")
public class AccountCurrencyMappingEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public long accountId;
    public int currencyId;
    public String currencyCode;
    public String currencyName;
    public String currencySymbol;
    public int mainCurrencyId;
    public String mainCurrencyCode;
    public String mainCurrencyName;
    public String mainCurrencySymbol;
    public double conversionRate;
    public boolean isActive;
}