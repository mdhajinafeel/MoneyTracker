package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.CurrencyEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.MasterViewModel;
import com.nprotech.moneytracker.viewmodel.WalletViewModel;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddAccountActivity extends BaseActivity {

    private MaterialToolbar toolbar;
    private LinearLayout layoutStep1, layoutStep2, layoutStep3;
    private AppCompatTextView tvStep2, tvStep3, tvSkip;
    private View viewLine1, viewLine2;
    private TextInputLayout tilInitialAmount;
    private TextInputEditText etWalletName, etCurrency, etInitialAmount;
    private MaterialButton btnNextStep1, btnNextStep2, btnCreateAccount;
    private ActivityResultLauncher<Intent> currencyLauncher;
    private int currentStep = 1;
    private MasterViewModel masterViewModel;
    private AccountViewModel accountViewModel;
    private WalletViewModel walletViewModel;
    private CurrencyEntity currency;
    private static final int INTENT_FLAGS = Intent.FLAG_ACTIVITY_NEW_TASK
            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            | Intent.FLAG_ACTIVITY_CLEAR_TOP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            toolbar = findViewById(R.id.toolbar);
            toolbar.setNavigationIcon(null);

            layoutStep1 = findViewById(R.id.layoutStep1);
            layoutStep2 = findViewById(R.id.layoutStep2);
            layoutStep3 = findViewById(R.id.layoutStep3);
            tvStep2 = findViewById(R.id.tvStep2);
            tvStep3 = findViewById(R.id.tvStep3);
            viewLine1 = findViewById(R.id.viewLine1);
            viewLine2 = findViewById(R.id.viewLine2);
            tilInitialAmount = findViewById(R.id.tilInitialAmount);
            etWalletName = findViewById(R.id.etWalletName);
            etCurrency = findViewById(R.id.etCurrency);
            etInitialAmount = findViewById(R.id.etInitialAmount);
            btnNextStep1 = findViewById(R.id.btnNextStep1);
            btnNextStep2 = findViewById(R.id.btnNextStep2);
            btnCreateAccount = findViewById(R.id.btnCreateAccount);
            tvSkip = findViewById(R.id.tvSkip);

            masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);
            accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
            walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);

            setupClickListeners();
            setupCurrencyLauncher();

            fetchDefaultCurrency();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void setupCurrencyLauncher() {
        currencyLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            currency = (CurrencyEntity) data.getSerializableExtra("currency");
                            if (currency != null) {
                                etCurrency.setText(currency.name);
                                tilInitialAmount.setPrefixText(currency.symbol);
                                tilInitialAmount.setPrefixTextAppearance(R.style.CurrencyPrefixStyle);
                                tilInitialAmount.setPrefixTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.vibrant_orange)));
                            }
                        }
                    }
                });
    }

    private void setupClickListeners() {

        toolbar.setNavigationOnClickListener(v -> handleBackNavigation());

        btnNextStep1.setOnClickListener(view -> showStep2());

        btnNextStep2.setOnClickListener(view -> showStep3());

        btnCreateAccount.setOnClickListener(view -> {

            if (TextUtils.isEmpty(etInitialAmount.getText())) {
                Toast.makeText(getApplicationContext(), getString(R.string.enter_the_amount), Toast.LENGTH_SHORT).show();
                return;
            }

            saveAccounts(false);
        });

        tvSkip.setOnClickListener(view -> saveAccounts(true));

        etCurrency.setOnClickListener(v -> {
            Intent intent = new Intent(this, CurrencyListActivity.class);
            intent.putExtra("currency", currency);
            currencyLauncher.launch(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackNavigation();
            }
        });
    }

    private void showStep2() {

        if (TextUtils.isEmpty(etWalletName.getText())) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_the_name), Toast.LENGTH_SHORT).show();
            return;
        }

        hideKeyboard(this);

        currentStep = 2;
        animateLayout(layoutStep1, layoutStep2);

        toolbar.setNavigationIcon(R.drawable.ic_back);

        tvStep2.setBackgroundResource(R.drawable.bg_step_active);
        tvStep2.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        viewLine1.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.vibrant_orange));
    }

    private void showStep3() {

        if (TextUtils.isEmpty(etCurrency.getText())) {
            Toast.makeText(getApplicationContext(), getString(R.string.select_the_currency), Toast.LENGTH_SHORT).show();
            return;
        }

        etInitialAmount.requestFocus();

        currentStep = 3;
        animateLayout(layoutStep2, layoutStep3);

        toolbar.setNavigationIcon(R.drawable.ic_back);

        tvStep3.setBackgroundResource(R.drawable.bg_step_active);
        tvStep3.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        viewLine2.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.vibrant_orange));
    }

    private void backToStep1() {

        hideKeyboard(this);

        currentStep = 1;
        animateLayout(layoutStep2, layoutStep1);

        toolbar.setNavigationIcon(null);

        tvStep2.setBackgroundResource(R.drawable.bg_step_inactive);
        tvStep2.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.medium_grey));
        viewLine1.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.grey_88));
        etWalletName.requestFocus();
    }

    private void backToStep2() {

        hideKeyboard(this);

        currentStep = 2;
        animateLayout(layoutStep3, layoutStep2);

        tvStep3.setBackgroundResource(R.drawable.bg_step_inactive);
        tvStep3.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.medium_grey));
        viewLine2.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.grey_88));
    }

    private void animateLayout(View hideView, View showView) {
        hideView.animate().alpha(0f).translationX(-100f).setDuration(250).withEndAction(() -> {
            hideView.setVisibility(View.GONE);
            showView.setVisibility(View.VISIBLE);
            showView.setAlpha(0f);
            showView.setTranslationX(100f);
            showView.animate().alpha(1f).translationX(0f).setDuration(250).start();
        }).start();
    }

    private void fetchDefaultCurrency() {
        currency = masterViewModel.getDefaultCurrency();
        if (currency != null) {
            tilInitialAmount.setPrefixText(currency.symbol);
            tilInitialAmount.setPrefixTextAppearance(R.style.CurrencyPrefixStyle);
            tilInitialAmount.setPrefixTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.vibrant_orange)));
            etCurrency.setText(currency.name);
        }
    }

    private void saveAccounts(boolean isSkip) {

        double initialAmount;
        if (isSkip) {
            initialAmount = 0;
        } else {
            initialAmount = Double.parseDouble(Objects.requireNonNull(etInitialAmount.getText()).toString());
        }

        AccountEntity account = new AccountEntity();
        account.name = Objects.requireNonNull(etWalletName.getText()).toString().trim();
        account.currencyCode = currency.code;
        account.currencyName = currency.name;
        account.currencySymbol = currency.symbol;
        account.balance = initialAmount;
        account.isDeleted = false;
        account.isSynced = false;
        account.ordering = accountViewModel.getLastAccountOrder() + 1;
        long accountId = accountViewModel.saveAccount(account);

        if (accountId > 0) {

            PreferenceManager.INSTANCE.setAccountId(accountId);

            WalletEntity wallet = new WalletEntity();
            wallet.accountId = (int) accountId;
            wallet.name = getString(R.string.cash);
            wallet.walletColor = "#0097E6";
            wallet.currencyName = currency.name;
            wallet.currencyCode = currency.code;
            wallet.currencySymbol = currency.symbol;
            wallet.categoryIcon = 0;
            wallet.initialAmount = initialAmount;
            wallet.dueDate = 0;
            wallet.statementDate = 0;
            wallet.ordering = walletViewModel.getLastWalletOrder() + 1;
            wallet.isHidden = false;
            wallet.isExclude = false;
            wallet.isActive = false;
            wallet.isSynced = false;
            wallet.isDeleted = false;
            long walletId = walletViewModel.saveWallet(wallet);

            PreferenceManager.INSTANCE.setWalletId(walletId);
        }

        startActivity(new Intent(AddAccountActivity.this, MainActivity.class).addFlags(INTENT_FLAGS));
        overridePendingTransition(R.anim.fade_fast_in, R.anim.fade_fast_out);
    }

    private void handleBackNavigation() {
        if (currentStep == 3) {
            backToStep2();
        } else if (currentStep == 2) {
            backToStep1();
        } else {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }
}