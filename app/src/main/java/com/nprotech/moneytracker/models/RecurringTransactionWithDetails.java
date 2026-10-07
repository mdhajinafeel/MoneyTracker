package com.nprotech.moneytracker.models;

import androidx.room.Embedded;

import com.nprotech.moneytracker.db.entites.RecurringTransactionEntity;

public class RecurringTransactionWithDetails {

    @Embedded
    public RecurringTransactionEntity recurringTransaction;
    public String currencySymbol, color, categoryName, walletName, fromWalletName;
    public double exchangeRate;
    public Integer icon;
}