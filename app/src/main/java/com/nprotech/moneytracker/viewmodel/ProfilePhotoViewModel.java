package com.nprotech.moneytracker.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.models.ProfilePhotoUploadResult;
import com.nprotech.moneytracker.repositories.ProfilePhotoRepository;

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;

@HiltViewModel
public class ProfilePhotoViewModel extends ViewModel {

    private final ProfilePhotoRepository repository;
    private final MutableLiveData<ProfilePhotoUploadResult> uploadResult = new MutableLiveData<>();

    private boolean uploading = false;

    @Inject
    public ProfilePhotoViewModel(ProfilePhotoRepository repository) {
        this.repository = repository;
    }

    public LiveData<ProfilePhotoUploadResult> getUploadResult() {
        return uploadResult;
    }

    public void uploadProfilePhoto(Uri uri) {
        if (uri == null || uploading) {
            return;
        }

        uploading = true;
        uploadResult.setValue(ProfilePhotoUploadResult.loading());

        repository.uploadProfilePhoto(uri, result -> {
            uploading = false;
            uploadResult.postValue(result);
        });
    }
}