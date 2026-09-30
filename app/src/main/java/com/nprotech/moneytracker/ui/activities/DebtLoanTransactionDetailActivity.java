package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.DebtLoanType;
import com.nprotech.moneytracker.db.entites.DebtLoanPaymentTransactionEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanTransactionAttachmentEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.models.DebtLoanTransactionWithDetails;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.DebtLoanViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DebtLoanTransactionDetailActivity extends BaseActivity {

    private AppCompatImageView icBack, ivMore;
    private View decorCircleLarge, decorCircleSmall;
    private AppCompatTextView tvPaidDate, tvWallet, tvPaidAmount, tvPaymentMethod, tvReferenceId, tvNotes, tvAttachmentTitle;
    private RecyclerViewAdapter<DebtLoanTransactionAttachmentEntity> attachmentAdapter;
    private RecyclerView rvAttachments;
    private MaterialCardView cardTransactionAttachments;
    private DebtLoanViewModel debtLoanViewModel;
    private DebtLoanTransactionWithDetails debtLoanTransactionWithDetail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt_loan_transaction_detail);
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
            ivMore = toolbarWrapper.findViewById(R.id.ivMore);

            ivMore.setVisibility(View.VISIBLE);

            tvPaidDate = findViewById(R.id.tvPaidDate);
            tvWallet = findViewById(R.id.tvWallet);
            tvPaidAmount = findViewById(R.id.tvPaidAmount);
            tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
            tvReferenceId = findViewById(R.id.tvReferenceId);
            tvNotes = findViewById(R.id.tvNotes);
            tvAttachmentTitle = findViewById(R.id.tvAttachmentTitle);
            rvAttachments = findViewById(R.id.rvAttachments);
            cardTransactionAttachments = findViewById(R.id.cardTransactionAttachments);

            tvTitle.setText(R.string.payment_details);

            decorCircleLarge = findViewById(R.id.decorCircleLarge);
            decorCircleSmall = findViewById(R.id.decorCircleSmall);

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

                debtLoanViewModel = new ViewModelProvider(this).get(DebtLoanViewModel.class);

                initializeAttachmentAdapter();
                bindData(bundle);
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

    private void bindData(Bundle bundle) {
        try {

            int debtLoanTransactionId = bundle.getInt("debtLoanTransactionId", 0);
            int debtLoanId = bundle.getInt("debtLoanId", 0);
            int debtLoanPaymentId = bundle.getInt("debtLoanPaymentId", 0);

            debtLoanViewModel.getPaymentDetailById(debtLoanTransactionId, debtLoanId, debtLoanPaymentId).observe(this, transactionWithDetails -> {

                if(transactionWithDetails != null && transactionWithDetails.debtLoanPayment != null) {
                    debtLoanTransactionWithDetail = transactionWithDetails;

                    DebtLoanPaymentTransactionEntity debtLoanPayment = transactionWithDetails.debtLoanPayment;

                    decorCircleLarge.setBackgroundTintList(getColorStateList(R.color.color_income_circle));
                    decorCircleSmall.setBackgroundTintList(getColorStateList(R.color.color_income_circle));

                    tvPaidDate.setText(DateHelper.getFormattedDate(debtLoanPayment.paymentDate));
                    tvWallet.setText(transactionWithDetails.walletName);
                    tvPaidAmount.setText(CommonUtils.getBeautifyAmount(debtLoanPayment.currencySymbol, debtLoanPayment.convertedAmount));

                    String paymentMethod = getString(R.string.cash);
                    if (debtLoanPayment.paymentMethod == DebtLoanType.PAYMENT_METHOD_CASH) {
                        paymentMethod = getString(R.string.cash);
                    } else if (debtLoanPayment.paymentMethod == DebtLoanType.PAYMENT_METHOD_UPI) {
                        paymentMethod = getString(R.string.upi);
                    } else if (debtLoanPayment.paymentMethod == DebtLoanType.PAYMENT_METHOD_BANK_TRANSFER) {
                        paymentMethod = getString(R.string.bank_transfer);
                    } else if (debtLoanPayment.paymentMethod == DebtLoanType.PAYMENT_METHOD_CARD) {
                        paymentMethod = getString(R.string.card);
                    } else if (debtLoanPayment.paymentMethod == DebtLoanType.PAYMENT_METHOD_CHEQUE) {
                        paymentMethod = getString(R.string.cheque);
                    } else if (debtLoanPayment.paymentMethod == DebtLoanType.PAYMENT_METHOD_NET_BANKING) {
                        paymentMethod = getString(R.string.net_banking);
                    } else if (debtLoanPayment.paymentMethod == DebtLoanType.PAYMENT_METHOD_DEMAND_DRAFT) {
                        paymentMethod = getString(R.string.demand_draft);
                    } else if (debtLoanPayment.paymentMethod == DebtLoanType.PAYMENT_METHOD_OTHER) {
                        paymentMethod = getString(R.string.others);
                    }
                    tvPaymentMethod.setText(paymentMethod);

                    tvReferenceId.setText(debtLoanPayment.reference);
                    tvNotes.setText(debtLoanPayment.notes);

                    List<DebtLoanTransactionAttachmentEntity> attachments = debtLoanViewModel.getAttachments(debtLoanPayment.debtLoanId, debtLoanPayment.debtLoanPaymentId,
                            debtLoanPayment.id);
                    if (attachments != null && !attachments.isEmpty()) {
                        int attachmentCount = attachments.size();
                        tvAttachmentTitle.setText(getResources().getQuantityString(R.plurals.attachments_count, attachmentCount, attachmentCount));
                        attachmentAdapter.setItems(attachments);
                        cardTransactionAttachments.setVisibility(View.VISIBLE);
                    } else {
                        cardTransactionAttachments.setVisibility(View.GONE);
                    }
                } else {
                    finishWithTransitions();
                }
            });

            debtLoanViewModel.getTransactionDeleted().observe(this, deleted -> {
                if (Boolean.TRUE.equals(deleted)) {
                    Toast.makeText(this, R.string.payment_deleted_successfully, Toast.LENGTH_SHORT).show();
                    finishWithTransitions();
                } else {
                    Toast.makeText(this, getString(R.string.error_update), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAttachmentAdapter() {
        try {

            attachmentAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(), R.layout.item_transaction_attachment) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, DebtLoanTransactionAttachmentEntity attachment) {
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

                    tvAttachmentSize.setText(Formatter.formatFileSize(DebtLoanTransactionDetailActivity.this, attachment.attachmentSize));

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
                        Glide.with(DebtLoanTransactionDetailActivity.this).load(uri).centerCrop().into(ivAttachmentPreview);
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

    private void openAttachment(DebtLoanTransactionAttachmentEntity attachment) {
        try {
            File file = new File(attachment.attachmentPath);

            if (!file.exists()) {
                Toast.makeText(this, R.string.attachment_not_found, Toast.LENGTH_SHORT).show();
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
            icBack.setOnClickListener(view -> finishWithTransitions());

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finishWithTransitions();
                }
            });

            ivMore.setOnClickListener(v -> showOptionDialog(debtLoanTransactionWithDetail));
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
    }

    private void showOptionDialog(DebtLoanTransactionWithDetails debtLoanTransactionWithDetail) {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_payment_transaction_options, findViewById(android.R.id.content), false);
            LinearLayout optionEdit = bottomView.findViewById(R.id.optionEdit);
            LinearLayout optionDelete = bottomView.findViewById(R.id.optionDelete);

            // EDIT
            optionEdit.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(DebtLoanTransactionDetailActivity.this, DebtLoanRecordPaymentActivity.class)
                        .putExtra("isEdit", true)
                        .putExtra("debtLoanId", debtLoanTransactionWithDetail.debtLoanPayment.debtLoanId)
                        .putExtra("debtLoanTransactionId", debtLoanTransactionWithDetail.debtLoanPayment.id));
                ActivityUtils.overrideOpenTransition(DebtLoanTransactionDetailActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            // DELETE
            optionDelete.setOnClickListener(v -> {
                dialog.dismiss();
                showDeleteDialog(debtLoanTransactionWithDetail);
            });

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showOptionDialog", e);
        }
    }

    private void showDeleteDialog(DebtLoanTransactionWithDetails debtLoanTransactionWithDetail) {

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
            deleteTransaction(debtLoanTransactionWithDetail);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteTransaction(DebtLoanTransactionWithDetails debtLoanTransactionWithDetail) {
        try {
            debtLoanViewModel.deletePaymentTransaction(debtLoanTransactionWithDetail.debtLoanPayment.id,
                    debtLoanTransactionWithDetail.debtLoanPayment.debtLoanId, debtLoanTransactionWithDetail.debtLoanPayment.debtLoanPaymentId);
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteTransaction", e);
        }
    }
}