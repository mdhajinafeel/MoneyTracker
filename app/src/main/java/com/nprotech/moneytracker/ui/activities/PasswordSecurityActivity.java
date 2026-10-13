package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.PasswordCredentialStore;
import com.nprotech.moneytracker.utils.PinCredentialStore;

import java.util.concurrent.Executor;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PasswordSecurityActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private ConstraintLayout passwordContainer, pinContainer, fingerprintContainer, autoLockContainer, primaryMethodContainer;
    private SwitchCompat switchAppLock, switchFingerprint;
    private AppCompatTextView tvAppLockDesc, tvFingerprintDesc, tvPasswordDesc, tvPinDesc, tvAutoLockDesc, tvPrimaryMethodDesc;
    private boolean updatingFingerprintSwitch = false;
    private boolean updatingAppLockSwitch = false;
    private boolean pendingEnableAppLock = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_setting);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            View rootView = findViewById(R.id.rootView);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            passwordContainer = findViewById(R.id.passwordContainer);
            pinContainer = findViewById(R.id.pinContainer);
            fingerprintContainer = findViewById(R.id.fingerprintContainer);
            autoLockContainer = findViewById(R.id.autoLockContainer);
            primaryMethodContainer = findViewById(R.id.primaryMethodContainer);
            switchAppLock = findViewById(R.id.switchAppLock);
            switchFingerprint = findViewById(R.id.switchFingerprint);
            tvAppLockDesc = findViewById(R.id.tvAppLockDesc);
            tvFingerprintDesc = findViewById(R.id.tvFingerprintDesc);
            tvPasswordDesc = findViewById(R.id.tvPasswordDesc);
            tvPinDesc = findViewById(R.id.tvPinDesc);
            tvAutoLockDesc = findViewById(R.id.tvAutoLockDesc);
            tvPrimaryMethodDesc = findViewById(R.id.tvPrimaryMethodDesc);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            tvTitle.setText(R.string.password_security);

            bindData();
            setupListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {
            tvAppLockDesc.setText(getString(R.string.app_lock_hint, getString(R.string.app_name)));

            boolean passwordSet = PasswordCredentialStore.isPasswordSet();
            boolean pinSet = PinCredentialStore.isPinSet();
            boolean appLockEnabled = PreferenceManager.INSTANCE.isAppLockEnabled();

            if (appLockEnabled && !passwordSet && !pinSet) {
                appLockEnabled = false;
                PreferenceManager.INSTANCE.setAppLockEnabled(false);
                PreferenceManager.INSTANCE.setFingerprintEnabled(false);
            }

            switchAppLock.setOnCheckedChangeListener(null);
            switchAppLock.setChecked(appLockEnabled);
            setSecurityOptionsEnabled(appLockEnabled);

            updateCredentialDescriptions();
            switchFingerprint.setChecked(PreferenceManager.INSTANCE.isFingerprintEnabled());
            updateAutoLockDescription();
            updatePrimaryMethodDescription();
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void updateCredentialDescriptions() {
        boolean passwordSet = PasswordCredentialStore.isPasswordSet();
        boolean pinSet = PinCredentialStore.isPinSet();
        tvPasswordDesc.setText(passwordSet ? R.string.change_password : R.string.not_set);
        tvPinDesc.setText(pinSet ? R.string.change_pin : R.string.not_set);
        tvFingerprintDesc.setText(PreferenceManager.INSTANCE.isFingerprintEnabled() ? R.string.enabled : R.string.not_enabled);
    }

    private void updateAutoLockDescription() {
        if (tvAutoLockDesc == null) {
            return;
        }

        int seconds = PreferenceManager.INSTANCE.getAutoLockSeconds();

        if (seconds == Constants.AUTO_LOCK_IMMEDIATELY) {
            tvAutoLockDesc.setText(R.string.auto_lock_immediately);
        } else if (seconds == Constants.AUTO_LOCK_NEVER) {
            tvAutoLockDesc.setText(R.string.auto_lock_never);
        } else if (seconds < 60) {
            tvAutoLockDesc.setText(getString(R.string.auto_lock_30_seconds));
        } else {
            int minutes = seconds / 60;
            tvAutoLockDesc.setText(getResources().getQuantityString(R.plurals.auto_lock_interval, minutes, minutes));
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            });

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finish();
                    ActivityUtils.overrideCloseTransition(PasswordSecurityActivity.this, R.anim.scale_in, R.anim.right_to_left);
                }
            });

            switchAppLock.setOnCheckedChangeListener((buttonView, isChecked) -> {

                if (updatingAppLockSwitch) {
                    return;
                }

                boolean passwordSet = PasswordCredentialStore.isPasswordSet();
                boolean pinSet = PinCredentialStore.isPinSet();

                if (isChecked && !passwordSet && !pinSet) {
                    pendingEnableAppLock = true;
                    updatingAppLockSwitch = true;
                    switchAppLock.setChecked(false);
                    updatingAppLockSwitch = false;

                    PreferenceManager.INSTANCE.setAppLockEnabled(false);
                    setSecurityOptionsEnabled(false);

                    Toast.makeText(this, R.string.create_password_or_pin_first, Toast.LENGTH_LONG).show();

                    showCreateUnlockMethodDialog();
                    return;
                }

                PreferenceManager.INSTANCE.setAppLockEnabled(isChecked);
                setSecurityOptionsEnabled(isChecked);

                if (!isChecked) {
                    updatingFingerprintSwitch = true;
                    switchFingerprint.setChecked(false);
                    updatingFingerprintSwitch = false;

                    PreferenceManager.INSTANCE.setFingerprintEnabled(false);
                    tvFingerprintDesc.setText(R.string.not_enabled);
                }
            });

            passwordContainer.setOnClickListener(v -> {
                startActivity(new Intent(PasswordSecurityActivity.this, SettingPasswordActivity.class));
                ActivityUtils.overrideOpenTransition(PasswordSecurityActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            pinContainer.setOnClickListener(v -> {
                startActivity(new Intent(PasswordSecurityActivity.this, SettingPinActivity.class));
                ActivityUtils.overrideOpenTransition(PasswordSecurityActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            switchFingerprint.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (updatingFingerprintSwitch) {
                    return;
                }

                if (!isChecked) {
                    disableFingerprintSwitch();
                    return;
                }

                if (!switchAppLock.isChecked()) {
                    disableFingerprintSwitch();
                    Toast.makeText(this, R.string.enable_app_lock_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean credentialExists = PasswordCredentialStore.isPasswordSet() || PinCredentialStore.isPinSet();
                if (!credentialExists) {
                    disableFingerprintSwitch();
                    Toast.makeText(this, "Create a password or PIN first.", Toast.LENGTH_LONG).show();
                    return;
                }

                enableFingerprintAuthentication();
            });

            autoLockContainer.setOnClickListener(v -> {
                if (!switchAppLock.isChecked()) {
                    Toast.makeText(PasswordSecurityActivity.this, R.string.enable_app_lock_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                showAutoLockBottomSheet();
            });

            primaryMethodContainer.setOnClickListener(v -> {
                if (!switchAppLock.isChecked()) {
                    Toast.makeText(this, R.string.enable_app_lock_first, Toast.LENGTH_SHORT).show();
                    return;
                }
                showPrimaryUnlockMethodBottomSheet();
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void showCreateUnlockMethodDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_enable_app_lock, null);

        AppCompatTextView tvCancel = dialogView.findViewById(R.id.tvCancel);
        MaterialButton btnPin = dialogView.findViewById(R.id.btnPin);
        MaterialButton btnPassword = dialogView.findViewById(R.id.btnPassword);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        tvCancel.setOnClickListener(v -> dialog.dismiss());

        btnPin.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(PasswordSecurityActivity.this, SettingPinActivity.class);
            startActivity(intent);
        });

        btnPassword.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(PasswordSecurityActivity.this, SettingPasswordActivity.class);
            startActivity(intent);
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.90), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void enableFingerprintAuthentication() {
        try {
            BiometricManager biometricManager = BiometricManager.from(this);

            int authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK;
            int result = biometricManager.canAuthenticate(authenticators);

            if (result != BiometricManager.BIOMETRIC_SUCCESS) {
                disableFingerprintSwitch();

                String message;

                if (result == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                    message = getString(R.string.no_biometric_enrolled);
                } else if (result == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE) {
                    message = getString(R.string.device_not_support_biometric);
                } else {
                    message = getString(R.string.biometric_authentication_unavailable);
                }

                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                return;
            }

            Executor executor = ContextCompat.getMainExecutor(this);

            BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult authResult) {
                    super.onAuthenticationSucceeded(authResult);
                    PreferenceManager.INSTANCE.setFingerprintEnabled(true);
                    tvFingerprintDesc.setText(getString(R.string.enabled));
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(PasswordSecurityActivity.this, R.string.biometric_not_recognized, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    disableFingerprintSwitch();
                    Toast.makeText(PasswordSecurityActivity.this, errString, Toast.LENGTH_SHORT).show();
                }
            });

            BiometricPrompt.PromptInfo promptInfo =
                    new BiometricPrompt.PromptInfo.Builder()
                            .setTitle(getString(R.string.enable_biometric_unlock))
                            .setSubtitle(getString(R.string.verify_biometric_enable))
                            .setAllowedAuthenticators(authenticators)
                            .setNegativeButtonText(getString(R.string.cancel))
                            .build();
            biometricPrompt.authenticate(promptInfo);
        } catch (Exception e) {
            disableFingerprintSwitch();
            AppLogger.e(getClass(), "enableFingerprintAuthentication", e);
        }
    }

    private void disableFingerprintSwitch() {
        updatingFingerprintSwitch = true;
        switchFingerprint.setChecked(false);
        updatingFingerprintSwitch = false;
        PreferenceManager.INSTANCE.setFingerprintEnabled(false);
        tvFingerprintDesc.setText(R.string.not_enabled);
    }

    private void showAutoLockBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_auto_lock, findViewById(android.R.id.content), false);
        dialog.setContentView(sheetView);
        RadioGroup radioGroup = sheetView.findViewById(R.id.rgAutoLockOptions);
        MaterialButton btnSave = sheetView.findViewById(R.id.btnApply);
        MaterialButton btnReset = sheetView.findViewById(R.id.btnReset);
        AppCompatTextView tvClose = sheetView.findViewById(R.id.tvClose);
        AppCompatTextView tvSubHeader = sheetView.findViewById(R.id.tvSubHeader);
        int currentSeconds = PreferenceManager.INSTANCE.getAutoLockSeconds();
        int selectedId = getAutoLockRadioId(currentSeconds);
        radioGroup.check(selectedId);

        tvSubHeader.setText(getString(R.string.auto_lock_subtitle, getString(R.string.app_name)));

        updateRadioTextStyle(radioGroup, selectedId);
        radioGroup.setOnCheckedChangeListener(this::updateRadioTextStyle);

        tvClose.setOnClickListener(v -> dialog.dismiss());

        btnReset.setOnClickListener(v -> {
            PreferenceManager.INSTANCE.setAutoLockSeconds(Constants.AUTO_LOCK_NEVER);
            radioGroup.check(R.id.rbAutoLockNever);
            updateAutoLockDescription();
            dialog.dismiss();
        });

        btnSave.setOnClickListener(v -> {
            int checkedId = radioGroup.getCheckedRadioButtonId();
            if (checkedId == -1) {
                Toast.makeText(this, R.string.select_auto_lock_interval, Toast.LENGTH_SHORT).show();
                return;
            }
            int seconds = getAutoLockSecondsFromRadioId(checkedId);
            PreferenceManager.INSTANCE.setAutoLockSeconds(seconds);
            updateAutoLockDescription();
            dialog.dismiss();
        });

        dialog.setDismissWithAnimation(true);
        dialog.show();
    }

    private void showPrimaryUnlockMethodBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_primary_unlock_method, findViewById(android.R.id.content), false);
        dialog.setContentView(sheetView);
        RadioGroup radioGroup = sheetView.findViewById(R.id.rgPrimaryUnlockMethod);
        AppCompatRadioButton rbPassword = sheetView.findViewById(R.id.rbPrimaryPassword);
        AppCompatRadioButton rbPin = sheetView.findViewById(R.id.rbPrimaryPin);
        AppCompatRadioButton rbFingerprint = sheetView.findViewById(R.id.rbPrimaryFingerprint);
        MaterialButton btnApply = sheetView.findViewById(R.id.btnApply);
        MaterialButton btnReset = sheetView.findViewById(R.id.btnReset);
        AppCompatTextView tvClose = sheetView.findViewById(R.id.tvClose);
        AppCompatTextView tvSubHeader = sheetView.findViewById(R.id.tvSubHeader);
        String primaryUnlockMethod = PreferenceManager.INSTANCE.getPrimaryUnlockMethod();
        int selectedId = getPrimaryUnlockMethodRadioId(primaryUnlockMethod);
        radioGroup.check(selectedId);

        updateRadioTextStyle(radioGroup, selectedId);
        radioGroup.setOnCheckedChangeListener(this::updateRadioTextStyle);

        tvSubHeader.setText(getString(R.string.primary_unlock_method_hint, getString(R.string.app_name)));

        boolean passwordSet = PasswordCredentialStore.isPasswordSet();
        boolean pinSet = PinCredentialStore.isPinSet();
        boolean fingerprintEnabled = PreferenceManager.INSTANCE.isFingerprintEnabled();

        // Fingerprint requires a password or PIN fallback.
        boolean fingerprintAvailable = fingerprintEnabled && (passwordSet || pinSet);
        rbPassword.setEnabled(passwordSet);
        rbPin.setEnabled(pinSet);
        rbFingerprint.setEnabled(fingerprintAvailable);
        String currentMethod = PreferenceManager.INSTANCE.getPrimaryUnlockMethod();

        // Use a configured method if the saved method is no longer available.
        if (Constants.PRIMARY_METHOD_PASSWORD.equals(currentMethod) && !passwordSet) {
            currentMethod = null;
        }

        if (Constants.PRIMARY_METHOD_PIN.equals(currentMethod) && !pinSet) {
            currentMethod = null;
        }

        if (Constants.PRIMARY_METHOD_FINGERPRINT.equals(currentMethod) && !fingerprintAvailable) {
            currentMethod = null;
        }

        if (currentMethod == null) {
            currentMethod = getDefaultPrimaryUnlockMethod();
        }

        if (Constants.PRIMARY_METHOD_PASSWORD.equals(currentMethod)) {
            radioGroup.check(R.id.rbPrimaryPassword);
        } else if (Constants.PRIMARY_METHOD_PIN.equals(currentMethod)) {
            radioGroup.check(R.id.rbPrimaryPin);
        } else if (Constants.PRIMARY_METHOD_FINGERPRINT.equals(currentMethod)) {
            radioGroup.check(R.id.rbPrimaryFingerprint);
        } else {
            radioGroup.clearCheck();
        }

        tvClose.setOnClickListener(v -> dialog.dismiss());

        btnReset.setOnClickListener(v -> {
            String defaultMethod = getDefaultPrimaryUnlockMethod();
            if (defaultMethod == null) {
                Toast.makeText(this, R.string.select_primary_unlock_method, Toast.LENGTH_SHORT).show();
                return;
            }

            PreferenceManager.INSTANCE.setPrimaryUnlockMethod(defaultMethod);
            if (Constants.PRIMARY_METHOD_PASSWORD.equals(defaultMethod)) {
                radioGroup.check(R.id.rbPrimaryPassword);
            } else if (Constants.PRIMARY_METHOD_PIN.equals(defaultMethod)) {
                radioGroup.check(R.id.rbPrimaryPin);
            }
            updatePrimaryMethodDescription();
            dialog.dismiss();
        });

        btnApply.setOnClickListener(v -> {
            int checkedId = radioGroup.getCheckedRadioButtonId();

            String selectedMethod;
            if (checkedId == R.id.rbPrimaryPassword && passwordSet) {
                selectedMethod = Constants.PRIMARY_METHOD_PASSWORD;
            } else if (checkedId == R.id.rbPrimaryPin && pinSet) {
                selectedMethod = Constants.PRIMARY_METHOD_PIN;
            } else if (checkedId == R.id.rbPrimaryFingerprint && fingerprintAvailable) {
                selectedMethod = Constants.PRIMARY_METHOD_FINGERPRINT;
            } else {
                Toast.makeText(this, R.string.select_primary_unlock_method, Toast.LENGTH_SHORT).show();
                return;
            }

            PreferenceManager.INSTANCE.setPrimaryUnlockMethod(selectedMethod);
            updatePrimaryMethodDescription();
            dialog.dismiss();
        });

        dialog.setDismissWithAnimation(true);
        dialog.show();
    }

    private int getAutoLockRadioId(int seconds) {
        return switch (seconds) {
            case Constants.AUTO_LOCK_IMMEDIATELY -> R.id.rbAutoLockImmediately;
            case Constants.AUTO_LOCK_30_SECONDS -> R.id.rbAutoLock30Seconds;
            case Constants.AUTO_LOCK_1_MINUTE -> R.id.rbAutoLock1Minute;
            case Constants.AUTO_LOCK_5_MINUTES -> R.id.rbAutoLock5Minutes;
            case Constants.AUTO_LOCK_15_MINUTES -> R.id.rbAutoLock15Minutes;
            case Constants.AUTO_LOCK_NEVER -> R.id.rbAutoLockNever;
            default -> R.id.rbAutoLockNever;
        };
    }

    private int getPrimaryUnlockMethodRadioId(String primaryUnlock) {
        return switch (primaryUnlock) {
            case Constants.PRIMARY_METHOD_PASSWORD -> R.id.rbPrimaryPassword;
            case Constants.PRIMARY_METHOD_PIN -> R.id.rbPrimaryPin;
            case Constants.PRIMARY_METHOD_FINGERPRINT -> R.id.rbPrimaryFingerprint;
            default -> R.id.rbPrimaryPassword;
        };
    }

    private int getAutoLockSecondsFromRadioId(int checkedId) {
        if (checkedId == R.id.rbAutoLockImmediately) {
            return Constants.AUTO_LOCK_IMMEDIATELY;
        } else if (checkedId == R.id.rbAutoLock30Seconds) {
            return Constants.AUTO_LOCK_30_SECONDS;
        } else if (checkedId == R.id.rbAutoLock1Minute) {
            return Constants.AUTO_LOCK_1_MINUTE;
        } else if (checkedId == R.id.rbAutoLock5Minutes) {
            return Constants.AUTO_LOCK_5_MINUTES;
        } else if (checkedId == R.id.rbAutoLock15Minutes) {
            return Constants.AUTO_LOCK_15_MINUTES;
        } else if (checkedId == R.id.rbAutoLockNever) {
            return Constants.AUTO_LOCK_NEVER;
        }
        return Constants.AUTO_LOCK_NEVER;
    }

    private String getDefaultPrimaryUnlockMethod() {
        boolean passwordSet = PasswordCredentialStore.isPasswordSet();
        boolean pinSet = PinCredentialStore.isPinSet();

        if (passwordSet) {
            return Constants.PRIMARY_METHOD_PASSWORD;
        }

        if (pinSet) {
            return Constants.PRIMARY_METHOD_PIN;
        }

        return null;
    }

    private void updatePrimaryMethodDescription() {
        if (tvPrimaryMethodDesc == null) {
            return;
        }

        String method = PreferenceManager.INSTANCE.getPrimaryUnlockMethod();
        if (Constants.PRIMARY_METHOD_FINGERPRINT.equals(method)) {
            tvPrimaryMethodDesc.setText(R.string.fingerprint);
        } else if (Constants.PRIMARY_METHOD_PIN.equals(method)) {
            tvPrimaryMethodDesc.setText(R.string.text_pin);
        } else if (Constants.PRIMARY_METHOD_PASSWORD.equals(method)) {
            tvPrimaryMethodDesc.setText(R.string.password);
        } else {
            tvPrimaryMethodDesc.setText(R.string.not_set);
        }
    }

    private void updateRadioTextStyle(RadioGroup radioGroup, int checkedId) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            View child = radioGroup.getChildAt(i);
            if (child instanceof AppCompatRadioButton radioButton) {
                radioButton.setTypeface(ResourcesCompat.getFont(this, radioButton.getId() == checkedId ? R.font.exo2_bold : R.font.exo2_medium));
            }
        }
    }

    private void setSecurityOptionsEnabled(boolean enabled) {
        View[] views = {passwordContainer, pinContainer, fingerprintContainer, autoLockContainer, primaryMethodContainer};
        for (View view : views) {
            view.setAlpha(enabled ? 1f : 0.4f);
            view.setClickable(enabled);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (tvPasswordDesc == null || tvPinDesc == null) {
            return;
        }

        updateCredentialDescriptions();

        if (pendingEnableAppLock) {
            boolean passwordSet = PasswordCredentialStore.isPasswordSet();
            boolean pinSet = PinCredentialStore.isPinSet();

            if (passwordSet || pinSet) {
                pendingEnableAppLock = false;

                PreferenceManager.INSTANCE.setAppLockEnabled(true);

                updatingAppLockSwitch = true;
                switchAppLock.setChecked(true);
                updatingAppLockSwitch = false;

                setSecurityOptionsEnabled(true);
            }
        }
    }
}