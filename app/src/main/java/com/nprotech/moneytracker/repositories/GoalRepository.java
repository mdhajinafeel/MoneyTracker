package com.nprotech.moneytracker.repositories;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.GoalContributionType;
import com.nprotech.moneytracker.db.dao.GoalDao;
import com.nprotech.moneytracker.db.entites.GoalContributionEntity;
import com.nprotech.moneytracker.db.entites.GoalEntity;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.models.GoalContributionSummary;
import com.nprotech.moneytracker.models.GoalContributionWithCurrency;
import com.nprotech.moneytracker.models.GoalWithDetails;

import java.util.List;

public class GoalRepository {

    private final GoalDao goalDao;

    public GoalRepository(GoalDao goalDao) {
        this.goalDao = goalDao;
    }

    public LiveData<List<GoalWithDetails>> getGoals(int accountId, boolean isArchived, boolean isCompleted) {
        return goalDao.getGoals(accountId, isArchived, isCompleted);
    }

    public LiveData<List<GoalWithDetails>> getArchivedGoals(int accountId) {
        return goalDao.getArchivedGoals(accountId);
    }

    public LiveData<GoalWithDetails> getGoalDetailById(int goalId) {
        return goalDao.getGoalDetailById(goalId);
    }

    public boolean deleteGoal(int goalId) {
        return goalDao.deleteGoal(goalId, System.currentTimeMillis()) > 0;
    }

    public boolean deleteContribution(int type, int goalId, int contributionId) {

        if(type == GoalContributionType.INITIAL) {
            goalDao.updateInitialAmount(goalId, System.currentTimeMillis());
        }

        boolean deleted = goalDao.deleteContribution(goalId, contributionId) > 0;

        GoalEntity goal = goalDao.getGoal(goalId);
        double current = goalDao.getCurrentAmount(goalId);

        goal.savedAmount = current;
        goal.isCompleted = current >= goal.targetAmount;
        if (goal.isCompleted) {
            goal.completedOn = System.currentTimeMillis();
        } else {
            goal.completedOn = 0;
        }
        goal.updatedAt = System.currentTimeMillis();
        goalDao.updateGoal(goal);

        return deleted;

    }

    public boolean disableAutoSave(int goalId) {
        return goalDao.disableAutoSave(goalId, System.currentTimeMillis()) > 0;
    }

    public boolean archiveRestoreGoal(int goalId, boolean isArchive) {
        if (goalDao.archiveRestoreGoal(goalId, System.currentTimeMillis(), isArchive, isArchive ? System.currentTimeMillis() : 0) > 0) {
            updateGoalCompletion(goalId);
        }

        return true;
    }

    public boolean markAsCompletedGoal(int goalId) {
        return goalDao.markAsCompletedGoal(goalId, System.currentTimeMillis()) > 0;
    }

    public boolean markAsInProgressGoal(int goalId) {
        return goalDao.markAsInProgressGoal(goalId, System.currentTimeMillis()) > 0;
    }

    // --------------------------------------------------
    // CREATE GOAL
    // --------------------------------------------------

    public long createGoal(GoalEntity goal, double initialAmount, Context context) {

        long now = System.currentTimeMillis();

        goal.createdAt = now;
        goal.updatedAt = now;

        if (goal.autoSaveEnabled) {
            goal.nextAutoSaveDate = DateHelper.calculateNextAutoSaveDate(goal, goal.autoSaveStartDate);
        } else {
            goal.nextAutoSaveDate = 0;
        }

        long goalId = goalDao.insertGoal(goal);

        if (goalId > 0 && initialAmount > 0) {

            GoalContributionEntity contribution = new GoalContributionEntity((int) goalId, initialAmount, GoalContributionType.INITIAL,
                    now, context.getString(R.string.initial_savings), now, now, false);
            long contributeId = goalDao.insertContribution(contribution);
            if (contributeId > 0) {
                goalDao.updateSavedAmount((int) goalId, initialAmount, now);
            }

            updateGoalCompletion((int) goalId);
        }


        return goalId;
    }

    // --------------------------------------------------
    // ADD MONEY
    // --------------------------------------------------

