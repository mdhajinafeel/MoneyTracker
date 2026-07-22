package com.nprotech.moneytracker.ui.activities;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.CategoryEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.viewmodel.CategoryViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CategoryPickerActivity extends BaseActivity {

    private RecyclerView rvCategories;
    private ConstraintLayout emptyWrapper;

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

            tvTitle.setText(getString(R.string.select_category));
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(CategoryPickerActivity.this, R.anim.scale_in, R.anim.right_to_left);
            });

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

            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {

                CategoryViewModel categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

                categoryViewModel.incomeCategory(bundle.getInt("transactionType"));

                categoryViewModel.getIncomeCategories().observe(this, categoryEntities -> {
                    if (!categoryEntities.isEmpty()) {
                        bindCategories(categoryEntities);
                        rvCategories.setVisibility(View.VISIBLE);
                        emptyWrapper.setVisibility(View.GONE);
                    } else {
                        rvCategories.setVisibility(View.GONE);
                        emptyWrapper.setVisibility(View.VISIBLE);
                    }
                });
            }

            getOnBackPressedDispatcher().addCallback(this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            finish();
                            ActivityUtils.overrideCloseTransition(CategoryPickerActivity.this, R.anim.scale_in, R.anim.right_to_left);
                        }
                    });
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindCategories(List<CategoryEntity> expenseCategories) {
        try {
            RecyclerViewAdapter<CategoryEntity> expenseCategoryAdapter = new RecyclerViewAdapter<>(getApplicationContext(), expenseCategories, R.layout.item_category_picker) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, CategoryEntity categoryEntity) {
                    holder.setViewImageResource(R.id.ivCategory, DataHelper.getCategoryIcons().get(categoryEntity.icon));
                    holder.setViewText(R.id.tvCategory, categoryEntity.getName(getApplicationContext()));

                    if (Build.VERSION.SDK_INT >= 29) {
                        holder.getView(R.id.colorView).getBackground().setColorFilter(new BlendModeColorFilter(Color.parseColor(categoryEntity.color), BlendMode.SRC_OVER));
                    } else {

                        Drawable drawable = holder.getView(R.id.colorView).getBackground().mutate();
                        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_OVER);
                        DrawableCompat.setTint(drawable, Color.parseColor(categoryEntity.color));
                        holder.getView(R.id.colorView).setBackground(drawable);
                    }

                    holder.itemView.setOnClickListener(view -> {
                        Intent intent = new Intent();
                        intent.putExtra("category", categoryEntity);
                        setResult(-1, intent);
                        finish();
                        ActivityUtils.overrideCloseTransition(CategoryPickerActivity.this, R.anim.slide_in_left, R.anim.slide_out_right);
                    });
                }
            };

            rvCategories.setAdapter(expenseCategoryAdapter);
            rvCategories.setHasFixedSize(true);
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindCategories", e);
        }
    }
}