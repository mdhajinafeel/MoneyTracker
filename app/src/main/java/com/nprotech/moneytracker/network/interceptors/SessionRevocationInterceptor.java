package com.nprotech.moneytracker.network.interceptors;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class SessionRevocationInterceptor implements Interceptor {

    public interface Listener {
        void onSessionRevoked();
    }

    private final Listener listener;

    public SessionRevocationInterceptor(Listener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        Response response = chain.proceed(chain.request());

        if (response.code() == 401) {
            try {
                String body = response.peekBody(1024 * 1024).string();
                JSONObject json = new JSONObject(body);
                if ("SESSION_REVOKED".equals(json.optString("code"))) {
                    listener.onSessionRevoked();
                }
            } catch (Exception ignored) {
                // Preserve the original API response.
            }
        }

        return response;
    }
}