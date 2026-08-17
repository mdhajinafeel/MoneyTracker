package com.nprotech.moneytracker.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.CategoryEntity;
import com.nprotech.moneytracker.db.entites.CurrencyEntity;
import com.nprotech.moneytracker.db.entites.GoalEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.CalendarHelper;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.GoalFrequencyModel;
import com.nprotech.moneytracker.models.GoalWithDetails;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.utils.IntentUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.CategoryViewModel;
import com.nprotech.moneytracker.viewmodel.GoalViewModel;
import com.nprotech.moneytracker.viewmodel.MasterViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateGoalActivity extends BaseActivity {

    private AppCompatImageView icBack, ivGoalIcon;
    private AppCompatTextView tvSave, tvCategory, tvTargetCurrency, tvTargetAmount, tvTargetDate, tvGoalInitial, tvAutoSaveAmount, tvFrequency,
            tvStartDate, tvWeekDay, tvDayOfMonth, tvMonth, tvDay, maxLimitLabel;
    private AppCompatEditText etGoalName, etDescription;
    private MaterialCardView cardGoalCategory, cardGoalCurrency, cardGoalTarget, cardGoalDate, cardGoalInitial;
    private SwitchCompat switchAutoView;
    private LinearLayout layoutAutoSaveFields, layoutStartDate, layoutWeekDay, layoutDayOfMonth;
    private ConstraintLayout layoutYearly;
    private double targetAmount = 0, goalInitialAmount = 0, autoSaveAmount = 0;
    private ActivityResultLauncher<Intent> calculatorLauncher, categoryLauncher, currencyLauncher;
    private AccountEntity account;
    private AccountViewModel accountViewModel;
    private CategoryViewModel categoryViewModel;
    private MasterViewModel masterViewModel;
    private GoalViewModel goalViewModel;
    private CategoryEntity goalCategory;
    private CurrencyEntity currency;
    private int selectedFrequency = Constants.GOAL_FREQUENCY_MONTHLY;
    private long targetDate = 0, autoSaveStartDate = 0;
    private int autoSaveWeekDay = Calendar.MONDAY;
    private int autoSaveDayOfMonth = 1;
    private int autoSaveMonth = Calendar.JANUARY;
    private int autoSaveDay = 1;
    private boolean isEdit = false;
    private int goalId = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_goal);
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

            tvSave.setVisibility(View.VISIBLE);

            cardGoalCategory = findViewById(R.id.cardGoalCategory);
            cardGoalCurrency = findViewById(R.id.cardGoalCurrency);
            cardGoalTarget = findViewById(R.id.cardGoalTarget);
            cardGoalDate = findViewById(R.id.cardGoalDate);
            cardGoalInitial = findViewById(R.id.cardGoalInitial);
            etGoalName = findViewById(R.id.etGoalName);
            etDescription = findViewById(R.id.etDescription);
            maxLimitLabel = findViewById(R.id.maxLimitLabel);
            tvCategory = findViewById(R.id.tvCategory);
            tvTargetCurrency = findViewById(R.id.tvTargetCurrency);
            ivGoalIcon = findViewById(R.id.ivGoalIcon);
            tvTargetDate = findViewById(R.id.tvTargetDate);
            tvTargetAmount = findViewById(R.id.tvTargetAmount);
            tvGoalInitial = findViewById(R.id.tvGoalInitial);
            switchAutoView = findViewById(R.id.switchAutoView);
            layoutAutoSaveFields = findViewById(R.id.layoutAutoSaveFields);
            layoutStartDate = findViewById(R.id.layoutStartDate);
            layoutWeekDay = findViewById(R.id.layoutWeekDay);
            layoutDayOfMonth = findViewById(R.id.layoutDayOfMonth);
            layoutYearly = findViewById(R.id.layoutYearly);
            tvAutoSaveAmount = findViewById(R.id.tvAutoSaveAmount);
            tvFrequency = findViewById(R.id.tvFrequency);
            tvStartDate = findViewById(R.id.tvStartDate);
            tvWeekDay = findViewById(R.id.tvWeekDay);
            tvDayOfMonth = findViewById(R.id.tvDayOfMonth);
            tvMonth = findViewById(R.id.tvMonth);
            tvDay = findViewById(R.id.tvDay);

            accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
            categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
            masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);
            goalViewModel = new ViewModelProvider(this).get(GoalViewModel.class);

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

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {

                isEdit = bundle.getBoolean("isEdit", false);
                goalId = bundle.getInt("goalId", 0);

                if (isEdit) {
                    tvTitle.setText(getString(R.string.edit_goal));
                } else {
                    tvTitle.setText(getString(R.string.add_goal));
                }

                bindData();
                setupListeners();
                setupLauncher();
                updateSaveButtonState();
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

            account = accountViewModel.getAccountDetailById((int) PreferenceManager.INSTANCE.getAccountId());

            if (isEdit) {

                tvSave.setText(getString(R.string.update));

                GoalWithDetails goal = goalViewModel.fetchGoalDetails(goalId);
                if (goal != null) {

                    currency = masterViewModel.getCurrencyByCode(goal.currencyCode);
                    goalCategory = categoryViewModel.getCategoryById(goal.categoryId, false);

                    etGoalName.setText(goal.name);
                    maxLimitLabel.setText(getString(R.string.character_limit, Objects.requireNonNull(etGoalName.getText()).toString().length()));

                    targetAmount = goal.targetAmount;
                    goalInitialAmount = goal.initialAmount;
                    autoSaveAmount = goal.autoSaveAmount;

                    targetDate = goal.targetDate;
                    tvTargetDate.setText(DateHelper.getFormattedDate(targetDate, "dd/MM/yyyy"));
                    etDescription.setText(goal.notes);

                    // ---------------------------------------------
// AUTO SAVE
// ---------------------------------------------
                    if (goal.autoSaveEnabled) {

                        // Enable auto save
                        switchAutoView.setChecked(true);

                        // Restore auto save values
                        autoSaveAmount = goal.autoSaveAmount;
                        selectedFrequency = goal.autoSaveFrequency;

                        autoSaveStartDate = goal.autoSaveStartDate;
                        autoSaveWeekDay = goal.autoSaveWeekDay;
                        autoSaveDayOfMonth = goal.autoSaveDayOfMonth;
                        autoSaveMonth = goal.autoSaveMonth;
                        autoSaveDay = goal.autoSaveDay;

                        // Restore start date
                        if (autoSaveStartDate > 0) {
                            tvStartDate.setText(DateHelper.getFormattedDate(autoSaveStartDate, "dd MMM yyyy"));
                        }

                        // Restore frequency
                        switch (selectedFrequency) {

                            case Constants.GOAL_FREQUENCY_DAILY:
                                tvFrequency.setText(getString(R.string.calendar_daily));
                                break;

                            case Constants.GOAL_FREQUENCY_WEEKLY:
                                tvFrequency.setText(getString(R.string.calendar_weekly));
                                break;

                            case Constants.GOAL_FREQUENCY_MONTHLY:
                                tvFrequency.setText(getString(R.string.calendar_monthly));
                                break;

                            case Constants.GOAL_FREQUENCY_YEARLY:
                                tvFrequency.setText(getString(R.string.calendar_yearly));
                                break;

                            default:
                                selectedFrequency = Constants.GOAL_FREQUENCY_MONTHLY;
                                tvFrequency.setText(getString(R.string.calendar_monthly));
                                break;
                        }

                        // Restore frequency-specific fields
                        updateFrequencyFields();

                        // Show auto-save fields
                        layoutAutoSaveFields.setVisibility(View.VISIBLE);
                        layoutAutoSaveFields.setAlpha(1f);
                        layoutAutoSaveFields.setTranslationY(0f);

                    } else {

                        // Auto save disabled
                        switchAutoView.setChecked(false);

                        initializeAutoSaveFields();
                    }
                }
            } else {

                tvSave.setText(getString(R.string.save));

                currency = masterViewModel.getFirstCurrencyForAccount((int) PreferenceManager.INSTANCE.getAccountId());
                maxLimitLabel.setText(getString(R.string.character_limit, 0));

                goalCategory = getGoalCategoryId();

                targetDate = System.currentTimeMillis();
                tvTargetDate.setText(DateHelper.getFormattedDate(DateHelper.getCurrentDateTime()));

                layoutAutoSaveFields.setVisibility(View.GONE);
                layoutAutoSaveFields.setAlpha(1f);
                layoutAutoSaveFields.setTranslationY(0f);

                initializeAutoSaveFields();
            }

            updateCategory();
            updateCurrencyFields();
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAutoSaveFields() {

        // Default frequency
        selectedFrequency = Constants.GOAL_FREQUENCY_MONTHLY;
        tvFrequency.setText(getString(R.string.calendar_monthly));

        // Default schedule values
        autoSaveWeekDay = Calendar.MONDAY;
        autoSaveDayOfMonth = 1;
        autoSaveMonth = Calendar.JANUARY;
        autoSaveDay = 1;

        // Prepare the fields
        updateFrequencyFields();

        // But hide the entire Auto Save section
        layoutAutoSaveFields.setVisibility(View.GONE);
        layoutAutoSaveFields.setAlpha(1f);
        layoutAutoSaveFields.setTranslationY(0f);
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            });

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finish();
                    ActivityUtils.overrideCloseTransition(CreateGoalActivity.this, R.anim.scale_in, R.anim.right_to_left);
                }
            });

            etGoalName.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {

                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    maxLimitLabel.setText(getString(R.string.character_limit, charSequence.length()));
                }
            });

            cardGoalCategory.setOnClickListener(view -> {
                hideKeyboard(this);

                Intent intent = new Intent(this, GoalCategoryPickerActivity.class);
                intent.putExtra("transactionType", TransactionEntity.TYPE_GOAL);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                categoryLauncher.launch(intent, options);
            });
            tvCategory.setOnClickListener(view -> cardGoalCategory.performClick());

            cardGoalCurrency.setOnClickListener(view -> {
                Intent intent = new Intent(this, CurrencyPickerActivity.class);
                intent.putExtra("currency", currency);
                intent.putExtra("type", "goal");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.slide_in_right, R.anim.slide_out_left);
                currencyLauncher.launch(intent, options);
            });
            tvTargetCurrency.setOnClickListener(view -> cardGoalCurrency.performClick());

            cardGoalDate.setOnClickListener(view -> openTargetDatePicker());
            tvTargetDate.setOnClickListener(view -> cardGoalDate.performClick());

            cardGoalTarget.setOnClickListener(view -> {
                hideKeyboard(this);
                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", targetAmount);
                intent.putExtra("type", "targetAmount");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                calculatorLauncher.launch(intent, options);
            });
            tvTargetAmount.setOnClickListener(view -> cardGoalTarget.performClick());

            cardGoalInitial.setOnClickListener(view -> {
                hideKeyboard(this);
                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", goalInitialAmount);
                intent.putExtra("type", "goalInitialAmount");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                calculatorLauncher.launch(intent, options);
            });
            tvGoalInitial.setOnClickListener(view -> cardGoalInitial.performClick());

            // AUTO SAVE
            switchAutoView.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                animateAutoSaveFields(isChecked);
                updateSaveButtonState();
            });

            tvAutoSaveAmount.setOnClickListener(view -> {
                hideKeyboard(this);
                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", autoSaveAmount);
                intent.putExtra("type", "autoSaveAmount");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                calculatorLauncher.launch(intent, options);
            });

            tvFrequency.setOnClickListener(view -> showFrequencyPicker());

            layoutStartDate.setOnClickListener(v -> openAutoSaveStartDatePicker());
            tvStartDate.setOnClickListener(v -> openAutoSaveStartDatePicker());

            // SAVE
            tvSave.setOnClickListener(view -> saveGoal());
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void setupLauncher() {
        calculatorLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            double amount = data.getDoubleExtra("amount", 0);
                            String type = data.getStringExtra("type");

                            if (type != null && type.equalsIgnoreCase("targetAmount")) {
                                targetAmount = amount;
                                tvTargetAmount.setText(CommonUtils.getBeautifyAmount(currency.symbol, targetAmount));
                            } else if (type != null && type.equalsIgnoreCase("goalInitialAmount")) {
                                goalInitialAmount = amount;
                                tvGoalInitial.setText(CommonUtils.getBeautifyAmount(currency.symbol, goalInitialAmount));
                            } else if (type != null && type.equalsIgnoreCase("autoSaveAmount")) {
                                autoSaveAmount = amount;
                                tvAutoSaveAmount.setText(CommonUtils.getBeautifyAmount(currency.symbol, autoSaveAmount));
                            }
                            updateSaveButtonState();
                        }
                    }
                });

        categoryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {

                            CategoryEntity categoryEntity = IntentUtils.getSerializableExtra(data, "category", CategoryEntity.class);
                            if (categoryEntity != null) {
                                goalCategory = categoryEntity;
                                updateCategory();
                                updateSaveButtonState();
                            }
                        }
                    }
                });

        currencyLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            currency = IntentUtils.getSerializableExtra(data, "currency", CurrencyEntity.class);
                            if (currency != null) {
                                updateCurrencyFields();
                            }
                        }
                    }
                });
    }

    private void openTargetDatePicker() {

        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, R.style.CustomDateTimePickerDialog,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0);
                    selectedDate.set(Calendar.MILLISECOND, 0);
                    targetDate = selectedDate.getTimeInMillis();
                    String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedDate.getTime());
                    tvTargetDate.setText(date);
                    updateSaveButtonState();
                }, year, month, day);

        // Allow today and future dates only
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        dialog.show();
        int color = ContextCompat.getColor(this, R.color.vibrant_orange);
        dialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(color);
        dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(color);
    }

    private void updateSaveButtonState() {
        boolean enabled = targetAmount > 0;

        enabled &= goalCategory != null;
        enabled &= !tvTargetDate.getText().toString().isEmpty();
        enabled &= !Objects.requireNonNull(etGoalName.getText()).toString().isEmpty();

        // Auto Save validation
        if (switchAutoView.isChecked()) {
            enabled &= autoSaveAmount > 0;
            enabled &= autoSaveStartDate > 0;
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

    private CategoryEntity getGoalCategoryId() {
        return categoryViewModel.getDefaultCategoryByType(1, TransactionEntity.TYPE_GOAL);
    }

    private void updateCategory() {
        tvCategory.setText(goalCategory.name);

        Drawable background = ivGoalIcon.getBackground().mutate();
        DrawableCompat.setTint(background, Color.parseColor(goalCategory.color));
        ivGoalIcon.setBackground(background);

        ivGoalIcon.setImageResource(DataHelper.getGoalIcons().get(goalCategory.icon));
    }

    private void animateAutoSaveFields(boolean show) {

        hideKeyboard(this);

        if (show) {

            // Make sure default frequency is initialized
            if (tvFrequency.getText().toString().isEmpty()) {

                tvFrequency.setText(
                        getString(R.string.calendar_monthly)
                );
            }

            updateFrequencyFields();

            layoutAutoSaveFields.setVisibility(View.VISIBLE);
            layoutAutoSaveFields.setAlpha(0f);
            layoutAutoSaveFields.setTranslationY(-20f);

            layoutAutoSaveFields.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .start();

        } else {

            layoutAutoSaveFields.animate()
                    .alpha(0f)
                    .translationY(-20f)
                    .setDuration(250)
                    .withEndAction(() -> {

                        layoutAutoSaveFields.setVisibility(
                                View.GONE
                        );

                        layoutAutoSaveFields.setAlpha(1f);
                        layoutAutoSaveFields.setTranslationY(0f);
                    })
                    .start();
        }
    }

    private void showFrequencyPicker() {
        try {
            List<GoalFrequencyModel> frequencyList = new ArrayList<>();
            frequencyList.add(new GoalFrequencyModel(Constants.GOAL_FREQUENCY_DAILY, R.drawable.ic_calendar_daily, getString(R.string.calendar_daily)));
            frequencyList.add(new GoalFrequencyModel(Constants.GOAL_FREQUENCY_WEEKLY, R.drawable.ic_calendar_weekly, getString(R.string.calendar_weekly)));
            frequencyList.add(new GoalFrequencyModel(Constants.GOAL_FREQUENCY_MONTHLY, R.drawable.ic_calendar_monthly, getString(R.string.calendar_monthly)));
            frequencyList.add(new GoalFrequencyModel(Constants.GOAL_FREQUENCY_YEARLY, R.drawable.ic_yearly, getString(R.string.calendar_yearly)));

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_calendar_filter_layout, findViewById(android.R.id.content), false);

            AppCompatTextView tvSelectRange = bottomView.findViewById(R.id.tvSelectRange);
            RecyclerView rvSelectRange = bottomView.findViewById(R.id.rvSelectRange);

            tvSelectRange.setText(R.string.select_frequency);

            RecyclerViewAdapter<GoalFrequencyModel> adapter = new RecyclerViewAdapter<>(this, frequencyList, R.layout.item_calendar_filter) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, GoalFrequencyModel frequency) {
                    Typeface medium = ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_medium);
                    Typeface semiBold = ResourcesCompat.getFont(holder.itemView.getContext(), R.font.exo2_semibold);

                    boolean selected = selectedFrequency == frequency.frequency;

                    holder.setViewText(R.id.tvFilterName, frequency.frequencyName);
                    holder.setViewImageDrawable(R.id.ivIcon, ContextCompat.getDrawable(getApplicationContext(), frequency.icon));

                    holder.setViewVisibility(R.id.ivSelected, selected ? View.VISIBLE : View.GONE);

                    holder.setViewTypeface(R.id.tvFilterName, selected ? semiBold : medium);

                    holder.getView(R.id.rlFilterView).setOnClickListener(v -> {
                        selectedFrequency = frequency.frequency;
                        tvFrequency.setText(frequency.frequencyName);
                        updateFrequencyFields();
                        dialog.dismiss();
                    });
                }
            };

            rvSelectRange.setAdapter(adapter);
            rvSelectRange.setHasFixedSize(true);
            rvSelectRange.setItemAnimator(null);

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showFrequencyPicker", e);
        }
    }

    private void updateFrequencyFields() {

        layoutStartDate.setVisibility(View.VISIBLE);

        layoutWeekDay.setVisibility(View.GONE);
        layoutDayOfMonth.setVisibility(View.GONE);
        layoutYearly.setVisibility(View.GONE);

        switch (selectedFrequency) {

            case Constants.GOAL_FREQUENCY_DAILY:
                break;

            case Constants.GOAL_FREQUENCY_WEEKLY:
                layoutWeekDay.setVisibility(View.VISIBLE);
                tvWeekDay.setText(CalendarHelper.getWeekDayName(autoSaveWeekDay, getApplicationContext()));
                break;

            case Constants.GOAL_FREQUENCY_MONTHLY:
                layoutDayOfMonth.setVisibility(View.VISIBLE);
                tvDayOfMonth.setText(String.valueOf(autoSaveDayOfMonth));
                break;

            case Constants.GOAL_FREQUENCY_YEARLY:
                layoutYearly.setVisibility(View.VISIBLE);
                tvMonth.setText(CalendarHelper.getMonthName(autoSaveMonth, getApplicationContext()));
                tvDay.setText(String.valueOf(autoSaveDay));

                break;
        }
    }

    private void openAutoSaveStartDatePicker() {

        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, R.style.CustomDateTimePickerDialog,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0);
                    selectedDate.set(Calendar.MILLISECOND, 0);
                    autoSaveStartDate = selectedDate.getTimeInMillis();
                    tvStartDate.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedDate.getTime()));
                    prepareAutoSaveSchedule(selectedDate);
                    updateSaveButtonState();
                }, year, month, day
        );

        // Today and future only
        dialog.getDatePicker().setMinDate(
                System.currentTimeMillis() - 1000
        );

        dialog.show();
        int color = ContextCompat.getColor(this, R.color.vibrant_orange);
        dialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(color);
        dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(color);
    }

    private void prepareAutoSaveSchedule(Calendar calendar) {
        autoSaveWeekDay = calendar.get(Calendar.DAY_OF_WEEK);
        autoSaveDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        autoSaveMonth = calendar.get(Calendar.MONTH);
        autoSaveDay = calendar.get(Calendar.DAY_OF_MONTH);
        updateFrequencyFields();
    }

    private void saveGoal() {
        try {
            if (!validateGoal()) {
                return;
            }

            GoalEntity goal = new GoalEntity();

            // ------------------------------------------------
            // BASIC GOAL
            // ------------------------------------------------
            goal.id = goalId;
            goal.name = Objects.requireNonNull(etGoalName.getText()).toString().trim();
            goal.targetAmount = targetAmount;
            goal.initialAmount = goalInitialAmount;
            goal.targetDate = targetDate;
            goal.category = goalCategory.id;
            goal.currencyId = currency.id;
            goal.accountId = (int) PreferenceManager.INSTANCE.getAccountId();
            goal.notes = Objects.requireNonNull(etDescription.getText()).toString().trim();

            // ------------------------------------------------
            // AUTO SAVE
            // ------------------------------------------------
            goal.autoSaveEnabled = switchAutoView.isChecked();

            if (goal.autoSaveEnabled) {
                goal.autoSaveAmount = autoSaveAmount;
                goal.autoSaveFrequency = selectedFrequency;
                goal.autoSaveStartDate = autoSaveStartDate;
                goal.autoSaveWeekDay = autoSaveWeekDay;
                goal.autoSaveDayOfMonth = autoSaveDayOfMonth;
                goal.autoSaveMonth = autoSaveMonth;
                goal.autoSaveDay = autoSaveDay;
                goal.nextAutoSaveDate = autoSaveStartDate;
            } else {
                goal.autoSaveAmount = 0;
                goal.autoSaveFrequency = 0;
                goal.autoSaveStartDate = 0;
                goal.autoSaveWeekDay = 0;
                goal.autoSaveDayOfMonth = 0;
                goal.autoSaveMonth = 0;
                goal.autoSaveDay = 0;
                goal.nextAutoSaveDate = 0;
            }

            long now = System.currentTimeMillis();
            goal.updatedAt = now;

            // ------------------------------------------------
            // EDIT
            // ------------------------------------------------
            if (isEdit) {

                long result = goalViewModel.updateGoal(goal, this);

                if (result > 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.goal_updated_successfully), Toast.LENGTH_SHORT).show();
                    finish();
                    ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_update), Toast.LENGTH_SHORT).show();
                }
            } else {

                // ------------------------------------------------
                // NEW GOAL
                // ------------------------------------------------
                goal.savedAmount = 0;
                goal.isSynced = false;
                goal.isDeleted = false;
                goal.isCompleted = false;
                goal.completedOn = 0;
                goal.isArchived = false;
                goal.archivedOn = 0;
                goal.startedDate = now;

                long newGoalId = goalViewModel.createGoal(goal, goalInitialAmount, this);

                if (newGoalId > 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.goal_added_successfully), Toast.LENGTH_SHORT).show();
                    finish();
                    ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_add), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "saveGoal", e);
        }
    }

    private boolean validateGoal() {

        if (etGoalName.getText() == null || etGoalName.getText().toString().trim().isEmpty()) {
            etGoalName.requestFocus();
            return false;
        }

        if (goalCategory == null) {
            return false;
        }

        if (targetAmount <= 0) {
            return false;
        }

        if (targetDate <= 0) {
            return false;
        }

        if (switchAutoView.isChecked()) {

            if (autoSaveAmount <= 0) {
                return false;
            }

            return autoSaveStartDate > 0;
        }
        return true;
    }

    private void updateCurrencyFields() {
        tvTargetCurrency.setText(getString(R.string.currency_display, currency.code, currency.name));
        tvTargetAmount.setText(CommonUtils.getBeautifyAmount(currency.symbol, targetAmount));
        tvGoalInitial.setText(CommonUtils.getBeautifyAmount(currency.symbol, goalInitialAmount));
        tvAutoSaveAmount.setText(CommonUtils.getBeautifyAmount(currency.symbol, autoSaveAmount));
    }
}