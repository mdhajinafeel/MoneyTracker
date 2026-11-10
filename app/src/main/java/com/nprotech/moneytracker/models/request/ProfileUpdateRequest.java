package com.nprotech.moneytracker.models.request;

import com.google.gson.annotations.SerializedName;

public class ProfileUpdateRequest {

    @SerializedName("full_name")
    private final String fullName;

    public ProfileUpdateRequest(String fullName) {
        this.fullName = fullName;
    }
}