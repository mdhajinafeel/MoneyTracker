package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "debt_loans")
public class DebtLoanEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int type;
    public String name;
    public String title;
    public int debtLoanIcon;
    public int debtLoanColor;
    public int walletId;
    public String currencyCode;
    public String currencySymbol;
    public double principalAmount;
    public String notes;
    public long startDate;
    public Long dueDate;
    public Long firstPaymentDate;
    public int interestType;
    public double interestRate;
    public double interestAmount;
    public int interestPeriod;
    public int interestCalculationMethod;
    public int compoundFrequency;
    public int interestDuration;
    public int customInterestDuration;
    public int customInterestDurationPeriod;
    public int repaymentMethod;
    public int repaymentFrequency;
    public int noOfInstallments;
    public double installmentAmount;
    public double totalInterest;
    public double totalAmount;
    public double paidAmount;
    public double remainingAmount;
    public boolean isMoneyReceivedLent = false;
    public boolean reminderEnabled;
    public int reminderDays;
    public int reminderHour;
    public int reminderMinute;
    public boolean isSynced = false;
    public boolean isDeleted = false;
    public long createdAt;
    public long updatedAt;
    public String tempDebtLoanServerId;
    public int debtLoanId = 0;
}