package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.models.GoalWithDetails;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.GoalViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GoalDetailActivity extends BaseActivity {

    private AppCompatImageView icBack, ivGoalIcon, ivMore;
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
                        } else if (isGoalCompleted) {
                            tvTargetDays.setText(DateHelper.getFormattedDate(goalWithDetail.completedOn, "dd MMM yyyy"));
                            tvGoalStatus.setText(getString(R.string.achieved));
                        } else {
                            tvTargetDays.setText(getResources().getQuantityString(R.plurals.days_count, (int) daysLeft, daysLeft));
                            tvGoalStatus.setText(getString(R.string.in_progress));
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
                        tvNotes.setText(goalWithDetail.notes == null ? "" : goalWithDetail.notes);
                        tvGoalId.setText(CommonUtils.getDisplayId(goalWithDetail.id, "GOAL"));
                    }

                    updateFields();
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

    private void updateFields() {
        try {
            if (isGoalArchived) {
                Drawable statusDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_archive);
                if (statusDrawable != null) {
                    int size = getResources().getDimensionPixelSize(R.dimen.icon_10);
                    statusDrawable.setBounds(0, 0, size, size);
                    tvGoalStatus.setCompoundDrawablesRelative(null, null, statusDrawable, null);
                }
            } else if (isGoalCompleted) {
                Drawable statusDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_complete);
                if (statusDrawable != null) {
                    int size = getResources().getDimensionPixelSize(R.dimen.icon_10);
                    statusDrawable.setBounds(0, 0, size, size);
                    tvGoalStatus.setCompoundDrawablesRelative(null, null, statusDrawable, null);
                }
            } else {
                tvGoalStatus.setCompoundDrawablesRelative(null, null, null, null);
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
                Drawable calendarDaysDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_calendar_days);
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

            tvAutoSaveDisabled.setOnClickListener(v -> {
                showDisabledDialog(goal);
            });

            ivMore.setOnClickListener(v -> {
                showOptionDialog(goal);
            });

            btnEnableAutoSave.setOnClickListener(v -> {
                startActivity(new Intent(this, CreateAutoSaveActivity.class)
                        .putExtra("type", "goal")
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
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_sheet_goal_options, findViewById(android.R.id.content), false);
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
                if (goalViewModel.archiveRestoreGoal(goal.id, true)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.goal_archived_successfully), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_archive), Toast.LENGTH_SHORT).show();
                }
            });

            // RESTORE
            optionRestore.setOnClickListener(view -> {
                dialog.dismiss();
                if (goalViewModel.archiveRestoreGoal(goal.id, false)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.goal_restored_successfully), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_archive), Toast.LENGTH_SHORT).show();
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
        tvTitle.setText(R.string.delete_goal);
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
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        tvTitle.setText(R.string.disable_auto_save);
        tvMessage.setText(R.string.disable_message);
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
}