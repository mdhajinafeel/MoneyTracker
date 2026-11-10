package com.nprotech.moneytracker.utils;

import android.app.Activity;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.nprotech.moneytracker.R;

public class GoogleSignInHelper {

    private GoogleSignInHelper() {
        // Utility class
    }

    public interface Callback {
        void onSuccess(String idToken, String email, String displayName);

        void onError(String message);
    }

    public static void signIn(@NonNull Activity activity, @NonNull Callback callback) {
        try {
            CredentialManager credentialManager = CredentialManager.create(activity);
            GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false).setServerClientId(activity.getString(R.string.google_web_client_id)).build();
            GetCredentialRequest request = new GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build();
            credentialManager.getCredentialAsync(activity, request, new CancellationSignal(),
                    ContextCompat.getMainExecutor(activity),
                    new CredentialManagerCallback<>() {

                        @Override
                        public void onResult(GetCredentialResponse result) {
                            handleCredential(result.getCredential(), callback);
                        }

                        @Override
                        public void onError(@NonNull GetCredentialException e) {
                            callback.onError(e.getMessage() != null ? e.getMessage() : "Google Sign-In was cancelled or failed.");
                        }
                    });
        } catch (Exception e) {
            callback.onError(e.getMessage() != null ? e.getMessage() : "Unable to start Google Sign-In.");
        }
    }

    private static void handleCredential(Credential credential, Callback callback) {
        if (!(credential instanceof CustomCredential customCredential)) {
            callback.onError("Unexpected credential type.");
            return;
        }

        if (!GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                .equals(customCredential.getType())) {
            callback.onError("Google did not return an ID token.");
            return;
        }

        try {
            GoogleIdTokenCredential googleCredential = GoogleIdTokenCredential.createFrom(customCredential.getData());
            callback.onSuccess(googleCredential.getIdToken(), googleCredential.getEmail(), googleCredential.getDisplayName());
        } catch (Exception e) {
            callback.onError("Unable to read the Google account response.");
        }
    }
}