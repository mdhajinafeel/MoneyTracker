package com.nprotech.moneytracker.repositories;

import androidx.lifecycle.LiveData;

import com.nprotech.moneytracker.db.dao.RecurringTransactionDao;
import com.nprotech.moneytracker.db.entites.RecurringTransactionEntity;
import com.nprotech.moneytracker.models.RecurringTransactionWithDetails;

import java.util.List;

public class RecurringTransactionRepository {

    private final RecurringTransactionDao recurringTransactionDao;

    public RecurringTransactionRepository(RecurringTransactionDao recurringTransactionDao) {
        this.recurringTransactionDao = recurringTransactionDao;
    }

    public long insert(RecurringTransactionEntity recurringTransaction) {
        return recurringTransactionDao.insert(recurringTransaction);
    }

    public int update(RecurringTransactionEntity recurringTransaction) {
        return recurringTransactionDao.update(recurringTransaction);
    }

    public void delete(RecurringTransactionEntity recurringTransaction) {
        recurringTransactionDao.delete(recurringTransaction);
    }

    public List<RecurringTransactionEntity> getAll() {
        return recurringTransactionDao.getAll();
    }

    public List<RecurringTransactionEntity> getDueRecurringTransactions(long currentTime) {
        return recurringTransactionDao.getDueRecurringTransactions(currentTime);
    }

    public LiveData<List<RecurringTransactionWithDetails>> getRecurringTransactions(int accountId, boolean isPaused, boolean isCompleted) {
        return recurringTransactionDao.getRecurringTransactions(accountId, isPaused, isCompleted);
    }

    public LiveData<Integer> getActiveRecurringCount(int accountId) {
        return recurringTransactionDao.getActiveRecurringCount(accountId);
    }

    public boolean deleteRecurring(int recurringId) {
        return recurringTransactionDao.deleteRecurring(recurringId, System.currentTimeMillis()) > 0;
    }

    public boolean activateRecurring(int recurringId, int status) {
        return recurringTransactionDao.activateRecurring(recurringId, status, System.currentTimeMillis()) > 0;
    }

    public LiveData<RecurringTransactionWithDetails> getRecurringTransactionDetails(String tempRecurringServerId) {
        return recurringTransactionDao.getRecurringTransactionDetails(tempRecurringServerId);
    }

    public RecurringTransactionWithDetails getRecurringTransactionDetail(String tempRecurringServerId) {
        return recurringTransactionDao.getRecurringTransactionDetail(tempRecurringServerId);
    }

    public List<RecurringTransactionEntity> getUpcomingReminderTransactions(long currentTime, long reminderLimit) {
        return recurringTransactionDao.getUpcomingReminderTransactions(currentTime, reminderLimit);
    }
}