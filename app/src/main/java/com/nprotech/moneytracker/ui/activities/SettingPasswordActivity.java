package com.nprotech.moneytracker.ui.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.PasswordCredentialStore;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingPasswordActivity extends BaseActivity {

    private AppCompatImageView icBack, ivPasswordVisible, ivConfirmPasswordVisible, ivCurrentPasswordVisible, ivRequirementLength,
            ivRequirementLetter, ivRequirementNumber;
    private AppCompatEditText etPassword, etConfirmPassword, etCurrentPassword;
    private AppCompatTextView tvRequirementLength, tvRequirementLetter, tvRequirementNumber, lblCurrentPassword, tvCreatePasswordHeading,
            lblCreatePasswordHint, lblPassword, lblConfirmPassword;
    private MaterialCardView cardCurrentPassword;
    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;
    private MaterialButton btnCreatePassword, btnRemovePassword;
    private boolean isChangeMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_create_password);
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
            ivPasswordVisible = findViewById(R.id.ivPasswordVisible);
            ivConfirmPasswordVisible = findViewById(R.id.ivConfirmPasswordVisible);
            ivCurrentPasswordVisible = findViewById(R.id.ivCurrentPasswordVisible);
            ivConfirmPasswordVisible = findViewById(R.id.ivConfirmPasswordVisible);
            etPassword = findViewById(R.id.etPassword);
            etConfirmPassword = findViewById(R.id.etConfirmPassword);
            etCurrentPassword = findViewById(R.id.etCurrentPassword);
            btnCreatePassword = findViewById(R.id.btnCreatePassword);
            ivPasswordVisible = findViewById(R.id.ivPasswordVisible);
            ivRequirementLength = findViewById(R.id.ivRequirementLength);
            ivRequirementLetter = findViewById(R.id.ivRequirementLetter);
            ivRequirementNumber = findViewById(R.id.ivRequirementNumber);
            tvRequirementLength = findViewById(R.id.tvRequirementLength);
            tvRequirementLetter = findViewById(R.id.tvRequirementLetter);
            tvRequirementNumber = findViewById(R.id.tvRequirementNumber);
            lblCurrentPassword = findViewById(R.id.lblCurrentPassword);
            cardCurrentPassword = findViewById(R.id.cardCurrentPassword);
            btnRemovePassword = findViewById(R.id.btnRemovePassword);
            tvCreatePasswordHeading = findViewById(R.id.tvCreatePasswordHeading);
            lblCreatePasswordHint = findViewById(R.id.lblCreatePasswordHint);
            lblPassword = findViewById(R.id.lblPassword);
            lblConfirmPassword = findViewById(R.id.lblConfirmPassword);

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

            tvTitle.setText(R.string.password);

            bindData();
            setupListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {

            lblCreatePasswordHint.setText(getString(R.string.create_password_hint, getString(R.string.app_name)));

            isChangeMode = PasswordCredentialStore.isPasswordSet();
            btnRemovePassword.setVisibility(isChangeMode ? View.VISIBLE : View.GONE);
            configurePasswordScreen();
            btnCreatePassword.setEnabled(false);
            btnCreatePassword.setAlpha(0.4f);
            updatePasswordRequirements();
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> finishWithTransitions());

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finishWithTransitions();
                }
            });

            ivPasswordVisible.setOnClickListener(v -> {
                passwordVisible = !passwordVisible;
                togglePasswordVisibility(etPassword, ivPasswordVisible, passwordVisible);
            });

            ivConfirmPasswordVisible.setOnClickListener(v -> {
                confirmPasswordVisible = !confirmPasswordVisible;
                togglePasswordVisibility(etConfirmPassword, ivConfirmPasswordVisible, confirmPasswordVisible);
            });

            ivCurrentPasswordVisible.setOnClickListener(v -> {
                boolean show = etCurrentPassword.getTransformationMethod() instanceof PasswordTransformationMethod;
                togglePasswordVisibility(etCurrentPassword, ivCurrentPasswordVisible, show);
            });

            TextWatcher passwordWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updatePasswordRequirements();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            };

            etCurrentPassword.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    etConfirmPassword.requestFocus();
                    return true;
                }
                return false;
            });

            etPassword.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    etConfirmPassword.requestFocus();
                    return true;
                }
                return false;
            });

            etPassword.addTextChangedListener(passwordWatcher);
            etConfirmPassword.addTextChangedListener(passwordWatcher);

            btnCreatePassword.setOnClickListener(v -> createPassword());

            btnRemovePassword.setOnClickListener(v -> showRemovePasswordDialog());
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void createPassword() {
        String currentPassword = etCurrentPassword.getText() == null ? "" : etCurrentPassword.getText().toString();
        String password = etPassword.getText() == null ? "" : etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText() == null ? "" : etConfirmPassword.getText().toString();

        // Verify the current password when changing.
        if (isChangeMode && currentPassword.isEmpty()) {
            etCurrentPassword.setError(getString(R.string.enter_current_password));
            etCurrentPassword.requestFocus();
            return;
        }

        if (!isPasswordValid(password)) {
            etPassword.setError(getString(R.string.password_character_hint));
            etPassword.requestFocus();
            return;
        }

        if (isChangeMode && currentPassword.equals(password)) {
            etPassword.setError(getString(R.string.new_password_different));
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.passwords_do_not_match));
            etConfirmPassword.requestFocus();
            return;
        }

        btnCreatePassword.setEnabled(false);
        btnCreatePassword.setAlpha(0.4f);
        btnCreatePassword.setText(isChangeMode ? R.string.text_changing : R.string.text_creating);
        btnRemovePassword.setVisibility(View.GONE);

        new Thread(() -> {
            boolean currentPasswordValid = true;
            Exception operationError = null;

            try {
                if (isChangeMode) {
                    currentPasswordValid = PasswordCredentialStore.verifyPassword(currentPassword);
                }

                if (currentPasswordValid) {
                    PasswordCredentialStore.savePassword(password);
                }
            } catch (Exception e) {
                operationError = e;
            }

            final boolean verified = currentPasswordValid;
            final Exception error = operationError;

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (error != null) {
                    AppLogger.e(getClass(), "createPassword", error);
                    Toast.makeText(this, isChangeMode ? R.string.unable_change_password : R.string.unable_save_password, Toast.LENGTH_LONG).show();
                    restorePasswordButton();
                    return;
                }

                if (!verified) {
                    etCurrentPassword.setError(getString(R.string.incorrect_current_password));
                    etCurrentPassword.requestFocus();
                    restorePasswordButton();
                    return;
                }

                Toast.makeText(this, isChangeMode ? R.string.password_changed : R.string.password_created, Toast.LENGTH_SHORT).show();
                finishWithTransitions();
            });
        }).start();
    }

    private void restorePasswordButton() {
        btnCreatePassword.setText(isChangeMode ? R.string.change_password : R.string.create_password);
        updatePasswordRequirements();
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

    private boolean hasMinimumLength(String password) {
        return password.length() >= 8;
    }

    private boolean containsLetter(String password) {
        return password.matches("(?s).*\\p{L}.*");
    }

    private boolean containsNumber(String password) {
        return password.matches("(?s).*\\d.*");
    }

    private boolean isPasswordValid(String password) {
        return hasMinimumLength(password) && containsLetter(password) && containsNumber(password);
    }

    private void updateRequirement(AppCompatImageView icon, AppCompatTextView text, boolean satisfied) {
        int color = android.graphics.Color.parseColor(satisfied ? "#7540F4" : "#AAA5C5");
        icon.setColorFilter(color);
        text.setTextColor(Color.parseColor(satisfied ? "#3730A3" : "#77718F"));
    }

    private void updatePasswordRequirements() {
        String password = etPassword.getText() == null ? "" : etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText() == null ? "" : etConfirmPassword.getText().toString();
        updateRequirement(ivRequirementLength, tvRequirementLength, hasMinimumLength(password));
        updateRequirement(ivRequirementLetter, tvRequirementLetter, containsLetter(password));
        updateRequirement(ivRequirementNumber, tvRequirementNumber, containsNumber(password));
        boolean valid = isPasswordValid(password);
        boolean matches = !confirmPassword.isEmpty() && password.equals(confirmPassword);
        btnCreatePassword.setEnabled(valid && matches);
        if (valid && matches) {
            btnCreatePassword.setAlpha(1f);
        } else {
            btnCreatePassword.setAlpha(0.4f);
        }
    }

    private void configurePasswordScreen() {
        isChangeMode = PasswordCredentialStore.isPasswordSet();
        if (isChangeMode) {
            tvCreatePasswordHeading.setText(R.string.change_your_password);
            lblCurrentPassword.setVisibility(View.VISIBLE);
            cardCurrentPassword.setVisibility(View.VISIBLE);
            btnCreatePassword.setText(R.string.change_password);
            etPassword.setHint(R.string.enter_new_password);
            etConfirmPassword.setHint(R.string.re_enter_new_password);

            lblPassword.setText(getString(R.string.new_password));
            lblConfirmPassword.setText(getString(R.string.confirm_new_password));
        } else {
            tvCreatePasswordHeading.setText(R.string.create_your_password);
            lblCurrentPassword.setVisibility(View.GONE);
            cardCurrentPassword.setVisibility(View.GONE);
            btnCreatePassword.setText(R.string.create_password);
            etPassword.setHint(R.string.enter_your_password);
            etConfirmPassword.setHint(R.string.re_enter_your_password);

            lblPassword.setText(getString(R.string.password));
            lblConfirmPassword.setText(getString(R.string.confirm_password));
        }
    }

    private void showRemovePasswordDialog() {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);

        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        MaterialButton tvDelete = view.findViewById(R.id.tvDelete);

        tvDelete.setText(getString(R.string.text_remove));
        tvTitle.setText(getString(R.string.remove_password));
        tvMessage.setText(R.string.remove_password_message);
        tvSubMessage.setVisibility(View.GONE);

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            removePassword();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void removePassword() {
        try {
            PasswordCredentialStore.removePassword();
            Toast.makeText(this, R.string.password_removed, Toast.LENGTH_SHORT).show();
            finishWithTransitions();
        } catch (Exception e) {
            AppLogger.e(getClass(), "removePassword", e);
        }
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(SettingPasswordActivity.this, R.anim.scale_in, R.anim.right_to_left);
    }
}