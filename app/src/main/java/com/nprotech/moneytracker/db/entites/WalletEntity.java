package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "wallets",
        indices = {
                @Index(value = {"accountId", "isDeleted", "isArchived"}),
                @Index(value = {"accountId", "currencyCode", "isActive", "isDeleted"}),
                @Index(value = {"accountId", "ordering"}),
                @Index(value = {"accountId", "isDefault"}),
                @Index(value = {"walletServerId"}),
                @Index(value = {"tempWalletServerId"})
        }
)
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
    public double creditLimit;
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
    public boolean isDefault = false;
    public boolean isArchived = false;
    public long archivedAt;
    public int walletServerId = 0;
    public String tempWalletServerId;
}