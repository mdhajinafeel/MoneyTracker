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
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.ui.activities.CreateTransactionActivity;
import com.nprotech.moneytracker.ui.adapters.DailyTransactionAdapter;
import com.nprotech.moneytracker.viewmodel.TransactionViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionFragment extends Fragment {

    private FloatingActionButton fabAdd;
    private ConstraintLayout emptyWrapper;
    private RecyclerView rvTransactions;
    private TransactionViewModel transactionViewModel;
    private DailyTransactionAdapter dailyTransactionAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        try {
            fabAdd = view.findViewById(R.id.fabAdd);
            emptyWrapper = view.findViewById(R.id.emptyWrapper);
            rvTransactions = view.findViewById(R.id.rvTransactions);

            transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

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

                startActivity(new Intent(requireContext(), CreateTransactionActivity.class).putExtra("type", 2));
                requireActivity().overridePendingTransition(R.anim.top_to_bottom, R.anim.scale_out);
            });

            transactionViewModel.getDailyTransactionData((int) PreferenceManager.INSTANCE.getAccountId()).observe(getViewLifecycleOwner(),
                    dailyTransModels -> {
                        if (dailyTransModels != null && !dailyTransModels.isEmpty()) {
                            emptyWrapper.setVisibility(View.GONE);
                            rvTransactions.setVisibility(View.VISIBLE);

                            dailyTransactionAdapter.setItems(dailyTransModels);
                        } else {

                            rvTransactions.setVisibility(View.GONE);
                            emptyWrapper.setVisibility(View.VISIBLE);
                        }
                    });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void initializeAdapters() {
        try {
            rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
            dailyTransactionAdapter = new DailyTransactionAdapter(requireContext(), new ArrayList<>());
            rvTransactions.setAdapter(dailyTransactionAdapter);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}