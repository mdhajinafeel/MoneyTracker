package com.nprotech.moneytracker.ui.fragments;

import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.CategoryEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.viewmodel.CategoryViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ExpenseCategoryFragment extends Fragment {

    private ConstraintLayout emptyWrapper;
    private RecyclerView rvExpenseCategory;
    private CategoryViewModel categoryViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_category, container, false);
        try {
            rvExpenseCategory = view.findViewById(R.id.rvExpenseCategory);
            emptyWrapper = view.findViewById(R.id.emptyWrapper);

            categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

            categoryViewModel.expenseCategory(2);

            categoryViewModel.getExpenseCategories().observe(requireActivity(), categoryEntities -> {
                if (!categoryEntities.isEmpty()) {
                    bindExpenseCategories(categoryEntities);
                    rvExpenseCategory.setVisibility(View.VISIBLE);
                    emptyWrapper.setVisibility(View.GONE);
                } else {
                    rvExpenseCategory.setVisibility(View.GONE);
                    emptyWrapper.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void bindExpenseCategories(List<CategoryEntity> expenseCategories) {
        try {
            RecyclerViewAdapter<CategoryEntity> expenseCategoryAdapter = new RecyclerViewAdapter<>(requireActivity(), expenseCategories, R.layout.item_manage_category) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, CategoryEntity categoryEntity) {
                    holder.setViewImageResource(R.id.ivCategory, DataHelper.getCategoryIcons().get(categoryEntity.icon));
                    holder.setViewText(R.id.tvCategory, categoryEntity.getName(requireContext()));

                    if (Build.VERSION.SDK_INT >= 29) {
                        holder.getView(R.id.colorView).getBackground().setColorFilter(new BlendModeColorFilter(Color.parseColor(categoryEntity.color), BlendMode.SRC_OVER));
                    } else {
                        Drawable drawable = holder.getView(R.id.colorView).getBackground().mutate();
                        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_OVER);
                        DrawableCompat.setTint(drawable, Color.parseColor(categoryEntity.color));
                        holder.getView(R.id.colorView).setBackground(drawable);
                    }
                }
            };

            rvExpenseCategory.setAdapter(expenseCategoryAdapter);
            rvExpenseCategory.setHasFixedSize(true);
        }catch (Exception e){
            AppLogger.e(getClass(), "bindExpenseCategories", e);
        }
    }
}