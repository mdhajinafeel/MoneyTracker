package com.nprotech.moneytracker.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.nprotech.moneytracker.ui.fragments.AchievedGoalFragment;
import com.nprotech.moneytracker.ui.fragments.MyGoalFragment;

public class GoalTabAdapter extends FragmentStateAdapter {

    public GoalTabAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? new MyGoalFragment() : new AchievedGoalFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    @Override
    public boolean containsItem(long itemId) {
        return itemId == 0 || itemId == 1;
    }
}