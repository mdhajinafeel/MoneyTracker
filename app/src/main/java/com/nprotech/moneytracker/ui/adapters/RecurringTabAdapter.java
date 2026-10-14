package com.nprotech.moneytracker.ui.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.nprotech.moneytracker.ui.fragments.CompletedRecurringFragment;
import com.nprotech.moneytracker.ui.fragments.DeactivatedRecurringFragment;
import com.nprotech.moneytracker.ui.fragments.InProgressRecurringFragment;

public class RecurringTabAdapter extends FragmentStateAdapter {

    private final int accountId;

    public RecurringTabAdapter(@NonNull FragmentActivity activity, int accountId) {
        super(activity);
        this.accountId = accountId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle args = new Bundle();
        args.putInt("accountId", accountId);

        Fragment fragment = switch (position) {
            case 0 -> new InProgressRecurringFragment();
            case 1 -> new DeactivatedRecurringFragment();
            case 2 -> new CompletedRecurringFragment();
            default -> throw new IllegalArgumentException(
                    "Invalid position: " + position
            );
        };

        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public int getItemCount() {
        return 3;
    }

    @Override
    public boolean containsItem(long itemId) {
        return itemId >= 0 && itemId < 3;
    }
}