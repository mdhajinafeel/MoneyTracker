package com.nprotech.moneytracker.ui.fragments;

import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.models.CategoryExpenseModel;
import com.nprotech.moneytracker.ui.adapters.TransactionBreakdownAdapter;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.utils.CustomPieChartRenderer;
import com.nprotech.moneytracker.utils.SimpleDividerItemDecoration;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.StatisticsViewModel;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ExpenseBreakdownFragment extends Fragment {

    private ConstraintLayout emptyWrapper;
    private MaterialCardView breakdownCard;
    private RecyclerView rvExpenseBreakdown;
    private PieChart pieChartExpense;
    private AccountViewModel accountViewModel;
    private StatisticsViewModel statisticsViewModel;
    private String currencySymbol = "";
    private int selectedAccountId;
    private long loadedStart, loadedEnd;
    private List<CategoryExpenseModel> expenseList = new ArrayList<>();
    private TransactionBreakdownAdapter transactionBreakdownAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_breakdown, container, false);
        try {
            rvExpenseBreakdown = view.findViewById(R.id.rvExpenseBreakdown);
            emptyWrapper = view.findViewById(R.id.emptyWrapper);
            breakdownCard = view.findViewById(R.id.breakdownCard);
            pieChartExpense = view.findViewById(R.id.pieChartExpense);

            accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
            statisticsViewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);

            setupListeners();
            observeData();
            initializeAdapters();
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void observeData() {
        try {

            statisticsViewModel.getBreakdownFilter().observe(getViewLifecycleOwner(), filter -> {

                if (filter == null) return;

                selectedAccountId = filter.accountId;

                loadedStart = filter.startDate;
                loadedEnd = filter.endDate;

                statisticsViewModel.loadCategoryExpense(TransactionEntity.TYPE_EXPENSE, selectedAccountId, loadedStart, loadedEnd);

                AccountEntity account = accountViewModel.getAccountDetailById(selectedAccountId);
                if (account != null) {
                    currencySymbol = account.currencySymbol;
                    transactionBreakdownAdapter.setCurrencySymbol(currencySymbol);
                }
            });

            statisticsViewModel.getCategoryExpense().observe(getViewLifecycleOwner(), list -> {
                expenseList = list == null ? new ArrayList<>() : list;
                setupPieBreakdownChart(expenseList, getString(R.string.expense));
            });

            statisticsViewModel.getCategoryExpenseTransaction().observe(getViewLifecycleOwner(), transactionList -> {
                if (transactionList == null || transactionList.isEmpty()) {
                    breakdownCard.setVisibility(View.GONE);
                    emptyWrapper.setVisibility(View.VISIBLE);
                } else {
                    emptyWrapper.setVisibility(View.GONE);
                    breakdownCard.setVisibility(View.VISIBLE);
                    transactionBreakdownAdapter.setItems(transactionList);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "observeData", e);
        }
    }

    private void initializeAdapters() {
        try {
            rvExpenseBreakdown.setLayoutManager(new LinearLayoutManager(requireContext()));
            transactionBreakdownAdapter = new TransactionBreakdownAdapter(requireContext(), new ArrayList<>(), currencySymbol, TransactionEntity.TYPE_EXPENSE);
            rvExpenseBreakdown.setAdapter(transactionBreakdownAdapter);
            rvExpenseBreakdown.setHasFixedSize(true);
            rvExpenseBreakdown.setItemAnimator(null);
            rvExpenseBreakdown.addItemDecoration(new SimpleDividerItemDecoration(requireContext()));
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    private void setupPieBreakdownChart(List<CategoryExpenseModel> list, String title) {
        try {
            if (list == null || list.isEmpty()) {

                emptyWrapper.setVisibility(View.VISIBLE);
                pieChartExpense.setVisibility(View.GONE);
                rvExpenseBreakdown.setVisibility(View.GONE);
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
                } catch (Exception e) {
                    colors.add(Color.GRAY);
                }
            }

            for (CategoryExpenseModel item : list) {
                item.percentage = total == 0 ? 0 : (item.amount * 100.0) / total;
            }

            updateCenterText(title, total, pieChartExpense);
            pieChartExpense.setRenderer(new CustomPieChartRenderer(requireContext(), pieChartExpense, pieChartExpense.getAnimator(), pieChartExpense.getViewPortHandler()));
            pieChartExpense.setUsePercentValues(true);
            pieChartExpense.setTransparentCircleRadius(0f);
            pieChartExpense.setHoleRadius(65f);
            pieChartExpense.getDescription().setEnabled(false);
            pieChartExpense.setRotationEnabled(true);
            pieChartExpense.setDrawEntryLabels(false);
            pieChartExpense.setExtraOffsets(10f, 10f, 10f, 10f);
            pieChartExpense.setExtraBottomOffset(40f);
            pieChartExpense.setMinOffset(5f);
            pieChartExpense.getLegend().setEnabled(false);

            PieDataSet dataSet = getPieDataSet(entries, colors);
            PieData data = getPieData(dataSet, requireActivity());

            pieChartExpense.setData(data);
            pieChartExpense.setUsePercentValues(true);
            pieChartExpense.setDrawHoleEnabled(true);
            pieChartExpense.setEntryLabelColor(Color.WHITE);
            pieChartExpense.setHighlightPerTapEnabled(true);

            pieChartExpense.animateY(1000);
            pieChartExpense.invalidate();

            emptyWrapper.setVisibility(View.GONE);
            pieChartExpense.setVisibility(View.VISIBLE);
            breakdownCard.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            AppLogger.e(getClass(), "setupPieBreakdownChart", e);
        }
    }

    @NonNull
    private static PieData getPieData(PieDataSet dataSet, Context context) {
        PieData data = new PieData(dataSet);
        data.setDrawValues(true);
        data.setValueTextSize(14f);
        Typeface typeface = ResourcesCompat.getFont(context, R.font.exo2_medium);
        data.setValueTypeface(typeface);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
                numberFormat.setMaximumFractionDigits(1);
                numberFormat.setMinimumFractionDigits(1);
                return numberFormat.format(value) + "%";
            }
        });
        return data;
    }

    @NonNull
    private static PieDataSet getPieDataSet(List<PieEntry> entries, List<Integer> colors) {
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);

        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(6f);

        dataSet.setValueLinePart1OffsetPercentage(115f);
        dataSet.setValueLinePart1Length(0.40f);
        dataSet.setValueLinePart2Length(0.25f);
        dataSet.setValueLineWidth(1.5f);

        dataSet.setUsingSliceColorAsValueLineColor(true);
        dataSet.setValueTextColors(colors);
        return dataSet;
    }

    private void setupListeners() {
        try {

            pieChartExpense.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    PieEntry entry = (PieEntry) e;
                    updateCenterText(entry.getLabel(), entry.getValue(), pieChartExpense);
                }

                @Override
                public void onNothingSelected() {
                    updateCenterText(getString(R.string.expense), getTotal(expenseList), pieChartExpense);
                }
            });

        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void updateCenterText(String title, double amount, PieChart pieChart) {

        String value = CommonUtils.getBeautifyAmount(currencySymbol, amount);
        String text = value + "\n" + title;

        SpannableString center = new SpannableString(text);
        int valueEnd = value.length();

        // Amount
        center.setSpan(new StyleSpan(Typeface.BOLD),
                0, valueEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        center.setSpan(new RelativeSizeSpan(1.6f),
                0, valueEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Title
        center.setSpan(new RelativeSizeSpan(0.80f),
                valueEnd + 1, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        center.setSpan(new ForegroundColorSpan(Color.GRAY),
                valueEnd + 1, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

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
}