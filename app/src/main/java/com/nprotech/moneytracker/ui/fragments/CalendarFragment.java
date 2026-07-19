package com.nprotech.moneytracker.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.CalendarHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.CalendarDayModel;
import com.nprotech.moneytracker.models.CalendarSummaryModel;
import com.nprotech.moneytracker.ui.adapters.CalendarAdapter;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.TransactionViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;
import me.grantland.widget.AutofitTextView;

@AndroidEntryPoint
public class CalendarFragment extends Fragment {

    private AutofitTextView tvIncome, tvExpense, tvTotal;
    private AppCompatTextView tvDate;
    private AppCompatImageView ivPrevious, ivNext;
    private RecyclerView rvCalendar;
    private Date date;
    private CalendarAdapter calendarAdapter;
    private TransactionViewModel transactionViewModel;
    private List<CalendarDayModel> monthCells;
    private long loadedStart = -1, loadedEnd = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        try {

            ivPrevious = view.findViewById(R.id.ivPrevious);
            ivNext = view.findViewById(R.id.ivNext);
            tvDate = view.findViewById(R.id.tvDate);
            tvIncome = view.findViewById(R.id.tvIncome);
            tvExpense = view.findViewById(R.id.tvExpense);
            tvTotal = view.findViewById(R.id.tvTotal);
            rvCalendar = view.findViewById(R.id.rvCalendar);

            transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

            bindData();
            initializeAdapters();
            loadCalendar();
            observeData();      // Only once
            loadCalendarData(); // First month's data
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
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAdapters() {
        try {

            if (calendarAdapter != null) {
                return;
            }

            int weekStartOn = PreferenceManager.INSTANCE.getWeekStartOn();

            calendarAdapter = new CalendarAdapter(requireContext(), weekStartOn);
            calendarAdapter.setHeaderLayout(R.layout.item_calendar_header);
            GridLayoutManager layoutManager = getGridLayoutManager();
            rvCalendar.setLayoutManager(layoutManager);
            rvCalendar.setAdapter(calendarAdapter);
            rvCalendar.setHasFixedSize(true);
            rvCalendar.setItemAnimator(null);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    @NonNull
    private GridLayoutManager getGridLayoutManager() {
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 7) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return calendarAdapter.getItemViewType(position) == 0 ? 7 : 1;
            }
        });
        return layoutManager;
    }

    private void loadCalendar() {

        int weekStartOn = PreferenceManager.INSTANCE.getWeekStartOn();
        tvDate.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date));
        calendarAdapter.setWeekStartOn(weekStartOn);
        monthCells = CalendarHelper.getMonthCells(date, weekStartOn);
        calendarAdapter.setItems(monthCells);
    }

    private void observeData() {

        transactionViewModel.getCalendarSummary()
                .observe(getViewLifecycleOwner(), this::updateCalendar);

        transactionViewModel.getCalendarHeader()
                .observe(getViewLifecycleOwner(), header -> {

                    if (header == null)
                        return;

                    tvIncome.setText(CommonUtils.formatCompact(header.income));
                    tvExpense.setText(CommonUtils.formatCompact(header.expense));
                    tvTotal.setText(CommonUtils.formatCompact(header.total));
                });
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

        transactionViewModel.loadCalendar(
                (int) PreferenceManager.INSTANCE.getAccountId(),
                start,
                end
        );
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateCalendar(List<CalendarSummaryModel> summaries) {

        if (summaries == null) {
            summaries = Collections.emptyList();
        }

        List<CalendarDayModel> cells = monthCells;

        Map<Long, CalendarSummaryModel> map = new HashMap<>();

        for (CalendarSummaryModel summary : summaries) {
            map.put(CalendarHelper.getStartOfDay(summary.dayTimestamp), summary);
        }

        for (CalendarDayModel cell : cells) {

            CalendarSummaryModel summary = map.get(CalendarHelper.getStartOfDay(cell.date.getTime()));

            if (summary != null) {
                cell.hasTransaction = true;
                cell.income = summary.income;
                cell.expense = summary.expense;
                cell.total = summary.total;
            } else {
                cell.hasTransaction = false;
                cell.income = 0;
                cell.expense = 0;
                cell.total = 0;
            }
        }

        calendarAdapter.setItems(monthCells);
    }

    private void setupListeners() {
        try {

            ivPrevious.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.MONTH, -1);
                date = calendar.getTime();

                loadCalendar();      // Update month UI
                loadCalendarData();  // Load transactions
            });

            ivNext.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.MONTH, 1);
                date = calendar.getTime();

                loadCalendar();
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

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden && calendarAdapter != null) {

            Date today = CalendarHelper.getInitialDate();

            if (!isSameMonth(date, today)) {
                date = today;
                loadCalendar();
                loadCalendarData();
            }
        }
    }

    private boolean isSameMonth(Date first, Date second) {

        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();

        c1.setTime(first);
        c2.setTime(second);

        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH);
    }
}