package com.nprotech.moneytracker.ui.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.DebtLoanType;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanPaymentEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.FrequencyModel;
import com.nprotech.moneytracker.models.ReducingBalancePaymentModel;
import com.nprotech.moneytracker.ui.adapters.ColorSpinnerAdapter;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.utils.CustomNumberPicker;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.DebtLoanViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateDebtLoanActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener {

    private AppCompatImageView icBack, ivBorrowSelected, ivLentSelected, ivDebtIcon, ivDueDateSelect;
    private AppCompatTextView tvSave, nameLabel, maxLimitName, maxLimitTitle, tvWallet, tvTitle, tvAmount, moneyReceiveLabel, moneyReceiveHint, tvReminder,
            maxLimitNote, tvPrincipalAmount, tvInterestAmount, lblTotalAmount, tvTotalAmount, tvStartDate, tvDueDate, tvInterest, tvInterestRate, tvRepaymentMethod,
            tvInterestPeriod, tvCalcMethod, tvCompoundFrequency, tvDuration, tvSchedulePaymentCount, tvScheduleFrequency, tvScheduleTotalAmount,
            tvScheduleTotalInterest;
    private TextInputLayout tilInterestRate, tilInterestPeriod, tilInterestAmount, tilCalculationMethod, tilInterestDuration, tilCompoundFrequency,
            tilCustomInterestDuration;
    private AppCompatEditText etName, etTitle, etNotes;
    private TextInputEditText etInterestRate, etInterestPeriod, etInterestAmount, etCalculationMethod, etInterestDuration, etCompoundFrequency,
            etNoOfInstallments, etPaymentFrequency, etInstallmentAmount, etFirstPaymentDate, etCustomInterestDuration;
    private MaterialCardView cardBorrow, cardLent, cardWallet, cardAmount, cardReminder, cardColor, cardIcon, cardRepaymentSchedule;
    private LinearLayout layoutInterestWrapper, layoutRepaymentWrapper, layoutInterestRate, layoutInterestPeriod, layoutCalcMethod,
            layoutCompoundFrequency, layoutDuration;
    private View viewInterest;
    private ConstraintLayout layoutInterestFields, layoutRepaymentFields, layoutIcon;
    private RecyclerView rvRepaymentSchedule;
    private FrameLayout frameColor;
    private AppCompatSpinner colorSpinner;
    private SwitchCompat switchMoneyReceive;
    private ActivityResultLauncher<Intent> calculatorLauncher, iconLauncher;
    private WalletEntity selectedWallet;
    private AccountEntity account;
    private List<WalletEntity> walletLists;
    private int selectedType = 0, debtIcon = 0, selectedInterest = 0, tempInterest = 0, selectedInterestPeriod = 0, tempInterestPeriod = 0,
            selectedInterestCalcMethodPeriod = 0, tempInterestCalcMethodPeriod = 0, selectedInterestCompFreqPeriod = 0, tempInterestCompFreqPeriod = 0,
            selectedInterestDurationPeriod = 0, tempInterestDurationPeriod = 0, selectedRepaymentMethod = 0, tempRepaymentMethod = 0,
            selectedRepaymentFrequency = 0, tempRepaymentFrequency = 0;
    private int selectedNoOfInstallments = 0, tempNoOfInstallments = 0, customInterestDuration = 0, customInterestDurationPeriod = 0;
    private boolean isEdit = false;
    private double debtLoanPrincipalAmount = 0, debtLoanInterestAmount = 0;
    private static final int DATE_TYPE_START = 1;
    private static final int DATE_TYPE_DUE = 2;
    private static final int DATE_TYPE_FIRST_PAYMENT = 3;
    private int selectedDateType = DATE_TYPE_START;
    private Date startDate, dueDate, firstPaymentDate;
    private ArrayList<String> debtColorLists;
    private AccountViewModel accountViewModel;
    private DebtLoanViewModel debtLoanViewModel;
    private Typeface medium, semiBold;
    private List<ReducingBalancePaymentModel> schedule;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_debt_loan);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            View rootView = findViewById(R.id.rootView);
            tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);

            icBack = findViewById(R.id.icBack);
            tvSave = findViewById(R.id.tvSave);
            ivBorrowSelected = findViewById(R.id.ivBorrowSelected);
            ivLentSelected = findViewById(R.id.ivLentSelected);
            cardBorrow = findViewById(R.id.cardBorrow);
            cardLent = findViewById(R.id.cardLent);
            etName = findViewById(R.id.etName);
            etTitle = findViewById(R.id.etTitle);
            nameLabel = findViewById(R.id.nameLabel);
            maxLimitName = findViewById(R.id.maxLimitName);
            maxLimitTitle = findViewById(R.id.maxLimitTitle);
            cardWallet = findViewById(R.id.cardWallet);
            tvWallet = findViewById(R.id.tvWallet);
            cardAmount = findViewById(R.id.cardAmount);
            tvAmount = findViewById(R.id.tvAmount);
            layoutInterestWrapper = findViewById(R.id.layoutInterestWrapper);
            layoutInterestFields = findViewById(R.id.layoutInterestFields);
            etInterestRate = findViewById(R.id.etInterestRate);
            etInterestPeriod = findViewById(R.id.etInterestPeriod);
            etInterestAmount = findViewById(R.id.etInterestAmount);
            etCalculationMethod = findViewById(R.id.etCalculationMethod);
            etInterestDuration = findViewById(R.id.etInterestDuration);
            etCompoundFrequency = findViewById(R.id.etCompoundFrequency);
            layoutRepaymentWrapper = findViewById(R.id.layoutRepaymentWrapper);
            etNoOfInstallments = findViewById(R.id.etNoOfInstallments);
            etPaymentFrequency = findViewById(R.id.etPaymentFrequency);
            etInstallmentAmount = findViewById(R.id.etInstallmentAmount);
            etFirstPaymentDate = findViewById(R.id.etFirstPaymentDate);
            moneyReceiveLabel = findViewById(R.id.moneyReceiveLabel);
            moneyReceiveHint = findViewById(R.id.moneyReceiveHint);
            switchMoneyReceive = findViewById(R.id.switchMoneyReceive);
            cardReminder = findViewById(R.id.cardReminder);
            tvReminder = findViewById(R.id.tvReminder);
            cardColor = findViewById(R.id.cardColor);
            cardIcon = findViewById(R.id.cardIcon);
            frameColor = findViewById(R.id.frameColor);
            colorSpinner = findViewById(R.id.colorSpinner);
            layoutIcon = findViewById(R.id.layoutIcon);
            etNotes = findViewById(R.id.etNotes);
            maxLimitNote = findViewById(R.id.maxLimitNote);
            tvPrincipalAmount = findViewById(R.id.tvPrincipalAmount);
            tvInterestAmount = findViewById(R.id.tvInterestAmount);
            lblTotalAmount = findViewById(R.id.lblTotalAmount);
            tvTotalAmount = findViewById(R.id.tvTotalAmount);
            ivDebtIcon = findViewById(R.id.ivDebtIcon);
            tvStartDate = findViewById(R.id.tvStartDate);
            tvDueDate = findViewById(R.id.tvDueDate);
            ivDueDateSelect = findViewById(R.id.ivDueDateSelect);
            tvInterest = findViewById(R.id.tvInterest);
            tilInterestRate = findViewById(R.id.tilInterestRate);
            tilInterestPeriod = findViewById(R.id.tilInterestPeriod);
            tilInterestAmount = findViewById(R.id.tilInterestAmount);
            tilCalculationMethod = findViewById(R.id.tilCalculationMethod);
            tilInterestDuration = findViewById(R.id.tilInterestDuration);
            tilCompoundFrequency = findViewById(R.id.tilCompoundFrequency);
            layoutInterestRate = findViewById(R.id.layoutInterestRate);
            layoutInterestPeriod = findViewById(R.id.layoutInterestPeriod);
            layoutCalcMethod = findViewById(R.id.layoutCalcMethod);
            layoutCompoundFrequency = findViewById(R.id.layoutCompoundFrequency);
            layoutDuration = findViewById(R.id.layoutDuration);
            viewInterest = findViewById(R.id.viewInterest);
            tvInterestRate = findViewById(R.id.tvInterestRate);
            tvInterestPeriod = findViewById(R.id.tvInterestPeriod);
            tvCalcMethod = findViewById(R.id.tvCalcMethod);
            tvCompoundFrequency = findViewById(R.id.tvCompoundFrequency);
            tvDuration = findViewById(R.id.tvDuration);
            tvRepaymentMethod = findViewById(R.id.tvRepaymentMethod);
            layoutRepaymentFields = findViewById(R.id.layoutRepaymentFields);
            cardRepaymentSchedule = findViewById(R.id.cardRepaymentSchedule);
            rvRepaymentSchedule = findViewById(R.id.rvRepaymentSchedule);
            cardRepaymentSchedule.setVisibility(View.GONE);
            tvSchedulePaymentCount = findViewById(R.id.tvSchedulePaymentCount);
            tvScheduleFrequency = findViewById(R.id.tvScheduleFrequency);
            tvScheduleTotalAmount = findViewById(R.id.tvScheduleTotalAmount);
            tvScheduleTotalInterest = findViewById(R.id.tvScheduleTotalInterest);
            tilCustomInterestDuration = findViewById(R.id.tilCustomInterestDuration);
            etCustomInterestDuration = findViewById(R.id.etCustomInterestDuration);

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

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {

                isEdit = bundle.getBoolean("isEdit", false);

                accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
                debtLoanViewModel = new ViewModelProvider(this).get(DebtLoanViewModel.class);

                medium = ResourcesCompat.getFont(this, R.font.exo2_medium);
                semiBold = ResourcesCompat.getFont(this, R.font.exo2_semibold);

                bindData();
                setupListeners();
                setupLauncher();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finishWithTransitions();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {

            walletLists = accountViewModel.getWalletsByAccountId(PreferenceManager.INSTANCE.getAccountId());
            account = accountViewModel.getAccountDetailById(PreferenceManager.INSTANCE.getAccountId());

            debtColorLists = new ArrayList<>();
            debtColorLists = DataHelper.getDebtLoanColorList();
            ColorSpinnerAdapter colorSpinnerAdapter = new ColorSpinnerAdapter(this, R.layout.list_drop_down_color, R.id.label, debtColorLists);
            colorSpinner.setAdapter(colorSpinnerAdapter);

            if (isEdit) {

                tvSave.setText(getString(R.string.update));
                tvTitle.setText(getString(R.string.edit_debt_loan));

            } else {

                tvSave.setText(getString(R.string.save));
                tvTitle.setText(getString(R.string.create_debt_loan));

                // Default start date = today
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = calendar.getTime();
                tvStartDate.setText(DateHelper.getDateFromPicker(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)));

                selectedType = DebtLoanType.BORROW;
                selectedInterest = DebtLoanType.DEBT_NO_INTEREST;
                tempInterest = DebtLoanType.DEBT_NO_INTEREST;
                selectedInterestPeriod = DebtLoanType.INTEREST_PERIOD_YEAR;
                tempInterestPeriod = DebtLoanType.INTEREST_PERIOD_YEAR;
                selectedInterestCalcMethodPeriod = DebtLoanType.INTEREST_CALC_SI;
                tempInterestCalcMethodPeriod = DebtLoanType.INTEREST_CALC_SI;
                selectedInterestCompFreqPeriod = DebtLoanType.INTEREST_CI_COMP_FREQ_MONTHLY;
                tempInterestCompFreqPeriod = DebtLoanType.INTEREST_CI_COMP_FREQ_MONTHLY;
                selectedInterestDurationPeriod = DebtLoanType.INTEREST_LOAN_PERIOD;
                tempInterestDurationPeriod = DebtLoanType.INTEREST_LOAN_PERIOD;
                selectedRepaymentMethod = DebtLoanType.REPAYMENT_FLEXIBLE;
                tempRepaymentMethod = DebtLoanType.REPAYMENT_FLEXIBLE;
                selectedRepaymentFrequency = DebtLoanType.REPAYMENT_FREQ_MONTHLY;
                tempRepaymentFrequency = DebtLoanType.REPAYMENT_FREQ_MONTHLY;
                customInterestDurationPeriod = DebtLoanType.INTEREST_PERIOD_MONTH;
                debtIcon = 152;

                if (!walletLists.isEmpty()) {
                    selectedWallet = walletLists.get(0);
                    tvWallet.setText(getString(R.string.wallet_info, selectedWallet.name,
                            CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol, selectedWallet.amount)));
                }
            }

            CommonUtils.setDrawableEditText(CreateDebtLoanActivity.this, etInterestRate, R.drawable.ic_percentage, R.dimen.icon_18, R.color.primary_dark,
                    Gravity.START);
            CommonUtils.setDrawablesTIEditText(CreateDebtLoanActivity.this, etInterestPeriod, R.drawable.ic_calendar, R.drawable.ic_account_caret,
                    R.dimen.icon_20, R.dimen.icon_14, R.color.primary_dark);
            CommonUtils.setDrawablesTIEditText(CreateDebtLoanActivity.this, etInterestAmount, R.drawable.ic_coins, R.drawable.ic_account_caret,
                    R.dimen.icon_20, R.dimen.icon_14, R.color.primary_dark);
            CommonUtils.setDrawablesTIEditText(CreateDebtLoanActivity.this, etCalculationMethod, R.drawable.ic_calculator, R.drawable.ic_account_caret,
                    R.dimen.icon_20, R.dimen.icon_14, R.color.primary_dark);
            CommonUtils.setDrawablesTIEditText(CreateDebtLoanActivity.this, etInterestDuration, R.drawable.ic_calendar, R.drawable.ic_account_caret,
                    R.dimen.icon_20, R.dimen.icon_14, R.color.primary_dark);
            CommonUtils.setDrawablesTIEditText(CreateDebtLoanActivity.this, etCompoundFrequency, R.drawable.ic_calendar, R.drawable.ic_account_caret,
                    R.dimen.icon_20, R.dimen.icon_14, R.color.primary_dark);

            CommonUtils.setDrawablesTIEditText(CreateDebtLoanActivity.this, etNoOfInstallments, R.drawable.ic_number, R.drawable.ic_account_caret,
                    R.dimen.icon_20, R.dimen.icon_14, R.color.primary_dark);
            CommonUtils.setDrawablesTIEditText(CreateDebtLoanActivity.this, etPaymentFrequency, R.drawable.ic_calendar, R.drawable.ic_account_caret,
                    R.dimen.icon_20, R.dimen.icon_14, R.color.primary_dark);
            CommonUtils.setDrawablesTIEditText(CreateDebtLoanActivity.this, etInstallmentAmount, R.drawable.ic_coins, R.drawable.ic_account_caret,
                    R.dimen.icon_20, R.dimen.icon_14, R.color.primary_dark);
            CommonUtils.setDrawablesTIEditText(CreateDebtLoanActivity.this, etFirstPaymentDate, R.drawable.ic_calendar, R.drawable.ic_account_caret,
                    R.dimen.icon_20, R.dimen.icon_14, R.color.primary_dark);
            CommonUtils.setDrawablesTIEditText(CreateDebtLoanActivity.this, etCustomInterestDuration, R.drawable.ic_calendar_custom,
                    R.drawable.ic_account_caret, R.dimen.icon_20, R.dimen.icon_14, R.color.primary_dark);

            updateTypeSelection();
            updateAmountText();
            updateInterestFields();
            updateInterestPeriodFields();
            updateInterestCalcMethodFields();
            updateInterestCompFrequencyFields();
            updateInterestDurationFields();
            updatePaymentFrequency();
            updateSaveButtonState();
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> finishWithTransitions());

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finishWithTransitions();
                }
            });

            // TYPE
            cardBorrow.setOnClickListener(v -> {
                selectedType = DebtLoanType.BORROW;
                updateTypeSelection();
            });

            cardLent.setOnClickListener(v -> {
                selectedType = DebtLoanType.LENT;
                updateTypeSelection();
            });

            // NAME
            etName.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    maxLimitName.setText(getString(R.string.character_limit, charSequence.length()));
                    updateSaveButtonState();
                }
            });

            // TITLE
            etTitle.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    maxLimitTitle.setText(getString(R.string.character_limit_35, charSequence.length()));
                    updateSaveButtonState();
                }
            });

            // WALLET
            tvWallet.setOnClickListener(v -> selectWallets());

            cardWallet.setOnClickListener(v -> selectWallets());

            // AMOUNT
            tvAmount.setOnClickListener(view -> {

                hideKeyboard(this);

                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", debtLoanPrincipalAmount);
                intent.putExtra("type", "amount");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                calculatorLauncher.launch(intent, options);
            });

            cardAmount.setOnClickListener(view -> tvAmount.performClick());

            // START DATE
            tvStartDate.setOnClickListener(v -> {
                selectedDateType = DATE_TYPE_START;
                openDateDialog();
            });

            // DUE DATE
            tvDueDate.setOnClickListener(v -> {
                selectedDateType = DATE_TYPE_DUE;
                openDateDialog();
            });

            ivDueDateSelect.setOnClickListener(v -> {
                if (dueDate != null && !tvDueDate.getText().toString().trim().isEmpty()) {
                    dueDate = null;
                    tvDueDate.setText("");
                    ivDueDateSelect.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_account_caret));
                    calculateSelectedInterest();
                    updateInterestSummary();
                    updateRepaymentSchedule();
                    updateSaveButtonState();
                }
            });

            // INTEREST
            layoutInterestWrapper.setOnClickListener(v -> showInterestPicker());

            tvInterest.setOnClickListener(v -> showInterestPicker());

            // INTEREST AMOUNT
            etInterestAmount.setOnClickListener(v -> {
                hideKeyboard(this);

                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", debtLoanInterestAmount);
                intent.putExtra("type", "interest_amount");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                calculatorLauncher.launch(intent, options);
            });

            // INTEREST PERIOD
            etInterestPeriod.setOnClickListener(v -> showInterestPeriodPicker());

            // INTEREST CALC METHOD
            etCalculationMethod.setOnClickListener(v -> showInterestCalcMethodPicker());

            // INTEREST COMP FREQUENCY
            etCompoundFrequency.setOnClickListener(v -> showInterestCompFrequencyPicker());

            // INTEREST DURATION
            etInterestDuration.setOnClickListener(v -> showInterestDurationPicker());

            etInterestRate.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    calculateSelectedInterest();
                    updateInstallmentAmount();
                    updateRepaymentSchedule();
                    updateSaveButtonState();
                }
            });

            etCustomInterestDuration.setOnClickListener(v -> showCustomInterestDurationPicker());

            // REPAYMENT
            layoutRepaymentWrapper.setOnClickListener(v -> showRepaymentPicker());

            tvRepaymentMethod.setOnClickListener(v -> showRepaymentPicker());

            // INSTALLMENTS
            etNoOfInstallments.setOnClickListener(v -> showNoOfInstallmentsPicker());

            // PAYMENT FREQUENCY
            etPaymentFrequency.setOnClickListener(v -> showPaymentFrequencyPicker());

            // FIRST PAYMENT DATE
            etFirstPaymentDate.setOnClickListener(v -> {
                selectedDateType = DATE_TYPE_FIRST_PAYMENT;
                showFirstPaymentDatePicker();
            });

            // REMINDER
            cardReminder.setOnClickListener(v -> {

            });

            tvReminder.setOnClickListener(v -> {

            });

            // COLOR
            cardColor.setOnClickListener(view -> {
                prepareBottomSheet();
                colorSpinner.requestFocus();
                colorSpinner.performClick();
            });

            frameColor.setOnClickListener(view -> {
                prepareBottomSheet();
                colorSpinner.requestFocus();
                colorSpinner.performClick();
            });

            // ICON
            cardIcon.setOnClickListener(v -> {
                hideKeyboard(this);
                Intent intent = new Intent(this, IconPickerActivity.class);
                intent.putExtra("selectedColor", debtColorLists.get(colorSpinner.getSelectedItemPosition()));
                intent.putExtra("iconType", "debt");
                intent.putExtra("selectedIcon", debtIcon);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.left_to_right, R.anim.scale_out);
                iconLauncher.launch(intent, options);
            });

            layoutIcon.setOnClickListener(v -> {
                hideKeyboard(this);
                Intent intent = new Intent(this, IconPickerActivity.class);
                intent.putExtra("selectedColor", debtColorLists.get(colorSpinner.getSelectedItemPosition()));
                intent.putExtra("iconType", "debt");
                intent.putExtra("selectedIcon", debtIcon);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.left_to_right, R.anim.scale_out);
                iconLauncher.launch(intent, options);
            });

            // NOTES
            etNotes.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    maxLimitNote.setText(getString(R.string.character_limit_50, charSequence.length()));
                }
            });

            // SAVE
            tvSave.setOnClickListener(v -> {
                if (!tvSave.isEnabled()) {
                    return;
                }

                saveDebtLoan();
            });

            debtLoanViewModel.getDataSavedStatus().observe(this, aBoolean -> {
                tvSave.setEnabled(true);
                if (Boolean.TRUE.equals(aBoolean)) {

                    if (selectedType == DebtLoanType.BORROW) {
                        Toast.makeText(getApplicationContext(), getString(R.string.debt_created), Toast.LENGTH_SHORT).show();
                    } else if (selectedType == DebtLoanType.LENT) {
                        Toast.makeText(getApplicationContext(), getString(R.string.loan_created), Toast.LENGTH_SHORT).show();
                    }

                    setResult(Activity.RESULT_OK);
                    finishWithTransitions();
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void setupLauncher() {
        try {
            calculatorLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        double amount = data.getDoubleExtra("amount", 0);
                        String type = data.getStringExtra("type");

                        if (type != null && type.equalsIgnoreCase("amount")) {
                            debtLoanPrincipalAmount = amount;
                        } else if (type != null && type.equalsIgnoreCase("interest_amount")) {
                            debtLoanInterestAmount = amount;
                        }

                        calculateSelectedInterest();
                        updateAmountText();
                        updateInstallmentAmount();
                        updateRepaymentSchedule();
                        updateSaveButtonState();
                    }
                }
            });

            iconLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        int selectedCategoryIcon = data.getIntExtra("debtIcon", 0);
                        ivDebtIcon.setImageResource(DataHelper.getCategoryIcons().get(selectedCategoryIcon));
                        debtIcon = selectedCategoryIcon;
                    }
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupLauncher", e);
        }
    }

    private void updateTypeSelection() {
        try {

            if (selectedType == DebtLoanType.BORROW) {

                // =====================================================
                // BORROW SELECTED
                // =====================================================
                cardBorrow.setCardBackgroundColor(getColor(R.color.very_light_lavender));
                cardBorrow.setStrokeColor(getColor(R.color.primary_light));
                cardBorrow.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.stroke_width_1));

                cardLent.setCardBackgroundColor(getColor(R.color.white));
                cardLent.setStrokeColor(getColor(R.color.colorTransparent));
                cardLent.setStrokeWidth(0);

                ivBorrowSelected.setVisibility(View.VISIBLE);
                ivLentSelected.setVisibility(View.GONE);

                nameLabel.setText(getString(R.string.borrowed_from));
                moneyReceiveLabel.setText(getString(R.string.money_received_now));
                moneyReceiveHint.setText(getString(R.string.add_received_money));
                lblTotalAmount.setText(getString(R.string.total_to_pay));
            } else {
                // =====================================================
                // LENT SELECTED
                // =====================================================
                cardLent.setCardBackgroundColor(getColor(R.color.very_light_lavender));
                cardLent.setStrokeColor(getColor(R.color.primary_light));
                cardLent.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.stroke_width_1));

                cardBorrow.setCardBackgroundColor(getColor(R.color.white));
                cardBorrow.setStrokeColor(getColor(R.color.colorTransparent));
                cardBorrow.setStrokeWidth(0);

                ivLentSelected.setVisibility(View.VISIBLE);
                ivBorrowSelected.setVisibility(View.GONE);

                nameLabel.setText(getString(R.string.lent_to));
                moneyReceiveLabel.setText(getString(R.string.money_lent_now));
                moneyReceiveHint.setText(getString(R.string.deduct_lent_money));
                lblTotalAmount.setText(getString(R.string.total_to_receive));
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "updateSourceSelection", e);
        }
    }

    private void selectWallets() {
        try {

            prepareBottomSheet();

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_wallet_picker_layout, findViewById(android.R.id.content), false);
            RecyclerView rvWallets = bottomView.findViewById(R.id.rvWallets);
            View viewLine = bottomView.findViewById(R.id.viewLine);
            LinearLayout layoutAddWallet = bottomView.findViewById(R.id.layoutAddWallet);
            viewLine.setVisibility(View.GONE);
            layoutAddWallet.setVisibility(View.GONE);

            RecyclerViewAdapter<WalletEntity> adapter = new RecyclerViewAdapter<>(this, walletLists, R.layout.item_switch_accounts) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, WalletEntity walletEntity) {

                    holder.setViewText(R.id.tvAccountName, walletEntity.name);

                    holder.setViewText(R.id.tvAccountBalance, getString(R.string.account_balance_format,
                            CommonUtils.getBeautifyAmount(walletEntity.currencySymbol, walletEntity.amount)));

                    holder.getView(R.id.ivSelected).setVisibility(selectedWallet != null && selectedWallet.id == walletEntity.id ? View.VISIBLE : View.GONE);
                    holder.getView(R.id.rlAccountView).setOnClickListener(v -> {
                        selectedWallet = walletEntity;
                        tvWallet.setText(getString(R.string.wallet_info, selectedWallet.name,
                                CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol, selectedWallet.amount)));

                        updateAmountText();
                        updateSaveButtonState();
                        dialog.dismiss();
                    });

                }
            };

            rvWallets.setAdapter(adapter);
            rvWallets.setHasFixedSize(true);
            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "selectWallets", e);
        }
    }

    private void updateAmountText() {
        String symbol = selectedWallet != null ? selectedWallet.currencySymbol : account.currencySymbol;
        tvAmount.setText(CommonUtils.getBeautifyAmount(symbol, debtLoanPrincipalAmount));
        tvPrincipalAmount.setText(CommonUtils.getBeautifyAmount(symbol, debtLoanPrincipalAmount));

        etInterestAmount.setText(CommonUtils.getBeautifyAmount(symbol, debtLoanInterestAmount));
        tvInterestAmount.setText(CommonUtils.getBeautifyAmount(symbol, debtLoanInterestAmount));

        double debtLoanTotalAmount = debtLoanPrincipalAmount + debtLoanInterestAmount;
        tvTotalAmount.setText(CommonUtils.getBeautifyAmount(symbol, debtLoanTotalAmount));
    }

    // INTEREST PICKER
    private void showInterestPicker() {
        try {

            prepareBottomSheet();
            tempInterest = selectedInterest;

            List<FrequencyModel> frequencyList = new ArrayList<>();
            frequencyList.add(new FrequencyModel(DebtLoanType.DEBT_NO_INTEREST, R.drawable.ic_zero, getString(R.string.no_interest)));
            frequencyList.add(new FrequencyModel(DebtLoanType.DEBT_PERCENTAGE, R.drawable.ic_percentage, getString(R.string.interest_percentage)));
            frequencyList.add(new FrequencyModel(DebtLoanType.DEBT_FIXED_AMOUNT, R.drawable.ic_fixed_amount, getString(R.string.fixed_amount)));

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_calendar_filter_layout, findViewById(android.R.id.content), false);

            AppCompatTextView tvSelectRange = bottomView.findViewById(R.id.tvSelectRange);
            RecyclerView rvSelectRange = bottomView.findViewById(R.id.rvSelectRange);
            LinearLayout layoutActions = bottomView.findViewById(R.id.layoutActions);
            AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);
            MaterialButton btnPrimary = bottomView.findViewById(R.id.btnPrimary);
            MaterialButton btnSecondary = bottomView.findViewById(R.id.btnSecondary);

            tvSelectRange.setText(R.string.select_interest);
            layoutActions.setVisibility(View.VISIBLE);
            tvClose.setVisibility(View.VISIBLE);

            RecyclerViewAdapter<FrequencyModel> adapter = new RecyclerViewAdapter<>(this, frequencyList, R.layout.item_calendar_filter) {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onPostBindViewHolder(ViewHolder holder, FrequencyModel frequency) {
                    boolean selected = tempInterest == frequency.frequency;

                    holder.setViewText(R.id.tvFilterName, frequency.frequencyName);
                    holder.setViewImageDrawable(R.id.ivIcon, ContextCompat.getDrawable(getApplicationContext(), frequency.icon));

                    holder.setViewVisibility(R.id.ivSelected, selected ? View.VISIBLE : View.GONE);
                    holder.setViewTypeface(R.id.tvFilterName, selected ? semiBold : medium);

                    holder.getView(R.id.rlFilterView).setOnClickListener(v -> {
                        tempInterest = frequency.frequency;
                        notifyDataSetChanged();
                    });
                }
            };

            rvSelectRange.setAdapter(adapter);
            rvSelectRange.setHasFixedSize(true);
            rvSelectRange.setItemAnimator(null);

            btnPrimary.setOnClickListener(v -> {
                selectedInterest = tempInterest;
                updateInterestFields();
                dialog.dismiss();
            });

            btnSecondary.setOnClickListener(v -> {
                selectedInterest = DebtLoanType.DEBT_NO_INTEREST;
                updateInterestFields();
                dialog.dismiss();
            });

            tvClose.setOnClickListener(v -> dialog.dismiss());

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showFrequencyPicker", e);
        }
    }

    private void updateInterestFields() {

        String text = "";
        switch (selectedInterest) {
            case DebtLoanType.DEBT_NO_INTEREST:
                text = getString(R.string.no_interest);
                layoutInterestFields.setVisibility(View.GONE);
                debtLoanInterestAmount = 0;
                tvInterestRate.setText(getString(R.string.percentage_value, String.valueOf(debtLoanInterestAmount)));
                break;

            case DebtLoanType.DEBT_PERCENTAGE:
                text = getString(R.string.interest_percentage);
                layoutInterestFields.setVisibility(View.VISIBLE);

                tilInterestRate.setVisibility(View.VISIBLE);
                tilInterestPeriod.setVisibility(View.VISIBLE);
                tilCalculationMethod.setVisibility(View.VISIBLE);
                tilInterestDuration.setVisibility(View.VISIBLE);

                if (selectedInterestCalcMethodPeriod == DebtLoanType.INTEREST_CALC_CI) {
                    tilCompoundFrequency.setVisibility(View.VISIBLE);
                } else {
                    tilCompoundFrequency.setVisibility(View.GONE);
                }

                tilInterestAmount.setVisibility(View.GONE);
                break;

            case DebtLoanType.DEBT_FIXED_AMOUNT:
                text = getString(R.string.fixed_amount);
                layoutInterestFields.setVisibility(View.VISIBLE);

                tilInterestAmount.setVisibility(View.VISIBLE);

                tilInterestRate.setVisibility(View.GONE);
                tilInterestPeriod.setVisibility(View.GONE);
                tilCalculationMethod.setVisibility(View.GONE);
                tilCompoundFrequency.setVisibility(View.GONE);
                tilInterestDuration.setVisibility(View.GONE);

                debtLoanInterestAmount = 0;
                break;
        }

        tvInterest.setText(text);
        calculateSelectedInterest();
        updateAmountText();
        updateInterestSummary();
        updateRepaymentSchedule();
        updateSaveButtonState();
    }

    // INTEREST PERIOD
    private void showInterestPeriodPicker() {
        try {

            prepareBottomSheet();
            tempInterestPeriod = selectedInterestPeriod;

            List<FrequencyModel> frequencyList = new ArrayList<>();
            frequencyList.add(new FrequencyModel(DebtLoanType.INTEREST_PERIOD_DAY, R.drawable.ic_calendar_daily, getString(R.string.interest_per_day)));
            frequencyList.add(new FrequencyModel(DebtLoanType.INTEREST_PERIOD_WEEK, R.drawable.ic_calendar_weekly, getString(R.string.interest_per_week)));
            frequencyList.add(new FrequencyModel(DebtLoanType.INTEREST_PERIOD_MONTH, R.drawable.ic_calendar_monthly, getString(R.string.interest_per_month)));
            frequencyList.add(new FrequencyModel(DebtLoanType.INTEREST_PERIOD_YEAR, R.drawable.ic_yearly, getString(R.string.interest_per_year)));

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_calendar_filter_layout, findViewById(android.R.id.content), false);

            AppCompatTextView tvSelectRange = bottomView.findViewById(R.id.tvSelectRange);
            RecyclerView rvSelectRange = bottomView.findViewById(R.id.rvSelectRange);
            LinearLayout layoutActions = bottomView.findViewById(R.id.layoutActions);
            AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);
            MaterialButton btnPrimary = bottomView.findViewById(R.id.btnPrimary);
            MaterialButton btnSecondary = bottomView.findViewById(R.id.btnSecondary);

            tvSelectRange.setText(R.string.select_interest_period);
            layoutActions.setVisibility(View.VISIBLE);
            tvClose.setVisibility(View.VISIBLE);

            RecyclerViewAdapter<FrequencyModel> adapter = new RecyclerViewAdapter<>(this, frequencyList, R.layout.item_calendar_filter) {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onPostBindViewHolder(ViewHolder holder, FrequencyModel frequency) {
                    boolean selected = tempInterestPeriod == frequency.frequency;

                    holder.setViewText(R.id.tvFilterName, frequency.frequencyName);
                    holder.setViewImageDrawable(R.id.ivIcon, ContextCompat.getDrawable(getApplicationContext(), frequency.icon));

                    holder.setViewVisibility(R.id.ivSelected, selected ? View.VISIBLE : View.GONE);
                    holder.setViewTypeface(R.id.tvFilterName, selected ? semiBold : medium);

                    holder.getView(R.id.rlFilterView).setOnClickListener(v -> {
                        tempInterestPeriod = frequency.frequency;
                        notifyDataSetChanged();
                    });
                }
            };

            rvSelectRange.setAdapter(adapter);
            rvSelectRange.setHasFixedSize(true);
            rvSelectRange.setItemAnimator(null);

            btnPrimary.setOnClickListener(v -> {
                selectedInterestPeriod = tempInterestPeriod;
                updateInterestPeriodFields();
                dialog.dismiss();
            });

            btnSecondary.setOnClickListener(v -> {
                selectedInterestPeriod = DebtLoanType.INTEREST_PERIOD_YEAR;
                updateInterestPeriodFields();
                dialog.dismiss();
            });

            tvClose.setOnClickListener(v -> dialog.dismiss());

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showInterestPeriodPicker", e);
        }
    }

    private void updateInterestPeriodFields() {

        String text = switch (selectedInterestPeriod) {
            case DebtLoanType.INTEREST_PERIOD_DAY -> getString(R.string.interest_per_day);
            case DebtLoanType.INTEREST_PERIOD_WEEK -> getString(R.string.interest_per_week);
            case DebtLoanType.INTEREST_PERIOD_MONTH -> getString(R.string.interest_per_month);
            case DebtLoanType.INTEREST_PERIOD_YEAR -> getString(R.string.interest_per_year);
            default -> "";
        };

        etInterestPeriod.setText(text);
        calculateSelectedInterest();
        updateInterestSummary();
        updateRepaymentSchedule();
        updateSaveButtonState();
    }

    // INTEREST CALC METHOD
    private void showInterestCalcMethodPicker() {
        try {

            prepareBottomSheet();
            tempInterestCalcMethodPeriod = selectedInterestCalcMethodPeriod;

            List<FrequencyModel> frequencyList = new ArrayList<>();
            frequencyList.add(new FrequencyModel(DebtLoanType.INTEREST_CALC_SI, R.drawable.ic_simple_interest, getString(R.string.interest_simple_interest)));
            frequencyList.add(new FrequencyModel(DebtLoanType.INTEREST_CALC_FLAT_RATE, R.drawable.ic_flat_rate, getString(R.string.interest_flat_rate)));
            frequencyList.add(new FrequencyModel(DebtLoanType.INTEREST_CALC_REDUCE_BALANCE, R.drawable.ic_reducing_balance, getString(R.string.interest_reducing_balance)));
            frequencyList.add(new FrequencyModel(DebtLoanType.INTEREST_CALC_CI, R.drawable.ic_compound_interest, getString(R.string.interest_compound_interest)));

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_calendar_filter_layout, findViewById(android.R.id.content), false);

            AppCompatTextView tvSelectRange = bottomView.findViewById(R.id.tvSelectRange);
            RecyclerView rvSelectRange = bottomView.findViewById(R.id.rvSelectRange);
            LinearLayout layoutActions = bottomView.findViewById(R.id.layoutActions);
            AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);
            MaterialButton btnPrimary = bottomView.findViewById(R.id.btnPrimary);
            MaterialButton btnSecondary = bottomView.findViewById(R.id.btnSecondary);

            tvSelectRange.setText(R.string.select_calc_method);
            layoutActions.setVisibility(View.VISIBLE);
            tvClose.setVisibility(View.VISIBLE);

            RecyclerViewAdapter<FrequencyModel> adapter = new RecyclerViewAdapter<>(this, frequencyList, R.layout.item_calendar_filter) {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onPostBindViewHolder(ViewHolder holder, FrequencyModel frequency) {
                    boolean selected = tempInterestCalcMethodPeriod == frequency.frequency;

                    holder.setViewText(R.id.tvFilterName, frequency.frequencyName);
                    holder.setViewImageDrawable(R.id.ivIcon, ContextCompat.getDrawable(getApplicationContext(), frequency.icon));

                    holder.setViewVisibility(R.id.ivSelected, selected ? View.VISIBLE : View.GONE);
                    holder.setViewTypeface(R.id.tvFilterName, selected ? semiBold : medium);

                    holder.getView(R.id.rlFilterView).setOnClickListener(v -> {
                        tempInterestCalcMethodPeriod = frequency.frequency;
                        notifyDataSetChanged();
                    });
                }
            };

            rvSelectRange.setAdapter(adapter);
            rvSelectRange.setHasFixedSize(true);
            rvSelectRange.setItemAnimator(null);

            btnPrimary.setOnClickListener(v -> {
                selectedInterestCalcMethodPeriod = tempInterestCalcMethodPeriod;
                updateInterestCalcMethodFields();
                dialog.dismiss();
            });

            btnSecondary.setOnClickListener(v -> {
                selectedInterestCalcMethodPeriod = DebtLoanType.INTEREST_CALC_SI;
                updateInterestCalcMethodFields();
                dialog.dismiss();
            });

            tvClose.setOnClickListener(v -> dialog.dismiss());

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showInterestCalcMethodPicker", e);
        }
    }

    private void updateInterestCalcMethodFields() {

        String text = "";

        switch (selectedInterestCalcMethodPeriod) {
            case DebtLoanType.INTEREST_CALC_SI:
                text = getString(R.string.interest_simple_interest);
                tilCompoundFrequency.setVisibility(View.GONE);
                break;

            case DebtLoanType.INTEREST_CALC_FLAT_RATE:
                text = getString(R.string.interest_flat_rate);
                tilCompoundFrequency.setVisibility(View.GONE);
                break;

            case DebtLoanType.INTEREST_CALC_REDUCE_BALANCE:
                text = getString(R.string.interest_reducing_balance);
                tilCompoundFrequency.setVisibility(View.GONE);
                break;
            case DebtLoanType.INTEREST_CALC_CI:
                text = getString(R.string.interest_compound_interest);
                tilCompoundFrequency.setVisibility(View.VISIBLE);
                break;
        }

        etCalculationMethod.setText(text);
        calculateSelectedInterest();
        updateInterestSummary();
        updateRepaymentSchedule();
        updateSaveButtonState();
    }

    // COMPOUND FREQUENCY
    private void showInterestCompFrequencyPicker() {
        try {

            prepareBottomSheet();
            tempInterestCompFreqPeriod = selectedInterestCompFreqPeriod;

            List<FrequencyModel> frequencyList = new ArrayList<>();
            frequencyList.add(new FrequencyModel(DebtLoanType.INTEREST_CI_COMP_FREQ_DAILY, R.drawable.ic_calendar, getString(R.string.calendar_daily)));
            frequencyList.add(new FrequencyModel(DebtLoanType.INTEREST_CI_COMP_FREQ_WEEKLY, R.drawable.ic_calendar_weekly, getString(R.string.calendar_weekly)));
            frequencyList.add(new FrequencyModel(DebtLoanType.INTEREST_CI_COMP_FREQ_MONTHLY, R.drawable.ic_calendar_monthly, getString(R.string.calendar_monthly)));
            frequencyList.add(new FrequencyModel(DebtLoanType.INTEREST_CI_COMP_FREQ_QUARTERLY, R.drawable.ic_quarterly, getString(R.string.calendar_quarterly)));
            frequencyList.add(new FrequencyModel(DebtLoanType.INTEREST_CI_COMP_FREQ_HALF_YEARLY, R.drawable.ic_calendar_half_yearly, getString(R.string.calendar_half_yearly)));
            frequencyList.add(new FrequencyModel(DebtLoanType.INTEREST_CI_COMP_FREQ_YEARLY, R.drawable.ic_yearly, getString(R.string.calendar_yearly)));

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_calendar_filter_layout, findViewById(android.R.id.content), false);

            AppCompatTextView tvSelectRange = bottomView.findViewById(R.id.tvSelectRange);
            RecyclerView rvSelectRange = bottomView.findViewById(R.id.rvSelectRange);
            LinearLayout layoutActions = bottomView.findViewById(R.id.layoutActions);
            AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);
            MaterialButton btnPrimary = bottomView.findViewById(R.id.btnPrimary);
            MaterialButton btnSecondary = bottomView.findViewById(R.id.btnSecondary);

            tvSelectRange.setText(R.string.select_comp_frequency);
            layoutActions.setVisibility(View.VISIBLE);
            tvClose.setVisibility(View.VISIBLE);

            RecyclerViewAdapter<FrequencyModel> adapter = new RecyclerViewAdapter<>(this, frequencyList, R.layout.item_calendar_filter) {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onPostBindViewHolder(ViewHolder holder, FrequencyModel frequency) {
                    boolean selected = tempInterestCompFreqPeriod == frequency.frequency;

                    holder.setViewText(R.id.tvFilterName, frequency.frequencyName);
                    holder.setViewImageDrawable(R.id.ivIcon, ContextCompat.getDrawable(getApplicationContext(), frequency.icon));

                    holder.setViewVisibility(R.id.ivSelected, selected ? View.VISIBLE : View.GONE);
                    holder.setViewTypeface(R.id.tvFilterName, selected ? semiBold : medium);

                    holder.getView(R.id.rlFilterView).setOnClickListener(v -> {
                        tempInterestCompFreqPeriod = frequency.frequency;
                        notifyDataSetChanged();
                    });
                }
            };

            rvSelectRange.setAdapter(adapter);
            rvSelectRange.setHasFixedSize(true);
            rvSelectRange.setItemAnimator(null);

            btnPrimary.setOnClickListener(v -> {
                selectedInterestCompFreqPeriod = tempInterestCompFreqPeriod;
                updateInterestCompFrequencyFields();
                dialog.dismiss();
            });

            btnSecondary.setOnClickListener(v -> {
                selectedInterestCompFreqPeriod = DebtLoanType.INTEREST_CI_COMP_FREQ_MONTHLY;
                updateInterestCompFrequencyFields();
                dialog.dismiss();
            });

            tvClose.setOnClickListener(v -> dialog.dismiss());

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showInterestCompFrequencyPicker", e);
        }
    }

    private void updateInterestCompFrequencyFields() {

        String text = switch (selectedInterestCompFreqPeriod) {
            case DebtLoanType.INTEREST_CI_COMP_FREQ_DAILY -> getString(R.string.calendar_daily);
            case DebtLoanType.INTEREST_CI_COMP_FREQ_WEEKLY -> getString(R.string.calendar_weekly);
            case DebtLoanType.INTEREST_CI_COMP_FREQ_MONTHLY -> getString(R.string.calendar_monthly);
            case DebtLoanType.INTEREST_CI_COMP_FREQ_QUARTERLY ->
                    getString(R.string.calendar_quarterly);
            case DebtLoanType.INTEREST_CI_COMP_FREQ_HALF_YEARLY ->
                    getString(R.string.calendar_half_yearly);
            case DebtLoanType.INTEREST_CI_COMP_FREQ_YEARLY -> getString(R.string.calendar_yearly);
            default -> "";
        };

        etCompoundFrequency.setText(text);
        calculateSelectedInterest();
        updateAmountText();
        updateInterestSummary();
        updateRepaymentSchedule();
        updateSaveButtonState();
    }

    // INTEREST DURATION
    private void showInterestDurationPicker() {
        try {

            prepareBottomSheet();
            tempInterestDurationPeriod = selectedInterestDurationPeriod;

            List<FrequencyModel> frequencyList = new ArrayList<>();
            frequencyList.add(new FrequencyModel(DebtLoanType.INTEREST_LOAN_PERIOD, R.drawable.ic_calendar_empty, getString(R.string.same_loan_period)));
            frequencyList.add(new FrequencyModel(DebtLoanType.INTEREST_CUSTOM_PERIOD, R.drawable.ic_calendar_custom, getString(R.string.custom_period)));

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_calendar_filter_layout, findViewById(android.R.id.content), false);

            AppCompatTextView tvSelectRange = bottomView.findViewById(R.id.tvSelectRange);
            RecyclerView rvSelectRange = bottomView.findViewById(R.id.rvSelectRange);
            LinearLayout layoutActions = bottomView.findViewById(R.id.layoutActions);
            AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);
            MaterialButton btnPrimary = bottomView.findViewById(R.id.btnPrimary);
            MaterialButton btnSecondary = bottomView.findViewById(R.id.btnSecondary);

            tvSelectRange.setText(R.string.select_interest_duration);
            layoutActions.setVisibility(View.VISIBLE);
            tvClose.setVisibility(View.VISIBLE);

            RecyclerViewAdapter<FrequencyModel> adapter = new RecyclerViewAdapter<>(this, frequencyList, R.layout.item_calendar_filter) {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onPostBindViewHolder(ViewHolder holder, FrequencyModel frequency) {
                    boolean selected = tempInterestDurationPeriod == frequency.frequency;

                    holder.setViewText(R.id.tvFilterName, frequency.frequencyName);
                    holder.setViewImageDrawable(R.id.ivIcon, ContextCompat.getDrawable(getApplicationContext(), frequency.icon));

                    holder.setViewVisibility(R.id.ivSelected, selected ? View.VISIBLE : View.GONE);
                    holder.setViewTypeface(R.id.tvFilterName, selected ? semiBold : medium);

                    holder.getView(R.id.rlFilterView).setOnClickListener(v -> {
                        tempInterestDurationPeriod = frequency.frequency;
                        notifyDataSetChanged();
                    });
                }
            };

            rvSelectRange.setAdapter(adapter);
            rvSelectRange.setHasFixedSize(true);
            rvSelectRange.setItemAnimator(null);

            btnPrimary.setOnClickListener(v -> {
                selectedInterestDurationPeriod = tempInterestDurationPeriod;
                updateInterestDurationFields();
                dialog.dismiss();
            });

            btnSecondary.setOnClickListener(v -> {
                selectedInterestDurationPeriod = DebtLoanType.INTEREST_LOAN_PERIOD;
                updateInterestDurationFields();
                dialog.dismiss();
            });

            tvClose.setOnClickListener(v -> dialog.dismiss());

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showInterestDurationPicker", e);
        }
    }

    private void updateInterestDurationFields() {

        String text = switch (selectedInterestDurationPeriod) {
            case DebtLoanType.INTEREST_LOAN_PERIOD -> getString(R.string.same_loan_period);

            case DebtLoanType.INTEREST_CUSTOM_PERIOD -> getString(R.string.custom_period);

            default -> "";
        };

        etInterestDuration.setText(text);

        if (selectedInterestDurationPeriod == DebtLoanType.INTEREST_CUSTOM_PERIOD) {
            tilCustomInterestDuration.setVisibility(View.VISIBLE);
        } else {
            tilCustomInterestDuration.setVisibility(View.GONE);
            etCustomInterestDuration.setText("");
            customInterestDuration = 0;
            customInterestDurationPeriod = DebtLoanType.INTEREST_PERIOD_MONTH;
        }

        calculateSelectedInterest();
        updateInterestSummary();
        updateRepaymentSchedule();
        updateSaveButtonState();
    }

    // CALCULATE INTEREST
    private void calculateSelectedInterest() {

        if (selectedInterest == DebtLoanType.DEBT_NO_INTEREST) {
            debtLoanInterestAmount = 0;
            updateAmountText();
            updateInterestSummary();
            return;
        }

        if (selectedInterest == DebtLoanType.DEBT_FIXED_AMOUNT) {
            // Fixed amount is selected manually from CalculatorActivity.
            updateAmountText();
            updateInterestSummary();
            return;
        }

        if (selectedInterest != DebtLoanType.DEBT_PERCENTAGE) {
            debtLoanInterestAmount = 0;
            updateAmountText();
            return;
        }

        switch (selectedInterestCalcMethodPeriod) {
            case DebtLoanType.INTEREST_CALC_SI:
            case DebtLoanType.INTEREST_CALC_FLAT_RATE:
                calculateSimpleInterest();
                break;
            case DebtLoanType.INTEREST_CALC_CI:
                calculateCompoundInterest();
                break;
            case DebtLoanType.INTEREST_CALC_REDUCE_BALANCE:

                List<ReducingBalancePaymentModel> schedule;

                if (selectedRepaymentMethod == DebtLoanType.REPAYMENT_INSTALLMENTS) {
                    schedule = generateInstallmentSchedule();
                } else {
                    schedule = generateReducingBalanceSchedule();
                }

                debtLoanInterestAmount = 0;

                for (ReducingBalancePaymentModel payment : schedule) {
                    debtLoanInterestAmount += payment.interestAmount;
                }

                updateAmountText();
                updateInterestSummary();
                break;
            default:
                debtLoanInterestAmount = 0;
                updateAmountText();
                updateInterestSummary();
                break;
        }
    }

    private void calculateSimpleInterest() {

        if (debtLoanPrincipalAmount <= 0) {
            debtLoanInterestAmount = 0;
            updateAmountText();
            updateInterestSummary();
            return;
        }

        String rateText = etInterestRate.getText() != null ? etInterestRate.getText().toString().trim() : "";

        if (rateText.isEmpty() || rateText.equals(".")) {
            debtLoanInterestAmount = 0;
            updateAmountText();
            updateInterestSummary();
            return;
        }

        double rate;

        try {
            rate = Double.parseDouble(rateText);
        } catch (NumberFormatException e) {
            debtLoanInterestAmount = 0;
            updateAmountText();
            updateInterestSummary();
            return;
        }

        if (rate <= 0) {
            debtLoanInterestAmount = 0;
            updateAmountText();
            updateInterestSummary();
            return;
        }

        double duration;

        // CUSTOM INTEREST DURATION
        if (selectedInterestDurationPeriod == DebtLoanType.INTEREST_CUSTOM_PERIOD) {

            if (customInterestDuration <= 0) {
                debtLoanInterestAmount = 0;
                updateAmountText();
                updateInterestSummary();
                return;
            }
            duration = getDurationInInterestPeriod(customInterestDuration, customInterestDurationPeriod, selectedInterestPeriod);
        } else {
            duration = getLoanDurationInInterestPeriod();
        }

        if (duration <= 0) {
            debtLoanInterestAmount = 0;
            updateAmountText();
            updateInterestSummary();
            return;
        }

        debtLoanInterestAmount = debtLoanPrincipalAmount * (rate / 100.0) * duration;

        updateAmountText();
        updateInterestSummary();
    }

    private double getDurationInInterestPeriod(int duration, int durationPeriod, int interestPeriod) {
        double days;
        switch (durationPeriod) {
            case DebtLoanType.INTEREST_PERIOD_DAY:
                days = duration;
                break;
            case DebtLoanType.INTEREST_PERIOD_WEEK:
                days = duration * 7.0;
                break;
            case DebtLoanType.INTEREST_PERIOD_MONTH:
                days = duration * (365.0 / 12.0);
                break;
            case DebtLoanType.INTEREST_PERIOD_YEAR:
                days = duration * 365.0;
                break;
            default:
                return 0;
        }

        return switch (interestPeriod) {
            case DebtLoanType.INTEREST_PERIOD_DAY -> days;
            case DebtLoanType.INTEREST_PERIOD_WEEK -> days / 7.0;
            case DebtLoanType.INTEREST_PERIOD_MONTH -> days / (365.0 / 12.0);
            case DebtLoanType.INTEREST_PERIOD_YEAR -> days / 365.0;
            default -> 0;
        };
    }

    private double getCompoundPeriodsForCustomDuration() {

        if (customInterestDuration <= 0) {
            return 0;
        }

        return switch (customInterestDurationPeriod) {
            case DebtLoanType.INTEREST_PERIOD_DAY -> switch (selectedInterestCompFreqPeriod) {
                case DebtLoanType.INTEREST_CI_COMP_FREQ_DAILY -> customInterestDuration;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_WEEKLY -> customInterestDuration / 7.0;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_MONTHLY -> customInterestDuration / 30.0;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_QUARTERLY -> customInterestDuration / 90.0;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_HALF_YEARLY ->
                        customInterestDuration / 182.5;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_YEARLY -> customInterestDuration / 365.0;
                default -> 0;
            };
            case DebtLoanType.INTEREST_PERIOD_WEEK -> switch (selectedInterestCompFreqPeriod) {
                case DebtLoanType.INTEREST_CI_COMP_FREQ_DAILY -> customInterestDuration * 7.0;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_WEEKLY -> customInterestDuration;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_MONTHLY ->
                        customInterestDuration * 7.0 / 30.0;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_QUARTERLY ->
                        customInterestDuration * 7.0 / 90.0;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_HALF_YEARLY ->
                        customInterestDuration * 7.0 / 182.5;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_YEARLY ->
                        customInterestDuration * 7.0 / 365.0;
                default -> 0;
            };
            case DebtLoanType.INTEREST_PERIOD_MONTH -> switch (selectedInterestCompFreqPeriod) {
                case DebtLoanType.INTEREST_CI_COMP_FREQ_DAILY -> customInterestDuration * 30.0;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_WEEKLY ->
                        customInterestDuration * 30.0 / 7.0;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_MONTHLY -> customInterestDuration;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_QUARTERLY -> customInterestDuration / 3.0;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_HALF_YEARLY -> customInterestDuration / 6.0;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_YEARLY -> customInterestDuration / 12.0;
                default -> 0;
            };
            case DebtLoanType.INTEREST_PERIOD_YEAR -> switch (selectedInterestCompFreqPeriod) {
                case DebtLoanType.INTEREST_CI_COMP_FREQ_DAILY -> customInterestDuration * 365.0;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_WEEKLY -> customInterestDuration * 52.0;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_MONTHLY -> customInterestDuration * 12.0;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_QUARTERLY -> customInterestDuration * 4.0;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_HALF_YEARLY -> customInterestDuration * 2.0;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_YEARLY -> customInterestDuration;
                default -> 0;
            };
            default -> 0;
        };
    }

    private double getLoanDurationInInterestPeriod() {

        if (startDate == null || dueDate == null) {
            return 0;
        }

        long diffMillis = dueDate.getTime() - startDate.getTime();
        double days = diffMillis / (1000.0 * 60 * 60 * 24);

        return switch (selectedInterestPeriod) {
            case DebtLoanType.INTEREST_PERIOD_DAY -> days;
            case DebtLoanType.INTEREST_PERIOD_WEEK -> days / 7.0;
            case DebtLoanType.INTEREST_PERIOD_MONTH -> days / (365.0 / 12.0);
            case DebtLoanType.INTEREST_PERIOD_YEAR -> days / 365.0;
            default -> 0;
        };
    }

    private void calculateCompoundInterest() {
        try {

            if (selectedInterest != DebtLoanType.DEBT_PERCENTAGE) {
                return;
            }

            if (selectedInterestCalcMethodPeriod != DebtLoanType.INTEREST_CALC_CI) {
                return;
            }

            if (debtLoanPrincipalAmount <= 0) {
                debtLoanInterestAmount = 0;
                updateAmountText();
                updateInterestSummary();
                return;
            }

            String rateText = etInterestRate.getText() != null ? etInterestRate.getText().toString().trim() : "";

            if (rateText.isEmpty()) {
                debtLoanInterestAmount = 0;
                updateAmountText();
                updateInterestSummary();
                return;
            }

            double rate;

            try {
                rate = Double.parseDouble(rateText);
            } catch (NumberFormatException e) {
                debtLoanInterestAmount = 0;
                updateAmountText();
                updateInterestSummary();
                return;
            }

            if (rate < 0) {
                debtLoanInterestAmount = 0;
                updateAmountText();
                updateInterestSummary();
                return;
            }

            if (rate == 0) {
                debtLoanInterestAmount = 0;
                updateAmountText();
                updateInterestSummary();
                return;
            }

            double duration;
            if (selectedInterestDurationPeriod == DebtLoanType.INTEREST_CUSTOM_PERIOD) {

                if (customInterestDuration <= 0) {
                    debtLoanInterestAmount = 0;
                    updateAmountText();
                    updateInterestSummary();
                    return;
                }

                duration = getDurationInInterestPeriod(customInterestDuration, customInterestDurationPeriod, selectedInterestPeriod);

            } else {
                duration = getLoanDurationInInterestPeriod();
            }

            if (duration <= 0) {
                debtLoanInterestAmount = 0;
                updateAmountText();
                updateInterestSummary();
                return;
            }

            if (selectedInterestDurationPeriod == DebtLoanType.INTEREST_LOAN_PERIOD && (startDate == null || dueDate == null)) {
                debtLoanInterestAmount = 0;
                updateAmountText();
                updateInterestSummary();
                return;
            }

            double days;
            if (selectedInterestDurationPeriod != DebtLoanType.INTEREST_CUSTOM_PERIOD) {
                Calendar start = Calendar.getInstance();
                start.setTime(startDate);
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                start.set(Calendar.MILLISECOND, 0);

                Calendar due = Calendar.getInstance();
                due.setTime(dueDate);
                due.set(Calendar.HOUR_OF_DAY, 0);
                due.set(Calendar.MINUTE, 0);
                due.set(Calendar.SECOND, 0);
                due.set(Calendar.MILLISECOND, 0);

                long differenceMillis = due.getTimeInMillis() - start.getTimeInMillis();
                days = TimeUnit.MILLISECONDS.toDays(differenceMillis);

                if (days <= 0) {
                    debtLoanInterestAmount = 0;
                    updateAmountText();
                    updateInterestSummary();
                    return;
                }
            }

            double rateDecimal = rate / 100.0;
            double compoundRate;
            double interestPeriodsPerYear;

            switch (selectedInterestPeriod) {
                case DebtLoanType.INTEREST_PERIOD_DAY:
                    interestPeriodsPerYear = 365.0;
                    break;
                case DebtLoanType.INTEREST_PERIOD_WEEK:
                    interestPeriodsPerYear = 52.0;
                    break;
                case DebtLoanType.INTEREST_PERIOD_MONTH:
                    interestPeriodsPerYear = 12.0;
                    break;
                case DebtLoanType.INTEREST_PERIOD_YEAR:
                    interestPeriodsPerYear = 1.0;
                    break;
                default:
                    debtLoanInterestAmount = 0;
                    updateAmountText();
                    updateInterestSummary();
                    return;
            }

            double compoundPeriodsPerYear;
            switch (selectedInterestCompFreqPeriod) {
                case DebtLoanType.INTEREST_CI_COMP_FREQ_DAILY:
                    compoundPeriodsPerYear = 365.0;
                    break;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_WEEKLY:
                    compoundPeriodsPerYear = 52.0;
                    break;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_MONTHLY:
                    compoundPeriodsPerYear = 12.0;
                    break;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_QUARTERLY:
                    compoundPeriodsPerYear = 4.0;
                    break;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_HALF_YEARLY:
                    compoundPeriodsPerYear = 2.0;
                    break;
                case DebtLoanType.INTEREST_CI_COMP_FREQ_YEARLY:
                    compoundPeriodsPerYear = 1.0;
                    break;
                default:
                    debtLoanInterestAmount = 0;
                    updateAmountText();
                    updateInterestSummary();
                    return;
            }

            compoundRate = rateDecimal * (interestPeriodsPerYear / compoundPeriodsPerYear);

            double numberOfCompoundPeriods;

            if (selectedInterestDurationPeriod == DebtLoanType.INTEREST_CUSTOM_PERIOD) {
                numberOfCompoundPeriods = getCompoundPeriodsForCustomDuration();
            } else {
                Calendar start = Calendar.getInstance();
                start.setTime(startDate);

                Calendar due = Calendar.getInstance();
                due.setTime(dueDate);

                switch (selectedInterestCompFreqPeriod) {
                    case DebtLoanType.INTEREST_CI_COMP_FREQ_DAILY:
                        numberOfCompoundPeriods = TimeUnit.MILLISECONDS.toDays(due.getTimeInMillis() - start.getTimeInMillis());
                        break;
                    case DebtLoanType.INTEREST_CI_COMP_FREQ_WEEKLY:
                        long weeklyDays = TimeUnit.MILLISECONDS.toDays(due.getTimeInMillis() - start.getTimeInMillis());
                        numberOfCompoundPeriods = weeklyDays / 7.0;
                        break;
                    case DebtLoanType.INTEREST_CI_COMP_FREQ_MONTHLY:
                        int months = (due.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12 + due.get(Calendar.MONTH) - start.get(Calendar.MONTH);
                        Calendar monthlyDue = (Calendar) start.clone();
                        monthlyDue.add(Calendar.MONTH, months);
                        if (monthlyDue.after(due)) {
                            months--;
                        }
                        numberOfCompoundPeriods = months;
                        break;
                    case DebtLoanType.INTEREST_CI_COMP_FREQ_QUARTERLY:
                        int quarterlyMonths = (due.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12 + due.get(Calendar.MONTH) - start.get(Calendar.MONTH);
                        numberOfCompoundPeriods = quarterlyMonths / 3.0;
                        break;
                    case DebtLoanType.INTEREST_CI_COMP_FREQ_HALF_YEARLY:
                        int halfYearMonths = (due.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12 + due.get(Calendar.MONTH) - start.get(Calendar.MONTH);
                        numberOfCompoundPeriods = halfYearMonths / 6.0;
                        break;
                    case DebtLoanType.INTEREST_CI_COMP_FREQ_YEARLY:
                        int yearlyMonths = (due.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12 + due.get(Calendar.MONTH) - start.get(Calendar.MONTH);
                        numberOfCompoundPeriods = yearlyMonths / 12.0;
                        break;
                    default:
                        numberOfCompoundPeriods = 0;
                        break;
                }
            }

            if (numberOfCompoundPeriods <= 0) {
                debtLoanInterestAmount = 0;
                updateAmountText();
                updateInterestSummary();
                return;
            }

            double totalAmount = debtLoanPrincipalAmount * Math.pow(1.0 + compoundRate, numberOfCompoundPeriods);

            debtLoanInterestAmount = totalAmount - debtLoanPrincipalAmount;

            if (debtLoanInterestAmount < 0) {
                debtLoanInterestAmount = 0;
            }

            updateAmountText();
            updateInterestSummary();
        } catch (Exception e) {
            AppLogger.e(getClass(), "calculateCompoundInterest", e);
            debtLoanInterestAmount = 0;
            updateAmountText();
            updateInterestSummary();
        }
    }

    private String getLoanDurationText() {

        if (startDate == null || dueDate == null) {
            return getString(R.string.not_set);
        }

        Calendar start = Calendar.getInstance();
        start.setTime(startDate);

        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar due = Calendar.getInstance();
        due.setTime(dueDate);

        due.set(Calendar.HOUR_OF_DAY, 0);
        due.set(Calendar.MINUTE, 0);
        due.set(Calendar.SECOND, 0);
        due.set(Calendar.MILLISECOND, 0);

        long differenceMillis = due.getTimeInMillis() - start.getTimeInMillis();
        long days = TimeUnit.MILLISECONDS.toDays(differenceMillis);

        if (days <= 0) {
            return getResources().getQuantityString(R.plurals.days_period, 0, 0);
        }

        return getResources().getQuantityString(R.plurals.days_period, (int) days, (int) days);
    }

    private void updateInterestSummary() {

        if (selectedInterest == DebtLoanType.DEBT_NO_INTEREST || selectedInterest == DebtLoanType.DEBT_FIXED_AMOUNT) {
            layoutInterestRate.setVisibility(View.GONE);
            layoutInterestPeriod.setVisibility(View.GONE);
            layoutCalcMethod.setVisibility(View.GONE);
            layoutDuration.setVisibility(View.GONE);
            layoutCompoundFrequency.setVisibility(View.GONE);

            viewInterest.setVisibility(View.GONE);

            if (selectedInterest == DebtLoanType.DEBT_NO_INTEREST) {
                debtLoanInterestAmount = 0;
            }
        } else {
            layoutInterestRate.setVisibility(View.VISIBLE);
            String rateText = etInterestRate.getText() != null ? etInterestRate.getText().toString().trim() : "";
            if (rateText.isEmpty()) {
                tvInterestRate.setText(getString(R.string.percentage_value, "0"));
            } else {
                try {
                    double rate = Double.parseDouble(rateText);
                    if (rate == Math.floor(rate)) {
                        tvInterestRate.setText(getString(R.string.percentage_value, String.valueOf((long) rate)));
                    } else {
                        tvInterestRate.setText(getString(R.string.percentage_value, rateText));
                    }
                } catch (NumberFormatException e) {
                    tvInterestRate.setText(getString(R.string.percentage_value, "0"));
                }
            }

            layoutInterestPeriod.setVisibility(View.VISIBLE);
            tvInterestPeriod.setText(etInterestPeriod.getText() != null ? etInterestPeriod.getText().toString().trim() : "");

            layoutCalcMethod.setVisibility(View.VISIBLE);
            tvCalcMethod.setText(etCalculationMethod.getText() != null ? etCalculationMethod.getText().toString().trim() : "");

            if (selectedInterestCalcMethodPeriod == DebtLoanType.INTEREST_CALC_CI) {
                layoutCompoundFrequency.setVisibility(View.VISIBLE);
                tvCompoundFrequency.setText(etCompoundFrequency.getText() != null ? etCompoundFrequency.getText().toString().trim() : "");
            } else {
                layoutCompoundFrequency.setVisibility(View.GONE);
            }

            layoutDuration.setVisibility(View.VISIBLE);
            if (selectedInterestDurationPeriod == DebtLoanType.INTEREST_CUSTOM_PERIOD) {
                if (customInterestDuration > 0) {
                    String period = switch (customInterestDurationPeriod) {
                        case DebtLoanType.INTEREST_PERIOD_DAY -> getString(R.string.day);
                        case DebtLoanType.INTEREST_PERIOD_WEEK -> getString(R.string.week_text);
                        case DebtLoanType.INTEREST_PERIOD_MONTH -> getString(R.string.month_text);
                        case DebtLoanType.INTEREST_PERIOD_YEAR -> getString(R.string.year_text);
                        default -> "";
                    };
                    tvDuration.setText(getString(R.string.interest_duration_format, customInterestDuration, period));
                } else {
                    tvDuration.setText(getString(R.string.not_set));
                }
            } else {
                tvDuration.setText(getLoanDurationText());
            }

            viewInterest.setVisibility(View.VISIBLE);
        }

        updateAmountText();
    }

    // REPAYMENT PICKER
    private void showRepaymentPicker() {
        try {

            prepareBottomSheet();
            tempRepaymentMethod = selectedRepaymentMethod;

            List<FrequencyModel> frequencyList = new ArrayList<>();
            frequencyList.add(new FrequencyModel(DebtLoanType.REPAYMENT_FLEXIBLE, R.drawable.ic_transaction, getString(R.string.flexible)));
            frequencyList.add(new FrequencyModel(DebtLoanType.REPAYMENT_INSTALLMENTS, R.drawable.ic_smart_tracking, getString(R.string.installments)));
            frequencyList.add(new FrequencyModel(DebtLoanType.REPAYMENT_EMI, R.drawable.ic_calendar_statement, getString(R.string.emi)));

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_calendar_filter_layout, findViewById(android.R.id.content), false);

            AppCompatTextView tvSelectRange = bottomView.findViewById(R.id.tvSelectRange);
            RecyclerView rvSelectRange = bottomView.findViewById(R.id.rvSelectRange);
            LinearLayout layoutActions = bottomView.findViewById(R.id.layoutActions);
            AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);
            MaterialButton btnPrimary = bottomView.findViewById(R.id.btnPrimary);
            MaterialButton btnSecondary = bottomView.findViewById(R.id.btnSecondary);

            tvSelectRange.setText(R.string.select_repayment_method);
            layoutActions.setVisibility(View.VISIBLE);
            tvClose.setVisibility(View.VISIBLE);

            RecyclerViewAdapter<FrequencyModel> adapter = new RecyclerViewAdapter<>(this, frequencyList, R.layout.item_calendar_filter) {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onPostBindViewHolder(ViewHolder holder, FrequencyModel frequency) {
                    boolean selected = tempRepaymentMethod == frequency.frequency;

                    holder.setViewText(R.id.tvFilterName, frequency.frequencyName);
                    holder.setViewImageDrawable(R.id.ivIcon, ContextCompat.getDrawable(getApplicationContext(), frequency.icon));

                    holder.setViewVisibility(R.id.ivSelected, selected ? View.VISIBLE : View.GONE);
                    holder.setViewTypeface(R.id.tvFilterName, selected ? semiBold : medium);

                    holder.getView(R.id.rlFilterView).setOnClickListener(v -> {
                        tempRepaymentMethod = frequency.frequency;
                        notifyDataSetChanged();
                    });
                }
            };

            rvSelectRange.setAdapter(adapter);
            rvSelectRange.setHasFixedSize(true);
            rvSelectRange.setItemAnimator(null);

            btnPrimary.setOnClickListener(v -> {
                selectedRepaymentMethod = tempRepaymentMethod;
                updateRepaymentFields();
                dialog.dismiss();
            });

            btnSecondary.setOnClickListener(v -> {
                selectedRepaymentMethod = DebtLoanType.REPAYMENT_FLEXIBLE;
                updateRepaymentFields();
                dialog.dismiss();
            });

            tvClose.setOnClickListener(v -> dialog.dismiss());

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showRepaymentPicker", e);
        }
    }

    private void updateRepaymentFields() {
        String text = "";
        switch (selectedRepaymentMethod) {
            case DebtLoanType.REPAYMENT_FLEXIBLE:
                text = getString(R.string.flexible);
                layoutRepaymentFields.setVisibility(View.GONE);
                break;
            case DebtLoanType.REPAYMENT_INSTALLMENTS:
                text = getString(R.string.installments);
                layoutRepaymentFields.setVisibility(View.VISIBLE);
                break;
            case DebtLoanType.REPAYMENT_EMI:
                text = getString(R.string.emi);
                layoutRepaymentFields.setVisibility(View.VISIBLE);
                break;
        }

        tvRepaymentMethod.setText(text);
        calculateSelectedInterest();
        updateInstallmentAmount();
        updateRepaymentSchedule();
        updateSaveButtonState();
    }

    private void showNoOfInstallmentsPicker() {

        prepareBottomSheet();
        View bottomView = getLayoutInflater().inflate(R.layout.bottom_number_picker, findViewById(android.R.id.content), false);

        CustomNumberPicker numberPicker = bottomView.findViewById(R.id.numberPicker);
        AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);
        MaterialButton tvDone = bottomView.findViewById(R.id.btnDone);

        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(600);
        numberPicker.setValue(tempNoOfInstallments > 0 ? tempNoOfInstallments : 1);
        Typeface typeface = ResourcesCompat.getFont(this, R.font.exo2_semibold);

        numberPicker.setCustomTypeface(typeface);

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(bottomView);

        tvClose.setOnClickListener(v -> dialog.dismiss());

        tvDone.setOnClickListener(v -> {
            tempNoOfInstallments = numberPicker.getValue();
            selectedNoOfInstallments = tempNoOfInstallments;
            etNoOfInstallments.setText(String.valueOf(selectedNoOfInstallments));
            calculateSelectedInterest();
            updateInstallmentAmount();
            updateRepaymentSchedule();
            updateSaveButtonState();
            dialog.dismiss();
        });

        dialog.show();
    }

    // REPAYMENT FREQUENCY
    private void showPaymentFrequencyPicker() {
        try {

            prepareBottomSheet();
            tempRepaymentFrequency = selectedRepaymentFrequency;

            List<FrequencyModel> frequencyList = new ArrayList<>();
            frequencyList.add(new FrequencyModel(DebtLoanType.REPAYMENT_FREQ_DAILY, R.drawable.ic_calendar_daily, getString(R.string.calendar_daily)));
            frequencyList.add(new FrequencyModel(DebtLoanType.REPAYMENT_FREQ_WEEKLY, R.drawable.ic_calendar_weekly, getString(R.string.calendar_weekly)));
            frequencyList.add(new FrequencyModel(DebtLoanType.REPAYMENT_FREQ_BIWEEKLY, R.drawable.ic_calendar_weekly, getString(R.string.calendar_biweekly)));
            frequencyList.add(new FrequencyModel(DebtLoanType.REPAYMENT_FREQ_MONTHLY, R.drawable.ic_calendar_monthly, getString(R.string.calendar_monthly)));
            frequencyList.add(new FrequencyModel(DebtLoanType.REPAYMENT_FREQ_QUARTERLY, R.drawable.ic_quarterly, getString(R.string.calendar_quarterly)));
            frequencyList.add(new FrequencyModel(DebtLoanType.REPAYMENT_FREQ_YEARLY, R.drawable.ic_yearly, getString(R.string.calendar_yearly)));

            BottomSheetDialog dialog = new BottomSheetDialog(this);

            View bottomView = getLayoutInflater().inflate(R.layout.bottom_calendar_filter_layout, findViewById(android.R.id.content), false);
            AppCompatTextView tvSelectRange = bottomView.findViewById(R.id.tvSelectRange);
            RecyclerView rvSelectRange = bottomView.findViewById(R.id.rvSelectRange);
            LinearLayout layoutActions = bottomView.findViewById(R.id.layoutActions);
            AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);
            MaterialButton btnPrimary = bottomView.findViewById(R.id.btnPrimary);
            MaterialButton btnSecondary = bottomView.findViewById(R.id.btnSecondary);

            tvSelectRange.setText(getString(R.string.select_frequency));
            layoutActions.setVisibility(View.VISIBLE);
            tvClose.setVisibility(View.VISIBLE);

            RecyclerViewAdapter<FrequencyModel> adapter = new RecyclerViewAdapter<>(this, frequencyList, R.layout.item_calendar_filter) {

                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onPostBindViewHolder(ViewHolder holder, FrequencyModel frequency) {
                    boolean selected = tempRepaymentFrequency == frequency.frequency;
                    holder.setViewText(R.id.tvFilterName, frequency.frequencyName);
                    holder.setViewImageDrawable(R.id.ivIcon, ContextCompat.getDrawable(getApplicationContext(), frequency.icon));

                    holder.setViewVisibility(R.id.ivSelected, selected ? View.VISIBLE : View.GONE);
                    holder.setViewTypeface(R.id.tvFilterName, selected ? semiBold : medium);

                    holder.getView(R.id.rlFilterView).setOnClickListener(v -> {
                        tempRepaymentFrequency = frequency.frequency;
                        notifyDataSetChanged();
                    });
                }
            };

            rvSelectRange.setAdapter(adapter);
            rvSelectRange.setHasFixedSize(true);
            rvSelectRange.setItemAnimator(null);

            btnPrimary.setOnClickListener(v -> {
                selectedRepaymentFrequency = tempRepaymentFrequency;
                updatePaymentFrequency();
                calculateSelectedInterest();
                updateInstallmentAmount();
                updateRepaymentSchedule();
                updateSaveButtonState();
                dialog.dismiss();
            });

            btnSecondary.setOnClickListener(v -> {
                selectedRepaymentFrequency = DebtLoanType.REPAYMENT_FREQ_MONTHLY;
                tempRepaymentFrequency = DebtLoanType.REPAYMENT_FREQ_MONTHLY;
                updatePaymentFrequency();
                calculateSelectedInterest();
                updateInstallmentAmount();
                updateRepaymentSchedule();
                updateSaveButtonState();
                dialog.dismiss();
            });

            tvClose.setOnClickListener(v -> dialog.dismiss());
            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showPaymentFrequencyPicker", e);
        }
    }

    private void updatePaymentFrequency() {

        String text = switch (selectedRepaymentFrequency) {
            case DebtLoanType.REPAYMENT_FREQ_DAILY -> getString(R.string.calendar_daily);
            case DebtLoanType.REPAYMENT_FREQ_WEEKLY -> getString(R.string.calendar_weekly);
            case DebtLoanType.REPAYMENT_FREQ_BIWEEKLY -> getString(R.string.calendar_biweekly);
            case DebtLoanType.REPAYMENT_FREQ_MONTHLY -> getString(R.string.calendar_monthly);
            case DebtLoanType.REPAYMENT_FREQ_QUARTERLY -> getString(R.string.calendar_quarterly);
            case DebtLoanType.REPAYMENT_FREQ_YEARLY -> getString(R.string.calendar_yearly);
            default -> "";
        };

        etPaymentFrequency.setText(text);
        updateRepaymentSchedule();
    }

    public void openDateDialog() {
        try {
            Calendar calendar = getCalendar();

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.CustomDateTimePickerDialog, this,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            // Due date cannot be before start date
            if (selectedDateType == DATE_TYPE_DUE && startDate != null) {
                datePickerDialog.getDatePicker().setMinDate(startDate.getTime());
            }

            datePickerDialog.show();

            int color = ContextCompat.getColor(this, R.color.vibrant_orange);
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(color);
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(color);
        } catch (Exception e) {
            AppLogger.e(getClass(), "openDateDialog", e);
        }
    }

    private void showFirstPaymentDatePicker() {
        try {

            prepareBottomSheet();

            Calendar calendar = Calendar.getInstance();

            if (firstPaymentDate != null) {
                calendar.setTime(firstPaymentDate);
            } else if (startDate != null) {
                calendar.setTime(startDate);
            }

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.CustomDateTimePickerDialog, this,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.getDatePicker().setMinDate(startDate.getTime());
            datePickerDialog.show();

            int color = ContextCompat.getColor(this, R.color.vibrant_orange);
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(color);
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(color);
        } catch (Exception e) {
            AppLogger.e(getClass(), "showFirstPaymentDatePicker", e);
        }
    }

    private void showCustomInterestDurationPicker() {

        prepareBottomSheet();
        View bottomView = getLayoutInflater().inflate(R.layout.bottom_custom_period_picker, findViewById(android.R.id.content), false);

        CustomNumberPicker numberPicker = bottomView.findViewById(R.id.numberPickerDuration);
        CustomNumberPicker periodPicker = bottomView.findViewById(R.id.numberPickerPeriod);
        AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);
        MaterialButton tvDone = bottomView.findViewById(R.id.btnDone);

        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(999);
        numberPicker.setValue(customInterestDuration > 0 ? customInterestDuration : 1);

        String[] periods = {getString(R.string.day), getString(R.string.week_text), getString(R.string.month_text), getString(R.string.year_text)};

        periodPicker.setDisplayedValues(periods);
        periodPicker.setMinValue(1);
        periodPicker.setMaxValue(periods.length);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.exo2_semibold);

        numberPicker.setCustomTypeface(typeface);
        periodPicker.setCustomTypeface(typeface);

        int periodValue = switch (customInterestDurationPeriod) {
            case DebtLoanType.INTEREST_PERIOD_DAY -> 1;
            case DebtLoanType.INTEREST_PERIOD_WEEK -> 2;
            case DebtLoanType.INTEREST_PERIOD_YEAR -> 4;
            default -> 3;
        };

        periodPicker.setValue(periodValue);

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(bottomView);

        tvClose.setOnClickListener(v -> dialog.dismiss());

        tvDone.setOnClickListener(v -> {
            customInterestDuration = numberPicker.getValue();
            customInterestDurationPeriod = switch (periodPicker.getValue()) {
                case 1 -> DebtLoanType.INTEREST_PERIOD_DAY;
                case 2 -> DebtLoanType.INTEREST_PERIOD_WEEK;
                case 4 -> DebtLoanType.INTEREST_PERIOD_YEAR;
                default -> DebtLoanType.INTEREST_PERIOD_MONTH;
            };
            updateCustomInterestDuration();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateCustomInterestDuration() {

        if (customInterestDuration <= 0) {
            etCustomInterestDuration.setText("");
            return;
        }

        String period = switch (customInterestDurationPeriod) {
            case DebtLoanType.INTEREST_PERIOD_DAY -> getString(R.string.day);
            case DebtLoanType.INTEREST_PERIOD_WEEK -> getString(R.string.week_text);
            case DebtLoanType.INTEREST_PERIOD_MONTH -> getString(R.string.month_text);
            case DebtLoanType.INTEREST_PERIOD_YEAR -> getString(R.string.year_text);
            default -> "";
        };

        etCustomInterestDuration.setText(getString(R.string.interest_duration_format, customInterestDuration, period));

        calculateSelectedInterest();
        updateInterestSummary();
        updateRepaymentSchedule();
        updateSaveButtonState();
    }

    private void updateInstallmentAmount() {

        if (selectedRepaymentMethod == DebtLoanType.REPAYMENT_FLEXIBLE) {
            etInstallmentAmount.setText("");
            return;
        }

        if (selectedNoOfInstallments <= 0 || debtLoanPrincipalAmount <= 0) {
            etInstallmentAmount.setText("");
            return;
        }

        double installmentAmount = 0;

        switch (selectedRepaymentMethod) {
            case DebtLoanType.REPAYMENT_INSTALLMENTS:
                List<ReducingBalancePaymentModel> schedule = generateInstallmentSchedule();
                if (!schedule.isEmpty()) {
                    installmentAmount = schedule.get(0).paymentAmount;
                }
                break;
            case DebtLoanType.REPAYMENT_EMI:
                installmentAmount = calculateEMI();
                break;
        }

        String symbol = selectedWallet != null ? selectedWallet.currencySymbol : account.currencySymbol;
        etInstallmentAmount.setText(CommonUtils.getBeautifyAmount(symbol, installmentAmount));
    }

    private double calculateEMI() {

        if (debtLoanPrincipalAmount <= 0 || selectedNoOfInstallments <= 0) {
            return 0;
        }

        // No Interest → simple division
        if (selectedInterest == DebtLoanType.DEBT_NO_INTEREST) {
            return debtLoanPrincipalAmount / selectedNoOfInstallments;
        }

        String rateText = etInterestRate.getText() != null ? etInterestRate.getText().toString().trim() : "";

        if (rateText.isEmpty() || rateText.equals(".")) {
            return debtLoanPrincipalAmount / selectedNoOfInstallments;
        }

        double rate;

        try {
            // Handles values such as "10."
            rate = Double.parseDouble(rateText);
        } catch (NumberFormatException e) {
            return 0;
        }

        // Zero interest
        if (rate <= 0) {
            return debtLoanPrincipalAmount / selectedNoOfInstallments;
        }

        double periodicRate = getRateForPeriod(rate);

        if (periodicRate <= 0) {
            return 0;
        }

        double principal = debtLoanPrincipalAmount;
        int numberOfPayments = selectedNoOfInstallments;
        double factor = Math.pow(1 + periodicRate, numberOfPayments);

        return principal * periodicRate * factor / (factor - 1);
    }

    private double getRateForPeriod(double rate) {

        double rateDecimal = rate / 100.0;

        double annualRate;

        switch (selectedInterestPeriod) {
            case DebtLoanType.INTEREST_PERIOD_DAY:
                annualRate = rateDecimal * 365.0;
                break;
            case DebtLoanType.INTEREST_PERIOD_WEEK:
                annualRate = rateDecimal * 52.0;
                break;
            case DebtLoanType.INTEREST_PERIOD_MONTH:
                annualRate = rateDecimal * 12.0;
                break;
            case DebtLoanType.INTEREST_PERIOD_YEAR:
                annualRate = rateDecimal;
                break;
            default:
                return 0;
        }

        return switch (selectedRepaymentFrequency) {
            case DebtLoanType.REPAYMENT_FREQ_DAILY -> annualRate / 365.0;
            case DebtLoanType.REPAYMENT_FREQ_WEEKLY -> annualRate / 52.0;
            case DebtLoanType.REPAYMENT_FREQ_BIWEEKLY -> annualRate / 26.0;
            case DebtLoanType.REPAYMENT_FREQ_MONTHLY -> annualRate / 12.0;
            case DebtLoanType.REPAYMENT_FREQ_QUARTERLY -> annualRate / 4.0;
            case DebtLoanType.REPAYMENT_FREQ_YEARLY -> annualRate;
            default -> 0;
        };
    }

    private List<ReducingBalancePaymentModel> generateReducingBalanceSchedule() {

        List<ReducingBalancePaymentModel> schedule = new ArrayList<>();

        if (debtLoanPrincipalAmount <= 0 || selectedNoOfInstallments <= 0) {
            return schedule;
        }

        String rateText = etInterestRate.getText() != null ? etInterestRate.getText().toString().trim() : "";
        double rate = 0;

        if (!rateText.isEmpty()) {
            try {
                rate = Double.parseDouble(rateText);
            } catch (NumberFormatException e) {
                return schedule;
            }
        }

        double periodicRate = getRateForPeriod(rate);
        double balance = debtLoanPrincipalAmount;
        double regularPaymentAmount;

        if (periodicRate <= 0) {
            regularPaymentAmount = debtLoanPrincipalAmount / selectedNoOfInstallments;
        } else {
            double factor = Math.pow(1 + periodicRate, selectedNoOfInstallments);
            regularPaymentAmount = debtLoanPrincipalAmount * periodicRate * factor / (factor - 1);
        }

        Calendar paymentCalendar = Calendar.getInstance();

        if (firstPaymentDate != null) {
            paymentCalendar.setTime(firstPaymentDate);
        } else if (startDate != null) {
            paymentCalendar.setTime(startDate);
        } else {
            return schedule;
        }

        for (int i = 1; i <= selectedNoOfInstallments; i++) {
            double openingBalance = balance;
            double paymentAmount = regularPaymentAmount;
            double interestAmount = openingBalance * periodicRate;
            double principalAmount = paymentAmount - interestAmount;

            if (i == selectedNoOfInstallments || principalAmount > openingBalance) {
                principalAmount = openingBalance;
                paymentAmount = principalAmount + interestAmount;
            }

            double closingBalance = openingBalance - principalAmount;
            if (closingBalance < 0) {
                closingBalance = 0;
            }

            ReducingBalancePaymentModel payment = new ReducingBalancePaymentModel();
            payment.paymentNumber = i;
            payment.paymentDate = paymentCalendar.getTime();
            payment.openingBalance = openingBalance;
            payment.paymentAmount = paymentAmount;
            payment.interestAmount = interestAmount;
            payment.principalAmount = principalAmount;
            payment.closingBalance = closingBalance;
            schedule.add(payment);
            balance = closingBalance;

            moveToNextPaymentDate(paymentCalendar);
        }

        return schedule;
    }

    private List<ReducingBalancePaymentModel> generateInstallmentSchedule() {

        List<ReducingBalancePaymentModel> schedule = new ArrayList<>();

        if (debtLoanPrincipalAmount <= 0 || selectedNoOfInstallments <= 0) {
            return schedule;
        }

        Calendar paymentCalendar = Calendar.getInstance();

        if (firstPaymentDate != null) {
            paymentCalendar.setTime(firstPaymentDate);
        } else if (startDate != null) {
            paymentCalendar.setTime(startDate);
        } else {
            return schedule;
        }

        if (selectedInterestCalcMethodPeriod == DebtLoanType.INTEREST_CALC_REDUCE_BALANCE) {

            double balance = debtLoanPrincipalAmount;
            double principalPerPayment = debtLoanPrincipalAmount / selectedNoOfInstallments;

            double rateText = 0;
            String rateString = etInterestRate.getText() != null ? etInterestRate.getText().toString().trim() : "";

            if (!rateString.isEmpty()) {
                try {
                    rateText = Double.parseDouble(rateString);
                } catch (NumberFormatException e) {
                    return schedule;
                }
            }

            double periodicRate = getRateForPeriod(rateText);

            for (int i = 1; i <= selectedNoOfInstallments; i++) {

                double openingBalance = balance;
                double principalAmount = principalPerPayment;

                if (i == selectedNoOfInstallments) {
                    principalAmount = openingBalance;
                }

                double interestAmount = openingBalance * periodicRate;
                double paymentAmount = principalAmount + interestAmount;
                double closingBalance = openingBalance - principalAmount;

                if (closingBalance < 0) {
                    closingBalance = 0;
                }

                ReducingBalancePaymentModel payment = new ReducingBalancePaymentModel();
                payment.paymentNumber = i;
                payment.paymentDate = paymentCalendar.getTime();
                payment.openingBalance = openingBalance;
                payment.paymentAmount = paymentAmount;
                payment.interestAmount = interestAmount;
                payment.principalAmount = principalAmount;
                payment.closingBalance = closingBalance;
                schedule.add(payment);
                balance = closingBalance;
                moveToNextPaymentDate(paymentCalendar);
            }

            return schedule;
        }

        double totalAmount = debtLoanPrincipalAmount + debtLoanInterestAmount;
        double regularPaymentAmount = totalAmount / selectedNoOfInstallments;
        double remainingAmount = totalAmount;

        for (int i = 1; i <= selectedNoOfInstallments; i++) {
            double paymentAmount = regularPaymentAmount;

            if (i == selectedNoOfInstallments) {
                paymentAmount = remainingAmount;
            }

            ReducingBalancePaymentModel payment = new ReducingBalancePaymentModel();
            payment.paymentNumber = i;
            payment.paymentDate = paymentCalendar.getTime();
            payment.openingBalance = remainingAmount;
            payment.paymentAmount = paymentAmount;
            double installmentInterest = debtLoanInterestAmount / selectedNoOfInstallments;
            double installmentPrincipal = paymentAmount - installmentInterest;
            payment.interestAmount = installmentInterest;
            payment.principalAmount = installmentPrincipal;
            payment.closingBalance = Math.max(0, remainingAmount - paymentAmount);
            schedule.add(payment);
            remainingAmount = payment.closingBalance;
            moveToNextPaymentDate(paymentCalendar);
        }

        return schedule;
    }

    private void moveToNextPaymentDate(Calendar calendar) {

        switch (selectedRepaymentFrequency) {

            case DebtLoanType.REPAYMENT_FREQ_DAILY:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;

            case DebtLoanType.REPAYMENT_FREQ_WEEKLY:
                calendar.add(Calendar.DAY_OF_MONTH, 7);
                break;

            case DebtLoanType.REPAYMENT_FREQ_BIWEEKLY:
                calendar.add(Calendar.DAY_OF_MONTH, 14);
                break;

            case DebtLoanType.REPAYMENT_FREQ_MONTHLY:
                calendar.add(Calendar.MONTH, 1);
                break;

            case DebtLoanType.REPAYMENT_FREQ_QUARTERLY:
                calendar.add(Calendar.MONTH, 3);
                break;

            case DebtLoanType.REPAYMENT_FREQ_YEARLY:
                calendar.add(Calendar.YEAR, 1);
                break;
        }
    }

    private void updateRepaymentSchedule() {

        if (selectedRepaymentMethod == DebtLoanType.REPAYMENT_FLEXIBLE) {
            cardRepaymentSchedule.setVisibility(View.GONE);
            return;
        }

        schedule = new ArrayList<>();

        if (selectedRepaymentMethod == DebtLoanType.REPAYMENT_INSTALLMENTS) {
            schedule = generateInstallmentSchedule();
        } else {
            schedule = generateReducingBalanceSchedule();
        }

        if (schedule.isEmpty()) {
            cardRepaymentSchedule.setVisibility(View.GONE);
            return;
        }

        tvSchedulePaymentCount.setText(getResources().getQuantityString(R.plurals.payment_count, schedule.size(), schedule.size()));
        tvScheduleFrequency.setText(getPaymentFrequencyText());

        double totalPayment = 0;
        double totalInterest = 0;

        for (ReducingBalancePaymentModel payment : schedule) {
            totalPayment += payment.paymentAmount;
            totalInterest += payment.interestAmount;
        }

        String symbol = selectedWallet != null ? selectedWallet.currencySymbol : account.currencySymbol;

        tvScheduleTotalAmount.setText(CommonUtils.getBeautifyAmount(symbol, totalPayment));
        tvScheduleTotalInterest.setText(CommonUtils.getBeautifyAmount(symbol, totalInterest));
        cardRepaymentSchedule.setVisibility(View.VISIBLE);

        RecyclerViewAdapter<ReducingBalancePaymentModel> adapter = new RecyclerViewAdapter<>(this, schedule, R.layout.item_repayment_schedule) {
            @Override
            public void onPostBindViewHolder(ViewHolder holder, ReducingBalancePaymentModel payment) {
                holder.setViewText(R.id.tvPaymentNumber, String.valueOf(payment.paymentNumber));
                holder.setViewText(R.id.tvPaymentDate, DateHelper.getFormattedDate(payment.paymentDate));
                String symbol = selectedWallet != null ? selectedWallet.currencySymbol : account.currencySymbol;
                holder.setViewText(R.id.tvPaymentAmount, CommonUtils.getBeautifyAmount(symbol, payment.paymentAmount));
                holder.setViewVisibility(R.id.layoutPaymentDetails, View.VISIBLE);
                holder.setViewVisibility(R.id.tvRemainingBalance, View.VISIBLE);
                holder.setViewText(R.id.tvPrincipal, CommonUtils.createAmountText(getString(R.string.text_principal),
                        CommonUtils.getBeautifyAmount(symbol, payment.principalAmount), medium, semiBold));
                holder.setViewText(R.id.tvInterest, CommonUtils.createAmountText(getString(R.string.text_interest),
                        CommonUtils.getBeautifyAmount(symbol, payment.interestAmount), medium, semiBold));
                holder.setViewText(R.id.tvRemainingBalance, CommonUtils.createAmountText(getString(R.string.text_balance),
                        CommonUtils.getBeautifyAmount(symbol, payment.closingBalance), medium, semiBold));

                // =====================================================
                // PAYMENT STATUS
                // =====================================================
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);

                Calendar paymentDate = Calendar.getInstance();
                paymentDate.setTime(payment.paymentDate);
                paymentDate.set(Calendar.HOUR_OF_DAY, 0);
                paymentDate.set(Calendar.MINUTE, 0);
                paymentDate.set(Calendar.SECOND, 0);
                paymentDate.set(Calendar.MILLISECOND, 0);

                if (paymentDate.before(today)) {
                    holder.setViewText(R.id.tvPaymentStatus, getString(R.string.overdue));
                    holder.setViewVisibility(R.id.tvPaymentStatus, View.VISIBLE);
                    holder.setViewBackgroundResource(R.id.tvPaymentStatus, R.drawable.bg_badge_expense);
                } else {
                    holder.setViewText(R.id.tvPaymentStatus, getString(R.string.upcoming));
                    holder.setViewVisibility(R.id.tvPaymentStatus, View.VISIBLE);
                    holder.setViewBackgroundResource(R.id.tvPaymentStatus, R.drawable.bg_badge_income);
                }
            }
        };

        rvRepaymentSchedule.setLayoutManager(new LinearLayoutManager(this));
        rvRepaymentSchedule.setAdapter(adapter);
        rvRepaymentSchedule.setHasFixedSize(false);
        rvRepaymentSchedule.setNestedScrollingEnabled(false);
    }

    private String getPaymentFrequencyText() {
        return switch (selectedRepaymentFrequency) {
            case DebtLoanType.REPAYMENT_FREQ_DAILY -> getString(R.string.day);
            case DebtLoanType.REPAYMENT_FREQ_WEEKLY -> getString(R.string.week_text);
            case DebtLoanType.REPAYMENT_FREQ_BIWEEKLY -> getString(R.string.bi_week_text);
            case DebtLoanType.REPAYMENT_FREQ_MONTHLY -> getString(R.string.month_text);
            case DebtLoanType.REPAYMENT_FREQ_QUARTERLY -> getString(R.string.quarter_text);
            case DebtLoanType.REPAYMENT_FREQ_YEARLY -> getString(R.string.year_text);
            default -> "";
        };
    }

    @NonNull
    private Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();

        Date date = null;

        if (selectedDateType == DATE_TYPE_START) {
            date = startDate;
        } else if (selectedDateType == DATE_TYPE_DUE) {
            date = dueDate != null ? dueDate : startDate;
        }

        if (date != null) {
            calendar.setTime(date);
        }

        return calendar;
    }

    private void updateSaveButtonState() {
        try {
            boolean enabled;

            // =====================================================
            // BASIC REQUIRED FIELDS
            // =====================================================
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";

            enabled = !name.isEmpty() && !title.isEmpty() && selectedWallet != null && debtLoanPrincipalAmount > 0 && startDate != null;

            // =====================================================
            // INTEREST
            // =====================================================
            if (enabled) {
                switch (selectedInterest) {
                    // -------------------------------------------------
                    // NO INTEREST
                    // -------------------------------------------------
                    case DebtLoanType.DEBT_NO_INTEREST:
                        break;

                    // -------------------------------------------------
                    // FIXED AMOUNT
                    // -------------------------------------------------
                    case DebtLoanType.DEBT_FIXED_AMOUNT:
                        if (debtLoanInterestAmount <= 0) {
                            enabled = false;
                        }
                        break;
                    // -------------------------------------------------
                    // PERCENTAGE
                    // -------------------------------------------------
                    case DebtLoanType.DEBT_PERCENTAGE:
                        // Interest Rate
                        String rateText = etInterestRate.getText() != null ? etInterestRate.getText().toString().trim() : "";

                        if (rateText.isEmpty()) {
                            enabled = false;
                            break;
                        }

                        try {
                            double rate = Double.parseDouble(rateText);
                            if (rate <= 0) {
                                enabled = false;
                                break;
                            }
                        } catch (NumberFormatException e) {
                            enabled = false;
                            break;
                        }

                        // Interest Period
                        if (selectedInterestPeriod <= 0) {
                            enabled = false;
                            break;
                        }

                        // Calculation Method
                        if (selectedInterestCalcMethodPeriod <= 0) {
                            enabled = false;
                            break;
                        }

                        // Interest Duration
                        if (selectedInterestDurationPeriod <= 0) {
                            enabled = false;
                            break;
                        }

                        // Custom Interest Duration
                        if (selectedInterestDurationPeriod == DebtLoanType.INTEREST_CUSTOM_PERIOD) {
                            if (customInterestDuration <= 0 || customInterestDurationPeriod <= 0) {
                                enabled = false;
                                break;
                            }
                        }

                        // Same as Loan Period
                        if (selectedInterestDurationPeriod == DebtLoanType.INTEREST_LOAN_PERIOD) {
                            if (dueDate == null) {
                                enabled = false;
                                break;
                            }
                        }

                        // Compound Frequency
                        if (selectedInterestCalcMethodPeriod == DebtLoanType.INTEREST_CALC_CI) {
                            if (selectedInterestCompFreqPeriod <= 0) {
                                enabled = false;
                            }
                        }
                        break;
                }
            }

            // =====================================================
            // REPAYMENT
            // =====================================================
            if (enabled && selectedRepaymentMethod != DebtLoanType.REPAYMENT_FLEXIBLE) {

                // Number of installments
                if (selectedNoOfInstallments <= 0) {
                    enabled = false;
                }

                // Payment frequency
                if (selectedRepaymentFrequency <= 0) {
                    enabled = false;
                }

                // First payment date
                if (firstPaymentDate == null) {
                    enabled = false;
                }
            }

            // =====================================================
            // UPDATE SAVE BUTTON
            // =====================================================
            tvSave.setEnabled(enabled);
            tvSave.setAlpha(enabled ? 1f : 0.5f);
        } catch (Exception e) {
            AppLogger.e(getClass(), "updateSaveButtonState", e);
            tvSave.setEnabled(false);
            tvSave.setAlpha(0.5f);
        }
    }

    private void saveDebtLoan() {
        try {
            tvSave.setEnabled(false);

            long now = System.currentTimeMillis();

            DebtLoanEntity entity = new DebtLoanEntity();

            entity.type = selectedType;
            entity.name = etName.getText() != null ? etName.getText().toString().trim() : "";
            entity.title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            entity.debtLoanIcon = debtIcon;
            entity.debtLoanColor = colorSpinner.getSelectedItemPosition();
            entity.walletId = selectedWallet != null ? selectedWallet.id : 0;
            entity.currencyCode = selectedWallet != null ? selectedWallet.currencyCode : account.currencyCode;
            entity.currencySymbol = selectedWallet != null ? selectedWallet.currencySymbol : account.currencySymbol;
            entity.principalAmount = debtLoanPrincipalAmount;
            entity.notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";
            entity.startDate = startDate != null ? startDate.getTime() : 0;
            entity.dueDate = dueDate != null ? dueDate.getTime() : null;
            entity.firstPaymentDate = firstPaymentDate != null ? firstPaymentDate.getTime() : null;
            entity.interestType = selectedInterest;

            String interestRateText = etInterestRate.getText() != null ? etInterestRate.getText().toString().trim() : "";
            entity.interestRate = interestRateText.isEmpty() ? 0 : CommonUtils.parseDouble(interestRateText);

            entity.interestAmount = debtLoanInterestAmount;
            entity.interestPeriod = selectedInterestPeriod;

            if (selectedInterest == DebtLoanType.DEBT_PERCENTAGE) {
                entity.interestCalculationMethod = selectedInterestCalcMethodPeriod;
            } else {
                entity.interestCalculationMethod = 0;
            }

            if (selectedInterestCalcMethodPeriod == DebtLoanType.INTEREST_CALC_CI) {
                entity.compoundFrequency = selectedInterestCompFreqPeriod;
                entity.interestDuration = selectedInterestDurationPeriod;
            } else {
                entity.compoundFrequency = 0;
                entity.interestDuration = 0;
            }

            if (selectedInterestDurationPeriod == DebtLoanType.INTEREST_CUSTOM_PERIOD) {
                entity.customInterestDuration = customInterestDuration;
                entity.customInterestDurationPeriod = customInterestDuration > 0 ? customInterestDurationPeriod : 0;
            } else {
                entity.customInterestDuration = 0;
                entity.customInterestDurationPeriod = 0;
            }

            entity.repaymentMethod = selectedRepaymentMethod;
            if (selectedRepaymentMethod != DebtLoanType.REPAYMENT_FLEXIBLE) {
                entity.repaymentFrequency = selectedRepaymentFrequency;
                entity.noOfInstallments = selectedNoOfInstallments;
                entity.installmentAmount = CommonUtils.parseDouble(Objects.requireNonNull(etInstallmentAmount.getText()).toString().trim());
            } else {
                entity.repaymentFrequency = 0;
                entity.noOfInstallments = 0;
                entity.installmentAmount = 0;
            }

            entity.totalInterest = debtLoanInterestAmount;
            entity.totalAmount = debtLoanPrincipalAmount + debtLoanInterestAmount;
            entity.paidAmount = 0;
            entity.remainingAmount = entity.totalAmount;
            entity.isDeleted = false;
            entity.isSynced = false;
            entity.isMoneyReceivedLent = switchMoneyReceive.isChecked();

            List<DebtLoanPaymentEntity> debtLoanPaymentList = new ArrayList<>();
            if (schedule != null && !schedule.isEmpty()) {
                for (ReducingBalancePaymentModel reducingBalancePaymentModel : schedule) {
                    DebtLoanPaymentEntity debtLoanPayment = new DebtLoanPaymentEntity();
                    debtLoanPayment.tempDebtLoanPaymentServerId = "DLP_" + now + "_" + reducingBalancePaymentModel.paymentNumber;
                    debtLoanPayment.debtLoanPaymentId = 0;
                    debtLoanPayment.paymentNumber = reducingBalancePaymentModel.paymentNumber;
                    debtLoanPayment.paymentDate = reducingBalancePaymentModel.paymentDate != null ? reducingBalancePaymentModel.paymentDate.getTime() : 0L;
                    debtLoanPayment.principalAmount = reducingBalancePaymentModel.principalAmount;
                    debtLoanPayment.interestAmount = reducingBalancePaymentModel.interestAmount;
                    debtLoanPayment.paymentAmount = reducingBalancePaymentModel.paymentAmount;
                    debtLoanPayment.status = DebtLoanPaymentEntity.PAYMENT_PENDING;
                    debtLoanPayment.paidAmount = 0;
                    debtLoanPayment.paidDate = 0L;
                    debtLoanPayment.createdAt = now;
                    debtLoanPayment.updatedAt = now;
                    debtLoanPayment.isSynced = false;
                    debtLoanPayment.isDeleted = false;

                    debtLoanPaymentList.add(debtLoanPayment);
                }
            }

            // -----------------------------------------------------
            // SAVE / UPDATE
            // -----------------------------------------------------
            if (isEdit) {
                entity.updatedAt = now;
            } else {
                entity.createdAt = now;
                entity.updatedAt = now;
                entity.debtLoanId = 0;
                entity.tempDebtLoanServerId = "DL_" + now;

                debtLoanViewModel.saveDebtLoan(entity, debtLoanPaymentList);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "saveDebtLoan", e);
        }
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            Date selectedDate = calendar.getTime();
            if (selectedDateType == DATE_TYPE_START) {
                startDate = selectedDate;
                tvStartDate.setText(DateHelper.getDateFromPicker(year, month, dayOfMonth));

                // If due date is before the new start date,
                // move due date to the new start date first.
                if (dueDate != null && dueDate.before(startDate)) {
                    dueDate = startDate;
                    Calendar dueCalendar = Calendar.getInstance();
                    dueCalendar.setTime(dueDate);
                    tvDueDate.setText(DateHelper.getDateFromPicker(dueCalendar.get(Calendar.YEAR), dueCalendar.get(Calendar.MONTH),
                            dueCalendar.get(Calendar.DAY_OF_MONTH)));
                    ivDueDateSelect.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_close));
                }
            } else if (selectedDateType == DATE_TYPE_DUE) {
                // Safety check
                if (startDate != null && selectedDate.before(startDate)) {
                    selectedDate = startDate;
                    Calendar startCalendar = Calendar.getInstance();
                    startCalendar.setTime(startDate);
                    year = startCalendar.get(Calendar.YEAR);
                    month = startCalendar.get(Calendar.MONTH);
                    dayOfMonth = startCalendar.get(Calendar.DAY_OF_MONTH);
                }
                dueDate = selectedDate;
                tvDueDate.setText(DateHelper.getDateFromPicker(year, month, dayOfMonth));
                ivDueDateSelect.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_close));
            } else if (selectedDateType == DATE_TYPE_FIRST_PAYMENT) {
                firstPaymentDate = selectedDate;
                etFirstPaymentDate.setText(DateHelper.getDateFromPicker(year, month, dayOfMonth));
                updateInstallmentAmount();
                updateRepaymentSchedule();
            }

            calculateSelectedInterest();
            updateInterestSummary();
            updateRepaymentSchedule();
            updateSaveButtonState();
        } catch (Exception e) {
            AppLogger.e(getClass(), "onDateSet", e);
        }
    }

    private void prepareBottomSheet() {
        // Clear focus from all EditTexts
        etName.clearFocus();
        etTitle.clearFocus();
        etNotes.clearFocus();

        etInterestRate.clearFocus();
        etInterestPeriod.clearFocus();
        etInterestAmount.clearFocus();
        etCalculationMethod.clearFocus();
        etInterestDuration.clearFocus();
        etCompoundFrequency.clearFocus();

        etNoOfInstallments.clearFocus();
        etPaymentFrequency.clearFocus();
        etInstallmentAmount.clearFocus();
        etFirstPaymentDate.clearFocus();
        etCustomInterestDuration.clearFocus();

        // Hide keyboard
        hideKeyboard(this);
    }
}