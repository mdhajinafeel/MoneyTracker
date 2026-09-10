package com.nprotech.moneytracker.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.repositories.BudgetRepository;

public class BudgetRepeatWorker extends Worker {

    private final BudgetRepository budgetRepository;

    public BudgetRepeatWorker(@NonNull Context context, @NonNull WorkerParameters workerParams, BudgetRepository budgetRepository) {
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