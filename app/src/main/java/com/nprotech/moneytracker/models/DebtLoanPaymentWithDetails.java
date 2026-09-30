package com.nprotech.moneytracker.models;

import androidx.room.Embedded;

import com.nprotech.moneytracker.db.entites.DebtLoanPaymentEntity;

import java.io.Serializable;

public class DebtLoanPaymentWithDetails implements Serializable {

    @Embedded
    public DebtLoanPaymentEntity debtLoanPayment;

    public String currencySymbol;
}