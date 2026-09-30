package com.nprotech.moneytracker.models;

import androidx.room.Embedded;

import com.nprotech.moneytracker.db.entites.DebtLoanPaymentTransactionEntity;

import java.io.Serializable;

public class DebtLoanTransactionWithDetails implements Serializable {

    @Embedded
    public DebtLoanPaymentTransactionEntity debtLoanPayment;

    public String walletName;
}