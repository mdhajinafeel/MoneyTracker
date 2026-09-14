package com.nprotech.moneytracker.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.BudgetEntity;
import com.nprotech.moneytracker.enums.SettingType;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.BudgetWithDetails;
import com.nprotech.moneytracker.models.CategoryBudgetProgress;
import com.nprotech.moneytracker.models.SettingItemModel;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.BudgetViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CategoryBudgetActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView tvSpentAmount, tvTargetAmount, tvProgressPercentage, tvAllCategories, tvSorting;
    private RecyclerView rvCategoryTransactions;
    private ProgressBar progressBudget;
    private ConstraintLayout emptyWrapper;
    private BudgetViewModel budgetViewModel;
    private BudgetWithDetails budgetDetail;
    private RecyclerViewAdapter<CategoryBudgetProgress> categoryBudgetAdapter;
    private Typeface semiTypeface, mediumTypeface;
    private int budgetId = 0;
    private SettingType selectedSortType = SettingType.HIGHEST_USAGE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_budget);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            View rootView = findViewById(R.id.rootView);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            rvCategoryTransactions = findViewById(R.id.rvCategoryTransactions);
            tvTargetAmount = findViewById(R.id.tvTargetAmount);
            tvSpentAmount = findViewById(R.id.tvSpentAmount);
            tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
            tvAllCategories = findViewById(R.id.tvAllCategories);
            progressBudget = findViewById(R.id.progressBudget);
            emptyWrapper = findViewById(R.id.emptyWrapper);
            tvSorting = findViewById(R.id.tvSorting);

            tvTitle.setText(getString(R.string.category_budgets));

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {

                budgetViewModel = new ViewModelProvider(this).get(BudgetViewModel.class);

                semiTypeface = ResourcesCompat.getFont(this, R.font.exo2_semibold);
                mediumTypeface = ResourcesCompat.getFont(this, R.font.exo2_medium);

                bindData(bundle);
                initializeAdapters();
                setupListeners();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData(Bundle bundle) {
        try {

            CommonUtils.setDrawable(this, tvSorting, R.drawable.ic_filter, R.dimen.icon_12, R.color.primary_dark, Gravity.START);

            budgetId = bundle.getInt("budgetId", 0);

            if (budgetId > 0) {
                budgetViewModel.getBudgetDetailById(budgetId).observe(this, budgetWithDetail -> {
                    if (budgetWithDetail != null) {

                        budgetDetail = budgetWithDetail;
                        BudgetEntity budget = budgetDetail.budget;

                        int budgetColor = Color.parseColor(budget.budgetColor);
                        int progress = CommonUtils.calculateProgress(budgetDetail.spentAmount, budget.amount);

                        tvSpentAmount.setText(CommonUtils.getBeautifyAmount(budget.currencySymbol, budgetWithDetail.spentAmount));
                        tvTargetAmount.setText(CommonUtils.getBeautifyAmount(budget.currencySymbol, budget.amount));

                        progressBudget.setProgressDrawable(CommonUtils.createGoalProgressDrawable(this, budgetColor));
                        progressBudget.setProgress(progress);
                        tvProgressPercentage.setTextColor(budgetColor);

                        loadCategoryBudgets(budget);
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finishWithTransitions();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    public void loadCategoryBudgets(BudgetEntity budget) {
        try {
            budgetViewModel.loadCategoryBudgetProgress(PreferenceManager.INSTANCE.getAccountId(), budget.id, budget.startDate, budget.endDate,
                    getSortValue(selectedSortType));
        } catch (Exception e) {
            AppLogger.e(getClass(), "loadCategoryBudgets", e);
        }
    }

    private void initializeAdapters() {
        try {

            // CATEGORY BUDGETS
            rvCategoryTransactions.setLayoutManager(new LinearLayoutManager(this));
            categoryBudgetAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(), R.layout.item_category_budget_transaction) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, CategoryBudgetProgress categoryBudget) {
                    MaterialCardView colorView = holder.getView(R.id.colorView);
                    AppCompatImageView imageView = holder.getView(R.id.imageView);
                    AppCompatTextView nameLabel = holder.getView(R.id.nameLabel);
                    AppCompatTextView tvAmount = holder.getView(R.id.tvAmount);
                    ProgressBar progressBudget = holder.getView(R.id.progressBudget);
                    AppCompatTextView tvProgressPercentage = holder.getView(R.id.tvProgressPercentage);
                    View divider = holder.getView(R.id.divider);

                    int color = Color.parseColor(categoryBudget.color);
                    int progress = CommonUtils.calculateProgress(categoryBudget.spentAmount, categoryBudget.budgetAmount);

                    colorView.setCardBackgroundColor(color);
                    imageView.setImageDrawable(ContextCompat.getDrawable(CategoryBudgetActivity.this, DataHelper.getCategoryIcons().get(categoryBudget.icon)));
                    nameLabel.setText(categoryBudget.getCategoryName(CategoryBudgetActivity.this));

                    String spentAmount = CommonUtils.getBeautifyAmount(categoryBudget.currencySymbol, categoryBudget.spentAmount);
                    String budgetAmount = getString(R.string.target_amount_value, CommonUtils.getBeautifyAmount(categoryBudget.currencySymbol, categoryBudget.budgetAmount));
                    CommonUtils.setFormattedText(tvAmount, spentAmount, budgetAmount, semiTypeface, mediumTypeface);

                    boolean isOverspent = categoryBudget.spentAmount > categoryBudget.budgetAmount;

                    if (isOverspent) {
                        progressBudget.setProgressDrawable(CommonUtils.createGoalProgressDrawable(CategoryBudgetActivity.this,
                                ContextCompat.getColor(CategoryBudgetActivity.this, R.color.overspent_dark)));
                        progressBudget.setProgress(100);
                        tvProgressPercentage.setText(R.string.text_over);
                        tvProgressPercentage.setTextColor(ContextCompat.getColor(CategoryBudgetActivity.this, R.color.overspent_dark));
                        tvProgressPercentage.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                                CategoryBudgetActivity.this, R.color.overspent_bg)));
                    } else {
                        progressBudget.setProgressDrawable(CommonUtils.createGoalProgressDrawable(CategoryBudgetActivity.this, color));
                        progressBudget.setProgress(progress);
                        tvProgressPercentage.setText(getString(R.string.alert_percentage_value, progress));

                        int darkColor = Color.rgb((int) (Color.red(color) * 0.75f), (int) (Color.green(color) * 0.75f), (int) (Color.blue(color) * 0.75f));
                        tvProgressPercentage.setTextColor(darkColor);

                        tvProgressPercentage.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(categoryBudget.color)));
                    }

                    int position = holder.getBindingAdapterPosition();
                    if (position == getItemCount() - 1) {
                        divider.setAlpha(0f);
                    } else {
                        divider.setAlpha(1f);
                    }

                    holder.getView(R.id.itemView).setOnClickListener(v -> {
                        Intent intent = new Intent(CategoryBudgetActivity.this, CategoryTransactionActivity.class);
                        intent.putExtra("budgetId", budgetId);
                        intent.putExtra("budgetStartDate", budgetDetail.budget.startDate);
                        intent.putExtra("budgetEndDate", budgetDetail.budget.endDate);
                        intent.putExtra("budgetWalletCount", budgetDetail.budget.walletCount);
                        intent.putExtra("budgetEndDate", budgetDetail.budget.endDate);
                        intent.putExtra("categoryId", categoryBudget.categoryId);
                        intent.putExtra("isFromBudget", true);
                        intent.putExtra("categoryName", categoryBudget.getCategoryName(CategoryBudgetActivity.this));
                        startActivity(intent);
                        ActivityUtils.overrideOpenTransition(CategoryBudgetActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                    });

                    holder.getView(R.id.ivMore).setOnClickListener(v -> {
                        Intent intent = new Intent(CategoryBudgetActivity.this, CategoryTransactionActivity.class);
                        intent.putExtra("budgetId", budgetId);
                        intent.putExtra("budgetStartDate", budgetDetail.budget.startDate);
                        intent.putExtra("budgetEndDate", budgetDetail.budget.endDate);
                        intent.putExtra("budgetWalletCount", budgetDetail.budget.walletCount);
                        intent.putExtra("categoryId", categoryBudget.categoryId);
                        intent.putExtra("isFromBudget", true);
                        intent.putExtra("categoryName", categoryBudget.getCategoryName(CategoryBudgetActivity.this));
                        startActivity(intent);
                        ActivityUtils.overrideOpenTransition(CategoryBudgetActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                    });
                }
            };
            rvCategoryTransactions.setAdapter(categoryBudgetAdapter);
            rvCategoryTransactions.setHasFixedSize(true);
            rvCategoryTransactions.setItemAnimator(null);
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

            tvSorting.setOnClickListener(v -> showCategoryFilterDialog());

            budgetViewModel.getCategoryBudgets().observe(this, categoryBudgetProgresses -> {
                if (categoryBudgetProgresses != null && !categoryBudgetProgresses.isEmpty()) {
                    categoryBudgetAdapter.setItems(categoryBudgetProgresses);
                    emptyWrapper.setVisibility(View.GONE);
                    int count = categoryBudgetProgresses.size();
                    tvAllCategories.setText(getResources().getQuantityString(R.plurals.category_amount_count, count, count));
                } else {
                    categoryBudgetAdapter.setItems(new ArrayList<>());
                    emptyWrapper.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showCategoryFilterDialog() {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_filter_option, findViewById(android.R.id.content), false);

            RecyclerView rvSortBy = bottomView.findViewById(R.id.rvSortBy);
            AppCompatTextView tvAttachments = bottomView.findViewById(R.id.tvAttachments);
            RecyclerView rvAttachments = bottomView.findViewById(R.id.rvAttachments);
            MaterialButton btnPrimary = bottomView.findViewById(R.id.btnPrimary);
            MaterialButton btnSecondary = bottomView.findViewById(R.id.btnSecondary);
            AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);

            tvAttachments.setVisibility(View.GONE);
            rvAttachments.setVisibility(View.GONE);

            // ---------------------------------------------------------
            // Sort options
            // ---------------------------------------------------------
            List<SettingItemModel> sortByList = new ArrayList<>();
            sortByList.add(new SettingItemModel(SettingType.HIGHEST_USAGE, 0, 0, 0,
                    getString(R.string.highest_usage), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.LOWEST_USAGE, 0, 0, 0,
                    getString(R.string.lowest_usage), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.HIGHEST_SPENDING, 0, 0, 0,
                    getString(R.string.highest_spending), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.LOWEST_SPENDING, 0, 0, 0,
                    getString(R.string.lowest_spending), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.HIGHEST_BUDGET, 0, 0, 0,
                    getString(R.string.highest_budget), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.LOWEST_BUDGET, 0, 0, 0,
                    getString(R.string.lowest_budget), true, false, null, true, false, 0));

            RecyclerViewAdapter<SettingItemModel> sortByAdapter = new RecyclerViewAdapter<>(this, sortByList, R.layout.item_backup_filter_option) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, SettingItemModel item) {
                    holder.setViewText(R.id.tvFilterName, item.title);
                    AppCompatImageView ivSelected = holder.getView(R.id.ivSelected);
                    boolean selected = item.settingType == selectedSortType;
                    ivSelected.setVisibility(selected ? View.VISIBLE : View.GONE);
                    holder.getView(R.id.rlFilterView).setOnClickListener(v -> {
                        selectedSortType = item.settingType;
                        notifyDataSetChanged();
                    });
                }
            };
            rvSortBy.setAdapter(sortByAdapter);
            rvSortBy.setHasFixedSize(true);
            rvSortBy.setItemAnimator(null);

            btnPrimary.setOnClickListener(v -> {
                applyCategoryFilters();
                dialog.dismiss();
            });

            btnSecondary.setOnClickListener(v -> {
                selectedSortType = SettingType.HIGHEST_USAGE;
                applyCategoryFilters();
                dialog.dismiss();
            });

            tvClose.setOnClickListener(v -> dialog.dismiss());

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showBackupFilterDialog", e);
        }
    }

    private void applyCategoryFilters() {
        try {
            updateSortingLabel();
            if (budgetDetail != null) {
                loadCategoryBudgets(budgetDetail.budget);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "applyCategoryFilters", e);
        }
    }

    private void updateSortingLabel() {
        try {
            switch (selectedSortType) {
                case HIGHEST_USAGE:
                    tvSorting.setText(getString(R.string.highest_usage));
                    break;
                case LOWEST_USAGE:
                    tvSorting.setText(getString(R.string.lowest_usage));
                    break;
                case HIGHEST_SPENDING:
                    tvSorting.setText(getString(R.string.highest_spending));
                    break;
                case LOWEST_SPENDING:
                    tvSorting.setText(getString(R.string.lowest_spending));
                    break;
                case HIGHEST_BUDGET:
                    tvSorting.setText(getString(R.string.highest_budget));
                    break;
                case LOWEST_BUDGET:
                    tvSorting.setText(getString(R.string.lowest_budget));
                    break;
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "updateSortingLabel", e);
        }
    }

    private int getSortValue(SettingType type) {
        if (type == SettingType.HIGHEST_USAGE) {
            return 1;
        } else if (type == SettingType.LOWEST_USAGE) {
            return 2;
        } else if (type == SettingType.HIGHEST_SPENDING) {
            return 3;
        } else if (type == SettingType.LOWEST_SPENDING) {
            return 4;
        } else if (type == SettingType.HIGHEST_BUDGET) {
            return 5;
        } else if (type == SettingType.LOWEST_BUDGET) {
            return 6;
        }

        return 1;
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
    }
}