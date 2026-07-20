package com.nprotech.moneytracker.ui.fragments;

import android.content.Intent;
import android.graphics.Color;
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
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.CalendarHelper;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.models.CategoryExpenseModel;
import com.nprotech.moneytracker.ui.activities.TransactionOverviewActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.StatisticsViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;
import me.grantland.widget.AutofitTextView;

@AndroidEntryPoint
public class StatisticsFragment extends Fragment {

    private AppCompatTextView tvDate, tvIncome, tvExpense, tvTotal;
    private AutofitTextView tvOpeningBalance, tvEndingBalance;
    private AppCompatImageView ivPrevious, ivNext;
    private PieChart pieBreakdownChart;
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
            pieBreakdownChart = view.findViewById(R.id.pieBreakdownChart);
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

            statisticsViewModel.getCategoryExpense().observe(getViewLifecycleOwner(), this::setupPieBreakdownChart);
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
        statisticsViewModel.loadCategoryExpense(selectedAccountId, start, end);
    }

    private void setupPieBreakdownChart(List<CategoryExpenseModel> list) {
        try {
            if (list == null || list.isEmpty()) {
                pieBreakdownChart.clear();
                pieBreakdownChart.setNoDataText("No expense data");
                return;
            }

            List<PieEntry> entries = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();

            double total = 0;

            for (CategoryExpenseModel item : list) {
                total += item.amount;

                String categoryName = item.categoryName;

                if (item.defaultCategoryId > 0) {
                    categoryName = DataHelper.getDefaultCategory(requireContext(), item.defaultCategoryId);
                }

                entries.add(new PieEntry((float) item.amount, categoryName));

                try {
                    colors.add(Color.parseColor(item.color));
                } catch (IllegalArgumentException e) {
                    colors.add(Color.GRAY); // fallback color
                }
            }

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(colors);
            dataSet.setSliceSpace(3f);
            dataSet.setSelectionShift(6f);

            PieData data = new PieData(dataSet);
            data.setDrawValues(false);

            pieBreakdownChart.setData(data);
            pieBreakdownChart.setCenterText(CommonUtils.getBeautifyAmount(currencySymbol, total));

            pieBreakdownChart.setUsePercentValues(true);

            pieBreakdownChart.setDrawHoleEnabled(true);
            pieBreakdownChart.setHoleRadius(50f);

            pieBreakdownChart.setTransparentCircleRadius(45f);

            pieBreakdownChart.setEntryLabelColor(Color.WHITE);

            pieBreakdownChart.setDrawEntryLabels(false);

            pieBreakdownChart.setRotationEnabled(true);

            pieBreakdownChart.setHighlightPerTapEnabled(true);

            pieBreakdownChart.getDescription().setEnabled(false);

            pieBreakdownChart.getLegend().setEnabled(false);

            pieBreakdownChart.animateY(1000);

            pieBreakdownChart.invalidate();

        } catch (Exception e) {
            AppLogger.e(getClass(), "setupPieBreakdownChart", e);
        }
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

            overviewMoreWrapper.setOnClickListener(view -> {
                startActivity(new Intent(requireContext(), TransactionOverviewActivity.class)
                        .putExtra("accountId", selectedAccountId)
                        .putExtra("transactionDate", date.getTime()));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            chartMoreWrapper.setOnClickListener(view -> {

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