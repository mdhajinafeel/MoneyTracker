package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "budget_category",
        primaryKeys = {"budgetId", "categoryId"},
        indices = {
                @Index(value = {"budgetCategoryId"}),
                @Index(value = {"tempBudgetCategoryId"}),
                @Index(value = {"budgetServerId"}),
                @Index(value = {"tempBudgetServerId"}),
                @Index(value = {"isSynced"}),
                @Index(value = {"isDeleted"})
        }
)
public class BudgetCategoryEntity {

    public int budgetId;
    public int categoryId;
    public boolean isSynced;
    public boolean isDeleted;
    public long createdAt;
    public long updatedAt;
    public int budgetCategoryId;
    public String tempBudgetCategoryId;
    public int budgetServerId;
    public String tempBudgetServerId;
}