package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.DebtLoanType;
import com.nprotech.moneytracker.db.entites.DebtLoanPaymentEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanPaymentTransactionEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.models.DebtLoanTransactionWithDetails;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.DebtLoanViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DebtLoanScheduleDetailActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView tvInstallmentNo, tvDueDate, tvPaymentAmount, tvPaidAmount, tvBalanceAmount, tvStatus;
    private RecyclerViewAdapter<DebtLoanTransactionWithDetails> debtLoanTransactionAdapter;
    private RecyclerView rvTransactions;
    private ConstraintLayout emptyWrapper;
    private DebtLoanViewModel debtLoanViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt_loan_schedule_detail);
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

            tvTitle.setText(R.string.installment_details);

            tvInstallmentNo = findViewById(R.id.tvInstallmentNo);
            tvDueDate = findViewById(R.id.tvDueDate);
            tvPaymentAmount = findViewById(R.id.tvPaymentAmount);
            tvPaidAmount = findViewById(R.id.tvPaidAmount);
            tvBalanceAmount = findViewById(R.id.tvBalanceAmount);
            rvTransactions = findViewById(R.id.rvTransactions);
            tvStatus = findViewById(R.id.tvStatus);
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

                debtLoanViewModel = new ViewModelProvider(this).get(DebtLoanViewModel.class);

                initializeAdapter();
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

            int debtLoanId = bundle.getInt("debtLoanId", 0);
            int debtLoanPaymentId = bundle.getInt("debtLoanPaymentId", 0);

            debtLoanViewModel.getPaymentDetailByDebtLoanId(debtLoanId, debtLoanPaymentId).observe(this, debtLoanPaymentWithDetails -> {

                DebtLoanPaymentEntity debtLoanPayment = debtLoanPaymentWithDetails.debtLoanPayment;
                int totalInstallments = debtLoanViewModel.getTotalInstallments(debtLoanId);

                tvInstallmentNo.setText(getString(R.string.installment_due_format1, debtLoanPayment.paymentNumber, totalInstallments));
                tvDueDate.setText(DateHelper.getFormattedDate(debtLoanPayment.paymentDate));
                tvPaymentAmount.setText(CommonUtils.getBeautifyAmount(debtLoanPaymentWithDetails.currencySymbol, debtLoanPayment.paymentAmount));
                tvPaidAmount.setText(CommonUtils.getBeautifyAmount(debtLoanPaymentWithDetails.currencySymbol, debtLoanPayment.paidAmount));

                double installmentRemaining = debtLoanPayment.paymentAmount - debtLoanPayment.paidAmount;
                tvBalanceAmount.setText(CommonUtils.getBeautifyAmount(debtLoanPaymentWithDetails.currencySymbol, installmentRemaining));

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

                debtLoanViewModel.getPaymentTransactionByDebtLoanId(debtLoanId, debtLoanPaymentId).observe(
                        this, debtLoanTransactionWithDetails -> {
                            if (debtLoanTransactionWithDetails != null && !debtLoanTransactionWithDetails.isEmpty()) {
                                debtLoanTransactionAdapter.setItems(debtLoanTransactionWithDetails);
                                emptyWrapper.setVisibility(View.GONE);
                                rvTransactions.setVisibility(View.VISIBLE);
                            } else {
                                emptyWrapper.setVisibility(View.VISIBLE);
                                rvTransactions.setVisibility(View.GONE);
                            }
                        });
            });

        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAdapter() {
        try {

            debtLoanTransactionAdapter = new RecyclerViewAdapter<>(DebtLoanScheduleDetailActivity.this, new ArrayList<>(), R.layout.item_payment_detailed_history) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, DebtLoanTransactionWithDetails paymentWithDetails) {

                    DebtLoanPaymentTransactionEntity debtLoanPayment = paymentWithDetails.debtLoanPayment;

                    AppCompatTextView tvStatus = holder.getView(R.id.tvStatus);
                    AppCompatTextView tvPaymentMethod = holder.getView(R.id.tvPaymentMethod);
                    tvPaymentMethod.setVisibility(View.VISIBLE);
                    tvStatus.setVisibility(View.VISIBLE);

                    holder.setViewText(R.id.tvPaidDate, DateHelper.getFormattedDate(debtLoanPayment.paymentDate, "MMM dd, yyyy"));
                    holder.setViewText(R.id.tvTotalAmount, CommonUtils.getBeautifyAmount(debtLoanPayment.currencySymbol, debtLoanPayment.amount));

                    String text = "";
                    if (debtLoanPayment.paymentMethod == DebtLoanType.PAYMENT_METHOD_CASH) {
                        text = getString(R.string.cash);
                    } else if (debtLoanPayment.paymentMethod == DebtLoanType.PAYMENT_METHOD_UPI) {
                        text = getString(R.string.upi);
                    } else if (debtLoanPayment.paymentMethod == DebtLoanType.PAYMENT_METHOD_BANK_TRANSFER) {
                        text = getString(R.string.bank_transfer);
                    } else if (debtLoanPayment.paymentMethod == DebtLoanType.PAYMENT_METHOD_CARD) {
                        text = getString(R.string.card);
                    } else if (debtLoanPayment.paymentMethod == DebtLoanType.PAYMENT_METHOD_CHEQUE) {
                        text = getString(R.string.cheque);
                    } else if (debtLoanPayment.paymentMethod == DebtLoanType.PAYMENT_METHOD_NET_BANKING) {
                        text = getString(R.string.net_banking);
                    } else if (debtLoanPayment.paymentMethod == DebtLoanType.PAYMENT_METHOD_DEMAND_DRAFT) {
                        text = getString(R.string.demand_draft);
                    } else if (debtLoanPayment.paymentMethod == DebtLoanType.PAYMENT_METHOD_OTHER) {
                        text = getString(R.string.others);
                    }

                    tvPaymentMethod.setText(text);
                    setDebtLoanStatusStyle(tvPaymentMethod, R.color.about_dark, R.color.about_light, R.color.about_dark);

                    tvStatus.setText(R.string.paid);
                    setDebtLoanStatusStyle(tvStatus, R.color.dark_income, R.color.very_light_income, R.color.dark_income);

                    holder.getView(R.id.layoutView).setOnClickListener(v -> {
                        Intent intent = new Intent(DebtLoanScheduleDetailActivity.this, DebtLoanTransactionDetailActivity.class);
                        intent.putExtra("debtLoanTransactionId", debtLoanPayment.id);
                        intent.putExtra("debtLoanId", debtLoanPayment.debtLoanId);
                        intent.putExtra("debtLoanPaymentId", debtLoanPayment.debtLoanPaymentId);
                        startActivity(intent);
                        ActivityUtils.overrideOpenTransition(DebtLoanScheduleDetailActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                    });

                    int position = holder.getBindingAdapterPosition();
                    if (position == getItemCount() - 1) {
                        holder.getView(R.id.divider).setAlpha(0f);
                    } else {
                        holder.getView(R.id.divider).setAlpha(1f);
                    }
                }
            };

            rvTransactions.setAdapter(debtLoanTransactionAdapter);
            rvTransactions.setLayoutManager(new LinearLayoutManager(this));
            rvTransactions.setHasFixedSize(false);
            rvTransactions.setItemAnimator(null);
            rvTransactions.setNestedScrollingEnabled(false);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapter", e);
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            });

            getOnBackPressedDispatcher().addCallback(
                    this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            finish();
                            ActivityUtils.overrideCloseTransition(DebtLoanScheduleDetailActivity.this, R.anim.scale_in, R.anim.right_to_left);
                        }
                    });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
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
}