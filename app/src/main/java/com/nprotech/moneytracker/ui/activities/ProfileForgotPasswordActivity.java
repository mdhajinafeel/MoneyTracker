package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.viewmodel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileForgotPasswordActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView tvLoaderTitle;
    private TextInputEditText etEmailAddress;
    private MaterialButton btnContinue;
    private View screenLoader;
    private AuthViewModel authViewModel;
    private boolean navigationHandled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_forgot_password);
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
            etEmailAddress = findViewById(R.id.etEmailAddress);
            btnContinue = findViewById(R.id.btnContinue);
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

            tvTitle.setText(getString(R.string.forgot_password_title));

            authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
            String email = getIntent().getStringExtra("email");

            if (email != null && !email.trim().isEmpty()) {
                etEmailAddress.setText(email);
            }

            setupListeners();
            setupObservers();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void setupListeners() {

        icBack.setOnClickListener(v -> finishWithTransitions());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishWithTransitions();
            }
        });

        btnContinue.setOnClickListener(v -> submitEmail());
    }

    private void submitEmail() {
        hideKeyboard(this);
        String email = getFieldText(etEmailAddress);

        if (email.isEmpty()) {
            etEmailAddress.setError(getString(R.string.enter_valid_email_address));
            etEmailAddress.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmailAddress.setError(getString(R.string.enter_valid_email_address));
            etEmailAddress.requestFocus();
            return;
        }

        authViewModel.forgotPassword(email);
    }

    private void setupObservers() {
        authViewModel.getLoading().observe(this, isLoading -> {
            boolean loading = Boolean.TRUE.equals(isLoading);
            tvLoaderTitle.setText(getString(R.string.sending));
            screenLoader.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnContinue.setEnabled(!loading);
            btnContinue.setText(loading ? R.string.sending : R.string.continue_pro);
        });

        authViewModel.getPasswordResetResponse().observe(this, response -> {
            if (response == null || navigationHandled || isFinishing()) {
                return;
            }
            navigationHandled = true;
            String email = getFieldText(etEmailAddress);
            Intent intent = new Intent(ProfileForgotPasswordActivity.this, ProfileResetPasswordOtpActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
            ActivityUtils.overrideOpenTransition(ProfileForgotPasswordActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
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

    private String getFieldText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
    }
}