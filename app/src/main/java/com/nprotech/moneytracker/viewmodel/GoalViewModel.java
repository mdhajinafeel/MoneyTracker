package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.GoalEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.models.GoalWithDetails;
import com.nprotech.moneytracker.repositories.GoalRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class GoalViewModel extends ViewModel {

    private final GoalRepository goalRepository;

    @Inject
    public GoalViewModel(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    public long insertGoal(GoalEntity goal) {
        return goalRepository.insertGoal(goal);
    }

    public void saveGoalMoney(int goalId, int walletId, double amount, boolean autoSave) {
        goalRepository.saveGoalMoney(goalId, walletId, amount, autoSave);
    }

    public LiveData<List<GoalWithDetails>> getGoals(boolean isCompleted, int accountId) {
        return goalRepository.getGoals(isCompleted, accountId);
    }

    public LiveData<GoalWithDetails> getGoalDetailById(int goalId) {
        return goalRepository.getGoalDetailById(goalId);
    }

    public void addMoneyToGoal(GoalWithDetails goal, WalletEntity wallet, double amount) {
        if (goal == null || wallet == null || amount <= 0) {
            return;
        }

        goalRepository.addMoneyToGoal(goal, wallet, amount);
    }

    public void withdrawMoneyFromGoal(GoalWithDetails goal, WalletEntity wallet, double amount) {
        if (goal == null || wallet == null || amount <= 0) {
            return;
        }

        if (goal.savedAmount < amount) {
            return;
        }

        goalRepository.withdrawMoneyFromGoal(goal, wallet, amount);
    }

    public boolean deleteGoal(int goalId) {
        return goalRepository.deleteGoal(goalId);
    }
}