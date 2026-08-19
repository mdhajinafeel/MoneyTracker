package com.nprotech.moneytracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.nprotech.moneytracker.db.entites.GoalContributionEntity;
import com.nprotech.moneytracker.db.entites.GoalEntity;
import com.nprotech.moneytracker.models.GoalContributionSummary;
import com.nprotech.moneytracker.models.GoalContributionWithCurrency;
import com.nprotech.moneytracker.models.GoalWithDetails;

import java.util.List;

@Dao
public interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GoalEntity> list);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertGoal(GoalEntity goal);

    @Query("SELECT g.name, g.targetAmount, g.savedAmount, g.targetDate, g.currencyId, " +
            "c.name AS categoryName, c.color, c.icon, g.id, g.startedDate, '' AS description, 0 AS moneyDate, g.category AS categoryId, " +
            "cu.symbol AS currencySymbol, cu.code AS currencyCode, cu.name AS currencyName, 0 AS goalAmount, g.autoSaveAmount, g.autoSaveEnabled, g.nextAutoSaveDate, " +
            "g.autoSaveFrequency, g.autoSaveStartDate, g.createdAt, g.notes, g.isCompleted, g.isArchived, g.completedOn, g.archivedOn, g.initialAmount, " +
            "g.autoSaveWeekDay, g.autoSaveDayOfMonth, g.autoSaveMonth, g.autoSaveDay " +
            "FROM goals g " +
            "JOIN categories c ON c.id = g.category AND c.type = 5 " +
            "JOIN currencies cu ON cu.id = g.currencyId " +
            "WHERE g.isDeleted = 0 AND g.id = :goalId")
    GoalWithDetails fetchGoalDetails(int goalId);

    @Query("SELECT g.name, g.targetAmount, g.savedAmount, g.targetDate, g.currencyId, " +
            "c.name AS categoryName, c.color, c.icon, g.id, g.startedDate, '' AS description, 0 AS moneyDate, g.category AS categoryId,  " +
            "cu.symbol AS currencySymbol, cu.code AS currencyCode, cu.name AS currencyName, 0 AS goalAmount, g.autoSaveAmount, g.autoSaveEnabled, g.nextAutoSaveDate, " +
            "g.autoSaveFrequency, g.autoSaveStartDate, g.createdAt, g.notes, g.isCompleted, g.isArchived, g.completedOn, g.archivedOn, g.initialAmount, " +
            "g.autoSaveWeekDay, g.autoSaveDayOfMonth, g.autoSaveMonth, g.autoSaveDay " +
            "FROM goals g " +
            "JOIN categories c ON c.id = g.category AND c.type = 5 " +
            "JOIN currencies cu ON cu.id = g.currencyId " +
            "WHERE g.isDeleted = 0 AND g.accountId = :accountId AND g.isArchived = :isArchived AND g.isCompleted = :isCompleted")
    LiveData<List<GoalWithDetails>> getGoals(int accountId, boolean isArchived,  boolean isCompleted);

    @Query("SELECT g.name, g.targetAmount, g.savedAmount, g.targetDate, g.currencyId, " +
            "c.name AS categoryName, c.color, c.icon, g.id, g.startedDate, '' AS description, " +
            "0 AS moneyDate, g.category AS categoryId, " +
            "cu.symbol AS currencySymbol, cu.code AS currencyCode, cu.name AS currencyName, " +
            "0 AS goalAmount, g.autoSaveAmount, g.autoSaveEnabled, g.nextAutoSaveDate, " +
            "g.autoSaveFrequency, g.autoSaveStartDate, g.createdAt, g.notes, " +
            "g.isCompleted, g.isArchived, g.completedOn, g.archivedOn, g.initialAmount, " +
            "g.autoSaveWeekDay, g.autoSaveDayOfMonth, g.autoSaveMonth, g.autoSaveDay " +
            "FROM goals g " +
            "JOIN categories c ON c.id = g.category AND c.type = 5 " +
            "JOIN currencies cu ON cu.id = g.currencyId " +
            "WHERE g.isDeleted = 0 " +
            "AND g.accountId = :accountId " +
            "AND g.isArchived = 1")
    LiveData<List<GoalWithDetails>> getArchivedGoals(int accountId);

    @Query("SELECT g.name, g.targetAmount, g.savedAmount, g.targetDate, g.currencyId, " +
            "c.name AS categoryName, c.color, c.icon, g.id, g.startedDate, '' AS description, 0 AS moneyDate, g.category AS categoryId, " +
            "cu.symbol AS currencySymbol, cu.code AS currencyCode, cu.name AS currencyName, 0 AS goalAmount, g.autoSaveAmount, g.autoSaveEnabled, g.nextAutoSaveDate, " +
            "g.autoSaveFrequency, g.autoSaveStartDate, g.createdAt, g.notes, g.isCompleted, g.isArchived, g.completedOn, g.archivedOn, g.initialAmount, " +
            "g.autoSaveWeekDay, g.autoSaveDayOfMonth, g.autoSaveMonth, g.autoSaveDay " +
            "FROM goals g " +
            "JOIN categories c ON c.id = g.category AND c.type = 5 " +
            "JOIN currencies cu ON cu.id = g.currencyId " +
            "WHERE g.isDeleted = 0 AND g.id = :goalId")
    LiveData<GoalWithDetails> getGoalDetailById(int goalId);

    @Update
    int updateGoal(GoalEntity goal);

    @Query("""
        SELECT * FROM goals
        WHERE autoSaveEnabled = 1
        AND nextAutoSaveDate > 0
        AND nextAutoSaveDate <= :currentTime
        AND isDeleted = 0
        AND isArchived = 0
        AND isCompleted = 0
        """)
    List<GoalEntity> getDueAutoSaveGoals(long currentTime);

    @Query("""
        SELECT MIN(nextAutoSaveDate)
        FROM goals
        WHERE autoSaveEnabled = 1
        AND nextAutoSaveDate > 0
        AND isDeleted = 0
        AND isArchived = 0
        AND isCompleted = 0
        """)
    Long getEarliestAutoSaveDate();

    @Query("UPDATE goals SET savedAmount = :savedAmount, updatedAt = :updatedAt WHERE id = :goalId")
    void updateSavedAmount(int goalId, double savedAmount, long updatedAt);

    @Query("UPDATE goals SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :goalId")
    int deleteGoal(int goalId, long updatedAt);

    @Query("UPDATE goals SET updatedAt = :updatedAt, nextAutoSaveDate = 0, autoSaveStartDate = 0, autoSaveMonth = 0, autoSaveDayOfMonth = 0, autoSaveDay = 0, " +
            "autoSaveAmount = 0, autoSaveFrequency = 0, autoSaveWeekDay = 0, autoSaveEnabled = 0 WHERE id = :goalId")
    int disableAutoSave(int goalId, long updatedAt);

    @Query("UPDATE goals SET isArchived = :isArchive, updatedAt = :updatedAt, archivedOn = :archivedOn WHERE id = :goalId")
    int archiveRestoreGoal(int goalId, long updatedAt, boolean isArchive, long archivedOn);

    @Query("UPDATE goals SET isCompleted = 1, updatedAt = :updatedAt WHERE id = :goalId")
    int markAsCompletedGoal(int goalId, long updatedAt);

    @Query("UPDATE goals SET isCompleted = 0, updatedAt = :updatedAt WHERE id = :goalId")
    int markAsInProgressGoal(int goalId, long updatedAt);

    @Query("UPDATE goals SET initialAmount = 0, updatedAt = :updatedAt WHERE id = :goalId")
    void updateInitialAmount(int goalId, long updatedAt);

    @Query("SELECT * FROM goals WHERE id = :goalId LIMIT 1")
    GoalEntity getGoal(int goalId);

    @Query("""
            SELECT COALESCE(
                SUM(
                    CASE
                        WHEN type IN (1, 2, 3) THEN amount
                        WHEN type = 4 THEN -amount
                        ELSE 0
                    END
                ), 0
            )
            FROM goal_contributions
            WHERE goalId = :goalId AND isDeleted = 0
            """)
    double getCurrentAmount(long goalId);

    @Query("""
        SELECT gc.*, c.symbol AS currencySymbol
        FROM goal_contributions gc
        INNER JOIN goals g ON g.id = gc.goalId
        INNER JOIN currencies c ON c.id = g.currencyId
        WHERE gc.goalId = :goalId AND gc.isDeleted = 0
        ORDER BY gc.date DESC
        LIMIT 5
        """)
    LiveData<List<GoalContributionWithCurrency>> getRecentContributions(int goalId);

    @Query("""
        SELECT gc.*, c.symbol AS currencySymbol
        FROM goal_contributions gc
        INNER JOIN goals g ON g.id = gc.goalId
        INNER JOIN currencies c ON c.id = g.currencyId
        WHERE gc.goalId = :goalId AND gc.isDeleted = 0
        ORDER BY gc.date DESC
        LIMIT :limit OFFSET :offset
        """)
    List<GoalContributionWithCurrency> getContributions(int goalId, int limit, int offset);

    @Insert
    long insertContribution(GoalContributionEntity contribution);

    @Query("UPDATE goal_contributions SET isDeleted = 1 WHERE goalId = :goalId AND id = :contributionId")
    int deleteContribution(int goalId, int contributionId);

    @Query("""
        SELECT COUNT(*)
        FROM goals
        WHERE accountId = :accountId
          AND isArchived = 0
          AND isCompleted = 0
          AND isDeleted = 0
        """)
    LiveData<Integer> getActiveGoalCount(int accountId);

    @Query("SELECT * FROM goal_contributions WHERE goalId = :goalId AND isDeleted = 0 AND type = :type LIMIT 1")
    GoalContributionEntity getInitialContribution(int goalId, int type);

    @Query("SELECT gc.*, c.symbol AS currencySymbol " +
            "FROM goal_contributions gc " +
            "INNER JOIN goals g ON g.id = gc.goalId " +
            "INNER JOIN currencies c ON c.id = g.currencyId " +
            "WHERE gc.goalId = :goalId AND gc.isDeleted = 0 AND gc.id = :contributionId")
    GoalContributionWithCurrency getContribution(int goalId, int contributionId);

    @Update
    void updateContribution(GoalContributionEntity contribution);

    @Query("SELECT COALESCE(SUM( CASE WHEN type = :autoSaveType THEN amount ELSE 0 END ), 0) AS autoSaveAmount, " +
            "COALESCE(SUM( CASE WHEN type = :autoSaveType THEN 1 ELSE 0 END ), 0) AS autoSaveCount, " +
            "COALESCE(SUM( CASE WHEN type = :manualType THEN amount ELSE 0 END ), 0) AS manualAmount, " +
            "COALESCE(SUM( CASE WHEN type = :manualType THEN 1 ELSE 0 END ), 0) AS manualCount, " +
            "COALESCE(SUM( CASE WHEN type = :initialType THEN amount ELSE 0 END ), 0) AS initialAmount, " +
            "COALESCE(SUM( CASE WHEN type = :initialType THEN 1 ELSE 0 END ), 0) AS initialCount, " +
            "COALESCE(SUM( CASE WHEN type = :withdrawalType THEN amount ELSE 0 END ), 0) AS withdrawalAmount, " +
            "COALESCE(SUM( CASE WHEN type = :withdrawalType THEN 1 ELSE 0 END ), 0) AS withdrawalCount, " +
            "c.symbol AS currencySymbol " +
            "FROM goal_contributions gc " +
            "INNER JOIN goals g ON g.id = gc.goalId " +
            "INNER JOIN currencies c ON c.id = g.currencyId " +
            "WHERE goalId = :goalId AND gc.isDeleted = 0")
    LiveData<GoalContributionSummary> getContributionSummary(int goalId, int autoSaveType, int manualType, int initialType, int withdrawalType);
}