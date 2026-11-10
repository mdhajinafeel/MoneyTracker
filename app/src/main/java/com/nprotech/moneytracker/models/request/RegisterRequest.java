package com.nprotech.moneytracker.models.request;

public record RegisterRequest(String full_name, String email, String password) {
}