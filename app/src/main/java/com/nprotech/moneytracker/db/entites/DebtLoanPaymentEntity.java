package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "debt_loan_payments")
public class DebtLoanPaymentEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String tempDebtLoanServerId;
    public int debtLoanServerId;
    public int debtLoanId;
    public int paymentNumber;
    public long paymentDate;
    public double principalAmount;
    public double interestAmount;
    public double paymentAmount;
    public int status;
    public double paidAmount;
    public Long paidDate;
    public long createdAt;
    public long updatedAt;
    public boolean isSynced = false;
    public boolean isDeleted = false;
    public String tempDebtLoanPaymentServerId;
    public int debtLoanPaymentId = 0;

    public static final int PAYMENT_PENDING = 0;
    public static final int PAYMENT_PAID = 1;
}