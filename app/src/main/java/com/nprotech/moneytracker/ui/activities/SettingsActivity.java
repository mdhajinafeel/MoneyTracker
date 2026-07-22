package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.IConstants;
import com.nprotech.moneytracker.db.entites.CommonDataEntity;
import com.nprotech.moneytracker.enums.SettingType;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.SettingItemModel;
import com.nprotech.moneytracker.ui.adapters.SettingOptionsAdapter;
import com.nprotech.moneytracker.ui.adapters.SettingsAdapter;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.viewmodel.CommonDataViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsActivity extends BaseActivity implements SettingsAdapter.OnSettingActionListener {

    private RecyclerView rvConfigurations, rvManagement, rvBackup, rvOthers;
    private ActivityResultLauncher<Intent> categoryLauncher;
    private CommonDataViewModel commonDataViewModel;
    private List<SettingItemModel> configurationList;
    private SettingsAdapter configurationAdapter;

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

            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(SettingsActivity.this, R.anim.scale_in, R.anim.bottom_to_top);
            });

            tvTitle.setText(getString(R.string.settings));

            commonDataViewModel = new ViewModelProvider(this).get(CommonDataViewModel.class);

            setupLauncher();

            getOnBackPressedDispatcher().addCallback(
                    this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            finish();
                            ActivityUtils.overrideCloseTransition(SettingsActivity.this, R.anim.scale_in, R.anim.bottom_to_top);
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
            settingItemModelList.add(new SettingItemModel(SettingType.ACCOUNT, getString(R.string.account), true, false, null, true, false));
            settingItemModelList.add(new SettingItemModel(SettingType.WALLET, getString(R.string.wallet), true, false, null, true, false));
            settingItemModelList.add(new SettingItemModel(SettingType.CURRENCY, getString(R.string.currency), true, false, null, true, false));
            settingItemModelList.add(new SettingItemModel(SettingType.MANAGE_CATEGORY, getString(R.string.manage_category), true, false, null, true, false));

            SettingsAdapter adapter = new SettingsAdapter(settingItemModelList, this);

            rvManagement.setLayoutManager(new LinearLayoutManager(this));
            rvManagement.setAdapter(adapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchManagementSettings", e);
        }
    }

    private void fetchConfigurationSettings() {
        try {
            configurationList = new ArrayList<>();
            configurationList.add(new SettingItemModel(SettingType.WEEK_STARTS_ON, getString(R.string.week_starts_on), true, true, getWeekStartSubtitle(), true, true));
            configurationList.add(new SettingItemModel(SettingType.STARTUP_SCREEN, getString(R.string.startup_screen), true, true, getStartupScreenSubtitle(), true, true));
            configurationList.add(new SettingItemModel(SettingType.LANGUAGE, getString(R.string.language), true, true, getLanguageSubtitle(), true, false));
            configurationList.add(new SettingItemModel(SettingType.PASSWORD, getString(R.string.password), true, true, getString(R.string.not_set), true, false));
            configurationList.add(new SettingItemModel(SettingType.SMART_REMINDER, getString(R.string.smart_reminder), true, true, getSmartReminderSubtitle(), true, true));

            configurationAdapter = new SettingsAdapter(configurationList, this);

            rvConfigurations.setLayoutManager(new LinearLayoutManager(this));
            rvConfigurations.setAdapter(configurationAdapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchConfigurationSettings", e);
        }
    }

    private void fetchBackupSettings() {
        try {
            List<SettingItemModel> settingItemModelList = new ArrayList<>();
            settingItemModelList.add(new SettingItemModel(SettingType.MANAGE_BACKUP, getString(R.string.manage_backup), true, false, null, true, false));

            SettingsAdapter adapter = new SettingsAdapter(settingItemModelList, this);

            rvBackup.setLayoutManager(new LinearLayoutManager(this));
            rvBackup.setAdapter(adapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchOtherSettings", e);
        }
    }

    private void fetchOtherSettings() {
        try {
            List<SettingItemModel> settingItemModelList = new ArrayList<>();
            settingItemModelList.add(new SettingItemModel(SettingType.VERSION, getString(R.string.version), false, true, getAppVersion(), false, false));

            SettingsAdapter adapter = new SettingsAdapter(settingItemModelList, this);

            rvOthers.setLayoutManager(new LinearLayoutManager(this));
            rvOthers.setAdapter(adapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchOtherSettings", e);
        }
    }

    private void fetchData(int type) {
        try {
            if (type == IConstants.DAY) {
                List<CommonDataEntity> daysList = commonDataViewModel.getDataByType(type);
                showSettingDialog(daysList, type);
            }

            if (type == IConstants.STARTUP_SCREEN) {
                List<CommonDataEntity> screenList = commonDataViewModel.getDataByType(type);
                showSettingDialog(screenList, type);
            }

            if (type == IConstants.LANGUAGE) {
                List<CommonDataEntity> languagesList = commonDataViewModel.getDataByType(type);
                showSettingDialog(languagesList, type);
            }

            if (type == IConstants.SMART_REMINDER) {
                List<CommonDataEntity> smartReminderList = commonDataViewModel.getDataByType(type);
                showSettingDialog(smartReminderList, type);
            }

        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchData", e);
        }
    }

    @Override
    public void onSettingClick(SettingItemModel item) {
        if (Objects.requireNonNull(item.settingType) == SettingType.MANAGE_CATEGORY) {
            Intent intent = new Intent(this, ManageCategoryActivity.class);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
            categoryLauncher.launch(intent, options);
        } else if (item.settingType == SettingType.WEEK_STARTS_ON) {
            fetchData(IConstants.DAY);
        } else if (item.settingType == SettingType.STARTUP_SCREEN) {
            fetchData(IConstants.STARTUP_SCREEN);
        } else if (item.settingType == SettingType.LANGUAGE) {
            fetchData(IConstants.LANGUAGE);
        } else if (item.settingType == SettingType.SMART_REMINDER) {
            fetchData(IConstants.SMART_REMINDER);
        }
    }

    private void showSettingDialog(List<CommonDataEntity> data, int type) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_setting_options, null, false);

        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        RecyclerView rvOptions = view.findViewById(R.id.rvOptions);
        ViewGroup.LayoutParams params = rvOptions.getLayoutParams();
        if (data.size() <= 5) {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else {
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        }

        rvOptions.setLayoutParams(params);
        rvOptions.setLayoutManager(new LinearLayoutManager(this));

        if (type == IConstants.DAY) {
            tvTitle.setText(getString(R.string.week_starts_on));
        } else if (type == IConstants.STARTUP_SCREEN) {
            tvTitle.setText(getString(R.string.startup_screen));
        } else if (type == IConstants.LANGUAGE) {
            tvTitle.setText(getString(R.string.language));
        } else if (type == IConstants.SMART_REMINDER) {
            tvTitle.setText(getString(R.string.smart_reminder));
        }

        SettingOptionsAdapter adapter = new SettingOptionsAdapter(data);
        rvOptions.setAdapter(adapter);

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvOk).setOnClickListener(v -> {
            CommonDataEntity selectedItem = adapter.getSelectedItem();
            if (selectedItem != null) {
                if (type == IConstants.DAY) {
                    PreferenceManager.INSTANCE.setWeekStartOn(selectedItem.value);
                    updateConfigurationSubtitle(SettingType.WEEK_STARTS_ON, getString(selectedItem.nameResId));
                } else if (type == IConstants.STARTUP_SCREEN) {
                    PreferenceManager.INSTANCE.setStartUpScreen(selectedItem.value);
                    updateConfigurationSubtitle(SettingType.STARTUP_SCREEN, getString(selectedItem.nameResId));
                } else if (type == IConstants.LANGUAGE) {
                    PreferenceManager.INSTANCE.setLanguage(selectedItem.value);
                    updateConfigurationSubtitle(SettingType.LANGUAGE, getString(selectedItem.nameResId));
                } else if (type == IConstants.SMART_REMINDER) {
                    PreferenceManager.INSTANCE.setSmartReminder(selectedItem.value);
                    if (selectedItem.value == 1) {
                        updateConfigurationSubtitle(SettingType.SMART_REMINDER, getString(selectedItem.nameResId));
                    } else {
                        updateConfigurationSubtitle(SettingType.SMART_REMINDER, getString(R.string.trigger_reminder_at, getString(selectedItem.nameResId)));
                    }
                }

                commonDataViewModel.updateSelectedData(selectedItem.type, selectedItem.id);
            }
            dialog.dismiss();
        });

        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void setupLauncher() {
        categoryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // DO NOTHING
                });
    }

    private void updateConfigurationSubtitle(SettingType settingType, String value) {
        for (int i = 0; i < configurationList.size(); i++) {
            SettingItemModel item = configurationList.get(i);
            if (item.settingType == settingType) {
                item.subTitle = value;
                configurationAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private String getWeekStartSubtitle() {
        List<CommonDataEntity> days = commonDataViewModel.getDataByType(IConstants.DAY);
        for (CommonDataEntity item : days) {
            if (item.value == PreferenceManager.INSTANCE.getWeekStartOn()) {
                return getString(item.nameResId);
            }
        }
        return getString(R.string.sunday);
    }

    private String getStartupScreenSubtitle() {
        List<CommonDataEntity> screens = commonDataViewModel.getDataByType(IConstants.STARTUP_SCREEN);
        for (CommonDataEntity item : screens) {
            if (item.value == PreferenceManager.INSTANCE.getStartUpScreen()) {
                return getString(item.nameResId);
            }
        }
        return getString(R.string.transaction);
    }

    private String getLanguageSubtitle() {
        List<CommonDataEntity> languages = commonDataViewModel.getDataByType(IConstants.LANGUAGE);
        for (CommonDataEntity item : languages) {
            if (item.value == PreferenceManager.INSTANCE.getLanguage()) {
                return getString(item.nameResId);
            }
        }
        return getString(R.string.system_default);
    }

    private String getSmartReminderSubtitle() {
        List<CommonDataEntity> smartReminders = commonDataViewModel.getDataByType(IConstants.SMART_REMINDER);
        for (CommonDataEntity item : smartReminders) {
            if (item.value == PreferenceManager.INSTANCE.getSmartReminder()) {
                if (item.value == 1) {
                    return getString(item.nameResId);
                } else {
                    return getString(R.string.trigger_reminder_at, getString(item.nameResId));
                }
            }
        }
        return getString(R.string.trigger_reminder_at, getString(R.string.time_0));
    }

    @SuppressWarnings("deprecation")
    private String getAppVersion() {
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

            String versionName = packageInfo.versionName;

            long versionCode;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = packageInfo.getLongVersionCode();
            } else {
                versionCode = packageInfo.versionCode;
            }

            return getString(R.string.app_version, versionName, versionCode);

        } catch (PackageManager.NameNotFoundException e) {
            AppLogger.e(getClass(), "getAppVersion", e);
            return "";
        }
    }
}