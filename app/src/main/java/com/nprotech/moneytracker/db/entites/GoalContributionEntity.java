package com.nprotech.moneytracker.db.entites;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "goal_contributions",
        foreignKeys = @ForeignKey(entity = GoalEntity.class, parentColumns = "id", childColumns = "goalId", onDelete = ForeignKey.CASCADE),
        indices = {@Index("goalId")})
public class GoalContributionEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int goalId;
    private double amount;
    private int type;
    /*** 1 = INITIAL * 2 = ADD * 3 = AUTO_SAVE * 4 = WITHDRAW ***/
    private long date;
    @Nullable
    private String note;
    private long createdAt;
    private long updatedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGoalId() {
        return goalId;
    }

    public void setGoalId(int goalId) {
        this.goalId = goalId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Nullable
    public String getNote() {
        return note;
    }

    public void setNote(@Nullable String note) {
        this.note = note;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Ignore
    public GoalContributionEntity(int goalId, double amount, int type, long date, @Nullable String note, long createdAt, long updatedAt) {
        this.goalId = goalId;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.note = note;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public GoalContributionEntity() {
    }
}