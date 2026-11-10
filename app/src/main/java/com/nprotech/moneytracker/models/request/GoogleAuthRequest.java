package com.nprotech.moneytracker.models.request;

import com.google.gson.annotations.SerializedName;

public record GoogleAuthRequest(@SerializedName("id_token") String idToken) {

    @Override
    public String idToken() {
        return idToken;
    }
}