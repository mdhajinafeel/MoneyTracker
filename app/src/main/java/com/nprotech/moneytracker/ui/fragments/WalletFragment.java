package com.nprotech.moneytracker.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.ui.activities.CreateWalletActivity;
import com.nprotech.moneytracker.ui.activities.WalletTransactionDetailedActivity;
import com.nprotech.moneytracker.ui.adapters.WalletsAdapter;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.WalletViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WalletFragment extends Fragment {

    private RecyclerView rvWallets;
    private WalletsAdapter walletsAdapter;
    private WalletViewModel walletViewModel;
    private AccountViewModel accountViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        try {

            rvWallets = view.findViewById(R.id.rvWallets);

            walletViewModel = new ViewModelProvider(requireActivity()).get(WalletViewModel.class);
            accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);

            initializeAdapters();
            setupListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void setupListeners() {
        try {
            accountViewModel.getSelectedAccount().observe(getViewLifecycleOwner(), account -> {
                if (account != null) {
                    walletViewModel.selectAccount(account.id);
                }
            });

            walletViewModel.getWallets().observe(getViewLifecycleOwner(), wallets -> walletsAdapter.setItems(wallets));

            walletsAdapter.setOnAddWalletClickListener(() -> {
                startActivity(new Intent(requireContext(), CreateWalletActivity.class)
                        .putExtra("isEdit", false));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            walletsAdapter.setOnWalletClickListener(wallet -> {
                Intent intent = new Intent(requireContext(), WalletTransactionDetailedActivity.class);
                intent.putExtra("walletId", wallet.id);
                startActivity(intent);
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.left_to_right, R.anim.scale_out);
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void initializeAdapters() {
        try {
            walletsAdapter = new WalletsAdapter(requireContext(), new ArrayList<>());
            rvWallets.setAdapter(walletsAdapter);
            rvWallets.setHasFixedSize(true);
            rvWallets.setItemAnimator(null);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}