package com.nprotech.moneytracker.constants;

public final class DebtLoanType {

    private DebtLoanType() {
    }

    // DEBT & LOAN INTEREST
    public static final int DEBT_NO_INTEREST = 1;
    public static final int DEBT_PERCENTAGE = 2;
    public static final int DEBT_FIXED_AMOUNT = 3;

    // INTEREST PERIOD
    public static final int INTEREST_PERIOD_DAY = 1;
    public static final int INTEREST_PERIOD_WEEK = 2;
    public static final int INTEREST_PERIOD_MONTH = 3;
    public static final int INTEREST_PERIOD_YEAR = 4;

    // INTEREST PERIOD
    public static final int INTEREST_CALC_SI = 1;
    public static final int INTEREST_CALC_FLAT_RATE = 2;
    public static final int INTEREST_CALC_REDUCE_BALANCE = 3;
    public static final int INTEREST_CALC_CI = 4;

    // CI - COMPOUND FREQUENCY
    public static final int INTEREST_CI_COMP_FREQ_DAILY = 1;
    public static final int INTEREST_CI_COMP_FREQ_WEEKLY = 2;
    public static final int INTEREST_CI_COMP_FREQ_MONTHLY = 3;
    public static final int INTEREST_CI_COMP_FREQ_QUARTERLY = 4;
    public static final int INTEREST_CI_COMP_FREQ_HALF_YEARLY = 5;
    public static final int INTEREST_CI_COMP_FREQ_YEARLY = 6;

    // INTEREST DURATION
    public static final int INTEREST_LOAN_PERIOD = 1;
    public static final int INTEREST_CUSTOM_PERIOD = 2;

    // REPAYMENT METHOD
    public static final int REPAYMENT_FLEXIBLE = 1;
    public static final int REPAYMENT_INSTALLMENTS = 2;
    public static final int REPAYMENT_EMI = 3;

    // REPAYMENT FREQUENCY
    public static final int REPAYMENT_FREQ_DAILY = 1;
    public static final int REPAYMENT_FREQ_WEEKLY = 2;
    public static final int REPAYMENT_FREQ_BIWEEKLY = 3;
    public static final int REPAYMENT_FREQ_MONTHLY = 4;
    public static final int REPAYMENT_FREQ_QUARTERLY = 5;
    public static final int REPAYMENT_FREQ_YEARLY = 6;
}