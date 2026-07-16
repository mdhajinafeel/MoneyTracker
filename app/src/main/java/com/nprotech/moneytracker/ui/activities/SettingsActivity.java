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
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.angads25.toggle.widget.LabeledSwitch;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.enums.SettingType;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.models.SettingItemModel;
import com.nprotech.moneytracker.ui.adapters.SettingsRecyclerAdapter;
import com.nprotech.moneytracker.ui.common.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsActivity extends BaseActivity implements SettingsRecyclerAdapter.OnSettingActionListener {

    private RecyclerView rvConfigurations, rvManagement, rvBackup, rvOthers;
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
            rvBackup = findViewById(R.id.rvBackup);
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
            rvBackup.post(this::fetchBackupSettings);
            rvOthers.post(this::fetchOtherSettings);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void fetchManagementSettings() {
        try {
            List<SettingItemModel> settingItemModelList = new ArrayList<>();
            settingItemModelList.add(new SettingItemModel(SettingType.ACCOUNT, getString(R.string.account), false, true, false, null, true));
            settingItemModelList.add(new SettingItemModel(SettingType.WALLET, getString(R.string.wallet), false, true, false, null, true));
            settingItemModelList.add(new SettingItemModel(SettingType.CURRENCY, getString(R.string.currency), false, true, false, null, true));
            settingItemModelList.add(new SettingItemModel(SettingType.MANAGE_CATEGORY, getString(R.string.manage_category), false, true, false, null, true));

            SettingsRecyclerAdapter adapter = new SettingsRecyclerAdapter(settingItemModelList, this);

            rvManagement.setLayoutManager(new LinearLayoutManager(this));
            rvManagement.setAdapter(adapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchManagementSettings", e);
        }
    }

    private void fetchConfigurationSettings() {
        try {
            List<SettingItemModel> settingItemModelList = new ArrayList<>();
            SettingsRecyclerAdapter adapter = new SettingsRecyclerAdapter(settingItemModelList, this);
            settingItemModelList.add(new SettingItemModel(SettingType.WEEK_STARTS_ON, getString(R.string.week_starts_on), false, true, true, getString(R.string.sunday), true));
            settingItemModelList.add(new SettingItemModel(SettingType.STARTUP_SCREEN, getString(R.string.startup_screen), false, true, true, getString(R.string.transaction), true));
            settingItemModelList.add(new SettingItemModel(SettingType.LANGUAGE, getString(R.string.language), false, true, true, getString(R.string.system_default), true));
            settingItemModelList.add(new SettingItemModel(SettingType.PASSWORD, getString(R.string.password), false, true, true, getString(R.string.not_set), true));
            settingItemModelList.add(new SettingItemModel(SettingType.SMART_REMINDER, getString(R.string.smart_reminder), false, true, true, getString(R.string.trigger_reminder_at_19_00), true));

            rvConfigurations.setLayoutManager(new LinearLayoutManager(this));
            rvConfigurations.setAdapter(adapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchConfigurationSettings", e);
        }
    }

    private void fetchBackupSettings() {
        try {
            List<SettingItemModel> settingItemModelList = new ArrayList<>();
            settingItemModelList.add(new SettingItemModel(SettingType.MANAGE_BACKUP, getString(R.string.manage_backup), false, false, false, null, true));

            SettingsRecyclerAdapter adapter = new SettingsRecyclerAdapter(settingItemModelList, this);

            rvBackup.setLayoutManager(new LinearLayoutManager(this));
            rvBackup.setAdapter(adapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchOtherSettings", e);
        }
    }

    private void fetchOtherSettings() {
        try {
            List<SettingItemModel> settingItemModelList = new ArrayList<>();
            settingItemModelList.add(new SettingItemModel(SettingType.VERSION, getString(R.string.version), false, false, true, getAppVersion(), false));

            SettingsRecyclerAdapter adapter = new SettingsRecyclerAdapter(settingItemModelList, this);

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
        if (Objects.requireNonNull(item.settingType) == SettingType.MANAGE_CATEGORY) {
            Intent intent = new Intent(this, ManageCategoryActivity.class);
            intent.putExtra("amount", 0);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
            categoryLauncher.launch(intent, options);
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