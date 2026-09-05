package com.nprotech.moneytracker.db.entites;

import android.content.Context;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.DataHelper;

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
    public static final int TYPE_BUDGET = 4;
    public static final int TYPE_GOAL = 5;
    public static final int TYPE_DEBT = 6;
    public static final int TYPE_RECURRING = 7;

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String tempTransactionServerId;
    public long serverId;
    public long accountId;
    public int type;
    public double amount;
    public double fee;
    public int walletId;
    public double convertedAmount;
    public double accountAmount;
    public Integer fromWalletId;
    public Integer categoryId;
    public Integer defaultCategoryId;
    public String description;
    public String memo;
    public long transactionDate;
    public long createdAt;
    public long updatedAt;
    public boolean isSynced = false;
    public boolean isDeleted = false;
    public String parentTransactionId = "";
    public int goalId = 0;
    public boolean isFee = false;

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

    public String getCategoryName(Context context) {
        return this.type == 3 ? context.getString(R.string.transfer) : getCategory(context);
    }

    public String getCategory(Context context) {
        if (this.defaultCategoryId != 0) {
            return DataHelper.getDefaultCategory(context, this.defaultCategoryId);
        }
        return "";
    }

    public TransactionEntity(TransactionEntity source, long currentTime) {

        this.id = 0;
        this.tempTransactionServerId = "T_" + currentTime;
        this.serverId = 0;

        this.accountId = source.accountId;
        this.type = source.type;
        this.amount = source.amount;
        this.fee = source.fee;
        this.walletId = source.walletId;
        this.convertedAmount = source.convertedAmount;
        this.accountAmount = source.accountAmount;
        this.fromWalletId = source.fromWalletId;
        this.categoryId = source.categoryId;
        this.defaultCategoryId = source.defaultCategoryId;
        this.description = source.description;
        this.memo = source.memo;

        this.transactionDate = currentTime;
        this.createdAt = currentTime;
        this.updatedAt = currentTime;

        this.isSynced = false;
        this.isDeleted = false;

        this.parentTransactionId = "";
        this.goalId = source.goalId;
        this.isFee = false;
    }
}