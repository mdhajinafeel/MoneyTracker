package com.nprotech.moneytracker.ui.activities;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.models.response.ChangePasswordResponse;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.viewmodel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileChangePasswordActivity extends BaseActivity {

    private AppCompatImageView icBack, ivCurrentPasswordVisible, ivPasswordVisible, ivConfirmPasswordVisible, ivRequirementLength,
            ivRequirementLetter, ivRequirementNumber;
    private AppCompatTextView lblCurrentPassword, tvRequirementLength, tvRequirementLetter, tvRequirementNumber, tvLoaderTitle;
    private AppCompatEditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private MaterialCardView cardCurrentPassword;
    private MaterialButton btnChangePassword;
    private View screenLoader;
    private AuthViewModel authViewModel;
    private boolean hasPassword;
    private boolean navigationHandled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_change_password);
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
            ivCurrentPasswordVisible = findViewById(R.id.ivCurrentPasswordVisible);
            ivPasswordVisible = findViewById(R.id.ivPasswordVisible);
            ivConfirmPasswordVisible = findViewById(R.id.ivConfirmPasswordVisible);
            ivRequirementLength = findViewById(R.id.ivRequirementLength);
            ivRequirementLetter = findViewById(R.id.ivRequirementLetter);
            ivRequirementNumber = findViewById(R.id.ivRequirementNumber);
            lblCurrentPassword = findViewById(R.id.lblCurrentPassword);
            tvRequirementLength = findViewById(R.id.tvRequirementLength);
            tvRequirementLetter = findViewById(R.id.tvRequirementLetter);
            tvRequirementNumber = findViewById(R.id.tvRequirementNumber);
            etCurrentPassword = findViewById(R.id.etCurrentPassword);
            etNewPassword = findViewById(R.id.etNewPassword);
            etConfirmPassword = findViewById(R.id.etConfirmPassword);
            cardCurrentPassword = findViewById(R.id.cardCurrentPassword);
            btnChangePassword = findViewById(R.id.btnChangePassword);
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

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {

                hasPassword = bundle.getBoolean("hasPassword", false);

                tvTitle.setText(hasPassword ? R.string.change_password : R.string.set_password);

                bindData();
                setupListeners();
                setupPasswordRequirementListeners();
                setupObservers();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {
            btnChangePassword.setText(hasPassword ? R.string.change_password : R.string.set_password);
            int visibility = hasPassword ? View.VISIBLE : View.GONE;
            lblCurrentPassword.setVisibility(visibility);
            cardCurrentPassword.setVisibility(visibility);
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

            btnChangePassword.setOnClickListener(view -> submitPassword());

            ivCurrentPasswordVisible.setOnClickListener(view -> togglePasswordVisibility(etCurrentPassword, ivCurrentPasswordVisible));

            ivPasswordVisible.setOnClickListener(view -> togglePasswordVisibility(etNewPassword, ivPasswordVisible));

            ivConfirmPasswordVisible.setOnClickListener(view -> togglePasswordVisibility(etConfirmPassword, ivConfirmPasswordVisible));
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void togglePasswordVisibility(AppCompatEditText editText, AppCompatImageView imageView) {

        boolean isPasswordHidden = editText.getTransformationMethod() instanceof PasswordTransformationMethod;
        int cursorPosition = editText.getSelectionStart();

        if (isPasswordHidden) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.ic_view_eye);
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.ic_eye_off);
        }

        if (cursorPosition >= 0) {
            editText.setSelection(Math.min(cursorPosition, editText.length()));
        }
    }

    private void setupPasswordRequirementListeners() {

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

        updatePasswordRequirements("");
    }

    private void updatePasswordRequirements(String password) {

        boolean hasMinimumLength = password.length() >= 8;

        boolean hasLetter = password.matches("(?s).*\\p{L}.*");

        boolean hasNumber = password.matches("(?s).*\\d.*");

        updateRequirement(
                ivRequirementLength,
                tvRequirementLength,
                hasMinimumLength
        );

        updateRequirement(
                ivRequirementLetter,
                tvRequirementLetter,
                hasLetter
        );

        updateRequirement(
                ivRequirementNumber,
                tvRequirementNumber,
                hasNumber
        );
    }

    private void updateRequirement(AppCompatImageView icon, AppCompatTextView text, boolean satisfied) {
        int color = ContextCompat.getColor(this, satisfied ? R.color.primary : R.color.funky_grey);
        icon.setImageTintList(android.content.res.ColorStateList.valueOf(color));
        text.setTextColor(color);
    }

    private void submitPassword() {
        try {
            hideKeyboard(this);

            String currentPassword = getFieldText(etCurrentPassword);
            String newPassword = getFieldText(etNewPassword);
            String confirmPassword = getFieldText(etConfirmPassword);

            if (hasPassword && currentPassword.isEmpty()) {
                etCurrentPassword.setError(getString(R.string.enter_current_password));
                etCurrentPassword.requestFocus();
                return;
            }

            if (newPassword.length() < 8 || newPassword.length() > 72) {
                etNewPassword.setError(getString(R.string.password_8_72_characters));
                etNewPassword.requestFocus();
                return;
            }

            if (!newPassword.matches("(?s).*\\p{L}.*")) {
                etNewPassword.setError(getString(R.string.password_one_letter));
                etNewPassword.requestFocus();
                return;
            }

            if (!newPassword.matches("(?s).*\\d.*")) {
                etNewPassword.setError(getString(R.string.password_one_number));
                etNewPassword.requestFocus();
                return;
            }

            if (confirmPassword.isEmpty()) {
                etConfirmPassword.setError(getString(R.string.enter_confirm_password));
                etConfirmPassword.requestFocus();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                etConfirmPassword.setError(getString(R.string.passwords_not_match));
                etConfirmPassword.requestFocus();
                return;
            }

            if (hasPassword) {
                authViewModel.changePassword(currentPassword, newPassword, confirmPassword);
            } else {
                authViewModel.setPassword(newPassword, confirmPassword);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "submitPassword", e);
        }
    }

    private String getFieldText(AppCompatEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString();
    }

    private void setupObservers() {

        authViewModel.getLoading().observe(this, isLoading -> {
            boolean loading = Boolean.TRUE.equals(isLoading);
            screenLoader.setVisibility(loading ? View.VISIBLE : View.GONE);
            tvLoaderTitle.setText(R.string.updating_password);
            btnChangePassword.setEnabled(!loading);
            btnChangePassword.setText(loading ? R.string.processing : hasPassword ? R.string.change_password : R.string.set_password);
        });

        authViewModel.getPasswordResponse().observe(this, response -> {
            if (response == null || isFinishing()) {
                return;
            }
            showPasswordSuccessDialog(response);
        });

        authViewModel.getErrorMessageResId().observe(this, messageResId -> {
            if (messageResId == null || isFinishing()) {
                return;
            }
            showPasswordErrorDialog(getString(messageResId));
        });

        authViewModel.getErrorMessageText().observe(this, message -> {
            if (message == null || message.trim().isEmpty() || isFinishing()) {
                return;
            }
            showPasswordErrorDialog(message);
        });
    }

    private void showPasswordSuccessDialog(ChangePasswordResponse response) {
        if (navigationHandled || isFinishing()) {
            return;
        }

        String message = response.getMessage() != null ? response.getMessage() : getString(R.string.password_updated_successfully);

        showAuthDialog(getString(R.string.success), message, R.drawable.ic_account,
                getString(R.string.continue_pro), () -> {
                    if (!navigationHandled) {
                        navigationHandled = true;
                        setResult(RESULT_OK);
                        finishWithTransitions();
                    }
                });
    }

    private void showPasswordErrorDialog(String message) {
        showAuthDialog(getString(R.string.something_went_wrong), message, R.drawable.ic_password, getString(R.string.ok), null);
    }

    private void showAuthDialog(String title, String message, int iconRes, String buttonText, Runnable onAction) {

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_auth_message, null);

        MaterialCardView cardHeader = dialogView.findViewById(R.id.cardHeader);
        AppCompatImageView ivDialogIcon = dialogView.findViewById(R.id.ivDialogIcon);
        AppCompatTextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        AppCompatTextView tvDialogMessage = dialogView.findViewById(R.id.tvDialogMessage);
        MaterialButton btnDialogAction = dialogView.findViewById(R.id.btnDialogAction);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        cardHeader.setCardBackgroundColor(getColor(R.color.budget_light));
        ivDialogIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.budget_dark)));
        tvDialogTitle.setText(title);
        tvDialogMessage.setText(message);
        ivDialogIcon.setImageResource(iconRes);
        btnDialogAction.setText(buttonText);

        btnDialogAction.setOnClickListener(v -> {
            dialog.dismiss();
            if (onAction != null) {
                onAction.run();
            }
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(ProfileChangePasswordActivity.this, R.anim.scale_in, R.anim.right_to_left);
    }
}