package com.nprotech.moneytracker.models.request;

public class ChangePasswordRequest {

    private final String current_password;
    private final String new_password;
    private final String confirm_password;

    public ChangePasswordRequest(String currentPassword, String newPassword,
                                 String confirmPassword) {
        this.current_password = currentPassword;
        this.new_password = newPassword;
        this.confirm_password = confirmPassword;
    }
}