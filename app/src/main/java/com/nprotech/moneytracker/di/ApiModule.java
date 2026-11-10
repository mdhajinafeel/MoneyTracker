package com.nprotech.moneytracker.di;

import android.util.Log;

import com.nprotech.moneytracker.BuildConfig;
import com.nprotech.moneytracker.helper.AppSessionManager;
import com.nprotech.moneytracker.network.IApiService;
import com.nprotech.moneytracker.network.RefreshHttpClient;
import com.nprotech.moneytracker.network.authenticator.TokenAuthenticator;
import com.nprotech.moneytracker.network.interceptors.AuthInterceptor;
import com.nprotech.moneytracker.network.interceptors.SessionRevocationInterceptor;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class ApiModule {

    @Provides
    @Singleton
    @RefreshHttpClient
    public OkHttpClient provideRefreshOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    public TokenAuthenticator provideTokenAuthenticator(@RefreshHttpClient OkHttpClient refreshOkHttpClient) {
        return new TokenAuthenticator(refreshOkHttpClient);
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(TokenAuthenticator tokenAuthenticator) {

        HttpLoggingInterceptor loggingInterceptor =
                new HttpLoggingInterceptor(message ->
                        Log.d("API_LOG", message)
                );

        // Show actual Bearer token only in DEBUG builds.
        if (BuildConfig.DEBUG) {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        } else {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor())
                .addInterceptor(loggingInterceptor)
                .authenticator(tokenAuthenticator)
                .addInterceptor(new SessionRevocationInterceptor(AppSessionManager::notifySessionRevoked))
                .build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(
                        GsonConverterFactory.create()
                )
                .build();
    }

    @Provides
    @Singleton
    public static IApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(IApiService.class);
    }
}