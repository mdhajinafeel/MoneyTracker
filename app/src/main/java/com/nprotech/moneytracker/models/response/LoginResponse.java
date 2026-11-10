package com.nprotech.moneytracker.models.response;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

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

        @SerializedName("avatar_url")
        private String avatarUrl;

        @SerializedName("session_id")
        private String sessionId;

        @SerializedName("access_token")
        private String accessToken;

        @SerializedName("access_token_expires_at")
        private String accessTokenExpiresAt;

        @SerializedName("refresh_token")
        private String refreshToken;

        @SerializedName("refresh_token_expires_at")
        private String refreshTokenExpiresAt;

        @SerializedName("created_at")
        private Long createdAt;

        @SerializedName("account_status")
        private String accountStatus;

        @SerializedName("is_google")
        private boolean isGoogle;

        @SerializedName("has_password")
        private boolean hasPassword;

        public String getUserId() {
            return userId;
        }

        public String getFullName() {
            return fullName;
        }

        public String getEmail() {
            return email;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getAccessTokenExpiresAt() {
            return accessTokenExpiresAt;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public String getRefreshTokenExpiresAt() {
            return refreshTokenExpiresAt;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public Long getCreatedAt() {
            return createdAt;
        }

        public String getAccountStatus() {
            return accountStatus;
        }

        public boolean isGoogle() {
            return isGoogle;
        }

        public boolean isHasPassword() {
            return hasPassword;
        }
    }
}