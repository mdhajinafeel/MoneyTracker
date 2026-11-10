package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
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
public class ProfileResetPasswordOtpActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView tvResend, tvLoaderTitle;
    private AppCompatEditText[] otpFields;
    private MaterialButton btnVerify;
    private AuthViewModel authViewModel;
    private View screenLoader;
    private String email;
    private CountDownTimer resendTimer;
    private static final long RESEND_TIMEOUT = 60_000L;
    private boolean isVerifying = false;
    private boolean isResending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_verify);
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
            tvResend = findViewById(R.id.tvResend);
            btnVerify = findViewById(R.id.btnVerify);
            screenLoader = findViewById(R.id.screenLoader);
            tvLoaderTitle = findViewById(R.id.tvLoaderTitle);
            otpFields = new AppCompatEditText[]{
                    findViewById(R.id.otp1),
                    findViewById(R.id.otp2),
                    findViewById(R.id.otp3),
                    findViewById(R.id.otp4),
                    findViewById(R.id.otp5),
                    findViewById(R.id.otp6)
            };

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

            tvTitle.setText(R.string.verify_email);

            authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
            email = getIntent().getStringExtra("email");
            setupOtpFields();
            setupListeners();
            observeViewModel();
            startResendTimer();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void setupOtpFields() {
        for (int i = 0; i < otpFields.length; i++) {
            final int position = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(
                        CharSequence s, int start, int before, int count) {

                    if (s.length() == 1 && position < otpFields.length - 1) {
                        otpFields[position + 1].requestFocus();
                    }

                    updateVerifyButton();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            otpFields[i].setOnKeyListener((v, keyCode, event) -> {

                if (keyCode == KeyEvent.KEYCODE_DEL
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && getFieldText(otpFields[position]).isEmpty()
                        && position > 0) {

                    otpFields[position - 1].requestFocus();
                    otpFields[position - 1].setText("");
                    return true;
                }

                return false;
            });
        }
    }

    private void setupListeners() {

        btnVerify.setOnClickListener(v -> verifyOtp());

        tvResend.setOnClickListener(v -> {
            if (resendTimer != null || isVerifying || isResending) {
                return;
            }

            resendOtp();
        });

        icBack.setOnClickListener(v -> finishWithTransitions());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishWithTransitions();
            }
        });
    }

    private void startResendTimer() {

        cancelResendTimer();
        tvResend.setEnabled(false);
        tvResend.setClickable(false);

        resendTimer = new CountDownTimer(RESEND_TIMEOUT, 1_000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = (millisUntilFinished + 999) / 1_000;
                tvResend.setText(getString(R.string.resend_in_seconds, seconds));
            }

            @Override
            public void onFinish() {

                tvResend.setText(R.string.text_resend);
                tvResend.setEnabled(true);
                tvResend.setClickable(true);

                resendTimer = null;
            }

        }.start();
    }

    private void cancelResendTimer() {
        if (resendTimer != null) {
            resendTimer.cancel();
            resendTimer = null;
        }
    }

    private void verifyOtp() {

        String otp = getEnteredOtp();

        if (otp.length() != 6) {
            Toast.makeText(this, R.string.complete_digit_code, Toast.LENGTH_SHORT).show();
            return;
        }

        if (isVerifying || isResending) return;

        isVerifying = true;
        authViewModel.verifyResetOtp(email, otp);
    }

    private void resendOtp() {

        if (isVerifying || isResending) return;

        isResending = true;
        authViewModel.forgotPassword(email);
    }

    private void observeViewModel() {
        authViewModel.getLoading().observe(this, loading -> {
            boolean isLoading = loading != null && loading;
            if(isResending) {
                tvLoaderTitle.setText(R.string.sending_otp);
            } else {
                tvLoaderTitle.setText(getString(R.string.verify_email));
            }

            screenLoader.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnVerify.setEnabled(!isLoading && getEnteredOtp().length() == 6);
            tvResend.setEnabled(!isLoading);
        });

        authViewModel.getPasswordResetResponse().observe(this, response -> {
            if (response == null || !response.isSuccess()) {
                return;
            }

            if (isResending) {
                isResending = false;
                clearOtpFields();
                startResendTimer();
                Toast.makeText(this, response.getMessage() != null ? response.getMessage() : getString(R.string.new_verification_code_sent),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (isVerifying) {
                String resetToken = response.getResetToken();
                if (resetToken == null || resetToken.trim().isEmpty()) {
                    isVerifying = false;
                    Toast.makeText(this, R.string.invalid_server_response, Toast.LENGTH_SHORT).show();
                    return;
                }

                isVerifying = false;
                Intent intent = new Intent(ProfileResetPasswordOtpActivity.this, ProfileResetPasswordActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("reset_token", resetToken);
                startActivity(intent);
                ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
                finish();
            }
        });


        authViewModel.getErrorMessageText().observe(this, error -> {
            if (error == null || error.trim().isEmpty()) return;
            isVerifying = false;
            isResending = false;
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    private String getEnteredOtp() {

        StringBuilder otp = new StringBuilder();

        for (AppCompatEditText field : otpFields) {
            otp.append(getFieldText(field));
        }

        return otp.toString();
    }

    private String getFieldText(AppCompatEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void clearOtpFields() {
        for (AppCompatEditText field : otpFields) {
            field.setText("");
        }

        otpFields[0].requestFocus();
        updateVerifyButton();
    }

    private void updateVerifyButton() {
        if (btnVerify != null) {
            btnVerify.setEnabled(getEnteredOtp().length() == 6);
        }
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(ProfileResetPasswordOtpActivity.this, R.anim.scale_in, R.anim.right_to_left);
    }

    @Override
    protected void onDestroy() {
        cancelResendTimer();
        super.onDestroy();
    }
}