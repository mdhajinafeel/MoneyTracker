package com.nprotech.moneytracker.viewmodel;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.DebtLoanEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanPaymentEntity;
import com.nprotech.moneytracker.repositories.DebtLoanRepository;
import com.nprotech.moneytracker.wrapper.SingleLiveEvent;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;

@HiltViewModel
public class DebtLoanViewModel extends ViewModel {

    private final DebtLoanRepository debtLoanRepository;
    private final SingleLiveEvent<Boolean> dataSavedStatus = new SingleLiveEvent<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public DebtLoanViewModel(DebtLoanRepository debtLoanRepository) {
        this.debtLoanRepository = debtLoanRepository;
    }

    public void saveDebtLoan(DebtLoanEntity entity, List<DebtLoanPaymentEntity> payments) {
        executor.execute(() -> {
           long debtLoanId = debtLoanRepository.saveDebtLoan(entity, payments);

           if(debtLoanId >0) {
               dataSavedStatus.postValue(true);
           } else {
               dataSavedStatus.postValue(false);
           }
        });
    }

    public LiveData<Boolean> getDataSavedStatus() {
        return dataSavedStatus;
    }

    public void updateDebtLoan(DebtLoanEntity entity) {
        debtLoanRepository.updateDebtLoan(entity);
    }

    public void deleteDebtLoan(DebtLoanEntity entity) {
        debtLoanRepository.deleteDebtLoan(entity);
    }

    public LiveData<DebtLoanEntity> getDebtLoanById(long id) {
        return debtLoanRepository.getDebtLoanById(id);
    }

    public LiveData<List<DebtLoanEntity>> getAllDebtLoans() {
        return debtLoanRepository.getAllDebtLoans();
    }

    @SuppressLint("EmptySuperCall")
    @Override
    protected void onCleared() {
        executor.shutdown();
        debtLoanRepository.shutdown();
        super.onCleared();
    }
}