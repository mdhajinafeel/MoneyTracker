package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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

import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.DebtLoanType;
import com.nprotech.moneytracker.db.entites.DebtLoanEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanPaymentEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanPaymentTransactionEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.models.DebtLoanPaymentWithDetails;
import com.nprotech.moneytracker.models.DebtLoanTransactionWithDetails;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.ui.common.MaxHeightRecyclerView;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.DebtLoanViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DebtLoanPaymentDetailActivity extends BaseActivity {

    private AppCompatImageView icBack, ivDebtLoanIcon;
    private AppCompatTextView tvTitle, tvDebtLoanName, lblDebtLoanTitle, tvDebtType, tvDebtLoanTitle, tvTotalAmount, tvPaidAmount, tvRemainingAmount;
    private MaterialCardView cardDebtLoanIcon;
    private ConstraintLayout emptyWrapper;
    private DebtLoanViewModel debtLoanViewModel;
    private int debtLoanId = 0;
    private MaxHeightRecyclerView rvTransactions;
    private RecyclerViewAdapter<DebtLoanTransactionWithDetails> debtLoanTransactionAdapter;
    private RecyclerViewAdapter<DebtLoanPaymentWithDetails> debtLoanPaymentAdapter;
    private DebtLoanEntity debtLoan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt_loan_payment_detail);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            View rootView = findViewById(R.id.rootView);

            tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);

            cardDebtLoanIcon = findViewById(R.id.cardDebtLoanIcon);
            ivDebtLoanIcon = findViewById(R.id.ivDebtLoanIcon);
            tvDebtLoanName = findViewById(R.id.tvDebtLoanName);
            tvDebtType = findViewById(R.id.tvDebtType);
            tvDebtLoanTitle = findViewById(R.id.tvDebtLoanTitle);
            tvTotalAmount = findViewById(R.id.tvTotalAmount);
            tvPaidAmount = findViewById(R.id.tvPaidAmount);
            tvRemainingAmount = findViewById(R.id.tvRemainingAmount);
            emptyWrapper = findViewById(R.id.emptyWrapper);
            rvTransactions = findViewById(R.id.rvTransactions);
            lblDebtLoanTitle = findViewById(R.id.lblDebtLoanTitle);

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

                debtLoanId = bundle.getInt("debtLoanId", 0);

                debtLoanViewModel = new ViewModelProvider(this).get(DebtLoanViewModel.class);

                bindData();
                setupListeners();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finishWithTransitions();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {
            if (debtLoanId > 0) {
                debtLoanViewModel.getDebtLoanById(debtLoanId).observe(this, debtLoanWithDetail -> {
                    if (debtLoanWithDetail != null) {

                        debtLoan = debtLoanWithDetail.debtLoan;

                        initializeAdapters();

                        int debtLoanColor = Color.parseColor(DataHelper.getDebtLoanColorList().get(debtLoan.debtLoanColor));

                        cardDebtLoanIcon.setCardBackgroundColor(debtLoanColor);
                        ivDebtLoanIcon.setImageDrawable(ContextCompat.getDrawable(this, DataHelper.getCategoryIcons().get(debtLoan.debtLoanIcon)));

                        tvDebtLoanName.setText(debtLoan.name);
                        tvDebtLoanTitle.setText(debtLoan.title);

                        if (debtLoan.type == DebtLoanType.BORROW) {
                            tvDebtType.setText(getString(R.string.borrow));
                            setDebtLoanStatusStyle(tvDebtType, R.color.expense, R.color.light_expense, R.color.expense);
                        } else if (debtLoan.type == DebtLoanType.LENT) {
                            tvDebtType.setText(getString(R.string.lent));
                            setDebtLoanStatusStyle(tvDebtType, R.color.dark_income, R.color.light_income, R.color.dark_income);
                        } else {
                            tvDebtType.setVisibility(View.GONE);
                        }

                        tvTotalAmount.setText(CommonUtils.getBeautifyAmount(debtLoan.currencySymbol, debtLoan.totalAmount));
                        tvPaidAmount.setText(CommonUtils.getBeautifyAmount(debtLoan.currencySymbol, debtLoan.paidAmount));

                        double remainingAmount = debtLoan.totalAmount - debtLoan.paidAmount;
                        tvRemainingAmount.setText(CommonUtils.getBeautifyAmount(debtLoan.currencySymbol, remainingAmount));

                        if (debtLoan.repaymentMethod == DebtLoanType.REPAYMENT_FLEXIBLE) {
                            tvTitle.setText(getString(R.string.payment_history));
                            lblDebtLoanTitle.setText(getString(R.string.payment_history));
                        } else {
                            tvTitle.setText(getString(R.string.payment_schedule));
                            lblDebtLoanTitle.setText(getString(R.string.payment_schedule));
                        }

                        loadPaymentHistory(debtLoan.id);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                        finishWithTransitions();
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

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> finishWithTransitions());

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finishWithTransitions();
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void initializeAdapters() {
        try {

            debtLoanPaymentAdapter = new RecyclerViewAdapter<>(DebtLoanPaymentDetailActivity.this, new ArrayList<>(), R.layout.item_payment_detailed_history) {
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
                        Intent intent = new Intent(DebtLoanPaymentDetailActivity.this, DebtLoanScheduleDetailActivity.class);
                        intent.putExtra("debtLoanId", debtLoanPayment.debtLoanId);
                        intent.putExtra("debtLoanPaymentId", debtLoanPayment.id);
                        startActivity(intent);
                        ActivityUtils.overrideOpenTransition(DebtLoanPaymentDetailActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                    });

                    int position = holder.getBindingAdapterPosition();
                    if (position == getItemCount() - 1) {
                        holder.getView(R.id.divider).setAlpha(0f);
                    } else {
                        holder.getView(R.id.divider).setAlpha(1f);
                    }
                }
            };

            debtLoanTransactionAdapter = new RecyclerViewAdapter<>(DebtLoanPaymentDetailActivity.this, new ArrayList<>(), R.layout.item_payment_detailed_history) {
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
                        Intent intent = new Intent(DebtLoanPaymentDetailActivity.this, DebtLoanTransactionDetailActivity.class);
                        intent.putExtra("debtLoanTransactionId", debtLoanPayment.id);
                        intent.putExtra("debtLoanId", debtLoanPayment.debtLoanId);
                        intent.putExtra("debtLoanPaymentId", debtLoanPayment.debtLoanPaymentId);
                        startActivity(intent);
                        ActivityUtils.overrideOpenTransition(DebtLoanPaymentDetailActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                    });

                    int position = holder.getBindingAdapterPosition();
                    if (position == getItemCount() - 1) {
                        holder.getView(R.id.divider).setAlpha(0f);
                    } else {
                        holder.getView(R.id.divider).setAlpha(1f);
                    }
                }
            };

            if (debtLoan.repaymentMethod == DebtLoanType.REPAYMENT_FLEXIBLE) {
                rvTransactions.setAdapter(debtLoanTransactionAdapter);
            } else {
                rvTransactions.setAdapter(debtLoanPaymentAdapter);
            }

            rvTransactions.setLayoutManager(new LinearLayoutManager(this));
            rvTransactions.setItemAnimator(null);
            rvTransactions.setHasFixedSize(true);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    private void loadPaymentHistory(int debtLoanId) {
        try {

            if (debtLoan.repaymentMethod == DebtLoanType.REPAYMENT_FLEXIBLE) {
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

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
    }
}