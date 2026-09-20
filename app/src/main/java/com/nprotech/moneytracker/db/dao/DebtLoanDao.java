package com.nprotech.moneytracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.nprotech.moneytracker.db.entites.DebtLoanEntity;

import java.util.List;

@Dao
public interface DebtLoanDao {

    @Insert
    long insert(DebtLoanEntity entity);

    @Update
    int update(DebtLoanEntity entity);

    @Query("SELECT * FROM debt_loans WHERE id = :id LIMIT 1")
    LiveData<DebtLoanEntity> getDebtLoanById(long id);

    @Query("SELECT * FROM debt_loans WHERE type = :type AND isDeleted = 0 ORDER BY createdAt DESC")
    LiveData<List<DebtLoanEntity>> getAllDebtLoans(int type);

    @Query("UPDATE debt_loans SET updatedAt = :updatedAt, isDeleted = 1 WHERE id = :debtLoanId AND isDeleted = 0")
    int delete(int debtLoanId, long updatedAt);
}