    public void addMoney(int goalId, double amount, long date, @Nullable String note, double savedAmount) {

        long now = System.currentTimeMillis();

        GoalContributionEntity contribution = new GoalContributionEntity(goalId, amount, GoalContributionType.ADD, date, note, now, now, false);
        long contributeId = goalDao.insertContribution(contribution);
        if (contributeId > 0) {
            goalDao.updateSavedAmount(goalId, savedAmount, now);
        }

        updateGoalCompletion(goalId);
    }

    // --------------------------------------------------
    // WITHDRAW MONEY
    // --------------------------------------------------

    public void withdrawMoney(int goalId, double amount, long date, @Nullable String note, double savedAmount) {

        long now = System.currentTimeMillis();

        GoalContributionEntity contribution = new GoalContributionEntity(goalId, amount, GoalContributionType.WITHDRAW, date, note, now, now, false);
        long contributeId = goalDao.insertContribution(contribution);
        if (contributeId > 0) {
            goalDao.updateSavedAmount(goalId, savedAmount, now);
        }
        updateGoalCompletion(goalId);
    }

    // --------------------------------------------------
    // CURRENT AMOUNT
    // --------------------------------------------------

    public double getCurrentAmount(int goalId) {
        return goalDao.getCurrentAmount(goalId);
    }

    private void updateGoalCompletion(int goalId) {

        GoalEntity goal = goalDao.getGoal(goalId);

        if (goal == null) {
            return;
        }

        double current = goalDao.getCurrentAmount(goalId);

        goal.isCompleted = current >= goal.targetAmount;
        if (goal.isCompleted) {
            goal.completedOn = System.currentTimeMillis();
        } else {
            goal.completedOn = 0;
        }
        goal.updatedAt = System.currentTimeMillis();
        goalDao.updateGoal(goal);
    }

    // --------------------------------------------------
    // AUTO SAVE
    // --------------------------------------------------

    public void executeAutoSave(GoalEntity goal) {

        if (!goal.autoSaveEnabled) {
            return;
        }

        long now = System.currentTimeMillis();

        // Safety check
        if (goal.nextAutoSaveDate > now) {
            return;
        }

        double currentAmount = getCurrentAmount(goal.id);
        double remaining = goal.targetAmount - currentAmount;

        // Goal already completed
        if (remaining <= 0) {

            goal.autoSaveEnabled = false;
            goal.isCompleted = true;
            goal.completedOn = now;
            goal.updatedAt = now;

            goalDao.updateGoal(goal);

            return;
        }

        // Don't exceed target
        double amount = Math.min(goal.autoSaveAmount, remaining);

        // Create AUTO_SAVE contribution
        GoalContributionEntity contribution = new GoalContributionEntity();

        contribution.setGoalId(goal.id);
        contribution.setAmount(amount);
        contribution.setType(GoalContributionType.AUTO_SAVE);
        contribution.setDate(now);
        contribution.setNote("Automatic savings");
        contribution.setCreatedAt(now);

        long contributeId = goalDao.insertContribution(contribution);
        if (contributeId > 0) {
            goal.savedAmount = currentAmount + amount;
            goalDao.updateSavedAmount(goal.id, goal.savedAmount, now);
        }

        // Calculate NEXT auto-save date
        goal.nextAutoSaveDate = DateHelper.calculateNextAutoSaveDate(goal, goal.nextAutoSaveDate);

        // Check if target was reached
        double newAmount = getCurrentAmount(goal.id);

        if (newAmount >= goal.targetAmount) {
            goal.isCompleted = true;
            goal.completedOn = now;
            goal.autoSaveEnabled = false;
            goal.nextAutoSaveDate = 0;
        }

        goal.updatedAt = now;
        goalDao.updateGoal(goal);
    }

    public LiveData<Integer> getActiveGoalCount(int accountId) {
        return goalDao.getActiveGoalCount(accountId);
    }

    public List<GoalEntity> getDueAutoSaveGoals(long currentTime) {
        return goalDao.getDueAutoSaveGoals(currentTime);
    }

    public Long getEarliestAutoSaveDate() {
        return goalDao.getEarliestAutoSaveDate();
    }

    public GoalWithDetails fetchGoalDetails(int goalId) {
        return goalDao.fetchGoalDetails(goalId);
    }

