package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "budget_wallet",
        primaryKeys = {"budgetId", "walletId"},
        indices = {
                @Index(value = {"budgetWalletId"}),
                @Index(value = {"tempBudgetWalletId"}),
                @Index(value = {"budgetServerId"}),
                @Index(value = {"tempBudgetServerId"}),
                @Index(value = {"isSynced"}),
                @Index(value = {"isDeleted"})
        }
)
public class BudgetWalletEntity {

    public int budgetId;
    public int walletId;
    public boolean isSynced;
    public boolean isDeleted;
    public long createdAt;
    public long updatedAt;
    public int budgetWalletId;
    public String tempBudgetWalletId;
    public int budgetServerId;
    public String tempBudgetServerId;
}