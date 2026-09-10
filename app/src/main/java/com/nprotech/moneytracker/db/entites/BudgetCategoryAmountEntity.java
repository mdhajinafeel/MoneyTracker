package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "budget_category_amount",
        primaryKeys = {"budgetId", "categoryId"},
        indices = {
                @Index(value = {"isSynced"}),
                @Index(value = {"isDeleted"}),
                @Index(value = {"budgetCategoryAmountId"}),
                @Index(value = {"tempBudgetCategoryAmountId"}),
                @Index(value = {"budgetServerId"}),
                @Index(value = {"tempBudgetServerId"})
        }
)
public class BudgetCategoryAmountEntity {

    public int budgetId;
    public int categoryId;
    public double amount;
    public boolean isSynced;
    public boolean isDeleted;
    public long createdAt;
    public long updatedAt;
    public int budgetCategoryAmountId;
    public String tempBudgetCategoryAmountId;
    public int budgetServerId;
    public String tempBudgetServerId;
}