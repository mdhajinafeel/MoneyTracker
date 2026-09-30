package com.nprotech.moneytracker.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
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
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.DebtLoanType;
import com.nprotech.moneytracker.db.entites.DebtLoanEntity;
import com.nprotech.moneytracker.enums.SettingType;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.models.DebtLoanWithDetails;
import com.nprotech.moneytracker.models.SettingItemModel;
import com.nprotech.moneytracker.ui.activities.CreateDebtLoanActivity;
import com.nprotech.moneytracker.ui.activities.DebtLoanDetailActivity;
import com.nprotech.moneytracker.ui.activities.DebtLoanPaymentDetailActivity;
import com.nprotech.moneytracker.ui.activities.DebtLoanRecordPaymentActivity;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.DebtLoanViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoanFragment extends Fragment {

    private RecyclerView rvDebtLoans;
    private ConstraintLayout emptyWrapper;
    private DebtLoanViewModel debtLoanViewModel;
    private RecyclerViewAdapter<DebtLoanWithDetails> loanAdapter;
    private MaterialCardView sortingContainer;
    private AppCompatTextView tvSorting;
    private SettingType selectedSortType = SettingType.NEWEST_FIRST;
    private SettingType selectedStatusType = SettingType.DEBTS_ALL;
    private DebtLoanEntity selectedDebtLoan;
    private Typeface medium, semiBold;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_debt_loan, container, false);
        try {
            View root = view.findViewById(R.id.rootView);
            rvDebtLoans = view.findViewById(R.id.rvDebtLoans);
            emptyWrapper = view.findViewById(R.id.emptyWrapper);
            AppCompatTextView emptyTitleLabel = view.findViewById(R.id.emptyTitleLabel);
            AppCompatTextView emptyTitleDesc = view.findViewById(R.id.emptyTitleDesc);

            sortingContainer = view.findViewById(R.id.sortingContainer);
            tvSorting = view.findViewById(R.id.tvSorting);

            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            debtLoanViewModel = new ViewModelProvider(this).get(DebtLoanViewModel.class);

            CommonUtils.setDrawable(requireActivity(), tvSorting, R.drawable.ic_filter, R.dimen.icon_12, R.color.primary_dark, Gravity.START);

            emptyTitleLabel.setText(getString(R.string.no_loans_yet));
            emptyTitleDesc.setText(getString(R.string.empty_loan_hint));

            initializeAdapter();
            observeData();
            bindData();
            setupListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void bindData() {
        try {
            medium = ResourcesCompat.getFont(requireActivity(), R.font.exo2_medium);
            semiBold = ResourcesCompat.getFont(requireActivity(), R.font.exo2_semibold);

            selectedSortType = SettingType.NEWEST_FIRST;
            selectedStatusType = SettingType.DEBTS_ALL;
            updateSortingLabel();

            loadDebtLoanHistory();
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void loadDebtLoanHistory() {
        debtLoanViewModel.loadDebtLoanHistory(DebtLoanType.LENT, getSortValue(selectedSortType), getStatusFilterValue(selectedStatusType));
    }

    private void observeData() {
        try {
            debtLoanViewModel.getDebtLoanList().observe(getViewLifecycleOwner(), debtLoans -> {
                if (debtLoans.isEmpty()) {
                    emptyWrapper.setVisibility(View.VISIBLE);
                    rvDebtLoans.setVisibility(View.GONE);
                } else {
                    emptyWrapper.setVisibility(View.GONE);
                    rvDebtLoans.setVisibility(View.VISIBLE);
                    loanAdapter.setItems(debtLoans);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "observeData", e);
        }
    }

    private void initializeAdapter() {
        try {

            loanAdapter = new RecyclerViewAdapter<>(requireActivity(), new ArrayList<>(), R.layout.item_debt_loan_detail) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, DebtLoanWithDetails debtLoanDetail) {

                    DebtLoanEntity debtLoan = debtLoanDetail.debtLoan;

                    MaterialCardView cardDebtIcon = holder.getView(R.id.cardDebtIcon);
                    AppCompatImageView ivDebtIcon = holder.getView(R.id.ivDebtIcon);
                    AppCompatTextView tvDebtTitle = holder.getView(R.id.tvDebtTitle);
                    AppCompatTextView tvDebtDesc = holder.getView(R.id.tvDebtDesc);
                    AppCompatTextView tvStartDate = holder.getView(R.id.tvStartDate);
                    AppCompatTextView tvDebtLoanStatus = holder.getView(R.id.tvDebtLoanStatus);
                    AppCompatTextView tvSavedAmount = holder.getView(R.id.tvSavedAmount);
                    AppCompatTextView tvTargetAmount = holder.getView(R.id.tvTargetAmount);
                    ProgressBar progressBudget = holder.getView(R.id.progressBudget);
                    AppCompatTextView tvProgressPercentage = holder.getView(R.id.tvProgressPercentage);
                    AppCompatImageView ivMore = holder.getView(R.id.ivMore);
                    LinearLayout layoutDaysLeft = holder.getView(R.id.layoutDaysLeft);
                    AppCompatTextView tvDaysLeft = holder.getView(R.id.tvDaysLeft);
                    AppCompatImageView ivAlertIcon = holder.getView(R.id.ivAlertIcon);
                    AppCompatTextView tvAlert = holder.getView(R.id.tvAlert);

                    CommonUtils.setDrawable(requireActivity(), tvStartDate, R.drawable.ic_calendar, R.dimen.icon_12, R.color.dark_grey, Gravity.START);
                    tvDebtTitle.setText(debtLoan.name);
                    tvDebtDesc.setText(debtLoan.title);

                    int debtLoanColor = Color.parseColor(DataHelper.getDebtLoanColorList().get(debtLoan.debtLoanColor));
                    int progress = CommonUtils.calculateProgress(debtLoan.paidAmount, debtLoan.totalAmount);

                    cardDebtIcon.setCardBackgroundColor(debtLoanColor);
                    ivDebtIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(), DataHelper.getCategoryIcons().get(debtLoan.debtLoanIcon)));

                    if (debtLoanDetail.statusType == 1) {
                        tvDebtLoanStatus.setText(getString(R.string.text_ongoing));
                        setDebtLoanStatusStyle(tvDebtLoanStatus, R.color.orange, R.color.light_orange, R.color.orange);
                    } else if (debtLoanDetail.statusType == 2) {
                        tvDebtLoanStatus.setText(getString(R.string.text_overdue));
                        setDebtLoanStatusStyle(tvDebtLoanStatus, R.color.expense, R.color.light_expense, R.color.expense);
                    } else if (debtLoanDetail.statusType == 3) {
                        tvDebtLoanStatus.setText(getString(R.string.text_completed));
                        setDebtLoanStatusStyle(tvDebtLoanStatus, R.color.dark_income, R.color.light_income, R.color.dark_income);
                    }

                    tvStartDate.setText(DateHelper.getFormattedDate(debtLoan.startDate));

                    if (debtLoan.dueDate != null) {
                        layoutDaysLeft.setVisibility(View.VISIBLE);

                        long daysLeft = getDaysLeft(debtLoan.dueDate);

                        if (daysLeft > 0) {
                            tvDaysLeft.setText(getResources().getQuantityString(R.plurals.days_count, (int) daysLeft, daysLeft));
                        } else if (daysLeft == 0) {
                            tvDaysLeft.setText(getString(R.string.due_today));
                        } else {
                            tvDaysLeft.setText(getResources().getQuantityString(R.plurals.days_overdue, (int) Math.abs(daysLeft), Math.abs(daysLeft)));
                        }

                    } else {
                        layoutDaysLeft.setVisibility(View.GONE);
                    }

                    tvSavedAmount.setText(CommonUtils.getBeautifyAmount(debtLoan.currencySymbol, debtLoan.paidAmount));
                    tvTargetAmount.setText(getString(R.string.target_amount_value, CommonUtils.getBeautifyAmount(debtLoan.currencySymbol, debtLoan.totalAmount)));

                    progressBudget.setProgressDrawable(CommonUtils.createGoalProgressDrawable(requireActivity(), debtLoanColor));
                    progressBudget.setProgress(progress);
                    tvProgressPercentage.setText(getString(R.string.alert_percentage_value, progress));
                    tvProgressPercentage.setTextColor(debtLoanColor);

                    ivAlertIcon.setImageTintList(ColorStateList.valueOf(debtLoanColor));
                    tvAlert.setTextColor(debtLoanColor);
                    if (debtLoan.reminderEnabled && debtLoan.dueDate != null) {
                        Calendar reminderCalendar = getReminderCalendar(debtLoan);
                        String reminderDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(reminderCalendar.getTime());
                        String reminderTime = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(reminderCalendar.getTime());
                        tvAlert.setText(getString(R.string.reminder_with_time, reminderDate, reminderTime));
                    } else {
                        tvAlert.setText(getString(R.string.not_set));
                    }

                    ivMore.setOnClickListener(v -> showOptionDialog(debtLoan));
                }
            };

            rvDebtLoans.setAdapter(loanAdapter);
            rvDebtLoans.setHasFixedSize(true);
            rvDebtLoans.setItemAnimator(null);
            rvDebtLoans.addOnScrollListener(
                    new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            if (dy <= 0) {
                                return;
                            }

                            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                            if (layoutManager == null) {
                                return;
                            }

                            int visibleItemCount = layoutManager.getChildCount();
                            int totalItemCount = layoutManager.getItemCount();
                            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                            if ((visibleItemCount + firstVisibleItemPosition + 10) >= totalItemCount) {
                                debtLoanViewModel.loadNextPage();
                            }
                        }
                    }
            );
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapter", e);
        }
    }

    @NonNull
    private static Calendar getReminderCalendar(DebtLoanEntity debtLoan) {
        Calendar reminderCalendar = Calendar.getInstance();
        reminderCalendar.setTimeInMillis(debtLoan.dueDate);

        // Subtract reminder days
        reminderCalendar.add(Calendar.DAY_OF_MONTH, -debtLoan.reminderDays);

        // Set reminder time
        reminderCalendar.set(Calendar.HOUR_OF_DAY, debtLoan.reminderHour);
        reminderCalendar.set(Calendar.MINUTE, debtLoan.reminderMinute);
        reminderCalendar.set(Calendar.SECOND, 0);
        reminderCalendar.set(Calendar.MILLISECOND, 0);
        return reminderCalendar;
    }

    private void setupListeners() {
        try {
            sortingContainer.setOnClickListener(v -> showFilterDialog());

            debtLoanViewModel.getDataSavedStatus().observe(getViewLifecycleOwner(), success -> {
                if (success == null) {
                    return;
                }

                if (success) {
                    if (selectedDebtLoan.type == DebtLoanType.BORROW) {
                        Toast.makeText(requireActivity(), R.string.debt_deleted, Toast.LENGTH_SHORT).show();
                    } else if (selectedDebtLoan.type == DebtLoanType.LENT) {
                        Toast.makeText(requireActivity(), R.string.loan_deleted, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (selectedDebtLoan.type == DebtLoanType.BORROW) {
                        Toast.makeText(requireActivity(), R.string.error_debt_delete, Toast.LENGTH_SHORT).show();
                    } else if (selectedDebtLoan.type == DebtLoanType.LENT) {
                        Toast.makeText(requireActivity(), R.string.error_loan_delete, Toast.LENGTH_SHORT).show();
                    }
                }

                selectedDebtLoan = null;
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showFilterDialog() {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(requireActivity());
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_filter_option, requireActivity().findViewById(android.R.id.content), false);

            AppCompatTextView tvStatus = bottomView.findViewById(R.id.tvStatus);
            AppCompatTextView tvAttachments = bottomView.findViewById(R.id.tvAttachments);
            RecyclerView rvStatus = bottomView.findViewById(R.id.rvStatus);
            RecyclerView rvSortBy = bottomView.findViewById(R.id.rvSortBy);
            RecyclerView rvAttachments = bottomView.findViewById(R.id.rvAttachments);
            MaterialButton btnPrimary = bottomView.findViewById(R.id.btnPrimary);
            MaterialButton btnSecondary = bottomView.findViewById(R.id.btnSecondary);
            AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);

            tvStatus.setVisibility(View.VISIBLE);
            rvStatus.setVisibility(View.VISIBLE);
            tvAttachments.setVisibility(View.GONE);
            rvAttachments.setVisibility(View.GONE);

            // ---------------------------------------------------------
            // Sort options
            // ---------------------------------------------------------
            List<SettingItemModel> sortByList = new ArrayList<>();
            sortByList.add(new SettingItemModel(SettingType.NEWEST_FIRST, 0, 0, 0,
                    getString(R.string.newest_first), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.OLDEST_FIRST, 0, 0, 0,
                    getString(R.string.oldest_first), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.LARGEST_FIRST, 0, 0, 0,
                    getString(R.string.largest_first), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.SMALLEST_FIRST, 0, 0, 0,
                    getString(R.string.smallest_first), true, false, null, true, false, 0));

            RecyclerViewAdapter<SettingItemModel> sortByAdapter = new RecyclerViewAdapter<>(requireActivity(), sortByList, R.layout.item_backup_filter_option) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, SettingItemModel item) {

                    AppCompatTextView tvFilterName = holder.getView(R.id.tvFilterName);
                    AppCompatImageView ivSelected = holder.getView(R.id.ivSelected);
                    boolean selected = item.settingType == selectedSortType;

                    tvFilterName.setText(item.title);
                    ivSelected.setVisibility(selected ? View.VISIBLE : View.GONE);
                    tvFilterName.setTypeface(selected ? semiBold : medium);

                    holder.getView(R.id.rlFilterView).setOnClickListener(v -> {
                        selectedSortType = item.settingType;
                        notifyDataSetChanged();
                    });
                }
            };
            rvSortBy.setAdapter(sortByAdapter);
            rvSortBy.setHasFixedSize(true);
            rvSortBy.setItemAnimator(null);

            // ---------------------------------------------------------
            // Filter options
            // ---------------------------------------------------------
            List<SettingItemModel> filterList = new ArrayList<>();
            filterList.add(new SettingItemModel(SettingType.DEBTS_ALL, 0, 0, 0,
                    getString(R.string.all_debts), true, false, null, true, false, 0));
            filterList.add(new SettingItemModel(SettingType.DEBTS_ONGOING, 0, 0, 0,
                    getString(R.string.text_ongoing), true, false, null, true, false, 0));
            filterList.add(new SettingItemModel(SettingType.DEBTS_COMPLETED, 0, 0, 0,
                    getString(R.string.text_completed), true, false, null, true, false, 0));
            filterList.add(new SettingItemModel(SettingType.DEBTS_OVERDUE, 0, 0, 0,
                    getString(R.string.text_overdue), true, false, null, true, false, 0));

            RecyclerViewAdapter<SettingItemModel> filterAdapter = new RecyclerViewAdapter<>(requireActivity(), filterList, R.layout.item_backup_filter_option) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, SettingItemModel item) {

                    AppCompatTextView tvFilterName = holder.getView(R.id.tvFilterName);
                    tvFilterName.setText(item.title);

                    AppCompatImageView ivSelected = holder.getView(R.id.ivSelected);
                    boolean selected = item.settingType == selectedStatusType;

                    tvFilterName.setTypeface(selected ? semiBold : medium);
                    ivSelected.setVisibility(selected ? View.VISIBLE : View.GONE);
                    holder.getView(R.id.rlFilterView).setOnClickListener(v -> {
                        selectedStatusType = item.settingType;
                        notifyDataSetChanged();
                    });
                }
            };
            rvStatus.setAdapter(filterAdapter);
            rvStatus.setHasFixedSize(true);
            rvStatus.setItemAnimator(null);

            btnPrimary.setOnClickListener(v -> {
                applyLoanFilters();
                dialog.dismiss();
            });

            btnSecondary.setOnClickListener(v -> {
                selectedSortType = SettingType.NEWEST_FIRST;
                selectedStatusType = SettingType.DEBTS_ALL;
                applyLoanFilters();
                dialog.dismiss();
            });

            tvClose.setOnClickListener(v -> dialog.dismiss());

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showFilterDialog", e);
        }
    }

    private void applyLoanFilters() {
        try {
            loadDebtLoanHistory();
            updateSortingLabel();
        } catch (Exception e) {
            AppLogger.e(getClass(), "applyBackupFilters", e);
        }
    }

    private void updateSortingLabel() {
        try {
            switch (selectedSortType) {
                case NEWEST_FIRST:
                    tvSorting.setText(getString(R.string.newest_first));
                    break;

                case OLDEST_FIRST:
                    tvSorting.setText(getString(R.string.oldest_first));
                    break;

                case LARGEST_FIRST:
                    tvSorting.setText(getString(R.string.largest_first));
                    break;

                case SMALLEST_FIRST:
                    tvSorting.setText(getString(R.string.smallest_first));
                    break;
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "updateSortingLabel", e);
        }
    }

    private int getSortValue(SettingType type) {
        if (type == SettingType.NEWEST_FIRST) {
            return 1;
        } else if (type == SettingType.OLDEST_FIRST) {
            return 2;
        } else if (type == SettingType.LARGEST_FIRST) {
            return 3;
        } else if (type == SettingType.SMALLEST_FIRST) {
            return 4;
        }

        return 1;
    }

    private int getStatusFilterValue(SettingType type) {

        if (type == SettingType.DEBTS_ALL) {
            return 1;
        } else if (type == SettingType.DEBTS_ONGOING) {
            return 2;
        } else if (type == SettingType.DEBTS_COMPLETED) {
            return 3;
        } else if (type == SettingType.DEBTS_OVERDUE) {
            return 4;
        }

        return 1;
    }

    private long getDaysLeft(long dueDate) {
        Calendar today = Calendar.getInstance();
        Calendar due = Calendar.getInstance();

        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        due.setTimeInMillis(dueDate);
        due.set(Calendar.HOUR_OF_DAY, 0);
        due.set(Calendar.MINUTE, 0);
        due.set(Calendar.SECOND, 0);
        due.set(Calendar.MILLISECOND, 0);

        long difference = due.getTimeInMillis() - today.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(difference);
    }

    private void showOptionDialog(DebtLoanEntity debtLoanDetail) {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(requireActivity());
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_debt_loan_options, requireActivity().findViewById(android.R.id.content), false);
            MaterialCardView colorView = bottomView.findViewById(R.id.colorView);
            AppCompatImageView ivIcon = bottomView.findViewById(R.id.ivIcon);
            AppCompatTextView nameLabel = bottomView.findViewById(R.id.nameLabel);
            AppCompatTextView lblViewSchedule = bottomView.findViewById(R.id.lblViewSchedule);
            AppCompatImageView ivDot = bottomView.findViewById(R.id.ivDot);
            AppCompatTextView tvTotalAmount = bottomView.findViewById(R.id.tvTotalAmount);
            LinearLayout optionEdit = bottomView.findViewById(R.id.optionEdit);
            LinearLayout optionViewDetails = bottomView.findViewById(R.id.optionViewDetails);
            LinearLayout optionRecord = bottomView.findViewById(R.id.optionRecord);
            LinearLayout optionSchedule = bottomView.findViewById(R.id.optionSchedule);
            LinearLayout optionDelete = bottomView.findViewById(R.id.optionDelete);

            if(debtLoanDetail.repaymentMethod == DebtLoanType.REPAYMENT_FLEXIBLE) {
                lblViewSchedule.setText(R.string.view_history);
            } else {
                lblViewSchedule.setText(R.string.view_schedule);
            }

            int color = Color.parseColor(DataHelper.getDebtLoanColorList().get(debtLoanDetail.debtLoanColor));

            colorView.setCardBackgroundColor(color);
            ivIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(), DataHelper.getCategoryIcons().get(debtLoanDetail.debtLoanIcon)));
            ImageViewCompat.setImageTintList(ivDot, ColorStateList.valueOf(color));
            nameLabel.setText(debtLoanDetail.name);
            tvTotalAmount.setText(CommonUtils.getBeautifyAmount(debtLoanDetail.currencySymbol, debtLoanDetail.totalAmount));

            // EDIT
            optionEdit.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(requireActivity(), CreateDebtLoanActivity.class)
                        .putExtra("isEdit", true)
                        .putExtra("debtLoanId", debtLoanDetail.id));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            // VIEW
            optionViewDetails.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(requireActivity(), DebtLoanDetailActivity.class)
                        .putExtra("debtLoanType", DebtLoanType.LENT)
                        .putExtra("debtLoanId", debtLoanDetail.id));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            // RECORD
            optionRecord.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(requireActivity(), DebtLoanRecordPaymentActivity.class)
                        .putExtra("isEdit", false)
                        .putExtra("debtLoanId", debtLoanDetail.id));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            // SCHEDULE
            optionSchedule.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(requireActivity(), DebtLoanPaymentDetailActivity.class)
                        .putExtra("debtLoanId", debtLoanDetail.id));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            // DELETE
            optionDelete.setOnClickListener(v -> {
                dialog.dismiss();
                showDeleteDialog(debtLoanDetail);
            });

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showOptionDialog", e);
        }
    }

    private void showDeleteDialog(DebtLoanEntity debtLoan) {

        AlertDialog dialog = new AlertDialog.Builder(requireActivity()).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);

        tvTitle.setText(R.string.delete_loan);
        tvMessage.setText(R.string.delete_loan_message);

        tvSubMessage.setText(R.string.delete_budget_sub_message);
        tvSubMessage.setVisibility(View.VISIBLE);
        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            deleteDebtLoan(debtLoan);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteDebtLoan(DebtLoanEntity debtLoan) {
        try {
            if (debtLoan == null) {
                return;
            }

            selectedDebtLoan = debtLoan;
            debtLoanViewModel.deleteDebtLoan(debtLoan.id);
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteDebtLoan", e);
        }
    }

    private void setDebtLoanStatusStyle(AppCompatTextView tvDebtLoanStatus, int textColor, int backgroundColor, int strokeColor) {
        tvDebtLoanStatus.setTextColor(ContextCompat.getColor(requireActivity(), textColor));
        Drawable background = AppCompatResources.getDrawable(requireActivity(), R.drawable.bg_badge_income);
        if (background != null) {
            background = background.mutate();
            if (background instanceof GradientDrawable drawable) {
                drawable.setColor(ContextCompat.getColor(requireActivity(), backgroundColor));
                drawable.setStroke(CommonUtils.dpToPx(requireActivity(), 1), ContextCompat.getColor(requireActivity(), strokeColor));
            }
            tvDebtLoanStatus.setBackground(background);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (debtLoanViewModel != null) {
            loadDebtLoanHistory();
        }
    }
}