package com.nprotech.moneytracker.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.enums.CalendarFilterType;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.CalendarHelper;
import com.nprotech.moneytracker.models.CalendarFilterModel;
import com.nprotech.moneytracker.models.CalendarRangeModel;
import com.nprotech.moneytracker.ui.adapters.DailyTransactionAdapter;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.StatisticsViewModel;
import com.nprotech.moneytracker.viewmodel.TransactionViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;
import me.grantland.widget.AutofitTextView;

@AndroidEntryPoint
public class TransactionOverviewActivity extends BaseActivity {

    private AppCompatImageView icBack, ivCalendar, ivPrevious, ivNext;
    private AppCompatTextView tvDate, tvStartDate, tvEndDate;
    private AutofitTextView tvIncome, tvExpense, tvTotal;
    private ConstraintLayout emptyWrapper;
    private RecyclerView rvTransactions;
    private FloatingActionButton fabAddTransaction;
    private Date date;
    private AccountViewModel accountViewModel;
    private StatisticsViewModel statisticsViewModel;
    private TransactionViewModel transactionViewModel;
    private int selectedAccountId = -1, accountId = 0;
    private long loadedStart = -1, loadedEnd = -1;
    private String currencySymbol = "";
    private DailyTransactionAdapter dailyTransactionAdapter;
    private CalendarFilterType selectedFilter;
    private long customStartDate = -1, customEndDate = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_overview);
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
            tvIncome = findViewById(R.id.tvIncome);
            tvExpense = findViewById(R.id.tvExpense);
            tvTotal = findViewById(R.id.tvTotal);
            ivPrevious = findViewById(R.id.ivPrevious);
            ivNext = findViewById(R.id.ivNext);
            rvTransactions = findViewById(R.id.rvTransactions);
            emptyWrapper = findViewById(R.id.emptyWrapper);
            fabAddTransaction = findViewById(R.id.fabAddTransaction);

            tvTitle.setText(getString(R.string.transaction_overview));
            ivCalendar.setVisibility(View.VISIBLE);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(rvTransactions, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
            statisticsViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
            transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {
                bindData(bundle);
                initializeAdapters();
                observeData();
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

            accountId = bundle.getInt("accountId", 0);

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
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void observeData() {
        try {
            AccountEntity account = accountViewModel.getAccountDetailById(accountId);
            if (account != null) {
                selectedAccountId = account.id;
                currencySymbol = account.currencySymbol;

                loadedStart = -1;
                loadedEnd = -1;
                loadCalendarData();
            }

            statisticsViewModel.getCalendarHeader().observe(this, header -> {
                if (header == null)
                    return;
                tvIncome.setText(CommonUtils.getBeautifyAmount(currencySymbol, header.income));
                tvExpense.setText(CommonUtils.getBeautifyAmount(currencySymbol, header.expense));
                tvTotal.setText(CommonUtils.getBeautifyAmount(currencySymbol, header.total));
            });

            transactionViewModel.getDailyTransactions().observe(this, dailyTransModels -> {

                if (dailyTransModels == null || dailyTransModels.isEmpty()) {
                    rvTransactions.setVisibility(View.GONE);
                    emptyWrapper.setVisibility(View.VISIBLE);
                } else {
                    emptyWrapper.setVisibility(View.GONE);
                    rvTransactions.setVisibility(View.VISIBLE);
                    dailyTransactionAdapter.setItems(dailyTransModels);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "observeData", e);
        }
    }

    private void loadCalendarData() {

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

        if (loadedStart == range.startDate && loadedEnd == range.endDate) {
            return;
        }

        loadedStart = range.startDate;
        loadedEnd = range.endDate;

        statisticsViewModel.loadCalendar(selectedAccountId, range.startDate, range.endDate);
        transactionViewModel.loadTransactions(selectedAccountId, range.startDate, range.endDate);
    }

    private void initializeAdapters() {
        try {
            rvTransactions.setLayoutManager(new LinearLayoutManager(this));
            dailyTransactionAdapter = new DailyTransactionAdapter(this, new ArrayList<>(), currencySymbol);
            rvTransactions.setAdapter(dailyTransactionAdapter);
            rvTransactions.setHasFixedSize(true);
            rvTransactions.setItemAnimator(null);

            rvTransactions.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm == null)
                        return;
                    int last = lm.findLastVisibleItemPosition();
                    if (last >= dailyTransactionAdapter.getItemCount() - 5) {
                        transactionViewModel.loadNextPage();
                    }
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.bottom_to_top);
            });

            ivPrevious.setOnClickListener(v -> moveDate(-1));

            ivNext.setOnClickListener(v -> moveDate(1));

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

                startActivity(new Intent(TransactionOverviewActivity.this, CreateTransactionActivity.class)
                        .putExtra("action", "add")
                        .putExtra("type", TransactionEntity.TYPE_EXPENSE));
                ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            getOnBackPressedDispatcher().addCallback(this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            finish();
                            ActivityUtils.overrideCloseTransition(TransactionOverviewActivity.this, R.anim.scale_in, R.anim.bottom_to_top);
                        }
                    });

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
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
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

    private void updateNavigationButtons() {

        boolean enabled = selectedFilter != CalendarFilterType.ALL
                && selectedFilter != CalendarFilterType.CUSTOM;

        ivPrevious.setEnabled(enabled);
        ivNext.setEnabled(enabled);

        float alpha = enabled ? 1f : 0.3f;
        ivPrevious.setAlpha(alpha);
        ivNext.setAlpha(alpha);
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