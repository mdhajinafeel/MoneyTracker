package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.angads25.toggle.widget.LabeledSwitch;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.models.SettingItemModel;
import com.nprotech.moneytracker.ui.adapters.SettingsRecyclerAdapter;
import com.nprotech.moneytracker.ui.common.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsActivity extends BaseActivity implements SettingsRecyclerAdapter.OnSettingActionListener {

    private RecyclerView rvConfigurations, rvManagement, rvOthers;
    private ActivityResultLauncher<Intent> categoryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            AppCompatImageView icBack = toolbarWrapper.findViewById(R.id.icBack);

            rvConfigurations = findViewById(R.id.rvConfigurations);
            rvManagement = findViewById(R.id.rvManagement);
            rvOthers = findViewById(R.id.rvOthers);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top,
                        v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            icBack.setOnClickListener(view -> finish());
            tvTitle.setText(getString(R.string.settings));

            setupLauncher();

            getOnBackPressedDispatcher().addCallback(
                    this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            finish();
                        }
                    });

            rvConfigurations.post(this::fetchConfigurationSettings);
            rvManagement.post(this::fetchManagementSettings);
            rvOthers.post(this::fetchOtherSettings);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void fetchConfigurationSettings() {
        try {
            List<SettingItemModel> settingItemModelList = new ArrayList<>();
            SettingsRecyclerAdapter adapter = new SettingsRecyclerAdapter(this, settingItemModelList, this);

            rvConfigurations.setLayoutManager(new LinearLayoutManager(this));
            rvConfigurations.setAdapter(adapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchConfigurationSettings", e);
        }
    }

    private void fetchManagementSettings() {
        try {
            List<SettingItemModel> settingItemModelList = new ArrayList<>();
            settingItemModelList.add(new SettingItemModel(1, getString(R.string.manage_category), false, true, false, null));

            SettingsRecyclerAdapter adapter = new SettingsRecyclerAdapter(this, settingItemModelList, this);

            rvManagement.setLayoutManager(new LinearLayoutManager(this));
            rvManagement.setAdapter(adapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchManagementSettings", e);
        }
    }

    private void fetchOtherSettings() {
        try {
            List<SettingItemModel> settingItemModelList = new ArrayList<>();
            settingItemModelList.add(new SettingItemModel(1, getString(R.string.version), false, false, true, getAppVersion()));

            SettingsRecyclerAdapter adapter = new SettingsRecyclerAdapter(this, settingItemModelList, this);

            rvOthers.setLayoutManager(new LinearLayoutManager(this));
            rvOthers.setAdapter(adapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchOtherSettings", e);
        }
    }

    @Override
    public void onSwitchToggle(SettingItemModel item, boolean isChecked, LabeledSwitch switchButton) {

    }

    @Override
    public void onSettingClick(SettingItemModel item) {
        if (item.settingId == 1) {
            Intent intent = new Intent(this, ManageCategoryActivity.class);
            intent.putExtra("amount", 0);
            categoryLauncher.launch(intent);
            overridePendingTransition(R.anim.left_to_right, R.anim.scale_out);
        }
    }

    private void setupLauncher() {
        categoryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // DO NOTHING
                });
    }

    private String getAppVersion() {
        String versionName = null;
        long versionCode = 0;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            versionName = pInfo.versionName;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = pInfo.getLongVersionCode();
            } else {
                versionCode = pInfo.versionCode;
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "getAppVersion", e);
        }

        return getString(R.string.app_version, versionName, versionCode);
    }
}