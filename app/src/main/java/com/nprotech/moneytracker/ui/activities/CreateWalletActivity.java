package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.AccountCurrencyMappingEntity;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.ui.adapters.ColorSpinnerAdapter;
import com.nprotech.moneytracker.ui.adapters.CurrencySpinnerAdapter;
import com.nprotech.moneytracker.ui.adapters.FontSpinnerAdapter;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.utils.IntentUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.WalletViewModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateWalletActivity extends BaseActivity {

    private AppCompatImageView icBack, ivWalletIcon;
    private AppCompatEditText etWalletName;
    private AppCompatTextView tvSave, tvTitle, amountLabel, tvAmount, rateLabel, maxLimitLabel;
    private AppCompatSpinner typeSpinner, colorSpinner, currencySpinner, statementDateSpinner, paymentDateSpinner;
    private FrameLayout frameColor;
    private MaterialCardView cardWalletExclude, cardWalletStatement, cardWalletPayment;
    private ActivityResultLauncher<Intent> calculatorLauncher, walletIconLauncher, currencyLauncher;
    private boolean isEdit = false;
    private double walletAmount = 0;
    private AccountEntity account;
    private AccountViewModel accountViewModel;
    private WalletViewModel walletViewModel;
    private ArrayList<String> walletColorLists;
    private int walletIcon = 0, walletId = 0;
    private WalletEntity walletEntity;
    private SwitchCompat switchExcludeView;
    private String selectedCurrencyCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_wallet);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            View root = findViewById(R.id.rootView);
            tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            tvSave = toolbarWrapper.findViewById(R.id.tvSave);
            icBack = toolbarWrapper.findViewById(R.id.icBack);

            tvSave.setVisibility(View.VISIBLE);

            etWalletName = findViewById(R.id.etWalletName);
            maxLimitLabel = findViewById(R.id.maxLimitLabel);
            typeSpinner = findViewById(R.id.typeSpinner);
            frameColor = findViewById(R.id.frameColor);
            colorSpinner = findViewById(R.id.colorSpinner);
            currencySpinner = findViewById(R.id.currencySpinner);
            statementDateSpinner = findViewById(R.id.statementDateSpinner);
            paymentDateSpinner = findViewById(R.id.paymentDateSpinner);
            ivWalletIcon = findViewById(R.id.ivWalletIcon);
            amountLabel = findViewById(R.id.amountLabel);
            tvAmount = findViewById(R.id.tvAmount);
            rateLabel = findViewById(R.id.rateLabel);
            cardWalletExclude = findViewById(R.id.cardWalletExclude);
            cardWalletStatement = findViewById(R.id.cardWalletStatement);
            cardWalletPayment = findViewById(R.id.cardWalletPayment);
            switchExcludeView = findViewById(R.id.switchExcludeView);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
            walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                isEdit = bundle.getBoolean("isEdit");
                walletId = bundle.getInt("walletId", 0);

                makeReadOnly();
                initializeAdapters();
                bindData(isEdit);
                setupLauncher();
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

    private void initializeAdapters() {
        try {
            List<String> walletTypes = Arrays.asList(getString(R.string.general), getString(R.string.cash), getString(R.string.bank), getString(R.string.credit_card), getString(R.string.debit_card));

            FontSpinnerAdapter fontSpinnerAdapter = new FontSpinnerAdapter(this, R.layout.list_drop_down_color, R.id.label, walletTypes);
            typeSpinner.setAdapter(fontSpinnerAdapter);

            walletColorLists = new ArrayList<>();
            walletColorLists = DataHelper.getColorList();
            ColorSpinnerAdapter colorSpinnerAdapter = new ColorSpinnerAdapter(this, R.layout.list_drop_down_color, R.id.label, walletColorLists);
            colorSpinner.setAdapter(colorSpinnerAdapter);

            List<String> dateList = DateHelper.getMonthDates();
            FontSpinnerAdapter statementAdapter = new FontSpinnerAdapter(this, R.layout.list_drop_down_color, R.id.label, dateList);
            FontSpinnerAdapter paymentAdapter = new FontSpinnerAdapter(this, R.layout.list_drop_down_color, R.id.label, dateList);

            statementDateSpinner.setAdapter(statementAdapter);
            paymentDateSpinner.setAdapter(paymentAdapter);

            // Set max dropdown height
            try {
                Field popupField = AppCompatSpinner.class.getDeclaredField("mPopup");
                popupField.setAccessible(true);

                ListPopupWindow popup = (ListPopupWindow) popupField.get(colorSpinner);
                if (popup != null) {
                    popup.setHeight(getResources().getDimensionPixelSize(R.dimen.spinner_dropdown_max_height));
                }
            } catch (Exception e) {
                AppLogger.e(getClass(), "ListPopupWindow", e);
            }

        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    private void bindData(boolean isEdit) {
        try {
            if (isEdit) {
                tvTitle.setText(getString(R.string.edit_wallet));
                tvSave.setText(getString(R.string.update));

                walletEntity = walletViewModel.getWalletByWalletId(walletId);

                if (walletEntity != null) {
                    account = accountViewModel.getAccountDetailById(walletEntity.accountId);

                    etWalletName.setText(walletEntity.name.trim());
                    walletAmount = walletEntity.initialAmount;
                    walletIcon = walletEntity.categoryIcon;
                    ivWalletIcon.setImageResource(DataHelper.getWalletIcons().get(walletIcon));
                    tvAmount.setText(CommonUtils.getBeautifyAmount(walletEntity.currencySymbol, walletAmount));
                    maxLimitLabel.setText(getString(R.string.character_limit_wallet, Objects.requireNonNull(etWalletName.getText()).toString().length()));

                    //Color
                    int colorPosition = walletColorLists.indexOf(walletEntity.walletColor);
                    if (colorPosition >= 0) colorSpinner.setSelection(colorPosition);

                    //Currency
                    selectedCurrencyCode = walletEntity.currencyCode;

                    // Wallet Type
                    typeSpinner.setSelection(walletEntity.walletType);

                    // Exclude
                    switchExcludeView.setChecked(walletEntity.isExclude);

                    // Credit Card Dates
                    if (walletEntity.statementDate > 0) {
                        statementDateSpinner.setSelection((int) walletEntity.statementDate - 1);
                    }

                    if (walletEntity.dueDate > 0) {
                        paymentDateSpinner.setSelection((int) walletEntity.dueDate - 1);
                    }
                }
            } else {
                account = accountViewModel.getAccountDetailById((int) PreferenceManager.INSTANCE.getAccountId());
                tvTitle.setText(getString(R.string.add_wallet));
                walletIcon = 0;
                tvAmount.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, walletAmount));
                maxLimitLabel.setText(getString(R.string.character_limit_wallet, 0));
            }

            updateSaveButtonState();
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void setupListeners() {
        try {

            etWalletName.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {

                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    maxLimitLabel.setText(getString(R.string.character_limit_wallet, charSequence.length()));
                    updateSaveButtonState();
                }
            });

            typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if (position == 3) { // Credit Card
                        cardWalletStatement.setVisibility(View.VISIBLE);
                        cardWalletPayment.setVisibility(View.VISIBLE);
                        cardWalletExclude.setVisibility(View.GONE);

                        amountLabel.setText(getString(R.string.credit_limit));
                    } else {
                        cardWalletExclude.setVisibility(View.VISIBLE);
                        cardWalletStatement.setVisibility(View.GONE);
                        cardWalletPayment.setVisibility(View.GONE);

                        amountLabel.setText(getString(R.string.initial_amount));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            tvAmount.setOnClickListener(view -> {
                hideKeyboard(this);
                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", walletAmount);
                intent.putExtra("type", "amount");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.left_to_right, R.anim.scale_out);
                calculatorLauncher.launch(intent, options);
            });

            frameColor.setOnClickListener(view -> {
                hideKeyboard(this);
                colorSpinner.requestFocus();
                colorSpinner.performClick();
            });

            ivWalletIcon.setOnClickListener(view -> {
                hideKeyboard(this);
                Intent intent = new Intent(this, IconPickerActivity.class);
                intent.putExtra("selectedColor", walletColorLists.get(colorSpinner.getSelectedItemPosition()));
                intent.putExtra("iconType", "wallet");
                intent.putExtra("selectedIcon", walletIcon);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.left_to_right, R.anim.scale_out);
                walletIconLauncher.launch(intent, options);
            });

            accountViewModel.getAccountCurrencyByAccountId((int) PreferenceManager.INSTANCE.getAccountId()).observe(this,
                    accountCurrencyMappingEntities -> {

                        AccountCurrencyMappingEntity accountCurrencyMapping = new AccountCurrencyMappingEntity();
                        accountCurrencyMapping.currencyName = getString(R.string.add_currency);
                        accountCurrencyMappingEntities.add(accountCurrencyMapping);

                        CurrencySpinnerAdapter currencySpinnerAdapter = new CurrencySpinnerAdapter(this, R.layout.item_list_drop_down, R.id.label,
                                accountCurrencyMappingEntities);
                        currencySpinner.setAdapter(currencySpinnerAdapter);

                        if (selectedCurrencyCode != null) {
                            for (int i = 0; i < currencySpinnerAdapter.getCount(); i++) {
                                AccountCurrencyMappingEntity item = currencySpinnerAdapter.getItem(i);

                                if (item != null && Objects.equals(item.currencyCode, selectedCurrencyCode)) {
                                    currencySpinner.setSelection(i, false);
                                    updateAmountText();
                                    break;
                                }
                            }

                            selectedCurrencyCode = null;
                        }

                        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                AccountCurrencyMappingEntity currency =
                                        (AccountCurrencyMappingEntity) parent.getItemAtPosition(position);

                                if (currency != null && currency.currencySymbol != null) {
                                    updateAmountText();
                                }

                                // Last item = Add Currency
                                if (position == parent.getCount() - 1) {

                                    // Move back to the previous selection
                                    int previousPosition = Math.max(0, parent.getSelectedItemPosition() - 1);
                                    currencySpinner.setSelection(previousPosition, false);

                                    hideKeyboard(CreateWalletActivity.this);

                                    currencySpinner.post(() -> {
                                        Intent intent = new Intent(CreateWalletActivity.this, AddCurrencyActivity.class)
                                                .putExtra("isEdit", false);
                                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(CreateWalletActivity.this, R.anim.left_to_right, R.anim.scale_out);
                                        currencyLauncher.launch(intent, options);
                                    });
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
                    });

            tvSave.setOnClickListener(view -> saveWallet());

            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            });

            getOnBackPressedDispatcher().addCallback(this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            finish();
                            ActivityUtils.overrideCloseTransition(CreateWalletActivity.this, R.anim.scale_in, R.anim.right_to_left);
                        }
                    });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void makeReadOnly() {
        tvAmount.setFocusable(false);
        tvAmount.setLongClickable(false);
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
                                walletAmount = amount;
                                updateAmountText();
                                updateSaveButtonState();
                            }
                        }
                    }
                });

        walletIconLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            int selectedWalletIcon = data.getIntExtra("walletIcon", 0);
                            ivWalletIcon.setImageResource(DataHelper.getWalletIcons().get(selectedWalletIcon));
                            walletIcon = selectedWalletIcon;
                        }
                    }
                });

        currencyLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            AccountCurrencyMappingEntity accountCurrencyMapping = IntentUtils.getSerializableExtra(data, "currencyMapping",
                                    AccountCurrencyMappingEntity.class);

                            if (accountCurrencyMapping != null) {
                                selectedCurrencyCode = accountCurrencyMapping.currencyCode;
                            }
                        }
                    }
                });
    }

    private void updateSaveButtonState() {

        boolean enabled = !Objects.requireNonNull(etWalletName.getText()).toString().isEmpty();
        tvSave.setEnabled(enabled);
        tvSave.setAlpha(enabled ? 1.0f : 0.5f); // Optional: make disabled state visible
    }

    private void saveWallet() {
        try {

            AccountCurrencyMappingEntity currency = (AccountCurrencyMappingEntity) currencySpinner.getSelectedItem();

            if (isEdit) {

                double oldRate = walletEntity.exchangeRate;
                double newRate = currency.conversionRate;
                double oldBalance = walletEntity.amount;
                double oldInitialAmount = walletEntity.initialAmount;

                walletEntity.name = Objects.requireNonNull(etWalletName.getText()).toString().trim();
                walletEntity.walletColor = walletColorLists.get(colorSpinner.getSelectedItemPosition());
                walletEntity.walletType = typeSpinner.getSelectedItemPosition();
                walletEntity.currencyName = currency.currencyName;
                walletEntity.currencyCode = currency.currencyCode;
                walletEntity.currencySymbol = currency.currencySymbol;
                walletEntity.categoryIcon = walletIcon;

                walletEntity.amount = oldBalance - oldInitialAmount + walletAmount;

                walletEntity.initialAmount = walletAmount;
                walletEntity.exchangeRate = newRate;

                walletEntity.ordering = walletViewModel.getMaxWalletOrdering((int) PreferenceManager.INSTANCE.getAccountId()) + 1;

                walletEntity.isExclude = switchExcludeView.isChecked();

                if (typeSpinner.getSelectedItemPosition() == 3) {
                    walletEntity.statementDate = statementDateSpinner.getSelectedItemPosition() + 1;
                    walletEntity.dueDate = paymentDateSpinner.getSelectedItemPosition() + 1;
                } else {
                    walletEntity.statementDate = 0;
                    walletEntity.dueDate = 0;
                }

                walletEntity.isSynced = false;

                // Update wallet
                walletViewModel.updateWallet(walletEntity);

                // Update account balance
                double oldAccountValue = oldBalance * oldRate;

                // Add new wallet value to account (using new exchange rate)
                double newAccountValue = walletEntity.amount * newRate;
                account.balance += (newAccountValue - oldAccountValue);
                account.isSynced = false;
                accountViewModel.updateAccount(account);

                Toast.makeText(getApplicationContext(), getString(R.string.wallet_updated_successfully), Toast.LENGTH_SHORT).show();

            } else {

                WalletEntity wallet = new WalletEntity();

                wallet.accountId = (int) PreferenceManager.INSTANCE.getAccountId();
                wallet.name = Objects.requireNonNull(etWalletName.getText()).toString().trim();
                wallet.walletColor = walletColorLists.get(colorSpinner.getSelectedItemPosition());
                wallet.walletType = typeSpinner.getSelectedItemPosition();
                wallet.currencyName = currency.currencyName;
                wallet.currencyCode = currency.currencyCode;
                wallet.currencySymbol = currency.currencySymbol;

                wallet.categoryIcon = walletIcon;
                wallet.initialAmount = walletAmount;
                wallet.amount = walletAmount;
                wallet.exchangeRate = currency.conversionRate;

                wallet.ordering = walletViewModel.getMaxWalletOrdering((int) PreferenceManager.INSTANCE.getAccountId()) + 1;

                wallet.isExclude = switchExcludeView.isChecked();

                if (typeSpinner.getSelectedItemPosition() == 3) {
                    wallet.statementDate = statementDateSpinner.getSelectedItemPosition() + 1;
                    wallet.dueDate = paymentDateSpinner.getSelectedItemPosition() + 1;
                } else {
                    wallet.statementDate = 0;
                    wallet.dueDate = 0;
                }

                wallet.isHidden = false;
                wallet.isActive = true;
                wallet.isDeleted = false;
                wallet.isSynced = false;

                // Save wallet
                walletViewModel.saveWallet(wallet);

                // Update account balance
                account.balance += wallet.amount * wallet.exchangeRate;
                account.isSynced = false;
                accountViewModel.updateAccount(account);

                Toast.makeText(getApplicationContext(), getString(R.string.wallet_created_successfully), Toast.LENGTH_SHORT).show();
            }

            setResult(RESULT_OK);
            finish();
            ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);

        } catch (Exception e) {
            AppLogger.e(getClass(), "saveWallet", e);
        }
    }

    private void updateAmountText() {
        AccountCurrencyMappingEntity currency = (AccountCurrencyMappingEntity) currencySpinner.getSelectedItem();

        if (currency == null) {
            return;
        }

        tvAmount.setText(CommonUtils.getBeautifyAmount(currency.currencySymbol, walletAmount));
        String targetCurrency = isEdit ? walletEntity.currencyCode : account.currencyCode;

        if (Objects.equals(currency.currencyCode, targetCurrency)) {
            rateLabel.setVisibility(View.GONE);
        } else {
            rateLabel.setVisibility(View.VISIBLE);
            rateLabel.setText(getString(R.string.exchange_rate_format, "1.00", currency.currencyCode,
                    getFormattedRate(currency.conversionRate), targetCurrency));
        }
    }

    private String getFormattedRate(double conversionRate) {
        String rate = String.valueOf(conversionRate);

        if (rate.isEmpty() || ".".equals(rate)) {
            return "0.00";
        }

        if (!rate.contains(".")) {
            return rate + ".00";
        }

        int decimalIndex = rate.indexOf('.');
        int decimalDigits = rate.length() - decimalIndex - 1;

        if (decimalDigits == 0) {
            return rate + "00";
        } else if (decimalDigits == 1) {
            return rate + "0";
        }

        return rate;
    }
}