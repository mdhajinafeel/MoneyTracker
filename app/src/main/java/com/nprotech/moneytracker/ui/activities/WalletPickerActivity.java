package com.nprotech.moneytracker.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WalletPickerActivity extends BaseActivity {

    private AppCompatTextView tvSave;
    private RecyclerView rvCategories;
    private ConstraintLayout emptyWrapper;
    private String color;
    private int icon, selectedPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_picker);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            AppCompatImageView icBack = toolbarWrapper.findViewById(R.id.icBack);
            tvSave = toolbarWrapper.findViewById(R.id.tvSave);

            tvTitle.setText(getString(R.string.select_icon));
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.bottom_to_top);
            });
            tvSave.setVisibility(View.VISIBLE);
            tvSave.setText(getString(R.string.done));

            emptyWrapper = findViewById(R.id.emptyWrapper);
            rvCategories = findViewById(R.id.rvCategories);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(emptyWrapper, (view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(rvCategories, (view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            getOnBackPressedDispatcher().addCallback(this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            finish();
                            ActivityUtils.overrideCloseTransition(WalletPickerActivity.this, R.anim.scale_in, R.anim.bottom_to_top);
                        }
                    });

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                color = bundle.getString("selectedColor");
                icon = bundle.getInt("selectedWalletIcon");

                selectedPosition = icon;
                setUpListeners();
                bindWalletIcons();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void setUpListeners() {
        try {
            tvSave.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.putExtra("walletIcon", icon);
                setResult(-1, intent);
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.slide_in_left, R.anim.slide_out_right);
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setUpListeners", e);
        }
    }

    private void bindWalletIcons() {
        try {

            List<Integer> walletIcons = DataHelper.getWalletIcons();
            if (walletIcons.isEmpty()) {

                rvCategories.setVisibility(View.GONE);
                emptyWrapper.setVisibility(View.VISIBLE);

            } else {

                RecyclerViewAdapter<Integer> walletIconAdapter = new RecyclerViewAdapter<>(WalletPickerActivity.this, walletIcons, R.layout.item_icon_picker) {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onPostBindViewHolder(ViewHolder holder, Integer wallets) {

                        ConstraintLayout wrapper = holder.getView(R.id.wrapper);
                        AppCompatImageView imageView = holder.getView(R.id.imageView);

                        imageView.setImageResource(wallets);

                        int position = holder.getBindingAdapterPosition();
                        if (position == selectedPosition) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                wrapper.getBackground().setColorFilter(
                                        new BlendModeColorFilter(Color.parseColor(color), BlendMode.SRC_OVER));
                                imageView.setColorFilter(
                                        new BlendModeColorFilter(Color.parseColor("#F8F8F8"), BlendMode.SRC_IN));
                            } else {
                                Drawable drawable = wrapper.getBackground().mutate();
                                DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_OVER);
                                DrawableCompat.setTint(drawable, Color.parseColor(color));
                                wrapper.setBackground(drawable);

                                imageView.setColorFilter(
                                        Color.parseColor("#F8F8F8"), PorterDuff.Mode.SRC_IN);
                            }

                        } else {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                wrapper.getBackground().setColorFilter(
                                        new BlendModeColorFilter(Color.parseColor("#E0E0E0"), BlendMode.SRC_OVER));
                                imageView.setColorFilter(
                                        new BlendModeColorFilter(Color.parseColor("#262525"), BlendMode.SRC_IN));
                            } else {
                                Drawable drawable = wrapper.getBackground().mutate();
                                DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_OVER);
                                DrawableCompat.setTint(drawable, Color.parseColor("#E0E0E0"));
                                wrapper.setBackground(drawable);

                                imageView.setColorFilter(
                                        Color.parseColor("#262525"), PorterDuff.Mode.SRC_IN);
                            }
                        }

                        holder.itemView.setOnClickListener(v -> {
                            selectedPosition = holder.getBindingAdapterPosition();
                            icon = selectedPosition;
                            notifyDataSetChanged();
                        });
                    }
                };

                rvCategories.setLayoutManager(new GridLayoutManager(this, 4));
                rvCategories.setAdapter(walletIconAdapter);
                rvCategories.setHasFixedSize(true);
                rvCategories.setVisibility(View.VISIBLE);
                emptyWrapper.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindWalletIcons", e);
        }
    }
}