package com.nprotech.moneytracker.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.nprotech.moneytracker.ui.fragments.AchievedGoalFragment;
import com.nprotech.moneytracker.ui.fragments.ArchivedGoalFragment;
import com.nprotech.moneytracker.ui.fragments.InProgressGoalFragment;

public class GoalTabAdapter extends FragmentStateAdapter {

    public GoalTabAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> new InProgressGoalFragment();
            case 1 -> new AchievedGoalFragment();
            case 2 -> new ArchivedGoalFragment();
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