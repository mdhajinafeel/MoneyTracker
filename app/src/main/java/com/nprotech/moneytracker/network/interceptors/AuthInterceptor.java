package com.nprotech.moneytracker.network.interceptors;

import androidx.annotation.NonNull;

import com.nprotech.moneytracker.helper.PreferenceManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        String path = originalRequest.url().encodedPath();

        // Login, registration and password-reset endpoints are public.
        boolean isPublicAuthEndpoint =
                path.endsWith("/auth/login")
                        || path.endsWith("/auth/register")
                        || path.endsWith("/auth/forgot-password")
                        || path.endsWith("/auth/reset-password")
                        || path.endsWith("/auth/verify-email")
                        || path.endsWith("/auth/refresh");

        String accessToken = PreferenceManager.INSTANCE.getAuthAccessToken();

        if (isPublicAuthEndpoint || accessToken.trim().isEmpty()) {
            return chain.proceed(originalRequest);
        }

        Request authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken.trim())
                .build();

        return chain.proceed(authenticatedRequest);
    }
}