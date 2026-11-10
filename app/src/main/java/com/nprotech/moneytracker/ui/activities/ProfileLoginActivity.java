package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.GoogleSignInHelper;
import com.nprotech.moneytracker.viewmodel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileLoginActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView tvCreateAccount, tvForgotPassword, tvLoaderTitle;
    private TextInputEditText etEmailAddress, etPassword;
    private AppCompatCheckBox cbRememberMe;
    private MaterialButton btnLogin, btnLoginGoogle;
    private View screenLoader;
    private AuthViewModel authViewModel;
    private boolean navigationHandled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_login);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View root = findViewById(R.id.rootView);
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            AppCompatTextView tvLoginDesc = findViewById(R.id.tvLoginDesc);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            btnLogin = findViewById(R.id.btnLogin);
            btnLoginGoogle = findViewById(R.id.btnLoginGoogle);
            tvCreateAccount = findViewById(R.id.tvCreateAccount);
            cbRememberMe = findViewById(R.id.cbRememberMe);
            tvForgotPassword = findViewById(R.id.tvForgotPassword);
            etEmailAddress = findViewById(R.id.etEmailAddress);
            etPassword = findViewById(R.id.etPassword);
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

            if (getIntent().getBooleanExtra("session_revoked", false)) {
                Toast.makeText(this, R.string.session_been_revoked, Toast.LENGTH_SHORT).show();
                getIntent().removeExtra("session_revoked");
            }

            tvTitle.setText(getString(R.string.login));
            tvLoginDesc.setText(getString(R.string.login_account, getString(R.string.app_name)));

            loadRememberedEmail();
            prefillEmailFromIntent();

            setupListeners();
            setupObservers();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> handleBackNavigation());

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    handleBackNavigation();
                }
            });

            btnLogin.setOnClickListener(v -> submitLogin());

            btnLoginGoogle.setOnClickListener(v -> signInWithGoogle());

            cbRememberMe.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isChecked) {
                    clearRememberedEmail();
                }
            });

            tvForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileLoginActivity.this, ProfileForgotPasswordActivity.class);
                intent.putExtra("email", getFieldText(etEmailAddress));
                startActivity(intent);
                ActivityUtils.overrideOpenTransition(ProfileLoginActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            tvCreateAccount.setOnClickListener(v -> {
                startActivity(new Intent(ProfileLoginActivity.this, ProfileCreateAccountActivity.class));
                ActivityUtils.overrideOpenTransition(ProfileLoginActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void submitLogin() {
        try {
            hideKeyboard(this);

            String email = getFieldText(etEmailAddress);
            String password = etPassword.getText() == null ? "" : etPassword.getText().toString();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmailAddress.setError(getString(R.string.enter_valid_email_address));
                etEmailAddress.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError(getString(R.string.password));
                etPassword.requestFocus();
                return;
            }

            saveRememberedEmail(email);
            authViewModel.login(email, password);
        } catch (Exception e) {
            AppLogger.e(getClass(), "submitLogin", e);
        }
    }

    private void signInWithGoogle() {
        hideKeyboard(this);
        btnLoginGoogle.setEnabled(false);

        GoogleSignInHelper.signIn(ProfileLoginActivity.this, new GoogleSignInHelper.Callback() {
            @Override
            public void onSuccess(String idToken, String email, String displayName) {
                btnLoginGoogle.setEnabled(true);
                authViewModel.authenticateWithGoogle(idToken);
                Toast.makeText(ProfileLoginActivity.this, getString(R.string.google_account_selected) + email, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                btnLoginGoogle.setEnabled(true);
                Toast.makeText(ProfileLoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupObservers() {
        authViewModel.getLoading().observe(this, isLoading -> {
            boolean loading = Boolean.TRUE.equals(isLoading);
            tvLoaderTitle.setText(getString(R.string.verifying_login));
            if (screenLoader != null) {
                screenLoader.setVisibility(loading ? View.VISIBLE : View.GONE);
            }

            btnLogin.setEnabled(!loading);
            btnLoginGoogle.setEnabled(!loading);

            btnLogin.setText(loading ? R.string.logging_in : R.string.login);
        });

        authViewModel.getLoginResponse().observe(this, response -> {
            if (response == null || navigationHandled || isFinishing()) {
                return;
            }

            openSettings();
        });

        authViewModel.getGoogleAuthResponse().observe(this, response -> {
            if (response == null || navigationHandled || isFinishing()) {
                return;
            }

            openSettings();
        });

        authViewModel.getErrorMessageText().observe(this, message -> {
            if (message == null || message.trim().isEmpty() || isFinishing()) {
                return;
            }

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        authViewModel.getErrorMessageResId().observe(this, messageResId -> {
            if (messageResId == null || isFinishing()) {
                return;
            }

            Toast.makeText(this, getString(messageResId), Toast.LENGTH_SHORT).show();
        });
    }

    private void openSettings() {
        if (navigationHandled) {
            return;
        }

        navigationHandled = true;

        Intent intent = new Intent(ProfileLoginActivity.this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
        finish();
    }

    private String getFieldText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void prefillEmailFromIntent() {
        String email = getIntent().getStringExtra("email");

        if (email != null && !email.trim().isEmpty()) {
            etEmailAddress.setText(email.trim());
            etPassword.requestFocus();
        }
    }

    private void loadRememberedEmail() {
        boolean rememberEmail = PreferenceManager.INSTANCE.isRememberEmailEnabled();
        cbRememberMe.setChecked(rememberEmail);
        if (rememberEmail) {
            etEmailAddress.setText(PreferenceManager.INSTANCE.getSavedEmail());
        }
    }

    private void saveRememberedEmail(String email) {
        if (cbRememberMe.isChecked()) {
            PreferenceManager.INSTANCE.saveRememberedEmail(email);
        } else {
            PreferenceManager.INSTANCE.clearRememberedEmail();
        }
    }

    private void clearRememberedEmail() {
        PreferenceManager.INSTANCE.clearRememberedEmail();
    }

    private boolean isFromVerification() {
        return getIntent().getBooleanExtra("from_verification", false);
    }

    private void handleBackNavigation() {
        if (isFromVerification()) {
            openSettings();
        } else {
            finishWithTransitions();
        }
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(ProfileLoginActivity.this, R.anim.scale_in, R.anim.right_to_left);
    }
}