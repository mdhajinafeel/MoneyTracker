package com.nprotech.moneytracker.repositories;

import androidx.lifecycle.LiveData;

import com.nprotech.moneytracker.db.dao.DebtLoanDao;
import com.nprotech.moneytracker.db.entites.DebtLoanEntity;

import java.util.List;

public class DebtLoanRepository {

    private final DebtLoanDao debtLoanDao;

    public DebtLoanRepository(DebtLoanDao debtLoanDao) {
        this.debtLoanDao = debtLoanDao;
    }

    public long saveDebtLoan(DebtLoanEntity entity) {
        return debtLoanDao.insert(entity);
    }

    public int updateDebtLoan(DebtLoanEntity entity) {
        return debtLoanDao.update(entity);
    }

    public int deleteDebtLoan(DebtLoanEntity entity) {
        return debtLoanDao.delete(entity.id, System.currentTimeMillis());
    }

    public LiveData<DebtLoanEntity> getDebtLoanById(long id) {
        return debtLoanDao.getDebtLoanById(id);
    }

    public LiveData<List<DebtLoanEntity>> getAllDebtLoans() {
        return debtLoanDao.getAllDebtLoans();
    }
}