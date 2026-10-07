package com.nprotech.moneytracker.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.nprotech.moneytracker.ui.fragments.CompletedRecurringFragment;
import com.nprotech.moneytracker.ui.fragments.DeactivatedRecurringFragment;
import com.nprotech.moneytracker.ui.fragments.InProgressRecurringFragment;

public class RecurringTabAdapter extends FragmentStateAdapter {

    public RecurringTabAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> new InProgressRecurringFragment();
            case 1 -> new DeactivatedRecurringFragment();
            case 2 -> new CompletedRecurringFragment();
            default -> throw new IllegalArgumentException("Invalid position: " + position);
        };
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