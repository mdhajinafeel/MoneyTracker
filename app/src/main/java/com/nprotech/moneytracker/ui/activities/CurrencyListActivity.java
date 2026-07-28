package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.CurrencyEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.IntentUtils;
import com.nprotech.moneytracker.viewmodel.MasterViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CurrencyListActivity extends BaseActivity {

    private RecyclerView rvCurrency;
    private MasterViewModel masterViewModel;
    private CurrencyEntity currency;
    private TextInputEditText etSearch;
    private List<CurrencyEntity> currencyLists;
    private RecyclerViewAdapter<CurrencyEntity> currenciesRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_list);
        statusBarDarkSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setNavigationOnClickListener(v -> finish());

            etSearch = findViewById(R.id.etSearch);
            rvCurrency = findViewById(R.id.rvCurrency);
            masterViewModel = new ViewModelProvider(this).get(MasterViewModel.class);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                currency = IntentUtils.getSerializableExtra(getIntent(), "currency", CurrencyEntity.class);

                fetchCurrencies(bundle.getString("type"));
                setupSearch();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void fetchCurrencies(String type) {
        try {
            if (type.equalsIgnoreCase("account")) {
                currencyLists = masterViewModel.getAllCurrencies();
            } else if (type.equalsIgnoreCase("wallet")) {
                currencyLists = masterViewModel.getCurrenciesForWallet((int) PreferenceManager.INSTANCE.getAccountId());
            }

            if (!currencyLists.isEmpty()) {
                currenciesRecyclerViewAdapter = new RecyclerViewAdapter<>(getApplicationContext(), currencyLists,
                        R.layout.item_currency) {
                    @Override
                    public void onPostBindViewHolder(ViewHolder holder, CurrencyEntity currencyEntity) {
                        holder.setViewText(R.id.tvCurrencyCode, currencyEntity.code);
                        holder.setViewText(R.id.tvCurrencyName, currencyEntity.name + " (" + currencyEntity.symbol + ")");

                        AppCompatImageView ivSelected = holder.getView(R.id.ivSelected);
                        if (currency != null && currency.id == currencyEntity.id) {
                            ivSelected.setVisibility(View.VISIBLE);
                        } else {
                            ivSelected.setVisibility(View.GONE);
                        }

                        holder.itemView.setOnClickListener(view -> {
                            Intent intent = new Intent();
                            intent.putExtra("currency", currencyEntity);
                            setResult(-1, intent);
                            finish();
                            ActivityUtils.overrideCloseTransition(CurrencyListActivity.this, R.anim.slide_in_left, R.anim.slide_out_right);
                        });
                    }
                };

                rvCurrency.setAdapter(currenciesRecyclerViewAdapter);
                rvCurrency.setHasFixedSize(true);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "fetchCurrencies", e);
        }
    }

    private void setupSearch() {

        etSearch.addTextChangedListener(new android.text.TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String keyword = s.toString().trim().toLowerCase();

                if (keyword.isEmpty()) {
                    currenciesRecyclerViewAdapter.replaceItems(new ArrayList<>(currencyLists));
                    return;
                }

                List<CurrencyEntity> filteredList = new ArrayList<>();
                for (CurrencyEntity item : currencyLists) {
                    if (item.code.toLowerCase().contains(keyword)
                            || item.name.toLowerCase().contains(keyword)
                            || item.symbol.toLowerCase().contains(keyword)) {
                        filteredList.add(item);
                    }
                }

                currenciesRecyclerViewAdapter.setItems(filteredList);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        ActivityUtils.overrideCloseTransition(this, R.anim.slide_in_left, R.anim.slide_out_right);
    }
}