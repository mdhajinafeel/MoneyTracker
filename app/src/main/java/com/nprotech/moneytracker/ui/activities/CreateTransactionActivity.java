package com.nprotech.moneytracker.ui.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.CategoryEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateTransactionActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private AppCompatImageView icBack;
    private AppCompatTextView tvTitle, tvAmount, tvSave, incomeLabel, expenseLabel, transferLabel, tvDay, tvHour, tvCategory, tvFee, tvFromWallet, tvWallet, walletLabel;
    private AppCompatEditText etDescription, etMemo;
    private ActivityResultLauncher<Intent> calculatorLauncher, categoryLauncher;
    private ConstraintLayout incomeWrapper, expenseWrapper, transferWrapper, clFromWallet, clFee, clCategory;
    private NestedScrollView scrollView;
    private Date date;
    private AccountEntity account;
    private WalletEntity selectedWallet, selectedFromWallet;
    private AccountViewModel accountViewModel;
    private List<WalletEntity> walletLists;
    private CategoryEntity incomeCategory, expenseCategory;
    private int transactionType;
    private double transactionAmount = 0, transactionFee = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_transaction);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            scrollView = findViewById(R.id.scrollView);
            tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            tvSave = toolbarWrapper.findViewById(R.id.tvSave);
            icBack = toolbarWrapper.findViewById(R.id.icBack);

            incomeWrapper = findViewById(R.id.incomeWrapper);
            expenseWrapper = findViewById(R.id.expenseWrapper);
            transferWrapper = findViewById(R.id.transferWrapper);
            incomeLabel = findViewById(R.id.incomeLabel);
            expenseLabel = findViewById(R.id.expenseLabel);
            transferLabel = findViewById(R.id.transferLabel);
            walletLabel = findViewById(R.id.walletLabel);

            tvDay = findViewById(R.id.tvDay);
            tvHour = findViewById(R.id.tvHour);
            tvAmount = findViewById(R.id.tvAmount);
            etDescription = findViewById(R.id.etDescription);
            tvCategory = findViewById(R.id.tvCategory);
            tvFromWallet = findViewById(R.id.tvFromWallet);
            tvWallet = findViewById(R.id.tvWallet);
            tvFee = findViewById(R.id.tvFee);
            etMemo = findViewById(R.id.etMemo);
            clCategory = findViewById(R.id.clCategory);
            clFromWallet = findViewById(R.id.clFromWallet);
            clFee = findViewById(R.id.clFee);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(scrollView, (view, insets) -> {
                Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
                view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), imeInsets.bottom);
                return insets;
            });

            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {
                transactionType = bundle.getInt("type");
                switchTransMode(transactionType);
            }

            accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

            backPressed();
            makeReadOnly();
            setupListeners();
            setupLauncher();
            bindData();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {
            account = accountViewModel.getAccountDetailById((int) PreferenceManager.INSTANCE.getAccountId());
            walletLists = accountViewModel.getWalletsByAccountId((int) PreferenceManager.INSTANCE.getAccountId());

            date = DateHelper.getCurrentDateTime();
            tvSave.setVisibility(View.VISIBLE);
            tvSave.setEnabled(false);
            enabledSaveOption();

            tvDay.setText(DateHelper.getFormattedDate(date));
            tvHour.setText(DateHelper.getFormattedTime(getApplicationContext(), date));
            tvAmount.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, 0));
            tvFee.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, 0));

            if (!walletLists.isEmpty()) {
                selectedWallet = walletLists.get(0);
                tvWallet.setText(getString(R.string.wallet_info, selectedWallet.name,
                        CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol, selectedWallet.initialAmount)));
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
            icBack.setOnClickListener(view -> {
                finish();
                overridePendingTransition(R.anim.scale_in, R.anim.bottom_to_top);
            });

            tvDay.setOnClickListener(view -> openDateDialog());
            tvHour.setOnClickListener(view -> openHourDialog());

            tvAmount.setOnClickListener(view -> {
                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", transactionAmount);
                intent.putExtra("type", "amount");
                calculatorLauncher.launch(intent);
                overridePendingTransition(R.anim.left_to_right, R.anim.scale_out);
            });

            tvWallet.setOnClickListener(view -> selectWallets("to"));

            tvFromWallet.setOnClickListener(view -> selectWallets("from"));

            incomeWrapper.setOnClickListener(view -> {
                switchTransMode(1);
                transactionType = 1;
            });

            expenseWrapper.setOnClickListener(view -> {
                switchTransMode(2);
                transactionType = 2;
            });

            transferWrapper.setOnClickListener(view -> {
                switchTransMode(3);
                transactionType = 3;
            });

            tvCategory.setOnClickListener(view -> {
                Intent intent = new Intent(this, CategoryPickerActivity.class);
                intent.putExtra("transactionType", transactionType);
                categoryLauncher.launch(intent);
                overridePendingTransition(R.anim.left_to_right, R.anim.scale_out);
            });

            tvFee.setOnClickListener(view -> {
                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", transactionFee);
                intent.putExtra("type", "fee");
                calculatorLauncher.launch(intent);
                overridePendingTransition(R.anim.left_to_right, R.anim.scale_out);
            });

            etMemo.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    scrollView.post(() -> scrollView.smoothScrollTo(0, v.getBottom()));
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "clickListeners", e);
        }
    }

    public void openDateDialog() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.CustomDateTimePickerDialog, this, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
        int color = ContextCompat.getColor(this, R.color.vibrant_orange);
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(color);
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(color);
    }

    public void openHourDialog() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this.date);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, R.style.CustomDateTimePickerDialog, this, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(getApplicationContext()));
        timePickerDialog.show();
        int color = ContextCompat.getColor(this, R.color.vibrant_orange);
        timePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(color);
        timePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(color);
    }

    private void switchTransMode(int mode) {
        try {
            if (mode == 1) {

                tvTitle.setText(getString(R.string.income));

                incomeWrapper.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.azure_blue));
                expenseWrapper.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.vibrant_orange));
                transferWrapper.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.vibrant_orange));

                incomeLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                expenseLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                transferLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));

                clFee.setVisibility(View.GONE);
                clFromWallet.setVisibility(View.GONE);
                clCategory.setVisibility(View.VISIBLE);

                walletLabel.setText(getString(R.string.wallet));
                if(incomeCategory != null) {
                    tvCategory.setText(incomeCategory.getName(getApplicationContext()));
                } else {
                    tvCategory.setText("");
                }
            } else if (mode == 2) {

                tvTitle.setText(getString(R.string.expense));

                incomeWrapper.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.vibrant_orange));
                expenseWrapper.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.bright_red));
                transferWrapper.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.vibrant_orange));

                incomeLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                expenseLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                transferLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));

                clFee.setVisibility(View.GONE);
                clFromWallet.setVisibility(View.GONE);
                clCategory.setVisibility(View.VISIBLE);

                walletLabel.setText(getString(R.string.wallet));
                if(expenseCategory != null) {
                    tvCategory.setText(expenseCategory.getName(getApplicationContext()));
                } else {
                    tvCategory.setText("");
                }
            } else if (mode == 3) {

                tvTitle.setText(getString(R.string.transfer));

                incomeWrapper.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.vibrant_orange));
                expenseWrapper.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.vibrant_orange));
                transferWrapper.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_grey));

                incomeLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                expenseLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                transferLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));

                clFee.setVisibility(View.VISIBLE);
                clFromWallet.setVisibility(View.VISIBLE);
                clCategory.setVisibility(View.GONE);

                walletLabel.setText(getString(R.string.to_wallet));
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "switchTransMode", e);
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

                            if(type!= null && type.equalsIgnoreCase("amount")) {
                                transactionAmount = amount;
                                tvAmount.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, amount));
                            } else if(type!= null && type.equalsIgnoreCase("fee")) {
                                transactionFee = amount;
                                tvFee.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, amount));
                            }
                        }
                    }
                });

        categoryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            CategoryEntity categoryEntity = (CategoryEntity) data.getSerializableExtra("category");
                            if (categoryEntity != null) {
                                if (transactionType == 1) {
                                    incomeCategory = categoryEntity;
                                } else {
                                    expenseCategory = categoryEntity;
                                }

                                tvCategory.setText(categoryEntity.getName(getApplicationContext()));
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
            RecyclerViewAdapter<WalletEntity> adapter = new RecyclerViewAdapter<>(getApplicationContext(), walletLists, R.layout.item_switch_accounts) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, WalletEntity walletEntity) {

                    holder.setViewText(R.id.tvAccountName, walletEntity.name);

                    holder.setViewText(R.id.tvAccountBalance, getString(R.string.account_balance_format,
                            CommonUtils.getBeautifyAmount(walletEntity.currencySymbol, walletEntity.initialAmount)));

                    if(type.equalsIgnoreCase("to")) {
                        holder.getView(R.id.ivSelected).setVisibility(selectedWallet.id == walletEntity.id ? View.VISIBLE : View.GONE);

                        holder.getView(R.id.rlAccountView).setOnClickListener(v -> {
                            selectedWallet = walletEntity;
                            dialog.dismiss();
                        });
                    } else if(type.equalsIgnoreCase("from")) {
                        holder.getView(R.id.ivSelected).setVisibility(selectedFromWallet.id == walletEntity.id ? View.VISIBLE : View.GONE);

                        holder.getView(R.id.rlAccountView).setOnClickListener(v -> {
                            selectedFromWallet = walletEntity;
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

    private void validateFields() {
        try {


        } catch (Exception e) {
            AppLogger.e(getClass(), "validateFields", e);
        }
    }

    private void enabledSaveOption() {
        try {
            if (tvSave.isEnabled()) {
                tvSave.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            } else {
                tvSave.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.funky_grey));
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "enabledSaveOption", e);
        }
    }

    private void backPressed() {
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        finish();
                        overridePendingTransition(R.anim.scale_in, R.anim.bottom_to_top);
                    }
                });
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        tvDay.setText(DateHelper.getDateFromPicker(getApplicationContext(), i, i1, i2));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(1, i);
        calendar.set(2, i1);
        calendar.set(5, i2);
        this.date = calendar.getTime();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        tvHour.setText(DateHelper.getTimeFromPicker(this, i, i1));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(11, i);
        calendar.set(12, i1);
        this.date = calendar.getTime();
    }
}