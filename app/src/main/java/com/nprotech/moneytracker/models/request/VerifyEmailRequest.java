package com.nprotech.moneytracker.models.request;

import com.google.gson.annotations.SerializedName;

public class VerifyEmailRequest {

    @SerializedName("email")
    private final String email;

    @SerializedName("code")
    private final String code;

    public VerifyEmailRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }
}