package com.nprotech.moneytracker.ui.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.enums.CalendarFilterType;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.CalendarFilterModel;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.utils.IntentUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateBudgetActivity extends BaseActivity {
    private AppCompatImageView icBack, ivSelectedPeriod;
    private AppCompatTextView tvSave, tvBudgetPeriod, selectedPeriodLabel, tvSelectBudgetPeriod, tvBudgetWallet, tvBudgetCategory,
            tvBudgetMethod, tvBudgetAmount, tvBudgetAlert, maxLimitLabel, lblBudgetAmountTips;
    private AppCompatEditText etBudgetName;
    private MaterialCardView cardBudgetPeriod, cardSelectedPeriod, cardBudgetWallet, cardBudgetCategory, cardBudgetMethod, cardBudgetAlert, cardBudgetAmount;
    private SwitchCompat switchAutoView;
    private boolean isEdit = false;
    private int budgetId = 0;
    private String budgetName, tempBudgetPeriod, budgetPeriod, budgetMethod, tempBudgetMethod;
    private long periodStartDate, periodEndDate;
    private final Set<Integer> selectedCategoryIds = new HashSet<>();
    private double budgetAmount = 0.0;
    private int budgetMethodId = 0, budgetPeriodId = 0, alertPercentage = 80;
    private Typeface medium, semibold;
    private List<WalletEntity> walletLists;
    private final Set<Integer> selectedWalletIds = new HashSet<>();
    private AccountViewModel accountViewModel;
    private ActivityResultLauncher<Intent> calculatorLauncher, categoryLauncher, categoryAmountLauncher;
    private AccountEntity account;
    private final Map<Integer, Double> categoryAmounts = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_budget);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            View rootView = findViewById(R.id.rootView);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            tvSave = toolbarWrapper.findViewById(R.id.tvSave);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            etBudgetName = findViewById(R.id.etBudgetName);
            maxLimitLabel = findViewById(R.id.maxLimitLabel);
            tvBudgetPeriod = findViewById(R.id.tvBudgetPeriod);
            ivSelectedPeriod = findViewById(R.id.ivSelectedPeriod);
            selectedPeriodLabel = findViewById(R.id.selectedPeriodLabel);
            tvSelectBudgetPeriod = findViewById(R.id.tvSelectBudgetPeriod);
            tvBudgetWallet = findViewById(R.id.tvBudgetWallet);
            tvBudgetCategory = findViewById(R.id.tvBudgetCategory);
            tvBudgetMethod = findViewById(R.id.tvBudgetMethod);
            tvBudgetAmount = findViewById(R.id.tvBudgetAmount);
            tvBudgetAlert = findViewById(R.id.tvBudgetAlert);
            cardBudgetPeriod = findViewById(R.id.cardBudgetPeriod);
            cardSelectedPeriod = findViewById(R.id.cardSelectedPeriod);
            cardBudgetWallet = findViewById(R.id.cardBudgetWallet);
            cardBudgetCategory = findViewById(R.id.cardBudgetCategory);
            cardBudgetMethod = findViewById(R.id.cardBudgetMethod);
            cardBudgetAmount = findViewById(R.id.cardBudgetAmount);
            cardBudgetAlert = findViewById(R.id.cardBudgetAlert);
            switchAutoView = findViewById(R.id.switchAutoView);
            lblBudgetAmountTips = findViewById(R.id.lblBudgetAmountTips);

            tvSave.setVisibility(View.VISIBLE);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

            medium = ResourcesCompat.getFont(this, R.font.exo2_medium);
            semibold = ResourcesCompat.getFont(this, R.font.exo2_semibold);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {

                isEdit = bundle.getBoolean("isEdit", false);
                budgetId = bundle.getInt("budgetId", 0);

                if (isEdit) {
                    tvTitle.setText(getString(R.string.edit_budget));
                } else {
                    tvTitle.setText(getString(R.string.add_budget));
                }

                bindData();
                setupLaunchers();
                setupListeners();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {

            walletLists = accountViewModel.getWalletsByAccountId((int) PreferenceManager.INSTANCE.getAccountId());
            account = accountViewModel.getAccountDetailById((int) PreferenceManager.INSTANCE.getAccountId());

            if (isEdit) {
                tvSave.setText(getString(R.string.update));
            } else {
                tvSave.setText(getString(R.string.save));

                budgetName = "";
                budgetPeriod = Constants.PERIOD_MONTHLY;
                budgetPeriodId = 2;
                budgetMethod = Constants.METHOD_SHARED;
                budgetMethodId = 1;
                selectedCategoryIds.clear();
                selectedWalletIds.clear();
                budgetAmount = 0.0;
                alertPercentage = 80;
                maxLimitLabel.setText(getString(R.string.character_limit, 0));

                if (walletLists != null) {
                    for (WalletEntity wallet : walletLists) {
                        selectedWalletIds.add(wallet.id);
                    }
                    updateWalletTexts();
                }

                updateCategoryTexts();
                initializeCurrentMonth();
                updateBudgetPeriod();
                updateSelectedPeriod();
                updateBudgetMethod();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeCurrentMonth() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            periodStartDate = calendar.getTimeInMillis();

            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.MILLISECOND, -1);
            periodEndDate = calendar.getTimeInMillis();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeCurrentMonth", e);
        }
    }

    private void updateBudgetPeriod() {
        try {
            switch (budgetPeriod) {
                case Constants.PERIOD_WEEKLY:
                    tvBudgetPeriod.setText(getString(R.string.calendar_weekly));
                    budgetPeriodId = 1;
                    break;
                case Constants.PERIOD_MONTHLY:
                    tvBudgetPeriod.setText(getString(R.string.calendar_monthly));
                    budgetPeriodId = 2;
                    break;
                case Constants.PERIOD_QUARTERLY:
                    tvBudgetPeriod.setText(getString(R.string.calendar_quarterly));
                    budgetPeriodId = 3;
                    break;
                case Constants.PERIOD_YEARLY:
                    tvBudgetPeriod.setText(getString(R.string.calendar_yearly));
                    budgetPeriodId = 4;
                    break;
                case Constants.PERIOD_CUSTOM:
                    tvBudgetPeriod.setText(getString(R.string.calendar_custom));
                    budgetPeriodId = 5;
                    break;
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "updateBudgetPeriod", e);
        }
    }

    private void updateSelectedPeriod() {

        String text;
        String label;
        int icon;
        switch (budgetPeriod) {
            case Constants.PERIOD_WEEKLY:
                text = DateHelper.formatDateRange(periodStartDate, periodEndDate);
                label = getString(R.string.calendar_weekly);
                icon = R.drawable.ic_calendar_weekly;
                break;
            case Constants.PERIOD_QUARTERLY:
                text = DateHelper.getQuarterText(periodStartDate) + " (" + DateHelper.getFormattedDate(periodStartDate, "MMM yyyy") +
                        " - " + DateHelper.getFormattedDate(periodEndDate, "MMM yyyy") + ")";
                label = getString(R.string.calendar_quarterly);
                icon = R.drawable.ic_quarterly_outline;
                break;
            case Constants.PERIOD_YEARLY:
                text = DateHelper.formatYear(periodStartDate);
                label = getString(R.string.calendar_yearly);
                icon = R.drawable.ic_yearly;
                break;
            case Constants.PERIOD_CUSTOM:
                text = DateHelper.formatDateRange(periodStartDate, periodEndDate);
                label = getString(R.string.calendar_custom);
                icon = R.drawable.ic_calendar_custom;
                break;
            default:
                text = DateHelper.formatMonthYear(periodStartDate);
                label = getString(R.string.calendar_monthly);
                icon = R.drawable.ic_calendar_monthly;
        }

        selectedPeriodLabel.setText(label);
        tvSelectBudgetPeriod.setText(text);
        ivSelectedPeriod.setImageDrawable(ContextCompat.getDrawable(this, icon));
    }

    private void updateWalletTexts() {
        try {
            if (selectedWalletIds != null) {
                if (selectedWalletIds.size() == walletLists.size()) {
                    tvBudgetWallet.setText(getString(R.string.all_wallets));
                } else if (selectedWalletIds.size() == 1) {
                    for (WalletEntity wallet : walletLists) {
                        if (selectedWalletIds.contains(wallet.id)) {
                            tvBudgetWallet.setText(getString(R.string.wallet_info, wallet.name,
                                    CommonUtils.getBeautifyAmount(wallet.currencySymbol, wallet.amount)));
                        }
                    }
                } else {
                    tvBudgetWallet.setText(getString(R.string.more_wallets_selected, selectedWalletIds.size()));
                }
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "updateWalletTexts", e);
        }
    }

    private void updateCategoryTexts() {
        try {
            if (selectedCategoryIds != null) {
                int count = selectedCategoryIds.size();
                tvBudgetCategory.setText(getResources().getQuantityString(R.plurals.category_selected_count, count, count));
            }

            updateAmountTexts();
        } catch (Exception e) {
            AppLogger.e(getClass(), "updateCategoryTexts", e);
        }
    }

    private void updateBudgetMethod() {
        try {
            if (budgetMethod.equalsIgnoreCase(Constants.METHOD_SHARED)) {
                budgetMethodId = 1;
                tvBudgetMethod.setText(getString(R.string.one_amount_budget));
            } else {
                budgetMethodId = 2;
                tvBudgetMethod.setText(getString(R.string.separate_method));
            }

            updateAmountTexts();
        } catch (Exception e) {
            AppLogger.e(getClass(), "updateBudgetMethod", e);
        }
    }

    private void updateAmountTexts() {
        try {
            cardBudgetAmount.setVisibility(View.VISIBLE);
            tvBudgetAmount.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, budgetAmount));

            if (budgetMethod.equals(Constants.METHOD_SHARED)) {
                lblBudgetAmountTips.setVisibility(View.VISIBLE);
                int count = selectedCategoryIds.size();
                lblBudgetAmountTips.setText(getResources().getQuantityString(R.plurals.budget_amount_tips_count, count, count));
            } else {
                lblBudgetAmountTips.setText("");
                lblBudgetAmountTips.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "updateAmountTexts", e);
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finishWithTransitions();
            });

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finishWithTransitions();
                }
            });

            etBudgetName.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    maxLimitLabel.setText(getString(R.string.character_limit, charSequence.length()));
                    updateSaveButtonState();
                }
            });

            // PERIOD
            cardBudgetPeriod.setOnClickListener(v -> showBudgetPeriod());

            tvBudgetPeriod.setOnClickListener(v -> showBudgetPeriod());

            cardSelectedPeriod.setOnClickListener(v -> {
                tempBudgetPeriod = budgetPeriod;
                showSelectedPeriod();
            });

            tvSelectBudgetPeriod.setOnClickListener(v -> {
                tempBudgetPeriod = budgetPeriod;
                showSelectedPeriod();
            });

            // WALLET
            cardBudgetWallet.setOnClickListener(v -> selectWallets());

            tvBudgetWallet.setOnClickListener(v -> selectWallets());

            // CATEGORY
            cardBudgetCategory.setOnClickListener(v -> {
                hideKeyboard(this);
                Intent intent = new Intent(this, CategoryPickerActivity.class);
                intent.putExtra("transactionType", TransactionEntity.TYPE_EXPENSE);
                intent.putExtra("isFromScreen", "budget");
                intent.putIntegerArrayListExtra("categoryIds", new ArrayList<>(selectedCategoryIds));

                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                categoryLauncher.launch(intent, options);
            });

            tvBudgetCategory.setOnClickListener(v -> {
                hideKeyboard(this);
                Intent intent = new Intent(this, CategoryPickerActivity.class);
                intent.putExtra("transactionType", TransactionEntity.TYPE_EXPENSE);
                intent.putExtra("isFromScreen", "budget");
                intent.putIntegerArrayListExtra("categoryIds", new ArrayList<>(selectedCategoryIds));

                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                categoryLauncher.launch(intent, options);
            });

            // METHOD
            cardBudgetMethod.setOnClickListener(v -> selectBudgetMethod());

            tvBudgetMethod.setOnClickListener(v -> selectBudgetMethod());

            // AMOUNT
            cardBudgetAmount.setOnClickListener(v -> {
                hideKeyboard(this);

                if (Objects.equals(budgetMethod, Constants.METHOD_SEPARATE)) {

                    if(selectedCategoryIds != null && !selectedCategoryIds.isEmpty()) {
                        hideKeyboard(this);
                        Intent intent = new Intent(this, CategoryAmountActivity.class);
                        intent.putExtra("currencySymbol", account.currencySymbol);
                        intent.putIntegerArrayListExtra("categoryIds", new ArrayList<>(selectedCategoryIds));
                        intent.putExtra("categoryAmounts", new HashMap<>(categoryAmounts));
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                        categoryAmountLauncher.launch(intent, options);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.select_category_before_proceeding), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent intent = new Intent(this, CalculatorActivity.class);
                    intent.putExtra("amount", budgetAmount);
                    intent.putExtra("type", "amount");
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                    calculatorLauncher.launch(intent, options);
                }
            });

            tvBudgetAmount.setOnClickListener(v -> {
                hideKeyboard(this);

                if (Objects.equals(budgetMethod, Constants.METHOD_SEPARATE)) {
                    if(selectedCategoryIds != null && !selectedCategoryIds.isEmpty()) {
                        hideKeyboard(this);
                        Intent intent = new Intent(this, CategoryAmountActivity.class);
                        intent.putExtra("currencySymbol", account.currencySymbol);
                        intent.putIntegerArrayListExtra("categoryIds", new ArrayList<>(selectedCategoryIds));
                        intent.putExtra("categoryAmounts", new HashMap<>(categoryAmounts));
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                        categoryAmountLauncher.launch(intent, options);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.select_category_before_proceeding), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent intent = new Intent(this, CalculatorActivity.class);
                    intent.putExtra("amount", budgetAmount);
                    intent.putExtra("type", "amount");
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                    calculatorLauncher.launch(intent, options);
                }
            });

            // ALERT
            cardBudgetAlert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            tvBudgetAlert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            // SAVE
            tvSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void setupLaunchers() {
        calculatorLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            double amount = data.getDoubleExtra("amount", 0);
                            String type = data.getStringExtra("type");

                            if (type != null && type.equalsIgnoreCase("amount")) {
                                budgetAmount = amount;
                                updateAmountTexts();
                            }
                        }
                    }
                });

        categoryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            ArrayList<Integer> categoryIds = data.getIntegerArrayListExtra("categoryIds");
                            if (categoryIds != null) {
                                selectedCategoryIds.clear();
                                selectedCategoryIds.addAll(categoryIds);

                                updateCategoryTexts();
                            }
                        }
                    }
                });

        categoryAmountLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            ArrayList<Integer> categoryIds = data.getIntegerArrayListExtra("categoryIds");

                            @SuppressWarnings("unchecked")
                            HashMap<Integer, Double> amounts = (HashMap<Integer, Double>) IntentUtils.getSerializableExtra(data, "categoryAmounts", HashMap.class);

                            if (categoryIds != null) {
                                selectedCategoryIds.clear();
                                selectedCategoryIds.addAll(categoryIds);
                            }

                            categoryAmounts.clear();

                            if (amounts != null) {
                                categoryAmounts.putAll(amounts);
                            }

                            budgetAmount = 0.0;
                            for (Double amount : categoryAmounts.values()) {
                                if (amount != null) {
                                    budgetAmount += amount;
                                }
                            }

                            updateCategoryTexts();
                        }
                    }
                });
    }

    private void showBudgetPeriod() {
        try {

            tempBudgetPeriod = budgetPeriod;

            List<CalendarFilterModel> calendarFilterModelList = new ArrayList<>();
            calendarFilterModelList.add(new CalendarFilterModel(CalendarFilterType.WEEKLY, 1, R.drawable.ic_calendar_weekly, getString(R.string.calendar_weekly), false));
            calendarFilterModelList.add(new CalendarFilterModel(CalendarFilterType.MONTHLY, 2, R.drawable.ic_calendar_monthly, getString(R.string.calendar_monthly), false));
            calendarFilterModelList.add(new CalendarFilterModel(CalendarFilterType.QUARTERLY, 3, R.drawable.ic_quarterly, getString(R.string.calendar_quarterly), false));
            calendarFilterModelList.add(new CalendarFilterModel(CalendarFilterType.YEARLY, 4, R.drawable.ic_yearly, getString(R.string.calendar_yearly), false));
            calendarFilterModelList.add(new CalendarFilterModel(CalendarFilterType.CUSTOM, 5, R.drawable.ic_calendar_custom, getString(R.string.calendar_custom), false));

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_calendar_filter_layout, findViewById(android.R.id.content), false);
            RecyclerView rvSelectRange = bottomView.findViewById(R.id.rvSelectRange);
            AppCompatTextView tvSelectRange = bottomView.findViewById(R.id.tvSelectRange);
            tvSelectRange.setText(getString(R.string.select_period));

            RecyclerViewAdapter<CalendarFilterModel> adapter = new RecyclerViewAdapter<>(this, calendarFilterModelList, R.layout.item_calendar_filter) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, CalendarFilterModel calendarFilter) {

                    holder.setViewText(R.id.tvFilterName, calendarFilter.filterName);
                    holder.setViewImageDrawable(R.id.ivIcon, ContextCompat.getDrawable(getApplicationContext(), calendarFilter.icon));

                    boolean isSelected = isBudgetPeriodSelected(calendarFilter.type);

                    holder.setViewVisibility(R.id.ivSelected, isSelected ? View.VISIBLE : View.GONE);
                    holder.setViewTypeface(R.id.tvFilterName, isSelected ? semibold : medium);

                    holder.getView(R.id.rlFilterView).setOnClickListener(v -> {
                        tempBudgetPeriod = getBudgetPeriodValue(calendarFilter.type);
                        dialog.dismiss();
                        showSelectedPeriod();
                    });
                }
            };

            rvSelectRange.setAdapter(adapter);
            rvSelectRange.setLayoutManager(new LinearLayoutManager(this));
            rvSelectRange.setItemAnimator(null);
            rvSelectRange.setHasFixedSize(true);

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showBudgetPeriod", e);
        }
    }

    private boolean isBudgetPeriodSelected(CalendarFilterType type) {
        return switch (type) {
            case WEEKLY -> Constants.PERIOD_WEEKLY.equals(budgetPeriod);
            case MONTHLY -> Constants.PERIOD_MONTHLY.equals(budgetPeriod);
            case QUARTERLY -> Constants.PERIOD_QUARTERLY.equals(budgetPeriod);
            case YEARLY -> Constants.PERIOD_YEARLY.equals(budgetPeriod);
            case CUSTOM -> Constants.PERIOD_CUSTOM.equals(budgetPeriod);
            default -> false;
        };
    }

    private String getBudgetPeriodValue(CalendarFilterType type) {
        return switch (type) {
            case WEEKLY -> Constants.PERIOD_WEEKLY;
            case QUARTERLY -> Constants.PERIOD_QUARTERLY;
            case YEARLY -> Constants.PERIOD_YEARLY;
            case CUSTOM -> Constants.PERIOD_CUSTOM;
            default -> Constants.PERIOD_MONTHLY;
        };
    }

    private void showSelectedPeriod() {
        try {
            switch (tempBudgetPeriod) {
                case Constants.PERIOD_WEEKLY:
                    showWeekPicker();
                    break;
                case Constants.PERIOD_MONTHLY:
                    showMonthPicker();
                    break;
                case Constants.PERIOD_QUARTERLY:
                    showQuarterPicker();
                    break;
                case Constants.PERIOD_YEARLY:
                    showYearPicker();
                    break;
                case Constants.PERIOD_CUSTOM:
                    showCustomDatePicker();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "showSelectedPeriod", e);
        }
    }

    private BottomSheetDialog createPeriodBottomSheet(String title) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View bottomView = getLayoutInflater().inflate(R.layout.bottom_budget_period, findViewById(android.R.id.content), false);
        AppCompatTextView tvTitle = bottomView.findViewById(R.id.tvTitle);
        AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);
        tvTitle.setText(title);

        tvClose.setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(bottomView);
        return dialog;
    }

    private void updateSaveButtonState() {
        boolean enabled = budgetAmount > 0 && budgetPeriodId > 0 && budgetMethodId > 0
                && selectedWalletIds != null && selectedCategoryIds != null;

        enabled &= !Objects.requireNonNull(etBudgetName.getText()).toString().isEmpty();

        if (switchAutoView.isChecked()) {

        }

        tvSave.setEnabled(enabled);
        enabledSaveOption(enabled);
    }

    private void enabledSaveOption(boolean isEnabled) {
        try {
            tvSave.setAlpha(isEnabled ? 1.0f : 0.5f); // Optional: make disabled state visible
        } catch (Exception e) {
            AppLogger.e(getClass(), "enabledSaveOption", e);
        }
    }

    // -------------------
    // ----- WEEKLY -----
    // -------------------
    private void showWeekPicker() {
        try {
            BottomSheetDialog dialog = createPeriodBottomSheet(getString(R.string.select_week));
            FrameLayout container = dialog.findViewById(R.id.pickerContainer);
            AppCompatTextView btnDone = dialog.findViewById(R.id.btnDone);
            AppCompatTextView tvStartDate = dialog.findViewById(R.id.tvStartDate);
            AppCompatTextView tvEndDate = dialog.findViewById(R.id.tvEndDate);

            if (container == null || btnDone == null || tvStartDate == null || tvEndDate == null) {
                return;
            }

            container.setVisibility(View.VISIBLE);

            Calendar calendar = Calendar.getInstance();

            // Current week
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

            Calendar selectedStart = (Calendar) calendar.clone();
            Calendar selectedEnd = (Calendar) calendar.clone();

            selectedEnd.add(Calendar.DAY_OF_MONTH, 6);
            final int[] selectedYear = {selectedStart.get(Calendar.YEAR)};
            final int[] selectedMonth = {selectedStart.get(Calendar.MONTH)};

            final int[] selectedWeek = {
                    DateHelper.getWeekOfMonth(selectedStart)
            };

            // -------------------------
            // Month navigation
            // -------------------------
            View monthView = getLayoutInflater().inflate(R.layout.item_budget_period_navigation, container, false);
            AppCompatTextView tvMonth = monthView.findViewById(R.id.tvValue);
            AppCompatImageView ivPreviousMonth = monthView.findViewById(R.id.ivPrevious);
            AppCompatImageView ivNextMonth = monthView.findViewById(R.id.ivNext);

            // -------------------------
            // Week navigation
            // -------------------------
            View weekView = getLayoutInflater().inflate(R.layout.item_budget_period_navigation, container, false);
            AppCompatTextView tvWeek = weekView.findViewById(R.id.tvValue);
            AppCompatImageView ivPreviousWeek = weekView.findViewById(R.id.ivPrevious);
            AppCompatImageView ivNextWeek = weekView.findViewById(R.id.ivNext);
            container.addView(monthView);
            LinearLayout.LayoutParams weekParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            weekParams.topMargin = getResources().getDimensionPixelSize(R.dimen.margin_40);
            container.addView(weekView, weekParams);

            // -------------------------
            // Update UI
            // -------------------------

            Runnable updateUi = () -> {
                Calendar start = Calendar.getInstance();
                start.set(selectedYear[0], selectedMonth[0], 1, 0, 0, 0);
                start.set(Calendar.DAY_OF_MONTH, DateHelper.getFirstMondayDate(selectedYear[0], selectedMonth[0]));
                start.add(Calendar.WEEK_OF_MONTH, selectedWeek[0] - 1);
                Calendar end = (Calendar) start.clone();
                end.add(Calendar.DAY_OF_MONTH, 6);
                periodStartDate = start.getTimeInMillis();
                periodEndDate = end.getTimeInMillis();
                tvMonth.setText(DateHelper.formatMonthYear(start.getTimeInMillis()));
                tvWeek.setText(getString(R.string.week_number, selectedWeek[0]));
                tvStartDate.setText(DateHelper.getFormattedDate(periodStartDate));
                tvEndDate.setText(DateHelper.getFormattedDate(periodEndDate));
            };

            // Initial UI
            updateUi.run();

            // -------------------------
            // Previous Month
            // -------------------------
            ivPreviousMonth.setOnClickListener(v -> {
                selectedMonth[0]--;
                if (selectedMonth[0] < Calendar.JANUARY) {
                    selectedMonth[0] = Calendar.DECEMBER;
                    selectedYear[0]--;
                }
                selectedWeek[0] = 1;
                updateUi.run();
            });

            // -------------------------
            // Next Month
            // -------------------------
            ivNextMonth.setOnClickListener(v -> {
                selectedMonth[0]++;
                if (selectedMonth[0] > Calendar.DECEMBER) {
                    selectedMonth[0] = Calendar.JANUARY;
                    selectedYear[0]++;
                }
                selectedWeek[0] = 1;
                updateUi.run();
            });

            // -------------------------
            // Previous Week
            // -------------------------
            ivPreviousWeek.setOnClickListener(v -> {
                selectedWeek[0]--;

                if (selectedWeek[0] < 1) {
                    selectedMonth[0]--;
                    if (selectedMonth[0] < Calendar.JANUARY) {
                        selectedMonth[0] = Calendar.DECEMBER;
                        selectedYear[0]--;
                    }
                    selectedWeek[0] = DateHelper.getWeeksInMonth(selectedYear[0], selectedMonth[0]);
                }
                updateUi.run();
            });

            // -------------------------
            // Next Week
            // -------------------------
            ivNextWeek.setOnClickListener(v -> {
                int maxWeeks = DateHelper.getWeeksInMonth(selectedYear[0], selectedMonth[0]);
                selectedWeek[0]++;

                if (selectedWeek[0] > maxWeeks) {
                    selectedMonth[0]++;
                    if (selectedMonth[0] > Calendar.DECEMBER) {
                        selectedMonth[0] = Calendar.JANUARY;
                        selectedYear[0]++;
                    }
                    selectedWeek[0] = 1;
                }
                updateUi.run();
            });

            // -------------------------
            // Done
            // -------------------------
            btnDone.setOnClickListener(v -> {
                budgetPeriod = tempBudgetPeriod;
                updateBudgetPeriod();
                updateSelectedPeriod();
                dialog.dismiss();
            });

            dialog.show();

        } catch (Exception e) {
            AppLogger.e(getClass(), "showWeekPicker", e);
        }
    }

    // -------------------
    // ----- MONTHLY -----
    // -------------------
    private void showMonthPicker() {
        try {
            BottomSheetDialog dialog = createPeriodBottomSheet(getString(R.string.select_month));
            FrameLayout container = dialog.findViewById(R.id.pickerContainer);
            AppCompatTextView btnDone = dialog.findViewById(R.id.btnDone);
            AppCompatTextView tvStartDate = dialog.findViewById(R.id.tvStartDate);
            AppCompatTextView tvEndDate = dialog.findViewById(R.id.tvEndDate);

            if (container == null || btnDone == null || tvStartDate == null || tvEndDate == null) {
                return;
            }

            container.setVisibility(View.VISIBLE);

            Calendar selected = Calendar.getInstance();

            View navigation = getLayoutInflater().inflate(R.layout.item_budget_period_navigation, container, false);
            AppCompatTextView tvValue = navigation.findViewById(R.id.tvValue);
            AppCompatImageView ivPrevious = navigation.findViewById(R.id.ivPrevious);
            AppCompatImageView ivNext = navigation.findViewById(R.id.ivNext);
            container.addView(navigation);

            Runnable updateUi = () -> {
                int year = selected.get(Calendar.YEAR);
                int month = selected.get(Calendar.MONTH);
                setMonthlyPeriod(year, month);
                tvValue.setText(DateHelper.formatMonthYear(periodStartDate));

                tvStartDate.setText(DateHelper.getFormattedDate(periodStartDate));
                tvEndDate.setText(DateHelper.getFormattedDate(periodEndDate));
            };

            updateUi.run();

            ivPrevious.setOnClickListener(v -> {
                selected.add(Calendar.MONTH, -1);
                updateUi.run();
            });

            ivNext.setOnClickListener(v -> {
                selected.add(Calendar.MONTH, 1);
                updateUi.run();
            });

            btnDone.setOnClickListener(v -> {
                budgetPeriod = tempBudgetPeriod;
                updateBudgetPeriod();
                updateSelectedPeriod();
                dialog.dismiss();
            });

            dialog.show();

        } catch (Exception e) {
            AppLogger.e(getClass(), "showMonthPicker", e);
        }
    }

    private void setMonthlyPeriod(int year, int month) {
        try {
            Calendar start = Calendar.getInstance();
            start.set(year, month, 1, 0, 0, 0);
            start.set(Calendar.MILLISECOND, 0);

            Calendar end = (Calendar) start.clone();
            end.add(Calendar.MONTH, 1);
            end.add(Calendar.MILLISECOND, -1);

            periodStartDate = start.getTimeInMillis();
            periodEndDate = end.getTimeInMillis();
        } catch (Exception e) {
            AppLogger.e(getClass(), "setMonthlyPeriod", e);
        }
    }

    // -------------------
    // ---- QUARTERLY ----
    // -------------------
    private void showQuarterPicker() {
        try {
            BottomSheetDialog dialog = createPeriodBottomSheet(getString(R.string.select_quarter));
            FrameLayout container = dialog.findViewById(R.id.pickerContainer);
            AppCompatTextView btnDone = dialog.findViewById(R.id.btnDone);
            AppCompatTextView tvStartDate = dialog.findViewById(R.id.tvStartDate);
            AppCompatTextView tvEndDate = dialog.findViewById(R.id.tvEndDate);

            if (container == null || btnDone == null || tvStartDate == null || tvEndDate == null) {
                return;
            }

            container.setVisibility(View.VISIBLE);

            Calendar current = Calendar.getInstance();

            final int[] selectedYear = {
                    current.get(Calendar.YEAR)
            };

            final int[] selectedQuarter = {
                    (current.get(Calendar.MONTH) / 3) + 1
            };

            // -------------------------
            // Year navigation
            // -------------------------
            View yearView = getLayoutInflater().inflate(R.layout.item_budget_period_navigation, container, false);
            AppCompatTextView tvYear = yearView.findViewById(R.id.tvValue);
            AppCompatImageView ivPreviousYear = yearView.findViewById(R.id.ivPrevious);
            AppCompatImageView ivNextYear = yearView.findViewById(R.id.ivNext);
            container.addView(yearView);

            // -------------------------
            // Quarter navigation
            // -------------------------
            View quarterView = getLayoutInflater().inflate(R.layout.item_budget_period_navigation, container, false);
            AppCompatTextView tvQuarter = quarterView.findViewById(R.id.tvValue);
            AppCompatImageView ivPreviousQuarter = quarterView.findViewById(R.id.ivPrevious);
            AppCompatImageView ivNextQuarter = quarterView.findViewById(R.id.ivNext);
            FrameLayout.LayoutParams quarterParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            quarterParams.topMargin = getResources().getDimensionPixelSize(R.dimen.margin_40);
            container.addView(quarterView, quarterParams);

            // -------------------------
            // Update UI
            // -------------------------
            Runnable updateUi = () -> {
                setQuarterlyPeriod(selectedYear[0], selectedQuarter[0]);
                tvYear.setText(String.valueOf(selectedYear[0]));
                tvQuarter.setText(getString(R.string.quarter_number, selectedQuarter[0]));
                tvStartDate.setText(DateHelper.getFormattedDate(periodStartDate));
                tvEndDate.setText(DateHelper.getFormattedDate(periodEndDate));
            };

            updateUi.run();

            // -------------------------
            // Previous Year
            // -------------------------
            ivPreviousYear.setOnClickListener(v -> {
                selectedYear[0]--;
                updateUi.run();
            });

            // -------------------------
            // Next Year
            // -------------------------
            ivNextYear.setOnClickListener(v -> {
                selectedYear[0]++;
                updateUi.run();
            });

            // -------------------------
            // Previous Quarter
            // -------------------------
            ivPreviousQuarter.setOnClickListener(v -> {
                selectedQuarter[0]--;
                if (selectedQuarter[0] < 1) {
                    selectedQuarter[0] = 4;
                    selectedYear[0]--;
                }
                updateUi.run();
            });

            // -------------------------
            // Next Quarter
            // -------------------------
            ivNextQuarter.setOnClickListener(v -> {
                selectedQuarter[0]++;
                if (selectedQuarter[0] > 4) {
                    selectedQuarter[0] = 1;
                    selectedYear[0]++;
                }
                updateUi.run();
            });

            // -------------------------
            // Done
            // -------------------------
            btnDone.setOnClickListener(v -> {
                budgetPeriod = tempBudgetPeriod;
                updateBudgetPeriod();
                updateSelectedPeriod();
                dialog.dismiss();
            });

            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showQuarterPicker", e);
        }
    }

    private void setQuarterlyPeriod(int year, int quarter) {
        try {
            int startMonth;
            switch (quarter) {
                case 1:
                    startMonth = Calendar.JANUARY;
                    break;
                case 2:
                    startMonth = Calendar.APRIL;
                    break;
                case 3:
                    startMonth = Calendar.JULY;
                    break;
                case 4:
                    startMonth = Calendar.OCTOBER;
                    break;
                default:
                    return;
            }

            Calendar start = Calendar.getInstance();
            start.set(year, startMonth, 1, 0, 0, 0);
            start.set(Calendar.MILLISECOND, 0);

            Calendar end = (Calendar) start.clone();
            end.add(Calendar.MONTH, 3);
            end.add(Calendar.MILLISECOND, -1);
            periodStartDate = start.getTimeInMillis();
            periodEndDate = end.getTimeInMillis();
        } catch (Exception e) {
            AppLogger.e(getClass(), "setQuarterlyPeriod", e);
        }
    }

    // -------------------
    // ------ YEARLY -----
    // -------------------
    private void showYearPicker() {
        try {
            BottomSheetDialog dialog = createPeriodBottomSheet(getString(R.string.select_year));
            FrameLayout container = dialog.findViewById(R.id.pickerContainer);
            AppCompatTextView btnDone = dialog.findViewById(R.id.btnDone);
            AppCompatTextView tvStartDate = dialog.findViewById(R.id.tvStartDate);
            AppCompatTextView tvEndDate = dialog.findViewById(R.id.tvEndDate);

            if (container == null || btnDone == null || tvStartDate == null || tvEndDate == null) {
                return;
            }

            container.setVisibility(View.VISIBLE);

            Calendar current = Calendar.getInstance();
            final int[] selectedYear = {
                    current.get(Calendar.YEAR)
            };

            // -------------------------
            // Year navigation
            // -------------------------
            View yearView = getLayoutInflater().inflate(R.layout.item_budget_period_navigation, container, false);
            AppCompatTextView tvYear = yearView.findViewById(R.id.tvValue);
            AppCompatImageView ivPrevious = yearView.findViewById(R.id.ivPrevious);
            AppCompatImageView ivNext = yearView.findViewById(R.id.ivNext);
            container.addView(yearView);

            // -------------------------
            // Update UI
            // -------------------------
            Runnable updateUi = () -> {
                setYearlyPeriod(selectedYear[0]);
                tvYear.setText(String.valueOf(selectedYear[0]));
                tvStartDate.setText(DateHelper.getFormattedDate(periodStartDate));
                tvEndDate.setText(DateHelper.getFormattedDate(periodEndDate));
            };
            updateUi.run();

            // -------------------------
            // Previous Year
            // -------------------------
            ivPrevious.setOnClickListener(v -> {
                selectedYear[0]--;
                updateUi.run();
            });

            // -------------------------
            // Next Year
            // -------------------------
            ivNext.setOnClickListener(v -> {
                selectedYear[0]++;
                updateUi.run();
            });

            // -------------------------
            // Done
            // -------------------------
            btnDone.setOnClickListener(v -> {
                budgetPeriod = tempBudgetPeriod;
                updateBudgetPeriod();
                updateSelectedPeriod();
                dialog.dismiss();
            });

            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showYearPicker", e);
        }
    }

    private void setYearlyPeriod(int year) {
        try {
            Calendar start = Calendar.getInstance();
            start.set(year, Calendar.JANUARY, 1, 0, 0, 0);
            start.set(Calendar.MILLISECOND, 0);

            Calendar end = Calendar.getInstance();
            end.set(year, Calendar.DECEMBER, 31, 23, 59, 59);
            end.set(Calendar.MILLISECOND, 999);
            periodStartDate = start.getTimeInMillis();
            periodEndDate = end.getTimeInMillis();
        } catch (Exception e) {
            AppLogger.e(getClass(), "setYearlyPeriod", e);
        }
    }

    // -------------------
    // ------ CUSTOM -----
    // -------------------
    private void showCustomDatePicker() {
        try {
            BottomSheetDialog dialog = createPeriodBottomSheet(getString(R.string.select_custom_period));
            FrameLayout container = dialog.findViewById(R.id.pickerContainer);
            AppCompatTextView tvStartDate = dialog.findViewById(R.id.tvStartDate);
            AppCompatTextView tvEndDate = dialog.findViewById(R.id.tvEndDate);
            AppCompatTextView btnDone = dialog.findViewById(R.id.btnDone);

            if (container == null || tvStartDate == null || tvEndDate == null || btnDone == null) {
                return;
            }

            container.setVisibility(View.GONE);

            tvStartDate.setText(DateHelper.getFormattedDate(periodStartDate));
            tvEndDate.setText(DateHelper.getFormattedDate(periodEndDate));

            tvStartDate.setOnClickListener(v -> showCustomDatePickerDialog(true, tvStartDate, tvEndDate));
            tvEndDate.setOnClickListener(v -> showCustomDatePickerDialog(false, tvStartDate, tvEndDate));

            btnDone.setOnClickListener(v -> {
                budgetPeriod = tempBudgetPeriod;
                updateBudgetPeriod();
                updateSelectedPeriod();
                dialog.dismiss();
            });

            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showCustomDatePicker", e);
        }
    }

    private void showCustomDatePickerDialog(boolean isStartDate, AppCompatTextView tvStartDate, AppCompatTextView tvEndDate) {
        try {
            Calendar selected = Calendar.getInstance();

            long currentDate = isStartDate ? periodStartDate : periodEndDate;

            if (currentDate > 0) {
                selected.setTimeInMillis(currentDate);
            }

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.CustomDateTimePickerDialog, (view, year,
                                                                                                                month, dayOfMonth) -> {
                Calendar result = Calendar.getInstance();
                if (isStartDate) {
                    result.set(year, month, dayOfMonth, 0, 0, 0);
                    result.set(Calendar.MILLISECOND, 0);
                    periodStartDate = result.getTimeInMillis();
                    if (periodEndDate < periodStartDate) {
                        periodEndDate = getEndOfDay(periodStartDate);
                    }
                } else {
                    result.set(year, month, dayOfMonth, 23, 59, 59);
                    result.set(Calendar.MILLISECOND, 999);
                    periodEndDate = result.getTimeInMillis();
                }

                tvStartDate.setText(DateHelper.getFormattedDate(periodStartDate));
                tvEndDate.setText(DateHelper.getFormattedDate(periodEndDate));
            }, selected.get(Calendar.YEAR), selected.get(Calendar.MONTH), selected.get(Calendar.DAY_OF_MONTH));

            // End date cannot be before start date
            if (!isStartDate && periodStartDate > 0) {
                datePickerDialog.getDatePicker().setMinDate(periodStartDate);
            }
            datePickerDialog.show();
            int color = ContextCompat.getColor(this, R.color.vibrant_orange);
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(color);
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(color);
        } catch (Exception e) {
            AppLogger.e(getClass(), "showCustomDatePickerDialog", e);
        }
    }

    // -------------------------
    // ------- WALLETS ---------
    // -------------------------
    private boolean areAllWalletsSelected(Set<Integer> selectedIds) {
        return walletLists != null && !walletLists.isEmpty() && selectedIds.size() == walletLists.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void selectWallets() {
        try {

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_wallet_picker_layout, findViewById(android.R.id.content), false);
            RecyclerView rvWallets = bottomView.findViewById(R.id.rvWallets);
            View viewLine = bottomView.findViewById(R.id.viewLine);
            RelativeLayout rlSelectAll = bottomView.findViewById(R.id.rlSelectAll);
            MaterialCheckBox ivSelectAll = bottomView.findViewById(R.id.ivSelectAll);
            LinearLayout layoutAddWallet = bottomView.findViewById(R.id.layoutAddWallet);
            LinearLayout layoutActions = bottomView.findViewById(R.id.layoutActions);
            AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);
            MaterialButton btnDone = bottomView.findViewById(R.id.btnDone);
            viewLine.setVisibility(View.GONE);
            rlSelectAll.setVisibility(View.VISIBLE);
            layoutAddWallet.setVisibility(View.GONE);
            layoutActions.setVisibility(View.VISIBLE);

            Set<Integer> tempSelectedWalletIds = new HashSet<>(selectedWalletIds);

            RecyclerViewAdapter<WalletEntity> adapter = new RecyclerViewAdapter<>(this, walletLists, R.layout.item_switch_accounts) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, WalletEntity walletEntity) {

                    MaterialCheckBox ivChecked = holder.getView(R.id.ivChecked);
                    RelativeLayout rlAccountView = holder.getView(R.id.rlAccountView);

                    ivChecked.setVisibility(View.VISIBLE);

                    holder.setViewText(R.id.tvAccountName, walletEntity.name);
                    holder.setViewText(R.id.tvAccountBalance, getString(R.string.account_balance_format,
                            CommonUtils.getBeautifyAmount(walletEntity.currencySymbol, walletEntity.amount)));

                    ivChecked.setOnCheckedChangeListener(null);
                    ivChecked.setChecked(tempSelectedWalletIds.contains(walletEntity.id));

                    ivChecked.setOnCheckedChangeListener((buttonView, isChecked) -> {

                        if (isChecked) {
                            tempSelectedWalletIds.add(walletEntity.id);
                        } else {
                            tempSelectedWalletIds.remove(walletEntity.id);
                        }

                        ivSelectAll.setOnCheckedChangeListener(null);
                        ivSelectAll.setChecked(
                                areAllWalletsSelected(tempSelectedWalletIds)
                        );

                        ivSelectAll.setOnCheckedChangeListener(
                                (button, checked) -> {
                                    tempSelectedWalletIds.clear();

                                    if (checked && walletLists != null) {
                                        for (WalletEntity wallet : walletLists) {
                                            tempSelectedWalletIds.add(wallet.id);
                                        }
                                    }

                                    notifyDataSetChanged();
                                }
                        );
                    });

                    rlAccountView.setOnClickListener(v -> ivChecked.setChecked(!ivChecked.isChecked()));
                }
            };

            ivSelectAll.setChecked(areAllWalletsSelected(tempSelectedWalletIds));

            ivSelectAll.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> {

                        // DO NOT invert isChecked here.
                        // isChecked is already the new state.

                        tempSelectedWalletIds.clear();

                        if (isChecked && walletLists != null) {
                            for (WalletEntity wallet : walletLists) {
                                tempSelectedWalletIds.add(wallet.id);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }
            );

            rlSelectAll.setOnClickListener(v -> {
                ivSelectAll.setChecked(!ivSelectAll.isChecked());
            });

            rvWallets.setAdapter(adapter);
            rvWallets.setHasFixedSize(true);
            rvWallets.setLayoutManager(new LinearLayoutManager(this));

            tvClose.setOnClickListener(v -> dialog.dismiss());
            btnDone.setOnClickListener(v -> {
                selectedWalletIds.clear();
                selectedWalletIds.addAll(tempSelectedWalletIds);
                updateWalletTexts();
                dialog.dismiss();
            });

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "selectWallets", e);
        }
    }

    // -------------------------
    // ----- BUDGET METHOD -----
    // -------------------------
    private void selectBudgetMethod() {
        try {

            tempBudgetMethod = "";

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_budget_method, findViewById(android.R.id.content), false);
            MaterialCardView separateCard = bottomView.findViewById(R.id.separateCard);
            MaterialCardView budgetAmountCard = bottomView.findViewById(R.id.budgetAmountCard);
            AppCompatImageView ivCheckSeparate = bottomView.findViewById(R.id.ivCheckSeparate);
            AppCompatImageView ivCheckBudget = bottomView.findViewById(R.id.ivCheckBudget);
            AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);
            AppCompatTextView btnApply = bottomView.findViewById(R.id.btnApply);

            if (Objects.equals(budgetMethod, Constants.METHOD_SEPARATE)) {
                separateCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.app_light_background));
                separateCard.setStrokeColor(getColor(R.color.primary_dark));
                separateCard.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.box_stroke));
                ivCheckSeparate.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_radio_checked));
            } else {
                budgetAmountCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.app_light_background));
                budgetAmountCard.setStrokeColor(getColor(R.color.primary_dark));
                budgetAmountCard.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.box_stroke));
                ivCheckBudget.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_radio_checked));
            }

            separateCard.setOnClickListener(v -> {
                separateCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.app_light_background));
                separateCard.setStrokeColor(getColor(R.color.primary_dark));
                separateCard.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.box_stroke));
                ivCheckSeparate.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_radio_checked));

                budgetAmountCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
                budgetAmountCard.setStrokeColor(getColor(R.color.text_grey));
                budgetAmountCard.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.stroke_width_0_4));
                ivCheckBudget.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_radio_unchecked));

                tempBudgetMethod = Constants.METHOD_SEPARATE;
            });

            budgetAmountCard.setOnClickListener(v -> {
                budgetAmountCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.app_light_background));
                budgetAmountCard.setStrokeColor(getColor(R.color.primary_dark));
                budgetAmountCard.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.box_stroke));
                ivCheckBudget.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_radio_checked));

                separateCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
                separateCard.setStrokeColor(getColor(R.color.text_grey));
                separateCard.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.stroke_width_0_4));
                ivCheckSeparate.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_radio_unchecked));

                tempBudgetMethod = Constants.METHOD_SHARED;
            });

            tvClose.setOnClickListener(v -> dialog.dismiss());
            btnApply.setOnClickListener(v -> {
                budgetMethod = tempBudgetMethod;
                updateBudgetMethod();
                dialog.dismiss();
            });

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "selectBudgetMethod", e);
        }
    }

    private long getEndOfDay(long date) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(date);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            return calendar.getTimeInMillis();
        } catch (Exception e) {
            AppLogger.e(getClass(), "getEndOfDay", e);
            return date;
        }
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
    }
}