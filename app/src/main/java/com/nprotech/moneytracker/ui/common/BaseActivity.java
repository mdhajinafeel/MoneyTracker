package com.nprotech.moneytracker.ui.common;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.nprotech.moneytracker.R;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void statusBarDarkSetting() {

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Window window = getWindow();

        // Keep classic layout (content below status bar)
        WindowCompat.setDecorFitsSystemWindows(window, true);

        // Set status bar icon color
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(window, window.getDecorView());

        // false = light icons (white)
        // true = dark icons (black)
        controller.setAppearanceLightStatusBars(true);

        // Android 14 and below
        setStatusBarColorCompat(window);
    }

    @SuppressWarnings("deprecation")
    protected void statusBarSetting() {

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Window window = getWindow();

        WindowCompat.setDecorFitsSystemWindows(window, true);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());

        // White status bar icons
        controller.setAppearanceLightStatusBars(false);

        // White navigation bar icons
        controller.setAppearanceLightNavigationBars(false);

        setStatusBarColorCompat(window);

        // Navigation bar color
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.primary_dark)
        );
    }

    @SuppressWarnings("deprecation")
    private void setStatusBarColorCompat(Window window) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.setStatusBarColor(
                    ContextCompat.getColor(this, R.color.vibrant_orange)
            );
        }
    }

    public void hideKeyboard(Context ctx) {
        InputMethodManager inputManager =
                (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null) return;
        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}