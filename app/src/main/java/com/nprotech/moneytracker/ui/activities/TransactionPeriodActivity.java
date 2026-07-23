package com.nprotech.moneytracker.ui.activities;

import android.app.DatePickerDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.enums.CalendarFilterType;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.CalendarHelper;
import com.nprotech.moneytracker.models.BreakdownChartModel;
import com.nprotech.moneytracker.models.CalendarFilterModel;
import com.nprotech.moneytracker.models.CalendarRangeModel;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.viewmodel.StatisticsViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionPeriodActivity extends BaseActivity {

    private AppCompatImageView icBack, ivCalendar, ivPrevious, ivNext;
    private AppCompatTextView tvDate, tvStartDate, tvEndDate, amountLabel;
    private BarChart barChartSpending;
    private Date date;
    private RecyclerView rvTransactions;
    private int selectedAccountId, transactionType;
    private StatisticsViewModel statisticsViewModel;
    private CalendarFilterType selectedFilter;
    private long customStartDate = -1, customEndDate = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_period);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            ivCalendar = toolbarWrapper.findViewById(R.id.ivCalendar);
            tvDate = findViewById(R.id.tvDate);
            ivPrevious = findViewById(R.id.ivPrevious);
            ivNext = findViewById(R.id.ivNext);
            barChartSpending = findViewById(R.id.barChartSpending);
            rvTransactions = findViewById(R.id.rvTransactions);

            tvTitle.setText(getString(R.string.period_spending));
            ivCalendar.setVisibility(View.VISIBLE);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(rvTransactions, (view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {

                statisticsViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

                bindData(bundle);
                setupListeners();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData(Bundle bundle) {
        try {
            selectedAccountId = bundle.getInt("accountId", 0);
            transactionType = 2;

            int filterId = bundle.getInt("selectedFilter", CalendarFilterType.MONTHLY.getId());

            selectedFilter = CalendarFilterType.fromId(filterId);
            customStartDate = bundle.getLong("customStartDate", -1);
            customEndDate = bundle.getLong("customEndDate", -1);
            updateNavigationButtons();

            long transactionDate = bundle.getLong("transactionDate", -1);

            if (transactionDate != -1) {
                date = new Date(transactionDate);
            } else {
                date = CalendarHelper.getInitialDate();
            }

            updatePeriodSpending();

            statisticsViewModel.getBreakdownChart().observe(this, chart -> {

                if (chart == null || chart.isEmpty()) {
                    barChartSpending.clear();
                    return;
                }

                showBarChart(chart);
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void updatePeriodSpending() {

        CalendarRangeModel range = switch (selectedFilter) {
            case DAILY -> CalendarHelper.getDailyRange(date);
            case WEEKLY -> CalendarHelper.getWeeklyRange(date);
            case QUARTERLY -> CalendarHelper.getQuarterRange(date);
            case YEARLY -> CalendarHelper.getYearRange(date);
            case ALL -> CalendarHelper.getAllRange(this);
            case CUSTOM -> CalendarHelper.getCustomRange(customStartDate, customEndDate);
            default -> CalendarHelper.getMonthlyRange(date);
        };

        tvDate.setText(range.title);
        statisticsViewModel.loadBreakdown(
                selectedAccountId,
                transactionType,
                selectedFilter,
                date,
                range.startDate,
                range.endDate);
    }

    private void showBarChart(List<BreakdownChartModel> list) {

        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            entries.add(new BarEntry(i, (float) list.get(i).amount));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");

        dataSet.setColor(ContextCompat.getColor(this, R.color.vibrant_orange));
        dataSet.setDrawValues(false);
        dataSet.setHighLightAlpha(0);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);
        barChartSpending.setData(data);

        setupXAxis(list);

        barChartSpending.getDescription().setEnabled(false);
        barChartSpending.getLegend().setEnabled(false);

        barChartSpending.setDrawGridBackground(false);
        barChartSpending.setDrawBorders(false);

        barChartSpending.setFitBars(true);
        barChartSpending.setPinchZoom(false);
        barChartSpending.setScaleEnabled(false);
        barChartSpending.setDoubleTapToZoomEnabled(false);

        barChartSpending.setExtraTopOffset(10);
        barChartSpending.setExtraBottomOffset(10);

        barChartSpending.getAxisRight().setEnabled(false);

        YAxis left = barChartSpending.getAxisLeft();

        left.setDrawAxisLine(false);
        left.setDrawGridLines(true);
        left.enableGridDashedLine(10f, 10f, 0f);

        left.setAxisMinimum(0f);

        left.setTextSize(11f);

        barChartSpending.invalidate();
    }

    private void setupXAxis(List<BreakdownChartModel> list) {

        XAxis axis = barChartSpending.getXAxis();

        axis.setPosition(XAxis.XAxisPosition.BOTTOM);

        axis.setGranularity(1f);

        axis.setValueFormatter(new ValueFormatter() {

            @Override
            public String getFormattedValue(float value) {

                int index = (int) value;

                if (index < 0 || index >= list.size())
                    return "";

                BreakdownChartModel model = list.get(index);

                return switch (selectedFilter) {
                    case DAILY -> String.format(Locale.getDefault(),
                            "%02d", (int) model.period);
                    case WEEKLY, MONTHLY, CUSTOM -> CalendarHelper.formatDay(model.period);
                    case QUARTERLY, YEARLY -> CalendarHelper.formatMonth(model.period);
                    case ALL -> String.valueOf((int) model.period);
                    default -> "";
                };
            }
        });

        axis.setDrawGridLines(false);
        axis.setDrawAxisLine(true);
        axis.setGranularity(1f);
        axis.setLabelCount(7);
        axis.setTextSize(12f);
        axis.setYOffset(6f);
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            });

            ivPrevious.setOnClickListener(v -> moveDate(-1));

            ivNext.setOnClickListener(v -> moveDate(1));

            ivCalendar.setOnClickListener(view -> {
                List<CalendarFilterModel> calendarFilterModelList = new ArrayList<>();
                calendarFilterModelList.add(new CalendarFilterModel(CalendarFilterType.DAILY, 1, R.drawable.ic_calendar_daily, getString(R.string.calendar_daily), false));
                calendarFilterModelList.add(new CalendarFilterModel(CalendarFilterType.WEEKLY, 2, R.drawable.ic_calendar_weekly, getString(R.string.calendar_weekly), false));
                calendarFilterModelList.add(new CalendarFilterModel(CalendarFilterType.MONTHLY, 3, R.drawable.ic_calendar_monthly, getString(R.string.calendar_monthly), false));
                calendarFilterModelList.add(new CalendarFilterModel(CalendarFilterType.QUARTERLY, 4, R.drawable.ic_quarterly, getString(R.string.calendar_quarterly), false));
                calendarFilterModelList.add(new CalendarFilterModel(CalendarFilterType.YEARLY, 5, R.drawable.ic_yearly, getString(R.string.calendar_yearly), false));
                calendarFilterModelList.add(new CalendarFilterModel(CalendarFilterType.ALL, 6, R.drawable.ic_calendar_all, getString(R.string.calendar_all), false));
                calendarFilterModelList.add(new CalendarFilterModel(CalendarFilterType.CUSTOM, 7, R.drawable.ic_calendar_custom, getString(R.string.calendar_custom), false));

                BottomSheetDialog dialog = new BottomSheetDialog(this);
                View bottomView = getLayoutInflater().inflate(R.layout.bottom_calendar_filter_layout, findViewById(android.R.id.content), false);
                RecyclerView rvSelectRange = bottomView.findViewById(R.id.rvSelectRange);

                RecyclerViewAdapter<CalendarFilterModel> adapter = new RecyclerViewAdapter<>(this, calendarFilterModelList, R.layout.item_calendar_filter) {
                    @Override
                    public void onPostBindViewHolder(ViewHolder holder, CalendarFilterModel calendarFilter) {

                        Typeface medium = ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_medium);
                        Typeface semiBold = ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_semibold);

                        holder.setViewText(R.id.tvFilterName, calendarFilter.filterName);
                        holder.setViewImageDrawable(R.id.ivIcon, ContextCompat.getDrawable(getApplicationContext(), calendarFilter.icon));

                        holder.setViewVisibility(R.id.ivSelected, selectedFilter.getId() == calendarFilter.type.getId() ? View.VISIBLE : View.GONE);
                        holder.setViewTypeface(R.id.tvFilterName, selectedFilter.getId() == calendarFilter.type.getId() ? semiBold : medium);

                        holder.getView(R.id.rlFilterView).setOnClickListener(v -> {

                            selectedFilter = calendarFilter.type;

                            if (selectedFilter != CalendarFilterType.CUSTOM) {
                                date = new Date();
                                updateNavigationButtons();
                                updatePeriodSpending();
                            }

                            dialog.dismiss();

                            if (selectedFilter == CalendarFilterType.CUSTOM) {
                                openDateDialog(selectedFilter);
                            }
                        });
                    }
                };

                rvSelectRange.setAdapter(adapter);
                rvSelectRange.setHasFixedSize(true);
                rvSelectRange.setItemAnimator(null);

                dialog.setContentView(bottomView);
                dialog.show();
            });

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finish();
                    ActivityUtils.overrideCloseTransition(TransactionPeriodActivity.this, R.anim.scale_in, R.anim.right_to_left);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
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
        updatePeriodSpending();
    }

    private void updateNavigationButtons() {

        boolean enabled = selectedFilter != CalendarFilterType.ALL
                && selectedFilter != CalendarFilterType.CUSTOM;

        ivPrevious.setEnabled(enabled);
        ivNext.setEnabled(enabled);

        float alpha = enabled ? 1f : 0.3f;
        ivPrevious.setAlpha(alpha);
        ivNext.setAlpha(alpha);
    }

    public void openDateDialog(CalendarFilterType calendarFilter) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_custom_date, findViewById(android.R.id.content), false);

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

            DatePickerDialog picker = new DatePickerDialog(this, R.style.CustomDateTimePickerDialog, (view1, year, month, dayOfMonth) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, dayOfMonth, 0, 0, 0);
                selected.set(Calendar.MILLISECOND, 0);
                customStartDate = selected.getTimeInMillis();

                SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvStartDate.setText(sdf1.format(selected.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            picker.show();
            applyFont(picker.getDatePicker());

            int color = ContextCompat.getColor(getApplicationContext(), R.color.vibrant_orange);
            picker.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(color);
            picker.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(color);
        });

        tvEndDate.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(customEndDate > 0 ? customEndDate : (customStartDate > 0 ? customStartDate : System.currentTimeMillis()));

            DatePickerDialog picker = new DatePickerDialog(this, R.style.CustomDateTimePickerDialog, (view1, year, month, dayOfMonth) -> {

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

            int color = ContextCompat.getColor(getApplicationContext(), R.color.vibrant_orange);
            picker.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(color);
            picker.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(color);
        });

        view.findViewById(R.id.tvOk).setOnClickListener(v -> {
            selectedFilter = calendarFilter;

            updateNavigationButtons();

            Calendar calendar = Calendar.getInstance();

            calendar.setTimeInMillis(customStartDate);

            date = calendar.getTime();
            updatePeriodSpending();
            dialog.dismiss();
        });

        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void applyFont(View view) {
        Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.exo2_medium);

        if (view instanceof TextView) {
            ((TextView) view).setTypeface(tf);
        } else if (view instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                applyFont(group.getChildAt(i));
            }
        }
    }
}