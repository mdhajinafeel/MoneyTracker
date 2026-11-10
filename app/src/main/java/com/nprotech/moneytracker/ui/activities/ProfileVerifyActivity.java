package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

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
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.viewmodel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileVerifyActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView tvResend, tvLoaderTitle;
    private AppCompatEditText[] otpFields;
    private MaterialButton btnVerify;
    private AuthViewModel authViewModel;
    private View screenLoader;
    private String email;
    private boolean verificationSucceeded = false;
    private CountDownTimer resendTimer;
    private static final long RESEND_TIMEOUT = 60_000L;

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
            setupObservers();
            startResendTimer();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void setupOtpFields() {
        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;

            otpFields[i].setInputType(InputType.TYPE_CLASS_NUMBER);
            otpFields[i].setImeOptions(i == otpFields.length - 1 ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT);

            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if (s.length() > 1) {
                        String value = s.toString();
                        for (int j = 0; j < value.length() && index + j < otpFields.length; j++) {
                            otpFields[index + j].setText(String.valueOf(value.charAt(j)));
                        }
                        return;
                    }

                    if (s.length() == 1 && index < otpFields.length - 1) {
                        otpFields[index + 1].requestFocus();
                    } else if (s.length() == 0 && index > 0) {
                        otpFields[index - 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        otpFields[0].requestFocus();
    }

    private String getOtp() {
        StringBuilder builder = new StringBuilder();

        for (AppCompatEditText field : otpFields) {
            Editable text = field.getText();
            if (text == null || text.length() != 1) {
                return "";
            }
            builder.append(text);
        }

        return builder.toString();
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

            btnVerify.setOnClickListener(v -> submitVerification());

            tvResend.setOnClickListener(v -> {
                if (resendTimer != null) {
                    return;
                }

                showAuthDialog(getString(R.string.verify_email_resend_unavailable_title),
                        getString(R.string.verify_email_resend_unavailable), R.drawable.ic_email,
                        getString(R.string.ok), null);
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void submitVerification() {
        hideKeyboard(this);

        String otp = getOtp();

        if (email == null || email.trim().isEmpty()) {
            showAuthDialog(
                    getString(R.string.email_missing),
                    getString(R.string.registration_email_address),
                    R.drawable.ic_email,
                    getString(R.string.go_back),
                    this::finishWithTransitions
            );
            return;
        }

        if (otp.length() != 6) {
            showAuthDialog(
                    getString(R.string.invalid_code),
                    getString(R.string.enter_complete_six_code),
                    R.drawable.ic_email,
                    getString(R.string.ok),
                    null
            );
            return;
        }

        // Replace with the actual method name in your AuthViewModel.
        authViewModel.verifyEmail(email.trim(), otp);
    }

    private void setupObservers() {
        try {
            authViewModel.getLoading().observe(this, isLoading -> {
                boolean loading = Boolean.TRUE.equals(isLoading);
                tvLoaderTitle.setText(getString(R.string.verifying_account));
                screenLoader.setVisibility(loading ? View.VISIBLE : View.GONE);
                btnVerify.setEnabled(!loading);
                btnVerify.setText(loading ? R.string.verifying_account : R.string.verify);
            });

            authViewModel.getVerifyEmailResponse().observe(this, response -> {
                if (response == null || isFinishing()) {
                    return;
                }

                verificationSucceeded = true;

                String message = response.getMessage();

                showAuthDialog(getString(R.string.verify_email_success_title),
                        message == null || message.trim().isEmpty() ? getString(R.string.verify_email_success_message) : message,
                        R.drawable.ic_email,
                        getString(R.string.verify_email_sign_in),
                        this::openLogin);
            });

            authViewModel.getErrorMessageText().observe(this, message -> {
                if (message == null || message.trim().isEmpty() || verificationSucceeded || isFinishing()) {
                    return;
                }

                showAuthDialog(getString(R.string.verify_email_error_title),
                        message,
                        R.drawable.ic_email,
                        getString(R.string.ok),
                        null);
            });

            authViewModel.getErrorMessageResId().observe(this, messageResId -> {
                if (messageResId == null || verificationSucceeded || isFinishing()) {
                    return;
                }

                showAuthDialog(
                        getString(R.string.verify_email_error_title),
                        getString(messageResId),
                        R.drawable.ic_email,
                        getString(R.string.ok),
                        null
                );
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupObservers", e);
        }
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

    private void startResendTimer() {
        if (resendTimer != null) {
            resendTimer.cancel();
        }

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

    @Override
    protected void onDestroy() {
        cancelResendTimer();
        super.onDestroy();
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(ProfileVerifyActivity.this, R.anim.scale_in, R.anim.right_to_left);
    }

    private void openLogin() {
        Intent intent = new Intent(ProfileVerifyActivity.this, ProfileLoginActivity.class);
        intent.putExtra("from_verification", true);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
        ActivityUtils.overrideCloseTransition(ProfileVerifyActivity.this, R.anim.scale_in, R.anim.right_to_left);
    }
}