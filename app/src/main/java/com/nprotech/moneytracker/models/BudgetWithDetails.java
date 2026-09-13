package com.nprotech.moneytracker.models;

import androidx.room.Embedded;

import com.nprotech.moneytracker.db.entites.BudgetEntity;

import java.io.Serializable;

public class BudgetWithDetails implements Serializable {

    @Embedded
    public BudgetEntity budget;

    public double spentAmount;
}