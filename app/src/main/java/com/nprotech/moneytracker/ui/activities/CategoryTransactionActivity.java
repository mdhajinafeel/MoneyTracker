package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.TransactionAttachmentEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.TransactionWithDetails;
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
import java.util.Objects;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CategoryTransactionActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private RecyclerView rvTransactions;
    private ConstraintLayout emptyWrapper;
    private WalletViewModel walletViewModel;
    private AccountViewModel accountViewModel;
    private TransactionViewModel transactionViewModel;
    private int categoryId = 0, walletId = 0;
    private DailyTransactionAdapter dailyTransactionAdapter;
    private String accountCurrencySymbol;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_transaction);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            View root = findViewById(R.id.rootView);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            rvTransactions = findViewById(R.id.rvTransactions);
            emptyWrapper = findViewById(R.id.emptyWrapper);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {

                if (!Objects.equals(bundle.getString("categoryName"), "")) {
                    tvTitle.setText(bundle.getString("categoryName"));
                } else {
                    tvTitle.setText(R.string.wallet_transactions);
                }

                walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);
                accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
                transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

                categoryId = bundle.getInt("categoryId");
                walletId = bundle.getInt("walletId");

                initializeAdapters();
                bindData();
                setupListeners();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {

            AccountEntity account = accountViewModel.getAccountDetailById((int) PreferenceManager.INSTANCE.getAccountId());
            if (account != null) {
                accountCurrencySymbol = account.currencySymbol;
                dailyTransactionAdapter.setAccountCurrencySymbol(accountCurrencySymbol);
                walletViewModel.loadTransactions(account.id, walletId, categoryId);
            }

            walletViewModel.getCategoryTransactions().observe(this, dailyTransModels -> {

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
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAdapters() {
        try {
            dailyTransactionAdapter = new DailyTransactionAdapter(this, new ArrayList<>(), accountCurrencySymbol,
                    item -> showTransactionActions(item,
                            // View Details
                            () -> {
                                startActivity(new Intent(CategoryTransactionActivity.this, TransactionDetailActivity.class)
                                        .putExtra("transactionId", item.transaction.tempTransactionServerId));
                                ActivityUtils.overrideOpenTransition(CategoryTransactionActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                            },

                            // Edit
                            () -> {
                                startActivity(new Intent(CategoryTransactionActivity.this, CreateTransactionActivity.class)
                                        .putExtra("transactionId", item.transaction.tempTransactionServerId)
                                        .putExtra("type", item.transaction.type)
                                        .putExtra("action", "edit"));
                                ActivityUtils.overrideOpenTransition(CategoryTransactionActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                            },

                            // Duplicate
                            () -> showDuplicateDialog(item),

                            // Delete
                            () -> showDeleteDialog(item)));
            rvTransactions.setAdapter(dailyTransactionAdapter);
            rvTransactions.setLayoutManager(new LinearLayoutManager(this));
            rvTransactions.setNestedScrollingEnabled(false);
            rvTransactions.setItemAnimator(null);
            rvTransactions.setHasFixedSize(true);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            });

            getOnBackPressedDispatcher().addCallback(this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            finish();
                            ActivityUtils.overrideCloseTransition(CategoryTransactionActivity.this, R.anim.scale_in, R.anim.right_to_left);
                        }
                    });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void showDuplicateDialog(TransactionWithDetails item) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        MaterialCardView cardHeader = view.findViewById(R.id.cardHeader);
        AppCompatImageView headerImage = view.findViewById(R.id.headerImage);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        MaterialButton tvDuplicate = view.findViewById(R.id.tvDelete);

        tvTitle.setText(R.string.duplicate_transaction);
        tvMessage.setText(R.string.duplicate_transaction_desc);
        tvDuplicate.setText(R.string.duplicate);
        tvDuplicate.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary));
        headerImage.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary));

        cardHeader.setCardBackgroundColor(getColor(R.color.light_lavender));
        headerImage.setImageDrawable(getDrawable(R.drawable.ic_copy_outline));

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

                File attachmentDirectory = new File(getFilesDir(), "uploads" + File.separator + duplicateTransactionId);
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

        AlertDialog dialog = new AlertDialog.Builder(this).create();
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
}