package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "accounts",
        indices = {
                @Index(value = {"ordering"})
        }
)
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
    public int serverId = 0;
    public String tempAccountServerId;
}