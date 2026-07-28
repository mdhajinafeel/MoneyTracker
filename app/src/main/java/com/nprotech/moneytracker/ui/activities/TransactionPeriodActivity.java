package com.nprotech.moneytracker.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.enums.CalendarFilterType;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.CalendarHelper;
import com.nprotech.moneytracker.models.BreakdownChartModel;
import com.nprotech.moneytracker.models.CalendarFilterModel;
import com.nprotech.moneytracker.models.CalendarRangeModel;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.TransactionAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.ChartMarkerView;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.utils.SimpleDividerItemDecoration;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
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
    private LineChart lineChartSpending;
    private Date date;
    private RecyclerView rvTransactions;
    private MaterialCardView breakdownCard;
    private ConstraintLayout emptyWrapper;
    private FrameLayout chartContainer;
    private FloatingActionButton fabAddTransaction;
    private int selectedAccountId, transactionType;
    private StatisticsViewModel statisticsViewModel;
    private AccountViewModel accountViewModel;
    private CalendarFilterType selectedFilter;
    private long customStartDate = -1, customEndDate = -1;
    private Typeface chartFont;
    private String currencySymbol = "";
    private TransactionAdapter transactionAdapter;

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
            lineChartSpending = findViewById(R.id.lineChartSpending);
            rvTransactions = findViewById(R.id.rvTransactions);
            breakdownCard = findViewById(R.id.breakdownCard);
            amountLabel = findViewById(R.id.amountLabel);
            emptyWrapper = findViewById(R.id.emptyWrapper);
            chartContainer = findViewById(R.id.chartContainer);
            fabAddTransaction = findViewById(R.id.fabAddTransaction);

            tvTitle.setText(getString(R.string.period_spending));
            ivCalendar.setVisibility(View.VISIBLE);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(breakdownCard, (view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {

                statisticsViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
                accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

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

            AccountEntity account = accountViewModel.getAccountDetailById(selectedAccountId);
            if (account == null) {
                return;
            }

            currencySymbol = account.currencySymbol;

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

            chartFont = ResourcesCompat.getFont(this, R.font.exo2_medium);

            initializeAdapters();
            updatePeriodSpending();

            statisticsViewModel.getBreakdownChart().observe(this, chart -> {

                if (chart == null || chart.isEmpty()) {

                    barChartSpending.clear();
                    lineChartSpending.clear();

                    amountLabel.setText(CommonUtils.getBeautifyAmount(currencySymbol, 0));

                    chartContainer.setVisibility(View.GONE);
                    breakdownCard.setVisibility(View.GONE);
                    emptyWrapper.setVisibility(View.VISIBLE);

                    return;
                }

                chartContainer.setVisibility(View.VISIBLE);
                emptyWrapper.setVisibility(View.GONE);

                switch (selectedFilter) {

                    case DAILY:
                        showDailyChart(chart);
                        break;

                    case WEEKLY:
                        showWeeklyChart(chart);
                        break;

                    case MONTHLY:
                        showMonthlyChart(chart);
                        break;

                    case QUARTERLY:
                        showQuarterlyChart(chart);
                        break;

                    case YEARLY:
                        showYearlyChart(chart);
                        break;

                    case ALL:
                        showAllChart(chart);
                        break;

                    case CUSTOM:

                        resetChart();

                        long days = ((customEndDate - customStartDate) / (24L * 60 * 60 * 1000L)) + 1;
                        if (days <= 7) {
                            showCustomBarChart(chart);
                        } else {
                            showCustomLineChart(chart);
                        }
                        break;
                }
            });

            statisticsViewModel.getTransactions().observe(this, list -> {

                if(list!=null && !list.isEmpty()) {
                    emptyWrapper.setVisibility(View.GONE);
                    breakdownCard.setVisibility(View.VISIBLE);
                    transactionAdapter.setItems(list);
                    rvTransactions.post(() -> rvTransactions.scrollToPosition(0));
                } else {
                    transactionAdapter.setItems(new ArrayList<>());
                    breakdownCard.setVisibility(View.GONE);
                    emptyWrapper.setVisibility(View.VISIBLE);
                }
            });

            barChartSpending.setExtraBottomOffset(12f);
            lineChartSpending.setExtraBottomOffset(12f);
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAdapters() {
        try {
            rvTransactions.setLayoutManager(new LinearLayoutManager(this));
            transactionAdapter = new TransactionAdapter(this, new ArrayList<>(), R.layout.item_transaction_period_detail);
            rvTransactions.setAdapter(transactionAdapter);
            rvTransactions.setHasFixedSize(true);
            rvTransactions.setItemAnimator(null);
            rvTransactions.addItemDecoration(new SimpleDividerItemDecoration(this));

            rvTransactions.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm == null)
                        return;
                    int last = lm.findLastVisibleItemPosition();
                    if (last >= transactionAdapter.getItemCount() - 5) {
                        statisticsViewModel.loadNextPage();
                    }
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
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

        statisticsViewModel.loadBreakdown(selectedAccountId, transactionType, selectedFilter, date, range.startDate, range.endDate);

        statisticsViewModel.loadTransactions(selectedAccountId, transactionType, range.startDate, range.endDate);
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

                            CalendarFilterType previousFilter = selectedFilter;

                            if (calendarFilter.type == CalendarFilterType.CUSTOM) {
                                openDateDialog(previousFilter);
                            } else {
                                selectedFilter = calendarFilter.type;
                                date = new Date();
                                updateNavigationButtons();
                                updatePeriodSpending();
                            }

                            dialog.dismiss();
                        });
                    }
                };

                rvSelectRange.setAdapter(adapter);
                rvSelectRange.setHasFixedSize(true);
                rvSelectRange.setItemAnimator(null);

                dialog.setContentView(bottomView);
                dialog.show();
            });

            fabAddTransaction.setOnClickListener(v -> {
                v.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(120)
                        .withEndAction(() ->
                                v.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(120)
                                        .start())
                        .start();

                startActivity(new Intent(TransactionPeriodActivity.this, CreateTransactionActivity.class)
                        .putExtra("action", "add")
                        .putExtra("type", TransactionEntity.TYPE_EXPENSE));
                ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
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

        view.findViewById(R.id.tvCancel).setOnClickListener(v -> {
            selectedFilter = calendarFilter;
            dialog.dismiss();
        });

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
            CommonUtils.applyFont(getApplicationContext(), picker.getDatePicker());

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
            CommonUtils.applyFont(getApplicationContext(), picker.getDatePicker());

            // Don't allow selecting an end date before the start date
            if (customStartDate > 0) {
                picker.getDatePicker().setMinDate(customStartDate);
            }

            int color = ContextCompat.getColor(getApplicationContext(), R.color.vibrant_orange);
            picker.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(color);
            picker.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(color);
        });

        view.findViewById(R.id.tvOk).setOnClickListener(v -> {
            selectedFilter = CalendarFilterType.CUSTOM;
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

    private void showDailyChart(List<BreakdownChartModel> dbList) {
        try {

            lineChartSpending.setVisibility(View.VISIBLE);
            barChartSpending.setVisibility(View.GONE);

            resetChart();
            updateTotalAmount(dbList);

            List<Entry> entries = new ArrayList<>();
            List<Long> hours = new ArrayList<>();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date); // selected day

            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            for (int hour = 0; hour < 24; hour++) {
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                hours.add(calendar.getTimeInMillis());
                entries.add(new Entry(hour, 0f));
            }

            for (BreakdownChartModel model : dbList) {
                int hour = (int) model.period;
                if (hour >= 0 && hour < 24) {
                    entries.set(hour, new Entry(hour, (float) model.amount));
                }
            }

            LineDataSet dataSet = new LineDataSet(entries, "");
            dataSet.setDrawValues(false);
            dataSet.setCircleRadius(3f);
            dataSet.setLineWidth(2f);
            dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            dataSet.setColor(ContextCompat.getColor(this, R.color.vibrant_orange));
            dataSet.setCircleColor(ContextCompat.getColor(this, R.color.vibrant_orange));

            LineData data = new LineData(dataSet);
            lineChartSpending.setData(data);

            XAxis axis = lineChartSpending.getXAxis();
            axis.setPosition(XAxis.XAxisPosition.BOTTOM);
            axis.setGranularity(1f);
            axis.setTypeface(chartFont);
            axis.setTextColor(ContextCompat.getColor(this, R.color.black));
            axis.setTextSize(10f);
            axis.setLabelCount(24, true);
            axis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.format(Locale.getDefault(), "%02d", (int) value);
                }
            });

            setupYAxis(data.getYMax(), lineChartSpending.getAxisLeft());

            lineChartSpending.getLegend().setEnabled(false);
            lineChartSpending.getDescription().setEnabled(false);
            lineChartSpending.setScaleEnabled(false);
            lineChartSpending.setPinchZoom(false);
            lineChartSpending.setDragEnabled(false);
            lineChartSpending.animateY(400);

            lineChartSpending.setExtraTopOffset(30f);
            lineChartSpending.setExtraBottomOffset(12f);

            lineChartSpending.invalidate();

            attachMarker(lineChartSpending, hours, "DAILY");

        } catch (Exception e) {
            AppLogger.e(getClass(), "showDailyChart", e);
        }
    }

    private void showWeeklyChart(List<BreakdownChartModel> dbList) {
        try {

            barChartSpending.setVisibility(View.VISIBLE);
            lineChartSpending.setVisibility(View.GONE);

            resetChart();
            updateTotalAmount(dbList);

            List<BarEntry> entries = new ArrayList<>();
            CalendarRangeModel range = CalendarHelper.getWeeklyRange(date);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(range.startDate);

            // Sunday -> Saturday
            List<Long> weekDays = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                weekDays.add(CalendarHelper.getStartOfDay(calendar.getTimeInMillis()));
                entries.add(new BarEntry(i, 0f));
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            for (BreakdownChartModel model : dbList) {
                long day = CalendarHelper.getStartOfDay(model.period);
                for (int i = 0; i < weekDays.size(); i++) {
                    if (weekDays.get(i) == day) {
                        entries.set(i, new BarEntry(i, (float) model.amount));
                        break;
                    }
                }
            }

            BarDataSet dataSet = new BarDataSet(entries, "");
            dataSet.setColor(ContextCompat.getColor(this, R.color.vibrant_orange));
            dataSet.setDrawValues(false);
            BarData data = new BarData(dataSet);
            data.setBarWidth(0.35f);

            barChartSpending.setData(data);

            SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());

            XAxis axis = barChartSpending.getXAxis();
            axis.setPosition(XAxis.XAxisPosition.BOTTOM);
            axis.setDrawGridLines(false);
            axis.setGranularity(1f);
            axis.setGranularityEnabled(true);
            axis.setAxisMinimum(-0.5f);
            axis.setAxisMaximum(6.5f);
            axis.setTypeface(chartFont);
            axis.setTextColor(ContextCompat.getColor(this, R.color.black));
            axis.setTextSize(10f);
            axis.setCenterAxisLabels(false);

            axis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = Math.round(value);

                    if (index >= 0 && index < weekDays.size()) {
                        return dayFormat.format(new Date(weekDays.get(index)));
                    }

                    return "";
                }
            });

            setupYAxis(data.getYMax());

            barChartSpending.getDescription().setEnabled(false);
            barChartSpending.getLegend().setEnabled(false);
            barChartSpending.setScaleEnabled(false);
            barChartSpending.setPinchZoom(false);
            barChartSpending.setDoubleTapToZoomEnabled(false);
            barChartSpending.setDragEnabled(false);
            barChartSpending.setFitBars(true);
            barChartSpending.animateY(400);

            barChartSpending.setExtraTopOffset(30f);
            barChartSpending.setExtraBottomOffset(12f);

            barChartSpending.invalidate();

            attachMarker(barChartSpending, weekDays, "WEEKLY");

        } catch (Exception e) {
            AppLogger.e(getClass(), "showWeeklyChart", e);
        }
    }

    private void showMonthlyChart(List<BreakdownChartModel> dbList) {
        try {

            barChartSpending.setVisibility(View.VISIBLE);
            lineChartSpending.setVisibility(View.GONE);

            resetChart();
            updateTotalAmount(dbList);

            CalendarRangeModel range = CalendarHelper.getMonthlyRange(date);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(range.startDate);

            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            List<Long> monthDays = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            List<BarEntry> entries = new ArrayList<>();

            for (int i = 0; i < daysInMonth; i++) {
                monthDays.add(CalendarHelper.getStartOfDay(calendar.getTimeInMillis()));
                labels.add(String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.DAY_OF_MONTH)));
                entries.add(new BarEntry(i, 0f));
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            for (BreakdownChartModel model : dbList) {
                long day = CalendarHelper.getStartOfDay(model.period);

                for (int i = 0; i < monthDays.size(); i++) {
                    if (monthDays.get(i) == day) {
                        entries.set(i, new BarEntry(i, (float) model.amount));
                        break;
                    }
                }
            }

            BarDataSet dataSet = new BarDataSet(entries, "");
            dataSet.setColor(ContextCompat.getColor(this, R.color.vibrant_orange));
            dataSet.setDrawValues(false);

            BarData data = new BarData(dataSet);
            data.setBarWidth(0.35f);

            barChartSpending.setData(data);

            XAxis axis = barChartSpending.getXAxis();
            axis.setPosition(XAxis.XAxisPosition.BOTTOM);
            axis.setDrawGridLines(false);
            axis.setGranularity(1f);
            axis.setGranularityEnabled(true);
            axis.setAxisMinimum(-0.5f);
            axis.setAxisMaximum(daysInMonth - 0.5f);
            axis.setValueFormatter(new IndexAxisValueFormatter(labels));
            axis.setTypeface(chartFont);
            axis.setTextColor(ContextCompat.getColor(this, R.color.black));
            axis.setTextSize(10f);

            setupYAxis(data.getYMax());

            barChartSpending.getDescription().setEnabled(false);
            barChartSpending.getLegend().setEnabled(false);
            barChartSpending.setScaleEnabled(true);
            barChartSpending.setDragEnabled(true);
            barChartSpending.setPinchZoom(false);
            barChartSpending.setDoubleTapToZoomEnabled(false);
            barChartSpending.setVisibleXRangeMaximum(7); // Show 7 days at a time
            barChartSpending.moveViewToX(-1);
            barChartSpending.setFitBars(true);
            barChartSpending.animateY(400);

            barChartSpending.invalidate();

            attachMarker(barChartSpending, monthDays, "MONTHLY");

        } catch (Exception e) {
            AppLogger.e(getClass(), "showMonthlyChart", e);
        }
    }

    private void showQuarterlyChart(List<BreakdownChartModel> dbList) {
        try {
            barChartSpending.setVisibility(View.VISIBLE);
            lineChartSpending.setVisibility(View.GONE);

            resetChart();
            updateTotalAmount(dbList);

            CalendarRangeModel range = CalendarHelper.getQuarterRange(date);

            List<BarEntry> entries = new ArrayList<>();
            List<Long> months = new ArrayList<>();

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(range.startDate);

            for (int i = 0; i < 3; i++) {

                months.add(CalendarHelper.getStartOfMonth(calendar.getTimeInMillis()));
                entries.add(new BarEntry(i, 0f));

                calendar.add(Calendar.MONTH, 1);
            }

            for (BreakdownChartModel model : dbList) {
                long month = CalendarHelper.getStartOfMonth(model.period);
                for (int i = 0; i < months.size(); i++) {
                    if (months.get(i).equals(month)) {
                        float currentAmount = entries.get(i).getY();
                        entries.set(i, new BarEntry(i, currentAmount + (float) model.amount));
                        break;
                    }
                }
            }

            BarDataSet dataSet = new BarDataSet(entries, "");
            dataSet.setColor(ContextCompat.getColor(this, R.color.vibrant_orange));
            dataSet.setDrawValues(false);

            BarData data = new BarData(dataSet);
            data.setBarWidth(0.25f);

            barChartSpending.setData(data);

            XAxis axis = barChartSpending.getXAxis();
            axis.setPosition(XAxis.XAxisPosition.BOTTOM);
            axis.setDrawGridLines(false);
            axis.setGranularity(1f);
            axis.setGranularityEnabled(true);
            axis.setAxisMinimum(-0.5f);
            axis.setAxisMaximum(6.5f);
            axis.setTypeface(chartFont);
            axis.setTextColor(ContextCompat.getColor(this, R.color.black));
            axis.setTextSize(10f);
            axis.setCenterAxisLabels(false);
            SimpleDateFormat dayFormat = new SimpleDateFormat("MMM", Locale.getDefault());
            axis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = Math.round(value);

                    if (index >= 0 && index < months.size()) {
                        return dayFormat.format(new Date(months.get(index)));
                    }

                    return "";
                }
            });

            barChartSpending.setVisibleXRangeMaximum(months.size());
            barChartSpending.moveViewToX(0f);

            setupYAxis(data.getYMax());

            barChartSpending.getDescription().setEnabled(false);
            barChartSpending.getLegend().setEnabled(false);

            barChartSpending.setScaleEnabled(false);
            barChartSpending.setPinchZoom(false);
            barChartSpending.setDragEnabled(false);
            barChartSpending.setDoubleTapToZoomEnabled(false);
            barChartSpending.setFitBars(true);
            barChartSpending.notifyDataSetChanged();
            barChartSpending.fitScreen();
            barChartSpending.moveViewToX(0f);
            barChartSpending.highlightValues(null);
            barChartSpending.animateY(400);

            barChartSpending.invalidate();

            attachMarker(barChartSpending, months, "QUARTERLY");
        } catch (Exception e) {
            AppLogger.e(getClass(), "showQuarterlyChart", e);
        }
    }

    private void showYearlyChart(List<BreakdownChartModel> dbList) {
        try {
            barChartSpending.setVisibility(View.VISIBLE);
            lineChartSpending.setVisibility(View.GONE);

            resetChart();
            updateTotalAmount(dbList);

            CalendarRangeModel range = CalendarHelper.getYearRange(date);

            List<BarEntry> entries = new ArrayList<>();
            List<Long> months = new ArrayList<>();

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(range.startDate);

            for (int i = 0; i < 12; i++) {

                months.add(CalendarHelper.getStartOfMonth(calendar.getTimeInMillis()));
                entries.add(new BarEntry(i, 0f));

                calendar.add(Calendar.MONTH, 1);
            }

            for (BreakdownChartModel model : dbList) {

                long month = CalendarHelper.getStartOfMonth(model.period);

                for (int i = 0; i < months.size(); i++) {

                    if (months.get(i) == month) {

                        entries.set(i,
                                new BarEntry(i, (float) model.amount));

                        break;
                    }
                }
            }

            BarDataSet dataSet = new BarDataSet(entries, "");
            dataSet.setColor(ContextCompat.getColor(this, R.color.vibrant_orange));
            dataSet.setDrawValues(false);
            dataSet.setHighLightAlpha(120);

            BarData data = new BarData(dataSet);
            data.setBarWidth(0.45f);

            barChartSpending.setData(data);

            XAxis axis = barChartSpending.getXAxis();
            axis.setPosition(XAxis.XAxisPosition.BOTTOM);
            axis.setDrawGridLines(false);
            axis.setGranularity(1f);
            axis.setGranularityEnabled(true);
            axis.setAxisMinimum(-0.5f);
            axis.setAxisMaximum(entries.size() - 0.5f); // 11.5f
            axis.setLabelCount(12, false);
            axis.setTypeface(chartFont);
            axis.setTextColor(ContextCompat.getColor(this, R.color.black));
            axis.setTextSize(10f);
            axis.setCenterAxisLabels(false);
            SimpleDateFormat dayFormat = new SimpleDateFormat("MMM", Locale.getDefault());
            axis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = Math.round(value);

                    if (index >= 0 && index < months.size()) {
                        return dayFormat.format(new Date(months.get(index)));
                    }

                    return "";
                }
            });

            setupYAxis(data.getYMax());

            barChartSpending.getDescription().setEnabled(false);
            barChartSpending.getLegend().setEnabled(false);
            barChartSpending.setScaleEnabled(true);
            barChartSpending.setDragEnabled(true);
            barChartSpending.setPinchZoom(false);
            barChartSpending.setDoubleTapToZoomEnabled(false);
            barChartSpending.setVisibleXRangeMaximum(12); // Show 7 days at a time
            barChartSpending.moveViewToX(-1);
            barChartSpending.setFitBars(true);
            barChartSpending.notifyDataSetChanged();
            barChartSpending.fitScreen();
            barChartSpending.animateY(400);

            barChartSpending.invalidate();

            attachMarker(barChartSpending, months, "YEARLY");
        } catch (Exception e) {
            AppLogger.e(getClass(), "showYearlyChart", e);
        }
    }

    private void showAllChart(List<BreakdownChartModel> dbList) {
        try {
            barChartSpending.setVisibility(View.GONE);
            lineChartSpending.setVisibility(View.VISIBLE);

            resetChart();
            updateTotalAmount(dbList);

            List<Entry> entries = new ArrayList<>();
            List<Long> years = new ArrayList<>();

            Calendar calendar = Calendar.getInstance();
            int currentYear = calendar.get(Calendar.YEAR);
            int startYear = currentYear - 4; // Last 5 years including current

            for (int year = startYear, index = 0;
                 year <= currentYear;
                 year++, index++) {

                years.add((long) year);
                entries.add(new Entry(index, 0f));
            }

            for (BreakdownChartModel model : dbList) {

                int year = (int) model.period;

                if (year < startYear || year > currentYear) {
                    continue;
                }

                int index = year - startYear;
                entries.set(index, new Entry(index, (float) model.amount));
            }

            LineDataSet dataSet = new LineDataSet(entries, "");

            dataSet.setColor(ContextCompat.getColor(this, R.color.vibrant_orange));
            dataSet.setCircleColor(ContextCompat.getColor(this, R.color.vibrant_orange));
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(3f);
            dataSet.setDrawValues(false);
            dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

            LineData data = new LineData(dataSet);

            lineChartSpending.setData(data);

            XAxis axis = lineChartSpending.getXAxis();

            axis.setPosition(XAxis.XAxisPosition.BOTTOM);
            axis.setDrawGridLines(false);
            axis.setGranularity(1f);
            axis.setLabelCount(years.size(), true);
            axis.setTypeface(chartFont);
            axis.setTextColor(ContextCompat.getColor(this, R.color.black));
            axis.setTextSize(9f);

            axis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {

                    int index = (int) value;

                    if (index < 0 || index >= years.size()) {
                        return "";
                    }

                    return String.valueOf(years.get(index));
                }
            });

            setupYAxis(data.getYMax());

            lineChartSpending.getDescription().setEnabled(false);
            lineChartSpending.getLegend().setEnabled(false);

            lineChartSpending.setDrawGridBackground(false);

            lineChartSpending.setExtraLeftOffset(16f);
            lineChartSpending.setExtraTopOffset(30f);
            lineChartSpending.setExtraBottomOffset(12f);

            lineChartSpending.setScaleEnabled(false);
            lineChartSpending.setScaleXEnabled(false);
            lineChartSpending.setScaleYEnabled(false);

            lineChartSpending.setPinchZoom(false);
            lineChartSpending.setDoubleTapToZoomEnabled(false);
            lineChartSpending.setDragEnabled(false);

            lineChartSpending.animateY(400);

            lineChartSpending.invalidate();

            attachMarker(lineChartSpending, years, "ALL");
        } catch (Exception e) {
            AppLogger.e(getClass(), "showAllChart", e);
        }
    }

    private void showCustomBarChart(List<BreakdownChartModel> dbList) {
        try {
            barChartSpending.setVisibility(View.VISIBLE);
            lineChartSpending.setVisibility(View.GONE);

            resetChart();
            updateTotalAmount(dbList);

            CalendarRangeModel range = CalendarHelper.getCustomRange(customStartDate, customEndDate);

            List<BarEntry> entries = new ArrayList<>();
            List<Long> days = new ArrayList<>();

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(range.startDate);

            int index = 0;

            while (calendar.getTimeInMillis() <= range.endDate) {

                days.add(CalendarHelper.getStartOfDay(calendar.getTimeInMillis()));
                entries.add(new BarEntry(index, 0f));

                calendar.add(Calendar.DAY_OF_MONTH, 1);
                index++;
            }

            for (BreakdownChartModel model : dbList) {

                long day = CalendarHelper.getStartOfDay(model.period);

                for (int i = 0; i < days.size(); i++) {

                    if (days.get(i) == day) {

                        entries.set(i,
                                new BarEntry(i, (float) model.amount));

                        break;
                    }
                }
            }

            BarDataSet dataSet = new BarDataSet(entries, "");

            dataSet.setColor(ContextCompat.getColor(this,
                    R.color.vibrant_orange));

            dataSet.setDrawValues(false);
            dataSet.setHighLightAlpha(120);

            BarData data = new BarData(dataSet);
            data.setBarWidth(0.45f);

            barChartSpending.setData(data);

            XAxis axis = barChartSpending.getXAxis();

            axis.setPosition(XAxis.XAxisPosition.BOTTOM);
            axis.setDrawGridLines(false);
            axis.setGranularity(1f);
            axis.setLabelCount(days.size(), true);
            axis.setTypeface(chartFont);
            axis.setTextColor(ContextCompat.getColor(this, R.color.black));
            axis.setTextSize(9f);

            axis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {

                    int index = (int) value;

                    if (index < 0 || index >= days.size()) {
                        return "";
                    }

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(days.get(index));

                    return new SimpleDateFormat("dd", Locale.getDefault())
                            .format(calendar.getTime());
                }
            });

            setupYAxis(data.getYMax());

            barChartSpending.getDescription().setEnabled(false);
            barChartSpending.getLegend().setEnabled(false);

            barChartSpending.setDrawGridBackground(false);
            barChartSpending.setDrawBorders(false);

            barChartSpending.setScaleEnabled(false);
            barChartSpending.setScaleXEnabled(false);
            barChartSpending.setScaleYEnabled(false);

            barChartSpending.setPinchZoom(false);
            barChartSpending.setDoubleTapToZoomEnabled(false);
            barChartSpending.setDragEnabled(false);

            barChartSpending.setFitBars(true);
            barChartSpending.animateY(400);

            barChartSpending.invalidate();

            attachMarker(barChartSpending, days, "CUSTOM");
        } catch (Exception e) {
            AppLogger.e(getClass(), "showCustomBarChart", e);
        }
    }

    private void showCustomLineChart(List<BreakdownChartModel> dbList) {
        try {
            barChartSpending.setVisibility(View.GONE);
            lineChartSpending.setVisibility(View.VISIBLE);

            resetChart();
            updateTotalAmount(dbList);

            CalendarRangeModel range = CalendarHelper.getCustomRange(customStartDate, customEndDate);

            List<Entry> entries = new ArrayList<>();
            List<Long> days = new ArrayList<>();

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(range.startDate);

            int index = 0;

            while (calendar.getTimeInMillis() <= range.endDate) {

                days.add(CalendarHelper.getStartOfDay(calendar.getTimeInMillis()));
                entries.add(new Entry(index, 0f));

                calendar.add(Calendar.DAY_OF_MONTH, 1);
                index++;
            }

            for (BreakdownChartModel model : dbList) {

                long day = CalendarHelper.getStartOfDay(model.period);

                for (int i = 0; i < days.size(); i++) {

                    if (days.get(i) == day) {

                        entries.set(i, new Entry(i, (float) model.amount));
                        break;
                    }
                }
            }

            LineDataSet dataSet = new LineDataSet(entries, "");

            dataSet.setColor(ContextCompat.getColor(this, R.color.vibrant_orange));
            dataSet.setCircleColor(ContextCompat.getColor(this, R.color.vibrant_orange));
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(3f);
            dataSet.setDrawValues(false);
            dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

            LineData data = new LineData(dataSet);

            lineChartSpending.setData(data);

            XAxis axis = lineChartSpending.getXAxis();

            axis.setPosition(XAxis.XAxisPosition.BOTTOM);
            axis.setDrawGridLines(false);
            axis.setGranularity(1f);
            axis.setAxisMinimum(-0.5f);
            axis.setAxisMaximum(entries.size() - 0.5f);

            axis.setLabelCount(Math.min(days.size(), 10), false);

            axis.setTypeface(chartFont);
            axis.setTextColor(ContextCompat.getColor(this, R.color.black));
            axis.setTextSize(9f);

            axis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {

                    int index = (int) value;

                    if (index < 0 || index >= days.size()) {
                        return "";
                    }

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(days.get(index));

                    return new SimpleDateFormat("dd", Locale.getDefault())
                            .format(calendar.getTime());
                }
            });

            setupYAxis(data.getYMax());

            lineChartSpending.getDescription().setEnabled(false);
            lineChartSpending.getLegend().setEnabled(false);

            lineChartSpending.setDrawGridBackground(false);

            lineChartSpending.setScaleEnabled(false);
            lineChartSpending.setScaleXEnabled(false);
            lineChartSpending.setScaleYEnabled(false);

            lineChartSpending.setPinchZoom(false);
            lineChartSpending.setDoubleTapToZoomEnabled(false);

            lineChartSpending.setDragEnabled(true);
            lineChartSpending.setVisibleXRangeMaximum(10f);
            lineChartSpending.moveViewToX(0f);

            lineChartSpending.animateY(400);

            lineChartSpending.invalidate();

            attachMarker(lineChartSpending, days, "CUSTOM");
        } catch (Exception e) {
            AppLogger.e(getClass(), "showCustomLineChart", e);
        }
    }

    private void setupYAxis(float max) {

        YAxis left;

        if (barChartSpending.getVisibility() == View.VISIBLE) {
            left = barChartSpending.getAxisLeft();
            barChartSpending.getAxisRight().setEnabled(false);
        } else {
            left = lineChartSpending.getAxisLeft();
            lineChartSpending.getAxisRight().setEnabled(false);
            left.setDrawLabels(true);
            left.setXOffset(8f);
        }

        left.setDrawAxisLine(false);
        left.setDrawGridLines(true);
        left.enableGridDashedLine(10f, 10f, 0f);

        left.setAxisMinimum(0f);

        max = Math.max(1f, max);

        float interval = calculateNiceInterval(max);
        float axisMax;

        if (max <= 100f) {
            axisMax = 100f;
        } else {
            axisMax = (float) Math.ceil((max * 1.1f) / interval) * interval;
        }

        left.setAxisMaximum(axisMax);
        left.setGranularity(interval);
        left.setLabelCount((int) (axisMax / interval) + 1, true);

        left.setTypeface(chartFont);
        left.setTextColor(ContextCompat.getColor(this, R.color.black));
        left.setTextSize(11f);
        left.setSpaceTop(10f);
    }

    private void setupYAxis(float maxValue, YAxis leftAxis) {

        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawGridLines(true);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);

        leftAxis.setAxisMinimum(0f);

        float max = Math.max(1f, maxValue);

        float interval = calculateNiceInterval(max);

        // Add 20% headroom
        float axisMax = max * 1.2f;

        // Round to a nice interval
        axisMax = (float) Math.ceil(axisMax / interval) * interval;

        // If max <= 100, always show up to 100
        if (axisMax < 100f) {
            axisMax = 100f;
        }

        leftAxis.setAxisMaximum(axisMax);
        leftAxis.setGranularity(interval);
        leftAxis.setLabelCount((int) (axisMax / interval) + 1, true);

        leftAxis.setTypeface(chartFont);
        leftAxis.setTextColor(ContextCompat.getColor(this, R.color.black));
        leftAxis.setTextSize(11f);
        leftAxis.setSpaceTop(10f);
    }

    private float calculateNiceInterval(float max) {

        float rawInterval = max / 5f;

        float magnitude = (float) Math.pow(10, Math.floor(Math.log10(rawInterval)));

        float residual = rawInterval / magnitude;

        if (residual > 5f)
            return 10f * magnitude;

        if (residual > 2f)
            return 5f * magnitude;

        if (residual > 1f)
            return 2f * magnitude;

        return magnitude;
    }

    private void resetChart() {
        barChartSpending.fitScreen();
        barChartSpending.clearAnimation();
        barChartSpending.highlightValues(null);
        barChartSpending.setScaleEnabled(false);
        barChartSpending.setDragEnabled(false);
        barChartSpending.setPinchZoom(false);
        barChartSpending.setDoubleTapToZoomEnabled(false);

        lineChartSpending.fitScreen();
        lineChartSpending.clearAnimation();
        lineChartSpending.highlightValues(null);
        lineChartSpending.setScaleEnabled(false);
        lineChartSpending.setDragEnabled(false);
        lineChartSpending.setPinchZoom(false);
        lineChartSpending.setDoubleTapToZoomEnabled(false);
    }

    private <T extends Chart<?>> void attachMarker(T chart, List<Long> periods, String filter) {
        ChartMarkerView marker = new ChartMarkerView(this, periods, filter, currencySymbol);
        marker.setChartView(chart);
        chart.setMarker(marker);
    }

    private void updateTotalAmount(List<BreakdownChartModel> dbList) {

        double total = 0;

        for (BreakdownChartModel model : dbList) {
            total += model.amount;
        }

        amountLabel.setText(CommonUtils.getBeautifyAmount(currencySymbol, total));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePeriodSpending();
    }
}