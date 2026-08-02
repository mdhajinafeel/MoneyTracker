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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.Insets;
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
import com.nprotech.moneytracker.helper.BillingHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.helper.SettingHelper;
import com.nprotech.moneytracker.models.PremiumFeatureModel;
import com.nprotech.moneytracker.models.SettingItemModel;
import com.nprotech.moneytracker.ui.adapters.SettingOptionsAdapter;
import com.nprotech.moneytracker.ui.adapters.SettingsAdapter;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.CommonDataViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsActivity extends BaseActivity implements SettingsAdapter.OnSettingActionListener {

    private AppCompatTextView tvPrice;
    private RecyclerView rvConfigurations, rvManagement, rvBackup, rvOthers;
    private CommonDataViewModel commonDataViewModel;
    private List<SettingItemModel> configurationList;
    private SettingsAdapter configurationAdapter;
    private BillingHelper billingHelper;

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
            View featureWallet = findViewById(R.id.featureWallet);
            View featureCloud = findViewById(R.id.featureCloud);
            View featureOthers = findViewById(R.id.featureOthers);
            View root = findViewById(R.id.rootView);
            tvPrice = findViewById(R.id.tvPrice);

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

            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(SettingsActivity.this, R.anim.scale_in, R.anim.right_to_left);
            });

            tvTitle.setText(getString(R.string.settings));

            commonDataViewModel = new ViewModelProvider(this).get(CommonDataViewModel.class);

            getOnBackPressedDispatcher().addCallback(
                    this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            finish();
                            ActivityUtils.overrideCloseTransition(SettingsActivity.this, R.anim.scale_in, R.anim.right_to_left);
                        }
                    });

            List<PremiumFeatureModel> features = Arrays.asList(
                    new PremiumFeatureModel(R.drawable.ic_wallet, "Unlimited\nWallets"),
                    new PremiumFeatureModel(R.drawable.ic_cloud, "Cloud\nBackup"),
                    new PremiumFeatureModel(R.drawable.ic_settings_other, "+12\nfeatures")
            );

            billingHelper = new BillingHelper(this);

            bindFeature(featureWallet, features.get(0));
            bindFeature(featureCloud, features.get(1));
            bindFeature(featureOthers, features.get(2));
            fetchProductRateMonthly();

            rvConfigurations.post(this::fetchConfigurationSettings);
            rvManagement.post(this::fetchManagementSettings);
            rvBackup.post(this::fetchBackupSettings);
            rvOthers.post(this::fetchOtherSettings);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindFeature(View featureView, PremiumFeatureModel feature) {

        AppCompatImageView ivIcon = featureView.findViewById(R.id.ivIcon);
        AppCompatTextView tvFeature = featureView.findViewById(R.id.tvFeature);

        ivIcon.setImageResource(feature.icon);
        tvFeature.setText(feature.title);
    }

    private void fetchManagementSettings() {
        try {
            List<SettingItemModel> managementList = new ArrayList<>();
            managementList.add(new SettingItemModel(SettingType.ACCOUNT, R.drawable.ic_account, R.color.account_dark, R.color.account_light,
                    getString(R.string.account), true, true, getString(R.string.manage_account_details), true, false));
            managementList.add(new SettingItemModel(SettingType.WALLET, R.drawable.ic_account_wallet, R.color.wallet_dark, R.color.wallet_light,
                    getString(R.string.wallet), true, true, getString(R.string.manage_wallets), true, false));
            managementList.add(new SettingItemModel(SettingType.CURRENCY, R.drawable.ic_exchange, R.color.currency_dark, R.color.currency_light,
                    getString(R.string.currency), true, true, getString(R.string.manage_currencies), true, false));
            managementList.add(new SettingItemModel(SettingType.MANAGE_CATEGORY, R.drawable.ic_category, R.color.category_dark, R.color.category_light,
                    getString(R.string.manage_category), true, true, getString(R.string.manage_categories), true, false));

            SettingsAdapter adapter = new SettingsAdapter(this, managementList, this);

            rvManagement.setLayoutManager(new LinearLayoutManager(this));
            rvManagement.setAdapter(adapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchManagementSettings", e);
        }
    }

    private void fetchConfigurationSettings() {
        try {
            configurationList = new ArrayList<>();
            configurationList.add(new SettingItemModel(SettingType.WEEK_STARTS_ON, R.drawable.ic_calendar, R.color.week_dark, R.color.week_light,
                    getString(R.string.week_starts_on), true, true, getWeekStartSubtitle(), true, true));
            configurationList.add(new SettingItemModel(SettingType.STARTUP_SCREEN, R.drawable.ic_startup, R.color.startup_dark, R.color.startup_light,
                    getString(R.string.startup_screen), true, true, getStartupScreenSubtitle(), true, true));
            configurationList.add(new SettingItemModel(SettingType.LANGUAGE, R.drawable.ic_language, R.color.language_dark, R.color.language_light,
                    getString(R.string.language), true, true, getLanguageSubtitle(), true, false));
            configurationList.add(new SettingItemModel(SettingType.PASSWORD, R.drawable.ic_password, R.color.password_dark, R.color.password_light,
                    getString(R.string.password), true, true, getString(R.string.not_set), true, false));
            configurationList.add(new SettingItemModel(SettingType.SMART_REMINDER, R.drawable.ic_reminder, R.color.reminder_dark, R.color.reminder_light,
                    getString(R.string.smart_reminder), true, true, getSmartReminderSubtitle(), true, true));

            configurationAdapter = new SettingsAdapter(this, configurationList, this);

            rvConfigurations.setLayoutManager(new LinearLayoutManager(this));
            rvConfigurations.setAdapter(configurationAdapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchConfigurationSettings", e);
        }
    }

    private void fetchBackupSettings() {
        try {
            List<SettingItemModel> settingItemModelList = new ArrayList<>();
            settingItemModelList.add(new SettingItemModel(SettingType.MANAGE_BACKUP, R.drawable.ic_settings_backup, R.color.backup_dark, R.color.backup_light,
                    getString(R.string.manage_backup), true, true, getString(R.string.backup_restore_data), true, false));

            SettingsAdapter adapter = new SettingsAdapter(this, settingItemModelList, this);

            rvBackup.setLayoutManager(new LinearLayoutManager(this));
            rvBackup.setAdapter(adapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchBackupSettings", e);
        }
    }

    private void fetchOtherSettings() {
        try {
            List<SettingItemModel> settingItemModelList = new ArrayList<>();
            settingItemModelList.add(new SettingItemModel(SettingType.RATE_APP, R.drawable.ic_rating, R.color.rate_dark, R.color.rate_light, getString(R.string.rate_app) + " " + getString(R.string.app_name), true, true, getString(R.string.rate_app_desc, getString(R.string.app_name)), true, false));
            settingItemModelList.add(new SettingItemModel(SettingType.SHARE_APP, R.drawable.ic_share, R.color.rate_dark, R.color.share_light, getString(R.string.share_app) + " " + getString(R.string.app_name), true, true, getString(R.string.share_app) + " " + getString(R.string.app_name) + " " + getString(R.string.with_your_friends), true, false));
            settingItemModelList.add(new SettingItemModel(SettingType.SEND_FEEDBACK, R.drawable.ic_feedback, R.color.feedback_dark, R.color.feedback_light, getString(R.string.send_feedback), true, true, getString(R.string.help_us_improve) + getString(R.string.app_name), true, false));
            settingItemModelList.add(new SettingItemModel(SettingType.PRIVACY_POLICY, R.drawable.ic_privacy, R.color.privacy_dark, R.color.privacy_light, getString(R.string.privacy_policy), true, true, getString(R.string.read_privacy_policy), true, false));
            settingItemModelList.add(new SettingItemModel(SettingType.TERMS_CONDITIONS, R.drawable.ic_terms, R.color.terms_dark, R.color.terms_light, getString(R.string.terms_conditions), true, true, getString(R.string.read_our_terms_and_conditions), true, false));
            settingItemModelList.add(new SettingItemModel(SettingType.ABOUT, R.drawable.ic_about, R.color.about_dark, R.color.about_light, getString(R.string.about) + " " + getString(R.string.app_name), true, true, getString(R.string.learn_more) + getString(R.string.app_name), true, false));
            settingItemModelList.add(new SettingItemModel(SettingType.VERSION, R.drawable.ic_version, R.color.version_dark, R.color.version_light, getString(R.string.version), false, true, getAppVersion(), false, false));

            SettingsAdapter adapter = new SettingsAdapter(this, settingItemModelList, this);

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
        if (Objects.requireNonNull(item.settingType) == SettingType.CURRENCY) {
            startActivity(new Intent(SettingsActivity.this, ManageCurrencyActivity.class));
            ActivityUtils.overrideOpenTransition(SettingsActivity.this, R.anim.left_to_right, R.anim.scale_out);
        } else if (item.settingType == SettingType.MANAGE_CATEGORY) {
            startActivity(new Intent(SettingsActivity.this, ManageCategoryActivity.class));
            ActivityUtils.overrideOpenTransition(SettingsActivity.this, R.anim.left_to_right, R.anim.scale_out);
        } else if (item.settingType == SettingType.WEEK_STARTS_ON) {
            fetchData(IConstants.DAY);
        } else if (item.settingType == SettingType.STARTUP_SCREEN) {
            fetchData(IConstants.STARTUP_SCREEN);
        } else if (item.settingType == SettingType.LANGUAGE) {
            fetchData(IConstants.LANGUAGE);
        } else if (item.settingType == SettingType.SMART_REMINDER) {
            fetchData(IConstants.SMART_REMINDER);
        } else if (item.settingType == SettingType.MANAGE_BACKUP) {
            startActivity(new Intent(SettingsActivity.this, ManageBackupActivity.class));
            ActivityUtils.overrideOpenTransition(SettingsActivity.this, R.anim.left_to_right, R.anim.scale_out);
        } else if (item.settingType == SettingType.RATE_APP) {
            SettingHelper.rateApp(this);
        } else if (item.settingType == SettingType.SHARE_APP) {
            SettingHelper.shareApp(this);
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

    private void fetchProductRateMonthly() {
        try {
            billingHelper.loadSubscriptionPrice(IConstants.SUBSCRIPTION_MONTHLY, new BillingHelper.PriceListener() {

                @Override
                public void onPriceLoaded(String price) {
                    runOnUiThread(() -> tvPrice.setText(price));
                }

                @Override
                public void onError() {
                    // TODO
                    runOnUiThread(() -> tvPrice.setText(CommonUtils.getBeautifyAmount("$", 49)));
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchProductRateMonthly", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        billingHelper.destroy();
    }
}