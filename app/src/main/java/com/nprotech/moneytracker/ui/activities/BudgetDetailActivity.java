package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.BudgetEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.BudgetWithDetails;
import com.nprotech.moneytracker.models.CategoryBudgetProgress;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.TransactionAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.ui.common.MaxHeightRecyclerView;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.utils.SimpleDividerItemDecoration;
import com.nprotech.moneytracker.viewmodel.BudgetViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BudgetDetailActivity extends BaseActivity {

    private AppCompatImageView icBack, ivMore, ivBudgetIcon, ivBudgetDot, ivBudgetPeriodDot, ivBudgetStatus;
    private MaterialCardView cardBudgetIcon, cardBudgetMethod;
    private AppCompatTextView tvBudgetName, tvBudgetDate, tvBudgetPeriod, tvBudgetMethod, tvBudgetAmount, tvBudgetStatus, tvSpentAmount, tvTargetAmount, tvProgressPercentage,
            lblStatus, lblAlertBudgetDesc, tvPeriod, tvRepeat, tvAlert, tvWallets, tvCategories, tvStatus, tvViewAll, lblMoreCategory, tvViewAllMethod;
    private ProgressBar progressBudget;
    private View rootView;
    private ConstraintLayout budgetDetailContainer, emptyWrapper;
    private MaxHeightRecyclerView rvTransactions, rvCategoryTransactions;
    private BudgetViewModel budgetViewModel;
    private BudgetWithDetails budgetDetail;
    private TransactionAdapter transactionAdapter;
    private RecyclerViewAdapter<CategoryBudgetProgress> categoryBudgetAdapter;
    private Typeface semiTypeface, mediumTypeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_detail);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            rootView = findViewById(R.id.rootView);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            ivMore = toolbarWrapper.findViewById(R.id.ivMore);

            ivMore.setVisibility(View.VISIBLE);

            cardBudgetIcon = findViewById(R.id.cardBudgetIcon);
            cardBudgetMethod = findViewById(R.id.cardBudgetMethod);
            ivBudgetIcon = findViewById(R.id.ivBudgetIcon);
            ivBudgetDot = findViewById(R.id.ivBudgetDot);
            ivBudgetPeriodDot = findViewById(R.id.ivBudgetPeriodDot);
            tvBudgetName = findViewById(R.id.tvBudgetName);
            tvBudgetDate = findViewById(R.id.tvBudgetDate);
            tvBudgetPeriod = findViewById(R.id.tvBudgetPeriod);
            tvBudgetMethod = findViewById(R.id.tvBudgetMethod);
            tvBudgetAmount = findViewById(R.id.tvBudgetAmount);
            tvBudgetStatus = findViewById(R.id.tvBudgetStatus);
            tvSpentAmount = findViewById(R.id.tvSpentAmount);
            tvTargetAmount = findViewById(R.id.tvTargetAmount);
            progressBudget = findViewById(R.id.progressBudget);
            tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
            ivBudgetStatus = findViewById(R.id.ivBudgetStatus);
            budgetDetailContainer = findViewById(R.id.budgetDetailContainer);
            lblStatus = findViewById(R.id.lblStatus);
            lblAlertBudgetDesc = findViewById(R.id.lblAlertBudgetDesc);
            tvPeriod = findViewById(R.id.tvPeriod);
            tvRepeat = findViewById(R.id.tvRepeat);
            tvAlert = findViewById(R.id.tvAlert);
            tvWallets = findViewById(R.id.tvWallets);
            tvCategories = findViewById(R.id.tvCategories);
            tvStatus = findViewById(R.id.tvStatus);
            tvViewAll = findViewById(R.id.tvViewAll);
            rvTransactions = findViewById(R.id.rvTransactions);
            rvCategoryTransactions = findViewById(R.id.rvCategoryTransactions);
            emptyWrapper = findViewById(R.id.emptyWrapper);
            lblMoreCategory = findViewById(R.id.lblMoreCategory);
            tvViewAllMethod = findViewById(R.id.tvViewAllMethod);

            tvTitle.setText(getString(R.string.budget_details));

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
                finishWithTransitions();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData(Bundle bundle) {
        try {
            int budgetId = bundle.getInt("budgetId", 0);

            if (budgetId > 0) {

                budgetViewModel.getBudgetDetailById(budgetId).observe(this, budgetWithDetail -> {
                    if (budgetWithDetail != null) {

                        budgetDetail = budgetWithDetail;
                        BudgetEntity budget = budgetDetail.budget;

                        int budgetColor = Color.parseColor(budget.budgetColor);
                        int progress = CommonUtils.calculateProgress(budgetDetail.spentAmount, budget.amount);

                        cardBudgetIcon.setCardBackgroundColor(budgetColor);
                        ivBudgetIcon.setImageDrawable(ContextCompat.getDrawable(this, DataHelper.getCategoryIcons().get(budget.budgetIcon)));

                        tvBudgetName.setText(budget.name);
                        ImageViewCompat.setImageTintList(ivBudgetDot, ColorStateList.valueOf(budgetColor));

                        tvBudgetPeriod.setText(DataHelper.getPeriodName(budget.periodId));
                        tvBudgetDate.setText(DateHelper.formatDateRange(budget.startDate, budget.endDate));
                        ImageViewCompat.setImageTintList(ivBudgetPeriodDot, ColorStateList.valueOf(budgetColor));

                        tvSpentAmount.setText(CommonUtils.getBeautifyAmount(budget.currencySymbol, budgetWithDetail.spentAmount));
                        tvTargetAmount.setText(CommonUtils.getBeautifyAmount(budget.currencySymbol, budget.amount));
                        tvBudgetAmount.setText(CommonUtils.getBeautifyAmount(budget.currencySymbol, budget.amount));

                        progressBudget.setProgressDrawable(CommonUtils.createGoalProgressDrawable(this, budgetColor));
                        progressBudget.setProgress(progress);
                        tvProgressPercentage.setText(getString(R.string.alert_percentage_value, progress));
                        tvProgressPercentage.setTextColor(budgetColor);

                        tvPeriod.setText(DateHelper.formatDateRange(budget.startDate, budget.endDate));
                        tvRepeat.setText(DataHelper.getPeriodName(budget.periodId));

                        if (budget.alertEnabled) {
                            tvAlert.setText(getString(R.string.enabled_percentage, budget.alertPercentage));
                        } else {
                            tvAlert.setText(getString(R.string.disabled));
                        }

                        int walletCount = budget.walletCount;
                        if (walletCount == -1) {
                            tvWallets.setText(getString(R.string.all_wallets));
                        } else {
                            tvWallets.setText(getResources().getQuantityString(R.plurals.wallet_count, walletCount, walletCount));
                        }

                        int categoryCount = budget.categoryCount;
                        if (budget.isAllCategory) {
                            tvCategories.setText(getString(R.string.all_categories));
                        } else {
                            tvCategories.setText(getResources().getQuantityString(R.plurals.category_amount_count, categoryCount, categoryCount));
                        }

                        updateBudgetStatus(budget, budgetWithDetail.spentAmount);
                        updateFields(budgetWithDetail);
                        updateBudgetMethod(budget);

                        loadTransactions(budget);
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

    private void updateBudgetStatus(BudgetEntity budget, double spentAmount) {

        double budgetAmount = budget.amount;

        if (budgetAmount <= 0) {
            return;
        }

        budgetDetailContainer.setVisibility(View.VISIBLE);

        double percentage = (spentAmount / budgetAmount) * 100.0;

        if (percentage > 100.0) {
            // OVER SPENT
            budgetDetailContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.overspent_bg)));
            ivBudgetStatus.setImageResource(R.drawable.ic_warning);
            ivBudgetStatus.setColorFilter(ContextCompat.getColor(this, R.color.overspent_dark));
            lblStatus.setText(getString(R.string.overspent_budget));
            double overSpent = spentAmount - budgetAmount;
            lblAlertBudgetDesc.setText(getString(R.string.overspent_desc, CommonUtils.getBeautifyAmount(budget.currencySymbol, overSpent)));
            lblStatus.setTextColor(ContextCompat.getColor(this, R.color.overspent_dark));
        } else if (percentage == 100.0) {
            lblStatus.setText(getString(R.string.completed_budget));
            lblAlertBudgetDesc.setText(getString(R.string.completed_budget_desc));
        } else if (budget.alertEnabled && percentage >= budget.alertPercentage) {
            // WARNING
            budgetDetailContainer.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.warning_bg)));
            ivBudgetStatus.setImageResource(R.drawable.ic_warning);
            ivBudgetStatus.setColorFilter(ContextCompat.getColor(this, R.color.warning_dark));
            lblStatus.setText(getString(R.string.warning));
            lblAlertBudgetDesc.setText(getString(R.string.warning_budget_desc, percentage));
            lblStatus.setTextColor(ContextCompat.getColor(this, R.color.warning_dark));
        } else {
            // ON TRACK
            lblStatus.setText(getString(R.string.you_re_on_track));
            lblAlertBudgetDesc.setText(getString(R.string.used_budget, percentage));
        }
    }

    private void updateFields(BudgetWithDetails budgetDetail) {
        try {

            BudgetEntity budget = budgetDetail.budget;

            boolean isOverSpent = false;
            boolean isCompleted = false;

            if (budgetDetail.spentAmount > budget.amount) {
                isOverSpent = true;
            } else if (budgetDetail.spentAmount == budget.amount) {
                isCompleted = true;
            }

            if (budget.isPaused) {
                tvBudgetStatus.setText(getString(R.string.text_paused));
                CommonUtils.setDrawable(this, tvBudgetStatus, R.drawable.ic_pause, R.dimen.icon_10, R.color.color_pause_dark, Gravity.START);
                setBudgetStatusStyle(tvBudgetStatus, R.color.color_pause_dark, R.color.color_pause_light, R.color.color_pause_dark);
                tvBudgetStatus.setTextColor(getColor(R.color.color_pause_dark));

                tvStatus.setText(getString(R.string.text_paused));
                CommonUtils.setDrawable(this, tvStatus, R.drawable.ic_pause, R.dimen.icon_14, R.color.color_pause_dark, Gravity.START);
                setBudgetStatusStyle(tvStatus, R.color.color_pause_dark, R.color.color_pause_light, R.color.color_pause_dark);
                tvStatus.setTextColor(getColor(R.color.color_pause_dark));
            } else if (budget.isArchived) {
                tvBudgetStatus.setText(getString(R.string.archived));
                CommonUtils.setDrawable(this, tvBudgetStatus, R.drawable.ic_archive, R.dimen.icon_10, R.color.category_dark, Gravity.START);
                setBudgetStatusStyle(tvBudgetStatus, R.color.category_dark, R.color.category_light, R.color.category_dark);
                tvBudgetStatus.setTextColor(getColor(R.color.category_dark));

                tvStatus.setText(getString(R.string.archived));
                CommonUtils.setDrawable(this, tvStatus, R.drawable.ic_archive, R.dimen.icon_14, R.color.category_dark, Gravity.START);
                setBudgetStatusStyle(tvStatus, R.color.category_dark, R.color.category_light, R.color.category_dark);
                tvStatus.setTextColor(getColor(R.color.category_dark));
            } else if (isOverSpent) {
                tvBudgetStatus.setText(getString(R.string.text_overspent));
                CommonUtils.setDrawable(this, tvBudgetStatus, R.drawable.ic_warning_triangle, R.dimen.icon_10, R.color.overspent_dark, Gravity.START);
                setBudgetStatusStyle(tvBudgetStatus, R.color.overspent_dark, R.color.overspent_bg, R.color.overspent_dark);
                tvBudgetStatus.setTextColor(getColor(R.color.overspent_dark));

                tvStatus.setText(getString(R.string.text_overspent));
                CommonUtils.setDrawable(this, tvStatus, R.drawable.ic_warning_triangle, R.dimen.icon_14, R.color.overspent_dark, Gravity.START);
                setBudgetStatusStyle(tvStatus, R.color.overspent_dark, R.color.overspent_bg, R.color.overspent_dark);
                tvStatus.setTextColor(getColor(R.color.overspent_dark));
            } else if (isCompleted) {
                tvBudgetStatus.setText(getString(R.string.text_completed));
                CommonUtils.setDrawable(this, tvBudgetStatus, R.drawable.ic_complete, R.dimen.icon_10, R.color.dark_income, Gravity.START);
                setBudgetStatusStyle(tvBudgetStatus, R.color.dark_income, R.color.very_light_income, R.color.dark_income);
                tvBudgetStatus.setTextColor(getColor(R.color.dark_income));

                tvStatus.setText(getString(R.string.text_completed));
                CommonUtils.setDrawable(this, tvStatus, R.drawable.ic_complete, R.dimen.icon_14, R.color.dark_income, Gravity.START);
                setBudgetStatusStyle(tvStatus, R.color.dark_income, R.color.very_light_income, R.color.dark_income);
                tvStatus.setTextColor(getColor(R.color.dark_income));
            } else {
                tvBudgetStatus.setText(getString(R.string.active));
                CommonUtils.setDrawable(this, tvBudgetStatus, R.drawable.ic_done, R.dimen.icon_10, R.color.dark_income, Gravity.START);
                setBudgetStatusStyle(tvBudgetStatus, R.color.dark_income, R.color.very_light_income, R.color.dark_income);
                tvBudgetStatus.setTextColor(getColor(R.color.dark_income));

                tvStatus.setText(getString(R.string.active));
                CommonUtils.setDrawable(this, tvStatus, R.drawable.ic_done, R.dimen.icon_14, R.color.dark_income, Gravity.START);
                setBudgetStatusStyle(tvStatus, R.color.dark_income, R.color.very_light_income, R.color.dark_income);
                tvStatus.setTextColor(getColor(R.color.dark_income));
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "updateFields", e);
        }
    }

    private void updateBudgetMethod(BudgetEntity budget) {
        try {
            if (budget.methodId == 1) {
                tvBudgetMethod.setText(getString(R.string.shared_detail_summary));
                cardBudgetMethod.setVisibility(View.GONE);
            } else {
                tvBudgetMethod.setText(getString(R.string.separate_detail_summary));
                cardBudgetMethod.setVisibility(View.VISIBLE);
                loadCategoryBudgets(budget);

                CommonUtils.setDrawables(BudgetDetailActivity.this, lblMoreCategory, R.drawable.ic_add,
                        R.drawable.ic_right_arrow, R.dimen.icon_14, R.color.primary_dark);
            }
        }catch (Exception e) {
            AppLogger.e(getClass(), "", e);
        }
    }

    private void loadTransactions(BudgetEntity budget) {
        try {

            List<Integer> categoryIds = budgetViewModel.getCategoryIdsByBudgetId(budget.id);
            List<Integer> walletIds = budgetViewModel.getWalletIdsByBudgetId(budget.id);

            budgetViewModel.loadTransactions(PreferenceManager.INSTANCE.getAccountId(), budget.startDate, budget.endDate, categoryIds, walletIds,
                    budget.isAllCategory, budget.walletCount == -1, true);
        } catch (Exception e) {
            AppLogger.e(getClass(), "loadTransactions", e);
        }
    }

    public void loadCategoryBudgets(BudgetEntity budget) {
        try {
            budgetViewModel.loadCategoryBudgetProgress(PreferenceManager.INSTANCE.getAccountId(), budget.id, budget.startDate, budget.endDate, 1);
        } catch (Exception e) {
            AppLogger.e(getClass(), "loadCategoryBudgets", e);
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

            ivMore.setOnClickListener(v -> showOptionDialog(budgetDetail));

            budgetViewModel.getTransactions().observe(this, list -> {
                if (list != null && !list.isEmpty()) {
                    emptyWrapper.setVisibility(View.GONE);
                    transactionAdapter.setItems(list);
                    rvTransactions.setVisibility(View.VISIBLE);
                    rvTransactions.post(() -> rvTransactions.scrollToPosition(0));
                } else {
                    transactionAdapter.setItems(new ArrayList<>());
                    emptyWrapper.setVisibility(View.VISIBLE);
                    rvTransactions.setVisibility(View.GONE);
                }
            });

            budgetViewModel.getCategoryBudgets().observe(this, categoryBudgetProgresses -> {
                if(categoryBudgetProgresses != null && !categoryBudgetProgresses.isEmpty()) {
                    if(categoryBudgetProgresses.size() > 5) {
                        categoryBudgetAdapter.setItems(new ArrayList<>(categoryBudgetProgresses.subList(0, 5)));
                        lblMoreCategory.setVisibility(View.VISIBLE);

                        lblMoreCategory.setText(getString(R.string.text_more_categories, categoryBudgetProgresses.size() - 5));
                    } else {
                        categoryBudgetAdapter.setItems(categoryBudgetProgresses);
                        lblMoreCategory.setVisibility(View.GONE);
                    }
                } else {
                    categoryBudgetAdapter.setItems(new ArrayList<>());
                    lblMoreCategory.setVisibility(View.GONE);
                }
            });

            tvViewAll.setOnClickListener(v -> {
                startActivity(new Intent(BudgetDetailActivity.this, BudgetTransactionActivity.class)
                        .putExtra("budgetId", budgetDetail.budget.id));
                ActivityUtils.overrideOpenTransition(BudgetDetailActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            tvViewAllMethod.setOnClickListener(v -> {
                startActivity(new Intent(BudgetDetailActivity.this, CategoryBudgetActivity.class)
                        .putExtra("budgetId", budgetDetail.budget.id));
                ActivityUtils.overrideOpenTransition(BudgetDetailActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            lblMoreCategory.setOnClickListener(v -> {
                startActivity(new Intent(BudgetDetailActivity.this, CategoryBudgetActivity.class)
                        .putExtra("budgetId", budgetDetail.budget.id));
                ActivityUtils.overrideOpenTransition(BudgetDetailActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void initializeAdapters() {
        try {

            // CATEGORY BUDGETS
            rvCategoryTransactions.setLayoutManager(new LinearLayoutManager(this));
            categoryBudgetAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(), R.layout.item_category_budget) {
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

                    boolean isOverspent = categoryBudget.spentAmount > categoryBudget.budgetAmount;

                    colorView.setCardBackgroundColor(color);
                    imageView.setImageDrawable(ContextCompat.getDrawable(BudgetDetailActivity.this, DataHelper.getCategoryIcons().get(categoryBudget.icon)));
                    nameLabel.setText(categoryBudget.getCategoryName(BudgetDetailActivity.this));

                    String spentAmount = CommonUtils.getBeautifyAmount(categoryBudget.currencySymbol, categoryBudget.spentAmount);
                    String budgetAmount = getString(R.string.target_amount_value, CommonUtils.getBeautifyAmount(categoryBudget.currencySymbol, categoryBudget.budgetAmount));
                    CommonUtils.setFormattedText(tvAmount, spentAmount, budgetAmount, semiTypeface, mediumTypeface);

                    if (isOverspent) {
                        progressBudget.setProgressDrawable(CommonUtils.createGoalProgressDrawable(BudgetDetailActivity.this,
                                        ContextCompat.getColor(BudgetDetailActivity.this, R.color.overspent_dark)));
                        progressBudget.setProgress(100);
                        tvProgressPercentage.setText(R.string.text_over);
                        tvProgressPercentage.setTextColor(ContextCompat.getColor(BudgetDetailActivity.this, R.color.overspent_dark));
                        tvProgressPercentage.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                                BudgetDetailActivity.this, R.color.overspent_bg)));
                    } else {
                        progressBudget.setProgressDrawable(CommonUtils.createGoalProgressDrawable(BudgetDetailActivity.this, color));
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
                }
            };
            rvCategoryTransactions.setAdapter(categoryBudgetAdapter);
            rvCategoryTransactions.setHasFixedSize(true);
            rvCategoryTransactions.setItemAnimator(null);

            updateRecyclerViewMaxHeight();

            categoryBudgetAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
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

            // TRANSACTION
            rvTransactions.setLayoutManager(new LinearLayoutManager(this));
            transactionAdapter = new TransactionAdapter(this, new ArrayList<>(), R.layout.item_transaction_period_detail,
                    false, "budget", null);
            rvTransactions.setAdapter(transactionAdapter);
            rvTransactions.setHasFixedSize(true);
            rvTransactions.setItemAnimator(null);
            rvTransactions.addItemDecoration(new SimpleDividerItemDecoration(this));

            transactionAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
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
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    private void showOptionDialog(BudgetWithDetails budgetDetail) {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_budget_options, findViewById(android.R.id.content), false);
            MaterialCardView colorView = bottomView.findViewById(R.id.colorView);
            AppCompatImageView ivBudgetIcon = bottomView.findViewById(R.id.ivBudgetIcon);
            AppCompatTextView nameLabel = bottomView.findViewById(R.id.nameLabel);
            AppCompatTextView tvBudgetPeriod = bottomView.findViewById(R.id.tvBudgetPeriod);
            AppCompatTextView tvBudgetAmount = bottomView.findViewById(R.id.tvBudgetAmount);
            LinearLayout optionEdit = bottomView.findViewById(R.id.optionEdit);
            View viewEdit = bottomView.findViewById(R.id.viewEdit);
            LinearLayout optionViewDetails = bottomView.findViewById(R.id.optionViewDetails);
            View viewViewDetails = bottomView.findViewById(R.id.viewViewDetails);
            LinearLayout optionPaused = bottomView.findViewById(R.id.optionPaused);
            View viewPaused = bottomView.findViewById(R.id.viewPaused);
            LinearLayout optionResume = bottomView.findViewById(R.id.optionResume);
            View viewResume = bottomView.findViewById(R.id.viewResume);
            LinearLayout optionArchive = bottomView.findViewById(R.id.optionArchive);
            View viewArchive = bottomView.findViewById(R.id.viewArchive);
            LinearLayout optionRestore = bottomView.findViewById(R.id.optionRestore);
            View viewRestore = bottomView.findViewById(R.id.viewRestore);
            LinearLayout optionDelete = bottomView.findViewById(R.id.optionDelete);

            BudgetEntity budget = budgetDetail.budget;

            colorView.setCardBackgroundColor(Color.parseColor(budget.budgetColor));
            ivBudgetIcon.setImageDrawable(ContextCompat.getDrawable(this, DataHelper.getCategoryIcons().get(budget.budgetIcon)));
            nameLabel.setText(budget.name);
            tvBudgetPeriod.setText(DataHelper.getPeriodName(budget.periodId));
            tvBudgetAmount.setText(CommonUtils.getBeautifyAmount(budget.currencySymbol, budget.amount));

            optionViewDetails.setVisibility(View.GONE);
            viewViewDetails.setVisibility(View.GONE);

            if (budget.isPaused) {
                optionEdit.setVisibility(View.VISIBLE);
                viewEdit.setVisibility(View.VISIBLE);
                optionResume.setVisibility(View.VISIBLE);
                viewResume.setVisibility(View.VISIBLE);
                optionArchive.setVisibility(View.VISIBLE);
                viewArchive.setVisibility(View.VISIBLE);
                optionPaused.setVisibility(View.GONE);
                viewPaused.setVisibility(View.GONE);
                optionRestore.setVisibility(View.GONE);
                viewRestore.setVisibility(View.GONE);
            } else if (budget.isArchived) {
                optionEdit.setVisibility(View.GONE);
                viewEdit.setVisibility(View.GONE);
                optionPaused.setVisibility(View.GONE);
                viewPaused.setVisibility(View.GONE);
                optionArchive.setVisibility(View.GONE);
                viewArchive.setVisibility(View.GONE);
                optionRestore.setVisibility(View.VISIBLE);
                viewRestore.setVisibility(View.VISIBLE);
            } else {
                optionEdit.setVisibility(View.VISIBLE);
                viewEdit.setVisibility(View.VISIBLE);

                if (budgetDetail.spentAmount >= budget.amount) {
                    optionPaused.setVisibility(View.GONE);
                    viewPaused.setVisibility(View.GONE);
                } else {
                    optionPaused.setVisibility(View.VISIBLE);
                    viewPaused.setVisibility(View.VISIBLE);
                }

                optionArchive.setVisibility(View.VISIBLE);
                viewArchive.setVisibility(View.VISIBLE);
            }

            // EDIT
            optionEdit.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(BudgetDetailActivity.this, CreateBudgetActivity.class)
                        .putExtra("isEdit", true)
                        .putExtra("budgetId", budget.id));
                ActivityUtils.overrideOpenTransition(BudgetDetailActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            // PAUSE
            optionPaused.setOnClickListener(v -> {
                dialog.dismiss();
                showPauseDialog(budget);
            });

            // RESUME
            optionResume.setOnClickListener(v -> {
                dialog.dismiss();
                showResumeDialog(budget);
            });

            // RESTORE
            optionRestore.setOnClickListener(v -> {
                dialog.dismiss();
                showRestoreDialog(budget);
            });

            // ARCHIVE
            optionArchive.setOnClickListener(v -> {
                dialog.dismiss();
                showArchiveDialog(budget);
            });

            // DELETE
            optionDelete.setOnClickListener(v -> {
                dialog.dismiss();
                showDeleteDialog(budget);
            });

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showOptionDialog", e);
        }
    }

    private void showArchiveDialog(BudgetEntity budget) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);

        MaterialCardView cardHeader = view.findViewById(R.id.cardHeader);
        AppCompatImageView headerImage = view.findViewById(R.id.headerImage);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        MaterialButton tvDelete = view.findViewById(R.id.tvDelete);
        tvTitle.setText(R.string.archive_budget);
        tvMessage.setText(R.string.archive_budget_message);
        tvSubMessage.setText(R.string.archive_budget_sub_message);
        tvSubMessage.setVisibility(View.VISIBLE);
        tvDelete.setText(getString(R.string.archive));

        cardHeader.setCardBackgroundColor(getColor(R.color.category_light));
        headerImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_archive_outline));
        headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.category_dark)));
        tvDelete.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.category_dark)));

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            archiveBudget(budget);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void archiveBudget(BudgetEntity budget) {
        try {
            if (budget == null) {
                return;
            }

            if (budgetViewModel.archiveRestoreBudget(budget.id, true, false)) {
                Toast.makeText(getApplicationContext(), getString(R.string.budget_archived), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_budget_archive), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "archiveBudget", e);
        }
    }

    private void showRestoreDialog(BudgetEntity budget) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);

        MaterialCardView cardHeader = view.findViewById(R.id.cardHeader);
        AppCompatImageView headerImage = view.findViewById(R.id.headerImage);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        MaterialButton tvDelete = view.findViewById(R.id.tvDelete);
        tvTitle.setText(R.string.restore_budget);
        tvMessage.setText(R.string.restore_budget_message);
        tvSubMessage.setText(R.string.restore_budget_sub_message);
        tvSubMessage.setVisibility(View.VISIBLE);
        tvDelete.setText(getString(R.string.restore));

        cardHeader.setCardBackgroundColor(getColor(R.color.backup_light));
        headerImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_refresh));
        headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.backup_dark)));
        tvDelete.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_dark)));

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            restoreBudget(budget);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void restoreBudget(BudgetEntity budget) {
        try {
            if (budget == null) {
                return;
            }

            if (budgetViewModel.archiveRestoreBudget(budget.id, false, false)) {
                Toast.makeText(getApplicationContext(), getString(R.string.budget_restored), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_budget_restore), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "archiveBudget", e);
        }
    }

    private void showPauseDialog(BudgetEntity budget) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);

        MaterialCardView cardHeader = view.findViewById(R.id.cardHeader);
        AppCompatImageView headerImage = view.findViewById(R.id.headerImage);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        MaterialButton tvDelete = view.findViewById(R.id.tvDelete);
        tvTitle.setText(R.string.pause_budget);
        tvMessage.setText(R.string.pause_budget_message);
        tvSubMessage.setText(R.string.pause_budget_sub_message);
        tvSubMessage.setVisibility(View.VISIBLE);
        tvDelete.setText(getString(R.string.text_pause));

        cardHeader.setCardBackgroundColor(getColor(R.color.color_pause_light));
        headerImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_outline));
        headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_pause_dark)));
        tvDelete.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_pause_dark)));

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            pauseBudget(budget);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void pauseBudget(BudgetEntity budget) {
        try {
            if (budget == null) {
                return;
            }

            if (budgetViewModel.pauseBudget(budget.id, true)) {
                Toast.makeText(getApplicationContext(), getString(R.string.budget_paused), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_budget_pause), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "pauseBudget", e);
        }
    }

    private void showResumeDialog(BudgetEntity budget) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);

        MaterialCardView cardHeader = view.findViewById(R.id.cardHeader);
        AppCompatImageView headerImage = view.findViewById(R.id.headerImage);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        MaterialButton tvDelete = view.findViewById(R.id.tvDelete);
        tvTitle.setText(R.string.resume_budget);
        tvMessage.setText(R.string.resume_budget_message);
        tvSubMessage.setText(R.string.resume_budget_sub_message);
        tvSubMessage.setVisibility(View.VISIBLE);
        tvDelete.setText(getString(R.string.text_resume));

        cardHeader.setCardBackgroundColor(getColor(R.color.resume_light));
        headerImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_resume_outline));
        headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.resume_dark)));
        tvDelete.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.resume_dark)));

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            resumeBudget(budget);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void resumeBudget(BudgetEntity budget) {
        try {
            if (budget == null) {
                return;
            }

            if (budgetViewModel.pauseBudget(budget.id, false)) {
                Toast.makeText(getApplicationContext(), getString(R.string.budget_resumed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_budget_resume), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "pauseBudget", e);
        }
    }

    private void showDeleteDialog(BudgetEntity budget) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        tvTitle.setText(R.string.delete_budget);
        tvMessage.setText(R.string.delete_budget_message);
        tvSubMessage.setText(R.string.delete_budget_sub_message);
        tvSubMessage.setVisibility(View.VISIBLE);
        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            deleteBudget(budget);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteBudget(BudgetEntity budget) {
        try {
            if (budget == null) {
                return;
            }

            if (budgetViewModel.deleteBudget(budget.id)) {
                Toast.makeText(getApplicationContext(), R.string.budget_deleted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.error_delete_budget, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteBudget", e);
        }
    }

    private void setBudgetStatusStyle(AppCompatTextView tvBudgetStatus, int textColor, int backgroundColor, int strokeColor) {

        tvBudgetStatus.setTextColor(ContextCompat.getColor(this, textColor));
        Drawable background = AppCompatResources.getDrawable(this, R.drawable.bg_badge_income);

        if (background != null) {
            background = background.mutate();
            if (background instanceof GradientDrawable drawable) {
                drawable.setColor(ContextCompat.getColor(this, backgroundColor));
                drawable.setStroke(CommonUtils.dpToPx(this, 1), ContextCompat.getColor(this, strokeColor));
            }

            tvBudgetStatus.setBackground(background);
        }
    }

    private void updateRecyclerViewMaxHeight() {
        rootView.post(() -> {
            int availableHeight = rootView.getHeight();
            int cardMargins = CommonUtils.dpToPx(this, 20);
            int maxHeight = availableHeight - cardMargins;
            if (maxHeight > 0) {
                rvTransactions.setMaxHeight(maxHeight);
                rvCategoryTransactions.setMaxHeight(maxHeight);
            }
        });
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
    }
}