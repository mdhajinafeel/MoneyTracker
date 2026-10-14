package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "accounts",
        indices = {
                @Index(value = {"ordering"}),
                @Index(value = {"serverId"}),
                @Index(value = {"isDeleted"})
        }
)
public class AccountEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String currencyCode;
    public String currencyName;
    public String currencySymbol;
    public double initialAmount;
    public double balance;
    public int ordering;
    public boolean isDeleted = false;
    public boolean isSynced = false;
    public boolean isDefault = false;
    public boolean isArchived = false;
    public long archivedAt;
    public int serverId = 0;
    public String tempAccountServerId;
    public int categoryIcon;
    public String accountColor;
}