package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "account_currency_mapping")
public class AccountCurrencyMappingEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public long accountId;
    public int currencyId;
    public String currencyCode;
    public String currencyName;
    public String currencySymbol;
    public boolean isActive;
}