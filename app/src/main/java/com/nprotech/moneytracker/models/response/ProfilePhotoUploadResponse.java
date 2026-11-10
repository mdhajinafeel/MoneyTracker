package com.nprotech.moneytracker.models.response;

import com.google.gson.annotations.SerializedName;

public class ProfilePhotoUploadResponse {

    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public Data data;

    public static class Data {
        @SerializedName("avatar_url")
        public String avatarUrl;
    }
}