package com.nprotech.moneytracker.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.PieChart;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.CalendarHelper;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.StatisticsViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;
import me.grantland.widget.AutofitTextView;

@AndroidEntryPoint
public class StatisticsFragment extends Fragment {

    private AppCompatTextView tvDate, tvIncome, tvExpense, tvTotal;
    private AutofitTextView tvOpeningBalance, tvEndingBalance;
    private AppCompatImageView ivPrevious, ivNext;
    private PieChart pieChart;
    private ConstraintLayout overviewMoreWrapper, chartMoreWrapper;
    private AccountViewModel accountViewModel;
    private StatisticsViewModel statisticsViewModel;
    private Date date;
    private String currencySymbol = "";
    private long loadedStart = -1, loadedEnd = -1;
    private int selectedAccountId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        try {

            ivPrevious = view.findViewById(R.id.ivPrevious);
            ivNext = view.findViewById(R.id.ivNext);
            tvOpeningBalance = view.findViewById(R.id.tvOpeningBalance);
            tvEndingBalance = view.findViewById(R.id.tvEndingBalance);
            tvDate = view.findViewById(R.id.tvDate);
            tvIncome = view.findViewById(R.id.tvIncome);
            tvExpense = view.findViewById(R.id.tvExpense);
            tvTotal = view.findViewById(R.id.tvTotal);
            pieChart = view.findViewById(R.id.pieChart);
            overviewMoreWrapper = view.findViewById(R.id.overviewMoreWrapper);
            chartMoreWrapper = view.findViewById(R.id.chartMoreWrapper);

            accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
            statisticsViewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);

            bindData();
            observeData();
            loadCalendarData();
            setupListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void bindData() {
        try {
            if (date == null) {
                date = CalendarHelper.getInitialDate();
                tvDate.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date));
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void observeData() {
        try {
            accountViewModel.getSelectedAccount().observe(getViewLifecycleOwner(), account -> {
                if (account == null) {
                    return;
                }

                selectedAccountId = account.id;
                currencySymbol = account.currencySymbol;

                // Reload statistics for the new account
                loadedStart = -1;
                loadedEnd = -1;
                loadCalendarData();
            });

            statisticsViewModel.getBalanceSummary().observe(getViewLifecycleOwner(), balance -> {
                if (balance == null) {
                    tvOpeningBalance.setText(CommonUtils.getBeautifyAmount(currencySymbol, 0));
                    tvEndingBalance.setText(CommonUtils.getBeautifyAmount(currencySymbol, 0));
                    return;
                }

                tvOpeningBalance.setText(CommonUtils.getBeautifyAmount(currencySymbol, balance.openingBalance));
                tvEndingBalance.setText(CommonUtils.getBeautifyAmount(currencySymbol, balance.closingBalance));
            });

            statisticsViewModel.getCalendarHeader().observe(getViewLifecycleOwner(), header -> {
                if (header == null)
                    return;
                tvIncome.setText(CommonUtils.getBeautifyAmount(currencySymbol, header.income));
                tvExpense.setText(CommonUtils.getBeautifyAmount(currencySymbol, header.expense));
                tvTotal.setText(CommonUtils.getBeautifyAmount(currencySymbol, header.total));
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "observeData", e);
        }
    }

    private void loadCalendarData() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // First day of the selected month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long start = calendar.getTimeInMillis();

        // Last day of the selected month
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);

        long end = calendar.getTimeInMillis();

        if (start == loadedStart && end == loadedEnd) {
            return;
        }

        loadedStart = start;
        loadedEnd = end;

        statisticsViewModel.loadCalendar(selectedAccountId, start, end);
        statisticsViewModel.loadBalanceSummary(selectedAccountId, start, end);
    }

    private void setupListeners() {
        try {

            ivPrevious.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.MONTH, -1);
                date = calendar.getTime();
                tvDate.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date));
                loadCalendarData();
            });

            ivNext.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.MONTH, 1);
                date = calendar.getTime();
                tvDate.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date));
                loadCalendarData();
            });

        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden) {
            resetToCurrentMonth();
        }
    }

    private void resetToCurrentMonth() {
        date = CalendarHelper.getInitialDate();
        tvDate.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date));
        loadedStart = -1;
        loadedEnd = -1;
        loadCalendarData();
    }
}