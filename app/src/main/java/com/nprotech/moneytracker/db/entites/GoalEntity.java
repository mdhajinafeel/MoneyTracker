package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "goals")
public class GoalEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    public double targetAmount;
    public double savedAmount;

    public long targetDate;

    public int category;

    // Goal Wallet
    public int walletId;
    public int accountId;

    // Auto Save
    public boolean autoSaveEnabled;
    public int autoSaveWalletId;
    public double autoSaveAmount;
    public int autoSaveFrequency;
    public long autoSaveStartDate;
    public int autoSaveWeekDay;
    public int autoSaveDayOfMonth;
    public int autoSaveMonth;
    public int autoSaveDay;
    public long nextAutoSaveDate;

    public boolean isSynced;
    public boolean isDeleted;
    public boolean isCompleted;
    public long startedDate;
    public long updatedAt;
}