package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.GoogleSignInHelper;
import com.nprotech.moneytracker.viewmodel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileCreateAccountActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView tvLogin, tvRequirementLength, tvRequirementLetter, tvRequirementNumber;
    private TextInputEditText etFullName, etEmailAddress, etPassword, etConfirmPassword;
    private MaterialButton btnCreateAccount, btnCreateGoogle;
    private AuthViewModel authViewModel;
    private View screenLoader;
    private boolean navigationHandled = false;
    private AppCompatImageView ivRequirementLength, ivRequirementLetter, ivRequirementNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_create_account);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View root = findViewById(R.id.rootView);
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            AppCompatTextView tvAccountDesc = findViewById(R.id.tvAccountDesc);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            btnCreateAccount = findViewById(R.id.btnCreateAccount);
            btnCreateGoogle = findViewById(R.id.btnCreateGoogle);
            tvLogin = findViewById(R.id.tvLogin);
            etFullName = findViewById(R.id.etFullName);
            etEmailAddress = findViewById(R.id.etEmailAddress);
            etPassword = findViewById(R.id.etPassword);
            etConfirmPassword = findViewById(R.id.etConfirmPassword);
            screenLoader = findViewById(R.id.screenLoader);
            ivRequirementLength = findViewById(R.id.ivRequirementLength);
            ivRequirementLetter = findViewById(R.id.ivRequirementLetter);
            ivRequirementNumber = findViewById(R.id.ivRequirementNumber);
            tvRequirementLength = findViewById(R.id.tvRequirementLength);
            tvRequirementLetter = findViewById(R.id.tvRequirementLetter);
            tvRequirementNumber = findViewById(R.id.tvRequirementNumber);

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

            tvTitle.setText(getString(R.string.create_account));
            tvAccountDesc.setText(getString(R.string.get_started_app, getString(R.string.app_name)));

            authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

            setupListeners();
            setupPasswordRequirementListeners();
            setupObservers();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void submitCreateAccount() {

        hideKeyboard(this);

        String fullName = getFieldText(etFullName);
        String email = getFieldText(etEmailAddress);
        String password = getFieldText(etPassword);
        String confirmPassword = getFieldText(etConfirmPassword);

        if (fullName.length() < 2 || fullName.length() > 150) {
            etFullName.setError(getString(R.string.enter_full_name));
            etFullName.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmailAddress.setError(getString(R.string.enter_valid_email_address));
            etEmailAddress.requestFocus();
            return;
        }

        if (password.length() < 8 || password.length() > 72) {
            etPassword.setError(getString(R.string.password_8_72_characters));
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.passwords_not_match));
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.matches("(?s).*\\p{L}.*")) {
            etPassword.setError(getString(R.string.password_one_letter));
            etPassword.requestFocus();
            return;
        }

        if (!password.matches("(?s).*\\d.*")) {
            etPassword.setError(getString(R.string.password_one_number));
            etPassword.requestFocus();
            return;
        }

        authViewModel.register(fullName, email, password);
    }

    private String getFieldText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
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

            btnCreateAccount.setOnClickListener(v -> submitCreateAccount());

            btnCreateGoogle.setOnClickListener(v -> {
                hideKeyboard(this);

                btnCreateGoogle.setEnabled(false);

                GoogleSignInHelper.signIn(ProfileCreateAccountActivity.this, new GoogleSignInHelper.Callback() {
                    @Override
                    public void onSuccess(String idToken, String email, String displayName) {
                        btnCreateGoogle.setEnabled(true);
                        authViewModel.authenticateWithGoogle(idToken);
                        Toast.makeText(ProfileCreateAccountActivity.this, getString(R.string.google_account_selected) + email, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        btnCreateGoogle.setEnabled(true);
                        Toast.makeText(ProfileCreateAccountActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            });

            tvLogin.setOnClickListener(v -> {
                startActivity(new Intent(ProfileCreateAccountActivity.this, ProfileLoginActivity.class));
                ActivityUtils.overrideOpenTransition(ProfileCreateAccountActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void setupObservers() {
        try {
            authViewModel.getLoading().observe(this, isLoading -> {
                boolean loading = Boolean.TRUE.equals(isLoading);
                screenLoader.setVisibility(loading ? View.VISIBLE : View.GONE);
                btnCreateAccount.setEnabled(!loading);
                btnCreateGoogle.setEnabled(!loading);
                btnCreateAccount.setText(loading ? R.string.creating_account : R.string.create_account);
            });

            authViewModel.getRegisterResponse().observe(this, response -> {
                if (response == null) {
                    return;
                }

                String message = response.getMessage() != null ? response.getMessage() : getString(R.string.check_email_verification_code);

                showAuthDialog(getString(R.string.check_email), message, R.drawable.ic_account,
                        getString(R.string.continue_pro),
                        () -> {
                            startActivity(new Intent(ProfileCreateAccountActivity.this, ProfileVerifyActivity.class)
                                    .putExtra("email", response.getData().getEmail()));
                            ActivityUtils.overrideOpenTransition(ProfileCreateAccountActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                        }
                );
            });

            authViewModel.getErrorMessageResId().observe(this, messageResId -> {
                if (messageResId == null) {
                    return;
                }

                showAuthDialog(getString(R.string.something_went_wrong), getString(messageResId), R.drawable.ic_account, getString(R.string.ok), null);
            });

            authViewModel.getErrorMessageText().observe(this, message -> {
                if (message == null || message.trim().isEmpty()) {
                    return;
                }

                showAuthDialog(getString(R.string.something_went_wrong), message, R.drawable.ic_account, getString(R.string.ok), null);
            });

            authViewModel.getGoogleAuthResponse().observe(this, response -> {
                if (response == null || navigationHandled || isFinishing()) {
                    return;
                }

                // Authentication succeeded and the session was saved.
                openSettings();
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupObservers", e);
        }
    }

    private void setupPasswordRequirementListeners() {
        etPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordRequirements(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Show the initial requirement state.
        updatePasswordRequirements("");
    }

    private void updatePasswordRequirements(String password) {
        boolean hasMinimumLength = password.length() >= 8;
        boolean hasLetter = password.matches("(?s).*\\p{L}.*");
        boolean hasNumber = password.matches("(?s).*\\d.*");
        updateRequirement(ivRequirementLength, tvRequirementLength, hasMinimumLength);
        updateRequirement(ivRequirementLetter, tvRequirementLetter, hasLetter);
        updateRequirement(ivRequirementNumber, tvRequirementNumber, hasNumber);
    }

    private void updateRequirement(AppCompatImageView icon, AppCompatTextView text, boolean satisfied) {
        int color = ContextCompat.getColor(this, satisfied ? R.color.primary : R.color.funky_grey);
        icon.setImageTintList(android.content.res.ColorStateList.valueOf(color));
        text.setTextColor(color);
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
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.90), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void openSettings() {
        if (navigationHandled) {
            return;
        }

        navigationHandled = true;

        Intent intent = new Intent(ProfileCreateAccountActivity.this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
        finish();
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(ProfileCreateAccountActivity.this, R.anim.scale_in, R.anim.right_to_left);
    }
}