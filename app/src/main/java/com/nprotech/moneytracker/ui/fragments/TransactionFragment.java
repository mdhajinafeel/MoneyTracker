package com.nprotech.moneytracker.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.TransactionAttachmentEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.TransactionWithDetails;
import com.nprotech.moneytracker.ui.activities.CreateTransactionActivity;
import com.nprotech.moneytracker.ui.activities.TransactionDetailActivity;
import com.nprotech.moneytracker.ui.adapters.DailyTransactionAdapter;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.TransactionViewModel;
import com.nprotech.moneytracker.viewmodel.WalletViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionFragment extends Fragment {

    private FloatingActionButton fabAdd;
    private ConstraintLayout emptyWrapper;
    private RecyclerView rvTransactions;
    private TransactionViewModel transactionViewModel;
    private AccountViewModel accountViewModel;
    private WalletViewModel walletViewModel;
    private DailyTransactionAdapter dailyTransactionAdapter;
    private String accountCurrencySymbol = "";
    private ShimmerFrameLayout shimmerLayout;
    private boolean firstLoad = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        try {
            fabAdd = view.findViewById(R.id.fabAdd);
            emptyWrapper = view.findViewById(R.id.emptyWrapper);
            rvTransactions = view.findViewById(R.id.rvTransactions);
            shimmerLayout = view.findViewById(R.id.shimmerLayout);

            transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
            accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
            walletViewModel = new ViewModelProvider(requireActivity()).get(WalletViewModel.class);

            setupListeners();
            initializeAdapters();
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }

        return view;
    }

    private void setupListeners() {
        try {
            // FAB CLICK
            fabAdd.setOnClickListener(v -> {
                v.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(120)
                        .withEndAction(() ->
                                v.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(120)
                                        .start())
                        .start();

                startActivity(new Intent(requireActivity(), CreateTransactionActivity.class)
                        .putExtra("action", "add")
                        .putExtra("type", TransactionEntity.TYPE_EXPENSE));
                ActivityUtils.overrideOpenTransition(requireActivity(), R.anim.top_to_bottom, R.anim.scale_out);
            });

            accountViewModel.getSelectedAccount().observe(getViewLifecycleOwner(), account -> {

                if (account != null) {

                    if (firstLoad) {
                        showLoading();
                    }

                    transactionViewModel.loadTransactions(account.id, 0, 0);
                    accountCurrencySymbol = account.currencySymbol;
                    dailyTransactionAdapter.setAccountCurrencySymbol(accountCurrencySymbol);
                }
            });

            transactionViewModel.getDailyTransactions().observe(getViewLifecycleOwner(), dailyTransModels -> {

                hideLoading();
                firstLoad = false;

                if (dailyTransModels == null || dailyTransModels.isEmpty()) {
                    rvTransactions.setVisibility(View.GONE);
                    emptyWrapper.setVisibility(View.VISIBLE);
                } else {
                    emptyWrapper.setVisibility(View.GONE);
                    rvTransactions.setVisibility(View.VISIBLE);
                    dailyTransactionAdapter.setItems(dailyTransModels);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void initializeAdapters() {
        try {
            rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
            dailyTransactionAdapter = new DailyTransactionAdapter(requireContext(), new ArrayList<>(), accountCurrencySymbol,
                    item -> ((BaseActivity) requireActivity()).showTransactionActions(item,
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
                            () -> showDeleteDialog(item)));

            rvTransactions.setAdapter(dailyTransactionAdapter);
            rvTransactions.setHasFixedSize(true);
            rvTransactions.setItemAnimator(null);

            rvTransactions.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm == null)
                        return;
                    int last = lm.findLastVisibleItemPosition();
                    if (last >= dailyTransactionAdapter.getItemCount() - 5) {
                        transactionViewModel.loadNextPage();
                    }
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
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

    private void showLoading() {
        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();

        rvTransactions.setVisibility(View.GONE);
        emptyWrapper.setVisibility(View.GONE);
    }

    private void hideLoading() {
        shimmerLayout.stopShimmer();
        shimmerLayout.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!firstLoad) {
            transactionViewModel.loadTransactions((int) PreferenceManager.INSTANCE.getAccountId(), 0, 0);
        }
    }
}