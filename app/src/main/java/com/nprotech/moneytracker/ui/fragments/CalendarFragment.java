package com.nprotech.moneytracker.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.CalendarHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.ui.adapters.CalendarAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

            bindData();
            initializeAdapters();
            loadCalendar();
            setupListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void bindData() {
        try {
            date = CalendarHelper.getInitialDate();
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAdapters() {
        try {
            int weekStartOn = PreferenceManager.INSTANCE.getWeekStartOn();
            calendarAdapter = new CalendarAdapter(requireContext(), weekStartOn);
            calendarAdapter.setHeaderLayout(R.layout.item_calendar_header);

            GridLayoutManager layoutManager = new GridLayoutManager(requireActivity(), 7);

            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return calendarAdapter.getItemViewType(position) == 0 ? 7 : 1;
                }
            });
            rvCalendar.setLayoutManager(layoutManager);
            rvCalendar.setAdapter(calendarAdapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    private void loadCalendar() {
        int weekStartOn = PreferenceManager.INSTANCE.getWeekStartOn();
        tvDate.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date));
        calendarAdapter.setWeekStartOn(weekStartOn);
        calendarAdapter.setItems(CalendarHelper.getMonthCells(date, weekStartOn));
    }

    private void setupListeners() {
        try {

            ivPrevious.setOnClickListener(view -> {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.add(Calendar.MONTH, -1);
                date = cal.getTime();
                loadCalendar();
            });

            ivNext.setOnClickListener(view -> {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.add(Calendar.MONTH, 1);
                date = cal.getTime();
                loadCalendar();
            });

            tvDate.setOnClickListener(view -> {

            });

        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCalendar();
    }
}