package com.nprotech.moneytracker.ui.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.DebtLoanType;
import com.nprotech.moneytracker.db.entites.DebtLoanEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.DebtLoanViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoanFragment extends Fragment {

    private RecyclerView rvDebtLoans;
    private ConstraintLayout emptyWrapper;
    private DebtLoanViewModel debtLoanViewModel;
    private RecyclerViewAdapter<DebtLoanEntity> loanAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_debt_loan, container, false);
        try {
            View root = view.findViewById(R.id.rootView);
            rvDebtLoans = view.findViewById(R.id.rvDebtLoans);
            emptyWrapper = view.findViewById(R.id.emptyWrapper);

            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            debtLoanViewModel = new ViewModelProvider(this).get(DebtLoanViewModel.class);

            bindData();
            initializeAdapter();
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void bindData() {
        try {
            debtLoanViewModel.getAllDebtLoans(DebtLoanType.LENT).observe(getViewLifecycleOwner(), debtLoans -> {
                if(debtLoans.isEmpty()) {
                    emptyWrapper.setVisibility(View.VISIBLE);
                    rvDebtLoans.setVisibility(View.GONE);
                } else {
                    emptyWrapper.setVisibility(View.GONE);
                    rvDebtLoans.setVisibility(View.VISIBLE);
                    loanAdapter.setItems(debtLoans);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAdapter() {
        try {

            loanAdapter = new RecyclerViewAdapter<>(requireActivity(), new ArrayList<>(), R.layout.item_debt_loan_detail) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, DebtLoanEntity debtLoan) {

                    MaterialCardView cardDebtIcon = holder.getView(R.id.cardDebtIcon);
                    AppCompatImageView ivDebtIcon = holder.getView(R.id.ivDebtIcon);
                    AppCompatTextView tvDebtTitle = holder.getView(R.id.tvDebtTitle);
                    AppCompatTextView tvStartDate = holder.getView(R.id.tvStartDate);
                    View viewLine2 = holder.getView(R.id.viewLine2);
                    AppCompatTextView tvDebtLoanStatus = holder.getView(R.id.tvDebtLoanStatus);
                    AppCompatTextView tvSavedAmount = holder.getView(R.id.tvSavedAmount);
                    AppCompatTextView tvTargetAmount = holder.getView(R.id.tvTargetAmount);
                    ProgressBar progressBudget = holder.getView(R.id.progressBudget);
                    AppCompatTextView tvProgressPercentage = holder.getView(R.id.tvProgressPercentage);
                    AppCompatImageView ivMore = holder.getView(R.id.ivMore);
                    LinearLayout layoutDaysLeft = holder.getView(R.id.layoutDaysLeft);
                    AppCompatTextView tvDaysLeft = holder.getView(R.id.tvDaysLeft);
                    AppCompatImageView ivAlertIcon = holder.getView(R.id.ivAlertIcon);
                    AppCompatTextView tvAlert = holder.getView(R.id.tvAlert);

                    CommonUtils.setDrawable(requireActivity(), tvStartDate, R.drawable.ic_calendar, R.dimen.icon_12, R.color.dark_grey, Gravity.START);
                    tvDebtTitle.setText(debtLoan.title);

                    int debtLoanColor = Color.parseColor(DataHelper.getDebtLoanColorList().get(debtLoan.debtLoanColor));
                    int progress = CommonUtils.calculateProgress(debtLoan.paidAmount, debtLoan.totalAmount);

                    cardDebtIcon.setCardBackgroundColor(debtLoanColor);
                    ivDebtIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(), DataHelper.getCategoryIcons().get(debtLoan.debtLoanIcon)));

                    tvStartDate.setText(DateHelper.getFormattedDate(debtLoan.startDate));
                    tvSavedAmount.setText(CommonUtils.getBeautifyAmount(debtLoan.currencySymbol, debtLoan.paidAmount));
                    tvTargetAmount.setText(getString(R.string.target_amount_value, CommonUtils.getBeautifyAmount(debtLoan.currencySymbol, debtLoan.totalAmount)));

                    progressBudget.setProgressDrawable(CommonUtils.createGoalProgressDrawable(requireActivity(), debtLoanColor));
                    progressBudget.setProgress(progress);
                    tvProgressPercentage.setText(getString(R.string.alert_percentage_value, progress));
                    tvProgressPercentage.setTextColor(debtLoanColor);

                    ivAlertIcon.setImageTintList(ColorStateList.valueOf(debtLoanColor));

                    ivMore.setOnClickListener(v -> {

                    });
                }
            };

            rvDebtLoans.setAdapter(loanAdapter);
            rvDebtLoans.setHasFixedSize(true);
            rvDebtLoans.setItemAnimator(null);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapter", e);
        }
    }
}