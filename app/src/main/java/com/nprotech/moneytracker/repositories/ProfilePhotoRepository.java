package com.nprotech.moneytracker.repositories;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.ProfilePhotoUploadResult;
import com.nprotech.moneytracker.models.response.ProfilePhotoUploadResponse;
import com.nprotech.moneytracker.network.IApiService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import dagger.hilt.android.qualifiers.ApplicationContext;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfilePhotoRepository {

    private static final long MAX_FILE_SIZE = 2L * 1024 * 1024;
    private final Context appContext;
    private final IApiService apiService;

    public ProfilePhotoRepository(@ApplicationContext Context context, IApiService apiService) {
        this.appContext = context;
        this.apiService = apiService;
    }

    public void uploadProfilePhoto(Uri uri, CallbackResult callback) {
        File imageFile;

        try {
            imageFile = copyToCache(uri);
            if (imageFile.length() == 0 || imageFile.length() > MAX_FILE_SIZE) {
                deleteTempFile(imageFile);
                callback.onResult(ProfilePhotoUploadResult.error(appContext.getString(R.string.select_image_smaller)));
                return;
            }
        } catch (IOException e) {
            callback.onResult(ProfilePhotoUploadResult.error(appContext.getString(R.string.could_read_selected_image)));
            return;
        }

        String mimeType = appContext.getContentResolver().getType(uri);

        if (mimeType == null || !(mimeType.equals("image/jpeg") || mimeType.equals("image/png") || mimeType.equals("image/webp"))) {
            deleteTempFile(imageFile);
            callback.onResult(ProfilePhotoUploadResult.error(appContext.getString(R.string.choose_jpeg_png_webp_image)));
            return;
        }

        RequestBody requestBody = RequestBody.create(imageFile, MediaType.get(mimeType));
        MultipartBody.Part photoPart = MultipartBody.Part.createFormData("photo", imageFile.getName(), requestBody);
        apiService.uploadProfilePhoto(photoPart).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ProfilePhotoUploadResponse> call, @NonNull Response<ProfilePhotoUploadResponse> response) {
                deleteTempFile(imageFile);

                ProfilePhotoUploadResponse body = response.body();

                if (response.isSuccessful() && body != null && body.success && body.data != null && body.data.avatarUrl != null
                        && !body.data.avatarUrl.trim().isEmpty()) {
                    String photoUrl = body.data.avatarUrl;
                    PreferenceManager.INSTANCE.setAuthProfilePhoto(photoUrl);
                    callback.onResult(ProfilePhotoUploadResult.success(photoUrl));
                } else {
                    String message = body != null && body.message != null ? body.message : appContext.getString(R.string.could_update_profile_photo);
                    callback.onResult(ProfilePhotoUploadResult.error(message));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfilePhotoUploadResponse> call, @NonNull Throwable t) {
                deleteTempFile(imageFile);
                callback.onResult(ProfilePhotoUploadResult.error(appContext.getString(R.string.upload_failed_check_connection_try_again)));
            }
        });
    }

    private File copyToCache(Uri uri) throws IOException {
        File file = File.createTempFile("profile_photo_", ".tmp", appContext.getCacheDir());
        ContentResolver resolver = appContext.getContentResolver();

        try (InputStream input = resolver.openInputStream(uri);
             OutputStream output = new FileOutputStream(file)) {

            if (input == null) {
                throw new IOException(appContext.getString(R.string.cannot_open_selected_image));
            }

            byte[] buffer = new byte[8192];
            int count;

            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
        } catch (IOException e) {
            deleteTempFile(file);
            throw e;
        }

        return file;
    }

    public interface CallbackResult {
        void onResult(ProfilePhotoUploadResult result);
    }

    private void deleteTempFile(File file) {
        if (file != null && file.exists() && !file.delete()) {
            AppLogger.w(getClass(), "Failed to delete temporary file: " + file.getName());
        }
    }
}