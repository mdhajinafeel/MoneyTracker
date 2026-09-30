package com.nprotech.moneytracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.nprotech.moneytracker.db.entites.DebtLoanPaymentEntity;
import com.nprotech.moneytracker.models.DebtLoanPaymentWithDetails;

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
            SELECT *, l.currencySymbol FROM debt_loan_payments lp
            INNER JOIN debt_loans l ON l.id = lp.debtLoanId
            WHERE lp.debtLoanId = :debtLoanId
            AND lp.isDeleted = 0
            ORDER BY lp.createdAt ASC
            """)
    LiveData<List<DebtLoanPaymentWithDetails>> getPaymentsByDebtLoanId(int debtLoanId);

    @Query("""
            SELECT * FROM debt_loan_payments
            WHERE debtLoanId = :debtLoanId
            AND status = 0
            AND isDeleted = 0
            ORDER BY paymentNumber ASC
            """)
    List<DebtLoanPaymentEntity> getPendingPayments(int debtLoanId);

    @Query("""
            SELECT *, l.currencySymbol FROM debt_loan_payments lp
            INNER JOIN debt_loans l ON l.id = lp.debtLoanId
            WHERE lp.debtLoanId = :debtLoanId AND lp.id = :debtLoanPaymentId
            AND lp.isDeleted = 0
            ORDER BY lp.createdAt ASC
            """)
    LiveData<DebtLoanPaymentWithDetails> getPaymentDetailByDebtLoanId(int debtLoanId, int debtLoanPaymentId);

    @Query("UPDATE debt_loan_payments SET updatedAt = :updatedAt, isDeleted = 1 WHERE debtLoanId = :debtLoanId AND isDeleted = 0")
    void deletePaymentsByDebtLoanId(int debtLoanId, long updatedAt);

    @Query("""
            SELECT COUNT(*)
            FROM debt_loan_payments
            WHERE debtLoanId = :debtLoanId
            AND isDeleted = 0
            """)
    int getTotalInstallments(int debtLoanId);

    @Query("""
            SELECT *
            FROM debt_loan_payments
            WHERE debtLoanId = :debtLoanId
            AND status = :status
            AND isDeleted = 0
            ORDER BY paymentNumber ASC
            LIMIT 1
            """)
    DebtLoanPaymentEntity getNextPendingPayment(int debtLoanId, int status);

    @Query("""
        SELECT * FROM debt_loan_payments
        WHERE debtLoanId = :debtLoanId
        AND isDeleted = 0
        ORDER BY paymentNumber ASC
        """)
    List<DebtLoanPaymentEntity> getPaymentsByDebtLoanIdSync(int debtLoanId);

    @Query("""
        SELECT *
        FROM debt_loan_payments
        WHERE id = :paymentId
        AND isDeleted = 0
        LIMIT 1
        """)
    DebtLoanPaymentEntity getPaymentById(int paymentId);

    @Query("""
        SELECT *
        FROM debt_loan_payments
        WHERE id = :paymentId
        LIMIT 1
        """)
    LiveData<DebtLoanPaymentEntity> getPaymentDataById(int paymentId);
}