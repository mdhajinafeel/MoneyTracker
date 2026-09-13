package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "budget",
        indices = {
                @Index(value = {"budgetServerId"}),
                @Index(value = {"tempBudgetServerId"}),
                @Index(value = {"isSynced"}),
                @Index(value = {"isDeleted"})
        }
)
public class BudgetEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public int accountId;
    public int periodId;
    public int methodId;
    public long startDate;
    public long endDate;
    public int categoryCount;
    public boolean isAllCategory;
    public int walletCount;
    public double amount;
    public String budgetColor;
    public int budgetIcon;
    public boolean alertEnabled;
    public int alertPercentage;
    public boolean alertTriggered;
    public boolean repeatEnabled;
    public int repeatGroupId;
    public long nextRepeatDate;
    public boolean isSynced;
    public boolean isDeleted;
    public boolean isArchived;
    public boolean isPaused;
    public long createdAt;
    public long updatedAt;
    public long pausedOn = 0;
    public long archivedOn = 0;
    public int budgetServerId;
    public String tempBudgetServerId;
    public String currencySymbol;
    public String currencyCode;
    public String currencyName;
}