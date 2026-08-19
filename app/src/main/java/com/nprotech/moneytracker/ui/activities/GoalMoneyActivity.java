package com.nprotech.moneytracker.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.GoalContributionEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.models.GoalContributionWithCurrency;
import com.nprotech.moneytracker.models.GoalWithDetails;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.GoalViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GoalMoneyActivity extends BaseActivity {

    private AppCompatImageView icBack, ivGoalIcon;
    private AppCompatTextView tvTitle, tvGoalName, tvGoalAmount, tvGoalProgress, tvRemainingAmount, tvTargetAmount,
            tvTargetDate;
    private AppCompatEditText etMemo;
    private MaterialCardView cardGoalAmount, cardGoalDate;
    private MaterialButton btnCancel, btnAddMoney;
    private ProgressBar progressGoal;
    private GoalViewModel goalViewModel;
    private double goalAmount;
    private ActivityResultLauncher<Intent> calculatorLauncher;
    private long targetDate = 0;
    private boolean isAddMoney;
    private GoalWithDetails goal;
    private boolean isEdit = false;
    private GoalContributionEntity editingContribution;
    private double originalContributionAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_money);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            View root = findViewById(R.id.rootView);
            icBack = toolbarWrapper.findViewById(R.id.icBack);

            ivGoalIcon = findViewById(R.id.ivGoalIcon);
            tvGoalName = findViewById(R.id.tvGoalName);
            tvGoalAmount = findViewById(R.id.tvGoalAmount);
            tvGoalProgress = findViewById(R.id.tvGoalProgress);
            tvRemainingAmount = findViewById(R.id.tvRemainingAmount);
            tvTargetAmount = findViewById(R.id.tvTargetAmount);
            tvTargetDate = findViewById(R.id.tvTargetDate);
            etMemo = findViewById(R.id.etMemo);
            progressGoal = findViewById(R.id.progressGoal);
            cardGoalAmount = findViewById(R.id.cardGoalAmount);
            cardGoalDate = findViewById(R.id.cardGoalDate);
            btnCancel = findViewById(R.id.btnCancel);
            btnAddMoney = findViewById(R.id.btnAddMoney);

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
                setupLauncher();
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
            int contributionId = bundle.getInt("contributionId", 0);
            isAddMoney = bundle.getBoolean("addMoney", false);
            isEdit = contributionId > 0;

            if (goalId > 0) {

                goalViewModel.getGoalDetailById(goalId).observe(this, goalWithDetail -> {
                    if (goalWithDetail != null) {

                        goal = goalWithDetail;

                        int goalColor = Color.parseColor(goalWithDetail.color);
                        int progress = CommonUtils.calculateGoalProgress(goalWithDetail.savedAmount, goalWithDetail.targetAmount);

                        Drawable background = ivGoalIcon.getBackground().mutate();
                        DrawableCompat.setTint(background, Color.parseColor(goalWithDetail.color));
                        ivGoalIcon.setBackground(background);

                        ivGoalIcon.setImageResource(DataHelper.getGoalIcons().get(goalWithDetail.icon));

                        tvGoalName.setText(goalWithDetail.name);

                        String savedAmount = CommonUtils.getBeautifyAmount(goal.currencySymbol, goalWithDetail.savedAmount);
                        String targetAmount = CommonUtils.getBeautifyAmount(goal.currencySymbol, goalWithDetail.targetAmount);
                        double remainingAmount = goalWithDetail.targetAmount - goalWithDetail.savedAmount;
                        tvRemainingAmount.setText(CommonUtils.getBeautifyAmount(goal.currencySymbol, remainingAmount));
                        tvGoalAmount.setText(getString(R.string.goal_amount_progress, savedAmount, targetAmount));

                        progressGoal.setProgressDrawable(CommonUtils.createGoalProgressDrawable(this, goalColor));
                        progressGoal.setProgress(progress);

                        tvGoalProgress.setText(getResources().getString(R.string.progress_percentage, progress));
                    }
                });

                if(contributionId > 0) {

                    GoalContributionWithCurrency goalContribution = goalViewModel.getContribution(goalId, contributionId);
                    if (goalContribution != null) {

                        editingContribution = goalContribution.getContribution();

                        targetDate = editingContribution.getDate();
                        tvTargetDate.setText(DateHelper.getFormattedDate(targetDate));

                        goalAmount = editingContribution.getAmount();
                        originalContributionAmount = editingContribution.getAmount();

                        tvTargetAmount.setText(CommonUtils.getBeautifyAmount(goalContribution.getCurrencySymbol(), goalAmount));
                        etMemo.setText(editingContribution.getNote());

                        updateSaveButtonState();

                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                        finish();
                        ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
                    }
                } else {
                    targetDate = System.currentTimeMillis();
                    tvTargetDate.setText(DateHelper.getFormattedDate(DateHelper.getCurrentDateTime()));
                }

                updateActionMode();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void updateActionMode() {
        if (isAddMoney) {
            btnAddMoney.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.btn_selector));

            if(isEdit) {
                tvTitle.setText(getString(R.string.update_contribution, getString(R.string.manual)));
                btnAddMoney.setText(getString(R.string.update));
            } else {
                tvTitle.setText(getString(R.string.add_money_goal));
                btnAddMoney.setText(getString(R.string.add_money));
            }
        } else {
            btnAddMoney.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.btn_withdraw_selector));

            if(isEdit) {
                tvTitle.setText(getString(R.string.update_contribution, getString(R.string.withdrawal)));
                btnAddMoney.setText(getString(R.string.update));
            } else {
                tvTitle.setText(getString(R.string.withdraw_money_goal));
                btnAddMoney.setText(getString(R.string.withdraw_money));
            }
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            });

            getOnBackPressedDispatcher().addCallback(this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            finish();
                            ActivityUtils.overrideCloseTransition(GoalMoneyActivity.this, R.anim.scale_in, R.anim.right_to_left);
                        }
                    });

            btnCancel.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            });

            cardGoalDate.setOnClickListener(view -> openTargetDatePicker());
            tvTargetDate.setOnClickListener(view -> cardGoalDate.performClick());

            cardGoalAmount.setOnClickListener(view -> {
                hideKeyboard(this);
                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", goalAmount);
                intent.putExtra("type", "goalAmount");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                calculatorLauncher.launch(intent, options);
            });
            tvTargetAmount.setOnClickListener(view -> cardGoalAmount.performClick());

            btnAddMoney.setOnClickListener(view -> {

                if (!Objects.requireNonNull(etMemo.getText()).toString().isEmpty()) {
                    goal.notes = etMemo.getText().toString().trim();
                }

                goal.moneyDate = targetDate;

                if (goalAmount <= 0) {
                    return;
                }

                goal.goalAmount = goalAmount;

                // =========================================================
                // EDIT EXISTING CONTRIBUTION
                // =========================================================
                if (isEdit) {

                    if (editingContribution == null) {
                        Toast.makeText(getApplicationContext(), R.string.parsing_error, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double oldAmount = originalContributionAmount;

                    // -----------------------------------------------------
                    // EDIT ADD MONEY
                    // -----------------------------------------------------
                    double difference = goalAmount - oldAmount;
                    double newSavedAmount;
                    if (isAddMoney) {

                        newSavedAmount = goal.savedAmount + difference;

                        // New amount cannot exceed target
                        if (newSavedAmount > goal.targetAmount) {
                            Toast.makeText(getApplicationContext(), R.string.amount_exceeds, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // -----------------------------------------------------
                    // EDIT WITHDRAW MONEY
                    // -----------------------------------------------------
                    else {

                        newSavedAmount = goal.savedAmount - difference;

                        // Cannot withdraw more than available goal amount
                        if (newSavedAmount < 0) {
                            Toast.makeText(getApplicationContext(), R.string.no_money_withdraw, Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }
                    editingContribution.setAmount(goalAmount);
                    editingContribution.setDate(goal.moneyDate);
                    editingContribution.setNote(etMemo.getText().toString().trim());
                    goal.savedAmount = newSavedAmount;
                    goalViewModel.updateContribution(goal, editingContribution);
                    Toast.makeText(getApplicationContext(), R.string.money_updated, Toast.LENGTH_SHORT).show();

                    finish();
                    ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);

                    return;
                }

                // =========================================================
                // ADD NEW CONTRIBUTION
                // =========================================================

                double currentAmount = goalViewModel.getCurrentAmount(goal.id);

                if (isAddMoney) {

                    // Add Money
                    if (currentAmount + goalAmount > goal.targetAmount) {
                        Toast.makeText(getApplicationContext(), R.string.amount_exceeds, Toast.LENGTH_SHORT).show();
                    } else {

                        goal.savedAmount = goal.savedAmount + goalAmount;
                        goalViewModel.addMoney(goal);

                        Toast.makeText(getApplicationContext(), R.string.money_added, Toast.LENGTH_SHORT).show();
                        finish();
                        ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
                    }

                } else {

                    // Withdraw Money
                    if (currentAmount <= 0) {
                        Toast.makeText(getApplicationContext(), R.string.no_money_withdraw, Toast.LENGTH_SHORT).show();
                    } else if (goalAmount > currentAmount) {
                        Toast.makeText(getApplicationContext(), R.string.withdrawal_exceeds_amount, Toast.LENGTH_SHORT).show();
                    } else {
                        goal.savedAmount = goal.savedAmount - goalAmount;
                        goalViewModel.withdrawMoney(goal);

                        Toast.makeText(getApplicationContext(), R.string.money_withdraw, Toast.LENGTH_SHORT).show();
                        finish();
                        ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
                    }
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void setupLauncher() {
        calculatorLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            double amount = data.getDoubleExtra("amount", 0);
                            String type = data.getStringExtra("type");

                            if (type != null && type.equalsIgnoreCase("goalAmount")) {
                                goalAmount = amount;
                                tvTargetAmount.setText(CommonUtils.getBeautifyAmount(goal.currencySymbol, goalAmount));
                            }
                            updateSaveButtonState();
                        }
                    }
                });
    }

    private void updateSaveButtonState() {
        boolean enabled = goalAmount > 0;

        enabled &= !tvTargetDate.getText().toString().isEmpty();

        btnAddMoney.setEnabled(enabled);
    }

    private void openTargetDatePicker() {

        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, R.style.CustomDateTimePickerDialog,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay, selectedDate.get(Calendar.HOUR_OF_DAY), selectedDate.get(Calendar.MINUTE),
                            selectedDate.get(Calendar.SECOND));
                    selectedDate.set(Calendar.MILLISECOND, 0);
                    targetDate = selectedDate.getTimeInMillis();
                    String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedDate.getTime());
                    tvTargetDate.setText(date);
                    updateSaveButtonState();
                }, year, month, day);

        dialog.show();
        int color = ContextCompat.getColor(this, R.color.vibrant_orange);
        dialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(color);
        dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(color);
    }
}