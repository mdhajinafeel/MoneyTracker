package com.nprotech.moneytracker.ui.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.MoreOptionsModel;
import com.nprotech.moneytracker.ui.activities.CreateWalletActivity;
import com.nprotech.moneytracker.ui.activities.GoalActivity;
import com.nprotech.moneytracker.ui.activities.WalletTransactionDetailedActivity;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.adapters.WalletsAdapter;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.GoalViewModel;
import com.nprotech.moneytracker.viewmodel.WalletViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MoreFragment extends Fragment {

    private RecyclerView rvWallets, rvMoreOptions;
    private WalletsAdapter walletsAdapter;
    private WalletViewModel walletViewModel;
    private AccountViewModel accountViewModel;
    private GoalViewModel goalViewModel;
    private List<MoreOptionsModel> moreOptionsModels;
    private RecyclerViewAdapter<MoreOptionsModel> moreOptionsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        try {

            rvWallets = view.findViewById(R.id.rvWallets);
            rvMoreOptions = view.findViewById(R.id.rvMoreOptions);

            walletViewModel = new ViewModelProvider(requireActivity()).get(WalletViewModel.class);
            accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
            goalViewModel = new ViewModelProvider(requireActivity()).get(GoalViewModel.class);

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
                    goalViewModel.selectAccount(account.id);
                }
            });

            walletViewModel.getWallets().observe(getViewLifecycleOwner(), wallets -> walletsAdapter.setItems(wallets));

            walletsAdapter.setOnAddWalletClickListener(() -> {
                startActivity(new Intent(requireContext(), CreateWalletActivity.class)
                        .putExtra("isEdit", false));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            walletsAdapter.setOnWalletClickListener(wallet -> {
                startActivity(new Intent(requireContext(), WalletTransactionDetailedActivity.class)
                        .putExtra("walletId", wallet.id));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            goalViewModel.goalCount().observe(getViewLifecycleOwner(), count -> {
                if (count == null) {
                    count = 0;
                }

                MoreOptionsModel goalModel = moreOptionsModels.get(1);
                goalModel.count = count;

                moreOptionsAdapter.notifyItemChanged(1);
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

            moreOptionsModels = new ArrayList<>();
            moreOptionsModels.add(new MoreOptionsModel(TransactionEntity.TYPE_BUDGET, getString(R.string.budget), getString(R.string.plan_your_spending), R.drawable.ic_more_budget, R.color.budget_dark, R.color.budget_light, 0));
            moreOptionsModels.add(new MoreOptionsModel(TransactionEntity.TYPE_GOAL, getString(R.string.goals), getString(R.string.track_your_progress), R.drawable.ic_more_goal, R.color.goal_dark, R.color.goal_light, 0));
            moreOptionsModels.add(new MoreOptionsModel(TransactionEntity.TYPE_DEBT, getString(R.string.debt), getString(R.string.manage_what_you_have_owe), R.drawable.ic_more_debt, R.color.debt_dark, R.color.debt_light, 0));
            moreOptionsModels.add(new MoreOptionsModel(TransactionEntity.TYPE_RECURRING, getString(R.string.recurring), getString(R.string.track_recurring_transactions), R.drawable.ic_more_recurring, R.color.reminder_dark, R.color.recurring_light, 0));

            moreOptionsAdapter = new RecyclerViewAdapter<>(requireActivity(), moreOptionsModels, R.layout.item_more_options) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, MoreOptionsModel moreOptionsModel) {

                    AppCompatImageView imageView = holder.getView(R.id.imageView);
                    ConstraintLayout colorBadgeView = holder.getView(R.id.colorBadgeView);

                    holder.getView(R.id.colorView).setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), moreOptionsModel.bgColor)));
                    imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), moreOptionsModel.icon));
                    imageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), moreOptionsModel.fgColor)));

                    holder.setViewText(R.id.tvSettingName, moreOptionsModel.title);
                    holder.setViewText(R.id.tvSettingDesc, moreOptionsModel.desc);

                    if (moreOptionsModel.count > 0) {
                        colorBadgeView.setVisibility(View.VISIBLE);
                        colorBadgeView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), moreOptionsModel.bgColor)));
                        holder.setViewTextColor(R.id.tvBadgeCount, ContextCompat.getColor(requireContext(), moreOptionsModel.fgColor));
                        holder.setViewText(R.id.tvBadgeCount, String.valueOf(moreOptionsModel.count));
                    } else {
                        colorBadgeView.setVisibility(View.GONE);
                    }

                    if(holder.getBindingAdapterPosition() == getItemCount() - 1) {
                        holder.getView(R.id.divider).setAlpha(0f);
                    } else {
                        holder.getView(R.id.divider).setAlpha(1f);
                    }

                    holder.getView(R.id.itemView).setOnClickListener(view -> {
                        if(moreOptionsModel.id == TransactionEntity.TYPE_GOAL) {
                            startActivity(new Intent(requireActivity(), GoalActivity.class)
                                    .putExtra("accountId", (int) PreferenceManager.INSTANCE.getAccountId()));
                            ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
                        }
                    });
                }
            };
            rvMoreOptions.setAdapter(moreOptionsAdapter);
            rvMoreOptions.setHasFixedSize(true);
            rvMoreOptions.setItemAnimator(null);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}