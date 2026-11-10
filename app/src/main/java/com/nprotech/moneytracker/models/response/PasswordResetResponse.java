package com.nprotech.moneytracker.models.response;

import com.google.gson.annotations.SerializedName;

public class PasswordResetResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Data data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getResetToken() {
        return data != null ? data.getResetToken() : null;
    }

    public static class Data {

        @SerializedName("reset_token")
        private String resetToken;

        public String getResetToken() {
            return resetToken;
        }
    }
}