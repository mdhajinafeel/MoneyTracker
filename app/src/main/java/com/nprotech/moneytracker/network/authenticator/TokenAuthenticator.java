package com.nprotech.moneytracker.network.authenticator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.network.RefreshHttpClient;

import org.json.JSONObject;

import okhttp3.Authenticator;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class TokenAuthenticator implements Authenticator {

    private static final String REFRESH_URL = "https://expenixo.codringreen.com/api/v1/auth/refresh";
    private final OkHttpClient refreshClient;

    public TokenAuthenticator(@RefreshHttpClient OkHttpClient refreshClient) {
        this.refreshClient = refreshClient;
    }

    @Nullable
    @Override
    public Request authenticate(Route route, @NonNull Response response) {

        // Retry at most once.
        if (responseCount(response) >= 2) {
            return null;
        }

        String failedAuthorization = response.request().header("Authorization");

        // Do not refresh requests that were not authenticated.
        if (failedAuthorization == null || !failedAuthorization.startsWith("Bearer ")) {
            return null;
        }

        // Do not refresh a session that has been explicitly revoked.
        try {
            if (response.code() == 401) {
                JSONObject errorJson = new JSONObject(response.peekBody(1024 * 1024).string());

                if ("SESSION_REVOKED".equals(errorJson.optString("code"))) {
                    AppLogger.w(getClass(), "Session has been revoked by the server.");
                    return null;
                }
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "Unable to inspect authentication response.", e);
        }

        synchronized (this) {

            PreferenceManager preferences = PreferenceManager.INSTANCE;

            String currentAccessToken = preferences.getAuthAccessToken();

            if (currentAccessToken.trim().isEmpty()) {
                return null;
            }

            // Another request may already have refreshed the token.
            if (!failedAuthorization.equals("Bearer " + currentAccessToken.trim())) {

                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + currentAccessToken.trim())
                        .build();
            }

            String refreshToken = preferences.getAuthRefreshToken();

            if (refreshToken.trim().isEmpty()) {
                AppLogger.w(getClass(), "Refresh token unavailable.");
                return null;
            }

            String newAccessToken = refreshAccessToken(refreshToken.trim());

            if (newAccessToken == null) {
                AppLogger.w(getClass(), "Token refresh failed.");
                return null;
            }

            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + newAccessToken)
                    .build();
        }
    }

    @Nullable
    private String refreshAccessToken(String refreshToken) {

        try {
            JSONObject json = new JSONObject();
            json.put("refresh_token", refreshToken);

            RequestBody requestBody = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(REFRESH_URL)
                    .post(requestBody)
                    .build();

            try (Response response = refreshClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    AppLogger.w(getClass(), "Refresh HTTP status: " + response.code());
                    return null;
                }

                JSONObject responseJson =
                        new JSONObject(response.body().string());

                if (!responseJson.optBoolean("success")) {
                    AppLogger.w(getClass(), "Refresh rejected by server.");
                    return null;
                }

                JSONObject data = responseJson.optJSONObject("data");

                if (data == null) {
                    AppLogger.w(getClass(), "Refresh response data is missing.");
                    return null;
                }

                String newAccessToken = data.optString("access_token", "");
                String newRefreshToken = data.optString("refresh_token", "");

                if (newAccessToken.trim().isEmpty() || newRefreshToken.trim().isEmpty()) {
                    AppLogger.w(getClass(), "New tokens missing from refresh response.");
                    return null;
                }

                // Save the rotated tokens.
                String accessTokenExpiry = data.optString("access_token_expires_at", "");
                String refreshTokenExpiry = data.optString("refresh_token_expires_at", "");
                PreferenceManager.INSTANCE.updateAuthTokens(newAccessToken, accessTokenExpiry, newRefreshToken, refreshTokenExpiry);
                return newAccessToken;
            }

        } catch (Exception e) {
            AppLogger.e(getClass(), "Token refresh error", e);
            return null;
        }
    }

    private int responseCount(Response response) {
        int count = 1;

        while ((response = response.priorResponse()) != null) {
            count++;
        }

        return count;
    }
}