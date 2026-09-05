package com.nprotech.moneytracker.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.TransactionAttachmentEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.CalendarHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.CalendarDayModel;
import com.nprotech.moneytracker.models.CalendarSummaryModel;
import com.nprotech.moneytracker.models.TransactionWithDetails;
import com.nprotech.moneytracker.ui.activities.CreateTransactionActivity;
import com.nprotech.moneytracker.ui.activities.TransactionDetailActivity;
import com.nprotech.moneytracker.ui.adapters.CalendarAdapter;
import com.nprotech.moneytracker.ui.adapters.TransactionAdapter;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.CalendarViewModel;
import com.nprotech.moneytracker.viewmodel.TransactionViewModel;
import com.nprotech.moneytracker.viewmodel.WalletViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;
import me.grantland.widget.AutofitTextView;

@AndroidEntryPoint
public class CalendarFragment extends Fragment {

    private AppCompatTextView tvDate;
    private AppCompatImageView ivPrevious, ivNext;
    private RecyclerView rvCalendar;
    private View incomeCard, expenseCard, totalCard;
    private Date date;
    private CalendarAdapter calendarAdapter;
    private CalendarViewModel calendarViewModel;
    private AccountViewModel accountViewModel;
    private WalletViewModel walletViewModel;
    private TransactionViewModel transactionViewModel;
    private String currencySymbol = "";
    private List<CalendarDayModel> monthCells;
    private long loadedStart = -1, loadedEnd = -1;
    private Date selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        try {

            ivPrevious = view.findViewById(R.id.ivPrevious);
            ivNext = view.findViewById(R.id.ivNext);
            tvDate = view.findViewById(R.id.tvDate);
            rvCalendar = view.findViewById(R.id.rvCalendar);
            incomeCard = view.findViewById(R.id.cardIncome);
            expenseCard = view.findViewById(R.id.cardExpense);
            totalCard = view.findViewById(R.id.cardTotal);

            calendarViewModel = new ViewModelProvider(requireActivity()).get(CalendarViewModel.class);
            accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
            walletViewModel = new ViewModelProvider(requireActivity()).get(WalletViewModel.class);
            transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

            bindData();
            initializeAdapters();
            loadCalendar();
            observeData();      // Only once
            loadCalendarData(); // First month's data
            setupListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }

