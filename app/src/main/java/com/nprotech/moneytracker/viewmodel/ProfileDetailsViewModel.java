package com.nprotech.moneytracker.viewmodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.models.response.ProfileUpdateResponse;
import com.nprotech.moneytracker.repositories.AuthRepository;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import jakarta.inject.Inject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class ProfileDetailsViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<ProfileUpdateResponse> updateResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private Call<ProfileUpdateResponse> updateCall;
    private final Context context;

    @Inject
    public ProfileDetailsViewModel(AuthRepository authRepository, @ApplicationContext Context context) {
        this.authRepository = authRepository;
        this.context = context;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<ProfileUpdateResponse> getUpdateResult() {
        return updateResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void updateProfile(String fullName) {

        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }

        if (updateCall != null) {
            updateCall.cancel();
        }

        loading.setValue(true);
        errorMessage.setValue(null);
        updateResult.setValue(null);

        updateCall = authRepository.updateProfile(fullName);

        updateCall.enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<ProfileUpdateResponse> call, @NonNull Response<ProfileUpdateResponse> response) {
                loading.setValue(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    updateResult.setValue(response.body());
                    return;
                }

                String message = "Unable to update your profile.";

                try {
                    if (response.errorBody() != null) {
                        ProfileUpdateResponse error = new Gson().fromJson(response.errorBody().string(), ProfileUpdateResponse.class);
                        if (error != null && error.getMessage() != null && !error.getMessage().trim().isEmpty()) {
                            message = error.getMessage();
                        }
                    } else if (response.body() != null
                            && response.body().getMessage() != null) {
                        message = response.body().getMessage();
                    }
                } catch (Exception ignored) {
                    // Use fallback message.
                }

                errorMessage.setValue(message);
            }

            @Override
            public void onFailure(@NonNull Call<ProfileUpdateResponse> call, @NonNull Throwable t) {
                loading.setValue(false);
                if (call.isCanceled()) {
                    return;
                }
                errorMessage.setValue(context.getString(R.string.check_internet_connection));
            }
        });
    }

    @Override
    protected void onCleared() {
        if (updateCall != null) {
            updateCall.cancel();
        }
        super.onCleared();
    }
}