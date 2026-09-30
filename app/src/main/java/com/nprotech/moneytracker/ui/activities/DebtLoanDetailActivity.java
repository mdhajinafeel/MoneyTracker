package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.DebtLoanType;
import com.nprotech.moneytracker.db.entites.DebtLoanEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanPaymentEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanPaymentTransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.models.DebtLoanPaymentWithDetails;
import com.nprotech.moneytracker.models.DebtLoanTransactionWithDetails;
import com.nprotech.moneytracker.models.DebtLoanWithDetails;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.ui.common.MaxHeightRecyclerView;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.DebtLoanViewModel;
import com.nprotech.moneytracker.viewmodel.WalletViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DebtLoanDetailActivity extends BaseActivity {

    private AppCompatImageView icBack, ivMore, ivDebtLoanIcon, ivDebtLoanDot;
    private MaterialCardView cardDebtLoanIcon;
    private AppCompatTextView tvDebtLoanName, tvDebtLoanTitle, tvDebtLoanAmount, tvDebtLoanDate, tvDebtLoanStatus, tvProgressPercentage, tvPaidAmount, tvTotalAmount, tvPrincipalAmount, tvInterestAmount, tvStartDate, tvDueDate, tvWallet, tvInterest, lblInterestPercent, tvInterestPercent, tvInterestPeriod, tvCalcMethod, tvCompoundFrequency, tvDuration, tvRepayment, tvInstallments, tvFrequency, tvInstallmentAmount, tvFirstPaymentDate, tvReminder, tvNotes, tvStatus, tvViewAll, lblDebtLoanTitle;
    private LinearLayout layoutDueDate, layoutInterest, layoutInterestPercent, layoutInterestPeriod, layoutCalcMethod, layoutCompoundFrequency, layoutDuration, layoutRepaymentMethod, layoutInstallments, layoutFrequency, layoutInstallmentAmount, layoutFirstPaymentDate;
    private ProgressBar progressDebtLoan;
    private MaxHeightRecyclerView rvTransactions;
    private ConstraintLayout emptyWrapper;
    private DebtLoanViewModel debtLoanViewModel;
    private WalletViewModel walletViewModel;
    private DebtLoanWithDetails debtLoanDetail;
    private RecyclerViewAdapter<DebtLoanTransactionWithDetails> debtLoanTransactionAdapter;
    private RecyclerViewAdapter<DebtLoanPaymentWithDetails> debtLoanPaymentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt_loan_detail);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            View rootView = findViewById(R.id.rootView);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            ivMore = toolbarWrapper.findViewById(R.id.ivMore);

            ivMore.setVisibility(View.VISIBLE);

            cardDebtLoanIcon = findViewById(R.id.cardDebtLoanIcon);
            ivDebtLoanIcon = findViewById(R.id.ivDebtLoanIcon);
            ivDebtLoanDot = findViewById(R.id.ivDebtLoanDot);
            tvDebtLoanName = findViewById(R.id.tvDebtLoanName);
            tvDebtLoanTitle = findViewById(R.id.tvDebtLoanTitle);
            lblDebtLoanTitle = findViewById(R.id.lblDebtLoanTitle);
            tvDebtLoanAmount = findViewById(R.id.tvDebtLoanAmount);
            tvDebtLoanDate = findViewById(R.id.tvDebtLoanDate);
            tvDebtLoanStatus = findViewById(R.id.tvDebtLoanStatus);
            progressDebtLoan = findViewById(R.id.progressDebtLoan);
            tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
            tvPaidAmount = findViewById(R.id.tvPaidAmount);
            tvTotalAmount = findViewById(R.id.tvTotalAmount);
            tvPrincipalAmount = findViewById(R.id.tvPrincipalAmount);
            tvInterestAmount = findViewById(R.id.tvInterestAmount);
            tvStartDate = findViewById(R.id.tvStartDate);
            tvDueDate = findViewById(R.id.tvDueDate);
            tvWallet = findViewById(R.id.tvWallet);
            tvInterest = findViewById(R.id.tvInterest);
            lblInterestPercent = findViewById(R.id.lblInterestPercent);
            tvInterestPercent = findViewById(R.id.tvInterestPercent);
            tvInterestPeriod = findViewById(R.id.tvInterestPeriod);
            tvCalcMethod = findViewById(R.id.tvCalcMethod);
            tvCompoundFrequency = findViewById(R.id.tvCompoundFrequency);
            tvDuration = findViewById(R.id.tvDuration);
            tvRepayment = findViewById(R.id.tvRepayment);
            tvInstallments = findViewById(R.id.tvInstallments);
            tvFrequency = findViewById(R.id.tvFrequency);
            tvInstallmentAmount = findViewById(R.id.tvInstallmentAmount);
            tvFirstPaymentDate = findViewById(R.id.tvFirstPaymentDate);
            tvReminder = findViewById(R.id.tvReminder);
            tvNotes = findViewById(R.id.tvNotes);
            tvStatus = findViewById(R.id.tvStatus);
            layoutDueDate = findViewById(R.id.layoutDueDate);
            layoutInterest = findViewById(R.id.layoutInterest);
            layoutInterestPercent = findViewById(R.id.layoutInterestPercent);
            layoutInterestPeriod = findViewById(R.id.layoutInterestPeriod);
            layoutCalcMethod = findViewById(R.id.layoutCalcMethod);
            layoutCompoundFrequency = findViewById(R.id.layoutCompoundFrequency);
            layoutDuration = findViewById(R.id.layoutDuration);
            layoutRepaymentMethod = findViewById(R.id.layoutRepaymentMethod);
            layoutInstallments = findViewById(R.id.layoutInstallments);
            layoutFrequency = findViewById(R.id.layoutFrequency);
            layoutInstallmentAmount = findViewById(R.id.layoutInstallmentAmount);
            layoutFirstPaymentDate = findViewById(R.id.layoutFirstPaymentDate);
            tvViewAll = findViewById(R.id.tvViewAll);
            rvTransactions = findViewById(R.id.rvTransactions);
            emptyWrapper = findViewById(R.id.emptyWrapper);

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

                int type = bundle.getInt("debtLoanType");

                if (type == DebtLoanType.BORROW) {
                    tvTitle.setText(R.string.debt_details);
                } else if (type == DebtLoanType.LENT) {
                    tvTitle.setText(R.string.loan_details);
                }

                debtLoanViewModel = new ViewModelProvider(this).get(DebtLoanViewModel.class);
                walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);

                tvViewAll.setVisibility(View.VISIBLE);

                bindData(bundle);
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
            int debtLoanId = bundle.getInt("debtLoanId", 0);

            if (debtLoanId > 0) {

                debtLoanViewModel.getDebtLoanById(debtLoanId).observe(this, debtLoanWithDetail -> {
                    if (debtLoanWithDetail != null) {

                        debtLoanDetail = debtLoanWithDetail;
                        DebtLoanEntity debtLoan = debtLoanWithDetail.debtLoan;

                        initializeAdapters();

                        int debtLoanColor = Color.parseColor(DataHelper.getDebtLoanColorList().get(debtLoan.debtLoanColor));
                        int progress = CommonUtils.calculateProgress(debtLoan.paidAmount, debtLoan.totalAmount);
                        CommonUtils.setDrawable(this, tvDebtLoanDate, R.drawable.ic_calendar, R.dimen.icon_14, R.color.dark_grey, Gravity.START);

                        cardDebtLoanIcon.setCardBackgroundColor(debtLoanColor);
                        ivDebtLoanIcon.setImageDrawable(ContextCompat.getDrawable(this, DataHelper.getCategoryIcons().get(debtLoan.debtLoanIcon)));
                        ImageViewCompat.setImageTintList(ivDebtLoanDot, ColorStateList.valueOf(debtLoanColor));

                        tvDebtLoanName.setText(debtLoan.name);
                        tvDebtLoanTitle.setText(debtLoan.title);

                        tvDebtLoanAmount.setText(CommonUtils.getBeautifyAmount(debtLoan.currencySymbol, debtLoan.totalAmount));

                        double remainingAmount = debtLoan.totalAmount - debtLoan.paidAmount;

                        tvPaidAmount.setText(CommonUtils.getBeautifyAmount(debtLoan.currencySymbol, debtLoan.paidAmount));
                        tvTotalAmount.setText(CommonUtils.getBeautifyAmount(debtLoan.currencySymbol, remainingAmount));

                        tvDebtLoanDate.setText(DateHelper.getFormattedDate(debtLoan.startDate));

                        if (debtLoanDetail.statusType == 1) {
                            tvDebtLoanStatus.setText(getString(R.string.text_ongoing));
                            tvStatus.setText(getString(R.string.text_ongoing));
                            setDebtLoanStatusStyle(tvDebtLoanStatus, R.color.orange, R.color.light_orange, R.color.orange);
                            setDebtLoanStatusStyle(tvStatus, R.color.orange, R.color.light_orange, R.color.orange);
                        } else if (debtLoanDetail.statusType == 2) {
                            tvDebtLoanStatus.setText(getString(R.string.text_overdue));
                            tvStatus.setText(getString(R.string.text_overdue));
                            setDebtLoanStatusStyle(tvDebtLoanStatus, R.color.expense, R.color.light_expense, R.color.expense);
                            setDebtLoanStatusStyle(tvStatus, R.color.expense, R.color.light_expense, R.color.expense);
                        } else if (debtLoanDetail.statusType == 3) {
                            tvDebtLoanStatus.setText(getString(R.string.text_completed));
                            tvStatus.setText(getString(R.string.text_completed));
                            setDebtLoanStatusStyle(tvDebtLoanStatus, R.color.dark_income, R.color.light_income, R.color.dark_income);
                            setDebtLoanStatusStyle(tvStatus, R.color.dark_income, R.color.light_income, R.color.dark_income);
                        }

                        progressDebtLoan.setProgressDrawable(CommonUtils.createGoalProgressDrawable(this, debtLoanColor));
                        progressDebtLoan.setProgress(progress);
                        tvProgressPercentage.setText(getString(R.string.alert_percentage_value, progress));
                        tvProgressPercentage.setTextColor(debtLoanColor);

                        tvPrincipalAmount.setText(CommonUtils.getBeautifyAmount(debtLoan.currencySymbol, debtLoan.principalAmount));
                        tvInterestAmount.setText(CommonUtils.getBeautifyAmount(debtLoan.currencySymbol, debtLoan.interestAmount));

                        // DETAILS
                        tvStartDate.setText(DateHelper.getFormattedDate(debtLoan.startDate));
                        if (debtLoan.dueDate != null && debtLoan.dueDate > 0) {
                            tvDueDate.setText(DateHelper.getFormattedDate(debtLoan.dueDate));
                            layoutDueDate.setVisibility(View.VISIBLE);
                        } else {
                            layoutDueDate.setVisibility(View.GONE);
                        }

                        WalletEntity wallet = walletViewModel.getWalletByWalletId(debtLoan.walletId);
                        String currencySymbol = "";
                        if (wallet != null) {
                            tvWallet.setText(getString(R.string.wallet_type_currency, wallet.currencyName, wallet.currencyCode));
                            currencySymbol = wallet.currencySymbol;
                        }

                        if (debtLoan.interestType == DebtLoanType.DEBT_NO_INTEREST) {
                            layoutInterest.setVisibility(View.VISIBLE);
                            layoutInterestPercent.setVisibility(View.GONE);
                            layoutInterestPeriod.setVisibility(View.GONE);
                            layoutCalcMethod.setVisibility(View.GONE);
                            layoutDuration.setVisibility(View.GONE);

                            tvInterest.setText(getString(R.string.not_set));
                        } else if (debtLoan.interestType == DebtLoanType.DEBT_PERCENTAGE) {
                            layoutInterest.setVisibility(View.VISIBLE);
                            layoutInterestPercent.setVisibility(View.VISIBLE);
                            layoutInterestPeriod.setVisibility(View.VISIBLE);
                            layoutCalcMethod.setVisibility(View.VISIBLE);
                            layoutDuration.setVisibility(View.VISIBLE);

                            tvInterest.setText(getString(R.string.interest_percentage));
                            lblInterestPercent.setText(getString(R.string.interest_rate));
                            tvInterestPercent.setText(getString(R.string.percentage_value, CommonUtils.formatInterestRate(debtLoan.interestRate)));

                            if (debtLoan.interestPeriod == DebtLoanType.INTEREST_PERIOD_DAY) {
                                tvInterestPeriod.setText(getString(R.string.interest_per_day));
                            } else if (debtLoan.interestPeriod == DebtLoanType.INTEREST_PERIOD_WEEK) {
                                tvInterestPeriod.setText(getString(R.string.interest_per_week));
                            } else if (debtLoan.interestPeriod == DebtLoanType.INTEREST_PERIOD_MONTH) {
                                tvInterestPeriod.setText(getString(R.string.interest_per_month));
                            } else if (debtLoan.interestPeriod == DebtLoanType.INTEREST_PERIOD_YEAR) {
                                tvInterestPeriod.setText(getString(R.string.interest_per_year));
                            }

                            if (debtLoan.interestCalculationMethod == DebtLoanType.INTEREST_CALC_SI) {
                                tvCalcMethod.setText(getString(R.string.interest_simple_interest));
                                layoutCompoundFrequency.setVisibility(View.GONE);
                            } else if (debtLoan.interestCalculationMethod == DebtLoanType.INTEREST_CALC_FLAT_RATE) {
                                tvCalcMethod.setText(getString(R.string.interest_flat_rate));
                                layoutCompoundFrequency.setVisibility(View.GONE);
                            } else if (debtLoan.interestCalculationMethod == DebtLoanType.INTEREST_CALC_REDUCE_BALANCE) {
                                tvCalcMethod.setText(getString(R.string.interest_reducing_balance));
                                layoutCompoundFrequency.setVisibility(View.GONE);
                            } else if (debtLoan.interestCalculationMethod == DebtLoanType.INTEREST_CALC_CI) {
                                tvCalcMethod.setText(getString(R.string.interest_compound_interest));
                                layoutCompoundFrequency.setVisibility(View.VISIBLE);

                                if (debtLoan.compoundFrequency == DebtLoanType.INTEREST_CI_COMP_FREQ_DAILY) {
                                    tvCompoundFrequency.setText(getString(R.string.calendar_daily));
                                } else if (debtLoan.compoundFrequency == DebtLoanType.INTEREST_CI_COMP_FREQ_WEEKLY) {
                                    tvCompoundFrequency.setText(getString(R.string.calendar_weekly));
                                } else if (debtLoan.compoundFrequency == DebtLoanType.INTEREST_CI_COMP_FREQ_MONTHLY) {
                                    tvCompoundFrequency.setText(getString(R.string.calendar_monthly));
                                } else if (debtLoan.compoundFrequency == DebtLoanType.INTEREST_CI_COMP_FREQ_QUARTERLY) {
                                    tvCompoundFrequency.setText(getString(R.string.calendar_quarterly));
                                } else if (debtLoan.compoundFrequency == DebtLoanType.INTEREST_CI_COMP_FREQ_HALF_YEARLY) {
                                    tvCompoundFrequency.setText(getString(R.string.calendar_half_yearly));
                                } else if (debtLoan.compoundFrequency == DebtLoanType.INTEREST_CI_COMP_FREQ_YEARLY) {
                                    tvCompoundFrequency.setText(getString(R.string.calendar_yearly));
                                }
                            }

                            if (debtLoan.interestDuration == DebtLoanType.INTEREST_CUSTOM_PERIOD) {
                                String period = getString(R.string.year_text);
                                if (debtLoan.customInterestDurationPeriod == 1) {
                                    period = getString(R.string.day);
                                } else if (debtLoan.customInterestDurationPeriod == 2) {
                                    period = getString(R.string.week_text);
                                } else if (debtLoan.customInterestDurationPeriod == 3) {
                                    period = getString(R.string.month_text);
                                }
                                tvDuration.setText(getString(R.string.interest_duration_format, debtLoan.customInterestDuration, period));
                            } else {
                                tvDuration.setText(getLoanDurationText(debtLoan.startDate, debtLoan.dueDate));
                            }
                        } else if (debtLoan.interestType == DebtLoanType.DEBT_FIXED_AMOUNT) {
                            layoutInterest.setVisibility(View.VISIBLE);
                            layoutInterestPercent.setVisibility(View.VISIBLE);
                            layoutInterestPeriod.setVisibility(View.GONE);
                            layoutCalcMethod.setVisibility(View.GONE);
                            layoutDuration.setVisibility(View.GONE);

                            tvInterest.setText(getString(R.string.fixed_amount));
                            lblInterestPercent.setText(getString(R.string.interest_amount));
                            tvInterestPercent.setText(getString(R.string.progress_percentage, debtLoan.interestRate));
                        }

                        if (debtLoan.repaymentMethod == DebtLoanType.REPAYMENT_FLEXIBLE) {
                            layoutRepaymentMethod.setVisibility(View.VISIBLE);
                            layoutInstallments.setVisibility(View.GONE);
                            layoutFrequency.setVisibility(View.GONE);
                            layoutInstallmentAmount.setVisibility(View.GONE);
                            layoutFirstPaymentDate.setVisibility(View.GONE);

                            tvRepayment.setText(getString(R.string.flexible));
                        } else if (debtLoan.repaymentMethod == DebtLoanType.REPAYMENT_INSTALLMENTS) {
                            layoutRepaymentMethod.setVisibility(View.VISIBLE);
                            layoutInstallments.setVisibility(View.VISIBLE);
                            layoutFrequency.setVisibility(View.VISIBLE);
                            layoutInstallmentAmount.setVisibility(View.VISIBLE);
                            layoutFirstPaymentDate.setVisibility(View.VISIBLE);

                            tvRepayment.setText(getString(R.string.installments));
                        } else if (debtLoan.repaymentMethod == DebtLoanType.REPAYMENT_EMI) {
                            layoutRepaymentMethod.setVisibility(View.VISIBLE);
                            layoutInstallments.setVisibility(View.VISIBLE);
                            layoutFrequency.setVisibility(View.VISIBLE);
                            layoutInstallmentAmount.setVisibility(View.VISIBLE);
                            layoutFirstPaymentDate.setVisibility(View.VISIBLE);

                            tvRepayment.setText(getString(R.string.emi));
                        }

                        if (debtLoan.noOfInstallments > 0) {
                            tvInstallments.setText(String.valueOf(debtLoan.noOfInstallments));
                        } else {
                            tvInstallments.setText("0");
                        }

                        if (debtLoan.repaymentFrequency == DebtLoanType.REPAYMENT_FREQ_DAILY) {
                            tvFrequency.setText(getString(R.string.calendar_daily));
                        } else if (debtLoan.repaymentFrequency == DebtLoanType.REPAYMENT_FREQ_WEEKLY) {
                            tvFrequency.setText(getString(R.string.calendar_weekly));
                        } else if (debtLoan.repaymentFrequency == DebtLoanType.REPAYMENT_FREQ_BIWEEKLY) {
                            tvFrequency.setText(getString(R.string.calendar_biweekly));
                        } else if (debtLoan.repaymentFrequency == DebtLoanType.REPAYMENT_FREQ_MONTHLY) {
                            tvFrequency.setText(getString(R.string.calendar_monthly));
                        } else if (debtLoan.repaymentFrequency == DebtLoanType.REPAYMENT_FREQ_QUARTERLY) {
                            tvFrequency.setText(getString(R.string.calendar_monthly));
                        } else if (debtLoan.repaymentFrequency == DebtLoanType.REPAYMENT_FREQ_YEARLY) {
                            tvFrequency.setText(getString(R.string.calendar_yearly));
                        }

                        if (debtLoan.installmentAmount > 0) {
                            tvInstallmentAmount.setText(CommonUtils.getBeautifyAmount(currencySymbol, debtLoan.installmentAmount));
                        } else {
                            tvInstallmentAmount.setText(CommonUtils.getBeautifyAmount(currencySymbol, 0));
                        }

                        if (debtLoan.firstPaymentDate != null && debtLoan.firstPaymentDate > 0) {
                            tvFirstPaymentDate.setText(DateHelper.getFormattedDate(debtLoan.firstPaymentDate));
                        }

                        if (debtLoan.reminderEnabled) {
                            String reminderText = switch (debtLoan.reminderDays) {
                                case 0 -> getString(R.string.on_payment_date);
                                case 1 -> getString(R.string.one_day_before);
                                case 3 -> getString(R.string.three_days_before);
                                case 7 -> getString(R.string.seven_days_before);
                                default -> getString(R.string.not_set);
                            };

                            tvReminder.setText(getString(R.string.reminder_with_time, reminderText, formatReminderTime(debtLoan.reminderHour, debtLoan.reminderMinute)));
                        } else {
                            tvReminder.setText(getString(R.string.not_set));
                        }

                        if (debtLoan.notes != null && !debtLoan.notes.isEmpty()) {
                            tvNotes.setText(debtLoan.notes);
                        }

                        loadPaymentHistory(debtLoan.id);
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

    private void loadPaymentHistory(int debtLoanId) {
        try {
            if (debtLoanDetail.debtLoan.repaymentMethod == DebtLoanType.REPAYMENT_FLEXIBLE) {
                debtLoanViewModel.getPaymentTransactionByDebtLoanId(debtLoanId).observe(this, debtLoanPayments -> {
                    if (!debtLoanPayments.isEmpty()) {
                        rvTransactions.setVisibility(View.VISIBLE);
                        emptyWrapper.setVisibility(View.GONE);
                        debtLoanTransactionAdapter.setItems(debtLoanPayments);
                    } else {
                        emptyWrapper.setVisibility(View.VISIBLE);
                        rvTransactions.setVisibility(View.GONE);
                    }
                });
            } else {
                debtLoanViewModel.getPaymentsByDebtLoanId(debtLoanId).observe(this, debtLoanPayments -> {
                    if (!debtLoanPayments.isEmpty()) {
                        rvTransactions.setVisibility(View.VISIBLE);
                        emptyWrapper.setVisibility(View.GONE);
                        debtLoanPaymentAdapter.setItems(debtLoanPayments);
                    } else {
                        emptyWrapper.setVisibility(View.VISIBLE);
                        rvTransactions.setVisibility(View.GONE);
                    }
                });
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "loadPaymentHistory", e);
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

            ivMore.setOnClickListener(v -> showOptionDialog(debtLoanDetail.debtLoan));

            tvViewAll.setOnClickListener(v -> {
                startActivity(new Intent(DebtLoanDetailActivity.this, DebtLoanPaymentDetailActivity.class).putExtra("debtLoanId", debtLoanDetail.debtLoan.id));
                ActivityUtils.overrideOpenTransition(DebtLoanDetailActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            debtLoanViewModel.getDataSavedStatus().observe(this, success -> {
                if (success == null) {
                    return;
                }

                DebtLoanEntity debtLoan = debtLoanDetail.debtLoan;

                if (success) {
                    if (debtLoan.type == DebtLoanType.BORROW) {
                        Toast.makeText(this, R.string.debt_deleted, Toast.LENGTH_SHORT).show();
                    } else if (debtLoan.type == DebtLoanType.LENT) {
                        Toast.makeText(this, R.string.loan_deleted, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (debtLoan.type == DebtLoanType.BORROW) {
                        Toast.makeText(this, R.string.error_debt_delete, Toast.LENGTH_SHORT).show();
                    } else if (debtLoan.type == DebtLoanType.LENT) {
                        Toast.makeText(this, R.string.error_loan_delete, Toast.LENGTH_SHORT).show();
                    }
                }

                finishWithTransitions();
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void initializeAdapters() {
        try {

            debtLoanPaymentAdapter = new RecyclerViewAdapter<>(DebtLoanDetailActivity.this, new ArrayList<>(), R.layout.item_payment_history) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, DebtLoanPaymentWithDetails paymentWithDetails) {

                    DebtLoanPaymentEntity debtLoanPayment = paymentWithDetails.debtLoanPayment;

                    AppCompatTextView tvStatus = holder.getView(R.id.tvStatus);
                    AppCompatTextView tvPaidAmount = holder.getView(R.id.tvPaidAmount);

                    tvStatus.setVisibility(View.VISIBLE);
                    holder.setViewVisibility(R.id.ivIcon, View.GONE);
                    holder.setViewVisibility(R.id.tvPaidNumber, View.VISIBLE);

                    holder.setViewText(R.id.tvPaidDate, DateHelper.getFormattedDate(debtLoanPayment.paymentDate, "MMM dd, yyyy"));

                    if (debtLoanPayment.paidAmount > 0) {
                        tvPaidAmount.setText(getString(R.string.paid_value, CommonUtils.getBeautifyAmount(paymentWithDetails.currencySymbol, debtLoanPayment.paidAmount)));
                        tvPaidAmount.setVisibility(View.VISIBLE);
                    } else {
                        tvPaidAmount.setText("");
                        tvPaidAmount.setVisibility(View.GONE);
                    }

                    holder.setViewText(R.id.tvTotalAmount, CommonUtils.getBeautifyAmount(paymentWithDetails.currencySymbol, debtLoanPayment.paymentAmount));
                    holder.setViewText(R.id.tvPaidNumber, "#" + debtLoanPayment.paymentNumber);

                    double installmentRemaining = debtLoanPayment.paymentAmount - debtLoanPayment.paidAmount;

                    if (installmentRemaining > 0 && debtLoanPayment.paidAmount > 0) {
                        tvStatus.setText(R.string.partial);
                        setDebtLoanStatusStyle(tvStatus, R.color.category_dark, R.color.category_light, R.color.category_dark);
                    } else if (debtLoanPayment.status == DebtLoanPaymentEntity.PAYMENT_PENDING) {
                        tvStatus.setText(R.string.text_pending);
                        setDebtLoanStatusStyle(tvStatus, R.color.privacy_dark, R.color.privacy_light, R.color.privacy_dark);
                    } else if (debtLoanPayment.status == DebtLoanPaymentEntity.PAYMENT_PAID) {
                        tvStatus.setText(R.string.paid);
                        setDebtLoanStatusStyle(tvStatus, R.color.dark_income, R.color.very_light_income, R.color.dark_income);
                    }

                    holder.getView(R.id.layoutView).setOnClickListener(v -> {
                        Intent intent = new Intent(DebtLoanDetailActivity.this, DebtLoanScheduleDetailActivity.class);
                        intent.putExtra("debtLoanId", debtLoanPayment.debtLoanId);
                        intent.putExtra("debtLoanPaymentId", debtLoanPayment.id);
                        startActivity(intent);
                        ActivityUtils.overrideOpenTransition(DebtLoanDetailActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                    });

                    int position = holder.getBindingAdapterPosition();
                    if (position == getItemCount() - 1) {
                        holder.getView(R.id.divider).setAlpha(0f);
                    } else {
                        holder.getView(R.id.divider).setAlpha(1f);
                    }
                }
            };

            debtLoanTransactionAdapter = new RecyclerViewAdapter<>(DebtLoanDetailActivity.this, new ArrayList<>(), R.layout.item_payment_history) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, DebtLoanTransactionWithDetails paymentWithDetails) {

                    DebtLoanPaymentTransactionEntity debtLoanPayment = paymentWithDetails.debtLoanPayment;
                    holder.setViewText(R.id.tvPaidDate, DateHelper.getFormattedDate(debtLoanPayment.paymentDate, "MMM dd, yyyy"));
                    holder.setViewText(R.id.tvTotalAmount, CommonUtils.getBeautifyAmount(debtLoanPayment.currencySymbol, debtLoanPayment.amount));

                    holder.getView(R.id.layoutView).setOnClickListener(v -> {
                        Intent intent = new Intent(DebtLoanDetailActivity.this, DebtLoanTransactionDetailActivity.class);
                        intent.putExtra("debtLoanTransactionId", debtLoanPayment.id);
                        intent.putExtra("debtLoanId", debtLoanPayment.debtLoanId);
                        intent.putExtra("debtLoanPaymentId", debtLoanPayment.debtLoanPaymentId);
                        startActivity(intent);
                        ActivityUtils.overrideOpenTransition(DebtLoanDetailActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                    });

                    int position = holder.getBindingAdapterPosition();
                    if (position == getItemCount() - 1) {
                        holder.getView(R.id.divider).setAlpha(0f);
                    } else {
                        holder.getView(R.id.divider).setAlpha(1f);
                    }
                }
            };

            if (debtLoanDetail.debtLoan.repaymentMethod == DebtLoanType.REPAYMENT_FLEXIBLE) {
                rvTransactions.setAdapter(debtLoanTransactionAdapter);
                lblDebtLoanTitle.setText(getString(R.string.payment_history));
            } else {
                rvTransactions.setAdapter(debtLoanPaymentAdapter);
                lblDebtLoanTitle.setText(getString(R.string.payment_schedule));
            }

            rvTransactions.setLayoutManager(new LinearLayoutManager(this));
            rvTransactions.setItemAnimator(null);
            rvTransactions.setHasFixedSize(true);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    private void showOptionDialog(DebtLoanEntity debtLoanDetail) {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_debt_loan_options, findViewById(android.R.id.content), false);
            MaterialCardView colorView = bottomView.findViewById(R.id.colorView);
            AppCompatImageView ivIcon = bottomView.findViewById(R.id.ivIcon);
            AppCompatTextView nameLabel = bottomView.findViewById(R.id.nameLabel);
            AppCompatTextView lblViewSchedule = bottomView.findViewById(R.id.lblViewSchedule);
            AppCompatImageView ivDot = bottomView.findViewById(R.id.ivDot);
            AppCompatTextView tvTotalAmount = bottomView.findViewById(R.id.tvTotalAmount);
            LinearLayout optionEdit = bottomView.findViewById(R.id.optionEdit);
            LinearLayout optionViewDetails = bottomView.findViewById(R.id.optionViewDetails);
            View viewViewDetails = bottomView.findViewById(R.id.viewViewDetails);
            LinearLayout optionRecord = bottomView.findViewById(R.id.optionRecord);
            LinearLayout optionSchedule = bottomView.findViewById(R.id.optionSchedule);
            LinearLayout optionDelete = bottomView.findViewById(R.id.optionDelete);

            int color = Color.parseColor(DataHelper.getDebtLoanColorList().get(debtLoanDetail.debtLoanColor));

            colorView.setCardBackgroundColor(color);
            ivIcon.setImageDrawable(ContextCompat.getDrawable(this, DataHelper.getCategoryIcons().get(debtLoanDetail.debtLoanIcon)));
            ImageViewCompat.setImageTintList(ivDot, ColorStateList.valueOf(color));
            nameLabel.setText(debtLoanDetail.name);
            tvTotalAmount.setText(CommonUtils.getBeautifyAmount(debtLoanDetail.currencySymbol, debtLoanDetail.totalAmount));

            optionViewDetails.setVisibility(View.GONE);
            viewViewDetails.setVisibility(View.GONE);

            if (debtLoanDetail.repaymentMethod == DebtLoanType.REPAYMENT_FLEXIBLE) {
                lblViewSchedule.setText(R.string.view_history);
            } else {
                lblViewSchedule.setText(R.string.view_schedule);
            }

            // EDIT
            optionEdit.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(DebtLoanDetailActivity.this, CreateDebtLoanActivity.class).putExtra("isEdit", true).putExtra("debtLoanId", debtLoanDetail.id));
                ActivityUtils.overrideOpenTransition(DebtLoanDetailActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            // RECORD
            optionRecord.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(DebtLoanDetailActivity.this, DebtLoanRecordPaymentActivity.class).putExtra("isEdit", false).putExtra("debtLoanId", debtLoanDetail.id));
                ActivityUtils.overrideOpenTransition(DebtLoanDetailActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            // SCHEDULE
            optionSchedule.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(DebtLoanDetailActivity.this, DebtLoanPaymentDetailActivity.class).putExtra("debtLoanId", debtLoanDetail.id));
                ActivityUtils.overrideOpenTransition(DebtLoanDetailActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            // DELETE
            optionDelete.setOnClickListener(v -> {
                dialog.dismiss();
                showDeleteDialog(debtLoanDetail);
            });

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showOptionDialog", e);
        }
    }

    private void showDeleteDialog(DebtLoanEntity debtLoan) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);

        if (debtLoan.type == DebtLoanType.BORROW) {
            tvTitle.setText(R.string.delete_debt);
            tvMessage.setText(R.string.delete_debt_message);
        } else if (debtLoan.type == DebtLoanType.LENT) {
            tvTitle.setText(R.string.delete_loan);
            tvMessage.setText(R.string.delete_loan_message);
        }

        tvSubMessage.setText(R.string.delete_budget_sub_message);
        tvSubMessage.setVisibility(View.VISIBLE);
        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            deleteDebtLoan(debtLoan);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteDebtLoan(DebtLoanEntity debtLoan) {
        try {
            if (debtLoan == null) {
                return;
            }

            debtLoanViewModel.deleteDebtLoan(debtLoan.id);
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteDebtLoan", e);
        }
    }

    private void setDebtLoanStatusStyle(AppCompatTextView textView, int textColor, int backgroundColor, int strokeColor) {
        textView.setTextColor(ContextCompat.getColor(this, textColor));
        Drawable background = AppCompatResources.getDrawable(this, R.drawable.bg_badge_income);
        if (background != null) {
            background = background.mutate();
            if (background instanceof GradientDrawable drawable) {
                drawable.setColor(ContextCompat.getColor(this, backgroundColor));
                drawable.setStroke(CommonUtils.dpToPx(this, 1), ContextCompat.getColor(this, strokeColor));
            }
            textView.setBackground(background);
        }
    }

    private String getLoanDurationText(long startDate, long dueDate) {

        if (startDate <= 0 || dueDate <= 0) {
            return getString(R.string.not_set);
        }

        long differenceMillis = getDifferenceMillis(startDate, dueDate);
        long days = TimeUnit.MILLISECONDS.toDays(differenceMillis);

        if (days <= 0) {
            return getResources().getQuantityString(R.plurals.days_period, 0, 0);
        }

        return getResources().getQuantityString(R.plurals.days_period, (int) days, (int) days);
    }

    private static long getDifferenceMillis(long startDate, long dueDate) {
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(startDate);

        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar due = Calendar.getInstance();
        due.setTimeInMillis(dueDate);

        due.set(Calendar.HOUR_OF_DAY, 0);
        due.set(Calendar.MINUTE, 0);
        due.set(Calendar.SECOND, 0);
        due.set(Calendar.MILLISECOND, 0);

        return due.getTimeInMillis() - start.getTimeInMillis();
    }

    private String formatReminderTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.getTime());
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
    }
}