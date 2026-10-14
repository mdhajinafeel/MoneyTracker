package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.AccountCurrencyMappingEntity;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.CurrencyEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.ui.adapters.ColorSpinnerAdapter;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.utils.IntentUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.MasterViewModel;
import com.nprotech.moneytracker.viewmodel.WalletViewModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateAccountActivity extends BaseActivity {

    private AppCompatImageView icBack, ivAccountIcon;
    private AppCompatTextView tvSave, tvCurrency, tvAmount, maxLimitLabel;
    private AppCompatEditText etAccountName;
    private AppCompatSpinner colorSpinner;
    private FrameLayout frameColor;
    private boolean isEdit = false;
    private int accountIcon = 0, accountId = 0;
    private MasterViewModel masterViewModel;
    private AccountViewModel accountViewModel;
    private WalletViewModel walletViewModel;
    private CurrencyEntity currency;
    private ActivityResultLauncher<Intent> calculatorLauncher, walletIconLauncher, currencyLauncher;
    private double initialAmount = 0;
    private ArrayList<String> accountColorLists;
    private double originalInitialAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
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
            tvCurrency = findViewById(R.id.tvCurrency);
            tvAmount = findViewById(R.id.tvAmount);
            colorSpinner = findViewById(R.id.colorSpinner);
            etAccountName = findViewById(R.id.etAccountName);
            frameColor = findViewById(R.id.frameColor);
            ivAccountIcon = findViewById(R.id.ivAccountIcon);
            maxLimitLabel = findViewById(R.id.maxLimitLabel);

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

                masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);
                accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
                walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);

                isEdit = bundle.getBoolean("isEdit", false);
                accountId = bundle.getInt("accountId", 0);

                if (isEdit) {
                    tvTitle.setText(getString(R.string.edit_account));
                } else {
                    tvTitle.setText(getString(R.string.add_account));
                }

                initializeAdapters();
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
            if (isEdit) {
                tvSave.setText(getString(R.string.update));

                AccountEntity account = accountViewModel.getAccountDetailById(accountId);
                if (account == null) {
                    Toast.makeText(this, getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                etAccountName.setText(account.name != null ? account.name : "");
                maxLimitLabel.setText(getString(R.string.character_limit_wallet, account.name != null ? account.name.length() : 0));
                initialAmount = account.balance;
                originalInitialAmount = initialAmount;
                accountIcon = account.categoryIcon;

                tvCurrency.setText(account.currencyName);
                tvAmount.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, initialAmount));

                int colorPosition = accountColorLists.indexOf(account.accountColor);
                if (colorPosition >= 0) {
                    colorSpinner.setSelection(colorPosition);
                }

                if (accountIcon >= 0 && accountIcon < DataHelper.getWalletIcons().size()) {
                    ivAccountIcon.setImageResource(DataHelper.getWalletIcons().get(accountIcon));
                }

                currency = masterViewModel.getCurrencyByCode(account.currencyCode);
            } else {
                tvSave.setText(getString(R.string.save));
                currency = masterViewModel.getDefaultCurrency();
                accountIcon = 3;
                initialAmount = 0;
                maxLimitLabel.setText(getString(R.string.character_limit_wallet, 0));
                if (currency != null) {
                    tvCurrency.setText(currency.name);
                    tvAmount.setText(CommonUtils.getBeautifyAmount(currency.symbol, initialAmount));
                }
                ivAccountIcon.setImageResource(DataHelper.getWalletIcons().get(accountIcon));
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAdapters() {
        try {
            accountColorLists = new ArrayList<>();
            accountColorLists = DataHelper.getColorList();
            ColorSpinnerAdapter colorSpinnerAdapter = new ColorSpinnerAdapter(this, R.layout.list_drop_down_color, R.id.label, accountColorLists);
            colorSpinner.setAdapter(colorSpinnerAdapter);

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

    private void setupLauncher() {
        currencyLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    currency = IntentUtils.getSerializableExtra(data, "currency", CurrencyEntity.class);
                    if (currency != null) {
                        tvCurrency.setText(currency.name);
                        tvAmount.setText(CommonUtils.getBeautifyAmount(currency.symbol, initialAmount));
                    }
                }
            }
        });

        calculatorLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    double amount = data.getDoubleExtra("amount", 0);
                    String type = data.getStringExtra("type");

                    if (type != null && type.equalsIgnoreCase("amount")) {
                        initialAmount = amount;
                        tvAmount.setText(CommonUtils.getBeautifyAmount(currency.symbol, initialAmount));
                        updateSaveButtonState();
                    }
                }
            }
        });

        walletIconLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    int selectedAccountIcon = data.getIntExtra("accountIcon", 0);
                    ivAccountIcon.setImageResource(DataHelper.getWalletIcons().get(selectedAccountIcon));
                    accountIcon = selectedAccountIcon;
                }
            }
        });
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
                    ActivityUtils.overrideCloseTransition(CreateAccountActivity.this, R.anim.scale_in, R.anim.right_to_left);
                }
            });

            etAccountName.addTextChangedListener(new TextWatcher() {
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

            tvCurrency.setOnClickListener(v -> {

                if (isEdit) {
                    if (!walletViewModel.canChangeAccountCurrency(accountId)) {
                        Toast.makeText(this, R.string.currency_edit_account, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                Intent intent = new Intent(this, CurrencyPickerActivity.class);
                intent.putExtra("currency", currency);
                intent.putExtra("type", "account");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.slide_in_right, R.anim.slide_out_left);
                currencyLauncher.launch(intent, options);
            });

            // AMOUNT
            tvAmount.setOnClickListener(view -> {

                hideKeyboard(this);

                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", initialAmount);
                intent.putExtra("type", "amount");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                calculatorLauncher.launch(intent, options);
            });

            tvSave.setOnClickListener(view -> saveAccount());

            frameColor.setOnClickListener(view -> {
                etAccountName.clearFocus();
                hideKeyboard(this);
                colorSpinner.requestFocus();
                colorSpinner.performClick();
            });

            ivAccountIcon.setOnClickListener(view -> {
                etAccountName.clearFocus();
                hideKeyboard(this);
                Intent intent = new Intent(this, IconPickerActivity.class);
                intent.putExtra("selectedColor", accountColorLists.get(colorSpinner.getSelectedItemPosition()));
                intent.putExtra("iconType", "account");
                intent.putExtra("selectedIcon", accountIcon);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.left_to_right, R.anim.scale_out);
                walletIconLauncher.launch(intent, options);
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void saveAccount() {

        long currentTime = System.currentTimeMillis();
        String name = Objects.requireNonNull(etAccountName.getText()).toString().trim();

        if (name.isEmpty() || currency == null) {
            return;
        }

        AccountEntity account;
        if (isEdit) {
            account = accountViewModel.getAccountDetailById(accountId);
            if (account == null) {
                Toast.makeText(this, getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            account = new AccountEntity();
        }

        boolean initialAmountChanged = isEdit && Double.compare(initialAmount, originalInitialAmount) != 0;

        if (initialAmountChanged) {
            boolean updated = walletViewModel.updateDefaultWalletOpeningAmount(accountId, initialAmount);

            if (!updated) {
                Toast.makeText(this, R.string.cannot_change_initial_amount, Toast.LENGTH_LONG).show();
                return;
            }
        }

        account.name = name;
        account.accountColor = accountColorLists.get(colorSpinner.getSelectedItemPosition());
        account.categoryIcon = accountIcon;
        account.currencyCode = currency.code;
        account.currencyName = currency.name;
        account.currencySymbol = currency.symbol;
        account.balance = initialAmount;
        account.initialAmount = initialAmount;

        if (isEdit) {
            accountViewModel.updateAccount(account);
        } else {
            account.isDeleted = false;
            account.isSynced = false;
            account.ordering = accountViewModel.getLastAccountOrder() + 1;
            account.isDefault = false;
            account.isArchived = false;
            account.archivedAt = 0;

            account.serverId = 0;
            account.tempAccountServerId = "A_" + currentTime;
            long accountId = accountViewModel.saveAccount(account);

            if (accountId > 0) {

                AccountCurrencyMappingEntity accountCurrencyMappingEntity = new AccountCurrencyMappingEntity();
                accountCurrencyMappingEntity.accountId = (int) accountId;
                accountCurrencyMappingEntity.tempAccountServerId = "A_" + currentTime;
                accountCurrencyMappingEntity.currencyId = currency.id;
                accountCurrencyMappingEntity.currencyCode = currency.code;
                accountCurrencyMappingEntity.currencyName = currency.name;
                accountCurrencyMappingEntity.currencySymbol = currency.symbol;
                accountCurrencyMappingEntity.mainCurrencyId = currency.id;
                accountCurrencyMappingEntity.mainCurrencyCode = currency.code;
                accountCurrencyMappingEntity.mainCurrencySymbol = currency.symbol;
                accountCurrencyMappingEntity.mainCurrencyName = currency.name;
                accountCurrencyMappingEntity.conversionRate = 1;
                accountCurrencyMappingEntity.isActive = true;
                accountCurrencyMappingEntity.isBase = true;
                accountCurrencyMappingEntity.isSynced = false;
                accountCurrencyMappingEntity.mappingServerId = 0;
                accountViewModel.saveAccountCurrencyMapping(accountCurrencyMappingEntity);

                WalletEntity wallet = new WalletEntity();
                wallet.accountId = (int) accountId;
                wallet.name = getString(R.string.cash_wallet);
                wallet.walletType = 0;
                wallet.walletColor = accountColorLists.get(colorSpinner.getSelectedItemPosition());
                wallet.currencyName = currency.name;
                wallet.currencyCode = currency.code;
                wallet.currencySymbol = currency.symbol;
                wallet.categoryIcon = 0;
                wallet.creditLimit = 0;
                wallet.initialAmount = initialAmount;
                wallet.amount = initialAmount;
                wallet.exchangeRate = 1;
                wallet.dueDate = 0;
                wallet.statementDate = 0;
                wallet.ordering = walletViewModel.getMaxWalletOrdering((int) accountId) + 1;
                wallet.isHidden = false;
                wallet.isExclude = false;
                wallet.isActive = true;
                wallet.isSynced = false;
                wallet.isDeleted = false;
                wallet.isDefault = true;
                wallet.isArchived = false;
                wallet.archivedAt = 0;
                wallet.walletServerId = 0;
                wallet.tempWalletServerId = "W_" + currentTime;
                walletViewModel.saveWallet(wallet);
            }
        }


        Toast.makeText(this, isEdit ? getString(R.string.account_updated) : getString(R.string.account_created), Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
        ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
    }

    private void updateSaveButtonState() {
        String name = Objects.requireNonNull(etAccountName.getText()).toString().trim();
        boolean enabled = !name.isEmpty();
        tvSave.setEnabled(enabled);
        tvSave.setAlpha(enabled ? 1.0f : 0.5f);
    }
}