package com.nprotech.moneytracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.nprotech.moneytracker.db.entites.DebtLoanPaymentTransactionEntity;
import com.nprotech.moneytracker.models.DebtLoanTransactionWithDetails;

import java.util.List;

@Dao
public interface DebtLoanPaymentTransactionDao {

    @Insert
    long insert(DebtLoanPaymentTransactionEntity transaction);

    @Insert
    void insertAll(List<DebtLoanPaymentTransactionEntity> transactions);

    @Update
    int update(DebtLoanPaymentTransactionEntity transaction);

    @Query("""
            SELECT *
            FROM debt_loan_payment_transactions
            WHERE debtLoanPaymentId = :debtLoanPaymentId
            AND isDeleted = 0
            ORDER BY paymentDate ASC, id ASC
            """)
    List<DebtLoanPaymentTransactionEntity> getTransactionsByPaymentId(
            int debtLoanPaymentId
    );

    @Query("""
            SELECT *
            FROM debt_loan_payment_transactions
            WHERE debtLoanId = :debtLoanId
            AND isDeleted = 0
            ORDER BY paymentDate ASC, id ASC
            """)
    List<DebtLoanPaymentTransactionEntity> getTransactionsByDebtLoanId(
            int debtLoanId
    );

    @Query("""
            SELECT COALESCE(SUM(convertedAmount), 0)
            FROM debt_loan_payment_transactions
            WHERE debtLoanPaymentId = :debtLoanPaymentId
            AND isDeleted = 0
            """)
    double getTotalPaidForPayment(int debtLoanPaymentId);

    @Query("""
            SELECT COALESCE(SUM(convertedAmount), 0)
            FROM debt_loan_payment_transactions
            WHERE debtLoanId = :debtLoanId
            AND isDeleted = 0
            """)
    double getTotalPaidForDebtLoan(int debtLoanId);

    @Query("""
            UPDATE debt_loan_payment_transactions
            SET isDeleted = 1, updatedAt = :updatedAt
            WHERE id = :debtLoanTransactionId AND debtLoanId = :debtLoanId AND debtLoanPaymentId = :debtLoanPaymentId
            """)
    int softDelete(int debtLoanId, int debtLoanPaymentId, int debtLoanTransactionId, long updatedAt);

    @Query("""
            DELETE FROM debt_loan_payment_transactions
            WHERE debtLoanId = :debtLoanId
            """)
    void deleteByDebtLoanId(int debtLoanId);

    @Query("""
            SELECT *, w.name AS walletName
            FROM debt_loan_payment_transactions lp
            INNER JOIN wallets w ON w.id = lp.walletId
            INNER JOIN debt_loans l ON l.id = lp.debtLoanId
            WHERE lp.debtLoanId = :debtLoanId
            AND lp.isDeleted = 0
            ORDER BY lp.createdAt ASC
            """)
    LiveData<List<DebtLoanTransactionWithDetails>> getPaymentsByDebtLoanId(int debtLoanId);

    @Query("""
            SELECT *, w.name AS walletName
            FROM debt_loan_payment_transactions lp
            INNER JOIN wallets w ON w.id = lp.walletId
            INNER JOIN debt_loans l ON l.id = lp.debtLoanId
            WHERE lp.debtLoanId = :debtLoanId AND lp.debtLoanPaymentId = :debtLoanPaymentId
            AND lp.isDeleted = 0
            ORDER BY lp.createdAt ASC
            """)
    LiveData<List<DebtLoanTransactionWithDetails>> getPaymentsByDebtLoanId(int debtLoanId, int debtLoanPaymentId);

    @Query("""
            SELECT *, w.name AS walletName
            FROM debt_loan_payment_transactions lp
            INNER JOIN wallets w ON w.id = lp.walletId
            INNER JOIN debt_loans l ON l.id = lp.debtLoanId
            WHERE lp.debtLoanId = :debtLoanId AND lp.debtLoanPaymentId = :debtLoanPaymentId AND lp.id = :debtLoanTransactionId
            AND lp.isDeleted = 0
            ORDER BY lp.createdAt ASC
            """)
    LiveData<DebtLoanTransactionWithDetails> getPaymentDetailById(int debtLoanTransactionId, int debtLoanId, int debtLoanPaymentId);

    @Query("""
        SELECT *
        FROM debt_loan_payment_transactions
        WHERE id = :transactionId
        AND debtLoanId = :debtLoanId
        AND isDeleted = 0
        LIMIT 1
        """)
    DebtLoanPaymentTransactionEntity getTransactionById(int transactionId, int debtLoanId);

    @Query("""
        SELECT *
        FROM debt_loan_payment_transactions
        WHERE id = :transactionId
        AND debtLoanId = :debtLoanId
        AND isDeleted = 0
        LIMIT 1
        """)
    LiveData<DebtLoanPaymentTransactionEntity> getTransactionDetailById(int transactionId, int debtLoanId);

    @Query("""
        SELECT *
        FROM debt_loan_payment_transactions
        WHERE id = :transactionId
        AND debtLoanId = :debtLoanId AND debtLoanPaymentId = :debtLoanPaymentId
        AND isDeleted = 0
        LIMIT 1
        """)
    DebtLoanPaymentTransactionEntity getTransactionByIdSync(int transactionId, int debtLoanId, int debtLoanPaymentId);
}