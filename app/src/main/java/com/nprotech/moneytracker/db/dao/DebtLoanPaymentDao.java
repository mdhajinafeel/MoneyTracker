package com.nprotech.moneytracker.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.nprotech.moneytracker.db.entites.DebtLoanPaymentEntity;

import java.util.List;

@Dao
public interface DebtLoanPaymentDao {

    @Insert
    long insert(DebtLoanPaymentEntity payment);

    @Insert
    void insertAll(List<DebtLoanPaymentEntity> payments);

    @Update
    int update(DebtLoanPaymentEntity payment);

    @Query("""
            SELECT * FROM debt_loan_payments
            WHERE debtLoanServerId = :debtLoanServerId
            ORDER BY paymentNumber ASC
            """)
    List<DebtLoanPaymentEntity> getPaymentsByDebtLoanId(int debtLoanServerId);

    @Query("""
            SELECT * FROM debt_loan_payments
            WHERE debtLoanServerId = :debtLoanServerId
            AND status = 0
            ORDER BY paymentNumber ASC
            """)
    List<DebtLoanPaymentEntity> getPendingPayments(int debtLoanServerId);

    @Query("UPDATE debt_loan_payments SET updatedAt = :updatedAt, isDeleted = 1 WHERE debtLoanId = :debtLoanId AND isDeleted = 0")
    void deletePaymentsByDebtLoanId(int debtLoanId, long updatedAt);
}