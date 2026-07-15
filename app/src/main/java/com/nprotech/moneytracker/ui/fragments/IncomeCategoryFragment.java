package com.nprotech.moneytracker.ui.fragments;

import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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
public class IncomeCategoryFragment extends Fragment {

    private ConstraintLayout emptyWrapper;
    private RecyclerView rvIncomeCategory;
    private CategoryViewModel categoryViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income_category, container, false);
        try {
            rvIncomeCategory = view.findViewById(R.id.rvIncomeCategory);
            emptyWrapper = view.findViewById(R.id.emptyWrapper);

            categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

            categoryViewModel.incomeCategory(1);

            categoryViewModel.getIncomeCategories().observe(requireActivity(), categoryEntities -> {
                if (!categoryEntities.isEmpty()) {
                    bindIncomeCategories(categoryEntities);
                    rvIncomeCategory.setVisibility(View.VISIBLE);
                    emptyWrapper.setVisibility(View.GONE);
                } else {
                    rvIncomeCategory.setVisibility(View.GONE);
                    emptyWrapper.setVisibility(View.VISIBLE);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void bindIncomeCategories(List<CategoryEntity> incomeCategories) {
        try {
            RecyclerViewAdapter<CategoryEntity> incomeCategoryAdapter = new RecyclerViewAdapter<>(requireActivity(), incomeCategories, R.layout.item_manage_category) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, CategoryEntity categoryEntity) {
                    holder.setViewImageResource(R.id.ivCategory, DataHelper.getCategoryIcons().get(categoryEntity.icon));
                    holder.setViewText(R.id.tvCategory, categoryEntity.getName(requireContext()));

                    if (Build.VERSION.SDK_INT >= 29) {
                        holder.getView(R.id.colorView).getBackground().setColorFilter(new BlendModeColorFilter(Color.parseColor(categoryEntity.color), BlendMode.SRC_OVER));
                    } else {
                        holder.getView(R.id.colorView).getBackground().setColorFilter(Color.parseColor(categoryEntity.color), PorterDuff.Mode.SRC_OVER);
                    }
                }
            };

            rvIncomeCategory.setAdapter(incomeCategoryAdapter);
            rvIncomeCategory.setHasFixedSize(true);
        }catch (Exception e){
            AppLogger.e(getClass(), "bindIncomeCategories", e);
        }
    }
}