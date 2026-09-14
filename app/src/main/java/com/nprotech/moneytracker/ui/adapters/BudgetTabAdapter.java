package com.nprotech.moneytracker.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.nprotech.moneytracker.ui.fragments.ArchivedBudgetFragment;
import com.nprotech.moneytracker.ui.fragments.CompletedBudgetFragment;
import com.nprotech.moneytracker.ui.fragments.InProgressBudgetFragment;
import com.nprotech.moneytracker.ui.fragments.PausedBudgetFragment;

public class BudgetTabAdapter extends FragmentStateAdapter {

    public BudgetTabAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> new InProgressBudgetFragment();
            case 1 -> new PausedBudgetFragment();
            case 2 -> new CompletedBudgetFragment();
            case 3 -> new ArchivedBudgetFragment();
            default -> throw new IllegalArgumentException("Invalid position: " + position);
        };
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    @Override
    public boolean containsItem(long itemId) {
        return itemId >= 0 && itemId < 4;
    }
}