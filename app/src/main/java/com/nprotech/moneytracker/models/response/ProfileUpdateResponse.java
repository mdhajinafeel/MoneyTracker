package com.nprotech.moneytracker.models.response;

import com.google.gson.annotations.SerializedName;

public class ProfileUpdateResponse {

    private boolean success;
    private String message;
    private Data data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Data getData() {
        return data;
    }

    public static class Data {

        @SerializedName("full_name")
        private String fullName;

        @SerializedName("avatar_url")
        private String avatarUrl;

        public String getFullName() {
            return fullName;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }
    }
}