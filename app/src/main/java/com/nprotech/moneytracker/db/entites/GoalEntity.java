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
    public double initialAmount;
    public long targetDate;
    public int category;
    public int currencyId;
    public int accountId;
    public String notes;

    // Auto Save
    public boolean autoSaveEnabled;
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
    public long completedOn;
    public boolean isArchived;
    public long archivedOn;
    public long startedDate;
    public long createdAt;
    public long updatedAt;
}