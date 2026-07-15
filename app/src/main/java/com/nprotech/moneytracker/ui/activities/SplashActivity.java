package com.nprotech.moneytracker.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;

import dagger.hilt.android.AndroidEntryPoint;

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
public class SplashActivity extends BaseActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private static final int INTENT_FLAGS = Intent.FLAG_ACTIVITY_NEW_TASK
            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            | Intent.FLAG_ACTIVITY_CLEAR_TOP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        hideKeyboard(this);

        // Delay and move to next activity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            boolean isAccountExists = PreferenceManager.INSTANCE.getAccountId() > 0;
            if (isAccountExists) {
                redirectToMain();
            } else {
                redirectToCreateAccount();
            }
        }, SPLASH_DELAY);
    }

    private void redirectToMain() {
        startActivity(new Intent(this, MainActivity.class)
                .putExtra("isFromLogin", false)
                .addFlags(INTENT_FLAGS));
        ActivityUtils.overrideOpenTransition(this, android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void redirectToCreateAccount() {
        startActivity(new Intent(this, AddAccountActivity.class)
                .addFlags(INTENT_FLAGS));
        ActivityUtils.overrideOpenTransition(this, android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}