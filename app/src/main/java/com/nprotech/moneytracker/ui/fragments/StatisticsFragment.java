package com.nprotech.moneytracker.ui.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.enums.CalendarFilterType;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.CalendarHelper;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.CalendarFilterModel;
import com.nprotech.moneytracker.models.CalendarRangeModel;
import com.nprotech.moneytracker.models.CategoryExpenseModel;
import com.nprotech.moneytracker.ui.activities.MainActivity;
import com.nprotech.moneytracker.ui.activities.TransactionBreakdownActivity;
import com.nprotech.moneytracker.ui.activities.TransactionOverviewActivity;
import com.nprotech.moneytracker.ui.adapters.ChartLegendAdapter;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
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
public class StatisticsFragment extends Fragment implements MainActivity.ToolbarActionListener {

    private AppCompatTextView tvDate, tvIncome, tvExpense, tvTotal, tvStartDate, tvEndDate;
    private AutofitTextView tvOpeningBalance, tvEndingBalance;
    private AppCompatImageView ivPrevious, ivNext;
    private MaterialButtonToggleGroup chartToggleGroup;
    private PieChart pieBreakdownChart;
    private RecyclerView rvChartLegend;
    private ConstraintLayout overviewMoreWrapper, chartMoreWrapper;
    private AccountViewModel accountViewModel;
    private StatisticsViewModel statisticsViewModel;
    private Date date;
    private String currencySymbol = "";
    private long loadedStart = -1, loadedEnd = -1;
    private int selectedAccountId = -1;
    private ChartLegendAdapter chartLegendAdapter;
    private List<CategoryExpenseModel> expenseList = new ArrayList<>();
    private List<CategoryExpenseModel> incomeList = new ArrayList<>();
    private CalendarFilterType selectedFilter;
    private long customStartDate = -1, customEndDate = -1;

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
            rvChartLegend = view.findViewById(R.id.rvChartLegend);
            overviewMoreWrapper = view.findViewById(R.id.overviewMoreWrapper);
            chartMoreWrapper = view.findViewById(R.id.chartMoreWrapper);
            chartToggleGroup = view.findViewById(R.id.chartToggleGroup);

            accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
            statisticsViewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);

            bindData();
            initializeAdapter();
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

                chartToggleGroup.check(R.id.btnExpense);

                pieBreakdownChart.clear();
                pieBreakdownChart.setNoDataText(getString(R.string.no_transaction));
                pieBreakdownChart.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.dark_grey));
                pieBreakdownChart.setNoDataTextTypeface(ResourcesCompat.getFont(requireContext(), R.font.exo2_semibold));
            }

            if (selectedFilter == null) {
                selectedFilter = CalendarFilterType.fromId(PreferenceManager.INSTANCE.getStatisticsFilter());
                updateNavigationButtons();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAdapter() {
        try {
            chartLegendAdapter = new ChartLegendAdapter(requireContext());

            rvChartLegend.setLayoutManager(new LinearLayoutManager(requireActivity()));
            rvChartLegend.setHasFixedSize(true);
            rvChartLegend.setItemAnimator(null);
            rvChartLegend.setAdapter(chartLegendAdapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapter", e);
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
                if (header == null) return;
                tvIncome.setText(CommonUtils.getBeautifyAmount(currencySymbol, header.income));
                tvExpense.setText(CommonUtils.getBeautifyAmount(currencySymbol, header.expense));
                tvTotal.setText(CommonUtils.getBeautifyAmount(currencySymbol, header.total));
            });

            statisticsViewModel.getCategoryExpense().observe(getViewLifecycleOwner(), list -> {
                expenseList = list == null ? new ArrayList<>() : list;

                if (chartToggleGroup.getCheckedButtonId() == R.id.btnExpense) {
                    setupPieBreakdownChart(expenseList, getString(R.string.expense));
                }
            });

            statisticsViewModel.getCategoryIncome().observe(getViewLifecycleOwner(), list -> {
                incomeList = list == null ? new ArrayList<>() : list;

                if (chartToggleGroup.getCheckedButtonId() == R.id.btnIncome) {
                    setupPieBreakdownChart(incomeList, getString(R.string.income));
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "observeData", e);
        }
    }

    private void loadCalendarData() {

        if (selectedAccountId <= 0 || date == null) return;

        CalendarRangeModel range = switch (selectedFilter) {
            case DAILY -> CalendarHelper.getDailyRange(date);
            case WEEKLY -> CalendarHelper.getWeeklyRange(date);
            case QUARTERLY -> CalendarHelper.getQuarterRange(date);
            case YEARLY -> CalendarHelper.getYearRange(date);
            case ALL -> CalendarHelper.getAllRange(requireContext());
            case CUSTOM -> CalendarHelper.getCustomRange(customStartDate, customEndDate);
            default -> CalendarHelper.getMonthlyRange(date);
        };

        tvDate.setText(range.title);

        if (loadedStart == range.startDate && loadedEnd == range.endDate) {
            return;
        }

        loadedStart = range.startDate;
        loadedEnd = range.endDate;

        statisticsViewModel.loadCalendar(selectedAccountId, range.startDate, range.endDate);
        statisticsViewModel.loadBalanceSummary(selectedAccountId, range.startDate, range.endDate);
        statisticsViewModel.loadCategoryTransaction(selectedAccountId, range.startDate, range.endDate);
    }

    private void moveDate(int offset) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        switch (selectedFilter) {

            case DAILY:
                calendar.add(Calendar.DAY_OF_MONTH, offset);
                break;

            case WEEKLY:
                calendar.add(Calendar.WEEK_OF_YEAR, offset);
                break;

            case MONTHLY:
                calendar.add(Calendar.MONTH, offset);
                break;

            case QUARTERLY:
                calendar.add(Calendar.MONTH, offset * 3);
                break;

            case YEARLY:
                calendar.add(Calendar.YEAR, offset);
                break;

            case ALL:
            case CUSTOM:
                return;
        }

        date = calendar.getTime();
        loadCalendarData();
    }

    private void setupPieBreakdownChart(List<CategoryExpenseModel> list, String title) {
        try {
            if (list == null || list.isEmpty()) {

                chartLegendAdapter.replaceItems(new ArrayList<>());
                rvChartLegend.setVisibility(View.GONE);

                pieBreakdownChart.clear();
                pieBreakdownChart.setNoDataText(getString(R.string.no_transaction));
                pieBreakdownChart.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.dark_grey));
                pieBreakdownChart.setNoDataTextTypeface(ResourcesCompat.getFont(requireContext(), R.font.exo2_semibold));
                return;
            }

            List<CategoryExpenseModel> chartData = new ArrayList<>();
            double remaining = 0;

            for (int i = 0; i < list.size(); i++) {
                if (i < 5) {
                    chartData.add(list.get(i));
                } else {
                    remaining += list.get(i).amount;
                }
            }

            if (remaining > 0) {
                chartData.add(new CategoryExpenseModel(0, 0, getString(R.string.remaining), "#BDBDBD", remaining));
            }

            List<PieEntry> entries = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();

            double total = 0;

            for (CategoryExpenseModel item : chartData) {

                total += item.amount;

                String categoryName = item.categoryName;

                if (item.defaultCategoryId > 0) {
                    categoryName = DataHelper.getDefaultCategory(requireContext(), item.defaultCategoryId);
                }

                entries.add(new PieEntry((float) item.amount, categoryName));

                try {
                    colors.add(Color.parseColor(item.color));
                } catch (Exception e) {
                    colors.add(Color.GRAY);
                }
            }

            for (CategoryExpenseModel item : chartData) {
                item.percentage = total == 0 ? 0 : (item.amount * 100.0) / total;
            }

            updateCenterText(title, total, pieBreakdownChart);

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(colors);
            dataSet.setSliceSpace(3f);
            dataSet.setSelectionShift(6f);

            PieData data = new PieData(dataSet);
            data.setDrawValues(false);

            pieBreakdownChart.setData(data);
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

            chartLegendAdapter.replaceItems(chartData);
            rvChartLegend.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            AppLogger.e(getClass(), "setupPieBreakdownChart", e);
        }
    }

    private void setupListeners() {
        try {

            ivPrevious.setOnClickListener(v -> moveDate(-1));
            ivNext.setOnClickListener(v -> moveDate(1));

            overviewMoreWrapper.setOnClickListener(view -> {
                startActivity(new Intent(requireContext(), TransactionOverviewActivity.class)
                        .putExtra("accountId", selectedAccountId)
                        .putExtra("selectedFilter", selectedFilter.getId())
                        .putExtra("customStartDate", customStartDate)
                        .putExtra("customEndDate", customEndDate)
                        .putExtra("transactionDate", date.getTime()));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            chartMoreWrapper.setOnClickListener(view -> {
                startActivity(new Intent(requireContext(), TransactionBreakdownActivity.class)
                        .putExtra("accountId", selectedAccountId)
                        .putExtra("selectedFilter", selectedFilter.getId())
                        .putExtra("customStartDate", customStartDate)
                        .putExtra("customEndDate", customEndDate)
                        .putExtra("transactionDate", date.getTime()));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            pieBreakdownChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    PieEntry entry = (PieEntry) e;
                    updateCenterText(entry.getLabel(), entry.getValue(), pieBreakdownChart);
                    chartLegendAdapter.setSelectedPosition((int) h.getX());
                }

                @Override
                public void onNothingSelected() {
                    if (chartToggleGroup.getCheckedButtonId() == R.id.btnExpense) {
                        updateCenterText(getString(R.string.expense), getTotal(expenseList), pieBreakdownChart);
                    } else {
                        updateCenterText(getString(R.string.income), getTotal(incomeList), pieBreakdownChart);
                    }
                    chartLegendAdapter.setSelectedPosition(RecyclerView.NO_POSITION);
                }
            });

            chartToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (!isChecked) return;

                if (checkedId == R.id.btnExpense) {
                    setupPieBreakdownChart(expenseList, getString(R.string.expense));
                } else if (checkedId == R.id.btnIncome) {
                    setupPieBreakdownChart(incomeList, getString(R.string.income));
                }
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
            resetStatistics();
        }
    }

    private void updateCenterText(String title, double amount, PieChart pieChart) {

        String value = CommonUtils.getBeautifyAmount(currencySymbol, amount);
        String text = value + "\n" + title;

        SpannableString center = new SpannableString(text);
        int valueEnd = value.length();

        // Amount
        center.setSpan(new StyleSpan(Typeface.BOLD), 0, valueEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        center.setSpan(new RelativeSizeSpan(1.6f), 0, valueEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Title
        center.setSpan(new RelativeSizeSpan(0.80f), valueEnd + 1, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        center.setSpan(new ForegroundColorSpan(Color.GRAY), valueEnd + 1, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        pieChart.setCenterText(center);

        Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.exo2_medium);
        if (typeface != null) {
            pieChart.setCenterTextTypeface(typeface);
        }
    }

    private double getTotal(List<CategoryExpenseModel> list) {
        double total = 0;
        if (list != null) {
            for (CategoryExpenseModel item : list) {
                total += item.amount;
            }
        }
        return total;
    }

    @Override
    public void onChartClicked() {

    }

    @Override
    public void onCalendarClicked() {
        List<CalendarFilterModel> calendarFilterModelList = new ArrayList<>();
        calendarFilterModelList.add(new CalendarFilterModel(CalendarFilterType.DAILY, 1, R.drawable.ic_calendar_daily, getString(R.string.calendar_daily), false));
        calendarFilterModelList.add(new CalendarFilterModel(CalendarFilterType.WEEKLY, 2, R.drawable.ic_calendar_weekly, getString(R.string.calendar_weekly), false));
        calendarFilterModelList.add(new CalendarFilterModel(CalendarFilterType.MONTHLY, 3, R.drawable.ic_calendar_monthly, getString(R.string.calendar_monthly), false));
        calendarFilterModelList.add(new CalendarFilterModel(CalendarFilterType.QUARTERLY, 4, R.drawable.ic_quarterly, getString(R.string.calendar_quarterly), false));
        calendarFilterModelList.add(new CalendarFilterModel(CalendarFilterType.YEARLY, 5, R.drawable.ic_yearly, getString(R.string.calendar_yearly), false));
        calendarFilterModelList.add(new CalendarFilterModel(CalendarFilterType.ALL, 6, R.drawable.ic_calendar_all, getString(R.string.calendar_all), false));
        calendarFilterModelList.add(new CalendarFilterModel(CalendarFilterType.CUSTOM, 7, R.drawable.ic_calendar_custom, getString(R.string.calendar_custom), false));

        BottomSheetDialog dialog = new BottomSheetDialog(requireActivity());
        View bottomView = getLayoutInflater().inflate(R.layout.bottom_calendar_filter_layout, requireActivity().findViewById(android.R.id.content), false);
        RecyclerView rvSelectRange = bottomView.findViewById(R.id.rvSelectRange);

        RecyclerViewAdapter<CalendarFilterModel> adapter = new RecyclerViewAdapter<>(requireContext(), calendarFilterModelList, R.layout.item_calendar_filter) {
            @Override
            public void onPostBindViewHolder(ViewHolder holder, CalendarFilterModel calendarFilter) {

                Typeface medium = ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_medium);
                Typeface semiBold = ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_semibold);

                holder.setViewText(R.id.tvFilterName, calendarFilter.filterName);
                holder.setViewImageDrawable(R.id.ivIcon, ContextCompat.getDrawable(requireContext(), calendarFilter.icon));

                holder.setViewVisibility(R.id.ivSelected, selectedFilter.getId() == calendarFilter.type.getId() ? View.VISIBLE : View.GONE);
                holder.setViewTypeface(R.id.tvFilterName, selectedFilter.getId() == calendarFilter.type.getId() ? semiBold : medium);

                holder.getView(R.id.rlFilterView).setOnClickListener(v -> {

                    if (calendarFilter.type == CalendarFilterType.CUSTOM) {
                        dialog.dismiss();
                        openDateDialog(calendarFilter.type);
                    } else {
                        selectedFilter = calendarFilter.type;
                        updateNavigationButtons();
                        date = new Date();

                        loadedStart = -1;
                        loadedEnd = -1;

                        dialog.dismiss();

                        loadCalendarData();
                    }
                });
            }
        };

        rvSelectRange.setAdapter(adapter);
        rvSelectRange.setHasFixedSize(true);
        rvSelectRange.setItemAnimator(null);

        dialog.setContentView(bottomView);
        dialog.show();
    }

    public void openDateDialog(CalendarFilterType calendarFilter) {

        AlertDialog dialog = new AlertDialog.Builder(requireActivity()).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_custom_date, requireActivity().findViewById(android.R.id.content), false);

        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvEndDate = view.findViewById(R.id.tvEndDate);

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        CalendarRangeModel range = CalendarHelper.getMonthlyRange(new Date());

        customStartDate = range.startDate;
        customEndDate = range.endDate;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        tvStartDate.setText(sdf.format(new Date(range.startDate)));
        tvEndDate.setText(sdf.format(new Date(range.endDate)));

        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());

        tvStartDate.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(customStartDate > 0 ? customStartDate : System.currentTimeMillis());

            DatePickerDialog picker = new DatePickerDialog(requireContext(), R.style.CustomDateTimePickerDialog, (view1, year, month, dayOfMonth) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, dayOfMonth, 0, 0, 0);
                selected.set(Calendar.MILLISECOND, 0);
                customStartDate = selected.getTimeInMillis();

                SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvStartDate.setText(sdf1.format(selected.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            picker.show();
            applyFont(picker.getDatePicker());

            int color = ContextCompat.getColor(requireContext(), R.color.vibrant_orange);
            picker.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(color);
            picker.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(color);
        });

        tvEndDate.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(customEndDate > 0 ? customEndDate : (customStartDate > 0 ? customStartDate : System.currentTimeMillis()));

            DatePickerDialog picker = new DatePickerDialog(requireContext(), R.style.CustomDateTimePickerDialog, (view1, year, month, dayOfMonth) -> {

                Calendar selected = Calendar.getInstance();
                selected.set(year, month, dayOfMonth, 23, 59, 59);
                selected.set(Calendar.MILLISECOND, 999);

                customEndDate = selected.getTimeInMillis();

                SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvEndDate.setText(sdf1.format(selected.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            picker.show();
            applyFont(picker.getDatePicker());

            // Don't allow selecting an end date before the start date
            if (customStartDate > 0) {
                picker.getDatePicker().setMinDate(customStartDate);
            }

            int color = ContextCompat.getColor(requireContext(), R.color.vibrant_orange);
            picker.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(color);
            picker.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(color);
        });

        view.findViewById(R.id.tvOk).setOnClickListener(v -> {
            selectedFilter = calendarFilter;

            updateNavigationButtons();

            // Current date (today), not the first day of the month
            date = new Date();

            loadedStart = -1;
            loadedEnd = -1;

            dialog.dismiss();

            loadCalendarData();
        });

        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void updateNavigationButtons() {

        boolean enabled = selectedFilter != CalendarFilterType.ALL && selectedFilter != CalendarFilterType.CUSTOM;

        ivPrevious.setEnabled(enabled);
        ivNext.setEnabled(enabled);

        float alpha = enabled ? 1f : 0.3f;
        ivPrevious.setAlpha(alpha);
        ivNext.setAlpha(alpha);
    }

    private void resetStatistics() {

        date = new Date();

        selectedFilter = CalendarFilterType.MONTHLY;
        PreferenceManager.INSTANCE.setStatisticsFilter(selectedFilter.getId());

        customStartDate = -1;
        customEndDate = -1;

        loadedStart = -1;
        loadedEnd = -1;

        updateNavigationButtons();

        chartToggleGroup.check(R.id.btnExpense);

        loadCalendarData();
    }

    private void applyFont(View view) {
        Typeface tf = ResourcesCompat.getFont(requireContext(), R.font.exo2_medium);

        if (view instanceof TextView) {
            ((TextView) view).setTypeface(tf);
        } else if (view instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                applyFont(group.getChildAt(i));
            }
        }
    }
}