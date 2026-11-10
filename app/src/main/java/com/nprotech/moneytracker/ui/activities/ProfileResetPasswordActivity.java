package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.viewmodel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileResetPasswordActivity extends BaseActivity {

    private AppCompatImageView icBack, ivPasswordVisible, ivConfirmPasswordVisible, ivRequirementLength,
            ivRequirementLetter, ivRequirementNumber;
    private AppCompatTextView tvRequirementLength, tvRequirementLetter, tvRequirementNumber, tvLoaderTitle;
    private AppCompatEditText etNewPassword, etConfirmPassword;
    private MaterialButton btnResetPassword;
    private View screenLoader;
    private AuthViewModel authViewModel;
    private boolean navigationHandled = false;
    private String email;
    private String resetToken;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private boolean isSubmitting = false;
    private boolean resetSucceeded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_reset_password);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View root = findViewById(R.id.rootView);
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            ivPasswordVisible = findViewById(R.id.ivPasswordVisible);
            ivConfirmPasswordVisible = findViewById(R.id.ivConfirmPasswordVisible);
            ivRequirementLength = findViewById(R.id.ivRequirementLength);
            ivRequirementLetter = findViewById(R.id.ivRequirementLetter);
            ivRequirementNumber = findViewById(R.id.ivRequirementNumber);
            tvRequirementLength = findViewById(R.id.tvRequirementLength);
            tvRequirementLetter = findViewById(R.id.tvRequirementLetter);
            tvRequirementNumber = findViewById(R.id.tvRequirementNumber);
            etNewPassword = findViewById(R.id.etNewPassword);
            etConfirmPassword = findViewById(R.id.etConfirmPassword);
            btnResetPassword = findViewById(R.id.btnResetPassword);
            screenLoader = findViewById(R.id.screenLoader);
            tvLoaderTitle = findViewById(R.id.tvLoaderTitle);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top,
                        v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

            tvTitle.setText(R.string.reset_password);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {

                email = bundle.getString("email");
                resetToken = bundle.getString("reset_token");

                if (email == null || email.trim().isEmpty() || resetToken == null || resetToken.trim().isEmpty()) {
                    Toast.makeText(this, R.string.invalid_reset_session, Toast.LENGTH_SHORT).show();
                    finishWithTransitions();
                    return;
                }

                setupPasswordFields();
                setupListeners();
                observeViewModel();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void setupPasswordFields() {

        etNewPassword.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordRequirements(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitNewPassword();
                return true;
            }
            return false;
        });
    }

    private void setupListeners() {

        icBack.setOnClickListener(v -> finishWithTransitions());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishWithTransitions();
            }
        });

        ivPasswordVisible.setOnClickListener(v -> togglePasswordVisibility(etNewPassword, ivPasswordVisible, true));

        ivConfirmPasswordVisible.setOnClickListener(v -> togglePasswordVisibility(etConfirmPassword, ivConfirmPasswordVisible, false));

        btnResetPassword.setOnClickListener(v -> submitNewPassword());
    }

    private void togglePasswordVisibility(
            AppCompatEditText editText,
            AppCompatImageView imageView,
            boolean isNewPassword) {

        boolean visible;

        if (isNewPassword) {
            isNewPasswordVisible = !isNewPasswordVisible;
            visible = isNewPasswordVisible;
        } else {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            visible = isConfirmPasswordVisible;
        }

        int cursorPosition = editText.getSelectionStart();

        if (visible) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.ic_view_eye);
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.ic_eye_off);
        }

        editText.setSelection(Math.max(0, Math.min(cursorPosition, editText.length())));
    }

    private void updatePasswordRequirements(String password) {

        boolean validLength = password.length() >= 8;
        boolean hasLetter = password.matches(".*[A-Za-z].*");
        boolean hasNumber = password.matches(".*[0-9].*");

        updateRequirement(ivRequirementLength, tvRequirementLength, validLength);
        updateRequirement(ivRequirementLetter, tvRequirementLetter, hasLetter);
        updateRequirement(ivRequirementNumber, tvRequirementNumber, hasNumber);
    }

    private void updateRequirement(AppCompatImageView icon, AppCompatTextView text, boolean satisfied) {
        int color = ContextCompat.getColor(this, satisfied ? R.color.primary : R.color.funky_grey);
        icon.setImageTintList(android.content.res.ColorStateList.valueOf(color));
        text.setTextColor(color);
    }

    private void submitNewPassword() {

        hideKeyboard(this);

        if (isSubmitting || resetSucceeded) {
            return;
        }

        String newPassword = getFieldText(etNewPassword);
        String confirmPassword = getFieldText(etConfirmPassword);

        if (newPassword.isEmpty()) {

            etNewPassword.setError(getString(R.string.enter_new_password));
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 8) {
            etNewPassword.setError(getString(R.string.password_minimum_length));
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() > 72) {
            etNewPassword.setError(getString(R.string.password_maximum_length));
            etNewPassword.requestFocus();
            return;
        }

        if (!newPassword.matches(".*[A-Za-z].*")) {
            etNewPassword.setError(getString(R.string.password_must_contain_letter));
            etNewPassword.requestFocus();
            return;
        }

        if (!newPassword.matches(".*[0-9].*")) {
            etNewPassword.setError(getString(R.string.password_must_contain_number));
            etNewPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError(getString(R.string.confirm_your_password));
            etConfirmPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.passwords_do_not_match));
            etConfirmPassword.requestFocus();
            return;
        }

        isSubmitting = true;
        authViewModel.resetPassword(email.trim(), resetToken, newPassword, confirmPassword);
    }

    private void observeViewModel() {

        authViewModel.getLoading().observe(this, loading -> {
            boolean isLoading = Boolean.TRUE.equals(loading);
            tvLoaderTitle.setText(R.string.resetting_password);
            screenLoader.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnResetPassword.setEnabled(!isLoading && !resetSucceeded);
        });

        authViewModel.getPasswordResetResponse().observe(this, response -> {

            if (response == null || isFinishing()) {
                return;
            }

            if (!isSubmitting || navigationHandled) {
                return;
            }

            if (!response.isSuccess()) {
                isSubmitting = false;
                return;
            }

            isSubmitting = false;
            resetSucceeded = true;

            openLogin();
        });

        authViewModel.getErrorMessageText().observe(this, error -> {

            if (error == null || error.trim().isEmpty() || resetSucceeded || isFinishing()) {
                return;
            }
            isSubmitting = false;
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });

        authViewModel.getErrorMessageResId().observe(this, errorResId -> {

            if (errorResId == null || resetSucceeded || isFinishing()) {
                return;
            }

            isSubmitting = false;

            Toast.makeText(this, getString(errorResId), Toast.LENGTH_SHORT).show();
        });
    }

    private String getFieldText(AppCompatEditText editText) {

        return editText.getText() == null
                ? ""
                : editText.getText().toString();
    }

    private void openLogin() {
        if (navigationHandled) {
            return;
        }

        navigationHandled = true;
        Intent intent = new Intent(ProfileResetPasswordActivity.this, ProfileLoginActivity.class);

        intent.putExtra("email", email);
        intent.putExtra("from_password_reset", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish();
        ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(ProfileResetPasswordActivity.this, R.anim.scale_in, R.anim.right_to_left);
    }
}