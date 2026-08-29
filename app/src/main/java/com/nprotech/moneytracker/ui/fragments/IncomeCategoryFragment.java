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

import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.CategoryEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.MaxHeightRecyclerView;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.CategoryViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class IncomeCategoryFragment extends Fragment {

    private ConstraintLayout emptyWrapper;
    private MaxHeightRecyclerView rvIncomeCategory;
    private MaterialCardView cardCategory;
    private CategoryViewModel categoryViewModel;
    private View categoryRoot;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income_category, container, false);
        try {
            categoryRoot = view.findViewById(R.id.categoryRoot);
            cardCategory = view.findViewById(R.id.cardCategory);
            rvIncomeCategory = view.findViewById(R.id.rvIncomeCategory);
            emptyWrapper = view.findViewById(R.id.emptyWrapper);

            categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

            categoryViewModel.incomeCategory(1);

            categoryViewModel.getIncomeCategories().observe(requireActivity(), categoryEntities -> {
                if (!categoryEntities.isEmpty()) {
                    bindIncomeCategories(categoryEntities);
                    cardCategory.setVisibility(View.VISIBLE);
                    emptyWrapper.setVisibility(View.GONE);
                } else {
                    cardCategory.setVisibility(View.GONE);
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
                    View divider = holder.getView(R.id.divider);

                    if (Build.VERSION.SDK_INT >= 29) {
                        holder.getView(R.id.colorView).getBackground().setColorFilter(new BlendModeColorFilter(Color.parseColor(categoryEntity.color), BlendMode.SRC_OVER));
                    } else {
                        Drawable drawable = holder.getView(R.id.colorView).getBackground().mutate();
                        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_OVER);
                        DrawableCompat.setTint(drawable, Color.parseColor(categoryEntity.color));
                        holder.getView(R.id.colorView).setBackground(drawable);
                    }

                    if (categoryEntity.defaultCategory > 0) {
                        holder.setViewVisibility(R.id.ivDelete, View.GONE);
                    } else {
                        holder.setViewVisibility(R.id.ivDelete, View.VISIBLE);
                    }

                    int position = holder.getBindingAdapterPosition();
                    if (position == getItemCount() - 1) {
                        divider.setAlpha(0f);
                    } else {
                        divider.setAlpha(1f);
                    }
                }
            };

            rvIncomeCategory.setAdapter(incomeCategoryAdapter);
            rvIncomeCategory.setHasFixedSize(true);

            updateRecyclerViewMaxHeight();

            incomeCategoryAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    updateRecyclerViewMaxHeight();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    updateRecyclerViewMaxHeight();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    updateRecyclerViewMaxHeight();
                }
            });
        }catch (Exception e){
            AppLogger.e(getClass(), "bindIncomeCategories", e);
        }
    }

    private void updateRecyclerViewMaxHeight() {
        categoryRoot.post(() -> {
            int availableHeight = categoryRoot.getHeight();
            int cardMargins = CommonUtils.dpToPx(requireActivity(), 20);
            int maxHeight = availableHeight - cardMargins;
            if (maxHeight > 0) {
                rvIncomeCategory.setMaxHeight(maxHeight);
            }
        });
    }
}