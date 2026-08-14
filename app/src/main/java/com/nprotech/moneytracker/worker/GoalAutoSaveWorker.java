package com.nprotech.moneytracker.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nprotech.moneytracker.db.MoneyTrackerDatabase;
import com.nprotech.moneytracker.db.dao.GoalDao;
import com.nprotech.moneytracker.db.entites.GoalEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.repositories.GoalRepository;

import java.util.List;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class GoalAutoSaveWorker extends Worker {

    private final GoalRepository goalRepository;

    @AssistedInject
    public GoalAutoSaveWorker(@Assisted @NonNull Context context, @Assisted @NonNull WorkerParameters workerParams, GoalRepository goalRepository) {
        super(context, workerParams);
        this.goalRepository = goalRepository;
    }

    @NonNull
    @Override
    public Result doWork() {

        try {
            List<GoalEntity> goals = goalRepository.getDueAutoSaveGoals();
            for (GoalEntity goal : goals) {
                processGoal(goal);
            }
            return Result.success();

        } catch (Exception e) {
            AppLogger.e(getClass(), "doWork", e);
            return Result.retry();
        }
    }

    private void processGoal(GoalEntity goal) {

        try {
            goalRepository.saveGoalMoney(goal.id, goal.autoSaveWalletId, goal.autoSaveAmount, true);
        } catch (GoalRepository.InsufficientWalletBalanceException e) {
            handleAutoSaveFailed(goal);
        } catch (Exception e) {
            AppLogger.e(getClass(), "processGoal", e);
        }
    }

    private void handleAutoSaveFailed(GoalEntity goal) {
        // Don't create transaction.
        // Don't decrease wallet.
        // Don't increase goal.

        // Move to next scheduled occurrence.
        goalRepository.skipAutoSave(goal);
    }
}