package com.nprotech.moneytracker.models;

import java.util.Date;

public class ReducingBalancePaymentModel {

    public int paymentNumber;
    public Date paymentDate;
    public double openingBalance,paymentAmount,interestAmount, principalAmount, closingBalance;
}