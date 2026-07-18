package com.nprotech.moneytracker.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.nprotech.moneytracker.db.entites.TransactionEntity;

import java.io.Serializable;

public class TransactionWithDetails implements Serializable {

    @Embedded
    public TransactionEntity transaction;

    @Relation(
            parentColumn = "tempTransactionServerId",
            entityColumn = "parentTransactionId"
    )
    public TransactionEntity feeTransaction;

    public String currencySymbol, color, categoryName, walletName, fromWalletName;
    public Integer icon;
}