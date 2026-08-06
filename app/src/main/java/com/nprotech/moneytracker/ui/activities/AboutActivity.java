package com.nprotech.moneytracker.ui.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;

import java.util.Calendar;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AboutActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView tvAppVersion, tvAppInfoVersion, tvAppBuildNumber, tvCopyRightDesc, tvChoose, tvAppTagline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View rootView = findViewById(R.id.rootView);
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            tvAppVersion = findViewById(R.id.tvAppVersion);
            tvAppInfoVersion = findViewById(R.id.tvAppInfoVersion);
            tvAppBuildNumber = findViewById(R.id.tvAppBuildNumber);
            tvCopyRightDesc = findViewById(R.id.tvCopyRightDesc);
            tvChoose = findViewById(R.id.tvChoose);
            tvAppTagline = findViewById(R.id.tvAppTagline);

            tvTitle.setText(getString(R.string.about_app, getString(R.string.about), getString(R.string.app_name)));

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            bindData();
            setupListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {
            String copyright = getString(
                    R.string.copyright_desc, String.valueOf(Calendar.getInstance().get(Calendar.YEAR)), // %1$s
                    getString(R.string.app_name),                                                // %2$s
                    getString(R.string.app_name)                                                // %3$s
            );

            SpannableString spannable = new SpannableString(copyright + " ");

            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_heart_copyright);
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
                spannable.setSpan(imageSpan, spannable.length() - 1, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvCopyRightDesc.setText(spannable);
            }

            tvAppVersion.setText(getString(R.string.version_label, getVersionName()));
            tvAppInfoVersion.setText(getVersionName());
            tvAppBuildNumber.setText(String.valueOf(getBuildNumber()));
            tvChoose.setText(getString(R.string.why_choose, getString(R.string.app_name)));

            String text = getString(R.string.app_tagline);
            SpannableString spannableTag = new SpannableString(text);
            int expenseStart = text.indexOf(getString(R.string.app_tag_text1));
            int expenseEnd = expenseStart + getString(R.string.app_tag_text1).length();
            int insightStart = text.indexOf(getString(R.string.app_tag_text2));
            int insightEnd = insightStart + getString(R.string.app_tag_text2).length();
            int color = ContextCompat.getColor(this, R.color.primary);
            spannableTag.setSpan(new ForegroundColorSpan(color), expenseStart, expenseEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableTag.setSpan(new ForegroundColorSpan(color), insightStart, insightEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvAppTagline.setText(spannableTag);
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            });

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finish();
                    ActivityUtils.overrideCloseTransition(AboutActivity.this, R.anim.scale_in, R.anim.right_to_left);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private String getVersionName() {
        try {
            PackageInfo packageInfo;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageInfo = getPackageManager().getPackageInfo(
                        getPackageName(),
                        PackageManager.PackageInfoFlags.of(0)
                );
            } else {
                packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            }

            return packageInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            AppLogger.e(getClass(), "getVersionName", e);
            return "";
        }
    }

    @SuppressWarnings("deprecation")
    private long getBuildNumber() {
        try {
            PackageInfo packageInfo;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageInfo = getPackageManager().getPackageInfo(
                        getPackageName(),
                        PackageManager.PackageInfoFlags.of(0)
                );
            } else {
                packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return packageInfo.getLongVersionCode();
            } else {
                return packageInfo.versionCode;
            }

        } catch (PackageManager.NameNotFoundException e) {
            AppLogger.e(getClass(), "getBuildNumber", e);
            return 0;
        }
    }
}