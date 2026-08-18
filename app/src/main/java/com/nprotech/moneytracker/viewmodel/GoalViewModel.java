package com.nprotech.moneytracker.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.GoalEntity;
import com.nprotech.moneytracker.helper.GoalWorkManagerHelper;
import com.nprotech.moneytracker.models.GoalContributionSummary;
import com.nprotech.moneytracker.models.GoalContributionWithCurrency;
import com.nprotech.moneytracker.models.GoalWithDetails;
import com.nprotech.moneytracker.repositories.GoalRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class GoalViewModel extends ViewModel {

    private final GoalRepository goalRepository;
    private final MutableLiveData<Integer> accountId = new MutableLiveData<>();
    private final LiveData<Integer> goalCount;
    private boolean loading = false, hasMore = true;
    private static final int PAGE_SIZE = 100;
    private int currentPage = 0, goalId;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<GoalContributionWithCurrency>> goalContributionList = new MutableLiveData<>(new ArrayList<>());

    @Inject
    public GoalViewModel(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
        goalCount = Transformations.switchMap(accountId, goalRepository::getActiveGoalCount);
    }

    public void selectAccount(int id) {
        accountId.setValue(id);
    }

    public long createGoal(GoalEntity goal, double initialAmount, Context context) {
        long goalId = goalRepository.createGoal(goal, initialAmount, context);

        if (goalId > 0) {
            Long nextRunDate = goalRepository.getEarliestAutoSaveDate();

            if (nextRunDate != null) {
                long delay = nextRunDate - System.currentTimeMillis();

                if (delay < 0) {
                    delay = 0;
                }

                GoalWorkManagerHelper.scheduleAutoSave(context, delay);
            }
        }

        return goalId;
    }

    public LiveData<List<GoalWithDetails>> getGoals(int accountId, boolean isArchived, boolean isCompleted) {
        return goalRepository.getGoals(accountId, isArchived, isCompleted);
    }

    public LiveData<List<GoalWithDetails>> getArchivedGoals(int accountId) {
        return goalRepository.getArchivedGoals(accountId);
    }

    public GoalWithDetails fetchGoalDetails(int goalId) {
        return goalRepository.fetchGoalDetails(goalId);
    }

    public LiveData<GoalWithDetails> getGoalDetailById(int goalId) {
        return goalRepository.getGoalDetailById(goalId);
    }

    public boolean deleteGoal(int goalId) {
        return goalRepository.deleteGoal(goalId);
    }

    public boolean disableAutoSave(int goalId) {
        return goalRepository.disableAutoSave(goalId);
    }

    public boolean archiveRestoreGoal(int goalId, boolean isArchive) {
        return goalRepository.archiveRestoreGoal(goalId, isArchive);
    }

    public boolean markAsCompletedGoal(int goalId) {
        return goalRepository.markAsCompletedGoal(goalId);
    }

    public boolean markAsInProgressGoal(int goalId) {
        return goalRepository.markAsInProgressGoal(goalId);
    }

    public void addMoney(GoalWithDetails goal) {
        goalRepository.addMoney(goal.id, goal.goalAmount, goal.moneyDate, goal.notes, goal.savedAmount);
    }

    public void withdrawMoney(GoalWithDetails goal) {
        goalRepository.withdrawMoney(goal.id, goal.goalAmount, goal.moneyDate, goal.notes, goal.savedAmount);
    }

    public double getCurrentAmount(int goalId) {
        return goalRepository.getCurrentAmount(goalId);
    }

    public LiveData<Integer> goalCount() {
        return goalCount;
    }

    public int updateGoal(GoalEntity goal, Context context) {
        return goalRepository.updateGoal(goal, context);
    }

    public GoalEntity getGoal(int goalId) {
        return goalRepository.getGoal(goalId);
    }

    public LiveData<GoalContributionSummary> getContributionSummary(int goalId, int autoSaveType, int manualType, int initialType, int withdrawalType) {
        return goalRepository.getContributionSummary(goalId, autoSaveType, manualType, initialType, withdrawalType);
    }

    public LiveData<List<GoalContributionWithCurrency>> getRecentContributions(int goalId) {
        return goalRepository.getRecentContributions(goalId);
    }

    public void loadNextPage() {

        if (loading || !hasMore)
            return;

        loading = true;

        executor.execute(() -> {
            List<GoalContributionWithCurrency> page = goalRepository.getContributions(goalId, currentPage, PAGE_SIZE);

            if (page.size() < PAGE_SIZE) {
                hasMore = false;
            }

            goalContributionList.postValue(page);
            currentPage++;
            loading = false;
        });
    }

    public void loadGoalContributions(int goalId) {
        this.goalId = goalId;
        currentPage = 0;
        hasMore = true;
        goalContributionList.setValue(new ArrayList<>());
        loadNextPage();
    }

    public LiveData<List<GoalContributionWithCurrency>> getGoalContributionList() {
        return goalContributionList;
    }
}