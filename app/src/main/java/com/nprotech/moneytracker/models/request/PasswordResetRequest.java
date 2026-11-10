package com.nprotech.moneytracker.models.request;

import com.google.gson.annotations.SerializedName;

public class PasswordResetRequest {

    @SerializedName("email")
    private final String email;

    @SerializedName("otp")
    private final String otp;

    @SerializedName("reset_token")
    private final String resetToken;

    @SerializedName("new_password")
    private final String newPassword;

    @SerializedName("confirm_password")
    private final String confirmPassword;

    public PasswordResetRequest(
            String email,
            String otp,
            String resetToken,
            String newPassword,
            String confirmPassword
    ) {
        this.email = email;
        this.otp = otp;
        this.resetToken = resetToken;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }
}