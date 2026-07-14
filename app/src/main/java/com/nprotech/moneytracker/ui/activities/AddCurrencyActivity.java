package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.AccountCurrencyMappingEntity;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.CurrencyEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.MasterViewModel;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddCurrencyActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView tvCurrencyName, tvSave, tvRate;
    private AppCompatEditText etRate;
    private ActivityResultLauncher<Intent> currencyLauncher;
    private CurrencyEntity mainCurrency, currency;
    private MasterViewModel masterViewModel;
    private AccountViewModel accountViewModel;
    private String currencyCode = "";
    private double exchangeRate = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_currency);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            NestedScrollView scrollView = findViewById(R.id.scrollView);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            tvSave = toolbarWrapper.findViewById(R.id.tvSave);
            tvCurrencyName = findViewById(R.id.tvCurrencyName);
            etRate = findViewById(R.id.etRate);
            tvRate = findViewById(R.id.tvRate);

            tvTitle.setText(getString(R.string.add_currency));
            tvSave.setVisibility(View.VISIBLE);

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

            masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);
            accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

            bindData();
            backPressed();
            setupListeners();
            setupLauncher();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {

            AccountEntity account = accountViewModel.getAccountDetailById((int) PreferenceManager.INSTANCE.getAccountId());
            if (account != null) {
                currencyCode = account.currencyCode;

                mainCurrency = masterViewModel.getCurrencyByCode(currencyCode);
            }

            currency = masterViewModel.getFirstCurrencyForWallet((int) PreferenceManager.INSTANCE.getAccountId());
            if (currency != null) {
                tvCurrencyName.setText(getString(R.string.currency_display, currency.code, currency.name));
                updateExchangeRate();
            }

            etRate.setText(R.string.exchange_rate_default_value);
            updateSaveButton();
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void backPressed() {
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        finish();
                        overridePendingTransition(R.anim.scale_in, R.anim.bottom_to_top);
                    }
                });
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                overridePendingTransition(R.anim.scale_in, R.anim.bottom_to_top);
            });

            tvCurrencyName.setOnClickListener(view -> {
                Intent intent = new Intent(this, CurrencyListActivity.class);
                intent.putExtra("currency", currency);
                intent.putExtra("type", "wallet");
                currencyLauncher.launch(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });

            etRate.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {

                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    String rate = charSequence.toString().trim();

                    if (rate.isEmpty() || ".".equals(rate)) {
                        exchangeRate = 0.0;
                    } else {
                        try {
                            exchangeRate = Double.parseDouble(rate);
                        } catch (NumberFormatException e) {
                            exchangeRate = 0.0;
                        }
                    }

                    updateExchangeRate();
                    updateSaveButton();
                }
            });

            tvSave.setOnClickListener(view -> {
                AccountCurrencyMappingEntity accountCurrencyMappingEntity = new AccountCurrencyMappingEntity();
                accountCurrencyMappingEntity.accountId = PreferenceManager.INSTANCE.getAccountId();
                accountCurrencyMappingEntity.currencyId = currency.id;
                accountCurrencyMappingEntity.currencyCode = currency.code;
                accountCurrencyMappingEntity.currencyName = currency.name;
                accountCurrencyMappingEntity.currencySymbol = currency.symbol;
                accountCurrencyMappingEntity.isActive = true;

                if(mainCurrency != null) {
                    accountCurrencyMappingEntity.mainCurrencyId = mainCurrency.id;
                    accountCurrencyMappingEntity.mainCurrencyCode = mainCurrency.code;
                    accountCurrencyMappingEntity.mainCurrencyName = mainCurrency.name;
                    accountCurrencyMappingEntity.mainCurrencySymbol = mainCurrency.symbol;
                }

                accountViewModel.saveAccountCurrencyMapping(accountCurrencyMappingEntity);

                runOnUiThread(() -> {
                    Intent intent = new Intent();
                    intent.putExtra("currencyMapping", accountCurrencyMappingEntity);
                    setResult(-1, intent);
                    finish();
                    overridePendingTransition(R.anim.scale_in, R.anim.right_to_left);
                });
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void setupLauncher() {
        currencyLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            currency = (CurrencyEntity) data.getSerializableExtra("currency");
                            if (currency != null) {
                                tvCurrencyName.setText(getString(R.string.currency_display, currency.code, currency.name));
                                updateExchangeRate();
                            }
                        }
                    }
                });
    }

    private String getFormattedRate() {
        String rate = Objects.requireNonNull(etRate.getText()).toString().trim();

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

    private void updateExchangeRate() {
        if (currency == null) return;
        tvRate.setText(getString(R.string.exchange_rate_format, "1.00", currency.code, getFormattedRate(), currencyCode));
    }

    private void updateSaveButton() {
        boolean enabled = exchangeRate > 0;

        tvSave.setEnabled(enabled);
        tvSave.setAlpha(enabled ? 1.0f : 0.5f);
    }
}