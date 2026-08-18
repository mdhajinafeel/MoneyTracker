package com.nprotech.moneytracker.models;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

import com.nprotech.moneytracker.db.entites.GoalContributionEntity;

public class GoalContributionWithCurrency {

    @Embedded
    private GoalContributionEntity contribution;

    @ColumnInfo(name = "currencySymbol")
    private String currencySymbol;

    public GoalContributionEntity getContribution() {
        return contribution;
    }

    public void setContribution(GoalContributionEntity contribution) {
        this.contribution = contribution;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }
}