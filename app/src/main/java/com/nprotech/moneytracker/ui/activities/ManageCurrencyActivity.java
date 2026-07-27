package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.AccountCurrencyMappingEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.SimpleDividerItemDecoration;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.WalletViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManageCurrencyActivity extends BaseActivity {

    private AppCompatImageView icBack, ivAdd;
    private RelativeLayout rlAccountCurrency;
    private RecyclerView rvAccountCurrency;
    private AppCompatTextView tvBaseCurrencyName;
    private AccountViewModel accountViewModel;
    private WalletViewModel walletViewModel;
    private ConstraintLayout emptyWrapper;
    private RecyclerViewAdapter<AccountCurrencyMappingEntity> accountCurrencyRecyclerViewAdapter;
    private ActivityResultLauncher<Intent> currencyLauncher;
    private int accountId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_mapping);
        statusBarDarkSetting();
        hideKeyboard(this);

        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            ivAdd = toolbarWrapper.findViewById(R.id.ivAdd);

            tvTitle.setText(getString(R.string.currency));
            ivAdd.setVisibility(View.VISIBLE);

            tvBaseCurrencyName = findViewById(R.id.tvBaseCurrencyName);
            rlAccountCurrency = findViewById(R.id.rlAccountCurrency);
            rvAccountCurrency = findViewById(R.id.rvAccountCurrency);
            emptyWrapper = findViewById(R.id.emptyWrapper);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top,
                        v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rlAccountCurrency), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, 0, 0, systemBars.bottom);
                return insets;
            });

            accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
            walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);

            bindData();
            initializeAdapters();
            observeData();
            setUpListeners();
            setUpLauncher();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {
            accountId = (int) PreferenceManager.INSTANCE.getAccountId();

            if (accountId != 0) {
                accountViewModel.loadAccountCurrencies(accountId);

                AccountCurrencyMappingEntity baseCurrencyMapping = accountViewModel.fetchAccountBaseCurrencyByAccountId(accountId);

                if (baseCurrencyMapping != null) {
                    tvBaseCurrencyName.setText(getString(R.string.currency_name_format, baseCurrencyMapping.mainCurrencyCode, baseCurrencyMapping.mainCurrencyName));
                }
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAdapters() {
        try {
            rvAccountCurrency.setLayoutManager(new LinearLayoutManager(this));

            accountCurrencyRecyclerViewAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(), R.layout.item_account_currency) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, AccountCurrencyMappingEntity currency) {
                    holder.setViewText(R.id.tvCurrencyName, currency.currencyCode + getString(R.string.dash)
                            + currency.currencyName);
                    holder.setViewText(R.id.tvCurrencyRate, getString(R.string.exchange_rate_format, "1.00", currency.currencyCode,
                            getFormattedRate(currency.conversionRate), currency.mainCurrencyCode));

                    holder.getView(R.id.ivEdit).setOnClickListener(view -> {
                        Intent intent = new Intent(ManageCurrencyActivity.this, AddCurrencyActivity.class)
                                .putExtra("currencyMapId", currency.id)
                                .putExtra("isEdit", true);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(ManageCurrencyActivity.this, R.anim.left_to_right, R.anim.scale_out);
                        currencyLauncher.launch(intent, options);
                    });

                    holder.getView(R.id.ivDelete).setOnClickListener(view -> showDeleteDialog(currency));
                }
            };

            rvAccountCurrency.setAdapter(accountCurrencyRecyclerViewAdapter);
            rvAccountCurrency.setHasFixedSize(true);
            rvAccountCurrency.setItemAnimator(null);
            rvAccountCurrency.addItemDecoration(new SimpleDividerItemDecoration(this));

            rvAccountCurrency.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm == null)
                        return;
                    int last = lm.findLastVisibleItemPosition();
                    if (last >= accountCurrencyRecyclerViewAdapter.getItemCount() - 5) {
                        accountViewModel.loadNextPage();
                    }
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    private void observeData() {
        try {

            accountViewModel.getAccountCurrencyMapping().observe(this, list -> {

                if (list == null || list.isEmpty()) {
                    rlAccountCurrency.setVisibility(View.GONE);
                    emptyWrapper.setVisibility(View.VISIBLE);
                } else {
                    emptyWrapper.setVisibility(View.GONE);
                    rlAccountCurrency.setVisibility(View.VISIBLE);
                    accountCurrencyRecyclerViewAdapter.setItems(list);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "observeData", e);
        }
    }

    private void setUpListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(ManageCurrencyActivity.this, R.anim.scale_in, R.anim.bottom_to_top);
            });

            ivAdd.setOnClickListener(view -> {
                Intent intent = new Intent(ManageCurrencyActivity.this, AddCurrencyActivity.class)
                        .putExtra("isEdit", false);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(ManageCurrencyActivity.this, R.anim.left_to_right, R.anim.scale_out);
                currencyLauncher.launch(intent, options);
            });

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finish();
                    ActivityUtils.overrideCloseTransition(ManageCurrencyActivity.this, R.anim.scale_in, R.anim.bottom_to_top);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setUpListeners", e);
        }
    }

    private void setUpLauncher() {
        try {
            currencyLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            accountViewModel.loadAccountCurrencies(accountId);
                        }
                    });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setUpListeners", e);
        }
    }

    private void showDeleteDialog(AccountCurrencyMappingEntity currency) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        tvTitle.setText(R.string.delete_currency);
        tvMessage.setText(R.string.delete_message_currency);
        tvSubMessage.setText(R.string.message_currency);
        tvSubMessage.setVisibility(View.VISIBLE);
        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            deleteCurrency(currency);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteCurrency(AccountCurrencyMappingEntity currency) {
        try {
            if (accountViewModel.updateAccountCurrencyMapping(accountId, currency.currencyId, currency.currencyCode)) {
                List<WalletEntity> wallets = walletViewModel.getWalletsByAccountAndCurrency(accountId, currency.currencyCode);
                if (wallets != null && !wallets.isEmpty()) {
                    for (WalletEntity wallet : wallets) {
                        wallet.currencyCode = currency.mainCurrencyCode;
                        wallet.currencyName = currency.mainCurrencyName;
                        wallet.currencySymbol = currency.mainCurrencySymbol;
                        wallet.exchangeRate = 1;
                        walletViewModel.updateWallet(wallet);
                    }
                }

                // Recalculate account balance
                double balance = walletViewModel.getAccountBalance(accountId);
                accountViewModel.updateAccountBalance(accountId, balance);

                Toast.makeText(getApplicationContext(), R.string.currency_has_been_removed_successfully, Toast.LENGTH_SHORT).show();
                accountViewModel.loadAccountCurrencies(accountId);
            }

        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteCurrency", e);
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