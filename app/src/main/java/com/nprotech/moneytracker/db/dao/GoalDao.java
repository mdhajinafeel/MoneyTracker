package com.nprotech.moneytracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.nprotech.moneytracker.db.entites.GoalEntity;
import com.nprotech.moneytracker.models.GoalWithDetails;

import java.util.List;

@Dao
public interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GoalEntity> list);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertGoal(GoalEntity goal);

    @Query("SELECT g.name, g.targetAmount, g.savedAmount, g.targetDate, w.currencyCode, w.currencySymbol, w.currencyName, " +
            "c.name AS categoryName, c.color, c.icon, g.id, g.startedDate, '' AS description, 0 AS moneyDate, g.accountId, g.category AS categoryId, '' AS notes " +
            "FROM goals g " +
            "JOIN wallets w ON w.id = g.walletId " +
            "JOIN categories c ON c.id = g.category AND c.type = 5 " +
            "WHERE g.isDeleted = 0 AND g.accountId = :accountId AND g.isCompleted = :isCompleted")
    LiveData<List<GoalWithDetails>> getGoals(boolean isCompleted, int accountId);

    @Query("SELECT * FROM goals WHERE id = :goalId LIMIT 1")
    GoalEntity getGoalById(int goalId);

    @Query("SELECT g.name, g.targetAmount, g.savedAmount, g.targetDate, w.currencyCode, w.currencySymbol, w.currencyName, " +
            "c.name AS categoryName, c.color, c.icon, g.id, g.startedDate, '' AS description, 0 AS moneyDate, g.accountId, g.category AS categoryId, '' AS notes " +
            "FROM goals g " +
            "JOIN wallets w ON w.id = g.walletId " +
            "JOIN categories c ON c.id = g.category AND c.type = 5 " +
            "WHERE g.isDeleted = 0 AND g.id = :goalId")
    LiveData<GoalWithDetails> getGoalDetailById(int goalId);

    @Update
    void updateGoal(GoalEntity goal);

    @Query("""
            SELECT *
            FROM goals
            WHERE autoSaveEnabled = 1
            AND isDeleted = 0
            AND nextAutoSaveDate > 0
            AND nextAutoSaveDate <= :currentTime
            """)
    List<GoalEntity> getDueAutoSaveGoals(long currentTime);

    @Query("UPDATE goals SET savedAmount = :savedAmount, updatedAt = :updatedAt WHERE id = :goalId")
    void updateSavedAmount(int goalId, double savedAmount, long updatedAt);

    @Query("UPDATE goals SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :goalId")
    int deleteGoal(int goalId, long updatedAt);
}