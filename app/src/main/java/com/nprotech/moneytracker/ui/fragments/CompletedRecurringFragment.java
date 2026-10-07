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
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.db.entites.RecurringTransactionEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.RecurringTransactionWithDetails;
import com.nprotech.moneytracker.ui.activities.RecurringTransactionDetailActivity;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.RecurringTransactionViewModel;

import java.util.ArrayList;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CompletedRecurringFragment extends Fragment {

    private RecyclerView rvRecurring;
    private ConstraintLayout emptyWrapper;
    private RecurringTransactionViewModel recurringTransactionViewModel;
    private RecyclerViewAdapter<RecurringTransactionWithDetails> recurringAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recurring, container, false);
        try {
            View root = view.findViewById(R.id.rootView);
            rvRecurring = view.findViewById(R.id.rvRecurring);
            emptyWrapper = view.findViewById(R.id.emptyWrapper);

            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            recurringTransactionViewModel = new ViewModelProvider(this).get(RecurringTransactionViewModel.class);

            bindData();
            initializeAdapter();
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void bindData() {
        try {

            recurringTransactionViewModel.getRecurringTransactions(PreferenceManager.INSTANCE.getAccountId(), false,
                    true).observe(getViewLifecycleOwner(), recurringTransactions -> {
                if (recurringTransactions.isEmpty()) {
                    emptyWrapper.setVisibility(View.VISIBLE);
                    rvRecurring.setVisibility(View.GONE);
                } else {
                    emptyWrapper.setVisibility(View.GONE);
                    rvRecurring.setVisibility(View.VISIBLE);
                    recurringAdapter.setItems(recurringTransactions);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAdapter() {
        try {

            recurringAdapter = new RecyclerViewAdapter<>(requireActivity(), new ArrayList<>(), R.layout.item_recurring_detail) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, RecurringTransactionWithDetails recurring) {

                    RecurringTransactionEntity recurringTransaction = recurring.recurringTransaction;

                    MaterialCardView cardCategoryIcon = holder.getView(R.id.cardCategoryIcon);
                    AppCompatImageView ivCategoryIcon = holder.getView(R.id.ivCategoryIcon);
                    AppCompatTextView nameLabel = holder.getView(R.id.nameLabel);
                    AppCompatTextView amountLabel = holder.getView(R.id.amountLabel);
                    AppCompatTextView tvRecurringType = holder.getView(R.id.tvRecurringType);
                    AppCompatTextView tvRepeatType = holder.getView(R.id.tvRepeatType);
                    AppCompatTextView tvRepeatPeriod = holder.getView(R.id.tvRepeatPeriod);
                    AppCompatTextView nextRecurring = holder.getView(R.id.nextRecurring);
                    AppCompatImageView ivRepeatDot = holder.getView(R.id.ivRepeatDot);
                    AppCompatImageView ivMore = holder.getView(R.id.ivMore);

                    int color = Color.parseColor(recurring.color);
                    cardCategoryIcon.setCardBackgroundColor(color);

                    if (recurringTransaction.type == TransactionEntity.TYPE_TRANSFER) {
                        ivCategoryIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_transfer));
                    } else {
                        if (recurring.icon == null || recurring.icon == 0) {
                            ivCategoryIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.category_0));
                        } else {
                            ivCategoryIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(), DataHelper.getCategoryIcons().get(recurring.icon)));
                        }
                    }
                    ImageViewCompat.setImageTintList(ivRepeatDot, ColorStateList.valueOf(color));

                    String categoryName = recurringTransaction.getCategoryName(requireActivity());
                    if (Objects.equals(categoryName, "")) {
                        categoryName = recurring.categoryName;
                    }
                    nameLabel.setText(categoryName);

                    double amount = 0;
                    int amountColor = ContextCompat.getColor(requireActivity(), R.color.income);
                    Drawable badge = ContextCompat.getDrawable(requireActivity(), R.drawable.bg_badge_income);
                    String badgeText = getString(R.string.income);
                    if (recurringTransaction.type == TransactionEntity.TYPE_INCOME) {
                        amount = recurringTransaction.amount;
                    } else if (recurringTransaction.type == TransactionEntity.TYPE_EXPENSE) {
                        amount = recurringTransaction.amount * -1;
                        amountColor = ContextCompat.getColor(requireActivity(), R.color.expense);
                        badge = ContextCompat.getDrawable(requireActivity(), R.drawable.bg_badge_expense);
                        badgeText = getString(R.string.expense);
                    } else if (recurringTransaction.type == TransactionEntity.TYPE_TRANSFER) {
                        amount = recurringTransaction.amount;
                        if (recurringTransaction.fee > 0) {
                            amount = amount + recurringTransaction.fee;
                        }
                        amountColor = ContextCompat.getColor(requireActivity(), R.color.transfer);
                        badge = ContextCompat.getDrawable(requireActivity(), R.drawable.bg_badge_transfer);
                        badgeText = getString(R.string.transfer);
                    }

                    amountLabel.setText(CommonUtils.getBeautifyAmount(recurring.currencySymbol, amount));
                    amountLabel.setTextColor(amountColor);

                    tvRecurringType.setBackgroundDrawable(badge);
                    tvRecurringType.setText(badgeText);
                    tvRecurringType.setTextColor(amountColor);

                    if (recurringTransaction.repeatType == Constants.REPEAT_DAILY) {
                        tvRepeatType.setText(getString(R.string.calendar_daily));
                        tvRepeatPeriod.setText(getResources().getQuantityString(R.plurals.every_day_period,
                                recurringTransaction.repeatInterval, recurringTransaction.repeatInterval));
                    } else if (recurringTransaction.repeatType == Constants.REPEAT_WEEKLY) {
                        tvRepeatType.setText(getString(R.string.calendar_weekly));
                        tvRepeatPeriod.setText(getString(R.string.every_week_period, DateHelper.getWeekDaysDisplay(recurringTransaction.repeatWeekDays,
                                requireActivity())));
                    } else if (recurringTransaction.repeatType == Constants.REPEAT_MONTHLY) {
                        tvRepeatType.setText(getString(R.string.calendar_monthly));
                        tvRepeatPeriod.setText(getResources().getQuantityString(R.plurals.every_month_period,
                                recurringTransaction.repeatInterval, recurringTransaction.repeatInterval));
                    } else if (recurringTransaction.repeatType == Constants.REPEAT_YEARLY) {
                        tvRepeatType.setText(getString(R.string.calendar_yearly));
                        tvRepeatPeriod.setText(getResources().getQuantityString(R.plurals.every_year_period,
                                recurringTransaction.repeatInterval, recurringTransaction.repeatInterval));
                    }

                    if (recurringTransaction.nextRunDate > 0) {
                        nextRecurring.setText(getString(R.string.next_recurring, DateHelper.getFormattedDate(recurringTransaction.nextRunDate)));
                    }

                    ivMore.setOnClickListener(v -> showOptionDialog(recurring));
                }
            };
            rvRecurring.setAdapter(recurringAdapter);
            rvRecurring.setHasFixedSize(true);
            rvRecurring.setItemAnimator(null);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapter", e);
        }
    }

    private void showOptionDialog(RecurringTransactionWithDetails recurring) {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(requireActivity());
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_recurring_options, requireActivity().findViewById(android.R.id.content), false);

            LinearLayout optionViewDetails = bottomView.findViewById(R.id.optionViewDetails);
            LinearLayout optionEdit = bottomView.findViewById(R.id.optionEdit);
            View viewEdit = bottomView.findViewById(R.id.viewEdit);
            LinearLayout optionActivate = bottomView.findViewById(R.id.optionActivate);
            View viewActivate = bottomView.findViewById(R.id.viewActivate);
            LinearLayout optionDeactivate = bottomView.findViewById(R.id.optionDeactivate);
            View viewDeactivate = bottomView.findViewById(R.id.viewDeactivate);
            LinearLayout optionDelete = bottomView.findViewById(R.id.optionDelete);

            optionActivate.setVisibility(View.GONE);
            optionDeactivate.setVisibility(View.GONE);
            optionEdit.setVisibility(View.GONE);
            viewActivate.setVisibility(View.GONE);
            viewDeactivate.setVisibility(View.GONE);
            viewEdit.setVisibility(View.GONE);

            // VIEW DETAILS
            optionViewDetails.setOnClickListener(view -> {
                Intent intent = new Intent(requireActivity(), RecurringTransactionDetailActivity.class);
                intent.putExtra("transactionId", recurring.recurringTransaction.tempRecurringServerId);
                startActivity(intent);
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
                dialog.dismiss();
                dialog.dismiss();
            });

            // DELETE
            optionDelete.setOnClickListener(view -> {
                dialog.dismiss();
                showDeleteDialog(recurring.recurringTransaction);
            });

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showOptionDialog", e);
        }
    }

    private void showDeleteDialog(RecurringTransactionEntity recurringTransaction) {

        AlertDialog dialog = new AlertDialog.Builder(requireActivity()).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        tvTitle.setText(R.string.delete_recurring);
        tvMessage.setText(R.string.delete_recurring_message);
        tvSubMessage.setVisibility(View.GONE);
        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            deleteRecurring(recurringTransaction);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteRecurring(RecurringTransactionEntity recurringTransaction) {
        try {
            if (recurringTransaction == null) {
                return;
            }

            if (recurringTransactionViewModel.deleteRecurring(recurringTransaction.id)) {
                Toast.makeText(requireActivity(), getString(R.string.recurring_deleted_successfully), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireActivity(), getString(R.string.error_delete_recurring), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteGoal", e);
        }
    }
}