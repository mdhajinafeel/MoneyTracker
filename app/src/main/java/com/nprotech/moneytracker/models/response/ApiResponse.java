package com.nprotech.moneytracker.models.response;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}