    // --------------------------------------------------
    // UPDATE GOAL
    // --------------------------------------------------

    public int updateGoal(GoalEntity goal, Context context) {

        long now = System.currentTimeMillis();

        // ---------------------------------------------
        // Get existing goal
        // ---------------------------------------------

        GoalEntity existingGoal = goalDao.getGoal(goal.id);

        if (existingGoal == null) {
            return 0;
        }

        // ---------------------------------------------
        // Preserve existing goal state
        // ---------------------------------------------

        goal.savedAmount = existingGoal.savedAmount;
        goal.startedDate = existingGoal.startedDate;
        goal.createdAt = existingGoal.createdAt;
        goal.isCompleted = existingGoal.isCompleted;
        goal.completedOn = existingGoal.completedOn;
        goal.isArchived = existingGoal.isArchived;
        goal.archivedOn = existingGoal.archivedOn;
        goal.isDeleted = existingGoal.isDeleted;
        goal.isSynced = false;
        goal.updatedAt = now;

        // ---------------------------------------------
        // INITIAL AMOUNT
        // ---------------------------------------------

        double oldInitialAmount = existingGoal.initialAmount;
        double newInitialAmount = goal.initialAmount;
        double initialDifference = newInitialAmount - oldInitialAmount;

        goal.savedAmount = existingGoal.savedAmount + initialDifference;

        // Prevent negative saved amount
        if (goal.savedAmount < 0) {
            goal.savedAmount = 0;
        }

        // ---------------------------------------------
        // AUTO SAVE
        // ---------------------------------------------

        if (goal.autoSaveEnabled) {
            goal.nextAutoSaveDate = DateHelper.calculateNextAutoSaveDate(goal, goal.autoSaveStartDate);
        } else {
            goal.autoSaveAmount = 0;
            goal.autoSaveFrequency = 0;
            goal.autoSaveStartDate = 0;
            goal.autoSaveWeekDay = 0;
            goal.autoSaveDayOfMonth = 0;
            goal.autoSaveMonth = 0;
            goal.autoSaveDay = 0;
            goal.nextAutoSaveDate = 0;
        }

        // ---------------------------------------------
        // UPDATE INITIAL CONTRIBUTION
        // ---------------------------------------------

        GoalContributionEntity initialContribution = goalDao.getInitialContribution(goal.id, GoalContributionType.INITIAL);

        if (initialContribution != null) {
            initialContribution.setAmount(newInitialAmount);
            initialContribution.setUpdatedAt(now);
            goalDao.updateContribution(initialContribution);
        } else if (newInitialAmount > 0) {
            GoalContributionEntity contribution =
                    new GoalContributionEntity(goal.id, newInitialAmount, GoalContributionType.INITIAL, existingGoal.startedDate,
                            context.getString(R.string.initial_savings), now, now, false);
            goalDao.insertContribution(contribution);
        }

        int result = goalDao.updateGoal(goal);

        // ---------------------------------------------
        // RECHECK COMPLETION
        // ---------------------------------------------

        if (result > 0) {
            updateGoalCompletion(goal.id);
        }

        return result;
    }

    public GoalEntity getGoal(int goalId) {
        return goalDao.getGoal(goalId);
    }

    public LiveData<GoalContributionSummary> getContributionSummary(int goalId, int autoSaveType, int manualType, int initialType, int withdrawalType) {
        return goalDao.getContributionSummary(goalId, autoSaveType, manualType, initialType, withdrawalType);
    }

    public LiveData<List<GoalContributionWithCurrency>> getRecentContributions(int goalId) {
        return goalDao.getRecentContributions(goalId);
    }

    public GoalContributionWithCurrency getContribution(int goalId, int contributionId) {
        return goalDao.getContribution(goalId, contributionId);
    }

    public void updateContribution(GoalWithDetails goal, GoalContributionEntity contribution) {
        goalDao.updateSavedAmount(goal.id, goal.savedAmount, System.currentTimeMillis());
        goalDao.updateContribution(contribution);
    }

    public List<GoalContributionWithCurrency> getContributions(int goalId, int page, int pageSize) {
        int offset = page * pageSize;
        return goalDao.getContributions(goalId, pageSize, offset);
    }
}