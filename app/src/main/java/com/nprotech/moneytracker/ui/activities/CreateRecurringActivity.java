package com.nprotech.moneytracker.ui.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRadioButton;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.CategoryEntity;
import com.nprotech.moneytracker.db.entites.RecurringTransactionEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.FrequencyModel;
import com.nprotech.moneytracker.models.RecurringTransactionWithDetails;
import com.nprotech.moneytracker.ui.adapters.FontSpinnerAdapter;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.utils.IntentUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.CategoryViewModel;
import com.nprotech.moneytracker.viewmodel.RecurringTransactionViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateRecurringActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener {

    private AppCompatImageView icBack;
    private AppCompatTextView tvSave, tvTitle, tvFromWallet, tvWallet, tvAmount, tvFee, tvCategory, tvRepeat, tvRepeatOn, walletTitleLabel,
            tvUntilDate;
    private AppCompatEditText etDescription, etNotes;
    private SwitchCompat switchReminder, switchCreateTransaction;
    private MaterialButtonToggleGroup toggleTransactionType;
    private MaterialButton btnIncome, btnExpense, btnTransfer;
    private MaterialCardView cardFromWallet, cardWallet, cardAmount, cardFee, cardCategory, cardRepeat, cardRepeatOn;
    private int transactionType;
    private Typeface medium, semiBold;
    private AccountEntity account;
    private WalletEntity selectedWallet, selectedFromWallet, normalTransactionWallet;
    private AccountViewModel accountViewModel;
    private CategoryViewModel categoryViewModel;
    private RecurringTransactionViewModel recurringTransactionViewModel;
    private List<WalletEntity> walletLists;
    private CategoryEntity incomeCategory, expenseCategory;
    private double transactionAmount = 0, transactionFee = 0;
    private ActivityResultLauncher<Intent> calculatorLauncher, categoryLauncher;
    private int tempReportType = Constants.REPEAT_NONE, selectedRepeatType = Constants.REPEAT_NONE;
    private int monthlyDay = 0;
    private int repeatInterval = 1;
    private int repeatTimes = 1;
    private int monthlyMode = Constants.MONTHLY_DAY;
    private int repeatEndType = 0;
    private String tempRecurringServerId;
    private int originalRepeatType;
    private int originalRepeatInterval;
    private int originalMonthlyMode;
    private int originalMonthlyDay;
    private String originalRepeatWeekDays;
    private RecurringTransactionWithDetails recurringTransactionWithDetails;
    private final Set<Integer> selectedWeekDays = new LinkedHashSet<>();
    private Date date;
    private long untilDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recurring);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            View rootView = findViewById(R.id.rootView);
            tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            tvSave = toolbarWrapper.findViewById(R.id.tvSave);
            icBack = toolbarWrapper.findViewById(R.id.icBack);

            tvFromWallet = findViewById(R.id.tvFromWallet);
            tvWallet = findViewById(R.id.tvWallet);
            tvAmount = findViewById(R.id.tvAmount);
            tvFee = findViewById(R.id.tvFee);
            tvCategory = findViewById(R.id.tvCategory);
            tvRepeat = findViewById(R.id.tvRepeat);
            tvRepeatOn = findViewById(R.id.tvRepeatOn);
            etDescription = findViewById(R.id.etDescription);
            etNotes = findViewById(R.id.etNotes);
            switchReminder = findViewById(R.id.switchReminder);
            switchCreateTransaction = findViewById(R.id.switchCreateTransaction);
            toggleTransactionType = findViewById(R.id.toggleTransactionType);
            btnIncome = findViewById(R.id.btnIncome);
            btnExpense = findViewById(R.id.btnExpense);
            btnTransfer = findViewById(R.id.btnTransfer);
            cardFromWallet = findViewById(R.id.cardFromWallet);
            cardWallet = findViewById(R.id.cardWallet);
            cardAmount = findViewById(R.id.cardAmount);
            cardFee = findViewById(R.id.cardFee);
            cardCategory = findViewById(R.id.cardCategory);
            cardRepeat = findViewById(R.id.cardRepeat);
            cardRepeatOn = findViewById(R.id.cardRepeatOn);
            walletTitleLabel = findViewById(R.id.walletTitleLabel);

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

                medium = ResourcesCompat.getFont(this, R.font.exo2_medium);
                semiBold = ResourcesCompat.getFont(this, R.font.exo2_semibold);

                String action = bundle.getString("action");
                transactionType = bundle.getInt("type");

                btnIncome.setTypeface(medium);
                btnExpense.setTypeface(medium);
                btnTransfer.setTypeface(medium);

                accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
                categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
                recurringTransactionViewModel = new ViewModelProvider(this).get(RecurringTransactionViewModel.class);

                if (Objects.equals(action, "add")) {
                    toggleTransactionType.setVisibility(View.VISIBLE);
                    switchTransMode(2);
                } else if (Objects.equals(action, "edit")) {
                    toggleTransactionType.setVisibility(View.GONE);
                    if (transactionType == 1) {
                        tvTitle.setText(getString(R.string.income_recurring));
                    } else if (transactionType == 2) {
                        tvTitle.setText(getString(R.string.expense_recurring));
                    } else {
                        tvTitle.setText(getString(R.string.transfer_recurring));
                    }
                    switchTransMode(transactionType);
                    tempRecurringServerId = bundle.getString("transactionId", "");
                }

                makeReadOnly();
                setupListeners();
                setupLauncher();
                bindData(action != null && action.equals("edit"));
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData(boolean isEdit) {
        try {
            if (isEdit) {

                recurringTransactionWithDetails = recurringTransactionViewModel.getRecurringTransactionDetail(tempRecurringServerId);

                if (recurringTransactionWithDetails == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                    finish();
                    ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
                    return;
                }

                account = accountViewModel.getAccountDetailById(recurringTransactionWithDetails.recurringTransaction.accountId);
                walletLists = accountViewModel.getWalletsByAccountId(recurringTransactionWithDetails.recurringTransaction.accountId);

                tvSave.setText(getString(R.string.update));
                tvSave.setEnabled(true);
                enabledSaveOption(true);

                if (recurringTransactionWithDetails != null) {
                    date = new Date(recurringTransactionWithDetails.recurringTransaction.startDate);
                    untilDate = recurringTransactionWithDetails.recurringTransaction.untilDate;
                    transactionAmount = recurringTransactionWithDetails.recurringTransaction.amount;
                    transactionFee = recurringTransactionWithDetails.recurringTransaction.fee;

                    if (!walletLists.isEmpty()) {
                        selectedWallet = null;
                        selectedFromWallet = null;

                        if (transactionType == TransactionEntity.TYPE_TRANSFER) {
                            // --------------------------------
                            // FROM WALLET
                            // --------------------------------
                            if (recurringTransactionWithDetails.recurringTransaction.fromWalletId != null) {

                                for (WalletEntity wallet : walletLists) {
                                    if (wallet.id == recurringTransactionWithDetails.recurringTransaction.fromWalletId) {
                                        selectedFromWallet = wallet;
                                        break;
                                    }
                                }
                            }

                            // --------------------------------
                            // TO WALLET
                            // --------------------------------
                            for (WalletEntity wallet : walletLists) {
                                if (wallet.id == recurringTransactionWithDetails.recurringTransaction.walletId) {
                                    selectedWallet = wallet;
                                    break;
                                }
                            }

                            // --------------------------------
                            // DISPLAY FROM WALLET
                            // --------------------------------
                            if (selectedFromWallet != null) {
                                tvFromWallet.setText(getString(R.string.wallet_info, selectedFromWallet.name,
                                        CommonUtils.getBeautifyAmount(selectedFromWallet.currencySymbol, selectedFromWallet.amount)));
                            }

                            // --------------------------------
                            // DISPLAY TO WALLET
                            // --------------------------------
                        } else {
                            for (WalletEntity wallet : walletLists) {
                                if (wallet.id == recurringTransactionWithDetails.recurringTransaction.walletId) {
                                    selectedWallet = wallet;
                                    break;
                                }
                            }

                        }
                        if (selectedWallet != null) {
                            tvWallet.setText(getString(R.string.wallet_info, selectedWallet.name,
                                    CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol, selectedWallet.amount)));
                        }
                    }

                    if (transactionType == TransactionEntity.TYPE_INCOME) {
                        if (recurringTransactionWithDetails.recurringTransaction.defaultCategoryId > 0) {
                            incomeCategory = categoryViewModel.getCategoryById(recurringTransactionWithDetails.recurringTransaction.defaultCategoryId,
                                    true);
                        } else {
                            incomeCategory = categoryViewModel.getCategoryById(recurringTransactionWithDetails.recurringTransaction.categoryId,
                                    false);
                        }
                    } else if (transactionType == TransactionEntity.TYPE_EXPENSE) {
                        if (recurringTransactionWithDetails.recurringTransaction.defaultCategoryId > 0) {
                            expenseCategory = categoryViewModel.getCategoryById(recurringTransactionWithDetails.recurringTransaction.defaultCategoryId,
                                    true);
                        } else {
                            expenseCategory = categoryViewModel.getCategoryById(recurringTransactionWithDetails.recurringTransaction.categoryId,
                                    false);
                        }
                    }

                    etDescription.setText(recurringTransactionWithDetails.recurringTransaction.description);
                    etNotes.setText(recurringTransactionWithDetails.recurringTransaction.notes);
                    tvCategory.setText(recurringTransactionWithDetails.recurringTransaction.getCategoryName(getApplicationContext()));

                    // --------------------------------------------------
                    // Repeat settings
                    // --------------------------------------------------
                    RecurringTransactionEntity recurring = recurringTransactionWithDetails.recurringTransaction;
                    selectedRepeatType = recurring.repeatType;
                    tempReportType = recurring.repeatType;
                    repeatInterval = recurring.repeatInterval > 0 ? recurring.repeatInterval : 1;
                    repeatTimes = recurring.repeatTimes;
                    monthlyMode = recurring.monthlyMode;

                    originalRepeatType = recurring.repeatType;
                    originalRepeatInterval = recurring.repeatInterval;
                    originalMonthlyMode = recurring.monthlyMode;
                    originalMonthlyDay = recurring.monthlyDay;
                    originalRepeatWeekDays = recurring.repeatWeekDays;

                    // Restore monthly day
                    monthlyDay = recurring.monthlyDay;

                    // Restore weekly days
                    selectedWeekDays.clear();
                    if (recurring.repeatWeekDays != null && !recurring.repeatWeekDays.trim().isEmpty()) {
                        String[] days = recurring.repeatWeekDays.split(",");
                        for (String day : days) {
                            try {
                                selectedWeekDays.add(Integer.parseInt(day.trim()));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }

                    // Restore repeat end type
                    if (untilDate > 0) {
                        repeatEndType = 1; // Until
                    } else if (repeatTimes > 0) {
                        repeatEndType = 2; // For X times
                    } else {
                        repeatEndType = 0; // Forever
                    }

                    updateAmountText();
                    updateRepeatText();

                    if (selectedRepeatType != Constants.REPEAT_NONE) {
                        updateRepeatOnText();
                        cardRepeatOn.setVisibility(View.VISIBLE);
                    } else {
                        tvRepeatOn.setText("");
                        cardRepeatOn.setVisibility(View.GONE);
                    }

                    switchReminder.setChecked(recurring.reminder);
                    switchCreateTransaction.setChecked(recurring.createTransaction);
                }
            } else {
                tvSave.setText(getString(R.string.save));

                account = accountViewModel.getAccountDetailById(PreferenceManager.INSTANCE.getAccountId());
                walletLists = accountViewModel.getWalletsByAccountId(PreferenceManager.INSTANCE.getAccountId());

                tempReportType = Constants.REPEAT_NONE;
                selectedRepeatType = Constants.REPEAT_NONE;
                repeatInterval = 1;
                repeatTimes = 0;
                repeatEndType = 0;
                untilDate = 0;
                transactionAmount = 0;
                transactionFee = 0;

                tvSave.setEnabled(false);
                enabledSaveOption(false);

                if (!walletLists.isEmpty()) {
                    selectedWallet = walletLists.get(0);
                    tvWallet.setText(getString(R.string.wallet_info, selectedWallet.name,
                            CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol, selectedWallet.amount)));
                }

                updateAmountText();
                updateRepeatText();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void makeReadOnly() {
        tvAmount.setFocusable(false);
        tvAmount.setLongClickable(false);
        tvCategory.setFocusable(false);
        tvCategory.setLongClickable(false);
        tvFromWallet.setFocusable(false);
        tvFromWallet.setLongClickable(false);
        tvWallet.setFocusable(false);
        tvWallet.setLongClickable(false);
        tvFee.setFocusable(false);
        tvFee.setLongClickable(false);
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

            toggleTransactionType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (!isChecked) return;

                if (checkedId == R.id.btnIncome) {
                    switchTransMode(1);
                    transactionType = 1;
                } else if (checkedId == R.id.btnExpense) {
                    switchTransMode(2);
                    transactionType = 2;
                } else if (checkedId == R.id.btnTransfer) {
                    switchTransMode(3);
                    transactionType = 3;
                }
            });

            // AMOUNT
            tvAmount.setOnClickListener(view -> {

                hideKeyboard(this);

                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", transactionAmount);
                intent.putExtra("type", "amount");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                calculatorLauncher.launch(intent, options);
            });

            cardAmount.setOnClickListener(view -> tvAmount.performClick());

            // WALLET
            tvWallet.setOnClickListener(view -> {
                hideKeyboard(this);
                selectWallets("to");
            });

            cardWallet.setOnClickListener(view -> tvWallet.performClick());

            // FROM WALLET
            tvFromWallet.setOnClickListener(view -> {
                hideKeyboard(this);
                selectWallets("from");
            });

            cardFromWallet.setOnClickListener(view -> tvFromWallet.performClick());

            // CATEGORY
            tvCategory.setOnClickListener(view -> {

                hideKeyboard(this);

                Intent intent = new Intent(this, CategoryPickerActivity.class);
                intent.putExtra("transactionType", transactionType);
                intent.putExtra("isFromScreen", "transaction");

                if (transactionType == 1) {
                    intent.putExtra("categoryId", incomeCategory != null ? incomeCategory.id : 0);
                } else {
                    intent.putExtra("categoryId", expenseCategory != null ? expenseCategory.id : 0);
                }

                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                categoryLauncher.launch(intent, options);
            });

            cardCategory.setOnClickListener(view -> tvCategory.performClick());

            // FEE
            tvFee.setOnClickListener(view -> {

                hideKeyboard(this);

                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", transactionFee);
                intent.putExtra("type", "fee");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                calculatorLauncher.launch(intent, options);
            });

            cardFee.setOnClickListener(view -> tvFee.performClick());

            // REPEAT
            tvRepeat.setOnClickListener(view -> showRepeatBottomSheet());

            cardRepeat.setOnClickListener(view -> tvRepeat.performClick());

            // REPEAT ON
            cardRepeatOn.setOnClickListener(view -> {
                if (selectedRepeatType != Constants.REPEAT_NONE) {
                    showRepeatOnBottomSheet();
                }
            });

            recurringTransactionViewModel.getSaveResult().observe(this, id -> {
                if (id == null) {
                    return;
                }
                if (id > 0) {
                    Toast.makeText(this, R.string.recurring_transaction_saved, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, R.string.failed_to_save_recurring_transaction, Toast.LENGTH_SHORT).show();
                }
            });

            tvSave.setOnClickListener(v -> {
                if (isFinishing()) {
                    return;
                }
                saveRecurringTransaction();
            });

            recurringTransactionViewModel.getOperationResult().observe(this, aBoolean -> {
                if (!Boolean.TRUE.equals(aBoolean)) {
                    return;
                }
                tvSave.setEnabled(true);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("isSaved", false);
                resultIntent.putExtra("isUpdated", true);
                resultIntent.putExtra("tempTransactionServerId", recurringTransactionWithDetails.recurringTransaction.tempRecurringServerId);
                setResult(RESULT_OK, resultIntent);
                finish();
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

                            if (type != null && type.equalsIgnoreCase("amount")) {
                                transactionAmount = amount;
                                updateAmountText();
                            } else if (type != null && type.equalsIgnoreCase("fee")) {
                                transactionFee = amount;
                                String symbol;

                                if (transactionType == TransactionEntity.TYPE_TRANSFER && selectedFromWallet != null) {
                                    symbol = selectedFromWallet.currencySymbol;
                                } else {
                                    symbol = account.currencySymbol;
                                }
                                tvFee.setText(CommonUtils.getBeautifyAmount(symbol, amount));
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
                                if (transactionType == 1) {
                                    incomeCategory = categoryEntity;
                                } else {
                                    expenseCategory = categoryEntity;
                                }

                                tvCategory.setText(categoryEntity.getName(getApplicationContext()));
                                updateSaveButtonState();
                            }
                        }
                    }
                });
    }

    private void selectWallets(String type) {
        try {

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

                    if (type.equalsIgnoreCase("to")) {
                        holder.getView(R.id.ivSelected).setVisibility(selectedWallet != null && selectedWallet.id == walletEntity.id ? View.VISIBLE : View.GONE);
                        holder.getView(R.id.rlAccountView).setOnClickListener(v -> {
                            selectedWallet = walletEntity;
                            normalTransactionWallet = walletEntity;

                            tvWallet.setText(getString(R.string.wallet_info, selectedWallet.name,
                                    CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol, selectedWallet.amount)));

                            updateAmountText();
                            updateSaveButtonState();
                            dialog.dismiss();
                        });
                    } else if (type.equalsIgnoreCase("from")) {

                        if (selectedFromWallet != null) {
                            holder.getView(R.id.ivSelected).setVisibility(selectedFromWallet.id == walletEntity.id ? View.VISIBLE : View.GONE);
                        }

                        holder.getView(R.id.rlAccountView).setOnClickListener(v -> {
                            selectedFromWallet = walletEntity;
                            tvFromWallet.setText(getString(R.string.wallet_info, selectedFromWallet.name,
                                    CommonUtils.getBeautifyAmount(selectedFromWallet.currencySymbol, selectedFromWallet.amount)));
                            updateAmountText();
                            tvFee.setText(CommonUtils.getBeautifyAmount(selectedFromWallet.currencySymbol, transactionFee));
                            updateSaveButtonState();
                            dialog.dismiss();
                        });
                    }
                }
            };

            rvWallets.setAdapter(adapter);
            rvWallets.setHasFixedSize(true);
            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "switchAccounts", e);
        }
    }

    private void restoreNormalTransactionWallet() {
        try {
            if (normalTransactionWallet != null) {
                if (walletLists != null && !walletLists.isEmpty()) {
                    for (WalletEntity wallet : walletLists) {
                        if (wallet.id == normalTransactionWallet.id) {
                            selectedWallet = wallet;
                            break;
                        }
                    }
                }
            }

            if (selectedWallet == null && walletLists != null && !walletLists.isEmpty()) {
                selectedWallet = walletLists.get(0);
            }

            if (selectedWallet != null) {
                tvWallet.setText(getString(R.string.wallet_info, selectedWallet.name, CommonUtils.getBeautifyAmount(
                        selectedWallet.currencySymbol, selectedWallet.amount)));
            } else {
                tvWallet.setText("");
            }

            updateAmountText();
            updateSaveButtonState();
        } catch (Exception e) {
            AppLogger.e(getClass(), "restoreNormalTransactionWallet", e);
        }
    }

    private void switchTransMode(int mode) {
        try {
            boolean isIncome = mode == TransactionEntity.TYPE_INCOME;
            boolean isExpense = mode == TransactionEntity.TYPE_EXPENSE;
            boolean isTransfer = mode == TransactionEntity.TYPE_TRANSFER;

            // -----------------------------
            // Common visibility
            // -----------------------------
            cardFromWallet.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
            cardFee.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
            cardCategory.setVisibility(isTransfer ? View.GONE : View.VISIBLE);

            // Wallet label
            walletTitleLabel.setText(isTransfer ? getString(R.string.to_wallet) : getString(R.string.wallet));

            // -----------------------------
            // Income
            // -----------------------------
            if (isIncome) {

                tvTitle.setText(getString(R.string.income));

                btnIncome.setBackgroundColor(ContextCompat.getColor(this, R.color.light_income));
                btnIncome.setTextColor(ContextCompat.getColor(this, R.color.income));
                btnIncome.setTypeface(semiBold);
                btnExpense.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                btnExpense.setTextColor(ContextCompat.getColor(this, R.color.text_grey));
                btnExpense.setTypeface(medium);
                btnTransfer.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                btnTransfer.setTextColor(ContextCompat.getColor(this, R.color.text_grey));
                btnTransfer.setTypeface(medium);

                tvCategory.setText(incomeCategory != null ? incomeCategory.getName(getApplicationContext()) : "");
                restoreNormalTransactionWallet();

                // -----------------------------
                // Expense
                // -----------------------------
            } else if (isExpense) {

                tvTitle.setText(getString(R.string.expense));

                btnExpense.setBackgroundColor(ContextCompat.getColor(this, R.color.light_expense));
                btnExpense.setTextColor(ContextCompat.getColor(this, R.color.expense));
                btnExpense.setTypeface(semiBold);
                btnIncome.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                btnIncome.setTextColor(ContextCompat.getColor(this, R.color.text_grey));
                btnIncome.setTypeface(medium);
                btnTransfer.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                btnTransfer.setTextColor(ContextCompat.getColor(this, R.color.text_grey));
                btnTransfer.setTypeface(medium);
                tvCategory.setText(expenseCategory != null ? expenseCategory.getName(getApplicationContext()) : "");
                restoreNormalTransactionWallet();

                // -----------------------------
                // Transfer
                // -----------------------------
            } else if (isTransfer) {

                if (selectedWallet != null) {
                    normalTransactionWallet = selectedWallet;
                }

                // Set first wallet as From Wallet
                selectedFromWallet = null;
                selectedWallet = null;

                if (walletLists != null && !walletLists.isEmpty()) {

                    // First wallet → From Wallet
                    selectedFromWallet = walletLists.get(0);
                    tvFromWallet.setText(getString(R.string.wallet_info, selectedFromWallet.name,
                            CommonUtils.getBeautifyAmount(selectedFromWallet.currencySymbol, selectedFromWallet.amount)));

                    // Second wallet → To Wallet
                    if (walletLists.size() > 1) {
                        selectedWallet = walletLists.get(1);
                        tvWallet.setText(getString(R.string.wallet_info, selectedWallet.name,
                                CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol, selectedWallet.amount)
                        ));
                    } else {
                        tvWallet.setText("");
                    }
                } else {
                    tvFromWallet.setText("");
                    tvWallet.setText("");
                }

                // Amount uses From Wallet currency
                updateAmountText();

                // Fee uses From Wallet currency
                tvFee.setText(CommonUtils.getBeautifyAmount(selectedFromWallet != null ? selectedFromWallet.currencySymbol : account.currencySymbol, transactionFee));

                tvTitle.setText(getString(R.string.transfer));

                btnTransfer.setBackgroundColor(ContextCompat.getColor(this, R.color.light_transfer));
                btnTransfer.setTextColor(ContextCompat.getColor(this, R.color.transfer));
                btnTransfer.setTypeface(semiBold);
                btnIncome.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                btnIncome.setTextColor(ContextCompat.getColor(this, R.color.text_grey));
                btnIncome.setTypeface(medium);
                btnExpense.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                btnExpense.setTextColor(ContextCompat.getColor(this, R.color.text_grey));
                btnExpense.setTypeface(medium);
            }

            updateSaveButtonState();
        } catch (Exception e) {
            AppLogger.e(getClass(), "switchTransMode", e);
        }
    }

    private void showRepeatBottomSheet() {
        try {

            tempReportType = selectedRepeatType;

            List<FrequencyModel> frequencyList = new ArrayList<>();
            frequencyList.add(new FrequencyModel(Constants.REPEAT_NONE, R.drawable.ic_calendar_none, getString(R.string.no_repeat)));
            frequencyList.add(new FrequencyModel(Constants.REPEAT_DAILY, R.drawable.ic_calendar_daily, getString(R.string.calendar_daily)));
            frequencyList.add(new FrequencyModel(Constants.REPEAT_WEEKLY, R.drawable.ic_calendar_weekly, getString(R.string.calendar_weekly)));
            frequencyList.add(new FrequencyModel(Constants.REPEAT_MONTHLY, R.drawable.ic_calendar_monthly, getString(R.string.calendar_monthly)));
            frequencyList.add(new FrequencyModel(Constants.REPEAT_YEARLY, R.drawable.ic_yearly, getString(R.string.calendar_yearly)));

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_sheet_repeat_recurring, findViewById(android.R.id.content), false);
            MaterialButton btnReset = bottomView.findViewById(R.id.btnReset);
            MaterialButton btnDone = bottomView.findViewById(R.id.btnApply);
            RecyclerView rvSelectRange = bottomView.findViewById(R.id.rvSelectRange);
            AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);

            RecyclerViewAdapter<FrequencyModel> adapter = new RecyclerViewAdapter<>(this, frequencyList, R.layout.item_calendar_filter) {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onPostBindViewHolder(ViewHolder holder, FrequencyModel frequency) {
                    boolean selected = tempReportType == frequency.frequency;

                    holder.setViewText(R.id.tvFilterName, frequency.frequencyName);
                    holder.setViewImageDrawable(R.id.ivIcon, ContextCompat.getDrawable(getApplicationContext(), frequency.icon));

                    holder.setViewVisibility(R.id.ivSelected, selected ? View.VISIBLE : View.GONE);
                    holder.setViewTypeface(R.id.tvFilterName, selected ? semiBold : medium);

                    holder.getView(R.id.rlFilterView).setOnClickListener(v -> {
                        tempReportType = frequency.frequency;
                        notifyDataSetChanged();
                    });
                }
            };

            rvSelectRange.setAdapter(adapter);
            rvSelectRange.setHasFixedSize(true);
            rvSelectRange.setItemAnimator(null);

            btnReset.setOnClickListener(v -> {
                applyRepeatType(Constants.REPEAT_NONE);
                dialog.dismiss();
            });

            btnDone.setOnClickListener(v -> {
                applyRepeatType(tempReportType);
                dialog.dismiss();
            });

            tvClose.setOnClickListener(v -> dialog.dismiss());

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showRepeatBottomSheet", e);
        }
    }

    private void showRepeatOnBottomSheet() {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_sheet_repeat_on, findViewById(android.R.id.content), false);

            HorizontalScrollView weekView = bottomView.findViewById(R.id.weekView);
            AppCompatTextView tvEveryRecurring = bottomView.findViewById(R.id.tvEveryRecurring);
            FrameLayout frameUntilDate = bottomView.findViewById(R.id.frameUntilDate);
            FrameLayout frameTimes = bottomView.findViewById(R.id.frameTimes);
            AppCompatTextView lblTimes = bottomView.findViewById(R.id.lblTimes);
            MaterialButton btnApply = bottomView.findViewById(R.id.btnApply);
            MaterialButton btnReset = bottomView.findViewById(R.id.btnReset);
            AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);
            AppCompatSpinner typeSpinner = bottomView.findViewById(R.id.typeSpinner);
            tvUntilDate = bottomView.findViewById(R.id.tvUntilDate);
            AppCompatEditText etRepeatEvery = bottomView.findViewById(R.id.etRepeatEvery);
            AppCompatEditText etRepeatTime = bottomView.findViewById(R.id.etRepeatTime);
            RadioGroup radioMonthly = bottomView.findViewById(R.id.radioMonthly);
            AppCompatRadioButton radioMonthlyLastDay = bottomView.findViewById(R.id.radioMonthlyLastDay);
            AppCompatRadioButton radioMonthlyDay = bottomView.findViewById(R.id.radioMonthlyDay);

            ConstraintLayout sunWrapper = bottomView.findViewById(R.id.sunWrapper);
            ConstraintLayout monWrapper = bottomView.findViewById(R.id.monWrapper);
            ConstraintLayout tueWrapper = bottomView.findViewById(R.id.tueWrapper);
            ConstraintLayout wedWrapper = bottomView.findViewById(R.id.wedWrapper);
            ConstraintLayout thuWrapper = bottomView.findViewById(R.id.thuWrapper);
            ConstraintLayout friWrapper = bottomView.findViewById(R.id.friWrapper);
            ConstraintLayout satWrapper = bottomView.findViewById(R.id.satWrapper);
            AppCompatTextView sunLabel = bottomView.findViewById(R.id.sunLabel);
            AppCompatTextView monLabel = bottomView.findViewById(R.id.monLabel);
            AppCompatTextView tueLabel = bottomView.findViewById(R.id.tueLabel);
            AppCompatTextView wedLabel = bottomView.findViewById(R.id.wedLabel);
            AppCompatTextView thuLabel = bottomView.findViewById(R.id.thuLabel);
            AppCompatTextView friLabel = bottomView.findViewById(R.id.friLabel);
            AppCompatTextView satLabel = bottomView.findViewById(R.id.satLabel);

            Date currentDate = DateHelper.getCurrentDateTime();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            if (monthlyDay <= 0) {
                monthlyDay = calendar.get(Calendar.DAY_OF_MONTH);
            }
            radioMonthlyDay.setText(getString(R.string.same_day_each_month, monthlyDay));

            List<String> recurringRepeatTypes = Arrays.asList(getString(R.string.forever), getString(R.string.until), getString(R.string.text_for));
            FontSpinnerAdapter fontSpinnerAdapter = new FontSpinnerAdapter(this, R.layout.list_drop_down_color, R.id.label, recurringRepeatTypes);
            typeSpinner.setAdapter(fontSpinnerAdapter);
            typeSpinner.setSelection(repeatEndType);

            etRepeatEvery.setText(String.valueOf(repeatInterval));

            etRepeatTime.setText(String.valueOf(repeatTimes));
            lblTimes.setText(getResources().getQuantityString(R.plurals.times_period, repeatTimes, repeatTimes));

            // Restore monthly selection
            if (monthlyMode == Constants.MONTHLY_LAST_DAY) {
                radioMonthlyLastDay.setChecked(true);
            } else {
                radioMonthlyDay.setChecked(true);
            }

            // Restore existing until date
            if (untilDate > 0) {
                date = new Date(untilDate);
                tvUntilDate.setText(DateHelper.getFormattedDate(date));
            } else {
                date = DateHelper.getCurrentDateTime();
                tvUntilDate.setText(DateHelper.getFormattedDate(date));
            }

            typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    repeatEndType = position;

                    if (position == 0) {
                        frameUntilDate.setVisibility(View.GONE);
                        frameTimes.setVisibility(View.GONE);
                        lblTimes.setVisibility(View.GONE);
                    } else if (position == 1) {
                        frameUntilDate.setVisibility(View.VISIBLE);
                        frameTimes.setVisibility(View.GONE);
                        lblTimes.setVisibility(View.GONE);
                    } else if (position == 2) {
                        frameUntilDate.setVisibility(View.GONE);
                        frameTimes.setVisibility(View.VISIBLE);
                        lblTimes.setVisibility(View.VISIBLE);

                        if (repeatTimes < 1) {
                            repeatTimes = 1;
                            etRepeatTime.setText("1");
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            switch (selectedRepeatType) {
                case Constants.REPEAT_NONE, Constants.REPEAT_DAILY:
                    weekView.setVisibility(View.GONE);
                    radioMonthly.setVisibility(View.GONE);
                    tvEveryRecurring.setText(getResources().getQuantityString(R.plurals.day_period, repeatInterval, repeatInterval));
                    break;
                case Constants.REPEAT_WEEKLY:
                    weekView.setVisibility(View.VISIBLE);
                    radioMonthly.setVisibility(View.GONE);
                    tvEveryRecurring.setText(getResources().getQuantityString(R.plurals.weeks_period, repeatInterval, repeatInterval));
                    break;
                case Constants.REPEAT_MONTHLY:
                    weekView.setVisibility(View.GONE);
                    radioMonthly.setVisibility(View.VISIBLE);
                    tvEveryRecurring.setText(getResources().getQuantityString(R.plurals.months_period, repeatInterval, repeatInterval));
                    break;
                case Constants.REPEAT_YEARLY:
                    weekView.setVisibility(View.GONE);
                    radioMonthly.setVisibility(View.GONE);
                    tvEveryRecurring.setText(getResources().getQuantityString(R.plurals.years_period, repeatInterval, repeatInterval));
                    break;
            }

            sunWrapper.setOnClickListener(v -> toggleWeekDay(sunWrapper, Calendar.SUNDAY, sunLabel));
            monWrapper.setOnClickListener(v -> toggleWeekDay(monWrapper, Calendar.MONDAY, monLabel));
            tueWrapper.setOnClickListener(v -> toggleWeekDay(tueWrapper, Calendar.TUESDAY, tueLabel));
            wedWrapper.setOnClickListener(v -> toggleWeekDay(wedWrapper, Calendar.WEDNESDAY, wedLabel));
            thuWrapper.setOnClickListener(v -> toggleWeekDay(thuWrapper, Calendar.THURSDAY, thuLabel));
            friWrapper.setOnClickListener(v -> toggleWeekDay(friWrapper, Calendar.FRIDAY, friLabel));
            satWrapper.setOnClickListener(v -> toggleWeekDay(satWrapper, Calendar.SATURDAY, satLabel));

            updateWeekDaySelection(sunWrapper, Calendar.SUNDAY, sunLabel);
            updateWeekDaySelection(monWrapper, Calendar.MONDAY, monLabel);
            updateWeekDaySelection(tueWrapper, Calendar.TUESDAY, tueLabel);
            updateWeekDaySelection(wedWrapper, Calendar.WEDNESDAY, wedLabel);
            updateWeekDaySelection(thuWrapper, Calendar.THURSDAY, thuLabel);
            updateWeekDaySelection(friWrapper, Calendar.FRIDAY, friLabel);
            updateWeekDaySelection(satWrapper, Calendar.SATURDAY, satLabel);

            etRepeatEvery.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    String value = s.toString().trim();
                    int repeatEvery = 0;
                    if (!value.isEmpty()) {
                        repeatEvery = Integer.parseInt(value);
                    }

                    repeatInterval = repeatEvery;

                    switch (selectedRepeatType) {
                        case Constants.REPEAT_DAILY:
                            tvEveryRecurring.setText(getResources().getQuantityString(R.plurals.day_period, repeatInterval, repeatInterval));
                            break;
                        case Constants.REPEAT_WEEKLY:
                            tvEveryRecurring.setText(getResources().getQuantityString(R.plurals.weeks_period, repeatInterval, repeatInterval));
                            break;
                        case Constants.REPEAT_MONTHLY:
                            tvEveryRecurring.setText(getResources().getQuantityString(R.plurals.months_period, repeatInterval, repeatInterval));
                            break;
                        case Constants.REPEAT_YEARLY:
                            tvEveryRecurring.setText(getResources().getQuantityString(R.plurals.years_period, repeatInterval, repeatInterval));
                            break;
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });

            etRepeatTime.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    String value = s.toString().trim();
                    int repeatTime = 0;
                    if (!value.isEmpty()) {
                        repeatTime = Integer.parseInt(value);
                    }

                    repeatTimes = repeatTime;
                    lblTimes.setText(getResources().getQuantityString(R.plurals.times_period, repeatTimes, repeatTimes));
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });

            tvUntilDate.setOnClickListener(view -> {
                hideKeyboard(this);
                openDateDialog();
            });

            frameUntilDate.setOnClickListener(view -> {
                hideKeyboard(this);
                openDateDialog();
            });

            radioMonthly.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.radioMonthlyDay) {
                    monthlyMode = Constants.MONTHLY_DAY;
                } else if (checkedId == R.id.radioMonthlyLastDay) {
                    monthlyMode = Constants.MONTHLY_LAST_DAY;
                }
            });

            btnApply.setOnClickListener(v -> {
                if (repeatInterval < 1) {
                    Toast.makeText(this, R.string.repeat_interval_atleast, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (repeatEndType == 1 && untilDate <= 0) {
                    Toast.makeText(this, R.string.please_select_until_date, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (repeatEndType == 2 && repeatTimes < 1) {
                    Toast.makeText(this, R.string.please_enter_times, Toast.LENGTH_SHORT).show();
                    return;
                }

                updateRepeatOnText();
                dialog.dismiss();
            });

            btnReset.setOnClickListener(v -> dialog.dismiss());

            tvClose.setOnClickListener(v -> dialog.dismiss());

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showRepeatOnBottomSheet", e);
        }
    }

    private void toggleWeekDay(ConstraintLayout wrapper, int day, AppCompatTextView label) {
        if (selectedWeekDays.contains(day)) {
            selectedWeekDays.remove(day);
            wrapper.setBackgroundResource(R.drawable.bg_recurring_weekly_uncheck);
            label.setTextColor(ContextCompat.getColor(this, R.color.dark_grey));
        } else {
            selectedWeekDays.add(day);
            wrapper.setBackgroundResource(R.drawable.bg_recurring_weekly_checked);
            label.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    private void updateWeekDaySelection(ConstraintLayout wrapper, int day, AppCompatTextView label) {
        boolean selected = selectedWeekDays.contains(day);
        wrapper.setBackgroundResource(selected ? R.drawable.bg_recurring_weekly_checked : R.drawable.bg_recurring_weekly_uncheck);
        label.setTextColor(ContextCompat.getColor(this, selected ? R.color.white : R.color.dark_grey));
    }

    private void updateRepeatOnText() {
        switch (selectedRepeatType) {
            case Constants.REPEAT_WEEKLY:
                if (selectedWeekDays.isEmpty()) {
                    tvRepeatOn.setText(getString(R.string.select_days));
                } else {
                    String intervalText = getResources().getQuantityString(R.plurals.weeks_period_value, repeatInterval, repeatInterval);
                    tvRepeatOn.setText(getString(R.string.text_every_value, intervalText, getSelectedWeekDaysText()));
                }
                break;
            case Constants.REPEAT_MONTHLY:
                if (monthlyMode == Constants.MONTHLY_LAST_DAY) {
                    tvRepeatOn.setText(getResources().getQuantityString(R.plurals.every_months_last_day, repeatInterval, repeatInterval));
                } else {
                    tvRepeatOn.setText(getResources().getQuantityString(R.plurals.every_months_day, repeatInterval, repeatInterval, monthlyDay));
                }
                break;
            case Constants.REPEAT_YEARLY:
                tvRepeatOn.setText(getResources().getQuantityString(R.plurals.every_year_period, repeatInterval, repeatInterval));
                break;
            case Constants.REPEAT_DAILY:
                String dayText = getResources().getQuantityString(R.plurals.day_period_value, repeatInterval, repeatInterval);
                tvRepeatOn.setText(getString(R.string.text_every_value1, dayText));
                break;
        }
    }

    private String getSelectedWeekDaysText() {
        StringBuilder result = new StringBuilder();

        if (selectedWeekDays.contains(Calendar.MONDAY))
            result.append("Mon, ");

        if (selectedWeekDays.contains(Calendar.TUESDAY))
            result.append("Tue, ");

        if (selectedWeekDays.contains(Calendar.WEDNESDAY))
            result.append("Wed, ");

        if (selectedWeekDays.contains(Calendar.THURSDAY))
            result.append("Thu, ");

        if (selectedWeekDays.contains(Calendar.FRIDAY))
            result.append("Fri, ");

        if (selectedWeekDays.contains(Calendar.SATURDAY))
            result.append("Sat, ");

        if (selectedWeekDays.contains(Calendar.SUNDAY))
            result.append("Sun, ");

        if (result.length() >= 2) {
            result.setLength(result.length() - 2);
        }

        return result.toString();
    }

    public void openDateDialog() {

        hideKeyboard(this);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.CustomDateTimePickerDialog, this, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
        int color = ContextCompat.getColor(this, R.color.vibrant_orange);
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(color);
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(color);
    }

    private void updateRepeatText() {
        switch (selectedRepeatType) {
            case Constants.REPEAT_NONE:
                tvRepeat.setText(getString(R.string.no_repeat));
                cardRepeatOn.setVisibility(View.GONE);
                break;
            case Constants.REPEAT_DAILY:
                tvRepeat.setText(getString(R.string.calendar_daily));
                cardRepeatOn.setVisibility(View.GONE);
                break;
            case Constants.REPEAT_WEEKLY:
                tvRepeat.setText(getString(R.string.calendar_weekly));
                cardRepeatOn.setVisibility(View.VISIBLE);
                break;
            case Constants.REPEAT_MONTHLY:
                tvRepeat.setText(getString(R.string.calendar_monthly));
                cardRepeatOn.setVisibility(View.VISIBLE);
                break;
            case Constants.REPEAT_YEARLY:
                tvRepeat.setText(getString(R.string.calendar_yearly));
                cardRepeatOn.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateAmountText() {
        String symbol;
        if (transactionType == TransactionEntity.TYPE_TRANSFER) {
            symbol = selectedFromWallet != null ? selectedFromWallet.currencySymbol : account.currencySymbol;
        } else {
            symbol = selectedWallet != null ? selectedWallet.currencySymbol : account.currencySymbol;
        }
        tvAmount.setText(CommonUtils.getBeautifyAmount(symbol, transactionAmount));
        tvFee.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, transactionFee));
    }

    private void saveRecurringTransaction() {

        if (!validateRecurringSettings()) {
            return;
        }

        // 1. Validate amount
        if (transactionAmount <= 0) {
            Toast.makeText(this, R.string.please_enter_an_amount, Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Validate wallet
        if (selectedWallet == null) {
            Toast.makeText(this, R.string.please_select_a_wallet, Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Validate recurrence
        if (selectedRepeatType == Constants.REPEAT_NONE) {
            Toast.makeText(this, R.string.please_select_a_repeat_frequency, Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Validate repeat interval
        if (repeatInterval < 1) {
            repeatInterval = 1;
        }

        boolean isEdit = recurringTransactionWithDetails != null;
        RecurringTransactionEntity entity;
        if (isEdit) {
            entity = recurringTransactionWithDetails.recurringTransaction;
        } else {
            entity = new RecurringTransactionEntity();
        }

        // --------------------------------------------------
        // Transaction
        // --------------------------------------------------
        entity.accountId = PreferenceManager.INSTANCE.getAccountId();
        entity.type = transactionType;
        entity.walletId = selectedWallet.id;
        entity.fromWalletId = selectedFromWallet != null ? selectedFromWallet.id : 0;
        entity.amount = transactionAmount;
        entity.fee = transactionFee;
        entity.description = Objects.requireNonNull(etDescription.getText()).toString().trim();
        entity.notes = Objects.requireNonNull(etNotes.getText()).toString().trim();

        // --------------------------------------------------
        // Category
        // --------------------------------------------------
        if (transactionType == TransactionEntity.TYPE_INCOME) {
            entity.categoryId = incomeCategory != null ? incomeCategory.id : 0;
            entity.defaultCategoryId = incomeCategory != null ? incomeCategory.defaultCategory : 0;
        } else if (transactionType == TransactionEntity.TYPE_EXPENSE) {
            entity.categoryId = expenseCategory != null ? expenseCategory.id : 0;
            entity.defaultCategoryId = expenseCategory != null ? expenseCategory.defaultCategory : 0;
        } else {
            CategoryEntity transferCategory = getTransferCategoryId();
            entity.categoryId = transferCategory.id;
            entity.defaultCategoryId = transferCategory.defaultCategory;
        }

        // --------------------------------------------------
        // Repeat settings
        // --------------------------------------------------
        entity.repeatType = selectedRepeatType;
        entity.repeatInterval = repeatInterval;
        entity.repeatWeekDays = getSelectedWeekDaysString();
        entity.monthlyMode = monthlyMode;

        long now = System.currentTimeMillis();

        if (!isEdit) {
            entity.startDate = now;
        }

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(entity.startDate);

        if (selectedRepeatType == Constants.REPEAT_MONTHLY) {
            entity.monthlyDay = monthlyDay;
        } else {
            entity.monthlyDay = startCalendar.get(Calendar.DAY_OF_MONTH);
        }

        if (!isEdit) {
            entity.yearlyMonth = startCalendar.get(Calendar.MONTH);
            entity.yearlyDay = startCalendar.get(Calendar.DAY_OF_MONTH);
        }

        // --------------------------------------------------
        // Dates
        // --------------------------------------------------
        entity.untilDate = untilDate;
        entity.repeatTimes = repeatTimes;
        if (!isEdit) {
            entity.completedTimes = 0;
        }
        entity.status = RecurringTransactionEntity.STATUS_IN_PROGRESS;

        // --------------------------------------------------
        // Transaction creation
        // --------------------------------------------------
        entity.reminder = switchReminder.isChecked();
        entity.createTransaction = switchCreateTransaction.isChecked();
        entity.isActive = true;

        if (!isEdit) {
            entity.createdAt = now;
        }

        entity.updatedAt = now;

        if (!isEdit) {
            entity.tempRecurringServerId = "RT_" + now;
            entity.serverId = 0;
            entity.isSynced = false;
            entity.isDeleted = false;
        } else {
            entity.isSynced = false;
        }

        // --------------------------------------------------
        // Calculate first run
        // --------------------------------------------------
        if (!isEdit || hasRecurrenceChanged()) {
            entity.nextRunDate = calculateFirstRunDate(entity);
        }

        // --------------------------------------------------
        // Save
        // --------------------------------------------------

        if (isEdit) {
            recurringTransactionViewModel.update(entity);
        } else {
            recurringTransactionViewModel.insert(entity);
        }
    }

    private CategoryEntity getTransferCategoryId() {

        List<Integer> transferIds = new ArrayList<>();
        transferIds.add(TransactionEntity.TYPE_EXPENSE);
        transferIds.add(TransactionEntity.TYPE_TRANSFER);

        return categoryViewModel.getDefaultCategoryByType(Constants.DEFAULT_CATEGORY_TRANSFER_ID, transferIds);
    }

    private String getSelectedWeekDaysString() {
        return TextUtils.join(",", selectedWeekDays);
    }

    private long calculateFirstRunDate(RecurringTransactionEntity entity) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(entity.startDate);

        return switch (entity.repeatType) {
            case Constants.REPEAT_DAILY -> {
                calendar.add(Calendar.DAY_OF_MONTH, entity.repeatInterval);
                yield calendar.getTimeInMillis();
            }
            case Constants.REPEAT_WEEKLY -> calculateFirstWeeklyRunDate(calendar, entity);
            case Constants.REPEAT_MONTHLY -> calculateFirstMonthlyRunDate(calendar, entity);
            case Constants.REPEAT_YEARLY -> calculateFirstYearlyRunDate(calendar, entity);
            default -> calendar.getTimeInMillis();
        };
    }

    private long calculateFirstWeeklyRunDate(Calendar calendar, RecurringTransactionEntity entity) {

        if (entity.repeatWeekDays == null || entity.repeatWeekDays.trim().isEmpty()) {
            calendar.add(Calendar.WEEK_OF_YEAR, entity.repeatInterval);
            return calendar.getTimeInMillis();
        }

        String[] days = entity.repeatWeekDays.split(",");

        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
        int nearestDay = -1;
        int nearestDifference = Integer.MAX_VALUE;

        for (String value : days) {
            try {
                int day = Integer.parseInt(value.trim());

                int difference = (day - currentDay + 7) % 7;
                if (difference == 0) {
                    difference = 7;
                }

                if (difference < nearestDifference) {
                    nearestDifference = difference;
                    nearestDay = day;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        if (nearestDay == -1) {
            calendar.add(Calendar.WEEK_OF_YEAR, entity.repeatInterval);
            return calendar.getTimeInMillis();
        }

        calendar.add(Calendar.DAY_OF_MONTH, nearestDifference);

        if (entity.repeatInterval > 1) {
            calendar.add(
                    Calendar.WEEK_OF_YEAR,
                    entity.repeatInterval - 1
            );
        }

        return calendar.getTimeInMillis();
    }

    private long calculateFirstMonthlyRunDate(Calendar calendar, RecurringTransactionEntity entity) {
        if (entity.monthlyMode == Constants.MONTHLY_LAST_DAY) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, entity.repeatInterval);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        } else {
            int originalDay = entity.monthlyDay;
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, entity.repeatInterval);
            int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            calendar.set(Calendar.DAY_OF_MONTH, Math.min(originalDay, maxDay));
        }
        return calendar.getTimeInMillis();
    }

    private long calculateFirstYearlyRunDate(Calendar calendar, RecurringTransactionEntity entity) {
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.YEAR, entity.repeatInterval);
        calendar.set(Calendar.MONTH, currentMonth);
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, Math.min(currentDay, maxDay));
        return calendar.getTimeInMillis();
    }

    private boolean validateRecurringSettings() {

        // Repeat type
        if (selectedRepeatType == Constants.REPEAT_NONE) {
            Toast.makeText(this, R.string.please_select_a_repeat_frequency, Toast.LENGTH_SHORT).show();
            return false;
        }

        // Repeat interval
        if (repeatInterval < 1) {
            Toast.makeText(this, R.string.repeat_interval_atleast, Toast.LENGTH_SHORT).show();
            return false;
        }

        // Weekly
        if (selectedRepeatType == Constants.REPEAT_WEEKLY) {
            if (selectedWeekDays.isEmpty()) {
                Toast.makeText(this, R.string.please_select_at_least_one_day, Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Repeat ending
        return validateRepeatEnd();
    }

    private boolean validateRepeatEnd() {

        // Forever
        if (repeatTimes == 0 && untilDate == 0) {
            return true;
        }

        // Until date
        if (untilDate > 0) {
            long startDate = System.currentTimeMillis();
            if (untilDate <= startDate) {
                Toast.makeText(this, R.string.until_date_start_date, Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }

        // For X times
        if (repeatTimes > 0) {
            return true;
        }

        Toast.makeText(this, R.string.select_recurring_transaction_end, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void updateSaveButtonState() {

        boolean enabled = transactionAmount > 0 && selectedWallet != null;

        switch (transactionType) {

            case TransactionEntity.TYPE_INCOME:
                enabled &= incomeCategory != null;
                break;

            case TransactionEntity.TYPE_EXPENSE:
                enabled &= expenseCategory != null;
                break;

            case TransactionEntity.TYPE_TRANSFER:
                enabled &= selectedFromWallet != null && selectedWallet != null && selectedWallet.id != selectedFromWallet.id;
                break;
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

    private void resetRepeatOnSettings() {
        selectedWeekDays.clear();
        repeatInterval = 1;
        repeatTimes = 0;
        repeatEndType = 0;
        monthlyMode = Constants.MONTHLY_DAY;
        Calendar calendar = Calendar.getInstance();
        monthlyDay = calendar.get(Calendar.DAY_OF_MONTH);
        untilDate = 0;
    }

    private void applyRepeatType(int newRepeatType) {
        if (selectedRepeatType != newRepeatType) {
            resetRepeatOnSettings();
        }

        selectedRepeatType = newRepeatType;
        updateRepeatText();

        if (selectedRepeatType == Constants.REPEAT_NONE) {
            cardRepeatOn.setVisibility(View.GONE);
            tvRepeatOn.setText("");
        } else {
            cardRepeatOn.setVisibility(View.VISIBLE);
            updateRepeatOnText();
        }
    }

    private boolean hasRecurrenceChanged() {
        if (originalRepeatType != selectedRepeatType) {
            return true;
        }

        if (originalRepeatInterval != repeatInterval) {
            return true;
        }

        if (originalMonthlyMode != monthlyMode) {
            return true;
        }

        if (originalMonthlyDay != monthlyDay) {
            return true;
        }

        StringBuilder currentWeekDaysBuilder = new StringBuilder();

        for (Integer day : selectedWeekDays) {
            if (!TextUtils.isEmpty(currentWeekDaysBuilder.toString())) {
                currentWeekDaysBuilder.append(",");
            }
            currentWeekDaysBuilder.append(day);
        }

        String currentWeekDays = currentWeekDaysBuilder.toString();
        String originalWeekDays = originalRepeatWeekDays == null ? "" : originalRepeatWeekDays;
        return !currentWeekDays.equals(originalWeekDays);
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        date = calendar.getTime();
        untilDate = calendar.getTimeInMillis();
        tvUntilDate.setText(DateHelper.getDateFromPicker(year, month, dayOfMonth));
    }
}