    private void bindData() {
        try {
            if (date == null) {
                date = CalendarHelper.getInitialDate();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAdapters() {
        try {

            if (calendarAdapter != null) {
                return;
            }

            int weekStartOn = PreferenceManager.INSTANCE.getWeekStartOn();

            calendarAdapter = new CalendarAdapter(requireContext(), weekStartOn);
            calendarAdapter.setHeaderLayout(R.layout.item_calendar_header);
            GridLayoutManager layoutManager = getGridLayoutManager();
            rvCalendar.setLayoutManager(layoutManager);
            rvCalendar.setAdapter(calendarAdapter);
            rvCalendar.setHasFixedSize(true);
            rvCalendar.setItemAnimator(null);

            calendarAdapter.setOnDateClickListener(this::showDayTransactions);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    @NonNull
    private GridLayoutManager getGridLayoutManager() {
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 7) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return calendarAdapter.getItemViewType(position) == 0 ? 7 : 1;
            }
        });
        return layoutManager;
    }

    private void loadCalendar() {
        int weekStartOn = PreferenceManager.INSTANCE.getWeekStartOn();
        tvDate.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date));
        calendarAdapter.setWeekStartOn(weekStartOn);
        monthCells = CalendarHelper.getMonthCells(date, weekStartOn);
        calendarAdapter.setItems(monthCells);
    }

    private void observeData() {

        accountViewModel.getSelectedAccount().observe(getViewLifecycleOwner(), account -> {
            if (account == null) {
                return;
            }

            currencySymbol = account.currencySymbol;

            // Force reload for the newly selected account
            loadedStart = -1;
            loadedEnd = -1;

            loadCalendarData();
        });

        calendarViewModel.getCalendarSummary().observe(getViewLifecycleOwner(), this::updateCalendar);

        calendarViewModel.getCalendarHeader().observe(getViewLifecycleOwner(), header -> {
            if (header == null)
                return;

            setupSummaryCard(incomeCard, R.drawable.ic_income, getString(R.string.income), header.income, R.color.income, R.drawable.bg_circle_income);
            setupSummaryCard(expenseCard, R.drawable.ic_expense, getString(R.string.expense), header.expense, R.color.expense, R.drawable.bg_circle_expense);
            setupSummaryCard(totalCard, R.drawable.ic_equal, getString(R.string.total), header.total, R.color.dark_grey, R.drawable.bg_circle_equal);
        });
    }

    private void loadCalendarData() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // First day of the selected month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long start = calendar.getTimeInMillis();

        // Last day of the selected month
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);

        long end = calendar.getTimeInMillis();

        if (start == loadedStart && end == loadedEnd) {
            return;
        }

        loadedStart = start;
        loadedEnd = end;

        calendarViewModel.loadCalendar((int) PreferenceManager.INSTANCE.getAccountId(), start, end);
    }

    private void updateCalendar(List<CalendarSummaryModel> summaries) {

        if (summaries == null) {
            summaries = Collections.emptyList();
        }

        List<CalendarDayModel> cells = monthCells;

        Map<Long, CalendarSummaryModel> map = new HashMap<>();

        for (CalendarSummaryModel summary : summaries) {
            map.put(CalendarHelper.getStartOfDay(summary.dayTimestamp), summary);
        }

        for (CalendarDayModel cell : cells) {

            CalendarSummaryModel summary = map.get(CalendarHelper.getStartOfDay(cell.date.getTime()));

            if (summary != null) {
                cell.hasTransaction = true;
                cell.income = summary.income;
                cell.expense = summary.expense;
                cell.total = summary.total;
            } else {
                cell.hasTransaction = false;
                cell.income = 0;
                cell.expense = 0;
                cell.total = 0;
            }
        }

        calendarAdapter.setItems(monthCells);
    }

    private void setupListeners() {
        try {

            ivPrevious.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.MONTH, -1);
                date = calendar.getTime();

                loadCalendar();      // Update month UI
                loadCalendarData();  // Load transactions
            });

            ivNext.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.MONTH, 1);
                date = calendar.getTime();

                loadCalendar();
                loadCalendarData();
            });

        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void showDayTransactions(CalendarDayModel day) {
        try {

            BottomSheetDialog dialog = new BottomSheetDialog(requireActivity());
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_calendar_transaction_layout, requireActivity().findViewById(android.R.id.content), false);
            AppCompatTextView tvTransactionDate = bottomView.findViewById(R.id.tvTransactionDate);
            AppCompatTextView tvAmount = bottomView.findViewById(R.id.tvAmount);
            RecyclerView rvTransactions = bottomView.findViewById(R.id.rvTransactions);
            AppCompatTextView tvNoTransactions = bottomView.findViewById(R.id.tvNoTransactions);
            AppCompatImageView ivPreviousDay = bottomView.findViewById(R.id.ivPreviousDay);
            AppCompatImageView ivNextDay = bottomView.findViewById(R.id.ivNextDay);
            MaterialButton btnAddTransaction = bottomView.findViewById(R.id.btnAddTransaction);

            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
            tvTransactionDate.setText(sdf.format(day.date));

            rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvTransactions.setHasFixedSize(true);
            rvTransactions.setItemAnimator(null);

            loadSelectedDay(day.date, tvTransactionDate);

            calendarViewModel.getDayTransactions().observe(getViewLifecycleOwner(), transactions -> {

                double total = 0;

                if (transactions == null || transactions.isEmpty()) {
                    rvTransactions.setVisibility(View.GONE);
                    tvNoTransactions.setVisibility(View.VISIBLE);
                    updateRecyclerViewHeight(rvTransactions, 0);
                } else {

                    for (TransactionWithDetails item : transactions) {
                        switch (item.transaction.type) {
                            case 1: // Income
                                total += item.transaction.amount * item.exchangeRate;
                                break;

                            case 2: // Expense
                                total -= item.transaction.amount * item.exchangeRate;
                                break;

                            case 3: // Transfer
                                // Ignore transfers in net total
                                break;
                        }
                    }
                    tvNoTransactions.setVisibility(View.GONE);
                    rvTransactions.setVisibility(View.VISIBLE);
                    rvTransactions.setAdapter(new TransactionAdapter(requireContext(), transactions, R.layout.item_transaction_period_detail,
                            true, "calendar", item -> ((BaseActivity) requireActivity()).showTransactionActions(item,
                            // View Details
                            () -> {
                                startActivity(new Intent(requireActivity(), TransactionDetailActivity.class)
                                        .putExtra("transactionId", item.transaction.tempTransactionServerId));
                                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
                            },

                            // Edit
                            () -> {
                                startActivity(new Intent(requireActivity(), CreateTransactionActivity.class)
                                        .putExtra("transactionId", item.transaction.tempTransactionServerId)
                                        .putExtra("type", item.transaction.type)
                                        .putExtra("action", "edit"));
                                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
                            },

                            // Duplicate
                            () -> showDuplicateDialog(item),

                            // Delete
                            () -> showDeleteDialog(item))));
                    updateRecyclerViewHeight(rvTransactions, transactions.size());
                }

                tvAmount.setText(CommonUtils.getBeautifyAmount(currencySymbol, total));
            });

            ivPreviousDay.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(selectedDate);
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                loadSelectedDay(calendar.getTime(), tvTransactionDate);
            });

            ivNextDay.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(selectedDate);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                loadSelectedDay(calendar.getTime(), tvTransactionDate);
            });

            btnAddTransaction.setOnClickListener(view -> {
                startActivity(new Intent(requireContext(), CreateTransactionActivity.class)
                        .putExtra("action", "add")
                        .putExtra("type", TransactionEntity.TYPE_EXPENSE)
                        .putExtra("transactionDate", CalendarHelper.getStartOfDay(selectedDate.getTime())));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showDayTransactions", e);
        }
    }

    private void loadSelectedDay(Date date, AppCompatTextView tvTransactionDate) {

        selectedDate = date;
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
        tvTransactionDate.setText(sdf.format(date));

        long start = CalendarHelper.getStartOfDay(date.getTime());
        long end = CalendarHelper.getEndOfDay(date.getTime());

        calendarViewModel.loadDayTransactions((int) PreferenceManager.INSTANCE.getAccountId(), start, end);
    }

    private void updateRecyclerViewHeight(RecyclerView recyclerView, int itemCount) {

        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();

        if (itemCount >= 7) {
            // Approximate height of one transaction item (adjust if needed)
            int itemHeight = CommonUtils.dpToPx(requireContext(), 72);

            params.height = itemHeight * 7;
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        recyclerView.setLayoutParams(params);
    }

    private void setupSummaryCard(View card, @DrawableRes int icon, String title, double amount, @ColorRes int colorRes, @DrawableRes int drawableRes) {

        AppCompatImageView imgIcon = card.findViewById(R.id.imgIcon);
        AppCompatTextView tvTitle = card.findViewById(R.id.tvTitle);
        AutofitTextView tvAmount = card.findViewById(R.id.tvAmount);
        View indicator = card.findViewById(R.id.colorIndicator);

        int color = ContextCompat.getColor(requireActivity(), colorRes);

        imgIcon.setImageResource(icon);
        imgIcon.setColorFilter(color);
        imgIcon.setBackgroundDrawable(ContextCompat.getDrawable(requireActivity(), drawableRes));

        tvTitle.setText(title);
        tvTitle.setTextColor(color);

        tvAmount.setText(CommonUtils.getBeautifyAmount(currencySymbol, amount));
        tvAmount.setTextColor(color);

        indicator.setBackgroundColor(color);
    }

    private void showDuplicateDialog(TransactionWithDetails item) {

        AlertDialog dialog = new AlertDialog.Builder(requireActivity()).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        MaterialCardView cardHeader = view.findViewById(R.id.cardHeader);
        AppCompatImageView headerImage = view.findViewById(R.id.headerImage);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        MaterialButton tvDuplicate = view.findViewById(R.id.tvDelete);

        tvTitle.setText(R.string.duplicate_transaction);
        tvMessage.setText(R.string.duplicate_transaction_desc);
        tvDuplicate.setText(R.string.duplicate);
        tvDuplicate.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.primary));
        headerImage.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.primary));

        cardHeader.setCardBackgroundColor(requireActivity().getColor(R.color.light_lavender));
        headerImage.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_copy_outline));

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());

        tvDuplicate.setOnClickListener(v -> {
            duplicateTransaction(item);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void duplicateTransaction(TransactionWithDetails item) {
        try {

            if (item == null || item.transaction == null) {
                return;
            }

            TransactionEntity source = item.transaction;
            long currentTime = System.currentTimeMillis();

            String duplicateTransactionId = "T_" + currentTime;

            // ============================================================
            // INCOME / EXPENSE
            // ============================================================
            if (source.type == TransactionEntity.TYPE_INCOME || source.type == TransactionEntity.TYPE_EXPENSE) {

                WalletEntity wallet = walletViewModel.getWalletByWalletId(source.walletId);
                AccountEntity account = accountViewModel.getAccountDetailById((int) source.accountId);

                TransactionEntity duplicate = new TransactionEntity(source, currentTime);
                duplicate.id = 0;
                duplicate.serverId = 0;
                duplicate.tempTransactionServerId = duplicateTransactionId;
                duplicate.transactionDate = currentTime;
                duplicate.createdAt = currentTime;
                duplicate.updatedAt = currentTime;
                duplicate.isDeleted = false;
                duplicate.isSynced = false;
                duplicate.parentTransactionId = "";
                duplicate.isFee = false;

                double exchangeRate = 1;

                if (source.type == TransactionEntity.TYPE_INCOME) {

                    if (wallet != null) {
                        wallet.amount += duplicate.amount;
                        exchangeRate = wallet.exchangeRate;
                    }

                    if (account != null) {
                        account.balance += duplicate.amount * exchangeRate;
                    }
                } else {

                    if (wallet != null) {
                        wallet.amount -= duplicate.amount;
                        exchangeRate = wallet.exchangeRate;
                    }

                    if (account != null) {
                        account.balance -= duplicate.amount * exchangeRate;
                    }
                }

                transactionViewModel.saveTransaction(duplicate, wallet, account);

                // ============================================================
                // TRANSFER
                // ============================================================
            } else if (source.type == TransactionEntity.TYPE_TRANSFER) {

                WalletEntity fromWallet = walletViewModel.getWalletByWalletId(source.fromWalletId);
                WalletEntity toWallet = walletViewModel.getWalletByWalletId(source.walletId);
                AccountEntity account = accountViewModel.getAccountDetailById((int) source.accountId);

                TransactionEntity duplicate = new TransactionEntity(source, currentTime);
                duplicate.id = 0;
                duplicate.serverId = 0;
                duplicate.tempTransactionServerId = duplicateTransactionId;
                duplicate.transactionDate = currentTime;
                duplicate.createdAt = currentTime;
                duplicate.updatedAt = currentTime;
                duplicate.isDeleted = false;
                duplicate.isSynced = false;
                duplicate.parentTransactionId = "";
                duplicate.isFee = false;

                // ========================================================
                // DUPLICATE TRANSFER BALANCE EFFECT
                // ========================================================
                if (fromWallet != null) {
                    fromWallet.amount -= duplicate.amount;
                }

                if (toWallet != null) {
                    toWallet.amount += duplicate.convertedAmount;
                }

                // ========================================================
                // ACCOUNT BALANCE EFFECT
                // ========================================================
                if (account != null && fromWallet != null && toWallet != null) {
                    if (!fromWallet.isExclude && toWallet.isExclude) {
                        account.balance -= duplicate.accountAmount;
                    } else if (fromWallet.isExclude && !toWallet.isExclude) {
                        account.balance += duplicate.accountAmount;
                    }
                }

                // ========================================================
                // DUPLICATE FEE
                // ========================================================
                TransactionEntity sourceFee = transactionViewModel.getFeeTransaction(source.tempTransactionServerId);

                TransactionEntity duplicateFee = null;

                if (sourceFee != null) {
                    long feeTime = currentTime + 1;
                    duplicateFee = new TransactionEntity(sourceFee, feeTime);
                    duplicateFee.id = 0;
                    duplicateFee.serverId = 0;
                    duplicateFee.tempTransactionServerId = "T_FEE_" + feeTime;
                    duplicateFee.parentTransactionId = duplicateTransactionId;
                    duplicateFee.isFee = true;
                    duplicateFee.isDeleted = false;
                    duplicateFee.isSynced = false;
                    duplicateFee.transactionDate = currentTime;
                    duplicateFee.createdAt = currentTime;
                    duplicateFee.updatedAt = currentTime;

                    if (fromWallet != null) {
                        fromWallet.amount -= duplicateFee.amount;
                    }

                    if (account != null && fromWallet != null && !fromWallet.isExclude) {
                        account.balance -= duplicateFee.accountAmount;
                    }
                }

                transactionViewModel.saveTransferTransaction(duplicate, duplicateFee, fromWallet, toWallet, account);
            }

            // ============================================================
            // COPY ATTACHMENTS
            // ============================================================

            duplicateTransactionAttachments(source.tempTransactionServerId, duplicateTransactionId);
        } catch (Exception e) {
            AppLogger.e(getClass(), "duplicateTransaction", e);
        }
    }

    private void duplicateTransactionAttachments(String sourceTransactionId, String duplicateTransactionId) {
        try {
            List<TransactionAttachmentEntity> sourceAttachments = transactionViewModel.getTransactionAttachments(sourceTransactionId);

            if (sourceAttachments == null || sourceAttachments.isEmpty()) {
                return;
            }

            List<TransactionAttachmentEntity> duplicateAttachments = new ArrayList<>();

            long currentTime = System.currentTimeMillis();
            for (TransactionAttachmentEntity sourceAttachment : sourceAttachments) {
                if (TextUtils.isEmpty(sourceAttachment.attachmentPath)) {
                    continue;
                }

                File sourceFile = new File(sourceAttachment.attachmentPath);
                if (!sourceFile.exists() || !sourceFile.isFile()) {
                    continue;
                }

                // --------------------------------------------------------
                // Create new physical file
                // --------------------------------------------------------

                String extension = sourceAttachment.attachmentExtension;
                String fileName = "ATT_" + UUID.randomUUID();
                if (!TextUtils.isEmpty(extension)) {
                    fileName += "." + extension;
                }

                File attachmentDirectory = new File(requireActivity().getFilesDir(), "uploads" + File.separator + duplicateTransactionId);
                if (!attachmentDirectory.exists() && !attachmentDirectory.mkdirs()) {
                    AppLogger.d(getClass(), "Unable to create attachment directory");
                    continue;
                }

                File destinationFile = getDestinationFile(attachmentDirectory, fileName, sourceFile);

                // --------------------------------------------------------
                // Create new DB attachment
                // --------------------------------------------------------

                TransactionAttachmentEntity duplicateAttachment = new TransactionAttachmentEntity();
                duplicateAttachment.tempTransactionServerId = duplicateTransactionId;
                duplicateAttachment.serverId = 0;
                duplicateAttachment.attachmentPath = destinationFile.getAbsolutePath();
                duplicateAttachment.attachmentName = sourceAttachment.attachmentName;
                duplicateAttachment.attachmentExtension = sourceAttachment.attachmentExtension;
                duplicateAttachment.attachmentSize = destinationFile.length();
                duplicateAttachment.createdAt = currentTime;
                duplicateAttachment.updatedAt = currentTime;
                duplicateAttachments.add(duplicateAttachment);
            }

            // ------------------------------------------------------------
            // Save attachment records
            // ------------------------------------------------------------
            if (!duplicateAttachments.isEmpty()) {
                transactionViewModel.saveTransactionAttachment(duplicateAttachments);
            }

        } catch (Exception e) {

            AppLogger.e(getClass(), "duplicateTransactionAttachments", e);
        }
    }

    @NonNull
    private static File getDestinationFile(File attachmentDirectory, String fileName, File sourceFile) throws IOException {
        File destinationFile = new File(attachmentDirectory, fileName);

        // --------------------------------------------------------
        // Copy physical file
        // --------------------------------------------------------

        try (InputStream input = new FileInputStream(sourceFile);
             OutputStream output = new FileOutputStream(destinationFile)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
            output.flush();
        }
        return destinationFile;
    }

    private void showDeleteDialog(TransactionWithDetails item) {

        AlertDialog dialog = new AlertDialog.Builder(requireActivity()).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.delete_transaction);
        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            deleteTransaction(item);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteTransaction(TransactionWithDetails item) {
        try {

            TransactionEntity transaction = item.transaction;

            WalletEntity wallet = walletViewModel.getWalletByWalletId(transaction.walletId);
            AccountEntity account = accountViewModel.getAccountDetailById((int) transaction.accountId);

            double exchangeRate = 1;
            switch (transaction.type) {
                case TransactionEntity.TYPE_INCOME:

                    if (wallet != null) {
                        wallet.amount -= transaction.amount;
                        exchangeRate = wallet.exchangeRate;
                    }
                    if (account != null) {
                        account.balance -= (transaction.amount * exchangeRate);
                    }

                    transactionViewModel.deleteTransaction(transaction, wallet, account);
                    break;

                case TransactionEntity.TYPE_EXPENSE:
                    if (wallet != null) {
                        wallet.amount += transaction.amount;
                        exchangeRate = wallet.exchangeRate;
                    }
                    if (account != null) {
                        account.balance += (transaction.amount * exchangeRate);
                    }

                    transactionViewModel.deleteTransaction(transaction, wallet, account);
                    break;

                case TransactionEntity.TYPE_TRANSFER:
                    WalletEntity fromWallet = walletViewModel.getWalletByWalletId(transaction.fromWalletId);
                    WalletEntity toWallet = walletViewModel.getWalletByWalletId(transaction.walletId);

                    // Reverse transfer
                    if (fromWallet != null) {
                        fromWallet.amount += transaction.amount;
                    }

                    if (toWallet != null) {
                        toWallet.amount -= transaction.convertedAmount;
                    }

                    // Reverse account effect for excluded wallets
                    if (account != null && fromWallet != null && toWallet != null) {
                        if (!fromWallet.isExclude && toWallet.isExclude) {
                            account.balance += transaction.accountAmount;
                        } else if (fromWallet.isExclude && !toWallet.isExclude) {
                            account.balance -= transaction.accountAmount;
                        }
                    }

                    // Reverse fee transaction
                    TransactionEntity feeTransaction = transactionViewModel.getFeeTransaction(transaction.tempTransactionServerId);

                    if (feeTransaction != null) {

                        // Restore fee to From Wallet
                        if (fromWallet != null) {
                            fromWallet.amount += feeTransaction.amount;
                        }

                        // Restore exact account amount used by fee
                        if (account != null && fromWallet != null && !fromWallet.isExclude) {
                            account.balance += feeTransaction.accountAmount;
                        }
                    }

                    // --------------------------------
                    // Delete everything together
                    // --------------------------------
                    transactionViewModel.deleteTransferTransaction(transaction, fromWallet, toWallet, account, feeTransaction);
                    break;
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteTransaction", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden && calendarAdapter != null) {
            Date today = CalendarHelper.getInitialDate();
            if (!CalendarHelper.isSameMonth(date, today)) {
                date = today;
                loadCalendar();
                loadCalendarData();
            }
        }
    }
}