package com.nprotech.moneytracker.ui.fragments;

import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.CalendarHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.CalendarDayModel;
import com.nprotech.moneytracker.models.CalendarSummaryModel;
import com.nprotech.moneytracker.models.TransactionWithDetails;
import com.nprotech.moneytracker.ui.activities.CreateTransactionActivity;
import com.nprotech.moneytracker.ui.adapters.CalendarAdapter;
import com.nprotech.moneytracker.ui.adapters.TransactionAdapter;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.CalendarViewModel;

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
    private CalendarViewModel calendarViewModel;
    private AccountViewModel accountViewModel;
    private String currencySymbol = "";
    private List<CalendarDayModel> monthCells;
    private long loadedStart = -1, loadedEnd = -1;
    private Date selectedDate;

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

            calendarViewModel = new ViewModelProvider(requireActivity()).get(CalendarViewModel.class);
            accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);

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

            calendarAdapter.setOnDateClickListener(this::showDayTransactions);
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

        accountViewModel.getSelectedAccount().observe(getViewLifecycleOwner(), account -> {
            if (account == null) {
                return;
            }

            currencySymbol = account.currencySymbol;

            // Force reload for the newly selected account
            loadedStart = -1;
            loadedEnd = -1;

            loadCalendarData();
        });

        calendarViewModel.getCalendarSummary().observe(getViewLifecycleOwner(), this::updateCalendar);

        calendarViewModel.getCalendarHeader().observe(getViewLifecycleOwner(), header -> {
            if (header == null)
                return;
            tvIncome.setText(CommonUtils.getBeautifyAmount(currencySymbol, header.income));
            tvExpense.setText(CommonUtils.getBeautifyAmount(currencySymbol, header.expense));
            tvTotal.setText(CommonUtils.getBeautifyAmount(currencySymbol, header.total));
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

        calendarViewModel.loadCalendar((int) PreferenceManager.INSTANCE.getAccountId(), start, end);
    }

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

    private void showDayTransactions(CalendarDayModel day) {
        try {

            BottomSheetDialog dialog = new BottomSheetDialog(requireActivity());
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_calendar_transaction_layout, requireActivity().findViewById(android.R.id.content), false);
            AppCompatTextView tvTransactionDate = bottomView.findViewById(R.id.tvTransactionDate);
            AppCompatTextView tvAmount = bottomView.findViewById(R.id.tvAmount);
            RecyclerView rvTransactions = bottomView.findViewById(R.id.rvTransactions);
            AppCompatTextView tvNoTransactions = bottomView.findViewById(R.id.tvNoTransactions);
            AppCompatImageView ivPreviousDay = bottomView.findViewById(R.id.ivPreviousDay);
            AppCompatImageView ivNextDay = bottomView.findViewById(R.id.ivNextDay);
            MaterialButton btnAddTransaction = bottomView.findViewById(R.id.btnAddTransaction);

            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
            tvTransactionDate.setText(sdf.format(day.date));

            rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvTransactions.setHasFixedSize(true);
            rvTransactions.setItemAnimator(null);

            loadSelectedDay(day.date, tvTransactionDate);

            calendarViewModel.getDayTransactions().observe(getViewLifecycleOwner(), transactions -> {

                double total = 0;

                if (transactions == null || transactions.isEmpty()) {
                    rvTransactions.setVisibility(View.GONE);
                    tvNoTransactions.setVisibility(View.VISIBLE);
                    updateRecyclerViewHeight(rvTransactions, 0);
                } else {

                    for (TransactionWithDetails item : transactions) {
                        switch (item.transaction.type) {
                            case 1: // Income
                                total += item.transaction.amount;
                                break;

                            case 2: // Expense
                                total -= item.transaction.amount;
                                break;

                            case 3: // Transfer
                                // Ignore transfers in net total
                                break;
                        }
                    }
                    tvNoTransactions.setVisibility(View.GONE);
                    rvTransactions.setVisibility(View.VISIBLE);
                    rvTransactions.setAdapter(new TransactionAdapter(requireContext(), transactions, currencySymbol));
                    updateRecyclerViewHeight(rvTransactions, transactions.size());
                }

                tvAmount.setText(CommonUtils.getBeautifyAmount(currencySymbol, total));
            });

            ivPreviousDay.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(selectedDate);
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                loadSelectedDay(calendar.getTime(), tvTransactionDate);
            });

            ivNextDay.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(selectedDate);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                loadSelectedDay(calendar.getTime(), tvTransactionDate);
            });

            btnAddTransaction.setOnClickListener(view -> {
                startActivity(new Intent(requireContext(), CreateTransactionActivity.class)
                        .putExtra("action", "add")
                        .putExtra("type", TransactionEntity.TYPE_EXPENSE)
                        .putExtra("transactionDate", CalendarHelper.getStartOfDay(selectedDate.getTime())));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showDayTransactions", e);
        }
    }

    private void loadSelectedDay(Date date, AppCompatTextView tvTransactionDate) {

        selectedDate = date;
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
        tvTransactionDate.setText(sdf.format(date));

        long start = CalendarHelper.getStartOfDay(date.getTime());
        long end = CalendarHelper.getEndOfDay(date.getTime());

        calendarViewModel.loadDayTransactions((int) PreferenceManager.INSTANCE.getAccountId(), start, end);
    }

    private void updateRecyclerViewHeight(RecyclerView recyclerView, int itemCount) {

        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();

        if (itemCount >= 7) {
            // Approximate height of one transaction item (adjust if needed)
            int itemHeight = CommonUtils.dpToPx(requireContext(), 72);

            params.height = itemHeight * 7;
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        recyclerView.setLayoutParams(params);
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
            if (!CalendarHelper.isSameMonth(date, today)) {
                date = today;
                loadCalendar();
                loadCalendarData();
            }
        }
    }
}