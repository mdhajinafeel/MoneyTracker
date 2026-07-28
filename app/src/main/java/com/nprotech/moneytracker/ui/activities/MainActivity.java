package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.IConstants;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.ui.fragments.CalendarFragment;
import com.nprotech.moneytracker.ui.fragments.StatisticsFragment;
import com.nprotech.moneytracker.ui.fragments.TransactionFragment;
import com.nprotech.moneytracker.ui.fragments.MoreFragment;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.TransactionViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends BaseActivity {

    private ConstraintLayout balanceWrapper, incomeWrapper, expenseWrapper;
    private AppCompatTextView tvAccountName, tvAccountBalance, tvTotalIncome, tvTotalExpense;
    private AccountViewModel accountViewModel;
    private TransactionViewModel transactionViewModel;
    private BottomNavigationView bottomNav;
    private AppCompatImageView ivSettings, ivChart, ivCalendar;
    private final List<AccountEntity> accountList = new ArrayList<>();
    private final TransactionFragment transactionFragment = new TransactionFragment();
    private final CalendarFragment calendarFragment = new CalendarFragment();
    private final StatisticsFragment statisticsFragment = new StatisticsFragment();
    private final MoreFragment moreFragment = new MoreFragment();
    private Fragment activeFragment;
    private long lastBackPressedTime = 0;
    private static final long EXIT_INTERVAL = 2000;
    private ToolbarActionListener toolbarActionListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {

            View toolbarWrapper = findViewById(R.id.toolbarWrapper);

            balanceWrapper = findViewById(R.id.balanceWrapper);
            incomeWrapper = findViewById(R.id.incomeWrapper);
            expenseWrapper = findViewById(R.id.expenseWrapper);
            ivSettings = findViewById(R.id.ivSettings);
            ivChart = findViewById(R.id.ivChart);
            ivCalendar = findViewById(R.id.ivCalendar);
            tvAccountName = findViewById(R.id.tvAccountName);
            tvAccountBalance = findViewById(R.id.tvAccountBalance);
            tvTotalIncome = findViewById(R.id.tvTotalIncome);
            tvTotalExpense = findViewById(R.id.tvTotalExpense);
            bottomNav = findViewById(R.id.bottomNav);

            accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
            transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top,
                        v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
                Insets systemBars =
                        insets.getInsets(WindowInsetsCompat.Type.systemBars());

                v.setPadding(0, 0, 0, systemBars.bottom);
                return insets;
            });

            accountViewModel.selectAccount((int) PreferenceManager.INSTANCE.getAccountId());

            observeData();

            bottomNav.setOnItemSelectedListener(item -> {

                View itemView = bottomNav.findViewById(item.getItemId());

                if (itemView != null) {
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.nav_item_animation);
                    itemView.startAnimation(animation);
                }

                if (item.getItemId() == R.id.nav_transaction) {
                    loadFragment(transactionFragment);
                    return true;
                } else if (item.getItemId() == R.id.nav_calendar) {
                    loadFragment(calendarFragment);
                    return true;
                } else if (item.getItemId() == R.id.nav_statistic) {
                    loadFragment(statisticsFragment);
                    return true;
                } else if (item.getItemId() == R.id.nav_more) {
                    loadFragment(moreFragment);
                    return true;
                }

                return false;
            });

            loadStartUpScreen();

            getSupportFragmentManager().addOnBackStackChangedListener(() -> {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                if (fragment instanceof TransactionFragment) {
                    bottomNav.setSelectedItemId(R.id.nav_transaction);
                } else if (fragment instanceof CalendarFragment) {
                    bottomNav.setSelectedItemId(R.id.nav_calendar);
                } else if (fragment instanceof StatisticsFragment) {
                    bottomNav.setSelectedItemId(R.id.nav_statistic);
                } else if (fragment instanceof MoreFragment) {
                    bottomNav.setSelectedItemId(R.id.nav_more);
                }
            });

            getOnBackPressedDispatcher().addCallback(
                    this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {

                            long currentTime = System.currentTimeMillis();

                            if (currentTime - lastBackPressedTime < EXIT_INTERVAL) {
                                finishAffinity();
                            } else {
                                lastBackPressedTime = currentTime;
                                Toast.makeText(MainActivity.this, R.string.press_again_to_exit, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

            clickListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void observeData() {
        accountViewModel.getSelectedAccount().observe(this, accountEntity -> {
            if (accountEntity == null) {
                return;
            }

            tvAccountName.setText(accountEntity.name);
            tvAccountBalance.setText(CommonUtils.getBeautifyAmount(accountEntity.currencySymbol, accountEntity.balance));

            transactionViewModel.accountSummaryById(accountEntity.id).observe(this, balanceSummaryModel -> {
                tvTotalIncome.setText(CommonUtils.getBeautifyAmount(accountEntity.currencySymbol, balanceSummaryModel.openingBalance));
                tvTotalExpense.setText(CommonUtils.getBeautifyAmount(accountEntity.currencySymbol, balanceSummaryModel.closingBalance));
            });
        });

        accountViewModel.getAllAccounts().observe(this, accounts -> {
            accountList.clear();
            if (accounts != null) {
                accountList.addAll(accounts);
            }
        });
    }

    private void clickListeners() {
        try {
            tvAccountName.setOnClickListener(v -> switchAccounts());

            ivSettings.setOnClickListener(view -> {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            ivChart.setOnClickListener(v -> {
                if (toolbarActionListener != null) {
                    toolbarActionListener.onChartClicked();
                }
            });

            ivCalendar.setOnClickListener(v -> {
                if (toolbarActionListener != null) {
                    toolbarActionListener.onCalendarClicked();
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "clickListeners", e);
        }
    }

    private void switchAccounts() {
        try {

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_account_picker_layout, findViewById(android.R.id.content), false);
            RecyclerView rvAccounts = bottomView.findViewById(R.id.rvAccounts);
            LinearLayout layoutAddAccount = bottomView.findViewById(R.id.layoutAddAccount);

            RecyclerViewAdapter<AccountEntity> adapter = new RecyclerViewAdapter<>(getApplicationContext(), accountList, R.layout.item_switch_accounts) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, AccountEntity accountEntity) {
                    holder.setViewText(R.id.tvAccountName, accountEntity.name);
                    holder.setViewText(R.id.tvAccountBalance, getString(R.string.account_balance_format,
                            CommonUtils.getBeautifyAmount(accountEntity.currencySymbol, accountEntity.balance)));
                    holder.getView(R.id.ivSelected).setVisibility(PreferenceManager.INSTANCE.getAccountId() == accountEntity.id ? View.VISIBLE : View.GONE);
                    holder.getView(R.id.rlAccountView).setOnClickListener(v -> {
                        PreferenceManager.INSTANCE.setAccountId(accountEntity.id);
                        accountViewModel.selectAccount(accountEntity.id);
                        dialog.dismiss();
                    });
                }
            };

            rvAccounts.setAdapter(adapter);
            rvAccounts.setHasFixedSize(true);

            layoutAddAccount.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, AddAccountActivity.class));
                ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            dialog.setContentView(bottomView);
            dialog.show();

        } catch (Exception e) {
            AppLogger.e(getClass(), "switchAccounts", e);
        }
    }

    private void loadFragment(Fragment fragment) {

        if (fragment == activeFragment)
            return;

        if (isFinishing() || isDestroyed())
            return;

        getSupportFragmentManager().beginTransaction()
                .hide(activeFragment)
                .show(fragment).commitNow();

        activeFragment = fragment;

        if (activeFragment instanceof ToolbarActionListener) {
            toolbarActionListener = (ToolbarActionListener) fragment;
        } else {
            toolbarActionListener = null;
        }

        updateToolbar(activeFragment);
    }

    private void updateToolbar(Fragment fragment) {

        if (fragment instanceof TransactionFragment) {
            balanceWrapper.setVisibility(View.VISIBLE);
            ivSettings.setVisibility(View.GONE);
            ivChart.setVisibility(View.GONE);
            ivCalendar.setVisibility(View.GONE);

            incomeWrapper.setVisibility(View.VISIBLE);
            expenseWrapper.setVisibility(View.VISIBLE);
        } else if (fragment instanceof CalendarFragment) {
            balanceWrapper.setVisibility(View.GONE);
            ivSettings.setVisibility(View.GONE);
            ivChart.setVisibility(View.GONE);
            ivCalendar.setVisibility(View.GONE);

            incomeWrapper.setVisibility(View.GONE);
            expenseWrapper.setVisibility(View.GONE);
        } else if (fragment instanceof StatisticsFragment) {
            balanceWrapper.setVisibility(View.GONE);
            ivSettings.setVisibility(View.GONE);
            ivChart.setVisibility(View.VISIBLE);
            ivCalendar.setVisibility(View.VISIBLE);

            incomeWrapper.setVisibility(View.GONE);
            expenseWrapper.setVisibility(View.GONE);
        } else if (fragment instanceof MoreFragment) {
            balanceWrapper.setVisibility(View.GONE);
            ivSettings.setVisibility(View.VISIBLE);
            ivChart.setVisibility(View.GONE);
            ivCalendar.setVisibility(View.GONE);

            incomeWrapper.setVisibility(View.GONE);
            expenseWrapper.setVisibility(View.GONE);
        }
    }

    private void loadStartUpScreen() {

        int startUpScreen = PreferenceManager.INSTANCE.getStartUpScreen();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentContainer, transactionFragment, "transaction")
                .hide(transactionFragment)
                .add(R.id.fragmentContainer, calendarFragment, "calendar")
                .hide(calendarFragment)
                .add(R.id.fragmentContainer, statisticsFragment, "statistics")
                .hide(statisticsFragment)
                .add(R.id.fragmentContainer, moreFragment, "wallet")
                .hide(moreFragment)
                .commitNow();

        switch (startUpScreen) {

            case IConstants.STARTUP_CALENDAR:
                getSupportFragmentManager()
                        .beginTransaction()
                        .show(calendarFragment)
                        .commit();

                activeFragment = calendarFragment;
                bottomNav.setSelectedItemId(R.id.nav_calendar);
                break;

            case IConstants.STARTUP_STATISTICS:
                getSupportFragmentManager()
                        .beginTransaction()
                        .show(statisticsFragment)
                        .commit();

                activeFragment = statisticsFragment;
                bottomNav.setSelectedItemId(R.id.nav_statistic);
                break;

            case IConstants.STARTUP_MORE:
                getSupportFragmentManager()
                        .beginTransaction()
                        .show(moreFragment)
                        .commit();

                activeFragment = moreFragment;
                bottomNav.setSelectedItemId(R.id.nav_more);
                break;

            case IConstants.STARTUP_TRANSACTION:
            default:
                getSupportFragmentManager()
                        .beginTransaction()
                        .show(transactionFragment)
                        .commit();

                activeFragment = transactionFragment;
                bottomNav.setSelectedItemId(R.id.nav_transaction);
                break;
        }

        if (activeFragment instanceof ToolbarActionListener) {
            toolbarActionListener = (ToolbarActionListener) activeFragment;
        } else {
            toolbarActionListener = null;
        }

        updateToolbar(activeFragment);
    }

    public interface ToolbarActionListener {
        void onChartClicked();
        void onCalendarClicked();
    }
}