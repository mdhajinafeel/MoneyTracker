package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.enums.SettingType;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.models.SettingItemModel;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManageBackupActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private RecyclerView rvBackupOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_backup);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            View root = findViewById(R.id.rootView);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            rvBackupOptions = findViewById(R.id.rvBackupOptions);

            tvTitle.setText(getString(R.string.manage_backup));

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

            rvBackupOptions.post(this::fetchManageBackupOptions);
            setupListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void fetchManageBackupOptions() {
        try {
            List<SettingItemModel> backupList = new ArrayList<>();

            backupList.add(new SettingItemModel(SettingType.BACKUP_NOW, R.drawable.ic_backup, R.color.backup_dark, R.color.backup_light,
                    getString(R.string.backup_now), true, true, getString(R.string.create_new_backup), true, false));
            backupList.add(new SettingItemModel(SettingType.BACKUP_HISTORY, R.drawable.ic_backup_history, R.color.category_dark, R.color.category_light,
                    getString(R.string.backup_history), true, true, getString(R.string.view_manage_backups), true, false));
            backupList.add(new SettingItemModel(SettingType.RESTORE_BACKUP, R.drawable.ic_restore_backup, R.color.account_dark, R.color.account_light,
                    getString(R.string.restore_backup), true, true, getString(R.string.restore_data), true, false));
            backupList.add(new SettingItemModel(SettingType.BACKUP_SETTINGS, R.drawable.ic_settings_configuration, R.color.startup_dark, R.color.startup_light,
                    getString(R.string.backup_settings), true, true, getString(R.string.settings_preferences), true, false));

            RecyclerViewAdapter<SettingItemModel> backupRecyclerViewAdapter = new RecyclerViewAdapter<>(this, backupList, R.layout.item_manage_backup_option) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, SettingItemModel settingItemModel) {

                    AppCompatImageView ivIcon = holder.getView(R.id.ivIcon);

                    holder.setViewText(R.id.tvTitle, settingItemModel.title);
                    holder.setViewText(R.id.tvTitleDesc, settingItemModel.subTitle);

                    Drawable background = ivIcon.getBackground().mutate();
                    DrawableCompat.setTint(background, ContextCompat.getColor(getApplicationContext(), settingItemModel.bgColor));
                    ivIcon.setBackground(background);
                    ivIcon.setImageResource(settingItemModel.icon);
                    ivIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), settingItemModel.fgColor)));

                    holder.getView(R.id.itemView).setOnClickListener(v -> {
                        if (Objects.requireNonNull(settingItemModel.settingType) == SettingType.BACKUP_NOW) {
                            startActivity(new Intent(ManageBackupActivity.this, BackupNowActivity.class));
                            ActivityUtils.overrideOpenTransition(ManageBackupActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                        } else if (Objects.requireNonNull(settingItemModel.settingType) == SettingType.BACKUP_HISTORY) {
                            startActivity(new Intent(ManageBackupActivity.this, BackupHistoryActivity.class));
                            ActivityUtils.overrideOpenTransition(ManageBackupActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                        } else if (Objects.requireNonNull(settingItemModel.settingType) == SettingType.RESTORE_BACKUP) {
                            startActivity(new Intent(ManageBackupActivity.this, BackupRestoreActivity.class));
                            ActivityUtils.overrideOpenTransition(ManageBackupActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                        } else if (Objects.requireNonNull(settingItemModel.settingType) == SettingType.BACKUP_SETTINGS) {
                            startActivity(new Intent(ManageBackupActivity.this, ManageCurrencyActivity.class));
                            ActivityUtils.overrideOpenTransition(ManageBackupActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                        }
                    });
                }
            };

            rvBackupOptions.setLayoutManager(new LinearLayoutManager(this));
            rvBackupOptions.setAdapter(backupRecyclerViewAdapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchManageBackupOptions", e);
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(ManageBackupActivity.this, R.anim.scale_in, R.anim.right_to_left);
            });

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finish();
                    ActivityUtils.overrideCloseTransition(ManageBackupActivity.this, R.anim.scale_in, R.anim.right_to_left);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }
}