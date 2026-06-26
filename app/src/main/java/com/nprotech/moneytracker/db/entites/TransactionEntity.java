package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(
        tableName = "transactions",
        indices = {
                @Index("accountId"),
                @Index("walletId"),
                @Index("categoryId"),
                @Index("serverId"),
                @Index("tempTransactionServerId"),
                @Index("isSynced"),
                @Index("isDeleted"),
        }
)
public class TransactionEntity implements Serializable {

    // Transaction Types
    public static final int TYPE_INCOME = 1;
    public static final int TYPE_EXPENSE = 2;
    public static final int TYPE_TRANSFER = 3;

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String tempTransactionServerId;
    public long serverId;
    public long accountId;
    public int type;
    public double amount;
    public double fee;
    public int walletId;
    public Integer fromWalletId;
    public Integer categoryId;
    public String description;
    public String memo;
    public long transactionDate;
    public long createdAt;
    public long updatedAt;
    public boolean isSynced = false;
    public boolean isDeleted = false;

    public TransactionEntity() {
    }

    public boolean isIncome() {
        return type == TYPE_INCOME;
    }

    public boolean isExpense() {
        return type == TYPE_EXPENSE;
    }

    public boolean isTransfer() {
        return type == TYPE_TRANSFER;
    }
}