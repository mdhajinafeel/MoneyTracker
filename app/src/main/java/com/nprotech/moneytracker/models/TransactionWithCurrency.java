package com.nprotech.moneytracker.models;

import androidx.room.Embedded;

import com.nprotech.moneytracker.db.entites.TransactionEntity;

import java.io.Serializable;

public class TransactionWithCurrency implements Serializable {

    @Embedded
    public TransactionEntity transaction;
    public String currencySymbol, color, categoryName;
    public Integer icon;
}