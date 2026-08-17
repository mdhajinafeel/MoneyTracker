package com.nprotech.moneytracker.ui.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.CalendarHelper;
import com.nprotech.moneytracker.models.GoalFrequencyModel;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateAutoSaveActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView tvSave, tvAutoSaveAmount, tvStartOn, tvFrequency, tvDayMonth, tvWeekDay, tvYearMonth, tvYearDay;
    private MaterialCardView cardAutoSaveAmount, cardAutoSaveFrequency, cardAutoSaveStartOn, cardAutoSaveDayMonth, cardAutoSaveWeekDay, cardAutoSaveYear, cardAutoSaveYearDay;
    private ActivityResultLauncher<Intent> calculatorLauncher;
    private double autoSaveAmount = 0;
    private long autoSaveStartDate = 0;
    private String currencySymbol = "";
    private int autoSaveWeekDay = Calendar.MONDAY;
    private int autoSaveDayOfMonth = 1;
    private int autoSaveMonth = Calendar.JANUARY;
    private int autoSaveDay = 1;
    private int selectedFrequency = Constants.GOAL_FREQUENCY_MONTHLY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_auto_save);
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

            tvTitle.setText(getString(R.string.auto_save));
            tvSave.setVisibility(View.VISIBLE);

            cardAutoSaveAmount = findViewById(R.id.cardAutoSaveAmount);
            cardAutoSaveFrequency = findViewById(R.id.cardAutoSaveFrequency);
            cardAutoSaveStartOn = findViewById(R.id.cardAutoSaveStartOn);
            cardAutoSaveDayMonth = findViewById(R.id.cardAutoSaveDayMonth);
            cardAutoSaveWeekDay = findViewById(R.id.cardAutoSaveWeekDay);
            cardAutoSaveYear = findViewById(R.id.cardAutoSaveYear);
            cardAutoSaveYearDay = findViewById(R.id.cardAutoSaveYearDay);
            tvAutoSaveAmount = findViewById(R.id.tvAutoSaveAmount);
            tvStartOn = findViewById(R.id.tvStartOn);
            tvFrequency = findViewById(R.id.tvFrequency);
            tvDayMonth = findViewById(R.id.tvDayMonth);
            tvWeekDay = findViewById(R.id.tvWeekDay);
            tvYearMonth = findViewById(R.id.tvYearMonth);
            tvYearDay = findViewById(R.id.tvYearDay);

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
                bindData(bundle);
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

    private void bindData(Bundle bundle) {
        try {
            int goalId = bundle.getInt("goalId", 0);
            currencySymbol = bundle.getString("currencySymbol", "");
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void updateSaveButtonState() {
        boolean enabled = autoSaveAmount > 0;
        enabled &= autoSaveStartDate > 0;

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
                    ActivityUtils.overrideCloseTransition(CreateAutoSaveActivity.this, R.anim.scale_in, R.anim.right_to_left);
                }
            });

            cardAutoSaveAmount.setOnClickListener(view -> {
                hideKeyboard(this);
                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", autoSaveAmount);
                intent.putExtra("type", "autoSaveAmount");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                calculatorLauncher.launch(intent, options);
            });
            tvAutoSaveAmount.setOnClickListener(view -> cardAutoSaveAmount.performClick());

            cardAutoSaveStartOn.setOnClickListener(v -> openAutoSaveStartDatePicker());
            tvStartOn.setOnClickListener(v -> openAutoSaveStartDatePicker());

            tvFrequency.setOnClickListener(view -> showFrequencyPicker());
            cardAutoSaveFrequency.setOnClickListener(v -> showFrequencyPicker());

            // SAVE
            tvSave.setOnClickListener(view -> {
            });
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

                            if (type != null && type.equalsIgnoreCase("autoSaveAmount")) {
                                autoSaveAmount = amount;
                                tvAutoSaveAmount.setText(CommonUtils.getBeautifyAmount(currencySymbol, autoSaveAmount));
                            }
                            updateSaveButtonState();
                        }
                    }
                });
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
                    tvStartOn.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedDate.getTime()));
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

    private void updateFrequencyFields() {

        cardAutoSaveStartOn.setVisibility(View.VISIBLE);

        cardAutoSaveWeekDay.setVisibility(View.GONE);
        cardAutoSaveDayMonth.setVisibility(View.GONE);
        cardAutoSaveYear.setVisibility(View.GONE);
        cardAutoSaveYearDay.setVisibility(View.GONE);

        switch (selectedFrequency) {

            case Constants.GOAL_FREQUENCY_DAILY:
                break;

            case Constants.GOAL_FREQUENCY_WEEKLY:
                cardAutoSaveWeekDay.setVisibility(View.VISIBLE);
                tvWeekDay.setText(CalendarHelper.getWeekDayName(autoSaveWeekDay, getApplicationContext()));
                break;

            case Constants.GOAL_FREQUENCY_MONTHLY:
                cardAutoSaveDayMonth.setVisibility(View.VISIBLE);
                tvDayMonth.setText(String.valueOf(autoSaveDayOfMonth));
                break;

            case Constants.GOAL_FREQUENCY_YEARLY:
                cardAutoSaveYear.setVisibility(View.VISIBLE);
                cardAutoSaveYearDay.setVisibility(View.VISIBLE);
                tvYearMonth.setText(CalendarHelper.getMonthName(autoSaveMonth, getApplicationContext()));
                tvYearDay.setText(String.valueOf(autoSaveDay));

                break;
        }
    }
}