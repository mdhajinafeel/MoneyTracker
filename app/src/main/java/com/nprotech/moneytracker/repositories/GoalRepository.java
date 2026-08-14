package com.nprotech.moneytracker.repositories;

import androidx.lifecycle.LiveData;
import androidx.room.Transaction;

import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.db.dao.AccountDao;
import com.nprotech.moneytracker.db.dao.GoalDao;
import com.nprotech.moneytracker.db.dao.TransactionDao;
import com.nprotech.moneytracker.db.dao.WalletDao;
import com.nprotech.moneytracker.db.entites.GoalEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.models.GoalWithDetails;

import java.util.Calendar;
import java.util.List;

public class GoalRepository {

    private final GoalDao goalDao;
    private final WalletDao walletDao;
    private final TransactionDao transactionDao;
    private final AccountDao accountDao;

    public GoalRepository(GoalDao goalDao, WalletDao walletDao, TransactionDao transactionDao, AccountDao accountDao) {
        this.goalDao = goalDao;
        this.walletDao = walletDao;
        this.transactionDao = transactionDao;
        this.accountDao = accountDao;
    }

    public long insertGoal(GoalEntity goal) {
        return goalDao.insertGoal(goal);
    }

    //==============================================================
    // SAVE MONEY TO GOAL
    // Manual Save + Auto Save
    //==============================================================
    @Transaction
    public void saveGoalMoney(int goalId, int walletId, double amount, boolean autoSave) {

        try {

            // -------------------------------------------------------
            // VALIDATION
            // -------------------------------------------------------

            if (amount <= 0) {
                throw new IllegalArgumentException("Goal save amount must be greater than zero");
            }

            // -------------------------------------------------------
            // GET GOAL
            // -------------------------------------------------------

            GoalEntity goal = goalDao.getGoalById(goalId);

            if (goal == null) {
                throw new IllegalStateException("Goal not found: " + goalId);
            }

            // -------------------------------------------------------
            // GET WALLET
            // -------------------------------------------------------

            WalletEntity wallet = walletDao.getWalletByWalletId(walletId);
            if (wallet == null) {
                throw new IllegalStateException("Wallet not found: " + walletId);
            }

            // -------------------------------------------------------
            // CHECK WALLET BALANCE
            // -------------------------------------------------------

            if (wallet.amount < amount) {
                throw new InsufficientWalletBalanceException("Insufficient wallet balance");
            }

            // -------------------------------------------------------
            // CREATE GOAL TRANSACTION
            // -------------------------------------------------------

            TransactionEntity transaction = new TransactionEntity();
            transaction.type = TransactionEntity.TYPE_GOAL;
            transaction.goalId = goalId;
            transaction.walletId = walletId;
            transaction.amount = amount;
            transaction.transactionDate = System.currentTimeMillis();

            if (autoSave) {
                transaction.description = "Auto Save - " + goal.name;
            } else {
                transaction.description = "Goal - " + goal.name;
            }

            transactionDao.insert(transaction);

            // -------------------------------------------------------
            // UPDATE WALLET BALANCE
            // -------------------------------------------------------

            wallet.amount = wallet.amount - amount;

            walletDao.updateWallet(wallet);

            // -------------------------------------------------------
            // UPDATE GOAL SAVED AMOUNT
            // -------------------------------------------------------

            goal.savedAmount = goal.savedAmount + amount;

            // -------------------------------------------------------
            // UPDATE NEXT AUTO SAVE DATE
            // -------------------------------------------------------

            if (autoSave && goal.autoSaveEnabled) {
                goal.nextAutoSaveDate = calculateNextAutoSaveDate(goal);
            }

            // -------------------------------------------------------
            // UPDATE GOAL
            // -------------------------------------------------------

            goalDao.updateGoal(goal);

        } catch (Exception e) {
            AppLogger.e(getClass(), "saveGoalMoney", e);
            throw e;
        }
    }


    //==============================================================
    // CALCULATE NEXT AUTO SAVE DATE
    //==============================================================

    private long calculateNextAutoSaveDate(GoalEntity goal) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(goal.nextAutoSaveDate);

        switch (goal.autoSaveFrequency) {

            //======================================================
            // DAILY
            //======================================================

            case Constants.GOAL_FREQUENCY_DAILY:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;

            //======================================================
            // WEEKLY
            //======================================================
            case Constants.GOAL_FREQUENCY_WEEKLY:
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                break;

            //======================================================
            // MONTHLY
            //======================================================
            case Constants.GOAL_FREQUENCY_MONTHLY:
                int dayOfMonth = goal.autoSaveDayOfMonth;
                calendar.add(Calendar.MONTH, 1);
                int maximumDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, Math.min(dayOfMonth, maximumDay));
                break;

