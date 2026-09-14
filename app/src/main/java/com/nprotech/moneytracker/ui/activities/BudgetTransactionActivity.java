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
import com.nprotech.moneytracker.db.entites.BudgetEntity;
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
import com.nprotech.moneytracker.viewmodel.BudgetViewModel;
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
public class BudgetTransactionActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private RecyclerView rvTransactions;
    private ConstraintLayout emptyWrapper;
    private BudgetViewModel budgetViewModel;
    private WalletViewModel walletViewModel;
    private AccountViewModel accountViewModel;
    private TransactionViewModel transactionViewModel;
    private DailyTransactionAdapter dailyTransactionAdapter;
    private int budgetId = 0;
    private String accountCurrencySymbol;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_transaction);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            View rootView = findViewById(R.id.rootView);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            rvTransactions = findViewById(R.id.rvTransactions);
            emptyWrapper = findViewById(R.id.emptyWrapper);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                tvTitle.setText(R.string.budget_transactions);

                walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);
                accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
                budgetViewModel = new ViewModelProvider(this).get(BudgetViewModel.class);
                transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

                budgetId = bundle.getInt("budgetId");
                initializeAdapters();
                bindData();
                setupListeners();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finishWithTransitions();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {

            AccountEntity account = accountViewModel.getAccountDetailById(PreferenceManager.INSTANCE.getAccountId());
            if (account != null) {
                accountCurrencySymbol = account.currencySymbol;
                dailyTransactionAdapter.setAccountCurrencySymbol(accountCurrencySymbol);
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finishWithTransitions();
            }

            budgetViewModel.getBudgetDetailById(budgetId).observe(this, budgetWithDetail -> {
                if (budgetWithDetail != null) {
                    loadTransactions(budgetWithDetail.budget);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAdapters() {
        try {
            rvTransactions.setLayoutManager(new LinearLayoutManager(this));
            dailyTransactionAdapter = new DailyTransactionAdapter(this, new ArrayList<>(), accountCurrencySymbol,
                    item -> showTransactionActions(item,
                    // View Details
                    () -> {
                        startActivity(new Intent(BudgetTransactionActivity.this, TransactionDetailActivity.class)
                                .putExtra("transactionId", item.transaction.tempTransactionServerId));
                        ActivityUtils.overrideOpenTransition(BudgetTransactionActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                    },

                    // Edit
                    () -> {
                        startActivity(new Intent(BudgetTransactionActivity.this, CreateTransactionActivity.class)
                                .putExtra("transactionId", item.transaction.tempTransactionServerId)
                                .putExtra("type", item.transaction.type)
                                .putExtra("action", "edit"));
                        ActivityUtils.overrideOpenTransition(BudgetTransactionActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
                    },

                    // Duplicate
                    () -> showDuplicateDialog(item),

                    // Delete
                    () -> showDeleteDialog(item), false));

            rvTransactions.setAdapter(dailyTransactionAdapter);
            rvTransactions.setHasFixedSize(true);
            rvTransactions.setItemAnimator(null);

            rvTransactions.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm == null) return;
                    int last = lm.findLastVisibleItemPosition();
                    if (last >= dailyTransactionAdapter.getItemCount() - 5) {
                        budgetViewModel.loadNextPage();
                    }
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    private void loadTransactions(BudgetEntity budget) {
        try {

            List<Integer> categoryIds = budgetViewModel.getCategoryIdsByBudgetId(budget.id);
            List<Integer> walletIds = budgetViewModel.getWalletIdsByBudgetId(budget.id);

            budgetViewModel.loadAllTransactions(PreferenceManager.INSTANCE.getAccountId(), budget.startDate, budget.endDate, categoryIds, walletIds,
                    budget.isAllCategory, budget.walletCount == -1, false);
        } catch (Exception e) {
            AppLogger.e(getClass(), "loadTransactions", e);
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> finishWithTransitions());

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finishWithTransitions();
                }
            });

            budgetViewModel.getDailyTransactions().observe(this, list -> {
                if(list!=null && !list.isEmpty()) {
                    emptyWrapper.setVisibility(View.GONE);
                    dailyTransactionAdapter.setItems(list);
                    rvTransactions.setVisibility(View.VISIBLE);
                    rvTransactions.post(() -> rvTransactions.scrollToPosition(0));
                } else {
                    dailyTransactionAdapter.setItems(new ArrayList<>());
                    emptyWrapper.setVisibility(View.VISIBLE);
                    rvTransactions.setVisibility(View.GONE);
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

        cardHeader.setCardBackgroundColor(getColor(R.color.light_lavender));
        headerImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_copy_outline));
        headerImage.setImageTintList(ContextCompat.getColorStateList(this, R.color.primary));

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());

        tvDuplicate.setOnClickListener(v -> {
            duplicateTransaction(item);
            Toast.makeText(getApplicationContext(), R.string.transaction_duplicated, Toast.LENGTH_SHORT).show();
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

                // ============================================================
                // INCOME / REFUND
                // ============================================================
                case TransactionEntity.TYPE_INCOME:
                    if (wallet != null) {
                        exchangeRate = wallet.exchangeRate;
                        if (isCreditCardWallet(wallet)) {
                            // Credit Card refund reduced outstanding.
                            // Delete -> restore outstanding.
                            wallet.amount += transaction.amount;
                        } else {
                            // Normal wallet income increased balance.
                            // Delete -> restore previous balance.
                            wallet.amount -= transaction.amount;
                        }
                    }

                    if (account != null) {
                        account.balance -= (transaction.amount * exchangeRate);
                    }
                    transactionViewModel.deleteTransaction(transaction, wallet, account);
                    break;

                // ============================================================
                // EXPENSE
                // ============================================================
                case TransactionEntity.TYPE_EXPENSE:
                    if (wallet != null) {
                        exchangeRate = wallet.exchangeRate;
                        if (isCreditCardWallet(wallet)) {
                            // Credit Card expense increased outstanding.
                            // Delete -> reduce outstanding.
                            wallet.amount -= transaction.amount;
                        } else {
                            // Normal wallet expense reduced balance.
                            // Delete -> restore previous balance.
                            wallet.amount += transaction.amount;
                        }
                    }

                    if (account != null) {
                        account.balance += (transaction.amount * exchangeRate);
                    }
                    transactionViewModel.deleteTransaction(transaction, wallet, account);
                    break;

                // ============================================================
                // TRANSFER
                // ============================================================
                case TransactionEntity.TYPE_TRANSFER:

                    WalletEntity fromWallet = walletViewModel.getWalletByWalletId(transaction.fromWalletId);
                    WalletEntity toWallet = walletViewModel.getWalletByWalletId(transaction.walletId);

                    // --------------------------------------------------------
                    // Reverse FROM wallet
                    // --------------------------------------------------------
                    if (fromWallet != null) {
                        if (isCreditCardWallet(fromWallet)) {
                            // Original CC -> another wallet:
                            // outstanding increased.
                            // Delete -> decrease outstanding.
                            fromWallet.amount -= transaction.amount;
                        } else {
                            // Original normal wallet:
                            // balance decreased.
                            // Delete -> restore balance.
                            fromWallet.amount += transaction.amount;
                        }
                    }

                    // --------------------------------------------------------
                    // Reverse TO wallet
                    // --------------------------------------------------------
                    if (toWallet != null) {
                        if (isCreditCardWallet(toWallet)) {
                            // Original payment to CC:
                            // outstanding decreased.
                            // Delete -> restore outstanding.
                            toWallet.amount += transaction.convertedAmount;
                        } else {
                            // Original normal wallet:
                            // balance increased.
                            // Delete -> restore previous balance.
                            toWallet.amount -= transaction.convertedAmount;
                        }
                    }

                    // --------------------------------------------------------
                    // Reverse account balance effect
                    // --------------------------------------------------------
                    if (account != null && fromWallet != null && toWallet != null) {
                        if (!fromWallet.isExclude && toWallet.isExclude) {
                            account.balance += transaction.accountAmount;
                        } else if (fromWallet.isExclude && !toWallet.isExclude) {
                            account.balance -= transaction.accountAmount;
                        }
                    }

                    // --------------------------------------------------------
                    // Reverse transfer fee
                    // --------------------------------------------------------
                    TransactionEntity feeTransaction = transactionViewModel.getFeeTransaction(transaction.tempTransactionServerId);

                    if (feeTransaction != null) {
                        if (fromWallet != null) {
                            if (isCreditCardWallet(fromWallet)) {
                                // Original CC transfer fee increased
                                // outstanding.
                                // Delete -> decrease outstanding.
                                fromWallet.amount -= feeTransaction.amount;
                            } else {
                                // Original normal wallet fee decreased
                                // balance.
                                // Delete -> restore balance.
                                fromWallet.amount += feeTransaction.amount;
                            }
                        }

                        // Restore exact account amount used by fee
                        if (account != null && fromWallet != null && !fromWallet.isExclude) {
                            account.balance += feeTransaction.accountAmount;
                        }
                    }

                    // --------------------------------------------------------
                    // Delete transfer + fee together
                    // --------------------------------------------------------
                    transactionViewModel.deleteTransferTransaction(transaction, fromWallet, toWallet, account, feeTransaction);
                    break;
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteTransaction", e);
        }
    }

    private boolean isCreditCardWallet(WalletEntity wallet) {
        return wallet != null && wallet.walletType == 3;
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
    }
}