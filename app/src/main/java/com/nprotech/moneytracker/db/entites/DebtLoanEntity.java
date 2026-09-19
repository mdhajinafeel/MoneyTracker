package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "debt_loans")
public class DebtLoanEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    // =========================================================
    // BASIC
    // =========================================================
    public int type;                 // Borrow / Lent
    public String name;
    public String title;
    public int icon;
    public int color;
    public long walletId;
    public String currencyCode;
    public String currencySymbol;
    public double principalAmount;
    public String notes;

    // =========================================================
    // DATES
    // =========================================================
    public long startDate;
    public Long dueDate;
    public Long firstPaymentDate;

    // =========================================================
    // INTEREST
    // =========================================================
    public int interestType;
    public double interestRate;
    public double interestAmount;
    public int interestPeriod;
    public int interestCalculationMethod;
    public int compoundFrequency;
    public int interestDuration;
    public int customInterestDuration;
    public int customInterestDurationPeriod;

    // =========================================================
    // REPAYMENT
    // =========================================================
    public int repaymentMethod;
    public int repaymentFrequency;
    public int noOfInstallments;
    public double installmentAmount;

    // =========================================================
    // TOTALS
    // =========================================================
    public double totalInterest;
    public double totalAmount;
    public double paidAmount;
    public double remainingAmount;

    // =========================================================
    // STATUS
    // =========================================================
    public boolean isSynced = false;
    public boolean isDeleted = false;

    // =========================================================
    // TIMESTAMPS
    // =========================================================
    public long createdAt;
    public long updatedAt;

    // =========================================================
    // IDS
    // =========================================================
    public String tempDebtLoanServerId;
    public int debtLoanId = 0;
}