            //======================================================
            // YEARLY
            //======================================================
            case Constants.GOAL_FREQUENCY_YEARLY:
                int month = goal.autoSaveMonth;
                int day = goal.autoSaveDay;
                calendar.add(Calendar.YEAR, 1);
                calendar.set(Calendar.MONTH, month);
                int maximumDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, Math.min(day, maximumDayOfMonth));
                break;

            default:
                throw new IllegalArgumentException("Unknown goal auto save frequency: " + goal.autoSaveFrequency);
        }

        return calendar.getTimeInMillis();
    }


    //==============================================================
    // INSUFFICIENT BALANCE EXCEPTION
    //==============================================================

    public static class InsufficientWalletBalanceException extends IllegalStateException {
        public InsufficientWalletBalanceException(String message) {
            super(message);
        }
    }

    public List<GoalEntity> getDueAutoSaveGoals() {
        return goalDao.getDueAutoSaveGoals(System.currentTimeMillis());
    }

    public void skipAutoSave(GoalEntity goal) {
        goal.nextAutoSaveDate = calculateNextAutoSaveDate(goal);
        goalDao.updateGoal(goal);
    }

    public LiveData<List<GoalWithDetails>> getGoals(boolean isCompleted, int accountId) {
        return goalDao.getGoals(isCompleted, accountId);
    }

    public LiveData<GoalWithDetails> getGoalDetailById(int goalId) {
        return goalDao.getGoalDetailById(goalId);
    }

    public void addMoneyToGoal(GoalWithDetails goal, WalletEntity wallet, double amount) {

        long now = System.currentTimeMillis();

        double newGoalAmount = goal.savedAmount + amount;
        double newWalletAmount = wallet.amount - amount;

        // Goal balance
        goalDao.updateSavedAmount(goal.id, newGoalAmount, now);

        // Wallet balance
        walletDao.updateWalletById(wallet.id, newWalletAmount);

        // Account balance
        accountDao.updateAccountById(wallet.accountId, -amount);

        // Transaction
        TransactionEntity transaction = new TransactionEntity();
        transaction.type = TransactionEntity.TYPE_EXPENSE;
        transaction.amount = amount;
        transaction.walletId = wallet.id;
        transaction.tempTransactionServerId = "T_" + now;
        transaction.accountId = goal.accountId;
        transaction.memo = goal.notes;
        transaction.description = goal.description;
        transaction.transactionDate = goal.moneyDate;
        transaction.createdAt = now;
        transaction.updatedAt = now;
        transaction.isSynced = false;
        transaction.isDeleted = false;
        transaction.fee = 0;
        transaction.categoryId = goal.categoryId;
        transaction.defaultCategoryId = 0;
        transaction.goalId = goal.id;

        transactionDao.insert(transaction);
    }

    public void withdrawMoneyFromGoal(GoalWithDetails goal, WalletEntity wallet, double amount) {
        long now = System.currentTimeMillis();

        if (goal.savedAmount < amount) {
            return;
        }

        double newGoalAmount = goal.savedAmount - amount;
        double newWalletAmount = wallet.amount + amount;

        // Goal balance
        goalDao.updateSavedAmount(goal.id, newGoalAmount, now);

        // Wallet balance
        walletDao.updateWalletById(wallet.id, newWalletAmount);

        // Account balance
        accountDao.updateAccountById(wallet.accountId, amount);

        // Transaction
        TransactionEntity transaction = new TransactionEntity();
        transaction.type = TransactionEntity.TYPE_INCOME;
        transaction.amount = amount;
        transaction.walletId = wallet.id;
        transaction.tempTransactionServerId = "T_" + now;
        transaction.accountId = goal.accountId;
        transaction.memo = goal.notes;
        transaction.description = goal.description;
        transaction.transactionDate = goal.moneyDate;
        transaction.createdAt = now;
        transaction.updatedAt = now;
        transaction.isSynced = false;
        transaction.isDeleted = false;
        transaction.fee = 0;
        transaction.categoryId = goal.categoryId;
        transaction.defaultCategoryId = 0;
        transaction.goalId = goal.id;

        transactionDao.insert(transaction);
    }

    public boolean deleteGoal(int goalId) {
        return goalDao.deleteGoal(goalId, System.currentTimeMillis()) > 0;
    }
}