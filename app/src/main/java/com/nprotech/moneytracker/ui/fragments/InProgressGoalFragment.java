package com.nprotech.moneytracker.ui.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.GoalWithDetails;
import com.nprotech.moneytracker.ui.activities.CreateGoalActivity;
import com.nprotech.moneytracker.ui.activities.GoalDetailActivity;
import com.nprotech.moneytracker.ui.activities.GoalMoneyActivity;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.GoalViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class InProgressGoalFragment extends Fragment {

    private RecyclerView rvGoals;
    private ConstraintLayout emptyWrapper;
    private GoalViewModel goalViewModel;
    private RecyclerViewAdapter<GoalWithDetails> goalAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goal, container, false);
        try {
            View root = view.findViewById(R.id.rootView);
            rvGoals = view.findViewById(R.id.rvGoals);
            emptyWrapper = view.findViewById(R.id.emptyWrapper);

            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            goalViewModel = new ViewModelProvider(this).get(GoalViewModel.class);

            bindData();
            initializeAdapter();
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void bindData() {
        try {

            goalViewModel.getGoals((int) PreferenceManager.INSTANCE.getAccountId(), false, false).observe(getViewLifecycleOwner(), goalWithDetails -> {
                if (goalWithDetails.isEmpty()) {
                    emptyWrapper.setVisibility(View.VISIBLE);
                    rvGoals.setVisibility(View.GONE);
                } else {
                    emptyWrapper.setVisibility(View.GONE);
                    rvGoals.setVisibility(View.VISIBLE);
                    goalAdapter.setItems(goalWithDetails);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAdapter() {
        try {

            Drawable calendarDrawable = AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_calendar);

            goalAdapter = new RecyclerViewAdapter<>(requireActivity(), new ArrayList<>(), R.layout.item_goal_detail) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, GoalWithDetails goalWithDetail) {

                    AppCompatTextView tvGoalCategory = holder.getView(R.id.tvGoalCategory);
                    AppCompatTextView tvProgress = holder.getView(R.id.tvProgress);
                    AppCompatTextView tvAutoSave = holder.getView(R.id.tvAutoSave);
                    AppCompatTextView tvTargetDate = holder.getView(R.id.tvTargetDate);
                    AppCompatImageView ivGoalCategoryDot = holder.getView(R.id.ivGoalCategoryDot);
                    ProgressBar progressGoal = holder.getView(R.id.progressGoal);

                    int goalColor = Color.parseColor(goalWithDetail.color);
                    long daysLeft = CommonUtils.calculateDaysLeft(goalWithDetail.targetDate);
                    int progress = CommonUtils.calculateGoalProgress(goalWithDetail.savedAmount, goalWithDetail.targetAmount);

                    holder.setViewText(R.id.tvGoalName, goalWithDetail.name);
                    tvGoalCategory.setText(goalWithDetail.categoryName);
                    holder.setViewText(R.id.tvSavedAmount, CommonUtils.getBeautifyAmount(goalWithDetail.currencySymbol, goalWithDetail.savedAmount));
                    holder.setViewText(R.id.tvTargetAmount, " / " + CommonUtils.getBeautifyAmount(goalWithDetail.currencySymbol, goalWithDetail.targetAmount));
                    tvTargetDate.setText(DateHelper.getFormattedDate(goalWithDetail.targetDate, "dd MMM yyyy"));
                    tvProgress.setText(getResources().getString(R.string.progress_percentage, progress));
                    holder.setViewText(R.id.tvDaysLeft, getResources().getQuantityString(R.plurals.days_count, (int) daysLeft, daysLeft));

                    AppCompatImageView ivGoalIcon = holder.getView(R.id.ivGoalIcon);

                    Drawable background = ivGoalIcon.getBackground().mutate();
                    DrawableCompat.setTint(background, Color.parseColor(goalWithDetail.color));
                    ivGoalIcon.setBackground(background);

                    ivGoalIcon.setImageResource(DataHelper.getGoalIcons().get(goalWithDetail.icon));

                    progressGoal.setProgressDrawable(CommonUtils.createGoalProgressDrawable(requireActivity(), goalColor));
                    progressGoal.setProgress(progress);

                    ImageViewCompat.setImageTintList(ivGoalCategoryDot, ColorStateList.valueOf(goalColor));
                    tvProgress.setTextColor(goalColor);

                    holder.getView(R.id.ivMore).setOnClickListener(view -> showOptionDialog(goalWithDetail));

                    if (goalWithDetail.autoSaveEnabled) {
                        tvAutoSave.setVisibility(View.VISIBLE);
                        tvAutoSave.setText(getString(R.string.auto_save_on_date, getString(R.string.auto_save),
                                CommonUtils.getBeautifyAmount(goalWithDetail.currencySymbol, goalWithDetail.autoSaveAmount),
                                DateHelper.getFormattedDate(goalWithDetail.nextAutoSaveDate, "dd MMM")));
                    } else {
                        tvAutoSave.setVisibility(View.GONE);
                    }

                    if (calendarDrawable != null) {
                        int size = getResources().getDimensionPixelSize(R.dimen.icon_12);
                        calendarDrawable.setBounds(0, 0, size, size);
                        tvTargetDate.setCompoundDrawablesRelative(calendarDrawable, null, null, null);
                    }
                }
            };
            rvGoals.setAdapter(goalAdapter);
            rvGoals.setHasFixedSize(true);
            rvGoals.setItemAnimator(null);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapter", e);
        }
    }

    private void showOptionDialog(GoalWithDetails goal) {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(requireActivity());
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_sheet_goal_options, requireActivity().findViewById(android.R.id.content), false);
            AppCompatTextView tvGoalName = bottomView.findViewById(R.id.tvGoalName);
            AppCompatTextView tvGoalAmount = bottomView.findViewById(R.id.tvGoalAmount);
            LinearLayout optionViewDetails = bottomView.findViewById(R.id.optionViewDetails);
            View viewViewDetails = bottomView.findViewById(R.id.viewViewDetails);
            LinearLayout optionAddMoney = bottomView.findViewById(R.id.optionAddMoney);
            View viewAddMoney = bottomView.findViewById(R.id.viewAddMoney);
            LinearLayout optionEdit = bottomView.findViewById(R.id.optionEdit);
            View viewEdit = bottomView.findViewById(R.id.viewEdit);
            LinearLayout optionWithdrawal = bottomView.findViewById(R.id.optionWithdrawal);
            View viewWithdrawal = bottomView.findViewById(R.id.viewWithdrawal);
            LinearLayout optionComplete = bottomView.findViewById(R.id.optionComplete);
            View viewComplete = bottomView.findViewById(R.id.viewComplete);
            LinearLayout optionArchive = bottomView.findViewById(R.id.optionArchive);
            LinearLayout optionDelete = bottomView.findViewById(R.id.optionDelete);
            View viewLineDelete = bottomView.findViewById(R.id.viewLineDelete);

            tvGoalName.setText(goal.name);
            optionViewDetails.setVisibility(View.VISIBLE);
            viewViewDetails.setVisibility(View.VISIBLE);
            optionEdit.setVisibility(View.VISIBLE);
            viewEdit.setVisibility(View.VISIBLE);
            optionAddMoney.setVisibility(View.VISIBLE);
            viewAddMoney.setVisibility(View.VISIBLE);
            optionWithdrawal.setVisibility(View.VISIBLE);
            viewWithdrawal.setVisibility(View.VISIBLE);
            optionDelete.setVisibility(View.VISIBLE);
            optionComplete.setVisibility(View.VISIBLE);
            viewComplete.setVisibility(View.VISIBLE);
            optionArchive.setVisibility(View.VISIBLE);
            viewLineDelete.setVisibility(View.VISIBLE);

            String savedAmount = CommonUtils.getBeautifyAmount(goal.currencySymbol, goal.savedAmount);
            String targetAmount = CommonUtils.getBeautifyAmount(goal.currencySymbol, goal.targetAmount);
            tvGoalAmount.setText(getString(R.string.goal_amount_progress, savedAmount, targetAmount));

            // ADD MONEY
            optionAddMoney.setOnClickListener(view -> {
                dialog.dismiss();
                startActivity(new Intent(requireActivity(), GoalMoneyActivity.class)
                        .putExtra("addMoney", true)
                        .putExtra("goalId", goal.id));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            // WITHDRAW MONEY
            optionWithdrawal.setOnClickListener(view -> {
                dialog.dismiss();
                startActivity(new Intent(requireActivity(), GoalMoneyActivity.class)
                        .putExtra("addMoney", false)
                        .putExtra("goalId", goal.id));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            // VIEW DETAILS
            optionViewDetails.setOnClickListener(view -> {
                dialog.dismiss();
                startActivity(new Intent(requireActivity(), GoalDetailActivity.class)
                        .putExtra("goalId", goal.id));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            // EDIT DETAILS
            optionEdit.setOnClickListener(view -> {
                dialog.dismiss();
                startActivity(new Intent(requireActivity(), CreateGoalActivity.class)
                        .putExtra("isEdit", true)
                        .putExtra("goalId", goal.id));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            // COMPLETED
            optionComplete.setOnClickListener(view -> {
                dialog.dismiss();
                if (goalViewModel.markAsCompletedGoal(goal.id)) {
                    Toast.makeText(requireActivity(), getString(R.string.goal_achieved_successfully), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireActivity(), getString(R.string.error_completing), Toast.LENGTH_SHORT).show();
                }
            });

            // ARCHIVE
            optionArchive.setOnClickListener(view -> {
                dialog.dismiss();
                if (goalViewModel.archiveRestoreGoal(goal.id, true)) {
                    Toast.makeText(requireActivity(), getString(R.string.goal_archived_successfully), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireActivity(), getString(R.string.error_archive), Toast.LENGTH_SHORT).show();
                }
            });

            // DELETE
            optionDelete.setOnClickListener(view -> {
                dialog.dismiss();
                showDeleteDialog(goal);
            });

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showOptionDialog", e);
        }
    }

    private void showDeleteDialog(GoalWithDetails goal) {

        AlertDialog dialog = new AlertDialog.Builder(requireActivity()).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.delete_goal);
        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            deleteGoal(goal);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteGoal(GoalWithDetails goal) {
        try {
            if (goal == null) {
                return;
            }

            if (goalViewModel.deleteGoal(goal.id)) {
                Toast.makeText(requireActivity(), getString(R.string.goal_deleted_successfully), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireActivity(), getString(R.string.error_delete_goal), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteGoal", e);
        }
    }
}