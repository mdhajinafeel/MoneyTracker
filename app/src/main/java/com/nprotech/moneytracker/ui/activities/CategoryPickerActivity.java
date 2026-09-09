package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.CategoryEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.ui.common.MaxHeightRecyclerView;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.CategoryViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CategoryPickerActivity extends BaseActivity {

    private MaxHeightRecyclerView rvCategories;
    private ConstraintLayout emptyWrapper;
    private View categoryRoot;
    private int categoryId = -1;
    private String isFromScreen = "";
    private final Set<Integer> selectedCategoryIds = new HashSet<>();
    private Set<Integer> tempSelectedCategoryIds = new HashSet<>();
    private Typeface medium, semiBold;

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
            AppCompatTextView tvSave = toolbarWrapper.findViewById(R.id.tvSave);
            AppCompatImageView icBack = toolbarWrapper.findViewById(R.id.icBack);

            tvTitle.setText(getString(R.string.select_category));
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(CategoryPickerActivity.this, R.anim.scale_in, R.anim.right_to_left);
            });

            categoryRoot = findViewById(R.id.categoryRoot);
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

            ViewCompat.setOnApplyWindowInsetsListener(categoryRoot, (view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            Bundle bundle = getIntent().getExtras();

            medium = ResourcesCompat.getFont(this, R.font.exo2_medium);
            semiBold = ResourcesCompat.getFont(this, R.font.exo2_semibold);

            if (bundle != null) {

                CategoryViewModel categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

                categoryViewModel.incomeCategory(bundle.getInt("transactionType"), true);
                categoryId = bundle.getInt("categoryId", -1);
                isFromScreen = bundle.getString("isFromScreen", "");

                if (isFromScreen.equalsIgnoreCase("budget")) {

                    tvSave.setVisibility(View.VISIBLE);
                    tvSave.setText(getString(R.string.done));

                    selectedCategoryIds.clear();
                    ArrayList<Integer> categoryIds = bundle.getIntegerArrayList("categoryIds");
                    if (categoryIds != null) {
                        selectedCategoryIds.addAll(categoryIds);
                    }

                    tvSave.setOnClickListener(v -> {
                        selectedCategoryIds.clear();
                        selectedCategoryIds.addAll(tempSelectedCategoryIds);
                        Intent intent = new Intent();
                        intent.putIntegerArrayListExtra("categoryIds", new ArrayList<>(selectedCategoryIds));
                        setResult(-1, intent);
                        finish();
                        ActivityUtils.overrideCloseTransition(CategoryPickerActivity.this, R.anim.slide_in_left, R.anim.slide_out_right);
                    });
                }

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

            tempSelectedCategoryIds = new HashSet<>(selectedCategoryIds);

            RecyclerViewAdapter<CategoryEntity> expenseCategoryAdapter = new RecyclerViewAdapter<>(this,
                    expenseCategories, R.layout.item_category_picker) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, CategoryEntity categoryEntity) {

                    AppCompatImageView ivSelected = holder.getView(R.id.ivSelected);
                    MaterialCheckBox ivChecked = holder.getView(R.id.ivChecked);
                    AppCompatTextView tvCategory = holder.getView(R.id.tvCategory);

                    holder.setViewImageResource(R.id.ivCategory, DataHelper.getCategoryIcons().get(categoryEntity.icon));
                    tvCategory.setText(categoryEntity.getName(getApplicationContext()));

                    if (isFromScreen.equalsIgnoreCase("transaction")) {
                        if (categoryEntity.id == categoryId) {
                            tvCategory.setTypeface(semiBold);
                            ivSelected.setVisibility(View.VISIBLE);
                        } else {
                            tvCategory.setTypeface(medium);
                            ivSelected.setVisibility(View.GONE);
                        }
                        ivChecked.setVisibility(View.GONE);

                        holder.itemView.setOnClickListener(view -> {
                            Intent intent = new Intent();
                            intent.putExtra("category", categoryEntity);
                            setResult(-1, intent);
                            finish();
                            ActivityUtils.overrideCloseTransition(CategoryPickerActivity.this, R.anim.slide_in_left, R.anim.slide_out_right);
                        });
                    } else {
                        ivSelected.setVisibility(View.GONE);
                        ivChecked.setVisibility(View.VISIBLE);

                        ivChecked.setOnCheckedChangeListener(null);
                        if(selectedCategoryIds.contains(categoryEntity.id)) {
                            tvCategory.setTypeface(semiBold);
                            ivChecked.setChecked(true);
                        } else {
                            tvCategory.setTypeface(medium);
                            ivChecked.setChecked(false);
                        }

                        ivChecked.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) {
                                tvCategory.setTypeface(semiBold);
                                tempSelectedCategoryIds.add(categoryEntity.id);
                            } else {
                                tvCategory.setTypeface(medium);
                                tempSelectedCategoryIds.remove(categoryEntity.id);
                            }
                        });

                        holder.itemView.setOnClickListener(v -> ivChecked.setChecked(!ivChecked.isChecked()));
                    }

                    if (Build.VERSION.SDK_INT >= 29) {
                        holder.getView(R.id.colorView).getBackground().setColorFilter(new BlendModeColorFilter(Color.parseColor(categoryEntity.color), BlendMode.SRC_OVER));
                    } else {

                        Drawable drawable = holder.getView(R.id.colorView).getBackground().mutate();
                        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_OVER);
                        DrawableCompat.setTint(drawable, Color.parseColor(categoryEntity.color));
                        holder.getView(R.id.colorView).setBackground(drawable);
                    }
                }
            };

            rvCategories.setAdapter(expenseCategoryAdapter);
            rvCategories.setHasFixedSize(true);
            updateRecyclerViewMaxHeight();

            expenseCategoryAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    updateRecyclerViewMaxHeight();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    updateRecyclerViewMaxHeight();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    updateRecyclerViewMaxHeight();
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindCategories", e);
        }
    }

    private void updateRecyclerViewMaxHeight() {
        categoryRoot.post(() -> {

            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            int bottomInset = categoryRoot.getPaddingBottom();
            int topMargin = CommonUtils.dpToPx(this, 10);
            int bottomMargin = CommonUtils.dpToPx(this, 16);

            int availableHeight =
                    categoryRoot.getHeight()
                            - toolbarWrapper.getHeight()
                            - topMargin
                            - bottomMargin
                            - bottomInset;

            if (availableHeight > 0) {
                rvCategories.setMaxHeight(availableHeight);
            }
        });
    }
}