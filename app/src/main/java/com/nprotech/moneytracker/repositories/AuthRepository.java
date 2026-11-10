package com.nprotech.moneytracker.repositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.PreferenceManager;
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
import com.nprotech.moneytracker.models.response.ProfileUpdateResponse;
import com.nprotech.moneytracker.models.response.RegisterResponse;
import com.nprotech.moneytracker.models.response.VerifyEmailResponse;
import com.nprotech.moneytracker.network.IApiService;

import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final IApiService apiService;
    private final Context appContext;

    public AuthRepository(@ApplicationContext Context context, IApiService apiService) {
        this.apiService = apiService;
        this.appContext = context;
    }

    public Call<LoginResponse> login(String email, String password, String device_id, String device_name,
                                     String platform, String platform_version) {
        return apiService.login(new LoginRequest(email, password, device_id, device_name, platform, platform_version));
    }

    public Call<RegisterResponse> register(String fullName, String email, String password) {
        return apiService.register(new RegisterRequest(fullName, email, password));
    }

    public Call<LogoutResponse> logout() {
        return apiService.logout();
    }

    public boolean saveLoginSession(Response<LoginResponse> response) {
        if (response == null || !response.isSuccessful()) {
            return false;
        }

        LoginResponse authResponse = response.body();
        if (authResponse == null || !authResponse.isSuccess() || authResponse.getData() == null) {
            return false;
        }

        LoginResponse.AuthData data = authResponse.getData();
        if (isEmpty(data.getAccessToken()) || isEmpty(data.getRefreshToken()) || isEmpty(data.getUserId())) {
            return false;
        }

        PreferenceManager.INSTANCE.saveAuthSession(data.getUserId(), data.getFullName(), data.getEmail(), data.getAvatarUrl(),
                data.getSessionId(), data.getAccessToken(), data.getAccessTokenExpiresAt(),
                data.getRefreshToken(), data.getRefreshTokenExpiresAt(), data.getAccountStatus(), data.getCreatedAt(), data.isGoogle(), data.isHasPassword());
        return true;
    }

    public Call<LoginResponse> googleAuth(String idToken) {
        return apiService.googleAuth(new GoogleAuthRequest(idToken));
    }

    public Call<VerifyEmailResponse> verifyEmail(String email, String otp) {
        return apiService.verifyEmail(new VerifyEmailRequest(email, otp));
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public Call<ChangePasswordResponse> setPassword(String newPassword, String confirmPassword) {
        return apiService.setPassword(new ChangePasswordRequest(null, newPassword, confirmPassword));
    }

    public Call<ChangePasswordResponse> changePassword(String currentPassword, String newPassword, String confirmPassword) {
        return apiService.changePassword(new ChangePasswordRequest(currentPassword, newPassword, confirmPassword));
    }

    public Call<ProfileUpdateResponse> updateProfile(String fullName) {
        return apiService.updateProfile(new ProfileUpdateRequest(fullName));
    }

    public Call<PasswordResetResponse> forgotPassword(String email) {
        PasswordResetRequest request = new PasswordResetRequest(email, null, null, null, null);
        return apiService.forgotPassword(request);
    }

    public Call<PasswordResetResponse> verifyResetOtp(String email, String otp) {
        PasswordResetRequest request = new PasswordResetRequest(email, otp, null, null, null);
        return apiService.verifyResetOtp(request);
    }

    public Call<PasswordResetResponse> resetPassword(String email, String resetToken, String newPassword, String confirmPassword) {
        PasswordResetRequest request = new PasswordResetRequest(email, null, resetToken, newPassword, confirmPassword);
        return apiService.resetPassword(request);
    }

    public Call<DeviceSessionResponse> getDevices() {
        return apiService.getDevices();
    }

    public LiveData<ApiResponse> revokeDevice(long deviceId) {
        MutableLiveData<ApiResponse> result = new MutableLiveData<>();

        apiService.revokeDevice(deviceId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(
                    Call<ApiResponse> call,
                    Response<ApiResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(response.body());
                } else {
                    result.postValue(new ApiResponse(
                            false,
                            "Unable to revoke device."
                    ));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                result.postValue(new ApiResponse(
                        false,
                        "Network error. Please try again."
                ));
            }
        });

        return result;
    }

    public LiveData<ApiResponse> revokeOtherDevices() {
        MutableLiveData<ApiResponse> result = new MutableLiveData<>();

        apiService.revokeOtherDevices().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(response.body());
                } else {
                    result.postValue(new ApiResponse(false, appContext.getString(R.string.unable_sign_out_other_devices)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                result.postValue(new ApiResponse(false, appContext.getString(R.string.network_error_try_again)));
            }
        });

        return result;
    }
}