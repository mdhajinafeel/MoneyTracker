package com.nprotech.moneytracker.ui.activities;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
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
import com.nprotech.moneytracker.utils.SimpleDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsActivity extends BaseActivity implements SettingsRecyclerAdapter.OnSettingActionListener {

    private RecyclerView rvConfigurations;

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

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top,
                        v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            icBack.setOnClickListener(view -> finish());
            tvTitle.setText(getString(R.string.settings));

            getOnBackPressedDispatcher().addCallback(
                    this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            finish();
                        }
                    });

            rvConfigurations.post(this::fetchSettings);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void fetchSettings() {
        try {
            List<SettingItemModel> settingItemModelList = new ArrayList<>();
            SettingsRecyclerAdapter adapter = new SettingsRecyclerAdapter(this, settingItemModelList, this);
            rvConfigurations.setLayoutManager(new LinearLayoutManager(this));
            rvConfigurations.addItemDecoration(new SimpleDividerItemDecoration(this));
            rvConfigurations.setAdapter(adapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchSettings", e);
        }
    }

    @Override
    public void onSwitchToggle(SettingItemModel item, boolean isChecked, LabeledSwitch switchButton) {

    }
}