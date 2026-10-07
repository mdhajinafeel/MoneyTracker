package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.db.entites.RecurringTransactionEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.models.RecurringTransactionWithDetails;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.RecurringTransactionViewModel;
import com.nprotech.moneytracker.viewmodel.WalletViewModel;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RecurringTransactionDetailActivity extends BaseActivity {

    private MaterialCardView cardTransactionSummary, cardTransactionIcon;
    private AppCompatImageView ivTransactionIcon, ivType, ivTransferArrow;
    private View decorCircleLarge, decorCircleSmall;
    private AppCompatTextView tvTransactionStatus, tvTransactionAmount, tvFromWalletSummary, tvTransactionDescription, tvReceivedAmount, tvToWalletSummary,
            tvCategory, tvType, tvStatus, tvWallet, tvAmount, tvReceived, lblAmount, lblFee, tvFromWallet, tvToWallet, tvFee, tvDesc, tvNote, tvFrequency,
            tvRepeatRecurring, tvRepeatTime, tvNextRunDate, tvReminder, tvCreateTransaction;
    private LinearLayout layoutReceived, layoutFee, layoutWallet, layoutFromWallet, layoutToWallet, layoutDescription, layoutNotes, layoutNextRunDate;
    private AppCompatImageView icBack, ivMore;
    private String transactionId;
    private ActivityResultLauncher<Intent> transactionEditLauncher;
    private RecurringTransactionViewModel transactionViewModel;
    private WalletViewModel walletViewModel;
    private RecurringTransactionWithDetails transactionWithDetails;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_transaction_detail);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View rootView = findViewById(R.id.rootView);
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            ivMore = toolbarWrapper.findViewById(R.id.ivMore);
            cardTransactionSummary = findViewById(R.id.cardTransactionSummary);
            cardTransactionIcon = findViewById(R.id.cardTransactionIcon);
            ivTransactionIcon = findViewById(R.id.ivTransactionIcon);
            decorCircleLarge = findViewById(R.id.decorCircleLarge);
            decorCircleSmall = findViewById(R.id.decorCircleSmall);
            ivType = findViewById(R.id.ivType);
            ivTransferArrow = findViewById(R.id.ivTransferArrow);
            tvTransactionStatus = findViewById(R.id.tvTransactionStatus);
            tvTransactionAmount = findViewById(R.id.tvTransactionAmount);
            tvFromWalletSummary = findViewById(R.id.tvFromWalletSummary);
            tvTransactionDescription = findViewById(R.id.tvTransactionDescription);
            tvReceivedAmount = findViewById(R.id.tvReceivedAmount);
            tvToWalletSummary = findViewById(R.id.tvToWalletSummary);
            layoutReceived = findViewById(R.id.layoutReceived);
            layoutFee = findViewById(R.id.layoutFee);
            layoutWallet = findViewById(R.id.layoutWallet);
            layoutFromWallet = findViewById(R.id.layoutFromWallet);
            layoutToWallet = findViewById(R.id.layoutToWallet);
            layoutDescription = findViewById(R.id.layoutDescription);
            layoutNotes = findViewById(R.id.layoutNotes);
            layoutNextRunDate = findViewById(R.id.layoutNextRunDate);
            tvFrequency = findViewById(R.id.tvFrequency);
            tvCategory = findViewById(R.id.tvCategory);
            tvType = findViewById(R.id.tvType);
            tvStatus = findViewById(R.id.tvStatus);
            tvWallet = findViewById(R.id.tvWallet);
            tvAmount = findViewById(R.id.tvAmount);
            tvReceived = findViewById(R.id.tvReceived);
            lblAmount = findViewById(R.id.lblAmount);
            lblFee = findViewById(R.id.lblFee);
            tvFromWallet = findViewById(R.id.tvFromWallet);
            tvToWallet = findViewById(R.id.tvToWallet);
            tvFee = findViewById(R.id.tvFee);
            tvDesc = findViewById(R.id.tvDesc);
            tvNote = findViewById(R.id.tvNote);
            tvRepeatRecurring = findViewById(R.id.tvRepeatRecurring);
            tvRepeatTime = findViewById(R.id.tvRepeatTime);
            tvNextRunDate = findViewById(R.id.tvNextRunDate);
            tvReminder = findViewById(R.id.tvReminder);
            tvCreateTransaction = findViewById(R.id.tvCreateTransaction);

            ivMore.setVisibility(View.VISIBLE);

            tvTitle.setText(R.string.recurring_detail);

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

                transactionViewModel = new ViewModelProvider(this).get(RecurringTransactionViewModel.class);
                walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);
                transactionId = bundle.getString("transactionId", "");

                bindData(transactionId);
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

    private void bindData(String tempTransactionServerId) {
        try {

            transactionViewModel.getRecurringTransactionDetails(tempTransactionServerId).observe(this, transactionWithDetail -> {

                if (transactionWithDetail == null) {
                    finish();
                    ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
                } else {

                    transactionWithDetails = transactionWithDetail;

                    RecurringTransactionEntity transaction = transactionWithDetail.recurringTransaction;
                    if (transaction.type == TransactionEntity.TYPE_INCOME) {
                        cardTransactionSummary.setCardBackgroundColor(getColor(R.color.color_income_card));
                        cardTransactionSummary.setStrokeColor(getColor(R.color.income));
                        cardTransactionIcon.setCardBackgroundColor(Color.parseColor(transactionWithDetail.color));

                        decorCircleLarge.setBackgroundTintList(getColorStateList(R.color.color_income_circle));
                        decorCircleSmall.setBackgroundTintList(getColorStateList(R.color.color_income_circle));

                        tvTransactionStatus.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_income));
                        tvTransactionStatus.setText(getString(R.string.income));
                        tvTransactionStatus.setTextColor(getColor(R.color.income));

                        tvType.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_income));
                        tvType.setText(getString(R.string.income));
                        tvType.setTextColor(getColor(R.color.income));

                        ivType.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_income));

                        tvWallet.setText(getString(R.string.wallet_currency, transactionWithDetail.walletName, transactionWithDetail.currencySymbol));
                        tvFromWalletSummary.setText(transactionWithDetail.walletName);

                        tvAmount.setText(CommonUtils.getBeautifyAmount(transactionWithDetail.currencySymbol, transaction.amount));
                        tvTransactionAmount.setText(CommonUtils.getBeautifyAmount(transactionWithDetail.currencySymbol, transaction.amount));

                        lblAmount.setText(getString(R.string.amount));
                        lblFee.setText(getString(R.string.fee));

                        tvFromWalletSummary.setVisibility(View.VISIBLE);
                        layoutReceived.setVisibility(View.GONE);
                        layoutWallet.setVisibility(View.VISIBLE);
                        layoutFromWallet.setVisibility(View.GONE);
                        layoutToWallet.setVisibility(View.GONE);
                        layoutFee.setVisibility(View.GONE);
                    } else if (transaction.type == TransactionEntity.TYPE_EXPENSE) {
                        cardTransactionSummary.setCardBackgroundColor(getColor(R.color.color_expense_card));
                        cardTransactionSummary.setStrokeColor(getColor(R.color.expense));
                        cardTransactionIcon.setCardBackgroundColor(Color.parseColor(transactionWithDetail.color));

                        decorCircleLarge.setBackgroundTintList(getColorStateList(R.color.color_expense_circle));
                        decorCircleSmall.setBackgroundTintList(getColorStateList(R.color.color_expense_circle));

                        tvTransactionStatus.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_expense));
                        tvTransactionStatus.setText(getString(R.string.expense));
                        tvTransactionStatus.setTextColor(getColor(R.color.expense));
                        tvType.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_expense));
                        tvType.setText(getString(R.string.expense));
                        tvType.setTextColor(getColor(R.color.expense));
                        ivType.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_expense));

                        tvWallet.setText(getString(R.string.wallet_currency, transactionWithDetail.walletName, transactionWithDetail.currencySymbol));
                        tvFromWalletSummary.setText(transactionWithDetail.walletName);

                        tvAmount.setText(CommonUtils.getBeautifyAmount(transactionWithDetail.currencySymbol, transaction.amount));
                        tvTransactionAmount.setText(CommonUtils.getBeautifyAmount(transactionWithDetail.currencySymbol, transaction.amount));

                        lblAmount.setText(getString(R.string.amount));
                        lblFee.setText(getString(R.string.fee));

                        tvFromWalletSummary.setVisibility(View.VISIBLE);
                        layoutReceived.setVisibility(View.GONE);
                        layoutWallet.setVisibility(View.VISIBLE);
                        layoutFromWallet.setVisibility(View.GONE);
                        layoutToWallet.setVisibility(View.GONE);
                        layoutFee.setVisibility(View.GONE);
                    } else if (transaction.type == TransactionEntity.TYPE_TRANSFER) {
                        cardTransactionSummary.setCardBackgroundColor(getColor(R.color.color_transfer_card));
                        cardTransactionSummary.setStrokeColor(getColor(R.color.transfer));
                        cardTransactionIcon.setCardBackgroundColor(getColor(R.color.transfer));

                        decorCircleLarge.setBackgroundTintList(getColorStateList(R.color.color_transfer_circle));
                        decorCircleSmall.setBackgroundTintList(getColorStateList(R.color.color_transfer_circle));

                        tvTransactionStatus.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_transfer));
                        tvTransactionStatus.setText(getString(R.string.transfer));
                        tvTransactionStatus.setTextColor(getColor(R.color.transfer));

                        tvType.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_transfer));
                        tvType.setText(getString(R.string.transfer));
                        tvType.setTextColor(getColor(R.color.transfer));
                        ivType.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_transfer_trans));

                        // Get both wallets
                        WalletEntity fromWallet = walletViewModel.getWalletByWalletId(transaction.fromWalletId);
                        WalletEntity toWallet = walletViewModel.getWalletByWalletId(transaction.walletId);

                        String fromCurrencySymbol = "", fromCurrencyCode = "";
                        String toCurrencySymbol = "", toCurrencyCode = "";

                        if (fromWallet != null) {
                            fromCurrencySymbol = fromWallet.currencySymbol;
                            fromCurrencyCode = fromWallet.currencyCode;
                        }

                        if (toWallet != null) {
                            toCurrencySymbol = toWallet.currencySymbol;
                            toCurrencyCode = toWallet.currencyCode;
                        }

                        ivTransferArrow.setVisibility(View.VISIBLE);
                        tvReceivedAmount.setVisibility(View.VISIBLE);
                        tvFromWalletSummary.setVisibility(View.VISIBLE);
                        tvToWalletSummary.setVisibility(View.VISIBLE);

                        double convertedAmount = transaction.amount;
                        if (fromWallet != null && toWallet != null) {
                            if (fromWallet.exchangeRate > 0 && toWallet.exchangeRate > 0) {
                                double accountAmount = transaction.amount * fromWallet.exchangeRate;
                                convertedAmount = accountAmount / toWallet.exchangeRate;
                            }
                        }

                        tvReceivedAmount.setText(CommonUtils.getBeautifyAmount(toCurrencySymbol, convertedAmount));
                        tvToWalletSummary.setText(transactionWithDetail.walletName);
                        tvFromWalletSummary.setText(transactionWithDetail.fromWalletName);

                        lblAmount.setText(getString(R.string.amount_from_wallet));
                        lblFee.setText(getString(R.string.fee_from_wallet));

                        tvReceived.setText(CommonUtils.getBeautifyAmount(toCurrencySymbol, convertedAmount));
                        tvFromWallet.setText(getString(R.string.wallet_currency, transactionWithDetail.fromWalletName, fromCurrencyCode));
                        tvToWallet.setText(getString(R.string.wallet_currency, transactionWithDetail.walletName, toCurrencyCode));
                        tvAmount.setText(CommonUtils.getBeautifyAmount(fromCurrencySymbol, transaction.amount));
                        tvTransactionAmount.setText(CommonUtils.getBeautifyAmount(fromCurrencySymbol, transaction.amount));
                        tvFee.setText(CommonUtils.getBeautifyAmount(fromCurrencySymbol, transaction.fee));

                        layoutReceived.setVisibility(View.VISIBLE);
                        layoutWallet.setVisibility(View.GONE);
                        layoutFromWallet.setVisibility(View.VISIBLE);
                        layoutToWallet.setVisibility(View.VISIBLE);
                        layoutFee.setVisibility(View.VISIBLE);
                    }

                    if (transaction.type == TransactionEntity.TYPE_TRANSFER) {
                        ivTransactionIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_transfer));
                    } else {
                        if (transactionWithDetail.icon == null || transactionWithDetail.icon == 0) {
                            ivTransactionIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.category_0));
                        } else {
                            ivTransactionIcon.setImageDrawable(ContextCompat.getDrawable(this, DataHelper.getCategoryIcons().get(transactionWithDetail.icon)));
                        }
                    }

                    if (transaction.description != null && !transaction.description.isEmpty()) {
                        tvTransactionDescription.setText(transaction.description);
                        tvTransactionDescription.setVisibility(View.VISIBLE);
                    } else {
                        tvTransactionDescription.setVisibility(View.GONE);
                    }

                    if (transaction.status == RecurringTransactionEntity.STATUS_PAUSED) {
                        tvStatus.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_expense));
                        tvStatus.setText(getString(R.string.text_deactivated));
                        tvStatus.setTextColor(getColor(R.color.expense));
                    } else if (transaction.status == RecurringTransactionEntity.STATUS_IN_PROGRESS) {
                        tvStatus.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_future));
                        tvStatus.setText(getString(R.string.in_progress));
                        tvStatus.setTextColor(getColor(R.color.primary_dark));
                    } else if (transaction.status == RecurringTransactionEntity.STATUS_COMPLETED) {
                        tvStatus.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_income));
                        tvStatus.setText(getString(R.string.text_completed));
                        tvStatus.setTextColor(getColor(R.color.income));
                    }

                    // DETAIL
                    String categoryName = transaction.getCategoryName(this);
                    if (Objects.equals(categoryName, "")) {
                        categoryName = transactionWithDetail.categoryName;
                    }

                    tvCategory.setText(categoryName);

                    boolean hasDescription = transaction.description != null && !transaction.description.trim().isEmpty();
                    boolean hasNote = transaction.notes != null && !transaction.notes.trim().isEmpty();

                    if (hasDescription) {
                        tvDesc.setText(transaction.description);
                        layoutDescription.setVisibility(View.VISIBLE);
                    } else {
                        layoutDescription.setVisibility(View.GONE);
                    }

                    if (hasNote) {
                        tvNote.setText(transaction.notes);
                        layoutNotes.setVisibility(View.VISIBLE);
                    } else {
                        layoutNotes.setVisibility(View.GONE);
                    }

                    if (transaction.repeatType == Constants.REPEAT_DAILY) {
                        tvRepeatRecurring.setText(getString(R.string.calendar_daily));
                        tvFrequency.setText(getResources().getQuantityString(R.plurals.every_day_period, transaction.repeatInterval, transaction.repeatInterval));
                    } else if (transaction.repeatType == Constants.REPEAT_WEEKLY) {
                        tvRepeatRecurring.setText(getString(R.string.calendar_weekly));
                        tvFrequency.setText(getString(R.string.every_week_period, DateHelper.getWeekDaysDisplay(transaction.repeatWeekDays, this)));
                    } else if (transaction.repeatType == Constants.REPEAT_MONTHLY) {
                        tvRepeatRecurring.setText(getString(R.string.calendar_monthly));
                        tvFrequency.setText(getResources().getQuantityString(R.plurals.every_month_period, transaction.repeatInterval, transaction.repeatInterval));
                    } else if (transaction.repeatType == Constants.REPEAT_YEARLY) {
                        tvRepeatRecurring.setText(getString(R.string.calendar_yearly));
                        tvFrequency.setText(getResources().getQuantityString(R.plurals.every_year_period, transaction.repeatInterval, transaction.repeatInterval));
                    }

                    if (transaction.untilDate > 0) {
                        tvRepeatTime.setText(getString(R.string.until_date, DateHelper.getFormattedDate(transaction.untilDate)));
                    } else if (transaction.repeatTimes > 0) {
                        tvRepeatTime.setText(getResources().getQuantityString(R.plurals.for_times_period, transaction.repeatTimes, transaction.repeatTimes));
                    } else {
                        tvRepeatTime.setText(getString(R.string.forever));
                    }

                    if (transaction.nextRunDate > 0) {
                        layoutNextRunDate.setVisibility(View.VISIBLE);
                        tvNextRunDate.setText(DateHelper.getFormattedDate(transaction.nextRunDate));
                    } else {
                        layoutNextRunDate.setVisibility(View.GONE);
                        tvNextRunDate.setText("");
                    }

                    if(transaction.reminder) {
                        tvReminder.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_income));
                        tvReminder.setText(getString(R.string.enabled));
                        tvReminder.setTextColor(getColor(R.color.income));
                    } else {
                        tvReminder.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_expense));
                        tvReminder.setText(getString(R.string.disabled));
                        tvReminder.setTextColor(getColor(R.color.expense));
                    }

                    if(transaction.createTransaction) {
                        tvCreateTransaction.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_income));
                        tvCreateTransaction.setText(getString(R.string.enabled));
                        tvCreateTransaction.setTextColor(getColor(R.color.income));
                    } else {
                        tvCreateTransaction.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_expense));
                        tvCreateTransaction.setText(getString(R.string.disabled));
                        tvCreateTransaction.setTextColor(getColor(R.color.expense));
                    }
                }
            });
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

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finish();
                    ActivityUtils.overrideCloseTransition(RecurringTransactionDetailActivity.this, R.anim.scale_in, R.anim.right_to_left);
                }
            });

            ivMore.setOnClickListener(v -> showOptionDialog(transactionWithDetails));
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void setupLauncher() {
        transactionEditLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            if (data.getBooleanExtra("isUpdated", false)) {
                                String tempTransactionServerId = data.getStringExtra("tempTransactionServerId");
                                if (tempTransactionServerId != null) {
                                    bindData(tempTransactionServerId);
                                }
                            }
                        }
                    }
                });
    }

    private void showOptionDialog(RecurringTransactionWithDetails recurring) {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_recurring_options, findViewById(android.R.id.content), false);

            LinearLayout optionViewDetails = bottomView.findViewById(R.id.optionViewDetails);
            View viewViewDetails = bottomView.findViewById(R.id.viewViewDetails);
            LinearLayout optionEdit = bottomView.findViewById(R.id.optionEdit);
            LinearLayout optionActivate = bottomView.findViewById(R.id.optionActivate);
            View viewActivate = bottomView.findViewById(R.id.viewActivate);
            LinearLayout optionDeactivate = bottomView.findViewById(R.id.optionDeactivate);
            View viewDeactivate = bottomView.findViewById(R.id.viewDeactivate);
            LinearLayout optionDelete = bottomView.findViewById(R.id.optionDelete);

            optionViewDetails.setVisibility(View.GONE);
            viewViewDetails.setVisibility(View.GONE);

            if (recurring.recurringTransaction.status == RecurringTransactionEntity.STATUS_PAUSED) {
                optionActivate.setVisibility(View.VISIBLE);
                viewActivate.setVisibility(View.VISIBLE);

                optionDeactivate.setVisibility(View.GONE);
                viewDeactivate.setVisibility(View.GONE);
            } else if (recurring.recurringTransaction.status == RecurringTransactionEntity.STATUS_IN_PROGRESS) {
                optionActivate.setVisibility(View.GONE);
                viewActivate.setVisibility(View.GONE);

                optionDeactivate.setVisibility(View.VISIBLE);
                viewDeactivate.setVisibility(View.VISIBLE);
            } else if (recurring.recurringTransaction.status == RecurringTransactionEntity.STATUS_COMPLETED) {
                optionActivate.setVisibility(View.GONE);
                viewActivate.setVisibility(View.GONE);

                optionDeactivate.setVisibility(View.GONE);
                viewDeactivate.setVisibility(View.GONE);
            }

            // EDIT DETAILS
            optionEdit.setOnClickListener(view -> {
                dialog.dismiss();
                Intent intent = new Intent(this, CreateRecurringActivity.class);
                intent.putExtra("transactionId", transactionId);
                intent.putExtra("type", transactionWithDetails.recurringTransaction.type);
                intent.putExtra("action", "edit");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.top_to_bottom, R.anim.scale_out);
                transactionEditLauncher.launch(intent, options);
            });

            // ACTIVATE
            optionActivate.setOnClickListener(view -> {
                dialog.dismiss();
                showDeactivateDialog(recurring.recurringTransaction, RecurringTransactionEntity.STATUS_IN_PROGRESS);
            });

            // DEACTIVATE
            optionDeactivate.setOnClickListener(view -> {
                dialog.dismiss();
                showDeactivateDialog(recurring.recurringTransaction, RecurringTransactionEntity.STATUS_PAUSED);
            });

            // DELETE
            optionDelete.setOnClickListener(view -> {
                dialog.dismiss();
                showDeleteDialog(recurring.recurringTransaction);
            });

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showOptionDialog", e);
        }
    }

    private void showDeleteDialog(RecurringTransactionEntity recurringTransaction) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        tvTitle.setText(R.string.delete_recurring);
        tvMessage.setText(R.string.delete_recurring_message);
        tvSubMessage.setVisibility(View.GONE);
        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            deleteRecurring(recurringTransaction);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteRecurring(RecurringTransactionEntity recurringTransaction) {
        try {
            if (recurringTransaction == null) {
                return;
            }

            if (transactionViewModel.deleteRecurring(recurringTransaction.id)) {
                Toast.makeText(this, getString(R.string.recurring_deleted_successfully), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.error_delete_recurring), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteGoal", e);
        }
    }

    private void showDeactivateDialog(RecurringTransactionEntity recurringTransaction, int status) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);

        MaterialCardView cardHeader = view.findViewById(R.id.cardHeader);
        AppCompatImageView headerImage = view.findViewById(R.id.headerImage);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        MaterialButton tvDelete = view.findViewById(R.id.tvDelete);
        tvSubMessage.setVisibility(View.VISIBLE);

        if (recurringTransaction.status == RecurringTransactionEntity.STATUS_PAUSED) {
            tvTitle.setText(R.string.activate_recurring);
            tvMessage.setText(R.string.delete_activate_message);
            tvSubMessage.setText(R.string.delete_activate_sub_message);
            tvDelete.setText(getString(R.string.activate));
        } else {
            tvTitle.setText(R.string.deactivate_recurring);
            tvMessage.setText(R.string.delete_deactivate_message);
            tvSubMessage.setText(R.string.delete_deactivate_sub_message);
            tvDelete.setText(getString(R.string.deactivate));
        }

        cardHeader.setCardBackgroundColor(getColor(R.color.dim_expense));

        if (status == RecurringTransactionEntity.STATUS_PAUSED) {
            headerImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_toggle_on));
        } else {
            headerImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_toggle_off));
        }

        headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.expense)));
        tvDelete.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.expense)));

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            deactivateRecurring(recurringTransaction, status);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deactivateRecurring(RecurringTransactionEntity recurringTransaction, int status) {
        try {
            if (recurringTransaction == null) {
                return;
            }

            if (transactionViewModel.activateRecurring(recurringTransaction.id, status)) {
                Toast.makeText(this, getString(R.string.deactivated_successfully), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.error_deactivate), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteGoal", e);
        }
    }
}