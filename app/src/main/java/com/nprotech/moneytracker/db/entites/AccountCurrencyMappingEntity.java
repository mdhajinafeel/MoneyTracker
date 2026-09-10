package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "account_currency_mapping",
        indices = {
                @Index(value = {"accountId", "isActive"}),
                @Index(value = {"accountId", "currencyId", "isActive"}),
                @Index(value = {"mappingServerId"}),
                @Index(value = {"isSynced"})
        }
)
public class AccountCurrencyMappingEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int accountId;
    public String tempAccountServerId;
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
    public boolean isBase;
    public boolean isSynced = false;
    public int mappingServerId = 0;
}