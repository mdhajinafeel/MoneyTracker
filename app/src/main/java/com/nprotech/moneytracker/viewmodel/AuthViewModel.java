package com.nprotech.moneytracker.viewmodel;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppSessionManager;
import com.nprotech.moneytracker.models.response.ApiResponse;
import com.nprotech.moneytracker.models.response.ChangePasswordResponse;
import com.nprotech.moneytracker.models.response.DeviceSessionResponse;
import com.nprotech.moneytracker.models.response.LoginResponse;
import com.nprotech.moneytracker.models.response.LogoutResponse;
import com.nprotech.moneytracker.models.response.PasswordResetResponse;
import com.nprotech.moneytracker.models.response.RegisterResponse;
import com.nprotech.moneytracker.models.response.VerifyEmailResponse;
import com.nprotech.moneytracker.repositories.AuthRepository;
import com.nprotech.moneytracker.utils.DeviceInfo;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import jakarta.inject.Inject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<RegisterResponse> registerResponse = new MutableLiveData<>();
    private final MutableLiveData<LogoutResponse> logoutResult = new MutableLiveData<>();
    private final MutableLiveData<Integer> errorMessageResId = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageText = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<LoginResponse> googleAuthResponse = new MutableLiveData<>();
    private final MutableLiveData<VerifyEmailResponse> verifyEmailResponse = new MutableLiveData<>();
    private final MutableLiveData<LoginResponse> loginResponse = new MutableLiveData<>();
    private Call<RegisterResponse> registerCall;
    private Call<VerifyEmailResponse> verifyEmailCall;
    private Call<LoginResponse> googleAuthCall;
    private Call<LoginResponse> loginCall;
    private final Context context;
    private final MutableLiveData<ChangePasswordResponse> passwordResponse = new MutableLiveData<>();
    private Call<ChangePasswordResponse> passwordCall;
    private Call<LogoutResponse> logoutCall;
    private final MutableLiveData<PasswordResetResponse> passwordResetResponse = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse> revokeResponse = new MutableLiveData<>();
    private final MutableLiveData<ApiResponse> revokeOthersResponse = new MutableLiveData<>();
    private final MutableLiveData<DeviceSessionResponse> devicesResponse = new MutableLiveData<>();
    private Call<DeviceSessionResponse> devicesCall;

    @Inject
    public AuthViewModel(AuthRepository authRepository, @ApplicationContext Context context) {
        this.authRepository = authRepository;
        this.context = context;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<RegisterResponse> getRegisterResponse() {
        return registerResponse;
    }

    public LiveData<Integer> getErrorMessageResId() {
        return errorMessageResId;
    }

    public LiveData<String> getErrorMessageText() {
        return errorMessageText;
    }

    public LiveData<LoginResponse> getGoogleAuthResponse() {
        return googleAuthResponse;
    }

    public LiveData<VerifyEmailResponse> getVerifyEmailResponse() {
        return verifyEmailResponse;
    }

    public LiveData<LoginResponse> getLoginResponse() {
        return loginResponse;
    }

    public LiveData<ChangePasswordResponse> getPasswordResponse() {
        return passwordResponse;
    }

    public LiveData<PasswordResetResponse> getPasswordResetResponse() {
        return passwordResetResponse;
    }

    public LiveData<ApiResponse> getRevokeResponse() {
        return revokeResponse;
    }

    public LiveData<ApiResponse> getRevokeOthersResponse() {
        return revokeOthersResponse;
    }

    public LiveData<DeviceSessionResponse> getDevicesResponse() {
        return devicesResponse;
    }

    public LiveData<LogoutResponse> getLogoutResult() {
        return logoutResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void register(String fullName, String email, String password) {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }

        loading.setValue(true);
        errorMessageResId.setValue(null);

        registerCall = authRepository.register(fullName, email, password);

        registerCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {

                loading.setValue(false);
                RegisterResponse body = response.body();

                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    registerResponse.setValue(body);
                    return;
                }

                @StringRes int messageResId;

                if (body != null && body.getMessage() != null) {
                    errorMessageResId.setValue(null);
                    errorMessageText.setValue(body.getMessage());
                    return;
                } else if (response.code() == 409) {
                    messageResId = R.string.error_account_already_exists;
                } else if (response.code() == 422) {
                    messageResId = R.string.error_check_details;
                } else if (response.code() == 429) {
                    messageResId = R.string.error_too_many_attempts;
                } else {
                    messageResId = R.string.error_account_creation_failed;
                }

                errorMessageResId.setValue(messageResId);
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable throwable) {
                loading.setValue(false);

                if (call.isCanceled()) {
                    return;
                }

                @StringRes int messageResId;
                messageResId = R.string.check_internet_connection;
                errorMessageResId.setValue(messageResId);
            }
        });
    }

    public void logout() {

        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }

        loading.setValue(true);
        errorMessage.setValue(null);
        logoutResult.setValue(null);

        logoutCall = authRepository.logout();

        logoutCall.enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<LogoutResponse> call, @NonNull Response<LogoutResponse> response) {
                loading.setValue(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    logoutResult.setValue(response.body());
                    return;
                }

                String message = context.getString(R.string.unable_log_out);

                try {
                    if (response.errorBody() != null) {
                        LogoutResponse error = new Gson().fromJson(response.errorBody().string(), LogoutResponse.class);
                        if (error != null && error.getMessage() != null) {
                            message = error.getMessage();
                        }
                    } else if (response.body() != null && response.body().getMessage() != null) {
                        message = response.body().getMessage();
                    }
                } catch (Exception ignored) {
                    // Keep fallback message.
                }

                errorMessage.setValue(message);
            }

            @Override
            public void onFailure(@NonNull Call<LogoutResponse> call, @NonNull Throwable t) {
                loading.setValue(false);

                if (call.isCanceled()) {
                    return;
                }

                errorMessage.setValue(context.getString(R.string.check_internet_connection));
            }
        });
    }

    public void authenticateWithGoogle(String idToken) {

        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }

        if (idToken == null || idToken.trim().isEmpty()) {
            errorMessageText.setValue(context.getString(R.string.google_token_missing));
            return;
        }

        loading.setValue(true);
        clearErrors();

        googleAuthCall = authRepository.googleAuth(idToken);

        googleAuthCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                loading.setValue(false);

                LoginResponse body = response.body();

                if (response.isSuccessful() && body != null && body.isSuccess() && authRepository.saveLoginSession(response)) {
                    AppSessionManager.reset();
                    googleAuthResponse.setValue(body);
                    return;
                }

                String message = body != null ? body.getMessage() : null;
                errorMessageText.setValue(message != null && !message.trim().isEmpty() ? message : context.getString(R.string.google_authentication_failed));
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable throwable) {
                loading.setValue(false);
                if (!call.isCanceled()) {
                    errorMessageResId.setValue(R.string.check_internet_connection);
                }
            }
        });
    }

    public void verifyEmail(String email, String otp) {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }

        if (email == null || email.trim().isEmpty() || otp == null || !otp.matches("[0-9]{6}")) {
            errorMessageText.setValue(context.getString(R.string.enter_complete_six_code));
            return;
        }

        loading.setValue(true);
        clearErrors();
        verifyEmailResponse.setValue(null);

        verifyEmailCall = authRepository.verifyEmail(email.trim(), otp);

        verifyEmailCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<VerifyEmailResponse> call, @NonNull Response<VerifyEmailResponse> response) {
                loading.setValue(false);

                VerifyEmailResponse body = response.body();

                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    verifyEmailResponse.setValue(body);
                    return;
                }

                if (body != null && body.getMessage() != null && !body.getMessage().trim().isEmpty()) {
                    errorMessageText.setValue(body.getMessage());
                } else if (response.code() == 429) {
                    errorMessageResId.setValue(R.string.verify_email_too_many_attempts);
                } else {
                    errorMessageResId.setValue(R.string.verify_email_failed);
                }
            }

            @Override
            public void onFailure(@NonNull Call<VerifyEmailResponse> call, @NonNull Throwable throwable) {
                loading.setValue(false);
                if (!call.isCanceled()) {
                    errorMessageResId.setValue(R.string.check_internet_connection);
                }
            }
        });
    }

    public void login(String email, String password) {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }

        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            errorMessageText.setValue(context.getString(R.string.enter_email_address_password));
            return;
        }

        loading.setValue(true);
        clearErrors();
        loginResponse.setValue(null);

        loginCall = authRepository.login(email.trim(), password, DeviceInfo.getDeviceId(context),
                DeviceInfo.getDeviceName(),
                DeviceInfo.getPlatform(),
                DeviceInfo.getPlatformVersion());

        loginCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                loading.setValue(false);

                LoginResponse body = response.body();

                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    if (authRepository.saveLoginSession(response)) {
                        AppSessionManager.reset();
                        loginResponse.setValue(body);
                    } else {
                        errorMessageText.setValue(context.getString(R.string.login_succeed_desc));
                    }

                    return;
                }

                String message = body != null ? body.getMessage() : null;

                if (message != null && !message.trim().isEmpty()) {
                    errorMessageText.setValue(message);
                } else if (response.code() == 401) {
                    errorMessageText.setValue(context.getString(R.string.invalid_email_address_password));
                } else if (response.code() == 403) {
                    errorMessageText.setValue(context.getString(R.string.verify_email_address));
                } else if (response.code() == 429) {
                    errorMessageResId.setValue(R.string.error_too_many_attempts);
                } else {
                    errorMessageText.setValue(context.getString(R.string.unable_to_login));
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable throwable) {
                loading.setValue(false);

                if (!call.isCanceled()) {
                    errorMessageResId.setValue(R.string.check_internet_connection);
                }
            }
        });
    }

    public void setPassword(String newPassword, String confirmPassword) {
        submitPasswordRequest(
                authRepository.setPassword(newPassword, confirmPassword)
        );
    }

    public void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        submitPasswordRequest(authRepository.changePassword(currentPassword, newPassword, confirmPassword));
    }

    private void submitPasswordRequest(Call<ChangePasswordResponse> call) {

        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }

        loading.setValue(true);
        clearErrors();
        passwordResponse.setValue(null);

        passwordCall = call;

        passwordCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ChangePasswordResponse> call, @NonNull Response<ChangePasswordResponse> response) {
                loading.setValue(false);
                ChangePasswordResponse body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    passwordResponse.setValue(body);
                    return;
                }

                if (body != null && body.getMessage() != null && !body.getMessage().trim().isEmpty()) {
                    errorMessageText.setValue(body.getMessage());
                } else if (response.code() == 401) {
                    errorMessageText.setValue(context.getString(R.string.invalid_email_address_password));
                } else if (response.code() == 422) {
                    errorMessageText.setValue(context.getString(R.string.error_check_details));
                } else {
                    errorMessageText.setValue(context.getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChangePasswordResponse> call, @NonNull Throwable throwable) {
                loading.setValue(false);
                if (!call.isCanceled()) {
                    errorMessageResId.setValue(R.string.check_internet_connection);
                }
            }
        });
    }

    public void forgotPassword(String email) {

        if (Boolean.TRUE.equals(loading.getValue())) return;

        loading.setValue(true);
        clearErrors();
        passwordResetResponse.setValue(null);

        Call<PasswordResetResponse> call =
                authRepository.forgotPassword(email);

        call.enqueue(new Callback<>() {

            @Override
            public void onResponse(
                    @NonNull Call<PasswordResetResponse> call,
                    @NonNull Response<PasswordResetResponse> response
            ) {
                loading.setValue(false);

                PasswordResetResponse body = response.body();

                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    passwordResetResponse.setValue(body);
                } else {
                    errorMessageText.setValue(
                            body != null && body.getMessage() != null
                                    ? body.getMessage()
                                    : "Unable to send verification code."
                    );
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<PasswordResetResponse> call,
                    @NonNull Throwable throwable
            ) {
                loading.setValue(false);

                if (!call.isCanceled()) {
                    errorMessageResId.setValue(R.string.check_internet_connection);
                }
            }
        });
    }

    public void verifyResetOtp(String email, String otp) {

        if (Boolean.TRUE.equals(loading.getValue())) return;

        loading.setValue(true);
        clearErrors();
        passwordResetResponse.setValue(null);

        Call<PasswordResetResponse> call = authRepository.verifyResetOtp(email, otp);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PasswordResetResponse> call, @NonNull Response<PasswordResetResponse> response) {
                loading.setValue(false);

                PasswordResetResponse body = response.body();

                if (response.isSuccessful() && body != null && body.isSuccess() && body.getResetToken() != null && !body.getResetToken().trim().isEmpty()) {
                    passwordResetResponse.setValue(body);
                } else {
                    errorMessageText.setValue(body != null && body.getMessage() != null ? body.getMessage() :
                            context.getString(R.string.invalid_expired_verification_code));
                }
            }

            @Override
            public void onFailure(@NonNull Call<PasswordResetResponse> call, @NonNull Throwable throwable) {
                loading.setValue(false);
                if (!call.isCanceled()) {
                    errorMessageResId.setValue(R.string.check_internet_connection);
                }
            }
        });
    }

    public void resetPassword(String email, String resetToken, String newPassword, String confirmPassword) {
        if (Boolean.TRUE.equals(loading.getValue()))
            return;

        loading.setValue(true);
        clearErrors();
        passwordResetResponse.setValue(null);

        Call<PasswordResetResponse> call = authRepository.resetPassword(email, resetToken, newPassword, confirmPassword);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PasswordResetResponse> call, @NonNull Response<PasswordResetResponse> response) {
                loading.setValue(false);

                PasswordResetResponse body = response.body();

                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    passwordResetResponse.setValue(body);
                } else {
                    errorMessageText.setValue(body != null && body.getMessage() != null ? body.getMessage() : context.getString(R.string.unable_reset_password));
                }
            }

            @Override
            public void onFailure(@NonNull Call<PasswordResetResponse> call, @NonNull Throwable throwable) {
                loading.setValue(false);
                if (!call.isCanceled()) {
                    errorMessageResId.setValue(R.string.check_internet_connection);
                }
            }
        });
    }

    public void revokeDevice(long deviceId) {
        revokeResponse.setValue(null);
        authRepository.revokeDevice(deviceId).observeForever(revokeResponse::setValue);
    }

    public void revokeOtherDevices() {
        revokeOthersResponse.setValue(null);
        authRepository.revokeOtherDevices().observeForever(revokeOthersResponse::setValue);
    }


    public void loadDevices() {

        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }

        loading.setValue(true);
        clearErrors();
        devicesResponse.setValue(null);

        devicesCall = authRepository.getDevices();

        devicesCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<DeviceSessionResponse> call, @NonNull Response<DeviceSessionResponse> response) {
                loading.setValue(false);
                DeviceSessionResponse body = response.body();
                if (response.isSuccessful() && body != null && body.isSuccess()) {
                    devicesResponse.setValue(body);
                } else {
                    errorMessageText.setValue(body != null && body.getMessage() != null
                            ? body.getMessage() : context.getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeviceSessionResponse> call, @NonNull Throwable throwable) {
                loading.setValue(false);
                if (!call.isCanceled()) {
                    errorMessageResId.setValue(R.string.check_internet_connection);
                }
            }
        });
    }

    private void clearErrors() {
        errorMessageResId.setValue(null);
        errorMessageText.setValue(null);
    }

    @SuppressLint("EmptySuperCall")
    @Override
    protected void onCleared() {
        if (registerCall != null) {
            registerCall.cancel();
        }

        if (verifyEmailCall != null) {
            verifyEmailCall.cancel();
        }

        if (googleAuthCall != null) {
            googleAuthCall.cancel();
        }

        if (loginCall != null) {
            loginCall.cancel();
        }

        if (passwordCall != null) {
            passwordCall.cancel();
        }

        if (logoutCall != null) {
            logoutCall.cancel();
        }

        if (devicesCall != null) {
            devicesCall.cancel();
        }

        super.onCleared();
    }
}