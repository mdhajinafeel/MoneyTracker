package com.nprotech.moneytracker.models.response;

import com.google.gson.annotations.SerializedName;

public class ChangePasswordResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("error_code")
    private String errorCode;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}