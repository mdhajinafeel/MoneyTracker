package com.nprotech.moneytracker.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.enums.CalendarFilterType;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.CalendarHelper;
import com.nprotech.moneytracker.models.BreakdownFilter;
import com.nprotech.moneytracker.models.CalendarRangeModel;
import com.nprotech.moneytracker.ui.adapters.BreakdownPagerAdapter;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.viewmodel.StatisticsViewModel;

import java.util.Calendar;
import java.util.Date;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionBreakdownActivity extends BaseActivity {

    private AppCompatImageView icBack, ivCalendar, ivPrevious, ivNext;
    private AppCompatTextView tvDate, tvStartDate, tvEndDate;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private Date date;
    private int selectedAccountId;
    private StatisticsViewModel statisticsViewModel;
    private CalendarFilterType selectedFilter;
    private long customStartDate = -1, customEndDate = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_breakdown);
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
            tabLayout = findViewById(R.id.tabLayout);
            viewPager = findViewById(R.id.viewPager);

            tvTitle.setText(getString(R.string.breakdown));
            ivCalendar.setVisibility(View.VISIBLE);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(viewPager, (view, insets) -> {
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

            updateBreakdown();

            BreakdownPagerAdapter adapter = new BreakdownPagerAdapter(this);
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(1);

            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                if (position == 0) {
                    tab.setText(getString(R.string.income));
                } else {
                    tab.setText(getString(R.string.expense));
                }
            }).attach();

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

                @Override
                public void onTabSelected(TabLayout.Tab tab) {

                    // 🔥 Animation
                    View tabView = ((ViewGroup) tabLayout.getChildAt(0))
                            .getChildAt(tab.getPosition());

                    tabView.animate()
                            .scaleX(1.1f)
                            .scaleY(1.1f)
                            .setDuration(150)
                            .withEndAction(() -> tabView.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(150)
                                    .start())
                            .start();
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });

            viewPager.setCurrentItem(1, false);
            viewPager.setPageTransformer(null);
            viewPager.setOffscreenPageLimit(1);
            viewPager.setUserInputEnabled(true);

        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
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

            getOnBackPressedDispatcher().addCallback(this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            finish();
                            ActivityUtils.overrideCloseTransition(TransactionBreakdownActivity.this, R.anim.scale_in, R.anim.bottom_to_top);
                        }
                    });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void updateBreakdown() {

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
        statisticsViewModel.setBreakdownFilter(new BreakdownFilter(selectedAccountId, selectedFilter, date, range.startDate, range.endDate));
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
        updateBreakdown();
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
}