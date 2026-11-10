package com.nprotech.moneytracker.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.nprotech.moneytracker.BuildConfig;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.db.entites.CommonDataEntity;
import com.nprotech.moneytracker.enums.SettingType;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.BillingHelper;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.helper.SettingHelper;
import com.nprotech.moneytracker.models.PremiumFeatureModel;
import com.nprotech.moneytracker.models.SettingItemModel;
import com.nprotech.moneytracker.notifications.SmartReminderManager;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.SettingOptionsAdapter;
import com.nprotech.moneytracker.ui.adapters.SettingsAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
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

    private AppCompatImageView icBack;
    private AppCompatTextView tvPrice, tvUpgradeTitle, tvSubtitle, tvPricePeriod, tvBadgeActive;
    private RecyclerView rvConfigurations, rvSecurity, rvManagement, rvBackup, rvOthers;
    private MaterialButton btnUpgrade;
    private View featureWallet, featureCloud, featureOthers;
    private CommonDataViewModel commonDataViewModel;
    private List<SettingItemModel> configurationList;
    private SettingsAdapter configurationAdapter;
    private BillingHelper billingHelper;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

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
            View root = findViewById(R.id.rootView);
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            featureWallet = findViewById(R.id.featureWallet);
            featureCloud = findViewById(R.id.featureCloud);
            featureOthers = findViewById(R.id.featureOthers);
            tvPrice = findViewById(R.id.tvPrice);
            btnUpgrade = findViewById(R.id.btnUpgrade);
            tvUpgradeTitle = findViewById(R.id.tvUpgradeTitle);
            tvSubtitle = findViewById(R.id.tvSubtitle);
            tvPrice = findViewById(R.id.tvPrice);
            tvPricePeriod = findViewById(R.id.tvPricePeriod);
            tvBadgeActive = findViewById(R.id.tvBadgeActive);

            rvSecurity = findViewById(R.id.rvSecurity);
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

            tvTitle.setText(getString(R.string.settings));

            commonDataViewModel = new ViewModelProvider(this).get(CommonDataViewModel.class);

            bindData();
            updateProMembershipCard();
            setupListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {

            List<PremiumFeatureModel> features = Arrays.asList(
                    new PremiumFeatureModel(R.drawable.ic_wallet, getString(R.string.unlimited_wallets)),
                    new PremiumFeatureModel(R.drawable.ic_cloud, getString(R.string.cloud_backup)),
                    new PremiumFeatureModel(R.drawable.ic_settings_other, getString(R.string.more_features)));

            billingHelper = new BillingHelper(getApplicationContext(), new BillingHelper.BillingCallback() {
                @Override
                public void onPricesLoaded(String monthlyPrice, String annualPrice, long monthlyAmountMicros, long annualAmountMicros, String currencyCode) {
                    runOnUiThread(() -> tvPrice.setText(monthlyPrice));
                }

                @Override
                public void onPurchaseTokenReceived(String productId, String purchaseToken) {
                    // Call your existing backend verification method here.
                }

                @Override
                public void onPurchasePending() {
                    // Show your existing pending purchase message.
                }

                @Override
                public void onPurchaseCancelled() {
                    // Handle purchase cancellation.
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show());
                }
            });

            if (BuildConfig.DEBUG) {
                // TODO Local development: no Play Store subscription lookup
                tvPrice.setText(R.string.text_49);
            } else {
                billingHelper.connect();
            }

            bindFeature(featureWallet, features.get(0));
            bindFeature(featureCloud, features.get(1));
            bindFeature(featureOthers, features.get(2));

            rvConfigurations.post(this::fetchConfigurationSettings);
            rvSecurity.post(this::fetchAccountSettings);
            rvManagement.post(this::fetchManagementSettings);
            rvBackup.post(this::fetchBackupSettings);
            rvOthers.post(this::fetchOtherSettings);
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void bindFeature(View featureView, PremiumFeatureModel feature) {

        AppCompatImageView ivIcon = featureView.findViewById(R.id.ivIcon);
        AppCompatTextView tvFeature = featureView.findViewById(R.id.tvFeature);

        ivIcon.setImageResource(feature.icon());
        tvFeature.setText(feature.title());
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(SettingsActivity.this, R.anim.scale_in, R.anim.right_to_left);
            });

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finish();
                    ActivityUtils.overrideCloseTransition(SettingsActivity.this, R.anim.scale_in, R.anim.right_to_left);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void fetchAccountSettings() {
        try {
            List<SettingItemModel> accountList = new ArrayList<>();
            if(PreferenceManager.INSTANCE.isAuthenticated()) {
                accountList.add(new SettingItemModel(SettingType.LOGGED_IN_PROFILE, 0, 0, 0,
                        PreferenceManager.INSTANCE.getAuthFullName(), true, true,
                        PreferenceManager.INSTANCE.getAuthEmail(), true, false, 0));

                RecyclerViewAdapter<SettingItemModel> profileAdapter = new RecyclerViewAdapter<>(this, accountList, R.layout.item_profile_detail) {
                    @Override
                    public void onPostBindViewHolder(ViewHolder holder, SettingItemModel settingItemModel) {
                        ShapeableImageView profilePhoto = holder.getView(R.id.profilePhoto);
                        AppCompatTextView tvProfileName = holder.getView(R.id.tvProfileName);
                        AppCompatTextView tvEmailAddress = holder.getView(R.id.tvEmailAddress);
                        AppCompatImageView ivNavigation = holder.getView(R.id.ivNavigation);

                        Glide.with(SettingsActivity.this)
                                .load(PreferenceManager.INSTANCE.getAuthProfilePhoto())
                                .placeholder(R.drawable.ic_profile_avatar)
                                .error(R.drawable.ic_profile_avatar)
                                .circleCrop()
                                .into(profilePhoto);

                        tvProfileName.setText(settingItemModel.title);
                        tvEmailAddress.setText(settingItemModel.subTitle);

                        ivNavigation.setOnClickListener(v -> {
                            startActivity(new Intent(SettingsActivity.this, ManageProfileLoggedInActivity.class));
                            ActivityUtils.overrideOpenTransition(SettingsActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                        });

                        holder.getView(R.id.itemView).setOnClickListener(v -> {
                            startActivity(new Intent(SettingsActivity.this, ManageProfileLoggedInActivity.class));
                            ActivityUtils.overrideOpenTransition(SettingsActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                        });
                    }
                };

                rvSecurity.setLayoutManager(new LinearLayoutManager(this));
                rvSecurity.setAdapter(profileAdapter);
            } else {
                accountList.add(new SettingItemModel(SettingType.PROFILE, R.drawable.ic_account, R.color.color_violet_purple, R.color.color_lavendar,
                        getString(R.string.profile), true, true, getString(R.string.sign_create_account, getString(R.string.app_name)),
                        true, false, 0));

                SettingsAdapter adapter = new SettingsAdapter(this, accountList, R.layout.item_setting, this);

                rvSecurity.setLayoutManager(new LinearLayoutManager(this));
                rvSecurity.setAdapter(adapter);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchManagementSettings", e);
        }
    }

    private void fetchManagementSettings() {
        try {
            List<SettingItemModel> managementList = new ArrayList<>();
            managementList.add(new SettingItemModel(SettingType.ACCOUNT, R.drawable.ic_bank, R.color.account_dark, R.color.account_light,
                    getString(R.string.account), true, true, getString(R.string.manage_account_details), true, false, 0));
            managementList.add(new SettingItemModel(SettingType.WALLET, R.drawable.ic_account_wallet, R.color.wallet_dark, R.color.wallet_light,
                    getString(R.string.wallet), true, true, getString(R.string.manage_wallets), true, false, 0));
            managementList.add(new SettingItemModel(SettingType.CURRENCY, R.drawable.ic_exchange, R.color.currency_dark, R.color.currency_light,
                    getString(R.string.currency), true, true, getString(R.string.manage_currencies), true, false, 0));
            managementList.add(new SettingItemModel(SettingType.MANAGE_CATEGORY, R.drawable.ic_category, R.color.category_dark, R.color.category_light,
                    getString(R.string.manage_category), true, true, getString(R.string.manage_categories), true, false, 0));

            SettingsAdapter adapter = new SettingsAdapter(this, managementList, R.layout.item_setting, this);

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
                    getString(R.string.week_starts_on), true, true, getWeekStartSubtitle(), true, false, 0));
            configurationList.add(new SettingItemModel(SettingType.STARTUP_SCREEN, R.drawable.ic_startup, R.color.startup_dark, R.color.startup_light,
                    getString(R.string.startup_screen), true, true, getStartupScreenSubtitle(), true, false, 0));
            configurationList.add(new SettingItemModel(SettingType.LANGUAGE, R.drawable.ic_language, R.color.language_dark, R.color.language_light,
                    getString(R.string.language), true, true, getLanguageSubtitle(), true, false, 0));
            configurationList.add(new SettingItemModel(SettingType.PASSWORD, R.drawable.ic_password, R.color.password_dark, R.color.password_light,
                    getString(R.string.password), true, true, PreferenceManager.INSTANCE.isAppLockEnabled() ? getString(R.string.enabled) : getString(R.string.not_set), true, false, 0));
            configurationList.add(new SettingItemModel(SettingType.SMART_REMINDER, R.drawable.ic_reminder, R.color.reminder_dark, R.color.reminder_light,
                    getString(R.string.smart_reminder), true, true, getSmartReminderSubtitle(), true, false, 0));

            configurationAdapter = new SettingsAdapter(this, configurationList, R.layout.item_setting, this);

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
                    getString(R.string.manage_backup), true, true, getString(R.string.backup_restore_data), true, false, 0));

            SettingsAdapter adapter = new SettingsAdapter(this, settingItemModelList, R.layout.item_setting, this);

            rvBackup.setLayoutManager(new LinearLayoutManager(this));
            rvBackup.setAdapter(adapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchBackupSettings", e);
        }
    }

    private void fetchOtherSettings() {
        try {
            List<SettingItemModel> settingItemModelList = new ArrayList<>();
            settingItemModelList.add(new SettingItemModel(SettingType.RATE_APP, R.drawable.ic_rating, R.color.rate_dark, R.color.rate_light,
                    getString(R.string.rate_app) + " " + getString(R.string.app_name), true, true,
                    getString(R.string.rate_app_desc, getString(R.string.app_name)), true, false, 0));
            settingItemModelList.add(new SettingItemModel(SettingType.SHARE_APP, R.drawable.ic_share, R.color.rate_dark, R.color.share_light,
                    getString(R.string.share_app) + " " + getString(R.string.app_name), true, true, getString(R.string.share_app) + " " +
                    getString(R.string.app_name) + " " + getString(R.string.with_your_friends), true, false, 0));
            settingItemModelList.add(new SettingItemModel(SettingType.SEND_FEEDBACK, R.drawable.ic_feedback, R.color.feedback_dark, R.color.feedback_light,
                    getString(R.string.send_feedback), true, true, getString(R.string.help_us_improve) + getString(R.string.app_name), true, false, 0));
            settingItemModelList.add(new SettingItemModel(SettingType.PRIVACY_POLICY, R.drawable.ic_privacy, R.color.privacy_dark, R.color.privacy_light,
                    getString(R.string.privacy_policy), true, true, getString(R.string.read_privacy_policy), true, false, 0));
            settingItemModelList.add(new SettingItemModel(SettingType.TERMS_CONDITIONS, R.drawable.ic_terms, R.color.terms_dark, R.color.terms_light,
                    getString(R.string.terms_conditions), true, true, getString(R.string.read_our_terms_and_conditions), true, false, 0));
            settingItemModelList.add(new SettingItemModel(SettingType.ABOUT, R.drawable.ic_about, R.color.about_dark, R.color.about_light,
                    getString(R.string.about) + " " + getString(R.string.app_name), true, true, getString(R.string.learn_more) + getString(R.string.app_name), true, false, 0));
            settingItemModelList.add(new SettingItemModel(SettingType.VERSION, R.drawable.ic_version, R.color.version_dark, R.color.version_light,
                    getString(R.string.version), false, true, getAppVersion(), false, false, 0));

            SettingsAdapter adapter = new SettingsAdapter(this, settingItemModelList, R.layout.item_setting, this);

            rvOthers.setLayoutManager(new LinearLayoutManager(this));
            rvOthers.setAdapter(adapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchOtherSettings", e);
        }
    }

    private void fetchData(int type) {
        try {
            if (type == Constants.DAY) {
                List<CommonDataEntity> daysList = commonDataViewModel.getDataByType(type);
                showSettingDialog(daysList, type);
            }

            if (type == Constants.STARTUP_SCREEN) {
                List<CommonDataEntity> screenList = commonDataViewModel.getDataByType(type);
                showSettingDialog(screenList, type);
            }

            if (type == Constants.LANGUAGE) {
                List<CommonDataEntity> languagesList = commonDataViewModel.getDataByType(type);
                showSettingDialog(languagesList, type);
            }

            if (type == Constants.SMART_REMINDER) {
                List<CommonDataEntity> smartReminderList = commonDataViewModel.getDataByType(type);
                showSettingDialog(smartReminderList, type);
            }

        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchData", e);
        }
    }

    @Override
    public void onSettingClick(SettingItemModel item) {
        if (Objects.requireNonNull(item.settingType) == SettingType.PROFILE) {
            startActivity(new Intent(SettingsActivity.this, ManageProfileActivity.class));
            ActivityUtils.overrideOpenTransition(SettingsActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
        } else if (Objects.requireNonNull(item.settingType) == SettingType.ACCOUNT) {
            startActivity(new Intent(SettingsActivity.this, ManageAccountActivity.class));
            ActivityUtils.overrideOpenTransition(SettingsActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
        } else if (Objects.requireNonNull(item.settingType) == SettingType.WALLET) {
            startActivity(new Intent(SettingsActivity.this, ManageWalletActivity.class));
            ActivityUtils.overrideOpenTransition(SettingsActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
        } else if (Objects.requireNonNull(item.settingType) == SettingType.CURRENCY) {
            startActivity(new Intent(SettingsActivity.this, ManageCurrencyActivity.class));
            ActivityUtils.overrideOpenTransition(SettingsActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
        } else if (item.settingType == SettingType.MANAGE_CATEGORY) {
            startActivity(new Intent(SettingsActivity.this, ManageCategoryActivity.class));
            ActivityUtils.overrideOpenTransition(SettingsActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
        } else if (item.settingType == SettingType.WEEK_STARTS_ON) {
            fetchData(Constants.DAY);
        } else if (item.settingType == SettingType.STARTUP_SCREEN) {
            fetchData(Constants.STARTUP_SCREEN);
        } else if (item.settingType == SettingType.LANGUAGE) {
            fetchData(Constants.LANGUAGE);
        } else if (item.settingType == SettingType.PASSWORD) {
            startActivity(new Intent(SettingsActivity.this, PasswordSecurityActivity.class));
            ActivityUtils.overrideOpenTransition(SettingsActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
        } else if (item.settingType == SettingType.SMART_REMINDER) {
            fetchData(Constants.SMART_REMINDER);
        } else if (item.settingType == SettingType.MANAGE_BACKUP) {
            startActivity(new Intent(SettingsActivity.this, ManageBackupActivity.class));
            ActivityUtils.overrideOpenTransition(SettingsActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
        } else if (item.settingType == SettingType.RATE_APP) {
            SettingHelper.rateApp(this);
        } else if (item.settingType == SettingType.SHARE_APP) {
            SettingHelper.shareApp(this);
        } else if (item.settingType == SettingType.PRIVACY_POLICY) {
            SettingHelper.openLegalPage(this, BuildConfig.FIREBASE_URL + "privacy-policy.html");
        } else if (item.settingType == SettingType.TERMS_CONDITIONS) {
            SettingHelper.openLegalPage(this, BuildConfig.FIREBASE_URL + "terms-and-conditions.html");
        } else if (item.settingType == SettingType.ABOUT) {
            startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
            ActivityUtils.overrideOpenTransition(SettingsActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
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

        if (type == Constants.DAY) {
            tvTitle.setText(getString(R.string.week_starts_on));
        } else if (type == Constants.STARTUP_SCREEN) {
            tvTitle.setText(getString(R.string.startup_screen));
        } else if (type == Constants.LANGUAGE) {
            tvTitle.setText(getString(R.string.language));
        } else if (type == Constants.SMART_REMINDER) {
            tvTitle.setText(getString(R.string.smart_reminder));
        }

        SettingOptionsAdapter adapter = new SettingOptionsAdapter(data, this);
        rvOptions.setAdapter(adapter);

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvOk).setOnClickListener(v -> {
            CommonDataEntity selectedItem = adapter.getSelectedItem();
            if (selectedItem != null) {
                if (type == Constants.DAY) {
                    PreferenceManager.INSTANCE.setWeekStartOn(selectedItem.value);
                    updateConfigurationSubtitle(SettingType.WEEK_STARTS_ON, getString(DataHelper.getNameResId(selectedItem.type, selectedItem.value)));
                } else if (type == Constants.STARTUP_SCREEN) {
                    PreferenceManager.INSTANCE.setStartUpScreen(selectedItem.value);
                    updateConfigurationSubtitle(SettingType.STARTUP_SCREEN, getString(DataHelper.getNameResId(selectedItem.type, selectedItem.value)));
                } else if (type == Constants.LANGUAGE) {
                    PreferenceManager.INSTANCE.setLanguage(selectedItem.value);
                    String languageTag = getLanguageTag(selectedItem.languageCode);
                    applyAppLanguage(languageTag);
                    updateConfigurationSubtitle(SettingType.LANGUAGE, getString(DataHelper.getNameResId(selectedItem.type, selectedItem.value)));
                } else if (type == Constants.SMART_REMINDER) {
                    PreferenceManager.INSTANCE.setSmartReminder(selectedItem.value);
                    SmartReminderManager manager = new SmartReminderManager(this);
                    if (selectedItem.value == 1) {
                        manager.cancelWorker();
                        updateConfigurationSubtitle(SettingType.SMART_REMINDER, getString(DataHelper.getNameResId(selectedItem.type, selectedItem.value)));
                    } else {
                        requestNotificationPermission();

                        manager.createNotificationChannel();
                        manager.scheduleNextReminderWithWorker(selectedItem.value);
                        updateConfigurationSubtitle(SettingType.SMART_REMINDER, getString(R.string.trigger_reminder_at,
                                getString(DataHelper.getNameResId(selectedItem.type, selectedItem.value))));
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
        List<CommonDataEntity> days = commonDataViewModel.getDataByType(Constants.DAY);
        for (CommonDataEntity item : days) {
            if (item.value == PreferenceManager.INSTANCE.getWeekStartOn()) {
                return getString(DataHelper.getNameResId(item.type, item.value));
            }
        }
        return getString(R.string.sunday);
    }

    private String getStartupScreenSubtitle() {
        List<CommonDataEntity> screens = commonDataViewModel.getDataByType(Constants.STARTUP_SCREEN);
        for (CommonDataEntity item : screens) {
            if (item.value == PreferenceManager.INSTANCE.getStartUpScreen()) {
                return getString(DataHelper.getNameResId(item.type, item.value));
            }
        }
        return getString(R.string.transaction);
    }

    private String getLanguageSubtitle() {
        List<CommonDataEntity> languages = commonDataViewModel.getDataByType(Constants.LANGUAGE);
        for (CommonDataEntity item : languages) {
            if (item.value == PreferenceManager.INSTANCE.getLanguage()) {
                return getString(DataHelper.getNameResId(item.type, item.value));
            }
        }
        return getString(R.string.system_default);
    }

    private String getSmartReminderSubtitle() {
        List<CommonDataEntity> smartReminders = commonDataViewModel.getDataByType(Constants.SMART_REMINDER);
        for (CommonDataEntity item : smartReminders) {
            if (item.value == PreferenceManager.INSTANCE.getSmartReminder()) {
                if (item.value == 1) {
                    return getString(DataHelper.getNameResId(item.type, item.value)
                    );
                }
                return getString(R.string.trigger_reminder_at, getString(DataHelper.getNameResId(item.type, item.value)));
            }
        }
        return getString(R.string.not_set);
    }

    private void applyAppLanguage(String languageTag) {
        LocaleListCompat appLocales;
        if (languageTag == null || languageTag.isEmpty() || "system".equalsIgnoreCase(languageTag)) {
            appLocales = LocaleListCompat.getEmptyLocaleList();
        } else {
            appLocales = LocaleListCompat.forLanguageTags(languageTag);
        }
        AppCompatDelegate.setApplicationLocales(appLocales);
    }

    private String getLanguageTag(String languageCode) {
        if (languageCode == null || languageCode.isEmpty() || "system".equalsIgnoreCase(languageCode)) {
            return "";
        }

        return languageCode;
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

    // TODO prices
    private void updateProMembershipCard() {
        boolean isProActive = PreferenceManager.INSTANCE.isProEnabled();

        if (isProActive) {
            tvBadgeActive.setVisibility(View.VISIBLE);
            CommonUtils.setDrawable(SettingsActivity.this, tvBadgeActive, R.drawable.ic_complete, R.dimen.icon_12, R.color.dark_income, Gravity.START);
            tvUpgradeTitle.setText(getString(R.string.app_premium, getString(R.string.app_name)));
            tvSubtitle.setText(R.string.enjoy_premium_features);
            btnUpgrade.setText(R.string.text_manage);
            tvPrice.setText(PreferenceManager.INSTANCE.getProPrice());
            tvPricePeriod.setText(PreferenceManager.INSTANCE.getProPeriod());

            btnUpgrade.setOnClickListener(v -> {
                startActivity(new Intent(this, ManageSubscriptionActivity.class));
                ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
            });
        } else {
            tvBadgeActive.setVisibility(View.GONE);
            tvUpgradeTitle.setText(R.string.upgrade_to_premium);
            tvSubtitle.setText(R.string.upgrade_subtext);
            btnUpgrade.setText(R.string.upgrade);

            if (BuildConfig.DEBUG) {
                tvPrice.setText(getString(R.string.text_49));
                tvPricePeriod.setText(getString(R.string.month));
            } else {
                // Release: price will be filled by Google Play.
                tvPrice.setText("—");
                tvPricePeriod.setText(getString(R.string.month));
            }

            btnUpgrade.setOnClickListener(v -> {
                startActivity(new Intent(this, UpgradePremiumActivity.class));
                ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProMembershipCard();
        fetchAccountSettings();
        if (configurationList != null && configurationAdapter != null) {
            updateConfigurationSubtitle(SettingType.PASSWORD,
                    PreferenceManager.INSTANCE.isAppLockEnabled() ? getString(R.string.enabled) : getString(R.string.not_set));
        }
    }

    @Override
    protected void onDestroy() {
        if (billingHelper != null) {
            billingHelper.disconnect();
            billingHelper = null;
        }
        super.onDestroy();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }
    }
}