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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;

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
import com.nprotech.moneytracker.utils.CommonUtils;
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
    private AppCompatTextView tvSave, tvTitle, amountLabel, tvAmount;
    private AppCompatSpinner typeSpinner, colorSpinner, currencySpinner, statementDateSpinner, paymentDateSpinner;
    private FrameLayout frameColor;
    private ConstraintLayout excludeWrapper, statementDateWrapper, paymentDateWrapper;
    private ActivityResultLauncher<Intent> calculatorLauncher, walletIconLauncher, currencyLauncher;
    private boolean isEdit = false;
    private double walletAmount = 0;
    private AccountEntity account;
    private AccountViewModel accountViewModel;
    private WalletViewModel walletViewModel;
    private ArrayList<String> walletColorLists;
    private int walletIcon = 0;
    private WalletEntity walletEntity;
    private SwitchCompat switchExcludeView;

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
            NestedScrollView scrollView = findViewById(R.id.scrollView);
            tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            tvSave = toolbarWrapper.findViewById(R.id.tvSave);
            icBack = toolbarWrapper.findViewById(R.id.icBack);

            tvSave.setVisibility(View.VISIBLE);

            etWalletName = findViewById(R.id.etWalletName);
            typeSpinner = findViewById(R.id.typeSpinner);
            frameColor = findViewById(R.id.frameColor);
            colorSpinner = findViewById(R.id.colorSpinner);
            currencySpinner = findViewById(R.id.currencySpinner);
            statementDateSpinner = findViewById(R.id.statementDateSpinner);
            paymentDateSpinner = findViewById(R.id.paymentDateSpinner);
            ivWalletIcon = findViewById(R.id.ivWalletIcon);
            amountLabel = findViewById(R.id.amountLabel);
            tvAmount = findViewById(R.id.tvAmount);
            excludeWrapper = findViewById(R.id.excludeWrapper);
            statementDateWrapper = findViewById(R.id.statementDateWrapper);
            paymentDateWrapper = findViewById(R.id.paymentDateWrapper);
            switchExcludeView = findViewById(R.id.switchExcludeView);

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

            accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
            walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                isEdit = bundle.getBoolean("isEdit");

                makeReadOnly();
                initializeAdapters();
                bindData(isEdit);
                setupLauncher();
                setupListeners();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void initializeAdapters() {
        try {
            List<String> walletTypes = Arrays.asList(getString(R.string.general), getString(R.string.cash), getString(R.string.bank), getString(R.string.credit_card), getString(R.string.debit_card));

            FontSpinnerAdapter fontSpinnerAdapter = new FontSpinnerAdapter(this, walletTypes);
            typeSpinner.setAdapter(fontSpinnerAdapter);

            walletColorLists = new ArrayList<>();
            walletColorLists = DataHelper.getColorList();
            ColorSpinnerAdapter colorSpinnerAdapter = new ColorSpinnerAdapter(this, R.layout.list_drop_down_color, R.id.label, walletColorLists);
            colorSpinner.setAdapter(colorSpinnerAdapter);

            List<String> dateList = DateHelper.getMonthDates();
            FontSpinnerAdapter statementAdapter = new FontSpinnerAdapter(this, dateList);
            FontSpinnerAdapter paymentAdapter = new FontSpinnerAdapter(this, dateList);

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

                walletEntity = (WalletEntity) getIntent().getSerializableExtra("wallet");

                if (walletEntity != null) {
                    etWalletName.setText(walletEntity.name);
                    walletAmount = walletEntity.amount;
                    walletIcon = walletEntity.categoryIcon;
                    ivWalletIcon.setImageResource(DataHelper.getWalletIcons().get(walletIcon));
                    tvAmount.setText(CommonUtils.getBeautifyAmount(walletEntity.currencySymbol, walletAmount));

                    //Color
                    int colorPosition = walletColorLists.indexOf(walletEntity.walletColor);
                    if (colorPosition >= 0) colorSpinner.setSelection(colorPosition);

                    //Currency
                    CurrencySpinnerAdapter adapter = (CurrencySpinnerAdapter) currencySpinner.getAdapter();

                    if (adapter != null) {
                        for (int i = 0; i < adapter.getCount(); i++) {
                            AccountCurrencyMappingEntity item = adapter.getItem(i);
                            if (item != null && walletEntity.currencyCode.equals(item.currencyCode)) {
                                currencySpinner.setSelection(i);
                                break;
                            }
                        }
                    }

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
                    updateSaveButtonState();
                }
            });

            typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if (position == 3) { // Credit Card
                        statementDateWrapper.setVisibility(View.VISIBLE);
                        paymentDateWrapper.setVisibility(View.VISIBLE);
                        excludeWrapper.setVisibility(View.GONE);

                        amountLabel.setText(getString(R.string.credit_limit));
                    } else {
                        excludeWrapper.setVisibility(View.VISIBLE);
                        statementDateWrapper.setVisibility(View.GONE);
                        paymentDateWrapper.setVisibility(View.GONE);

                        amountLabel.setText(getString(R.string.amount));
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
                calculatorLauncher.launch(intent);
                overridePendingTransition(R.anim.left_to_right, R.anim.scale_out);
            });

            frameColor.setOnClickListener(view -> {
                hideKeyboard(this);
                colorSpinner.requestFocus();
                colorSpinner.performClick();
            });

            ivWalletIcon.setOnClickListener(view -> {
                hideKeyboard(this);
                Intent intent = new Intent(this, WalletPickerActivity.class);
                intent.putExtra("selectedColor", walletColorLists.get(colorSpinner.getSelectedItemPosition()));
                intent.putExtra("selectedWalletIcon", walletIcon);
                walletIconLauncher.launch(intent);
                overridePendingTransition(R.anim.left_to_right, R.anim.scale_out);
            });

            accountViewModel.getAccountCurrencyByAccountId((int) PreferenceManager.INSTANCE.getAccountId()).observe(this,
                    accountCurrencyMappingEntities -> {

                        AccountCurrencyMappingEntity accountCurrencyMapping = new AccountCurrencyMappingEntity();
                        accountCurrencyMapping.currencyName = getString(R.string.add_currency);
                        accountCurrencyMappingEntities.add(accountCurrencyMapping);

                        CurrencySpinnerAdapter currencySpinnerAdapter = new CurrencySpinnerAdapter(this, R.layout.item_list_drop_down, R.id.label,
                                accountCurrencyMappingEntities);
                        currencySpinner.setAdapter(currencySpinnerAdapter);

                        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            private boolean firstSelection = true;

                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                                // Ignore the initial automatic selection
                                if (firstSelection) {
                                    firstSelection = false;
                                    return;
                                }

                                if (position == parent.getCount() - 1) {
                                    hideKeyboard(CreateWalletActivity.this);
                                    Intent intent = new Intent(CreateWalletActivity.this, AddCurrencyActivity.class);
                                    currencyLauncher.launch(intent);
                                    overridePendingTransition(R.anim.left_to_right, R.anim.scale_out);
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
                overridePendingTransition(R.anim.scale_in, R.anim.bottom_to_top);
            });

            getOnBackPressedDispatcher().addCallback(this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            finish();
                            overridePendingTransition(R.anim.scale_in, R.anim.bottom_to_top);
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
                                tvAmount.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, amount));
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
                            AccountCurrencyMappingEntity accountCurrencyMapping = (AccountCurrencyMappingEntity) data.getSerializableExtra("currencyMapping");

                            if (accountCurrencyMapping != null) {
                                CurrencySpinnerAdapter adapter = (CurrencySpinnerAdapter) currencySpinner.getAdapter();
                                adapter.insert(accountCurrencyMapping, adapter.getCount() - 1);
                                adapter.notifyDataSetChanged();
                                currencySpinner.setSelection(adapter.getPosition(accountCurrencyMapping));
                            }
                        }
                    }
                });
    }

    private void updateSaveButtonState() {

        boolean enabled = walletAmount > 0;

        if (Objects.requireNonNull(etWalletName.getText()).toString().isEmpty()) {
            enabled = false;
        }

        tvSave.setEnabled(enabled);
        tvSave.setAlpha(enabled ? 1.0f : 0.5f); // Optional: make disabled state visible
    }

    private void saveWallet() {
        try {

            AccountCurrencyMappingEntity currency = (AccountCurrencyMappingEntity) currencySpinner.getSelectedItem();

            if (isEdit) {

                walletEntity.name = Objects.requireNonNull(etWalletName.getText()).toString().trim();
                walletEntity.walletColor = walletColorLists.get(colorSpinner.getSelectedItemPosition());
                walletEntity.walletType = typeSpinner.getSelectedItemPosition();
                walletEntity.currencyName = currency.currencyName;
                walletEntity.currencyCode = currency.currencyCode;
                walletEntity.currencySymbol = currency.currencySymbol;

                walletEntity.categoryIcon = walletIcon;
                walletEntity.initialAmount = walletAmount;

                // Wallet Type
                walletEntity.ordering = walletViewModel.getMaxWalletOrdering((int) PreferenceManager.INSTANCE.getAccountId()) + 1;

                // Exclude
                walletEntity.isExclude = switchExcludeView.isChecked();

                // Credit Card Details
                if (typeSpinner.getSelectedItemPosition() == 3) {
                    walletEntity.statementDate = statementDateSpinner.getSelectedItemPosition() + 1;
                    walletEntity.dueDate = paymentDateSpinner.getSelectedItemPosition() + 1;
                } else {
                    walletEntity.statementDate = 0;
                    walletEntity.dueDate = 0;
                }

                walletEntity.isSynced = false;

                walletViewModel.updateWallet(walletEntity);

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

                // Wallet Type
                wallet.ordering = walletViewModel.getMaxWalletOrdering((int) PreferenceManager.INSTANCE.getAccountId()) + 1;

                // Exclude
                wallet.isExclude = switchExcludeView.isChecked();

                // Credit Card Details
                if (typeSpinner.getSelectedItemPosition() == 3) {
                    wallet.statementDate = statementDateSpinner.getSelectedItemPosition() + 1;
                    wallet.dueDate = paymentDateSpinner.getSelectedItemPosition() + 1;

                    amountLabel.setText(getString(R.string.credit_limit));
                } else {
                    wallet.statementDate = 0;
                    wallet.dueDate = 0;

                    amountLabel.setText(getString(R.string.amount));
                }

                wallet.isHidden = false;
                wallet.isActive = true;
                wallet.isDeleted = false;
                wallet.isSynced = false;

                walletViewModel.saveWallet(wallet);

                Toast.makeText(getApplicationContext(), getString(R.string.wallet_created_successfully), Toast.LENGTH_SHORT).show();
            }

            setResult(RESULT_OK);
            finish();
            overridePendingTransition(R.anim.scale_in, R.anim.bottom_to_top);

        } catch (Exception e) {
            AppLogger.e(getClass(), "saveWallet", e);
        }
    }
}