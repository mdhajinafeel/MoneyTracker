package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.models.MoreOptionsModel;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManageProfileActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private RecyclerView rvProfiles;
    private MaterialButton btnLogin, btnCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_profile);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View root = findViewById(R.id.rootView);
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            AppCompatTextView tvHeader = findViewById(R.id.tvHeader);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            rvProfiles = findViewById(R.id.rvProfiles);
            btnLogin = findViewById(R.id.btnLogin);
            btnCreateAccount = findViewById(R.id.btnCreateAccount);

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
            tvHeader.setText(getString(R.string.sign_in_app, getString(R.string.app_name)));

            initializeAdapters();
            setupListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void initializeAdapters() {
        try {
            List<MoreOptionsModel> moreOptionsModels = new ArrayList<>();
            moreOptionsModels.add(new MoreOptionsModel(1, getString(R.string.sync_data), getString(R.string.keep_data_safe_accessible),
                    R.drawable.ic_cloud_upload, R.color.budget_dark, R.color.budget_light, 0));
            moreOptionsModels.add(new MoreOptionsModel(2, getString(R.string.access_premium_features), getString(R.string.unlock_advanced_tools_insights),
                    R.drawable.ic_access_premium, R.color.category_dark, R.color.category_light, 0));
            moreOptionsModels.add(new MoreOptionsModel(3, getString(R.string.use_across_devices), getString(R.string.access_profile_multiple_devices),
                    R.drawable.ic_multiple_devices, R.color.currency_dark, R.color.currency_light, 0));

            RecyclerViewAdapter<MoreOptionsModel> moreOptionsAdapter = new RecyclerViewAdapter<>(ManageProfileActivity.this,
                    moreOptionsModels, R.layout.item_more_options) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, MoreOptionsModel moreOptionsModel) {

                    AppCompatImageView imageView = holder.getView(R.id.imageView);
                    ConstraintLayout colorBadgeView = holder.getView(R.id.colorBadgeView);

                    holder.getView(R.id.ivNavigation).setVisibility(View.GONE);

                    holder.getView(R.id.colorView).setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ManageProfileActivity.this,
                            moreOptionsModel.bgColor)));
                    imageView.setImageDrawable(ContextCompat.getDrawable(ManageProfileActivity.this, moreOptionsModel.icon));
                    imageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(ManageProfileActivity.this, moreOptionsModel.fgColor)));

                    holder.setViewText(R.id.tvSettingName, moreOptionsModel.title);
                    holder.setViewText(R.id.tvSettingDesc, moreOptionsModel.desc);

                    if (moreOptionsModel.count > 0) {
                        colorBadgeView.setVisibility(View.VISIBLE);
                        int bgColor = ContextCompat.getColor(ManageProfileActivity.this, moreOptionsModel.bgColor);
                        int fgColor = ContextCompat.getColor(ManageProfileActivity.this, moreOptionsModel.fgColor);
                        GradientDrawable badgeDrawable = (GradientDrawable) colorBadgeView.getBackground().mutate();
                        badgeDrawable.setColor(bgColor);
                        badgeDrawable.setStroke(CommonUtils.dpToPx(ManageProfileActivity.this, 1), fgColor);
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
                }
            };
            rvProfiles.setAdapter(moreOptionsAdapter);
            rvProfiles.setHasFixedSize(true);
            rvProfiles.setItemAnimator(null);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
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

            btnLogin.setOnClickListener(v -> {
                startActivity(new Intent(ManageProfileActivity.this, ProfileLoginActivity.class));
                ActivityUtils.overrideOpenTransition(ManageProfileActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            btnCreateAccount.setOnClickListener(v -> {
                startActivity(new Intent(ManageProfileActivity.this, ProfileCreateAccountActivity.class));
                ActivityUtils.overrideOpenTransition(ManageProfileActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(ManageProfileActivity.this, R.anim.scale_in, R.anim.right_to_left);
    }
}