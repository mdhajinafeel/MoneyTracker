package com.nprotech.moneytracker.models.request;

public record LoginRequest(String email, String password, String device_id, String device_name,
                           String platform, String platform_version) {
}