package com.nprotech.moneytracker.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.repositories.BudgetRepository;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class BudgetRepeatWorker extends Worker {

    private final BudgetRepository budgetRepository;

    @AssistedInject
    public BudgetRepeatWorker(@Assisted @NonNull Context context, @Assisted @NonNull WorkerParameters workerParams, BudgetRepository budgetRepository) {
        super(context, workerParams);
        this.budgetRepository = budgetRepository;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            budgetRepository.processRecurringBudgets();
            return Result.success();
        } catch (Exception e) {
            AppLogger.e(getClass(), "doWork", e);
            return Result.retry();
        }
    }
}