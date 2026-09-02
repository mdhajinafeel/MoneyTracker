package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.constants.GoalContributionType;
import com.nprotech.moneytracker.db.entites.GoalContributionEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.models.GoalContributionWithCurrency;
import com.nprotech.moneytracker.models.GoalWithDetails;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.GoalViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GoalDetailActivity extends BaseActivity {

    private AppCompatImageView icBack, ivGoalIcon, ivMore, ivEditAutoSaveImage;
    private AppCompatTextView tvTargetDate, tvTargetDays, tvGoalCategory, tvGoalName, tvGoalSavedAmount, tvGoalTargetAmount, lblGoalRemainingAmount, tvGoalRemainingAmount,
            tvAutoSaveAmount, tvFrequency, tvNextSave, tvStartDate, tvGoalProgress, tvAutoSaveHint, tvAutoSaveDisabled, lblViewAllContribution, tvGoalStatus, tvGoalReachHint,
            tvCategory, tvCreatedOn, tvNotes, tvGoalId, lblNoContributions, tvAutoSaveSummaryAmount, tvAutoSaveSummaryCount, tvManualSummaryAmount, tvManualSummaryCount,
            tvInitialSummaryAmount, tvInitialSummaryCount, tvWithdrawalSummaryAmount, tvWithdrawalSummaryCount;
    private ProgressBar progressGoal, progressRingGoal;
    private ConstraintLayout autoSaveContainer, autoSaveDisabledContainer, disableContainer;
    private MaterialButton btnEnableAutoSave;
    private RecyclerView rvTransactions;
    private GoalViewModel goalViewModel;
    private GoalWithDetails goal;
    private boolean isGoalCompleted = false, isGoalArchived = false;
    private double remainingAmount = 0;
    private String currencySymbol = "";
    private RecyclerViewAdapter<GoalContributionWithCurrency> contributionRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_details);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            View root = findViewById(R.id.rootView);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            ivMore = toolbarWrapper.findViewById(R.id.ivMore);
            ivMore.setVisibility(View.VISIBLE);

            ivGoalIcon = findViewById(R.id.ivGoalIcon);
            tvGoalName = findViewById(R.id.tvGoalName);
            tvGoalCategory = findViewById(R.id.tvGoalCategory);
            tvTargetDays = findViewById(R.id.tvTargetDays);
            tvTargetDate = findViewById(R.id.tvTargetDate);
            tvGoalStatus = findViewById(R.id.tvGoalStatus);
            tvGoalSavedAmount = findViewById(R.id.tvGoalSavedAmount);
            tvGoalTargetAmount = findViewById(R.id.tvGoalTargetAmount);
            lblGoalRemainingAmount = findViewById(R.id.lblGoalRemainingAmount);
            tvGoalRemainingAmount = findViewById(R.id.tvGoalRemainingAmount);
            progressGoal = findViewById(R.id.progressGoal);
            tvGoalReachHint = findViewById(R.id.tvGoalReachHint);
            progressRingGoal = findViewById(R.id.progressRingGoal);
            tvGoalProgress = findViewById(R.id.tvGoalProgress);
            tvAutoSaveAmount = findViewById(R.id.tvAutoSaveAmount);
            tvFrequency = findViewById(R.id.tvFrequency);
            tvNextSave = findViewById(R.id.tvNextSave);
            tvStartDate = findViewById(R.id.tvStartDate);
            tvAutoSaveHint = findViewById(R.id.tvAutoSaveHint);
            tvAutoSaveDisabled = findViewById(R.id.tvAutoSaveDisabled);
            lblViewAllContribution = findViewById(R.id.lblViewAllContribution);
            tvCategory = findViewById(R.id.tvCategory);
            tvCreatedOn = findViewById(R.id.tvCreatedOn);
            tvNotes = findViewById(R.id.tvNotes);
            tvGoalId = findViewById(R.id.tvGoalId);
            lblNoContributions = findViewById(R.id.lblNoContributions);
            rvTransactions = findViewById(R.id.rvTransactions);
            tvAutoSaveSummaryAmount = findViewById(R.id.tvAutoSaveSummaryAmount);
            tvAutoSaveSummaryCount = findViewById(R.id.tvAutoSaveSummaryCount);
            tvManualSummaryAmount = findViewById(R.id.tvManualSummaryAmount);
            tvManualSummaryCount = findViewById(R.id.tvManualSummaryCount);
            tvInitialSummaryAmount = findViewById(R.id.tvInitialSummaryAmount);
            tvInitialSummaryCount = findViewById(R.id.tvInitialSummaryCount);
            tvWithdrawalSummaryAmount = findViewById(R.id.tvWithdrawalSummaryAmount);
            tvWithdrawalSummaryCount = findViewById(R.id.tvWithdrawalSummaryCount);
            autoSaveContainer = findViewById(R.id.autoSaveContainer);
            autoSaveDisabledContainer = findViewById(R.id.autoSaveDisabledContainer);
            disableContainer = findViewById(R.id.disableContainer);
            btnEnableAutoSave = findViewById(R.id.btnEnableAutoSave);
            ivEditAutoSaveImage = findViewById(R.id.ivEditAutoSaveImage);

            tvTitle.setText(getString(R.string.goal_details));

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {

                goalViewModel = new ViewModelProvider(this).get(GoalViewModel.class);

                bindData(bundle);
                initializeAdapter();
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
            int goalId = bundle.getInt("goalId", 0);

            if (goalId > 0) {

                goalViewModel.getGoalDetailById(goalId).observe(this, goalWithDetail -> {
                    if (goalWithDetail != null) {

                        goal = goalWithDetail;

                        int goalColor = Color.parseColor(goalWithDetail.color);
                        long daysLeft = CommonUtils.calculateDaysLeft(goalWithDetail.targetDate);
                        int progress = CommonUtils.calculateGoalProgress(goalWithDetail.savedAmount, goalWithDetail.targetAmount);
                        currencySymbol = goalWithDetail.currencySymbol;

                        Drawable background = ivGoalIcon.getBackground().mutate();
                        DrawableCompat.setTint(background, Color.parseColor(goalWithDetail.color));
                        ivGoalIcon.setBackground(background);
                        ivGoalIcon.setImageResource(DataHelper.getGoalIcons().get(goalWithDetail.icon));

                        tvGoalName.setText(goalWithDetail.name);
                        tvGoalCategory.setText(goalWithDetail.categoryName);
                        tvTargetDate.setText(DateHelper.getFormattedDate(goalWithDetail.targetDate, "dd MMM yyyy"));

                        isGoalCompleted = goalWithDetail.isCompleted;
                        isGoalArchived = goalWithDetail.isArchived;

                        if (isGoalArchived) {
                            tvTargetDays.setText(DateHelper.getFormattedDate(goalWithDetail.archivedOn, "dd MMM yyyy"));
                            tvGoalStatus.setText(getString(R.string.archived));
                            tvGoalStatus.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
                            setGoalStatusStyle(R.color.primary_dark, R.color.light_lavender, R.color.primary_dark);
                        } else if (isGoalCompleted) {
                            tvTargetDays.setText(DateHelper.getFormattedDate(goalWithDetail.completedOn, "dd MMM yyyy"));
                            tvGoalStatus.setText(getString(R.string.achieved));
                            tvGoalStatus.setTextColor(ContextCompat.getColor(this, R.color.income));
                            setGoalStatusStyle(R.color.income, R.color.light_income, R.color.income);
                        } else if (daysLeft == 0) {

                            // Due today
                            tvTargetDays.setText(getString(R.string.due_today));
                            tvGoalStatus.setText(getString(R.string.in_progress));

                            tvGoalStatus.setTextColor(ContextCompat.getColor(this, R.color.orange));
                            setGoalStatusStyle(R.color.orange, R.color.light_orange, R.color.orange);

                        } else if (daysLeft < 0) {

                            // Overdue
                            long overdueDays = Math.abs(daysLeft);

                            tvTargetDays.setText(getResources().getQuantityString(R.plurals.days_overdue, (int) overdueDays, overdueDays));
                            tvGoalStatus.setText(getString(R.string.overdue));
                            tvGoalStatus.setTextColor(ContextCompat.getColor(this, R.color.expense));
                            setGoalStatusStyle(R.color.expense, R.color.light_expense, R.color.expense);
                        } else {

                            // In Progress
                            tvTargetDays.setText(getResources().getQuantityString(R.plurals.days_count, (int) daysLeft, daysLeft));
                            tvGoalStatus.setText(getString(R.string.in_progress));
                            tvGoalStatus.setTextColor(ContextCompat.getColor(this, R.color.orange));
                            setGoalStatusStyle(R.color.orange, R.color.light_orange, R.color.orange);
                        }

                        tvGoalSavedAmount.setText(CommonUtils.getBeautifyAmount(goalWithDetail.currencySymbol, goalWithDetail.savedAmount));
                        tvGoalTargetAmount.setText(CommonUtils.getBeautifyAmount(goalWithDetail.currencySymbol, goalWithDetail.targetAmount));

                        remainingAmount = goalWithDetail.targetAmount - goalWithDetail.savedAmount;
                        if (remainingAmount < 0) {
                            lblGoalRemainingAmount.setText(getString(R.string.extra_amount));
                            tvGoalRemainingAmount.setText(CommonUtils.getBeautifyAmount(goalWithDetail.currencySymbol, Math.abs(remainingAmount)));
                        } else {
                            lblGoalRemainingAmount.setText(getString(R.string.remaining_amount));
                            tvGoalRemainingAmount.setText(CommonUtils.getBeautifyAmount(goalWithDetail.currencySymbol, remainingAmount));
                        }

                        if (progress < 100) {
                            tvGoalProgress.setText(getResources().getString(R.string.progress_percentage_target, progress));
                        } else {
                            tvGoalProgress.setText(getResources().getString(R.string.progress_percentage_completed, progress));
                        }
                        progressRingGoal.setProgressDrawable(CommonUtils.createGoalRingDrawable(this, goalColor));
                        progressGoal.setProgressDrawable(CommonUtils.createGoalProgressDrawable(this, goalColor));
                        progressRingGoal.setProgress(progress);
                        progressGoal.setProgress(progress);

                        //-------------------------
                        //----- GOAL AUTO SAVE ---------
                        //-------------------------

                        if (goalWithDetail.autoSaveEnabled) {
                            autoSaveContainer.setVisibility(View.VISIBLE);
                            autoSaveDisabledContainer.setVisibility(View.GONE);
                            disableContainer.setVisibility(View.VISIBLE);

                            tvAutoSaveAmount.setText(CommonUtils.getBeautifyAmount(goalWithDetail.currencySymbol, goalWithDetail.autoSaveAmount));

                            if (goalWithDetail.autoSaveFrequency == Constants.GOAL_FREQUENCY_DAILY) {
                                tvFrequency.setText(getString(R.string.calendar_daily));
                            } else if (goalWithDetail.autoSaveFrequency == Constants.GOAL_FREQUENCY_WEEKLY) {
                                tvFrequency.setText(getString(R.string.calendar_weekly));
                            } else if (goalWithDetail.autoSaveFrequency == Constants.GOAL_FREQUENCY_MONTHLY) {
                                tvFrequency.setText(getString(R.string.calendar_monthly));
                            } else if (goalWithDetail.autoSaveFrequency == Constants.GOAL_FREQUENCY_YEARLY) {
                                tvFrequency.setText(getString(R.string.calendar_yearly));
                            }

                            if (isGoalArchived) {
                                btnEnableAutoSave.setEnabled(false);
                                btnEnableAutoSave.setAlpha(0.5f);
                            } else if (isGoalCompleted) {
                                tvNextSave.setText(getString(R.string.dash));
                                btnEnableAutoSave.setEnabled(false);
                                btnEnableAutoSave.setAlpha(0.5f);
                            } else {
                                tvNextSave.setText(DateHelper.getFormattedDate(goalWithDetail.nextAutoSaveDate, "dd MMM yyyy"));
                                btnEnableAutoSave.setEnabled(true);
                                btnEnableAutoSave.setAlpha(1f);
                            }

                            tvStartDate.setText(DateHelper.getFormattedDate(goalWithDetail.autoSaveStartDate, "dd MMM yyyy"));
                        } else {
                            autoSaveContainer.setVisibility(View.GONE);
                            autoSaveDisabledContainer.setVisibility(View.VISIBLE);
                            disableContainer.setVisibility(View.GONE);

                            if (isGoalArchived) {
                                btnEnableAutoSave.setEnabled(false);
                                btnEnableAutoSave.setAlpha(0.5f);
                            } else if (isGoalCompleted) {
                                btnEnableAutoSave.setEnabled(false);
                                btnEnableAutoSave.setAlpha(0.5f);
                            } else {
                                btnEnableAutoSave.setEnabled(true);
                                btnEnableAutoSave.setAlpha(1f);
                            }
                        }

                        //-------------------------
                        //----- GOAL INFO ---------
                        //-------------------------
                        tvCategory.setText(goalWithDetail.categoryName);
                        tvCreatedOn.setText(DateHelper.getFormattedDate(goalWithDetail.createdAt, "dd MMM yyyy"));
                        tvNotes.setText(goalWithDetail.notes == null || goalWithDetail.notes.isEmpty() ? getString(R.string.dash) : goalWithDetail.notes);
                        tvGoalId.setText(CommonUtils.getDisplayId(goalWithDetail.id, "GOAL"));
                    }

                    updateFields();
                    loadContribution(goalId);
                });
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void loadContribution(int goalId) {

        goalViewModel.getContributionSummary(goalId, GoalContributionType.AUTO_SAVE, GoalContributionType.ADD,
                GoalContributionType.INITIAL, GoalContributionType.WITHDRAW).observe(this, goalContributionSummary -> {
            if (goalContributionSummary != null) {
                tvAutoSaveSummaryAmount.setText(CommonUtils.getBeautifyAmount(goal.currencySymbol, goalContributionSummary.autoSaveAmount));
                tvManualSummaryAmount.setText(CommonUtils.getBeautifyAmount(goal.currencySymbol, goalContributionSummary.manualAmount));
                tvInitialSummaryAmount.setText(CommonUtils.getBeautifyAmount(goal.currencySymbol, goalContributionSummary.initialAmount));
                tvWithdrawalSummaryAmount.setText(CommonUtils.getBeautifyAmount(goal.currencySymbol, goalContributionSummary.withdrawalAmount));

                tvAutoSaveSummaryCount.setText(getResources().getQuantityString(R.plurals.contribution_count, goalContributionSummary.autoSaveCount, goalContributionSummary.autoSaveCount));
                tvManualSummaryCount.setText(getResources().getQuantityString(R.plurals.contribution_count, goalContributionSummary.manualCount, goalContributionSummary.manualCount));
                tvInitialSummaryCount.setText(getResources().getQuantityString(R.plurals.contribution_count, goalContributionSummary.initialCount, goalContributionSummary.initialCount));
                tvWithdrawalSummaryCount.setText(getResources().getQuantityString(R.plurals.contribution_count, goalContributionSummary.withdrawalCount, goalContributionSummary.withdrawalCount));
            }
        });

        goalViewModel.getRecentContributions(goalId).observe(this, goalContributions -> {
            if (goalContributions != null && !goalContributions.isEmpty()) {
                contributionRecyclerViewAdapter.setItems(goalContributions);
                lblNoContributions.setVisibility(View.GONE);
                rvTransactions.setVisibility(View.VISIBLE);
                lblViewAllContribution.setVisibility(View.VISIBLE);
            } else {
                rvTransactions.setVisibility(View.GONE);
                lblNoContributions.setVisibility(View.VISIBLE);
                lblViewAllContribution.setVisibility(View.GONE);
            }
        });
    }

    private void initializeAdapter() {

        contributionRecyclerViewAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(), R.layout.item_goal_contributions) {
            @Override
            public void onPostBindViewHolder(ViewHolder holder, GoalContributionWithCurrency goalContribution) {

                GoalContributionEntity contribution = goalContribution.getContribution();

                AppCompatImageView ivContributionIcon = holder.getView(R.id.ivContributionIcon);
                AppCompatTextView tvContributionType = holder.getView(R.id.tvContributionType);
                AppCompatTextView tvContributionDate = holder.getView(R.id.tvContributionDate);
                AppCompatTextView tvContributionAmount = holder.getView(R.id.tvContributionAmount);

                boolean withdrawal = contribution.getType() == GoalContributionType.WITHDRAW;

                // --------------------------------
                // Type
                // --------------------------------
                if (contribution.getType() == GoalContributionType.AUTO_SAVE) {
                    tvContributionType.setText(R.string.auto_save);
                    ivContributionIcon.setImageResource(R.drawable.ic_refresh);

                    ivContributionIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.light_income));
                    ivContributionIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.income));
                } else if (contribution.getType() == GoalContributionType.ADD) {
                    tvContributionType.setText(R.string.manual);
                    ivContributionIcon.setImageResource(R.drawable.ic_wallet);

                    ivContributionIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.light_lavender));
                    ivContributionIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primary_dark));
                } else if (contribution.getType() == GoalContributionType.INITIAL) {
                    tvContributionType.setText(R.string.initial_amount);
                    ivContributionIcon.setImageResource(R.drawable.ic_money_bag);

                    ivContributionIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.light_orange));
                    ivContributionIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.orange));
                } else if (contribution.getType() == GoalContributionType.WITHDRAW) {
                    tvContributionType.setText(R.string.withdrawal);
                    ivContributionIcon.setImageResource(R.drawable.ic_withdraw);

                    ivContributionIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.light_expense));
                    ivContributionIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.expense));
                }

                // --------------------------------
                // Date
                // --------------------------------
                tvContributionDate.setText(DateHelper.getFormattedDate(contribution.getDate(), "dd MMM yyyy, hh:mm a"));

                // --------------------------------
                // Amount
                // --------------------------------

                String amount = CommonUtils.getBeautifyAmount(goalContribution.getCurrencySymbol(), contribution.getAmount());
                if (withdrawal) {
                    tvContributionAmount.setText(getString(R.string.minus_amount, amount));
                    tvContributionAmount.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.expense));
                } else {
                    tvContributionAmount.setText(getString(R.string.plus_amount, amount));
                    tvContributionAmount.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.income));
                }

                int position = holder.getBindingAdapterPosition();

                if (position == getItemCount() - 1) {
                    holder.getView(R.id.divider).setAlpha(0f);
                } else {
                    holder.getView(R.id.divider).setAlpha(1f);
                }

                holder.getView(R.id.layoutView).setOnClickListener(v -> showContributionOptionDialog(goalContribution));
            }
        };

        rvTransactions.setHasFixedSize(true);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setItemAnimator(null);
        rvTransactions.setNestedScrollingEnabled(false);
        rvTransactions.setAdapter(contributionRecyclerViewAdapter);
    }

    private void updateFields() {
        try {
            long daysLeft = CommonUtils.calculateDaysLeft(goal.targetDate);

            if (isGoalArchived) {
                Drawable statusDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_archive);
                if (statusDrawable != null) {
                    int size = getResources().getDimensionPixelSize(R.dimen.icon_10);
                    statusDrawable.setBounds(0, 0, size, size);
                    tvGoalStatus.setCompoundDrawablesRelative(null, null, statusDrawable, null);
                    TextViewCompat.setCompoundDrawableTintList(tvGoalStatus, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_dark)));
                }
            } else if (daysLeft < 0) {
                // Overdue
                Drawable statusDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_overdue);
                if (statusDrawable != null) {
                    int size = getResources().getDimensionPixelSize(R.dimen.icon_10);
                    statusDrawable.setBounds(0, 0, size, size);
                    tvGoalStatus.setCompoundDrawablesRelative(null, null, statusDrawable, null);
                    TextViewCompat.setCompoundDrawableTintList(tvGoalStatus, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.expense)));
                }

            } else if (isGoalCompleted) {
                Drawable statusDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_complete);
                if (statusDrawable != null) {
                    int size = getResources().getDimensionPixelSize(R.dimen.icon_10);
                    statusDrawable.setBounds(0, 0, size, size);
                    tvGoalStatus.setCompoundDrawablesRelative(null, null, statusDrawable, null);
                    TextViewCompat.setCompoundDrawableTintList(tvGoalStatus, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.income)));
                }
            } else {
                Drawable statusDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_time_solid);
                if (statusDrawable != null) {
                    int size = getResources().getDimensionPixelSize(R.dimen.icon_10);
                    statusDrawable.setBounds(0, 0, size, size);
                    tvGoalStatus.setCompoundDrawablesRelative(statusDrawable, null, null, null);
                    TextViewCompat.setCompoundDrawableTintList(tvGoalStatus, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.orange)));
                }
            }

            Drawable categoryDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_category);
            if (categoryDrawable != null) {
                int size = getResources().getDimensionPixelSize(R.dimen.icon_12);
                categoryDrawable.setBounds(0, 0, size, size);
                tvGoalCategory.setCompoundDrawablesRelative(categoryDrawable, null, null, null);
            }

            if (isGoalArchived) {
                Drawable calendarDaysDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_calendar_archive);
                if (calendarDaysDrawable != null) {
                    int size = getResources().getDimensionPixelSize(R.dimen.icon_12);
                    calendarDaysDrawable.setBounds(0, 0, size, size);
                    tvTargetDays.setCompoundDrawablesRelative(calendarDaysDrawable, null, null, null);
                }
            } else if (isGoalCompleted) {
                Drawable calendarDaysDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_calendar_done);
                if (calendarDaysDrawable != null) {
                    int size = getResources().getDimensionPixelSize(R.dimen.icon_12);
                    calendarDaysDrawable.setBounds(0, 0, size, size);
                    tvTargetDays.setCompoundDrawablesRelative(calendarDaysDrawable, null, null, null);
                }
            } else {

                int iconRes;
                if (daysLeft < 0) {
                    // Overdue
                    iconRes = R.drawable.ic_calendar_overdue;
                } else {
                    // In progress / Due today
                    iconRes = R.drawable.ic_calendar_days;
                }

                Drawable calendarDaysDrawable = AppCompatResources.getDrawable(this, iconRes);

                if (calendarDaysDrawable != null) {
                    int size = getResources().getDimensionPixelSize(R.dimen.icon_12);
                    calendarDaysDrawable.setBounds(0, 0, size, size);
                    tvTargetDays.setCompoundDrawablesRelative(calendarDaysDrawable, null, null, null);
                }
            }

            Drawable calendarDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_calendar);
            if (calendarDrawable != null) {
                int size = getResources().getDimensionPixelSize(R.dimen.icon_12);
                calendarDrawable.setBounds(0, 0, size, size);
                tvTargetDate.setCompoundDrawablesRelative(calendarDrawable, null, null, null);
            }

            Drawable privacyDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_privacy);
            if (privacyDrawable != null) {
                int size = getResources().getDimensionPixelSize(R.dimen.icon_10);
                privacyDrawable.setBounds(0, 0, size, size);
                tvAutoSaveHint.setCompoundDrawablesRelative(privacyDrawable, null, null, null);
            }

            Drawable disabledDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_disabled);
            if (disabledDrawable != null) {
                int size = getResources().getDimensionPixelSize(R.dimen.icon_12);
                disabledDrawable.setBounds(0, 0, size, size);
                tvAutoSaveDisabled.setCompoundDrawablesRelative(disabledDrawable, null, null, null);
            }

            Drawable arrowDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_right_arrow);
            if (arrowDrawable != null) {
                int size = getResources().getDimensionPixelSize(R.dimen.icon_12);
                arrowDrawable.setBounds(0, 0, size, size);
                lblViewAllContribution.setCompoundDrawablesRelative(null, null, arrowDrawable, null);
            }

            if (isGoalCompleted) {
                tvGoalReachHint.setVisibility(View.VISIBLE);
                if (remainingAmount < 0) {
                    tvGoalReachHint.setText(getString(R.string.goal_reach_saved, CommonUtils.getBeautifyAmount(currencySymbol, Math.abs(remainingAmount))));
                } else {
                    tvGoalReachHint.setText(getString(R.string.goal_reached));
                }
            } else {
                tvGoalReachHint.setVisibility(View.GONE);
            }

            Drawable confettiDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_confeti);
            if (confettiDrawable != null) {
                int size = getResources().getDimensionPixelSize(R.dimen.icon_10);
                confettiDrawable.setBounds(0, 0, size, size);
                tvGoalReachHint.setCompoundDrawablesRelative(null, null, confettiDrawable, null);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "updateFields", e);
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            });

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finish();
                    ActivityUtils.overrideCloseTransition(GoalDetailActivity.this, R.anim.scale_in, R.anim.right_to_left);
                }
            });

            tvAutoSaveDisabled.setOnClickListener(v -> showDisabledDialog(goal));

            ivMore.setOnClickListener(v -> showOptionDialog(goal));

            btnEnableAutoSave.setOnClickListener(v -> {
                startActivity(new Intent(this, CreateAutoSaveActivity.class)
                        .putExtra("type", "goal")
                        .putExtra("isEdit", false)
                        .putExtra("currencySymbol", goal.currencySymbol)
                        .putExtra("goalId", goal.id));
                ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            lblViewAllContribution.setOnClickListener(v -> {
                startActivity(new Intent(GoalDetailActivity.this, ContributionsListActivity.class)
                        .putExtra("type", "goal")
                        .putExtra("goalId", goal.id));
                ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            ivEditAutoSaveImage.setOnClickListener(v -> {
                startActivity(new Intent(this, CreateAutoSaveActivity.class)
                        .putExtra("type", "goal")
                        .putExtra("isEdit", true)
                        .putExtra("currencySymbol", goal.currencySymbol)
                        .putExtra("goalId", goal.id));
                ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void showOptionDialog(GoalWithDetails goal) {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_goal_options, findViewById(android.R.id.content), false);
            AppCompatTextView tvGoalName = bottomView.findViewById(R.id.tvGoalName);
            AppCompatTextView tvGoalAmount = bottomView.findViewById(R.id.tvGoalAmount);

            LinearLayout optionAddMoney = bottomView.findViewById(R.id.optionAddMoney);
            View viewAddMoney = bottomView.findViewById(R.id.viewAddMoney);
            LinearLayout optionEdit = bottomView.findViewById(R.id.optionEdit);
            View viewEdit = bottomView.findViewById(R.id.viewEdit);
            LinearLayout optionWithdrawal = bottomView.findViewById(R.id.optionWithdrawal);
            View viewWithdrawal = bottomView.findViewById(R.id.viewWithdrawal);
            LinearLayout optionComplete = bottomView.findViewById(R.id.optionComplete);
            View viewComplete = bottomView.findViewById(R.id.viewComplete);
            LinearLayout optionRestore = bottomView.findViewById(R.id.optionRestore);
            View viewRestore = bottomView.findViewById(R.id.viewRestore);
            LinearLayout optionArchive = bottomView.findViewById(R.id.optionArchive);
            LinearLayout optionDelete = bottomView.findViewById(R.id.optionDelete);
            View viewLineDelete = bottomView.findViewById(R.id.viewLineDelete);
            LinearLayout optionProgress = bottomView.findViewById(R.id.optionProgress);
            View viewProgress = bottomView.findViewById(R.id.viewProgress);

            optionDelete.setVisibility(View.VISIBLE);
            viewLineDelete.setVisibility(View.VISIBLE);

            if (isGoalArchived) {
                optionRestore.setVisibility(View.VISIBLE);
                viewRestore.setVisibility(View.VISIBLE);
            } else if (isGoalCompleted) {
                optionEdit.setVisibility(View.VISIBLE);
                viewEdit.setVisibility(View.VISIBLE);
                optionProgress.setVisibility(View.VISIBLE);
                viewProgress.setVisibility(View.VISIBLE);
                optionAddMoney.setVisibility(View.VISIBLE);
                viewAddMoney.setVisibility(View.VISIBLE);
                optionWithdrawal.setVisibility(View.VISIBLE);
                viewWithdrawal.setVisibility(View.VISIBLE);
                optionArchive.setVisibility(View.VISIBLE);
            } else {
                optionEdit.setVisibility(View.VISIBLE);
                viewEdit.setVisibility(View.VISIBLE);
                optionAddMoney.setVisibility(View.VISIBLE);
                viewAddMoney.setVisibility(View.VISIBLE);
                optionWithdrawal.setVisibility(View.VISIBLE);
                viewWithdrawal.setVisibility(View.VISIBLE);
                optionComplete.setVisibility(View.VISIBLE);
                viewComplete.setVisibility(View.VISIBLE);
                optionArchive.setVisibility(View.VISIBLE);
            }

            tvGoalName.setText(goal.name);

            String savedAmount = CommonUtils.getBeautifyAmount(goal.currencySymbol, goal.savedAmount);
            String targetAmount = CommonUtils.getBeautifyAmount(goal.currencySymbol, goal.targetAmount);
            tvGoalAmount.setText(getString(R.string.goal_amount_progress, savedAmount, targetAmount));

            // ADD MONEY
            optionAddMoney.setOnClickListener(view -> {
                dialog.dismiss();
                startActivity(new Intent(this, GoalMoneyActivity.class)
                        .putExtra("addMoney", true)
                        .putExtra("goalId", goal.id));
                ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            // WITHDRAW MONEY
            optionWithdrawal.setOnClickListener(view -> {
                dialog.dismiss();
                startActivity(new Intent(this, GoalMoneyActivity.class)
                        .putExtra("addMoney", false)
                        .putExtra("goalId", goal.id));
                ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            // EDIT DETAILS
            optionEdit.setOnClickListener(view -> {
                dialog.dismiss();
                startActivity(new Intent(this, CreateGoalActivity.class)
                        .putExtra("isEdit", true)
                        .putExtra("goalId", goal.id));
                ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            // COMPLETED
            optionComplete.setOnClickListener(view -> {
                dialog.dismiss();
                if (goalViewModel.markAsCompletedGoal(goal.id)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.goal_achieved_successfully), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_completing), Toast.LENGTH_SHORT).show();
                }
            });

            // PROGRESS
            optionProgress.setOnClickListener(v -> {
                dialog.dismiss();
                if (goalViewModel.markAsInProgressGoal(goal.id)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.goal_moved_successfully), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_move), Toast.LENGTH_SHORT).show();
                }
            });

            // ARCHIVE
            optionArchive.setOnClickListener(view -> {
                dialog.dismiss();
                showArchiveDialog(goal);
            });

            // RESTORE
            optionRestore.setOnClickListener(view -> {
                dialog.dismiss();
                if (goalViewModel.archiveRestoreGoal(goal.id, false)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.goal_restored_successfully), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_restore), Toast.LENGTH_SHORT).show();
                }
            });

            // DELETE
            optionDelete.setOnClickListener(view -> {
                dialog.dismiss();
                showDeleteDialog(goal);
            });

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showOptionDialog", e);
        }
    }

    private void showDeleteDialog(GoalWithDetails goal) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        tvTitle.setText(R.string.delete_goal);
        tvMessage.setText(R.string.delete_goal_message);
        tvSubMessage.setText(R.string.delete_goal_sub_message);
        tvSubMessage.setVisibility(View.VISIBLE);
        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            deleteGoal(goal);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteGoal(GoalWithDetails goal) {
        try {
            if (goal == null) {
                return;
            }

            if (goalViewModel.deleteGoal(goal.id)) {
                Toast.makeText(getApplicationContext(), getString(R.string.goal_deleted_successfully), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_delete_goal), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteGoal", e);
        }
    }

    private void showDisabledDialog(GoalWithDetails goal) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);

        MaterialCardView cardHeader = view.findViewById(R.id.cardHeader);
        AppCompatImageView headerImage = view.findViewById(R.id.headerImage);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        tvTitle.setText(R.string.disable_auto_save);
        tvMessage.setText(R.string.disable_message);

        cardHeader.setCardBackgroundColor(ContextCompat.getColor(this, R.color.dim_expense));
        headerImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_remove));
        headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.delete_red)));

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            disableAutoSave(goal);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void disableAutoSave(GoalWithDetails goal) {
        try {
            if (goal == null) {
                return;
            }

            if (goalViewModel.disableAutoSave(goal.id)) {
                Toast.makeText(getApplicationContext(), getString(R.string.auto_save_disabled), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_disable), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteGoal", e);
        }
    }

    private void showArchiveDialog(GoalWithDetails goal) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);

        MaterialCardView cardHeader = view.findViewById(R.id.cardHeader);
        AppCompatImageView headerImage = view.findViewById(R.id.headerImage);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        tvTitle.setText(R.string.archive_goal);
        tvMessage.setText(R.string.delete_archive_message);
        tvSubMessage.setText(R.string.delete_archive_sub_message);
        tvSubMessage.setVisibility(View.VISIBLE);

        cardHeader.setCardBackgroundColor(ContextCompat.getColor(this, R.color.category_light));
        headerImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_archive_outline));
        headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.category_dark)));

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            archiveGoal(goal);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void archiveGoal(GoalWithDetails goal) {
        try {
            if (goal == null) {
                return;
            }

            if (goalViewModel.archiveRestoreGoal(goal.id, true)) {
                Toast.makeText(getApplicationContext(), getString(R.string.goal_archived_successfully), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_archive), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteGoal", e);
        }
    }

    private void setGoalStatusStyle(int textColor, int backgroundColor, int strokeColor) {

        tvGoalStatus.setTextColor(ContextCompat.getColor(this, textColor));

        Drawable background = AppCompatResources.getDrawable(this, R.drawable.bg_badge_income);

        if (background != null) {
            background = background.mutate();
            if (background instanceof GradientDrawable drawable) {
                drawable.setColor(ContextCompat.getColor(this, backgroundColor));
                drawable.setStroke(CommonUtils.dpToPx(this, 1), ContextCompat.getColor(this, strokeColor));
            }

            tvGoalStatus.setBackground(background);
        }
    }

    private void showContributionOptionDialog(GoalContributionWithCurrency goal) {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_goal_contribution_action, findViewById(android.R.id.content), false);

            AppCompatImageView ivContributionIcon = bottomView.findViewById(R.id.ivContributionIcon);
            AppCompatTextView tvContributionType = bottomView.findViewById(R.id.tvContributionType);
            AppCompatTextView tvAmount = bottomView.findViewById(R.id.tvAmount);
            AppCompatTextView tvDateTime = bottomView.findViewById(R.id.tvDateTime);
            AppCompatTextView tvNotes = bottomView.findViewById(R.id.tvNotes);
            MaterialButton btnPrimary = bottomView.findViewById(R.id.btnPrimary);
            MaterialButton btnSecondary = bottomView.findViewById(R.id.btnSecondary);
            AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);

            GoalContributionEntity contribution = goal.getContribution();
            boolean withdrawal = contribution.getType() == GoalContributionType.WITHDRAW;

            if (contribution.getType() == GoalContributionType.AUTO_SAVE) {
                tvContributionType.setText(R.string.auto_save_contribution);
                ivContributionIcon.setImageResource(R.drawable.ic_refresh);

                ivContributionIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.light_income));
                ivContributionIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.income));
            } else if (contribution.getType() == GoalContributionType.ADD) {
                tvContributionType.setText(R.string.manual_contribution);
                ivContributionIcon.setImageResource(R.drawable.ic_wallet);

                ivContributionIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.light_lavender));
                ivContributionIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primary_dark));
            } else if (contribution.getType() == GoalContributionType.INITIAL) {
                tvContributionType.setText(R.string.initial_amount);
                ivContributionIcon.setImageResource(R.drawable.ic_money_bag);

                ivContributionIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.light_orange));
                ivContributionIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.orange));
            } else if (contribution.getType() == GoalContributionType.WITHDRAW) {
                tvContributionType.setText(R.string.withdrawal);
                ivContributionIcon.setImageResource(R.drawable.ic_withdraw);

                ivContributionIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.light_expense));
                ivContributionIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.expense));
            }

            String amount = CommonUtils.getBeautifyAmount(goal.getCurrencySymbol(), contribution.getAmount());
            if (withdrawal) {
                tvAmount.setText(getString(R.string.minus_amount, amount));
                tvAmount.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.expense));
            } else {
                tvAmount.setText(getString(R.string.plus_amount, amount));
                tvAmount.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.income));
            }

            tvDateTime.setText(DateHelper.getFormattedDate(contribution.getDate(), "dd MMM yyyy, hh:mm a"));
            tvNotes.setText(contribution.getNote() == null || contribution.getNote().isEmpty() ? getString(R.string.dash) : contribution.getNote());

            if (contribution.getType() == GoalContributionType.AUTO_SAVE || contribution.getType() == GoalContributionType.INITIAL) {
                btnSecondary.setText(getString(R.string.text_remove));
            } else {
                btnSecondary.setText(getString(R.string.delete));
            }

            btnPrimary.setOnClickListener(v -> {
                if (contribution.getType() == GoalContributionType.ADD) {
                    dialog.dismiss();
                    startActivity(new Intent(GoalDetailActivity.this, GoalMoneyActivity.class)
                            .putExtra("contributionId", contribution.getId())
                            .putExtra("addMoney", true)
                            .putExtra("goalId", contribution.getGoalId()));
                    ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
                } else if (contribution.getType() == GoalContributionType.WITHDRAW) {
                    dialog.dismiss();
                    startActivity(new Intent(GoalDetailActivity.this, GoalMoneyActivity.class)
                            .putExtra("contributionId", contribution.getId())
                            .putExtra("addMoney", false)
                            .putExtra("goalId", contribution.getGoalId()));
                    ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
                } else if (contribution.getType() == GoalContributionType.INITIAL) {
                    dialog.dismiss();
                    startActivity(new Intent(GoalDetailActivity.this, CreateGoalActivity.class)
                            .putExtra("isEdit", true)
                            .putExtra("goalId", contribution.getGoalId()));
                    ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
                } else if (contribution.getType() == GoalContributionType.AUTO_SAVE) {
                    dialog.dismiss();
                    startActivity(new Intent(this, CreateAutoSaveActivity.class)
                            .putExtra("type", "goal")
                            .putExtra("isEdit", true)
                            .putExtra("currencySymbol", goal.getCurrencySymbol())
                            .putExtra("goalId", contribution.getGoalId()));
                    ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
                }
            });

            btnSecondary.setOnClickListener(v -> {
                dialog.dismiss();
                showDeleteContributionDialog(goal);
            });

            tvClose.setOnClickListener(v -> dialog.dismiss());

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showOptionDialog", e);
        }
    }

    private void showDeleteContributionDialog(GoalContributionWithCurrency goal) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);

        MaterialCardView cardHeader = view.findViewById(R.id.cardHeader);
        AppCompatImageView headerImage = view.findViewById(R.id.headerImage);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);

        if (goal.getContribution().getType() == GoalContributionType.INITIAL) {
            tvTitle.setText(R.string.remove_initial_amount);
            tvMessage.setText(R.string.remove_initial_message);
            tvSubMessage.setText(R.string.remove_initial_sub_message);
            tvSubMessage.setVisibility(View.VISIBLE);

            cardHeader.setCardBackgroundColor(ContextCompat.getColor(this, R.color.dim_expense));
            headerImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_remove));
            headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.delete_red)));
        } else if (goal.getContribution().getType() == GoalContributionType.ADD) {
            tvTitle.setText(R.string.delete_contribution);
            tvMessage.setText(R.string.delete_contribution_message);

            cardHeader.setCardBackgroundColor(ContextCompat.getColor(this, R.color.dim_expense));
            headerImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_delete));
            headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.delete_red)));
        } else if (goal.getContribution().getType() == GoalContributionType.WITHDRAW) {
            tvTitle.setText(R.string.delete_withdrawal);
            tvMessage.setText(R.string.delete_withdrawal_message);

            cardHeader.setCardBackgroundColor(ContextCompat.getColor(this, R.color.dim_expense));
            headerImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_delete));
            headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.delete_red)));
        }

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            deleteContribution(goal);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteContribution(GoalContributionWithCurrency goal) {
        try {
            if (goal == null) {
                return;
            }

            if (goalViewModel.deleteContribution(goal.getContribution().getType(), goal.getContribution().getGoalId(), goal.getContribution().getId())) {
                Toast.makeText(getApplicationContext(), getString(R.string.contribution_deleted_successfully), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_delete_contribution), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteGoal", e);
        }
    }
}