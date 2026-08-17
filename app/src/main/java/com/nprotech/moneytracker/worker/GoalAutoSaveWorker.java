package com.nprotech.moneytracker.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nprotech.moneytracker.db.entites.GoalEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.GoalWorkManagerHelper;
import com.nprotech.moneytracker.repositories.GoalRepository;

import java.util.List;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class GoalAutoSaveWorker extends Worker {

    private final GoalRepository repository;

    @AssistedInject
    public GoalAutoSaveWorker(@Assisted @NonNull Context context, @Assisted @NonNull WorkerParameters params, GoalRepository repository) {
        super(context, params);
        this.repository = repository;
    }

    @NonNull
    @Override
    public Result doWork() {

        try {

            long now = System.currentTimeMillis();

            List<GoalEntity> goals = repository.getDueAutoSaveGoals(now);

            for (GoalEntity goal : goals) {

                try {
                    repository.executeAutoSave(goal);
                } catch (Exception e) {
                    AppLogger.e(getClass(), "executeAutoSave", e);
                }
            }

            scheduleNext();
            return Result.success();
        } catch (Exception e) {
            AppLogger.e(getClass(), "doWork", e);
            return Result.retry();
        }
    }

    private void scheduleNext() {

        Long nextRunDate = repository.getEarliestAutoSaveDate();

        if (nextRunDate == null) {
            return;
        }

        long delay = nextRunDate - System.currentTimeMillis();

        GoalWorkManagerHelper.scheduleAutoSave(getApplicationContext(), Math.max(delay, 0));
    }
}