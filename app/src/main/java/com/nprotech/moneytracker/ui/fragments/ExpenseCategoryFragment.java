package com.nprotech.moneytracker.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.CategoryEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.ui.activities.CreateCategoryActivity;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.MaxHeightRecyclerView;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.CategoryViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ExpenseCategoryFragment extends Fragment {

    private ConstraintLayout emptyWrapper;
    private MaxHeightRecyclerView rvExpenseCategory;
    private MaterialCardView cardCategory;
    private View categoryRoot;
    private int selectedCategoryId = -1, selectedMoveCategoryId = -1, selectedMoveDefaultCategoryId = -1, selectedCategoryPosition = -1;
    private String selectedCategoryName, selectedMoveCategoryName;
    private CategoryViewModel categoryViewModel;
    private RecyclerViewAdapter<CategoryEntity> expenseCategoryAdapter;
    private Typeface medium, semiBold;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_category, container, false);
        try {
            categoryRoot = view.findViewById(R.id.categoryRoot);
            cardCategory = view.findViewById(R.id.cardCategory);
            rvExpenseCategory = view.findViewById(R.id.rvExpenseCategory);
            emptyWrapper = view.findViewById(R.id.emptyWrapper);

            categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

            medium = ResourcesCompat.getFont(requireActivity(), R.font.exo2_medium);
            semiBold = ResourcesCompat.getFont(requireActivity(), R.font.exo2_semibold);

            categoryViewModel.expenseCategory(false);

            categoryViewModel.getExpenseCategories().observe(requireActivity(), categoryEntities -> {

                if (expenseCategoryAdapter == null) {
                    bindExpenseCategories(categoryEntities);
                } else {
                    expenseCategoryAdapter.replaceItems(categoryEntities);
                }

                if (!categoryEntities.isEmpty()) {
                    cardCategory.setVisibility(View.VISIBLE);
                    emptyWrapper.setVisibility(View.GONE);
                } else {
                    cardCategory.setVisibility(View.GONE);
                    emptyWrapper.setVisibility(View.VISIBLE);
                }
            });

            categoryViewModel.getTransactionCount().observe(getViewLifecycleOwner(), count -> {
                        if (count == null) {
                            return;
                        }
                        showDeleteDialog(count, false);
                    }
            );
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void bindExpenseCategories(List<CategoryEntity> expenseCategories) {
        try {
            expenseCategoryAdapter = new RecyclerViewAdapter<>(requireActivity(), expenseCategories, R.layout.item_manage_category) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, CategoryEntity categoryEntity) {

                    AppCompatTextView tvCategoryStatus = holder.getView(R.id.tvCategoryStatus);

                    holder.setViewImageResource(R.id.ivCategory, DataHelper.getCategoryIcons().get(categoryEntity.icon));
                    holder.setViewText(R.id.tvCategory, categoryEntity.getName(requireContext()));
                    View divider = holder.getView(R.id.divider);

                    tvCategoryStatus.setVisibility(View.VISIBLE);
                    if(categoryEntity.active) {
                        tvCategoryStatus.setText(getString(R.string.active));
                        tvCategoryStatus.setTextColor(ContextCompat.getColor(requireActivity(), R.color.dark_income));
                    } else {
                        tvCategoryStatus.setText(getString(R.string.inactive));
                        tvCategoryStatus.setTextColor(ContextCompat.getColor(requireActivity(), R.color.expense));
                    }

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

                    holder.getView(R.id.ivEdit).setOnClickListener(v -> {
                        startActivity(new Intent(requireActivity(), CreateCategoryActivity.class)
                                .putExtra("categoryId", categoryEntity.id)
                                .putExtra("isEdit", true));
                        ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
                    });

                    holder.getView(R.id.ivDelete).setOnClickListener(v -> {
                        if (categoryEntity.defaultCategory > 0) {
                            return;
                        }
                        selectedCategoryPosition = holder.getBindingAdapterPosition();
                        selectedCategoryId = categoryEntity.id;
                        selectedCategoryName = categoryEntity.getName(requireContext());
                        categoryViewModel.checkCategoryForDelete(selectedCategoryId);
                    });
                }
            };

            rvExpenseCategory.setAdapter(expenseCategoryAdapter);
            rvExpenseCategory.setHasFixedSize(true);

            updateRecyclerViewMaxHeight();

            expenseCategoryAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
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
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindExpenseCategories", e);
        }
    }

    private void updateRecyclerViewMaxHeight() {
        categoryRoot.post(() -> {
            int availableHeight = categoryRoot.getHeight();
            int cardMargins = CommonUtils.dpToPx(requireActivity(), 20);
            int maxHeight = availableHeight - cardMargins;
            if (maxHeight > 0) {
                rvExpenseCategory.setMaxHeight(maxHeight);
            }
        });
    }

    private void showDeleteDialog(int count, boolean isMoved) {

        AlertDialog dialog = new AlertDialog.Builder(requireActivity()).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_confirm_delete, null, false);
        MaterialCardView cardHeader = view.findViewById(R.id.cardHeader);
        AppCompatImageView headerImage = view.findViewById(R.id.headerImage);
        AppCompatTextView tvDeleteTitle = view.findViewById(R.id.tvDeleteTitle);
        AppCompatTextView tvDeleteMessage = view.findViewById(R.id.tvDeleteMessage);
        MaterialCardView cardDeleteInfo = view.findViewById(R.id.cardDeleteInfo);
        AppCompatTextView tvClose = view.findViewById(R.id.tvClose);
        MaterialButton btnDelete = view.findViewById(R.id.btnDelete);

        if (count == 0) {
            tvDeleteTitle.setText(getString(R.string.delete_category));
            tvDeleteMessage.setText(getString(R.string.delete_category_message, selectedCategoryName));
            cardHeader.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.dim_expense));
            headerImage.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_delete));
            headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.bright_red)));
            cardDeleteInfo.setVisibility(View.VISIBLE);
            btnDelete.setText(getString(R.string.delete));
        } else {
            if (isMoved) {
                tvDeleteTitle.setText(getString(R.string.move_delete));
                tvDeleteMessage.setText(getResources().getQuantityString(R.plurals.move_category_desc, count, count, selectedCategoryName,
                        selectedMoveCategoryName, selectedCategoryName));
                cardHeader.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.light_lavender));
                headerImage.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_transfer_trans));
                headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.primary_dark)));
            } else {
                tvDeleteTitle.setText(getString(R.string.category_in_use));
                tvDeleteMessage.setText(getResources().getQuantityString(R.plurals.category_in_use_message, count, selectedCategoryName, count));
                cardHeader.setCardBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.category_light));
                headerImage.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_warning));
                headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.category_dark)));
            }
            cardDeleteInfo.setVisibility(View.GONE);
            btnDelete.setText(getString(R.string.move_delete));
        }

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        tvClose.setOnClickListener(v -> dialog.dismiss());

        if (count == 0) {
            btnDelete.setOnClickListener(v -> {
                dialog.dismiss();
                if (categoryViewModel.deleteCategory(selectedCategoryId)) {
                    expenseCategoryAdapter.removeItem(selectedCategoryPosition);
                    Toast.makeText(requireActivity(), getString(R.string.category_deleted), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireActivity(), getString(R.string.error_delete_category), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            if (isMoved) {
                btnDelete.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (categoryViewModel.moveTransactionsAndDeleteCategory(selectedCategoryId, selectedMoveCategoryId, selectedMoveDefaultCategoryId)) {
                        Toast.makeText(requireActivity(), getString(R.string.category_moved), Toast.LENGTH_SHORT).show();
                        selectedCategoryId = -1;
                        selectedMoveCategoryId = -1;
                        selectedMoveDefaultCategoryId = -1;
                        selectedCategoryName = "";
                        selectedMoveCategoryName = "";
                    } else {
                        Toast.makeText(requireActivity(), getString(R.string.error_moved_category), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                btnDelete.setOnClickListener(v -> {
                    dialog.dismiss();
                    showCategoryOptionDialog(count);
                });
            }
        }

        dialog.show();
    }

    private void showCategoryOptionDialog(int count) {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(requireActivity());
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_category_action, requireActivity().findViewById(android.R.id.content), false);

            AppCompatTextView tvSelectCategoryDesc = bottomView.findViewById(R.id.tvSelectCategoryDesc);
            RecyclerView rvCategory = bottomView.findViewById(R.id.rvCategory);
            AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);
            MaterialButton btnContinue = bottomView.findViewById(R.id.btnContinue);

            tvSelectCategoryDesc.setText(getResources().getQuantityString(R.plurals.selected_category_desc, count, count, selectedCategoryName));
            btnContinue.setEnabled(false);
            btnContinue.setAlpha(0.5f);

            categoryViewModel.getCategoriesForMove(TransactionEntity.TYPE_EXPENSE, selectedCategoryId);
            categoryViewModel.getCategoryOptions().observe(getViewLifecycleOwner(), categories -> {
                if (categories == null) {
                    return;
                }

                bindCategoryOptions(rvCategory, btnContinue, categories);
            });

            tvClose.setOnClickListener(v -> dialog.dismiss());

            btnContinue.setOnClickListener(v -> {
                dialog.dismiss();
                showDeleteDialog(count, true);
            });


            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showOptionDialog", e);
        }
    }

    private void bindCategoryOptions(RecyclerView recyclerView, MaterialButton btnContinue, List<CategoryEntity> categories) {

        RecyclerViewAdapter<CategoryEntity> adapter = new RecyclerViewAdapter<>(requireActivity(), categories, R.layout.item_category_picker) {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onPostBindViewHolder(ViewHolder holder, CategoryEntity categoryEntity) {

                AppCompatTextView tvCategory = holder.getView(R.id.tvCategory);

                holder.setViewImageResource(R.id.ivCategory, DataHelper.getCategoryIcons().get(categoryEntity.icon));
                tvCategory.setText(categoryEntity.getName(requireContext()));

                if (Build.VERSION.SDK_INT >= 29) {
                    holder.getView(R.id.colorView).getBackground().setColorFilter(new BlendModeColorFilter(Color.parseColor(categoryEntity.color), BlendMode.SRC_OVER));
                } else {
                    Drawable drawable = holder.getView(R.id.colorView).getBackground().mutate();
                    DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_OVER);
                    DrawableCompat.setTint(drawable, Color.parseColor(categoryEntity.color));
                    holder.getView(R.id.colorView).setBackground(drawable);
                }

                boolean selected = categoryEntity.id == selectedMoveCategoryId;

                if (selected) {
                    tvCategory.setTypeface(semiBold);
                } else {
                    tvCategory.setTypeface(medium);
                }

                holder.getView(R.id.ivSelected).setVisibility(selected ? View.VISIBLE : View.GONE);

                holder.itemView.setOnClickListener(view -> {
                    int position = holder.getAbsoluteAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) {
                        return;
                    }

                    selectedMoveCategoryId = categoryEntity.id;
                    selectedMoveDefaultCategoryId = categoryEntity.defaultCategory;
                    selectedMoveCategoryName = categoryEntity.getName(requireContext());

                    btnContinue.setEnabled(true);
                    btnContinue.setAlpha(1f);

                    notifyDataSetChanged();
                });
            }
        };

        btnContinue.setEnabled(false);
        btnContinue.setAlpha(0.5f);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }
}