package com.nprotech.moneytracker.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nprotech.moneytracker.db.entites.DebtLoanTransactionAttachmentEntity;

import java.util.List;

@Dao
public interface DebtLoanTransactionAttachmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<DebtLoanTransactionAttachmentEntity> transactionAttachmentEntities);

    @Query("SELECT * FROM debt_loan_transactions_attachment WHERE debtLoanId = :debtLoanId AND debtLoanPaymentId = :debtLoanPaymentId " +
            "AND debtLoanPaymentTransactionId = :debtLoanTransactionId")
    List<DebtLoanTransactionAttachmentEntity> getAttachments(int debtLoanId, int debtLoanPaymentId, int debtLoanTransactionId);

    @Query("""
        DELETE FROM debt_loan_transactions_attachment
        WHERE attachmentPath = :attachmentPath
        AND tempDebtLoanServerId = :tempDebtLoanServerId
        AND debtLoanId = :debtLoanId
        AND debtLoanPaymentId = :debtLoanPaymentId
        AND debtLoanPaymentTransactionId = :debtLoanPaymentTransactionId
        """)
    void deleteAttachment(String attachmentPath, String tempDebtLoanServerId, int debtLoanId, int debtLoanPaymentId, int debtLoanPaymentTransactionId);

    @Query("""
        DELETE FROM debt_loan_transactions_attachment
        WHERE debtLoanId = :debtLoanId
        AND debtLoanPaymentId = :debtLoanPaymentId
        AND debtLoanPaymentTransactionId = :debtLoanPaymentTransactionId
        """)
    void deleteByTransaction(int debtLoanId, int debtLoanPaymentId, int debtLoanPaymentTransactionId);

    @Query("""
        DELETE FROM debt_loan_transactions_attachment
        WHERE debtLoanId = :debtLoanId
        """)
    void deleteByDebtLoanId(int debtLoanId);

    @Query("""
        SELECT *
        FROM debt_loan_transactions_attachment
        WHERE debtLoanId = :debtLoanId
        """)
    List<DebtLoanTransactionAttachmentEntity> getAttachmentsByDebtLoanId(int debtLoanId);
}