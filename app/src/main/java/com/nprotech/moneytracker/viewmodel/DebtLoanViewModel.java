package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.DebtLoanEntity;
import com.nprotech.moneytracker.repositories.DebtLoanRepository;

import java.util.List;

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;

@HiltViewModel
public class DebtLoanViewModel extends ViewModel {

    private final DebtLoanRepository repository;

    @Inject
    public DebtLoanViewModel(DebtLoanRepository repository) {
        this.repository = repository;
    }

    public long saveDebtLoan(DebtLoanEntity entity) {
        return repository.saveDebtLoan(entity);
    }

    public int updateDebtLoan(DebtLoanEntity entity) {
        return repository.updateDebtLoan(entity);
    }

    public int deleteDebtLoan(DebtLoanEntity entity) {
        return repository.deleteDebtLoan(entity);
    }

    public LiveData<DebtLoanEntity> getDebtLoanById(long id) {
        return repository.getDebtLoanById(id);
    }

    public LiveData<List<DebtLoanEntity>> getAllDebtLoans() {
        return repository.getAllDebtLoans();
    }
}