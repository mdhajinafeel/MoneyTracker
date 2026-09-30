package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "debt_loan_payment_transactions")
public class DebtLoanPaymentTransactionEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int debtLoanId;
    public int debtLoanPaymentId;
    public int paymentMethod;
    public long paymentDate;
    public double amount;
    public int walletId;
    public String currencySymbol;
    public String currencyCode;
    public double convertedAmount;
    public double exchangeRate;
    public String reference;
    public String notes;
    public long createdAt;
    public long updatedAt;
    public boolean isSynced = false;
    public boolean isDeleted = false;
    public String tempDebtLoanTransactionServerId;
    public int debtLoanTransactionId = 0;
}