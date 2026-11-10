package com.nprotech.moneytracker.models;

public class ProfilePhotoUploadResult {

    public enum Status {
        LOADING,
        SUCCESS,
        ERROR
    }

    public final Status status;
    public final String photoUrl;
    public final String message;

    private ProfilePhotoUploadResult(
            Status status,
            String photoUrl,
            String message
    ) {
        this.status = status;
        this.photoUrl = photoUrl;
        this.message = message;
    }

    public static ProfilePhotoUploadResult loading() {
        return new ProfilePhotoUploadResult(
                Status.LOADING, null, null
        );
    }

    public static ProfilePhotoUploadResult success(String photoUrl) {
        return new ProfilePhotoUploadResult(
                Status.SUCCESS, photoUrl, null
        );
    }

    public static ProfilePhotoUploadResult error(String message) {
        return new ProfilePhotoUploadResult(
                Status.ERROR, null, message
        );
    }
}