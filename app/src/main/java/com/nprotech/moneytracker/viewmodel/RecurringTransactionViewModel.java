package com.nprotech.moneytracker.viewmodel;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.RecurringTransactionEntity;
import com.nprotech.moneytracker.models.RecurringTransactionWithDetails;
import com.nprotech.moneytracker.repositories.RecurringTransactionRepository;
import com.nprotech.moneytracker.wrapper.SingleLiveEvent;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;

@HiltViewModel
public class RecurringTransactionViewModel extends ViewModel {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Integer> saveResult = new MutableLiveData<>();
    private final SingleLiveEvent<Boolean> operationResult = new SingleLiveEvent<>();
    private final MutableLiveData<Integer> accountId = new MutableLiveData<>();
    private final LiveData<Integer> recurringCount;

    @Inject
    public RecurringTransactionViewModel(RecurringTransactionRepository recurringTransactionRepository) {
        this.recurringTransactionRepository = recurringTransactionRepository;
        recurringCount = Transformations.switchMap(accountId, recurringTransactionRepository::getActiveRecurringCount);
    }

    public void selectAccount(int id) {
        accountId.setValue(id);
    }

    public LiveData<Integer> getSaveResult() {
        return saveResult;
    }

    public LiveData<Boolean> getOperationResult() {
        return operationResult;
    }

    public void insert(RecurringTransactionEntity recurringTransaction) {
        executorService.execute(() -> {
            try {
                long id = recurringTransactionRepository.insert(recurringTransaction);
                saveResult.postValue((int) id);
            } catch (Exception e) {
                saveResult.postValue(-1);
            }
        });
    }

    public void update(RecurringTransactionEntity recurringTransaction) {
        executorService.execute(() -> {
            try {
                int result = recurringTransactionRepository.update(recurringTransaction);
                operationResult.postValue(result > 0);
            } catch (Exception e) {
                operationResult.postValue(false);
            }
        });
    }

    public void delete(RecurringTransactionEntity recurringTransaction) {
        executorService.execute(() -> {
            try {
                recurringTransactionRepository.delete(recurringTransaction);
                operationResult.postValue(true);
            } catch (Exception e) {
                operationResult.postValue(false);
            }
        });
    }

    public LiveData<List<RecurringTransactionWithDetails>> getRecurringTransactions(int accountId, boolean isPaused, boolean isCompleted) {
        return recurringTransactionRepository.getRecurringTransactions(accountId, isPaused, isCompleted);
    }

    public LiveData<Integer> recurringCount() {
        return recurringCount;
    }

    public boolean deleteRecurring(int recurringId) {
        return recurringTransactionRepository.deleteRecurring(recurringId);
    }

    public boolean activateRecurring(int recurringId, int status) {
        return recurringTransactionRepository.activateRecurring(recurringId, status);
    }

    public RecurringTransactionWithDetails getRecurringTransactionDetail(String tempRecurringServerId) {
        return recurringTransactionRepository.getRecurringTransactionDetail(tempRecurringServerId);
    }

    public LiveData<RecurringTransactionWithDetails> getRecurringTransactionDetails(String tempRecurringServerId) {
        return recurringTransactionRepository.getRecurringTransactionDetails(tempRecurringServerId);
    }

    @SuppressLint("EmptySuperCall")
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}