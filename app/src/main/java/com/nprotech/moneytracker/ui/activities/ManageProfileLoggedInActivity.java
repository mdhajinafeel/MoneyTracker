package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.MoreOptionsModel;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.AuthViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManageProfileLoggedInActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private RecyclerView rvProfiles;
    private AppCompatTextView tvFullName, tvEmailAddress, tvLoaderTitle;
    private ShapeableImageView profilePhoto;
    private AuthViewModel authViewModel;
    private boolean logoutInProgress = false;
    private View screenLoader;
    private boolean navigationHandled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_loggedin);
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
            rvProfiles = findViewById(R.id.rvProfiles);
            tvFullName = findViewById(R.id.tvFullName);
            tvEmailAddress = findViewById(R.id.tvEmailAddress);
            profilePhoto = findViewById(R.id.profilePhoto);
            screenLoader = findViewById(R.id.screenLoader);
            tvLoaderTitle = findViewById(R.id.tvLoaderTitle);

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

            tvTitle.setText(getString(R.string.profile));

            authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

            bindData();
            initializeAdapters();
            setupListeners();
            setupLogoutObservers();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {
            tvFullName.setText(PreferenceManager.INSTANCE.getAuthFullName());
            tvEmailAddress.setText(PreferenceManager.INSTANCE.getAuthEmail());
            Glide.with(ManageProfileLoggedInActivity.this)
                    .load(PreferenceManager.INSTANCE.getAuthProfilePhoto())
                    .placeholder(R.drawable.ic_profile_avatar)
                    .error(R.drawable.ic_profile_avatar)
                    .circleCrop()
                    .into(profilePhoto);
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAdapters() {
        try {
            List<MoreOptionsModel> moreOptionsModels = new ArrayList<>();
            moreOptionsModels.add(new MoreOptionsModel(1, getString(R.string.account_details), getString(R.string.view_edit_information),
                    R.drawable.ic_account, R.color.primary_dark, R.color.light_lavender, 0));

            if (PreferenceManager.INSTANCE.getAuthHasPassword()) {
                moreOptionsModels.add(new MoreOptionsModel(2, getString(R.string.change_password), getString(R.string.update_account_password),
                        R.drawable.ic_password, R.color.primary_dark, R.color.light_lavender, 0));
            } else {
                moreOptionsModels.add(new MoreOptionsModel(2, getString(R.string.set_password), getString(R.string.set_password_subtitle),
                        R.drawable.ic_password, R.color.primary_dark, R.color.light_lavender, 0));
            }

            moreOptionsModels.add(new MoreOptionsModel(3, getString(R.string.manage_devices), getString(R.string.view_active_sessions),
                    R.drawable.ic_multiple_devices, R.color.primary_dark, R.color.light_lavender, 0));
            moreOptionsModels.add(new MoreOptionsModel(4, getString(R.string.logout), getString(R.string.sign_out_account),
                    R.drawable.ic_logout, R.color.dark_orange, R.color.orange, 0));

            RecyclerViewAdapter<MoreOptionsModel> moreOptionsAdapter = new RecyclerViewAdapter<>(ManageProfileLoggedInActivity.this,
                    moreOptionsModels, R.layout.item_more_options) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, MoreOptionsModel moreOptionsModel) {

                    AppCompatImageView imageView = holder.getView(R.id.imageView);
                    ConstraintLayout colorBadgeView = holder.getView(R.id.colorBadgeView);

                    holder.getView(R.id.colorView).setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ManageProfileLoggedInActivity.this,
                            moreOptionsModel.bgColor)));
                    imageView.setImageDrawable(ContextCompat.getDrawable(ManageProfileLoggedInActivity.this, moreOptionsModel.icon));
                    imageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(ManageProfileLoggedInActivity.this, moreOptionsModel.fgColor)));

                    holder.setViewText(R.id.tvSettingName, moreOptionsModel.title);
                    holder.setViewText(R.id.tvSettingDesc, moreOptionsModel.desc);

                    if (moreOptionsModel.count > 0) {
                        colorBadgeView.setVisibility(View.VISIBLE);
                        int bgColor = ContextCompat.getColor(ManageProfileLoggedInActivity.this, moreOptionsModel.bgColor);
                        int fgColor = ContextCompat.getColor(ManageProfileLoggedInActivity.this, moreOptionsModel.fgColor);
                        GradientDrawable badgeDrawable = (GradientDrawable) colorBadgeView.getBackground().mutate();
                        badgeDrawable.setColor(bgColor);
                        badgeDrawable.setStroke(CommonUtils.dpToPx(ManageProfileLoggedInActivity.this, 1), fgColor);
                        colorBadgeView.setBackground(badgeDrawable);
                        holder.setViewTextColor(R.id.tvBadgeCount, fgColor);
                        holder.setViewText(R.id.tvBadgeCount, String.valueOf(moreOptionsModel.count));
                    } else {
                        colorBadgeView.setVisibility(View.GONE);
                    }

                    if (holder.getBindingAdapterPosition() == getItemCount() - 1) {
                        holder.getView(R.id.divider).setAlpha(0f);
                    } else {
                        holder.getView(R.id.divider).setAlpha(1f);
                    }

                    holder.getView(R.id.itemView).setOnClickListener(v -> {
                        if (moreOptionsModel.id == 1) {
                            startActivity(new Intent(ManageProfileLoggedInActivity.this, ProfileAccountDetailActivity.class));
                            ActivityUtils.overrideOpenTransition(ManageProfileLoggedInActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                        } else if (moreOptionsModel.id == 2) {
                            startActivity(new Intent(ManageProfileLoggedInActivity.this, ProfileChangePasswordActivity.class)
                                    .putExtra("hasPassword", PreferenceManager.INSTANCE.getAuthHasPassword()));
                            ActivityUtils.overrideOpenTransition(ManageProfileLoggedInActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                        } else if (moreOptionsModel.id == 3) {
                            startActivity(new Intent(ManageProfileLoggedInActivity.this, ManageProfileDevicesActivity.class));
                            ActivityUtils.overrideOpenTransition(ManageProfileLoggedInActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                        } else if (moreOptionsModel.id == 4) {
                            showLogoutConfirmation();
                        }
                    });
                }
            };
            rvProfiles.setAdapter(moreOptionsAdapter);
            rvProfiles.setHasFixedSize(true);
            rvProfiles.setItemAnimator(null);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    private void showLogoutConfirmation() {

        showAuthDialog(getString(R.string.logout), getString(R.string.logout_confirmation_message, getString(R.string.app_name)), R.drawable.ic_logout,
                getString(R.string.logout), true,
                () -> {
                    if (!logoutInProgress) {
                        logoutInProgress = true;
                        authViewModel.logout();
                    }
                }
        );
    }

    private void setupLogoutObservers() {

        authViewModel.getLoading().observe(this, loading -> {
            boolean isLoading = Boolean.TRUE.equals(loading);
            screenLoader.setVisibility(loading ? View.VISIBLE : View.GONE);
            tvLoaderTitle.setText(R.string.logging_out);
            if (isLoading) {
                logoutInProgress = true;
            }
        });

        authViewModel.getLogoutResult().observe(this, result -> {

            if (result == null || isFinishing()) {
                return;
            }

            PreferenceManager.INSTANCE.clearAuthSession();

            showAuthDialog(getString(R.string.success),
                    result.getMessage() != null ? result.getMessage() : getString(R.string.logout_successful), R.drawable.ic_logout,
                    getString(R.string.ok), false, this::openSettings);
        });

        authViewModel.getErrorMessage().observe(this, message -> {

            if (message == null || message.trim().isEmpty() || isFinishing()) {
                return;
            }

            logoutInProgress = false;

            showAuthDialog(getString(R.string.something_went_wrong), message, R.drawable.ic_logout, getString(R.string.ok), false, null);
        });
    }

    private void openSettings() {
        if (navigationHandled) {
            return;
        }

        navigationHandled = true;

        Intent intent = new Intent(ManageProfileLoggedInActivity.this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
        finish();
    }

    private void showAuthDialog(String title, String message, int iconRes, String buttonText, boolean isConfirmation, Runnable onAction) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_auth_message, null);

        MaterialCardView cardHeader = dialogView.findViewById(R.id.cardHeader);
        AppCompatImageView ivDialogIcon = dialogView.findViewById(R.id.ivDialogIcon);
        AppCompatTextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        AppCompatTextView tvDialogMessage = dialogView.findViewById(R.id.tvDialogMessage);
        MaterialButton btnDialogAction = dialogView.findViewById(R.id.btnDialogAction);
        MaterialButton btnCancelAction = dialogView.findViewById(R.id.btnCancelAction);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        if(isConfirmation) {
            btnCancelAction.setVisibility(View.VISIBLE);
        } else {
            btnCancelAction.setVisibility(View.GONE);
        }

        cardHeader.setCardBackgroundColor(getColor(R.color.orange));
        ivDialogIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.dark_orange)));

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

        btnCancelAction.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.90), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
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
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(ManageProfileLoggedInActivity.this, R.anim.scale_in, R.anim.right_to_left);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindData();
    }
}