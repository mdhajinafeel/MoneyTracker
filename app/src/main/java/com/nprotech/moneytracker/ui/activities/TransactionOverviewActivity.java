package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.CalendarHelper;
import com.nprotech.moneytracker.ui.adapters.DailyTransactionAdapter;
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
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionOverviewActivity extends BaseActivity {

    private AppCompatImageView icBack, ivPrevious, ivNext;
    private AppCompatTextView tvDate, tvIncome, tvExpense, tvTotal;
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
            long transactionDate = bundle.getLong("transactionDate", -1);

            if (transactionDate != -1) {
                date = new Date(transactionDate);
            } else {
                date = CalendarHelper.getInitialDate();
            }

            tvDate.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date));
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
        transactionViewModel.loadTransactions(selectedAccountId, start, end);
    }

    private void initializeAdapters() {
        try {
            rvTransactions.setLayoutManager(new LinearLayoutManager(this));
            dailyTransactionAdapter = new DailyTransactionAdapter(this, new ArrayList<>());
            rvTransactions.setAdapter(dailyTransactionAdapter);
            rvTransactions.setHasFixedSize(true);
            rvTransactions.setItemAnimator(null);
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
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }
}