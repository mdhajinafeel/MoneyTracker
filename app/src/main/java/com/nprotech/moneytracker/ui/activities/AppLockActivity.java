package com.nprotech.moneytracker.ui.activities;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.security.AppLockManager;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.PasswordCredentialStore;
import com.nprotech.moneytracker.utils.PinCredentialStore;

import java.security.GeneralSecurityException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AppLockActivity extends BaseActivity {

    private AppCompatImageView ivPasswordVisible;
    private AppCompatTextView tvCreatePinHint;
    private AppCompatEditText etPassword;
    private LinearLayout passwordLayout, pinLayout;
    private MaterialButton btnUnlock, btnFingerprint;
    private String enteredPin = "";
    private boolean passwordVisible = false;
    private boolean verifying = false;
    private final ExecutorService verificationExecutor = Executors.newSingleThreadExecutor();
    private String primaryMethod;
    private View[] pinDots;
    private boolean fingerprintPromptStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View rootView = findViewById(R.id.rootView);
            AppCompatTextView tvHeading = findViewById(R.id.tvHeading);
            tvCreatePinHint = findViewById(R.id.tvCreatePinHint);
            pinLayout = findViewById(R.id.pinLayout);
            passwordLayout = findViewById(R.id.passwordLayout);
            etPassword = findViewById(R.id.etPassword);
            ivPasswordVisible = findViewById(R.id.ivPasswordVisible);
            btnUnlock = findViewById(R.id.btnUnlock);
            btnFingerprint = findViewById(R.id.btnFingerprint);

            pinDots = new View[]{
                    findViewById(R.id.pinDot1),
                    findViewById(R.id.pinDot2),
                    findViewById(R.id.pinDot3),
                    findViewById(R.id.pinDot4),
                    findViewById(R.id.pinDot5),
                    findViewById(R.id.pinDot6)
            };

            tvHeading.setText(getString(R.string.unlock_app, getString(R.string.app_name)));

            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            setupUnlockMethod();
            setupPinKeypad();
            setupListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void setupUnlockMethod() {
        PreferenceManager prefs = PreferenceManager.INSTANCE;
        primaryMethod = prefs.getPrimaryUnlockMethod();

        boolean pinAvailable = PinCredentialStore.isPinSet();
        boolean passwordAvailable = PasswordCredentialStore.isPasswordSet();
        boolean fingerprintEnabled = prefs.isFingerprintEnabled();

        pinLayout.setVisibility(View.GONE);
        passwordLayout.setVisibility(View.GONE);
        btnUnlock.setVisibility(View.GONE);
        btnFingerprint.setVisibility(View.GONE);

        if (fingerprintEnabled) {
            btnFingerprint.setVisibility(View.VISIBLE);
            btnFingerprint.setText(R.string.fingerprint);
        }

        if (Constants.PRIMARY_METHOD_PASSWORD.equals(primaryMethod) && passwordAvailable) {
            passwordLayout.setVisibility(View.VISIBLE);
            btnUnlock.setVisibility(View.VISIBLE);
            tvCreatePinHint.setText(R.string.enter_your_password);
        } else if (Constants.PRIMARY_METHOD_PIN.equals(primaryMethod) && pinAvailable) {
            pinLayout.setVisibility(View.VISIBLE);
            btnUnlock.setVisibility(View.GONE);
            tvCreatePinHint.setText(R.string.enter_pin_or_password);
        } else if (pinAvailable || passwordAvailable) {
            if (pinAvailable) {
                primaryMethod = Constants.PRIMARY_METHOD_PIN;
                pinLayout.setVisibility(View.VISIBLE);
                btnUnlock.setVisibility(View.GONE);
            } else {
                primaryMethod = Constants.PRIMARY_METHOD_PASSWORD;
                passwordLayout.setVisibility(View.VISIBLE);
                btnUnlock.setVisibility(View.VISIBLE);
            }

            tvCreatePinHint.setText(R.string.enter_pin_or_password);
        }

        if (fingerprintEnabled && Constants.PRIMARY_METHOD_FINGERPRINT.equals(primaryMethod)) {
            btnFingerprint.post(this::startFingerprintAuthentication);
        }
    }

    private void setupPinKeypad() {
        int[] keyIds = {
                R.id.key0, R.id.key1, R.id.key2, R.id.key3, R.id.key4,
                R.id.key5, R.id.key6, R.id.key7, R.id.key8, R.id.key9
        };

        for (int digit = 0; digit <= 9; digit++) {
            final String value = String.valueOf(digit);
            findViewById(keyIds[digit]).setOnClickListener(v -> {
                if (verifying || enteredPin.length() >= 6) {
                    return;
                }

                enteredPin += value;
                updatePinDots();

                if (enteredPin.length() == 6) {
                    verifyPin();
                }
            });
        }

        findViewById(R.id.keyDelete).setOnClickListener(v -> {
            if (verifying || enteredPin.isEmpty()) {
                return;
            }

            enteredPin = enteredPin.substring(0, enteredPin.length() - 1);
            updatePinDots();
        });
    }

    private void updatePinDots() {
        int enteredCount = enteredPin.length();
        for (int i = 0; i < pinDots.length; i++) {
            View dot = pinDots[i];
            dot.setBackgroundResource(i < enteredCount ? R.drawable.bg_pin_dot_filled : R.drawable.bg_pin_dot);
        }
    }

    private void setupListeners() {
        try {
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    // Intentionally remain on the lock screen.
                }
            });

            btnUnlock.setOnClickListener(v -> {
                if (verifying) {
                    return;
                }

                if (Constants.PRIMARY_METHOD_PASSWORD.equals(primaryMethod)) {
                    verifyPassword();
                } else if (Constants.PRIMARY_METHOD_PIN.equals(primaryMethod)) {
                    if (enteredPin.length() != 6) {
                        Toast.makeText(AppLockActivity.this, R.string.enter_six_digit_pin, Toast.LENGTH_SHORT).show();
                    } else {
                        verifyPin();
                    }
                }
            });

            ivPasswordVisible.setOnClickListener(v -> {
                passwordVisible = !passwordVisible;
                togglePasswordVisibility(etPassword, ivPasswordVisible, passwordVisible);
            });

            btnFingerprint.setOnClickListener(v -> startFingerprintAuthentication());
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void togglePasswordVisibility(AppCompatEditText editText, AppCompatImageView eyeIcon, boolean show) {
        if (editText == null || eyeIcon == null) {
            AppLogger.e(getClass(), "togglePasswordVisibility", new IllegalStateException("Password field or visibility icon is missing"));
            return;
        }

        int cursorPosition = editText.getSelectionStart();

        if (show) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            eyeIcon.setImageResource(R.drawable.ic_view_eye);
            eyeIcon.setContentDescription("Hide password");
        } else {
            editText.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
            eyeIcon.setImageResource(R.drawable.ic_eye_off);
            eyeIcon.setContentDescription("Show password");
        }

        int length = editText.getText() != null ? editText.getText().length() : 0;
        editText.setSelection(Math.max(0, Math.min(cursorPosition, length)));
    }

    private void verifyPassword() {
        if (etPassword == null) {
            return;
        }

        String password = etPassword.getText() == null
                ? "" : etPassword.getText().toString();

        if (password.isEmpty()) {
            etPassword.setError("Enter your password.");
            return;
        }

        setVerifying(true);

        verificationExecutor.execute(() -> {
            boolean valid = false;
            Exception error = null;

            try {
                valid = PasswordCredentialStore.verifyPassword(password);
            } catch (GeneralSecurityException | IllegalArgumentException e) {
                error = e;
            }

            final boolean result = valid;
            final Exception verificationError = error;

            runOnUiThread(() -> {
                setVerifying(false);

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (verificationError != null) {
                    AppLogger.e(getClass(), "verifyPassword",
                            verificationError);
                    Toast.makeText(this, R.string.unable_verify_password, Toast.LENGTH_SHORT).show();
                } else if (result) {
                    onUnlockSuccess();
                } else {
                    etPassword.setError(getString(R.string.incorrect_password));
                    etPassword.setText("");
                }
            });
        });
    }

    private void verifyPin() {
        if (enteredPin.length() != 6 || verifying) {
            return;
        }

        String pin = enteredPin;
        setVerifying(true);

        verificationExecutor.execute(() -> {
            boolean valid = false;
            Exception error = null;

            try {
                valid = PinCredentialStore.verifyPin(pin);
            } catch (GeneralSecurityException | IllegalArgumentException e) {
                error = e;
            }

            final boolean result = valid;
            final Exception verificationError = error;

            runOnUiThread(() -> {
                setVerifying(false);

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (verificationError != null) {
                    AppLogger.e(getClass(), "verifyPin", verificationError);
                    Toast.makeText(this, getString(R.string.unable_verify_pin), Toast.LENGTH_SHORT).show();
                } else if (result) {
                    onUnlockSuccess();
                } else {
                    enteredPin = "";
                    updatePinDots();
                    Toast.makeText(this, R.string.incorrect_pin, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setVerifying(boolean value) {
        verifying = value;

        if (btnUnlock != null) {
            btnUnlock.setEnabled(!value);
        }
    }

    private void onUnlockSuccess() {
        AppLockManager.setAuthenticated(true);
        AppLockManager.resetBackgroundTime();

        setResult(RESULT_OK);
        finish();
        ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
    }

    private void startFingerprintAuthentication() {

        if (fingerprintPromptStarted || isFinishing() || isDestroyed()) {
            return;
        }

        fingerprintPromptStarted = true;

        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) != BiometricManager.BIOMETRIC_SUCCESS) {
            fingerprintPromptStarted = false;
            Toast.makeText(this, getString(R.string.biometric_authentication_unavailable_use_pin), Toast.LENGTH_LONG).show();
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                onUnlockSuccess();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(AppLockActivity.this, R.string.fingerprint_not_recognized, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                fingerprintPromptStarted = false;
                Toast.makeText(AppLockActivity.this, errString, Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo =
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Unlock Expenixo")
                        .setSubtitle("Verify your identity")
                        .setNegativeButtonText("Use PIN or password")
                        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                        .build();

        biometricPrompt.authenticate(promptInfo);
    }

    @Override
    protected void onDestroy() {
        AppLockManager.onLockScreenClosed();
        verificationExecutor.shutdownNow();
        super.onDestroy();
    }
}