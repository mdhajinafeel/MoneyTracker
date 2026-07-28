package com.nprotech.moneytracker.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;

import dagger.hilt.android.AndroidEntryPoint;

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
public class SplashActivity extends BaseActivity {

    private AppCompatImageView imgLogo;
    private AppCompatTextView tvAppName, tvAppTagline;
    private static final int SPLASH_DELAY = 1500; // 1 second
    private static final int INTENT_FLAGS = Intent.FLAG_ACTIVITY_NEW_TASK
            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            | Intent.FLAG_ACTIVITY_CLEAR_TOP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        imgLogo = findViewById(R.id.imgLogo);
        tvAppName = findViewById(R.id.tvAppName);
        tvAppTagline = findViewById(R.id.tvAppTagline);

        statusBarDarkSetting();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        hideKeyboard(this);

        startSplashAnimation();

        // Delay and move to next activity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            if (!isOnboardingCompleted()) {
                redirectToOnboarding();
                return;
            }

            boolean isAccountExists = PreferenceManager.INSTANCE.getAccountId() > 0;

            if (isAccountExists) {
                redirectToMain();
            } else {
                redirectToCreateAccount();
            }

        }, SPLASH_DELAY);
    }

    private void startSplashAnimation() {

        imgLogo.setAlpha(0f);
        imgLogo.setScaleX(0.90f);
        imgLogo.setScaleY(0.90f);

        tvAppName.setAlpha(0f);
        tvAppTagline.setAlpha(0f);

        imgLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .withEndAction(() ->
                        imgLogo.animate()
                                .translationY(-6f)
                                .setDuration(350)
                                .withEndAction(() ->
                                        imgLogo.animate()
                                                .translationY(0)
                                                .setDuration(350)
                                                .start())
                                .start())
                .start();

        tvAppName.postDelayed(() ->
                tvAppName.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start(), 250);

        tvAppTagline.postDelayed(() ->
                tvAppTagline.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start(), 250);
    }

    private void redirectToMain() {
        startActivity(new Intent(this, MainActivity.class)
                .putExtra("isFromLogin", false)
                .addFlags(INTENT_FLAGS));
        ActivityUtils.overrideOpenTransition(this, android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void redirectToCreateAccount() {
        startActivity(new Intent(this, AddAccountActivity.class)
                .addFlags(INTENT_FLAGS));
        ActivityUtils.overrideOpenTransition(this, android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void redirectToOnboarding() {
        startActivity(new Intent(this, OnboardingActivity.class)
                .addFlags(INTENT_FLAGS));
        ActivityUtils.overrideOpenTransition(this, android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private boolean isOnboardingCompleted() {
        return PreferenceManager.INSTANCE.getOnBoardingCompleted();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}