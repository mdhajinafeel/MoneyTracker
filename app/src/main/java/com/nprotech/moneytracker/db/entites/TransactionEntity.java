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
    public Integer defaultCategoryId;
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

    public String getCategoryName(Context context) {
        return this.type == 3 ? context.getString(R.string.transfer) : getCategory(context);
    }

    public String getCategory(Context context) {
        if (this.defaultCategoryId != 0) {
            return DataHelper.getDefaultCategory(context, this.defaultCategoryId);
        }
        return "";
    }
}