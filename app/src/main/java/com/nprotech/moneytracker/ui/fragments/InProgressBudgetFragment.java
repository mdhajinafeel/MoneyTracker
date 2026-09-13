package com.nprotech.moneytracker.ui.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.BudgetEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.BudgetWithDetails;
import com.nprotech.moneytracker.ui.activities.BudgetDetailActivity;
import com.nprotech.moneytracker.ui.activities.CreateBudgetActivity;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.BudgetViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class InProgressBudgetFragment extends Fragment {

    private RecyclerView rvBudgets;
    private ConstraintLayout emptyWrapper;
    private BudgetViewModel budgetViewModel;
    private RecyclerViewAdapter<BudgetWithDetails> budgetAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        try {
            View root = view.findViewById(R.id.rootView);
            rvBudgets = view.findViewById(R.id.rvBudgets);
            emptyWrapper = view.findViewById(R.id.emptyWrapper);

            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            budgetViewModel = new ViewModelProvider(this).get(BudgetViewModel.class);

            bindData();
            initializeAdapter();
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void bindData() {
        try {

            budgetViewModel.getBudgets(PreferenceManager.INSTANCE.getAccountId(), false, false,
                    false).observe(getViewLifecycleOwner(), budgetWithDetails -> {
                if (budgetWithDetails.isEmpty()) {
                    emptyWrapper.setVisibility(View.VISIBLE);
                    rvBudgets.setVisibility(View.GONE);
                } else {
                    emptyWrapper.setVisibility(View.GONE);
                    rvBudgets.setVisibility(View.VISIBLE);
                    budgetAdapter.setItems(budgetWithDetails);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAdapter() {
        try {

            budgetAdapter = new RecyclerViewAdapter<>(requireActivity(), new ArrayList<>(), R.layout.item_budget_detail) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, BudgetWithDetails budgetWithDetails) {
                    BudgetEntity budget = budgetWithDetails.budget;

                    MaterialCardView cardBudgetIcon = holder.getView(R.id.cardBudgetIcon);
                    AppCompatImageView ivBudgetIcon = holder.getView(R.id.ivBudgetIcon);
                    AppCompatTextView tvBudgetName = holder.getView(R.id.tvBudgetName);
                    AppCompatImageView ivBudgetDot = holder.getView(R.id.ivBudgetDot);
                    AppCompatTextView tvBudgetPeriod = holder.getView(R.id.tvBudgetPeriod);
                    AppCompatTextView tvBudgetDate = holder.getView(R.id.tvBudgetDate);
                    AppCompatTextView tvSavedAmount = holder.getView(R.id.tvSavedAmount);
                    AppCompatTextView tvTargetAmount = holder.getView(R.id.tvTargetAmount);
                    ProgressBar progressBudget = holder.getView(R.id.progressBudget);
                    AppCompatImageView ivMore = holder.getView(R.id.ivMore);
                    AppCompatImageView ivAlertIcon = holder.getView(R.id.ivAlertIcon);
                    AppCompatTextView tvWallets = holder.getView(R.id.tvWallets);
                    AppCompatTextView tvCategories = holder.getView(R.id.tvCategories);
                    AppCompatTextView tvAlert = holder.getView(R.id.tvAlert);
                    AppCompatTextView tvProgressPercentage = holder.getView(R.id.tvProgressPercentage);
                    LinearLayout layoutAlert = holder.getView(R.id.layoutAlert);

                    int budgetColor = Color.parseColor(budget.budgetColor);
                    int progress = CommonUtils.calculateProgress(budgetWithDetails.spentAmount, budget.amount);

                    cardBudgetIcon.setCardBackgroundColor(budgetColor);
                    ivBudgetIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(), DataHelper.getCategoryIcons().get(budget.budgetIcon)));

                    tvBudgetName.setText(budget.name);
                    ImageViewCompat.setImageTintList(ivBudgetDot, ColorStateList.valueOf(budgetColor));

                    tvBudgetPeriod.setText(DataHelper.getPeriodName(budget.periodId));
                    tvBudgetDate.setText(DateHelper.formatDateRange(budget.startDate, budget.endDate));

                    tvSavedAmount.setText(CommonUtils.getBeautifyAmount(budget.currencySymbol, budgetWithDetails.spentAmount));
                    tvTargetAmount.setText(getString(R.string.target_amount_value, CommonUtils.getBeautifyAmount(budget.currencySymbol, budget.amount)));

                    progressBudget.setProgressDrawable(CommonUtils.createGoalProgressDrawable(requireActivity(), budgetColor));
                    progressBudget.setProgress(progress);
                    tvProgressPercentage.setText(getString(R.string.alert_percentage_value, progress));
                    tvProgressPercentage.setTextColor(budgetColor);

                    int walletCount = budget.walletCount;
                    if(walletCount == -1) {
                        tvWallets.setText(getString(R.string.all_wallets));
                    } else {
                        tvWallets.setText(getResources().getQuantityString(R.plurals.wallet_count, walletCount, walletCount));
                    }

                    int categoryCount = budget.categoryCount;
                    if(budget.isAllCategory) {
                        tvCategories.setText(getString(R.string.all_categories));
                    } else {
                        tvCategories.setText(getResources().getQuantityString(R.plurals.category_amount_count, categoryCount, categoryCount));
                    }

                    if(budget.alertEnabled) {
                        layoutAlert.setVisibility(View.VISIBLE);
                        tvAlert.setText(getString(R.string.alert_at_percentage, budget.alertPercentage));
                        tvAlert.setTextColor(budgetColor);
                    } else {
                        layoutAlert.setVisibility(View.GONE);
                    }

                    ivAlertIcon.setImageTintList(ColorStateList.valueOf(budgetColor));

                    ivMore.setOnClickListener(v -> showOptionDialog(budgetWithDetails));
                }
            };

            rvBudgets.setAdapter(budgetAdapter);
            rvBudgets.setHasFixedSize(true);
            rvBudgets.setItemAnimator(null);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapter", e);
        }
    }

    private void showOptionDialog(BudgetWithDetails budgetDetail) {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(requireActivity());
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_budget_options, requireActivity().findViewById(android.R.id.content), false);
            MaterialCardView colorView = bottomView.findViewById(R.id.colorView);
            AppCompatImageView ivBudgetIcon = bottomView.findViewById(R.id.ivBudgetIcon);
            AppCompatTextView nameLabel = bottomView.findViewById(R.id.nameLabel);
            AppCompatTextView tvBudgetPeriod = bottomView.findViewById(R.id.tvBudgetPeriod);
            AppCompatTextView tvBudgetAmount = bottomView.findViewById(R.id.tvBudgetAmount);
            LinearLayout optionEdit = bottomView.findViewById(R.id.optionEdit);
            View viewEdit = bottomView.findViewById(R.id.viewEdit);
            LinearLayout optionViewDetails = bottomView.findViewById(R.id.optionViewDetails);
            LinearLayout optionPaused = bottomView.findViewById(R.id.optionPaused);
            View viewPaused = bottomView.findViewById(R.id.viewPaused);
            LinearLayout optionArchive = bottomView.findViewById(R.id.optionArchive);
            View viewArchive = bottomView.findViewById(R.id.viewArchive);
            LinearLayout optionDelete = bottomView.findViewById(R.id.optionDelete);

            optionEdit.setVisibility(View.VISIBLE);
            viewEdit.setVisibility(View.VISIBLE);
            optionPaused.setVisibility(View.VISIBLE);
            viewPaused.setVisibility(View.VISIBLE);
            optionArchive.setVisibility(View.VISIBLE);
            viewArchive.setVisibility(View.VISIBLE);

            BudgetEntity budget = budgetDetail.budget;

            colorView.setCardBackgroundColor(Color.parseColor(budget.budgetColor));
            ivBudgetIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(), DataHelper.getCategoryIcons().get(budget.budgetIcon)));
            nameLabel.setText(budget.name);
            tvBudgetPeriod.setText(DataHelper.getPeriodName(budget.periodId));
            tvBudgetAmount.setText(CommonUtils.getBeautifyAmount(budget.currencySymbol, budget.amount));

            // EDIT
            optionEdit.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(requireActivity(), CreateBudgetActivity.class)
                        .putExtra("isEdit", true)
                        .putExtra("budgetId", budget.id));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            // VIEW
            optionViewDetails.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(requireActivity(), BudgetDetailActivity.class)
                        .putExtra("budgetId", budget.id));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            // PAUSE
            optionPaused.setOnClickListener(v -> {
                dialog.dismiss();
                showPauseDialog(budget);
            });

            // ARCHIVE
            optionArchive.setOnClickListener(v -> {
                dialog.dismiss();
                showArchiveDialog(budget);
            });

            // DELETE
            optionDelete.setOnClickListener(v -> {
                dialog.dismiss();
                showDeleteDialog(budget);
            });

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showOptionDialog", e);
        }
    }

    private void showArchiveDialog(BudgetEntity budget) {

        AlertDialog dialog = new AlertDialog.Builder(requireActivity()).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);

        MaterialCardView cardHeader = view.findViewById(R.id.cardHeader);
        AppCompatImageView headerImage = view.findViewById(R.id.headerImage);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        MaterialButton tvDelete = view.findViewById(R.id.tvDelete);
        tvTitle.setText(R.string.archive_budget);
        tvMessage.setText(R.string.archive_budget_message);
        tvSubMessage.setText(R.string.archive_budget_sub_message);
        tvSubMessage.setVisibility(View.VISIBLE);
        tvDelete.setText(getString(R.string.archive));

        cardHeader.setCardBackgroundColor(requireActivity().getColor(R.color.category_light));
        headerImage.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_archive_outline));
        headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.category_dark)));
        tvDelete.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.category_dark)));

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            archiveBudget(budget);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void archiveBudget(BudgetEntity budget) {
        try {
            if (budget == null) {
                return;
            }

            if (budgetViewModel.archiveRestoreBudget(budget.id, true, false)) {
                Toast.makeText(requireActivity(), getString(R.string.budget_archived), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireActivity(), getString(R.string.error_budget_archive), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "archiveBudget", e);
        }
    }

    private void showPauseDialog(BudgetEntity budget) {

        AlertDialog dialog = new AlertDialog.Builder(requireActivity()).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);

        MaterialCardView cardHeader = view.findViewById(R.id.cardHeader);
        AppCompatImageView headerImage = view.findViewById(R.id.headerImage);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        MaterialButton tvDelete = view.findViewById(R.id.tvDelete);
        tvTitle.setText(R.string.pause_budget);
        tvMessage.setText(R.string.pause_budget_message);
        tvSubMessage.setText(R.string.pause_budget_sub_message);
        tvSubMessage.setVisibility(View.VISIBLE);
        tvDelete.setText(getString(R.string.text_pause));

        cardHeader.setCardBackgroundColor(requireActivity().getColor(R.color.color_pause_light));
        headerImage.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_pause_outline));
        headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.color_pause_dark)));
        tvDelete.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.color_pause_dark)));

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            pauseBudget(budget);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void pauseBudget(BudgetEntity budget) {
        try {
            if (budget == null) {
                return;
            }

            if (budgetViewModel.pauseBudget(budget.id, true)) {
                Toast.makeText(requireActivity(), getString(R.string.budget_paused), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireActivity(), getString(R.string.error_budget_pause), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "pauseBudget", e);
        }
    }

    private void showDeleteDialog(BudgetEntity budget) {

        AlertDialog dialog = new AlertDialog.Builder(requireActivity()).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        tvTitle.setText(R.string.delete_budget);
        tvMessage.setText(R.string.delete_budget_message);
        tvSubMessage.setText(R.string.delete_budget_sub_message);
        tvSubMessage.setVisibility(View.VISIBLE);
        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            deleteBudget(budget);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteBudget(BudgetEntity budget) {
        try {
            if (budget == null) {
                return;
            }

            if (budgetViewModel.deleteBudget(budget.id)) {
                Toast.makeText(requireActivity(), R.string.budget_deleted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireActivity(), R.string.error_delete_budget, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteBudget", e);
        }
    }
}