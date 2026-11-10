package com.nprotech.moneytracker.network;

import com.nprotech.moneytracker.models.request.ChangePasswordRequest;
import com.nprotech.moneytracker.models.request.GoogleAuthRequest;
import com.nprotech.moneytracker.models.request.LoginRequest;
import com.nprotech.moneytracker.models.request.PasswordResetRequest;
import com.nprotech.moneytracker.models.request.ProfileUpdateRequest;
import com.nprotech.moneytracker.models.request.RegisterRequest;
import com.nprotech.moneytracker.models.request.VerifyEmailRequest;
import com.nprotech.moneytracker.models.response.ApiResponse;
import com.nprotech.moneytracker.models.response.ChangePasswordResponse;
import com.nprotech.moneytracker.models.response.DeviceSessionResponse;
import com.nprotech.moneytracker.models.response.LoginResponse;
import com.nprotech.moneytracker.models.response.LogoutResponse;
import com.nprotech.moneytracker.models.response.PasswordResetResponse;
import com.nprotech.moneytracker.models.response.ProfilePhotoUploadResponse;
import com.nprotech.moneytracker.models.response.ProfileUpdateResponse;
import com.nprotech.moneytracker.models.response.RegisterResponse;
import com.nprotech.moneytracker.models.response.VerifyEmailResponse;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface IApiService {

    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest registerRequest);

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("auth/logout")
    Call<LogoutResponse> logout();

    @POST("auth/google")
    Call<LoginResponse> googleAuth(@Body GoogleAuthRequest request);

    @POST("auth/verify-email")
    Call<VerifyEmailResponse> verifyEmail(@Body VerifyEmailRequest request);

    @Multipart
    @POST("auth/profile/photo")
    Call<ProfilePhotoUploadResponse> uploadProfilePhoto(@Part MultipartBody.Part photo);

    @POST("auth/password/set")
    Call<ChangePasswordResponse> setPassword(@Body ChangePasswordRequest request);

    @POST("auth/password/change")
    Call<ChangePasswordResponse> changePassword(@Body ChangePasswordRequest request);

    @PUT("auth/profile")
    Call<ProfileUpdateResponse> updateProfile(@Body ProfileUpdateRequest request);

    @POST("auth/forgot-password")
    Call<PasswordResetResponse> forgotPassword(@Body PasswordResetRequest request);

    @POST("auth/verify-reset-otp")
    Call<PasswordResetResponse> verifyResetOtp(@Body PasswordResetRequest request);

    @POST("auth/reset-password")
    Call<PasswordResetResponse> resetPassword(@Body PasswordResetRequest request);

    @GET("auth/devices")
    Call<DeviceSessionResponse> getDevices();

    @DELETE("auth/devices/{id}")
    Call<ApiResponse> revokeDevice(@Path("id") long deviceId);

    @POST("auth/devices/revoke-others")
    Call<ApiResponse> revokeOtherDevices();
}