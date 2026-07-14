package com.nprotech.moneytracker.models;

import java.io.Serializable;

public class TransactionTypeAmountModel implements Serializable {

    private int type;
    private double amount;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}