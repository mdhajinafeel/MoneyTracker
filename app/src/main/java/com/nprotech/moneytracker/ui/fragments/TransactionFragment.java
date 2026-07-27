package com.nprotech.moneytracker.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.ui.activities.CreateTransactionActivity;
import com.nprotech.moneytracker.ui.adapters.DailyTransactionAdapter;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.TransactionViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionFragment extends Fragment {

    private FloatingActionButton fabAdd;
    private ConstraintLayout emptyWrapper;
    private RecyclerView rvTransactions;
    private TransactionViewModel transactionViewModel;
    private AccountViewModel accountViewModel;
    private DailyTransactionAdapter dailyTransactionAdapter;
    private String accountCurrencySymbol = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        try {
            fabAdd = view.findViewById(R.id.fabAdd);
            emptyWrapper = view.findViewById(R.id.emptyWrapper);
            rvTransactions = view.findViewById(R.id.rvTransactions);

            transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
            accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);

            setupListeners();
            initializeAdapters();
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }

        return view;
    }

    private void setupListeners() {
        try {
            // FAB CLICK
            fabAdd.setOnClickListener(v -> {
                v.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(120)
                        .withEndAction(() ->
                                v.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(120)
                                        .start())
                        .start();

                startActivity(new Intent(requireActivity(), CreateTransactionActivity.class)
                        .putExtra("action", "add")
                        .putExtra("type", TransactionEntity.TYPE_EXPENSE));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            accountViewModel.getSelectedAccount().observe(getViewLifecycleOwner(), account -> {

                if (account != null) {
                    transactionViewModel.loadTransactions(account.id, 0, 0);
                    accountCurrencySymbol = account.currencySymbol;
                    dailyTransactionAdapter.setAccountCurrencySymbol(accountCurrencySymbol);
                }
            });

            transactionViewModel.getDailyTransactions().observe(getViewLifecycleOwner(), dailyTransModels -> {

                if (dailyTransModels == null || dailyTransModels.isEmpty()) {
                    rvTransactions.setVisibility(View.GONE);
                    emptyWrapper.setVisibility(View.VISIBLE);
                } else {
                    emptyWrapper.setVisibility(View.GONE);
                    rvTransactions.setVisibility(View.VISIBLE);
                    dailyTransactionAdapter.setItems(dailyTransModels);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void initializeAdapters() {
        try {
            rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
            dailyTransactionAdapter = new DailyTransactionAdapter(requireContext(), new ArrayList<>(), accountCurrencySymbol);
            rvTransactions.setAdapter(dailyTransactionAdapter);
            rvTransactions.setHasFixedSize(true);
            rvTransactions.setItemAnimator(null);

            rvTransactions.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm == null)
                        return;
                    int last = lm.findLastVisibleItemPosition();
                    if (last >= dailyTransactionAdapter.getItemCount() - 5) {
                        transactionViewModel.loadNextPage();
                    }
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        transactionViewModel.loadTransactions((int) PreferenceManager.INSTANCE.getAccountId(), 0, 0);
    }
}