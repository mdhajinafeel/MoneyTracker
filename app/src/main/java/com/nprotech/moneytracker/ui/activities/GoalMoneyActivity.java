package com.nprotech.moneytracker.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.GoalWithDetails;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.GoalViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GoalMoneyActivity extends BaseActivity {

    private AppCompatImageView icBack, ivGoalIcon;
    private AppCompatTextView tvTitle, tvGoalName, tvGoalAmount, tvGoalProgress, tvRemainingAmount, walletLabel, tvGoalWallet, tvTargetAmount,
            tvTargetDate;
    private AppCompatEditText etMemo;
    private MaterialCardView cardGoalAmount, cardGoalWallet, cardGoalDate;
    private MaterialButton btnCancel, btnAddMoney;
    private ProgressBar progressGoal;
    private GoalViewModel goalViewModel;
    private AccountViewModel accountViewModel;
    private double goalAmount;
    private ActivityResultLauncher<Intent> calculatorLauncher;
    private WalletEntity selectedWallet;
    private List<WalletEntity> walletLists;
    private long targetDate = 0;
    private boolean isAddMoney;
    private GoalWithDetails goal;

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
            walletLabel = findViewById(R.id.walletLabel);
            tvGoalWallet = findViewById(R.id.tvGoalWallet);
            tvTargetAmount = findViewById(R.id.tvTargetAmount);
            tvTargetDate = findViewById(R.id.tvTargetDate);
            etMemo = findViewById(R.id.etMemo);
            progressGoal = findViewById(R.id.progressGoal);
            cardGoalAmount = findViewById(R.id.cardGoalAmount);
            cardGoalWallet = findViewById(R.id.cardGoalWallet);
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
                accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

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
            isAddMoney = bundle.getBoolean("addMoney", false);

            if (goalId > 0) {

                walletLists = accountViewModel.getWalletsByAccountId((int) PreferenceManager.INSTANCE.getAccountId());

                if (!walletLists.isEmpty()) {
                    selectedWallet = walletLists.get(0);
                    tvGoalWallet.setText(getString(R.string.wallet_info, selectedWallet.name,
                            CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol, selectedWallet.amount)));
                }

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

                        String savedAmount = CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol, goalWithDetail.savedAmount);
                        String targetAmount = CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol, goalWithDetail.targetAmount);
                        double remainingAmount = goalWithDetail.targetAmount - goalWithDetail.savedAmount;
                        tvRemainingAmount.setText(CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol, remainingAmount));
                        tvGoalAmount.setText(getString(R.string.goal_amount_progress, savedAmount, targetAmount));

                        progressGoal.setProgressDrawable(CommonUtils.createGoalProgressDrawable(this, goalColor));
                        progressGoal.setProgress(progress);

                        tvGoalProgress.setText(getResources().getString(R.string.progress_percentage, progress));
                    }
                });

                targetDate = System.currentTimeMillis();
                tvTargetDate.setText(DateHelper.getFormattedDate(DateHelper.getCurrentDateTime()));

                if (isAddMoney) {

                    btnAddMoney.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.btn_selector));

                    tvTitle.setText(getString(R.string.add_money_goal));
                    walletLabel.setText(getString(R.string.from_wallet));
                    btnAddMoney.setText(getString(R.string.add_money));
                } else {

                    btnAddMoney.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.btn_withdraw_selector));

                    tvTitle.setText(getString(R.string.withdraw_money_goal));
                    walletLabel.setText(getString(R.string.to_wallet));
                    btnAddMoney.setText(getString(R.string.withdraw_money));
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
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

            cardGoalWallet.setOnClickListener(view -> {
                hideKeyboard(this);
                selectWallets();
            });
            tvGoalWallet.setOnClickListener(view -> cardGoalWallet.performClick());

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

                if(!Objects.requireNonNull(etMemo.getText()).toString().isEmpty()) {
                    goal.notes = etMemo.getText().toString().trim();
                }

                goal.moneyDate = targetDate;

                if (goalAmount <= 0 || selectedWallet == null) {
                    return;
                }

                if (isAddMoney) {
                    goal.description = getString(R.string.goal_contribution);

                    goalViewModel.addMoneyToGoal(goal, selectedWallet, goalAmount);

                    Toast.makeText(getApplicationContext(), getString(R.string.money_added_successfully_to_the_goal), Toast.LENGTH_SHORT).show();
                } else {
                    goal.description = getString(R.string.goal_withdrawal);

                    goalViewModel.withdrawMoneyFromGoal(goal, selectedWallet, goalAmount);

                    Toast.makeText(getApplicationContext(), getString(R.string.money_withdraw_successfully_from_the_goal), Toast.LENGTH_SHORT).show();
                }
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
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
                                tvTargetAmount.setText(CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol, goalAmount));
                            }
                            updateSaveButtonState();
                        }
                    }
                });
    }

    private void updateSaveButtonState() {
        boolean enabled = goalAmount > 0;

        enabled &= selectedWallet != null;
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
                    selectedDate.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0);
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

    private void selectWallets() {
        try {

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_wallet_picker_layout, findViewById(android.R.id.content), false);
            RecyclerView rvWallets = bottomView.findViewById(R.id.rvWallets);
            View viewLine = bottomView.findViewById(R.id.viewLine);
            LinearLayout layoutAddWallet = bottomView.findViewById(R.id.layoutAddWallet);
            viewLine.setVisibility(View.GONE);
            layoutAddWallet.setVisibility(View.GONE);

            RecyclerViewAdapter<WalletEntity> adapter = new RecyclerViewAdapter<>(getApplicationContext(), walletLists, R.layout.item_switch_accounts) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, WalletEntity walletEntity) {

                    holder.setViewText(R.id.tvAccountName, walletEntity.name);

                    holder.setViewText(R.id.tvAccountBalance, getString(R.string.account_balance_format,
                            CommonUtils.getBeautifyAmount(walletEntity.currencySymbol, walletEntity.amount)));

                    if (selectedWallet != null) {
                        holder.getView(R.id.ivSelected).setVisibility(selectedWallet.id == walletEntity.id ? View.VISIBLE : View.GONE);
                    }

                    holder.getView(R.id.rlAccountView).setOnClickListener(v -> {
                        selectedWallet = walletEntity;

                        tvGoalWallet.setText(getString(R.string.wallet_info, selectedWallet.name,
                                CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol, selectedWallet.amount)));
                        tvTargetAmount.setText(CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol, goalAmount));

                        updateSaveButtonState();
                        dialog.dismiss();
                    });
                }
            };

            rvWallets.setAdapter(adapter);
            rvWallets.setHasFixedSize(true);
            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "switchAccounts", e);
        }
    }
}