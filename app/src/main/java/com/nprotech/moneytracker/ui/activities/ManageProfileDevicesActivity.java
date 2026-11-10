package com.nprotech.moneytracker.ui.activities;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.models.DeviceSessionModel;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.viewmodel.AuthViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManageProfileDevicesActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView tvLoaderTitle;
    private RecyclerView rvDevices;
    private MaterialButton btnRevokeOtherDevices;
    private AuthViewModel authViewModel;
    private View screenLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_manage_devices);
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
            rvDevices = findViewById(R.id.rvDevices);
            btnRevokeOtherDevices = findViewById(R.id.btnRevokeOtherDevices);
            tvLoaderTitle = findViewById(R.id.tvLoaderTitle);
            screenLoader = findViewById(R.id.screenLoader);

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

            tvTitle.setText(getString(R.string.manage_devices));
            authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

            initializeAdapters(new ArrayList<>());
            setupListeners();
            observeViewModel();
            loadDevices();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void initializeAdapters(List<DeviceSessionModel> devices) {
        try {
            RecyclerViewAdapter<DeviceSessionModel> deviceSessionAdapter = new RecyclerViewAdapter<>(this, devices != null ? devices : new ArrayList<>(),
                    R.layout.item_device_session) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, DeviceSessionModel device) {
                    AppCompatTextView tvDeviceName = holder.getView(R.id.tvDeviceName);
                    AppCompatTextView tvDeviceType = holder.getView(R.id.tvDeviceType);
                    AppCompatTextView tvLastActive = holder.getView(R.id.tvLastActive);
                    AppCompatTextView tvCurrentDevice = holder.getView(R.id.tvCurrentDevice);
                    MaterialButton btnRevokeDevice = holder.getView(R.id.btnRevokeDevice);

                    String deviceName = device.getDeviceName();
                    if (deviceName == null || deviceName.trim().isEmpty()) {
                        deviceName = holder.itemView.getContext().getString(R.string.unknown_device);
                    }
                    tvDeviceName.setText(deviceName);

                    String androidVersion = getAndroidVersion(device);
                    tvDeviceType.setText(androidVersion);

                    String lastActive = DateHelper.formatLastActive(device.getLastActiveAt());
                    tvLastActive.setText(holder.itemView.getContext().getString(R.string.last_active, lastActive == null ? "-" : lastActive));
                    tvCurrentDevice.setVisibility(device.isCurrent() ? View.VISIBLE : View.GONE);

                    btnRevokeDevice.setVisibility(device.isCurrent() ? View.GONE : View.VISIBLE);

                    btnRevokeDevice.setOnClickListener(v -> {
                        if (device.isCurrent()) {
                            return;
                        }

                        String selectedDeviceName = device.getDeviceName();
                        if (selectedDeviceName == null || selectedDeviceName.trim().isEmpty()) {
                            selectedDeviceName = getString(R.string.unknown_device);
                        }

                        showAuthDialog(
                                getString(R.string.revoke_device),
                                getString(R.string.revoke_device_confirmation, selectedDeviceName),
                                R.drawable.ic_devices,
                                getString(R.string.revoke),
                                () -> revokeDevice(device)
                        );
                    });
                }
            };

            rvDevices.setAdapter(deviceSessionAdapter);
            rvDevices.setLayoutManager(new LinearLayoutManager(this));
            rvDevices.setHasFixedSize(true);
            rvDevices.setItemAnimator(null);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    @NonNull
    private static String getAndroidVersion(DeviceSessionModel device) {
        String androidVersion = "";
        if(device.getPlatformVersion() != null) {
            androidVersion = device.getPlatformVersion().toUpperCase();
        }

        String appVersion = "";
        if(device.getAppVersion() != null && !device.getAppVersion().trim().isEmpty()) {
            appVersion = device.getAppVersion();
        }

        androidVersion = androidVersion + " - " + appVersion;
        return androidVersion;
    }

    private void observeViewModel() {

        authViewModel.getLoading().observe(this, isLoading -> {
            boolean loading = Boolean.TRUE.equals(isLoading);
            screenLoader.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnRevokeOtherDevices.setEnabled(!loading);
            if (isLoading) {
                tvLoaderTitle.setText(R.string.fetching_devices);
            }
        });

        authViewModel.getRevokeResponse().observe(this, response -> {
            if (response == null) return;
            if (response.isSuccess()) {
                Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
                loadDevices();
            } else {
                Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel.getRevokeOthersResponse().observe(this, response -> {
            if (response == null) return;
            if (response.isSuccess()) {
                Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
                loadDevices();
            } else {
                Toast.makeText(this, response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel.getDevicesResponse().observe(this, response -> {
            if (response == null) {
                return;
            }

            if (response.isSuccess()) {
                initializeAdapters(response.getDevices());
            } else {
                Toast.makeText(
                        this,
                        response.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> finishWithTransitions());

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finishWithTransitions();
                }
            });

            btnRevokeOtherDevices.setOnClickListener(v -> showAuthDialog(
                    getString(R.string.sign_out_other_devices),
                    getString(R.string.sign_out_other_devices_confirmation),
                    R.drawable.ic_devices,
                    getString(R.string.sign_out),
                    this::revokeOtherDevices
            ));
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void revokeDevice(DeviceSessionModel device) {
        if (device == null || device.isCurrent()) {
            return;
        }

        authViewModel.revokeDevice(device.getId());
    }

    private void revokeOtherDevices() {
        authViewModel.revokeOtherDevices();
    }

    private void loadDevices() {
        authViewModel.loadDevices();
    }

    private void showAuthDialog(String title, String message, int iconRes, String buttonText, Runnable onAction) {

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_auth_message, null);

        MaterialCardView cardHeader = dialogView.findViewById(R.id.cardHeader);
        AppCompatImageView ivDialogIcon = dialogView.findViewById(R.id.ivDialogIcon);
        AppCompatTextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        AppCompatTextView tvDialogMessage = dialogView.findViewById(R.id.tvDialogMessage);
        MaterialButton btnDialogAction = dialogView.findViewById(R.id.btnDialogAction);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        cardHeader.setCardBackgroundColor(getColor(R.color.budget_light));
        ivDialogIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.budget_dark)));
        tvDialogTitle.setText(title);
        tvDialogMessage.setText(message);
        ivDialogIcon.setImageResource(iconRes);
        btnDialogAction.setText(buttonText);

        btnDialogAction.setOnClickListener(v -> {
            dialog.dismiss();
            if (onAction != null) {
                onAction.run();
            }
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(ManageProfileDevicesActivity.this, R.anim.scale_in, R.anim.right_to_left);
    }
}