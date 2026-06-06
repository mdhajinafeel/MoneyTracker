package com.nprotech.moneytracker.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.nprotech.moneytracker.ui.fragments.ExpenseCategoryFragment;
import com.nprotech.moneytracker.ui.fragments.IncomeCategoryFragment;

public class CategoryPageAdapter extends FragmentStateAdapter {

    public CategoryPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new IncomeCategoryFragment();
        } else {
            return new ExpenseCategoryFragment();
        }
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