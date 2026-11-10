package com.nprotech.moneytracker.models.response;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private AuthData data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public AuthData getData() {
        return data;
    }

    public static class AuthData {

        @SerializedName("user_id")
        private String userId;

        @SerializedName("full_name")
        private String fullName;

        @SerializedName("email")
        private String email;

        public String getUserId() {
            return userId;
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmail() {
            return email;
        }
    }
}