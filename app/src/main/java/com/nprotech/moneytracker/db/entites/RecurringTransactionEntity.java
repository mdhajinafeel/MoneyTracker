package com.nprotech.moneytracker.db.entites;

import android.content.Context;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.DataHelper;

@Entity(tableName = "recurring_transactions")
public class RecurringTransactionEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int accountId;
    public int type;
    public int walletId;
    public Integer fromWalletId;
    public Integer categoryId;
    public Integer defaultCategoryId;
    public double amount;
    public double fee;
    public String description;
    public String notes;

    // Recurrence
    public int repeatType;
    public int repeatInterval;

    // Weekly
    public String repeatWeekDays;

    // Monthly
    public int monthlyMode;
    public int monthlyDay;

    // Yearly
    public int yearlyMonth;
    public int yearlyDay;

    // Start / end
    public long startDate;
    public long untilDate;

    // Forever / Until / Times
    public int repeatTimes;

    // Number of transactions already created
    public int completedTimes;

    // Next occurrence
    public long nextRunDate;

    public boolean reminder;
    public boolean reminderShown = false;
    public boolean createTransaction;
    public boolean isActive;

    public long createdAt;
    public long updatedAt;
    public String tempRecurringServerId;
    public long serverId;
    public boolean isSynced = false;
    public boolean isDeleted = false;
    public int status = STATUS_IN_PROGRESS;

    public static final int STATUS_IN_PROGRESS = 0;
    public static final int STATUS_PAUSED = 1;
    public static final int STATUS_COMPLETED = 2;

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