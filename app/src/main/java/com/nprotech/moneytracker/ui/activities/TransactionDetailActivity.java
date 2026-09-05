package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.TransactionAttachmentEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.models.TransactionWithDetails;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.TransactionViewModel;
import com.nprotech.moneytracker.viewmodel.WalletViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionDetailActivity extends BaseActivity {

    private MaterialCardView cardTransactionSummary, cardTransactionIcon, cardTransactionAttachments;
    private AppCompatImageView ivTransactionIcon, ivType, ivTransferArrow;
    private View decorCircleLarge, decorCircleSmall;
    private AppCompatTextView tvTransactionStatus, tvTransactionAmount, tvReceivedAmount, tvFromWalletSummary, tvToWalletSummary, tvTransactionDescription,
            tvCategory, tvAmount, tvReceived, tvFee, tvDateTime, tvWallet, tvType, tvFromWallet, tvToWallet, tvDesc, tvNote, tvAttachmentTitle, lblAmount, lblFee;
    private LinearLayout layoutReceived, layoutFee, layoutWallet, layoutFromWallet, layoutToWallet, layoutDescription, layoutNotes;
    private AppCompatImageView icBack, ivDelete, ivEdit;
    private RecyclerView rvAttachments;
    private RecyclerViewAdapter<TransactionAttachmentEntity> attachmentAdapter;
    private TransactionWithDetails transactionWithDetails;
    private ActivityResultLauncher<Intent> transactionEditLauncher;
    private TransactionViewModel transactionViewModel;
    private WalletViewModel walletViewModel;
    private AccountViewModel accountViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View rootView = findViewById(R.id.rootView);
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            ivDelete = toolbarWrapper.findViewById(R.id.ivDelete);
            ivEdit = toolbarWrapper.findViewById(R.id.ivEdit);
            cardTransactionSummary = findViewById(R.id.cardTransactionSummary);
            cardTransactionIcon = findViewById(R.id.cardTransactionIcon);
            cardTransactionAttachments = findViewById(R.id.cardTransactionAttachments);
            ivTransactionIcon = findViewById(R.id.ivTransactionIcon);
            decorCircleLarge = findViewById(R.id.decorCircleLarge);
            decorCircleSmall = findViewById(R.id.decorCircleSmall);
            tvTransactionStatus = findViewById(R.id.tvTransactionStatus);
            tvTransactionAmount = findViewById(R.id.tvTransactionAmount);
            tvTransactionDescription = findViewById(R.id.tvTransactionDescription);

            tvCategory = findViewById(R.id.tvCategory);
            tvFee = findViewById(R.id.tvFee);
            tvReceived = findViewById(R.id.tvReceived);
            tvAmount = findViewById(R.id.tvAmount);
            lblAmount = findViewById(R.id.lblAmount);
            tvReceivedAmount = findViewById(R.id.tvReceivedAmount);
            tvFromWalletSummary = findViewById(R.id.tvFromWalletSummary);
            tvToWalletSummary = findViewById(R.id.tvToWalletSummary);
            lblFee = findViewById(R.id.lblFee);
            tvDateTime = findViewById(R.id.tvDateTime);
            tvWallet = findViewById(R.id.tvWallet);
            tvType = findViewById(R.id.tvType);
            ivType = findViewById(R.id.ivType);
            ivTransferArrow = findViewById(R.id.ivTransferArrow);
            tvFromWallet = findViewById(R.id.tvFromWallet);
            tvToWallet = findViewById(R.id.tvToWallet);
            tvDesc = findViewById(R.id.tvDesc);
            tvNote = findViewById(R.id.tvNote);
            tvAttachmentTitle = findViewById(R.id.tvAttachmentTitle);
            layoutReceived = findViewById(R.id.layoutReceived);
            layoutFee = findViewById(R.id.layoutFee);
            layoutWallet = findViewById(R.id.layoutWallet);
            layoutFromWallet = findViewById(R.id.layoutFromWallet);
            layoutToWallet = findViewById(R.id.layoutToWallet);
            layoutDescription = findViewById(R.id.layoutDescription);
            layoutNotes = findViewById(R.id.layoutNotes);
            rvAttachments = findViewById(R.id.rvAttachments);

            ivEdit.setVisibility(View.VISIBLE);
            ivDelete.setVisibility(View.VISIBLE);

            tvTitle.setText(R.string.detail);

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

                transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
                walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);
                accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

                String transactionId = bundle.getString("transactionId", "");
                transactionWithDetails = transactionViewModel.getTransactions(transactionId);

                initializeAttachmentAdapter();
                bindData(transactionWithDetails);
                setupListeners();
                setupLauncher();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData(TransactionWithDetails transactionWithDetail) {
        try {

            TransactionEntity transaction = transactionWithDetail.transaction;
            List<TransactionAttachmentEntity> attachments = transactionViewModel.getTransactionAttachments(transaction.tempTransactionServerId);

            if (transaction.type == TransactionEntity.TYPE_INCOME) {
                cardTransactionSummary.setCardBackgroundColor(getColor(R.color.color_income_card));
                cardTransactionSummary.setStrokeColor(getColor(R.color.income));
                cardTransactionIcon.setCardBackgroundColor(Color.parseColor(transactionWithDetail.color));

                decorCircleLarge.setBackgroundTintList(getColorStateList(R.color.color_income_circle));
                decorCircleSmall.setBackgroundTintList(getColorStateList(R.color.color_income_circle));

                tvTransactionStatus.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_income));
                tvTransactionStatus.setText(getString(R.string.income));
                tvTransactionStatus.setTextColor(getColor(R.color.income));
                tvType.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_income));
                tvType.setText(getString(R.string.income));
                tvType.setTextColor(getColor(R.color.income));
                ivType.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_income));

                tvWallet.setText(getString(R.string.wallet_currency, transactionWithDetail.walletName, transactionWithDetail.currencySymbol));
                tvFromWalletSummary.setText(transactionWithDetail.walletName);

                tvAmount.setText(CommonUtils.getBeautifyAmount(transactionWithDetail.currencySymbol, transaction.amount));
                tvTransactionAmount.setText(CommonUtils.getBeautifyAmount(transactionWithDetail.currencySymbol, transaction.amount));

                lblAmount.setText(getString(R.string.amount));
                lblFee.setText(getString(R.string.fee));

                tvFromWalletSummary.setVisibility(View.VISIBLE);
                layoutReceived.setVisibility(View.GONE);
                layoutWallet.setVisibility(View.VISIBLE);
                layoutFromWallet.setVisibility(View.GONE);
                layoutToWallet.setVisibility(View.GONE);
                layoutFee.setVisibility(View.GONE);
            } else if (transaction.type == TransactionEntity.TYPE_EXPENSE) {
                cardTransactionSummary.setCardBackgroundColor(getColor(R.color.color_expense_card));
                cardTransactionSummary.setStrokeColor(getColor(R.color.expense));
                cardTransactionIcon.setCardBackgroundColor(Color.parseColor(transactionWithDetail.color));

                decorCircleLarge.setBackgroundTintList(getColorStateList(R.color.color_expense_circle));
                decorCircleSmall.setBackgroundTintList(getColorStateList(R.color.color_expense_circle));

                tvTransactionStatus.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_expense));
                tvTransactionStatus.setText(getString(R.string.expense));
                tvTransactionStatus.setTextColor(getColor(R.color.expense));
                tvType.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_expense));
                tvType.setText(getString(R.string.expense));
                tvType.setTextColor(getColor(R.color.expense));
                ivType.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_expense));

                tvWallet.setText(getString(R.string.wallet_currency, transactionWithDetail.walletName, transactionWithDetail.currencySymbol));
                tvFromWalletSummary.setText(transactionWithDetail.walletName);

                tvAmount.setText(CommonUtils.getBeautifyAmount(transactionWithDetail.currencySymbol, transaction.amount));
                tvTransactionAmount.setText(CommonUtils.getBeautifyAmount(transactionWithDetail.currencySymbol, transaction.amount));

                lblAmount.setText(getString(R.string.amount));
                lblFee.setText(getString(R.string.fee));

                tvFromWalletSummary.setVisibility(View.VISIBLE);
                layoutReceived.setVisibility(View.GONE);
                layoutWallet.setVisibility(View.VISIBLE);
                layoutFromWallet.setVisibility(View.GONE);
                layoutToWallet.setVisibility(View.GONE);
                layoutFee.setVisibility(View.GONE);
            } else if (transaction.type == TransactionEntity.TYPE_TRANSFER) {
                cardTransactionSummary.setCardBackgroundColor(getColor(R.color.color_transfer_card));
                cardTransactionSummary.setStrokeColor(getColor(R.color.transfer));
                cardTransactionIcon.setCardBackgroundColor(getColor(R.color.transfer));

                decorCircleLarge.setBackgroundTintList(getColorStateList(R.color.color_transfer_circle));
                decorCircleSmall.setBackgroundTintList(getColorStateList(R.color.color_transfer_circle));

                tvTransactionStatus.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_transfer));
                tvTransactionStatus.setText(getString(R.string.transfer));
                tvTransactionStatus.setTextColor(getColor(R.color.transfer));

                tvType.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_transfer));
                tvType.setText(getString(R.string.transfer));
                tvType.setTextColor(getColor(R.color.transfer));
                ivType.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_transfer_trans));

                // Get both wallets
                WalletEntity fromWallet = walletViewModel.getWalletByWalletId(transaction.fromWalletId);
                WalletEntity toWallet = walletViewModel.getWalletByWalletId(transaction.walletId);

                String fromCurrencySymbol = "", fromCurrencyCode = "";
                String toCurrencySymbol = "", toCurrencyCode = "";

                if (fromWallet != null) {
                    fromCurrencySymbol = fromWallet.currencySymbol;
                    fromCurrencyCode = fromWallet.currencyCode;
                }

                if (toWallet != null) {
                    toCurrencySymbol = toWallet.currencySymbol;
                    toCurrencyCode = toWallet.currencyCode;
                }

                ivTransferArrow.setVisibility(View.VISIBLE);
                tvReceivedAmount.setVisibility(View.VISIBLE);
                tvFromWalletSummary.setVisibility(View.VISIBLE);
                tvToWalletSummary.setVisibility(View.VISIBLE);

                tvReceivedAmount.setText(CommonUtils.getBeautifyAmount(toCurrencySymbol, transaction.convertedAmount));
                tvToWalletSummary.setText(transactionWithDetail.walletName);
                tvFromWalletSummary.setText(transactionWithDetail.fromWalletName);

                lblAmount.setText(getString(R.string.amount_from_wallet));
                lblFee.setText(getString(R.string.fee_from_wallet));

                tvReceived.setText(CommonUtils.getBeautifyAmount(toCurrencySymbol, transaction.convertedAmount));
                tvFromWallet.setText(getString(R.string.wallet_currency, transactionWithDetail.fromWalletName, fromCurrencyCode));
                tvToWallet.setText(getString(R.string.wallet_currency, transactionWithDetail.walletName, toCurrencyCode));
                tvAmount.setText(CommonUtils.getBeautifyAmount(fromCurrencySymbol, transaction.amount));
                tvTransactionAmount.setText(CommonUtils.getBeautifyAmount(fromCurrencySymbol, transaction.amount));
                tvFee.setText(CommonUtils.getBeautifyAmount(fromCurrencySymbol, transaction.fee));

                layoutReceived.setVisibility(View.VISIBLE);
                layoutWallet.setVisibility(View.GONE);
                layoutFromWallet.setVisibility(View.VISIBLE);
                layoutToWallet.setVisibility(View.VISIBLE);
                layoutFee.setVisibility(View.VISIBLE);
            }

            if (transaction.type == TransactionEntity.TYPE_TRANSFER) {
                ivTransactionIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_transfer));
            } else {
                if (transactionWithDetail.icon == null || transactionWithDetail.icon == 0) {
                    ivTransactionIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.category_0));
                } else {
                    ivTransactionIcon.setImageDrawable(ContextCompat.getDrawable(this, DataHelper.getCategoryIcons().get(transactionWithDetail.icon)));
                }
            }

            if (transaction.description != null && !transaction.description.isEmpty()) {
                tvTransactionDescription.setText(transaction.description);
                tvTransactionDescription.setVisibility(View.VISIBLE);
            } else {
                tvTransactionDescription.setVisibility(View.GONE);
            }

            // DETAIL

            String categoryName = transaction.getCategoryName(this);
            if (Objects.equals(categoryName, "")) {
                categoryName = transactionWithDetail.categoryName;
            }

            tvCategory.setText(categoryName);
            tvDateTime.setText(DateHelper.getFormattedDateTime(this, transaction.transactionDate));

            boolean hasDescription = transaction.description != null && !transaction.description.trim().isEmpty();
            boolean hasNote = transaction.memo != null && !transaction.memo.trim().isEmpty();

            if (hasDescription) {
                tvDesc.setText(transaction.description);
                layoutDescription.setVisibility(View.VISIBLE);
            } else {
                layoutDescription.setVisibility(View.GONE);
            }

            if (hasNote) {
                tvNote.setText(transaction.memo);
                layoutNotes.setVisibility(View.VISIBLE);
            } else {
                layoutNotes.setVisibility(View.GONE);
            }

            if (attachments != null && !attachments.isEmpty()) {
                int attachmentCount = attachments.size();
                tvAttachmentTitle.setText(getResources().getQuantityString(R.plurals.attachments_count, attachmentCount, attachmentCount));
                attachmentAdapter.setItems(attachments);
                cardTransactionAttachments.setVisibility(View.VISIBLE);
            } else {
                cardTransactionAttachments.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAttachmentAdapter() {
        try {
            attachmentAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(), R.layout.item_transaction_attachment) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, TransactionAttachmentEntity attachment) {
                    AppCompatImageView ivAttachmentPreview = holder.getView(R.id.ivAttachmentPreview);
                    AppCompatImageView ivAttachmentFileType = holder.getView(R.id.ivAttachmentFileType);
                    AppCompatTextView tvAttachmentName = holder.getView(R.id.tvAttachmentName);
                    AppCompatTextView tvAttachmentSize = holder.getView(R.id.tvAttachmentSize);
                    View cardDelete = holder.getView(R.id.cardDelete);
                    View addMoreContainer = holder.getView(R.id.addMoreContainer);
                    addMoreContainer.setVisibility(View.GONE);
                    cardDelete.setVisibility(View.GONE);

                    String fileName = attachment.attachmentName;
                    if (fileName == null || fileName.trim().isEmpty()) {
                        fileName = "Attachment";
                    }
                    tvAttachmentName.setText(fileName);
                    tvAttachmentName.setSelected(true);

                    tvAttachmentSize.setText(Formatter.formatFileSize(TransactionDetailActivity.this, attachment.attachmentSize));

                    File file = new File(attachment.attachmentPath);
                    Uri uri = Uri.fromFile(file);
                    String extension = attachment.attachmentExtension;
                    if (extension == null) {
                        extension = "";
                    }
                    extension = extension.toLowerCase(Locale.ROOT);

                    boolean isImage = extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png")
                            || extension.equals("webp") || extension.equals("heic");

                    if (isImage && file.exists()) {
                        ivAttachmentPreview.setVisibility(View.VISIBLE);
                        ivAttachmentFileType.setVisibility(View.GONE);
                        Glide.with(TransactionDetailActivity.this).load(uri).centerCrop().into(ivAttachmentPreview);
                    } else {
                        ivAttachmentPreview.setVisibility(View.GONE);
                        ivAttachmentFileType.setVisibility(View.VISIBLE);
                        ivAttachmentFileType.setImageResource(getFileIcon(attachment.attachmentName));
                    }

                    holder.getView(R.id.cardAttachmentPreview).setOnClickListener(v -> openAttachment(attachment));
                }
            };

            rvAttachments.setAdapter(attachmentAdapter);
            rvAttachments.setLayoutManager(new GridLayoutManager(this, 3));
            rvAttachments.setHasFixedSize(false);
            rvAttachments.setItemAnimator(null);
            rvAttachments.setNestedScrollingEnabled(false);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAttachmentAdapter", e);
        }
    }

    private int getFileIcon(String fileName) {

        if (fileName == null) {
            return R.drawable.ic_file_generic;
        }

        String name = fileName.toLowerCase(Locale.ROOT);

        if (name.endsWith(".pdf")) {
            return R.drawable.ic_file_pdf;
        }

        if (name.endsWith(".doc") || name.endsWith(".docx")) {
            return R.drawable.ic_file_doc;
        }

        if (name.endsWith(".ppt") || name.endsWith(".pptx")) {
            return R.drawable.ic_file_ppt;
        }

        if (name.endsWith(".xls") || name.endsWith(".xlsx") || name.endsWith(".csv")) {
            return R.drawable.ic_file_excel;
        }

        if (name.endsWith(".zip")) {
            return R.drawable.ic_file_zip;
        }

        if (name.endsWith(".rar")) {
            return R.drawable.ic_file_rar;
        }

        if (name.endsWith(".xml")) {
            return R.drawable.ic_file_xml;
        }

        return R.drawable.ic_file_generic;
    }

    private void openAttachment(TransactionAttachmentEntity attachment) {
        try {
            File file = new File(attachment.attachmentPath);

            if (!file.exists()) {
                Toast.makeText(this, "Attachment not found", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            String extension = MimeTypeMap.getFileExtensionFromUrl(attachment.attachmentName);

            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase(Locale.ROOT));

            if (mimeType == null) {
                mimeType = "*/*";
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(intent);

        } catch (Exception e) {
            AppLogger.e(getClass(), "openAttachment", e);
            Toast.makeText(this, getString(R.string.unable_to_open_attachment), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            });

            getOnBackPressedDispatcher().addCallback(
                    this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            finish();
                            ActivityUtils.overrideCloseTransition(TransactionDetailActivity.this, R.anim.scale_in, R.anim.right_to_left);
                        }
                    });

            ivEdit.setOnClickListener(view -> {
                Intent intent = new Intent(this, CreateTransactionActivity.class);
                intent.putExtra("transactionId", transactionWithDetails.transaction.tempTransactionServerId);
                intent.putExtra("type", transactionWithDetails.transaction.type);
                intent.putExtra("action", "edit");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.top_to_bottom, R.anim.scale_out);
                transactionEditLauncher.launch(intent, options);
            });

            ivDelete.setOnClickListener(view -> showDeleteDialog());

            transactionViewModel.getDeleteStatus().observe(this, success -> {
                if (Boolean.TRUE.equals(success)) {
                    Intent intent = new Intent();
                    intent.putExtra("isDeleted", true);
                    intent.putExtra("tempTransactionServerId", transactionWithDetails.transaction.tempTransactionServerId);
                    setResult(RESULT_OK, intent);
                    finish();
                    ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
                } else {
                    Toast.makeText(this, getString(R.string.trans_delete_failed), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void setupLauncher() {
        transactionEditLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            if (data.getBooleanExtra("isUpdated", false)) {
                                String tempTransactionServerId = data.getStringExtra("tempTransactionServerId");
                                if (tempTransactionServerId != null) {
                                    transactionWithDetails = transactionViewModel.getTransactions(tempTransactionServerId);
                                    bindData(transactionWithDetails);
                                }
                            }
                        }
                    }
                });
    }

    private void showDeleteDialog() {

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
            deleteTransaction();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteTransaction() {
        try {

            TransactionEntity transaction = transactionWithDetails.transaction;

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