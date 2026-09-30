package com.nprotech.moneytracker.models;

import androidx.room.Embedded;

import com.nprotech.moneytracker.db.entites.BudgetEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanEntity;

import java.io.Serializable;

public class DebtLoanWithDetails implements Serializable {

    @Embedded
    public DebtLoanEntity debtLoan;

    public int statusType;
}