package com.nprotech.moneytracker.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.DebtLoanType;
import com.nprotech.moneytracker.db.entites.DebtLoanEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanPaymentEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanPaymentTransactionEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanTransactionAttachmentEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.FrequencyModel;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.DebtLoanViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DebtLoanRecordPaymentActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener {

    private AppCompatImageView icBack, ivDebtLoanIcon, ivReceive, ivPaymentSelect, ivPaymentAmountSelect, ivPaymentMethodSelect, ivPaymentWalletSelect;
    private AppCompatTextView tvTitle, tvSave, tvDebtLoanName, tvDebtType, tvDebtLoanTitle, tvTotalAmount, tvPaidAmount, tvRemainingAmount, lblDebtLoanHintTitle,
            lblDebtLoanHintDesc, tvInstallment, tvPaymentDate, tvPaymentAmount, tvPaymentMethod, maxLimitNotes, attachmentTitleLabel, tvPaymentWallet;
    private AppCompatEditText etReference, etNotes;
    private MaterialCardView cardDebtLoanIcon, cardInstallmentNo, cardPaymentDate;
    private ConstraintLayout debtLoanHintContainer, attachFileContainer;
    private LinearLayout layoutPaymentDate, layoutPaymentAmount, layoutPaymentMethod, layoutPaymentWallet;
    private RecyclerView rvAttachmentImage;
    private DebtLoanViewModel debtLoanViewModel;
    private AccountViewModel accountViewModel;
    private ActivityResultLauncher<Intent> calculatorLauncher;
    private boolean isEdit;
    private int debtLoanId = 0;
    private int selectedPaymentMethod = 0;
    private int tempSelectedPaymentMethod = 0;
    private Date paymentDate;
    private long startDate = 0;
    private double paymentAmount = 0;
    private static final int DATE_TYPE_PAYMENT = 1;
    private int selectedDateType = DATE_TYPE_PAYMENT;
    private DebtLoanEntity debtLoan;
    private Typeface medium, semiBold;
    private Uri cameraTempUri;
    private final List<Uri> selectedFileUri = new ArrayList<>();
    private final List<String> existingAttachmentPaths = new ArrayList<>();
    private static final String ADD_MORE_URI = "expenixo://add_more";
    private RecyclerViewAdapter<Uri> uriRecyclerViewAdapter;
    private DebtLoanPaymentEntity nextPendingPayment;
    private DebtLoanPaymentEntity editingPayment;
    private WalletEntity selectedWallet;
    private List<WalletEntity> walletLists;
    private int debtLoanTransactionId = 0;
    private DebtLoanPaymentTransactionEntity existingTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt_loan_record_payment);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            View rootView = findViewById(R.id.rootView);
            tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);

            icBack = toolbarWrapper.findViewById(R.id.icBack);
            tvSave = toolbarWrapper.findViewById(R.id.tvSave);
            ivDebtLoanIcon = findViewById(R.id.ivDebtLoanIcon);
            ivReceive = findViewById(R.id.ivReceive);
            ivPaymentSelect = findViewById(R.id.ivPaymentSelect);
            ivPaymentAmountSelect = findViewById(R.id.ivPaymentAmountSelect);
            ivPaymentMethodSelect = findViewById(R.id.ivPaymentMethodSelect);
            tvDebtLoanName = findViewById(R.id.tvDebtLoanName);
            tvDebtType = findViewById(R.id.tvDebtType);
            tvDebtLoanTitle = findViewById(R.id.tvDebtLoanTitle);
            tvTotalAmount = findViewById(R.id.tvTotalAmount);
            tvPaidAmount = findViewById(R.id.tvPaidAmount);
            tvRemainingAmount = findViewById(R.id.tvRemainingAmount);
            lblDebtLoanHintTitle = findViewById(R.id.lblDebtLoanHintTitle);
            lblDebtLoanHintDesc = findViewById(R.id.lblDebtLoanHintDesc);
            tvInstallment = findViewById(R.id.tvInstallment);
            tvPaymentDate = findViewById(R.id.tvPaymentDate);
            tvPaymentAmount = findViewById(R.id.tvPaymentAmount);
            tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
            etReference = findViewById(R.id.etReference);
            etNotes = findViewById(R.id.etNotes);
            cardDebtLoanIcon = findViewById(R.id.cardDebtLoanIcon);
            cardInstallmentNo = findViewById(R.id.cardInstallmentNo);
            cardPaymentDate = findViewById(R.id.cardPaymentDate);
            debtLoanHintContainer = findViewById(R.id.debtLoanHintContainer);
            attachFileContainer = findViewById(R.id.attachFileContainer);
            layoutPaymentDate = findViewById(R.id.layoutPaymentDate);
            layoutPaymentAmount = findViewById(R.id.layoutPaymentAmount);
            layoutPaymentMethod = findViewById(R.id.layoutPaymentMethod);
            rvAttachmentImage = findViewById(R.id.rvAttachmentImage);
            maxLimitNotes = findViewById(R.id.maxLimitNotes);
            attachmentTitleLabel = findViewById(R.id.attachmentTitleLabel);
            layoutPaymentWallet = findViewById(R.id.layoutPaymentWallet);
            tvPaymentWallet = findViewById(R.id.tvPaymentWallet);
            ivPaymentWalletSelect = findViewById(R.id.ivPaymentWalletSelect);

            tvSave.setVisibility(View.VISIBLE);

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

                isEdit = bundle.getBoolean("isEdit", false);
                debtLoanId = bundle.getInt("debtLoanId", 0);
                debtLoanTransactionId = bundle.getInt("debtLoanTransactionId", 0);

                if (isEdit) {
                    tvSave.setText(getString(R.string.update));
                    tvTitle.setText(R.string.update_payment);
                } else {
                    tvSave.setText(getString(R.string.save));
                    tvTitle.setText(R.string.record_payment);
                }

                accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
                debtLoanViewModel = new ViewModelProvider(this).get(DebtLoanViewModel.class);

                medium = ResourcesCompat.getFont(this, R.font.exo2_medium);
                semiBold = ResourcesCompat.getFont(this, R.font.exo2_semibold);

                setupListeners();
                setupLauncher();
                initializeAdapter();
                bindData();
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

            if (isEdit) {
                tvSave.setText(getString(R.string.update));
                tvTitle.setText(getString(R.string.update_payment));
            } else {
                tvSave.setText(getString(R.string.save));
                tvTitle.setText(getString(R.string.record_payment));
            }

            debtLoanViewModel.getDebtLoanById(debtLoanId).observe(this, debtLoanWithDetails -> {
                if (debtLoanWithDetails == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                    finishWithTransitions();
                    return;
                }

                debtLoan = debtLoanWithDetails.debtLoan;
                walletLists = accountViewModel.getWalletsByAccountId(PreferenceManager.INSTANCE.getAccountId());

                // Existing Debt/Loan UI setup
                int debtLoanColor = Color.parseColor(DataHelper.getDebtLoanColorList().get(debtLoan.debtLoanColor));

                cardDebtLoanIcon.setCardBackgroundColor(debtLoanColor);
                ivDebtLoanIcon.setImageDrawable(ContextCompat.getDrawable(this, DataHelper.getCategoryIcons().get(debtLoan.debtLoanIcon)));
                tvDebtLoanName.setText(debtLoan.name);
                tvDebtLoanTitle.setText(debtLoan.title);

                startDate = debtLoan.startDate;

                if (debtLoan.type == DebtLoanType.BORROW) {
                    tvDebtType.setText(getString(R.string.borrow));
                    setDebtLoanStatusStyle(tvDebtType, R.color.expense, R.color.light_expense, R.color.expense);

                    debtLoanHintContainer.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.light_expense));
                    ivReceive.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.light_expense));
                    ivReceive.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_upward));
                    ImageViewCompat.setImageTintList(ivReceive, ColorStateList.valueOf(getColor(R.color.bright_red)));

                    lblDebtLoanHintTitle.setText(getString(R.string.you_are_paying));
                    lblDebtLoanHintDesc.setText(getString(R.string.reduce_the_debt_amount));
                } else if (debtLoan.type == DebtLoanType.LENT) {
                    tvDebtType.setText(getString(R.string.lent));
                    setDebtLoanStatusStyle(tvDebtType, R.color.dark_income, R.color.light_income, R.color.dark_income);

                    debtLoanHintContainer.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.very_light_income));
                    ivReceive.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.light_income));
                    ivReceive.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_downward));
                    ImageViewCompat.setImageTintList(ivReceive, ColorStateList.valueOf(getColor(R.color.dark_income)));

                    lblDebtLoanHintTitle.setText(getString(R.string.you_are_receiving));
                    lblDebtLoanHintDesc.setText(getString(R.string.reduce_the_remaining_amount));
                } else {
                    tvDebtType.setVisibility(View.GONE);
                }

                tvTotalAmount.setText(CommonUtils.getBeautifyAmount(debtLoan.currencySymbol, debtLoan.totalAmount));
                tvPaidAmount.setText(CommonUtils.getBeautifyAmount(debtLoan.currencySymbol, debtLoan.paidAmount));

                double remainingAmount = debtLoan.totalAmount - debtLoan.paidAmount;
                tvRemainingAmount.setText(CommonUtils.getBeautifyAmount(debtLoan.currencySymbol, remainingAmount));

                // ------------------------------------------------
                // EDIT MODE
                // ------------------------------------------------
                if (isEdit) {
                    loadTransactionForEdit();
                } else {
                    // ------------------------------------------------
                    // NEW PAYMENT MODE
                    // ------------------------------------------------
                    if (debtLoan.repaymentMethod != DebtLoanType.REPAYMENT_FLEXIBLE) {
                        cardInstallmentNo.setVisibility(View.VISIBLE);
                        cardInstallmentNo.setCardBackgroundColor(getColor(R.color.view_grey));
                        cardInstallmentNo.setStrokeColor(getColor(R.color.view_grey));
                        cardPaymentDate.setCardBackgroundColor(getColor(R.color.view_grey));
                        cardPaymentDate.setStrokeColor(getColor(R.color.view_grey));
                        cardInstallmentNo.setEnabled(false);
                        cardInstallmentNo.setClickable(false);
                        cardPaymentDate.setEnabled(false);
                        cardPaymentDate.setClickable(false);
                        layoutPaymentDate.setEnabled(false);
                        layoutPaymentDate.setClickable(false);
                        ivPaymentSelect.setEnabled(false);
                        ivPaymentSelect.setClickable(false);
                        updateNextInstallment();
                    } else {
                        cardInstallmentNo.setVisibility(View.GONE);
                        cardPaymentDate.setEnabled(true);
                        cardPaymentDate.setClickable(true);
                        layoutPaymentDate.setEnabled(true);
                        layoutPaymentDate.setClickable(true);
                        ivPaymentSelect.setEnabled(true);
                        ivPaymentSelect.setClickable(true);
                    }

                    Calendar calendar = Calendar.getInstance();
                    Calendar today = Calendar.getInstance();
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MILLISECOND, 0);

                    if (startDate > 0 && startDate <= today.getTimeInMillis()) {
                        calendar.setTimeInMillis(startDate);
                        paymentDate = calendar.getTime();
                        tvPaymentDate.setText(DateHelper.getDateFromPicker(
                                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)));
                    } else {
                        paymentDate = null;
                        Calendar startCalendar = Calendar.getInstance();
                        startCalendar.setTimeInMillis(startDate);
                        String availableFrom = DateHelper.getDateFromPicker(startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH),
                                startCalendar.get(Calendar.DAY_OF_MONTH));
                        tvPaymentDate.setText(getString(R.string.payment_available_from, availableFrom));
                    }

                    selectedPaymentMethod = DebtLoanType.PAYMENT_METHOD_CASH;
                    tempSelectedPaymentMethod = DebtLoanType.PAYMENT_METHOD_CASH;

                    if (!walletLists.isEmpty()) {
                        for (WalletEntity wallet : walletLists) {
                            if (debtLoan.walletId == wallet.id) {
                                selectedWallet = wallet;
                                break;
                            }
                        }

                        if (selectedWallet == null) {
                            selectedWallet = walletLists.get(0);
                        }

                        tvPaymentWallet.setText(getString(R.string.wallet_info, selectedWallet.name, CommonUtils.getBeautifyAmount(
                                selectedWallet.currencySymbol, selectedWallet.amount)));
                    }

                    if (debtLoan.repaymentMethod != DebtLoanType.REPAYMENT_FLEXIBLE) {
                        updateNextInstallment();
                    } else {
                        updateAmountText();
                    }

                    updatePaymentMethodFields();
                    updateAttachmentFileFields();
                    updateSaveButtonState();
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void loadExistingAttachments() {
        try {
            if (!isEdit || existingTransaction == null) {
                return;
            }

            List<DebtLoanTransactionAttachmentEntity> attachments = debtLoanViewModel.getAttachments(debtLoan.id, existingTransaction.debtLoanPaymentId,
                    existingTransaction.id);

            existingAttachmentPaths.clear();
            selectedFileUri.clear();

            if (attachments != null) {
                for (DebtLoanTransactionAttachmentEntity attachment : attachments) {
                    if (TextUtils.isEmpty(attachment.attachmentPath)) {
                        continue;
                    }
                    File file = new File(attachment.attachmentPath);
                    if (!file.exists()) {
                        continue;
                    }
                    existingAttachmentPaths.add(attachment.attachmentPath);
                    selectedFileUri.add(Uri.fromFile(file));
                }
            }

            refreshAttachmentList();
        } catch (Exception e) {
            AppLogger.e(getClass(), "loadExistingAttachments", e);
        }
    }

    private void loadTransactionForEdit() {
        debtLoanViewModel.getTransactionById(debtLoanTransactionId, debtLoanId).observe(this, transaction -> {
            if (transaction == null) {
                Toast.makeText(this, getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finishWithTransitions();
                return;
            }
            existingTransaction = transaction;

            if (debtLoan.repaymentMethod != DebtLoanType.REPAYMENT_FLEXIBLE && transaction.debtLoanPaymentId > 0) {
                debtLoanViewModel.getPaymentDataById(transaction.debtLoanPaymentId).observe(this, payment -> {
                    if (payment == null) {
                        Toast.makeText(this, getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                        finishWithTransitions();
                        return;
                    }
                    editingPayment = payment;
                    loadExistingTransaction(transaction);
                    loadExistingAttachments();
                });
            } else {
                loadExistingTransaction(transaction);
                loadExistingAttachments();
            }
        });
    }

    private void loadExistingTransaction(DebtLoanPaymentTransactionEntity transaction) {
        try {
            // Amount
            paymentAmount = transaction.amount;

            if (debtLoan.repaymentMethod != DebtLoanType.REPAYMENT_FLEXIBLE) {
                cardInstallmentNo.setVisibility(View.VISIBLE);
                cardInstallmentNo.setCardBackgroundColor(getColor(R.color.view_grey));
                cardInstallmentNo.setStrokeColor(getColor(R.color.view_grey));
                cardPaymentDate.setCardBackgroundColor(getColor(R.color.view_grey));
                cardPaymentDate.setStrokeColor(getColor(R.color.view_grey));
                cardInstallmentNo.setEnabled(false);
                cardInstallmentNo.setClickable(false);
                cardPaymentDate.setEnabled(false);
                cardPaymentDate.setClickable(false);
                layoutPaymentDate.setEnabled(false);
                layoutPaymentDate.setClickable(false);
                ivPaymentSelect.setEnabled(false);
                ivPaymentSelect.setClickable(false);

                if (editingPayment != null) {
                    int totalInstallments = debtLoanViewModel.getTotalInstallments(debtLoan.id);
                    String dueDate = formatInstallmentDate(editingPayment.paymentDate);
                    String installmentText = getString(R.string.installment_due_format, editingPayment.paymentNumber, totalInstallments, dueDate);
                    tvInstallment.setText(installmentText);
                }
            } else {
                cardInstallmentNo.setVisibility(View.GONE);
                cardPaymentDate.setEnabled(true);
                cardPaymentDate.setClickable(true);
                layoutPaymentDate.setEnabled(true);
                layoutPaymentDate.setClickable(true);
                ivPaymentSelect.setEnabled(true);
                ivPaymentSelect.setClickable(true);
            }

            // Date
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(transaction.paymentDate);

            paymentDate = calendar.getTime();
            tvPaymentDate.setText(DateHelper.getDateFromPicker(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)));

            // Payment method
            selectedPaymentMethod = transaction.paymentMethod;
            tempSelectedPaymentMethod = transaction.paymentMethod;

            // Reference
            etReference.setText(transaction.reference != null ? transaction.reference : "");

            // Notes
            etNotes.setText(transaction.notes != null ? transaction.notes : "");

            // Wallet
            selectedWallet = null;

            if (walletLists != null) {
                for (WalletEntity wallet : walletLists) {
                    if (wallet.id == transaction.walletId) {
                        selectedWallet = wallet;
                        break;
                    }
                }
            }

            if (selectedWallet != null) {
                tvPaymentWallet.setText(getString(R.string.wallet_info, selectedWallet.name, CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol,
                        selectedWallet.amount)));
            }

            updateAmountText();
            updatePaymentMethodFields();
            updateAttachmentFileFields();
            updateSaveButtonState();
        } catch (Exception e) {
            AppLogger.e(getClass(), "loadExistingTransaction", e);
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

            // PAYMENT DATE
            layoutPaymentDate.setOnClickListener(v -> {
                selectedDateType = DATE_TYPE_PAYMENT;
                openDateDialog();
            });

            ivPaymentSelect.setOnClickListener(v -> {
                selectedDateType = DATE_TYPE_PAYMENT;
                openDateDialog();
            });

            // PAYMENT WALLET
            layoutPaymentWallet.setOnClickListener(v -> selectWallets());

            ivPaymentWalletSelect.setOnClickListener(v -> selectWallets());

            // PAYMENT AMOUNT
            layoutPaymentAmount.setOnClickListener(v -> {
                hideKeyboard(this);

                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", paymentAmount);
                intent.putExtra("type", "amount");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                calculatorLauncher.launch(intent, options);
            });

            ivPaymentAmountSelect.setOnClickListener(v -> {
                hideKeyboard(this);

                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", paymentAmount);
                intent.putExtra("type", "amount");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                calculatorLauncher.launch(intent, options);
            });

            // PAYMENT METHOD
            layoutPaymentMethod.setOnClickListener(v -> showPaymentMethodPicker());

            ivPaymentMethodSelect.setOnClickListener(v -> showPaymentMethodPicker());

            // ATTACHMENTS
            attachFileContainer.setOnClickListener(view -> {
                hideKeyboard(this);
                showPicker();
            });

            // NOTES
            // NOTES
            etNotes.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    maxLimitNotes.setText(getString(R.string.character_limit_50, charSequence.length()));
                }
            });

            // SAVE
            tvSave.setOnClickListener(v -> {
                if (!tvSave.isEnabled()) {
                    return;
                }

                savePayment();
            });

            debtLoanViewModel.getDataSavedStatus().observe(this, aBoolean -> {
                tvSave.setEnabled(true);
                if (Boolean.TRUE.equals(aBoolean)) {

                    List<DebtLoanTransactionAttachmentEntity> attachmentEntities = new ArrayList<>();
                    if (!selectedFileUri.isEmpty()) {
                        for (Uri uri : selectedFileUri) {

                            if ("file".equalsIgnoreCase(uri.getScheme()) && existingAttachmentPaths.contains(uri.getPath())) {
                                continue;
                            }

                            try {
                                File file = saveFinalFile(uri, debtLoan.tempDebtLoanServerId);
                                DebtLoanTransactionAttachmentEntity transactionAttachment = new DebtLoanTransactionAttachmentEntity();
                                transactionAttachment.tempDebtLoanServerId = debtLoan.tempDebtLoanServerId;
                                transactionAttachment.serverId = 0;
                                transactionAttachment.debtLoanId = debtLoan.id;
                                transactionAttachment.debtLoanPaymentId = debtLoanViewModel.getPaymentId();
                                transactionAttachment.debtLoanPaymentTransactionId = debtLoanViewModel.getTransactionId();
                                transactionAttachment.attachmentPath = file.getAbsolutePath();
                                transactionAttachment.attachmentName = CommonUtils.getFileName(uri, this);
                                transactionAttachment.attachmentExtension = CommonUtils.getFileExtension(transactionAttachment.attachmentName);
                                transactionAttachment.attachmentSize = file.length();
                                transactionAttachment.createdAt = System.currentTimeMillis();
                                transactionAttachment.updatedAt = System.currentTimeMillis();

                                attachmentEntities.add(transactionAttachment);
                            } catch (Exception e) {
                                AppLogger.e(getClass(), "buildTransaction", e);
                            }
                        }

                        if (!attachmentEntities.isEmpty()) {
                            debtLoanViewModel.saveTransactionAttachment(attachmentEntities);
                        }
                    }

                    Toast.makeText(getApplicationContext(), getString(isEdit ? R.string.payment_updated : R.string.payment_recorded), Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finishWithTransitions();
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void setupLauncher() {
        try {
            calculatorLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        double amount = data.getDoubleExtra("amount", 0);
                        String type = data.getStringExtra("type");

                        if (type != null && type.equalsIgnoreCase("amount")) {
                            paymentAmount = amount;
                        }

                        updateAmountText();
                        updateSaveButtonState();
                    }
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupLauncher", e);
        }
    }

    private void initializeAdapter() {
        try {
            uriRecyclerViewAdapter = new RecyclerViewAdapter<>(this, selectedFileUri, R.layout.item_transaction_attachment) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, Uri uri) {
                    if (uri != null) {
                        View addMoreContainer = holder.getView(R.id.addMoreContainer);
                        View attachmentPreview = holder.getView(R.id.cardAttachmentPreview);
                        View deleteButton = holder.getView(R.id.cardDelete);

                        AppCompatImageView ivAttachmentPreview = holder.getView(R.id.ivAttachmentPreview);
                        AppCompatImageView ivAttachmentFileType = holder.getView(R.id.ivAttachmentFileType);
                        AppCompatTextView tvAttachmentName = holder.getView(R.id.tvAttachmentName);
                        AppCompatTextView tvAttachmentSize = holder.getView(R.id.tvAttachmentSize);

                        // =====================================
                        // ADD MORE ITEM
                        // =====================================
                        if (ADD_MORE_URI.equals(uri.toString())) {
                            addMoreContainer.setVisibility(View.VISIBLE);
                            attachmentPreview.setVisibility(View.GONE);
                            deleteButton.setVisibility(View.GONE);
                            tvAttachmentName.setVisibility(View.GONE);
                            tvAttachmentSize.setVisibility(View.GONE);
                            addMoreContainer.setOnClickListener(v -> {
                                hideKeyboard(DebtLoanRecordPaymentActivity.this);
                                if (selectedFileUri.size() < 5) {
                                    showPicker();
                                }
                            });
                            return;
                        }

                        // =========================================
                        // NORMAL ATTACHMENT ITEM
                        // =========================================
                        addMoreContainer.setVisibility(View.GONE);
                        attachmentPreview.setVisibility(View.VISIBLE);
                        deleteButton.setVisibility(View.VISIBLE);
                        tvAttachmentName.setVisibility(View.VISIBLE);
                        tvAttachmentSize.setVisibility(View.VISIBLE);

                        // =========================================
                        // FILE TYPE
                        // =========================================
                        String mime = getContentResolver().getType(uri);
                        if (mime == null) {
                            String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                            if (!TextUtils.isEmpty(extension)) {
                                mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase(Locale.ROOT));
                            }
                        }

                        // =========================================
                        // IMAGE & FILE
                        // =========================================
                        if (mime != null && mime.startsWith("image/")) {
                            Glide.with(ivAttachmentPreview.getContext())
                                    .load(uri)
                                    .into(ivAttachmentPreview);
                            ivAttachmentPreview.setVisibility(View.VISIBLE);
                            ivAttachmentFileType.setVisibility(View.GONE);
                        } else {
                            ivAttachmentPreview.setVisibility(View.GONE);
                            ivAttachmentFileType.setVisibility(View.VISIBLE);
                            ivAttachmentFileType.setImageResource(getFileIconFromUri(uri));
                        }

                        // =========================================
                        // FILE NAME
                        // =========================================
                        String fileName = CommonUtils.getFileName(uri, DebtLoanRecordPaymentActivity.this);
                        if (TextUtils.isEmpty(fileName)) {
                            fileName = "attachment";
                        }
                        tvAttachmentName.setText(fileName);
                        tvAttachmentName.setSelected(true);

                        // =========================================
                        // FILE SIZE
                        // =========================================
                        long fileSize = CommonUtils.getFileSize(uri, DebtLoanRecordPaymentActivity.this);
                        tvAttachmentSize.setText(Formatter.formatFileSize(DebtLoanRecordPaymentActivity.this, fileSize));

                        // =========================================
                        // DELETE
                        // =========================================
                        deleteButton.setOnClickListener(v -> {
                            int position = selectedFileUri.indexOf(uri);
                            if (position != -1) {
                                boolean isExistingAttachment = "file".equalsIgnoreCase(uri.getScheme()) && existingAttachmentPaths.contains(uri.getPath());
                                selectedFileUri.remove(position);
                                if (isExistingAttachment) {
                                    debtLoanViewModel.deleteAttachment(uri.getPath(), debtLoan.tempDebtLoanServerId, debtLoan.id,
                                            existingTransaction != null ? existingTransaction.debtLoanPaymentId : 0,
                                            existingTransaction != null ? existingTransaction.id : 0);
                                    deleteLocalFile(uri.getPath());
                                } else {
                                    deleteTemporaryUri(uri);
                                }
                                refreshAttachmentList();
                            }
                        });
                    }
                }
            };

            rvAttachmentImage.setAdapter(uriRecyclerViewAdapter);
            rvAttachmentImage.setHasFixedSize(true);
            rvAttachmentImage.setItemAnimator(null);
            rvAttachmentImage.setLayoutManager(new GridLayoutManager(this, 3));
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapter", e);
        }
    }

    private void deleteTemporaryUri(Uri uri) {
        try {
            if (uri == null) {
                return;
            }

            if ("content".equalsIgnoreCase(uri.getScheme())) {
                return;
            }

            if ("file".equalsIgnoreCase(uri.getScheme())) {
                File file = new File(Objects.requireNonNull(uri.getPath()));

                if (file.exists() && !file.delete()) {
                    AppLogger.d(getClass(), "Failed to delete file: " + file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteTemporaryUri", e);
        }
    }

    private void updateNextInstallment() {
        try {
            if (debtLoan == null) {
                nextPendingPayment = null;
                return;
            }

            if (debtLoan.repaymentMethod == DebtLoanType.REPAYMENT_FLEXIBLE) {
                nextPendingPayment = null;
                cardInstallmentNo.setVisibility(View.GONE);
                return;
            }

            int totalInstallments = debtLoanViewModel.getTotalInstallments(debtLoan.id);
            nextPendingPayment = debtLoanViewModel.getNextPendingPayment(debtLoan.id);
            if (nextPendingPayment == null || totalInstallments <= 0) {
                nextPendingPayment = null;
                cardInstallmentNo.setVisibility(View.GONE);
                return;
            }

            cardInstallmentNo.setVisibility(View.VISIBLE);

            String dueDate = formatInstallmentDate(nextPendingPayment.paymentDate);

            String installmentText = getString(R.string.installment_due_format, nextPendingPayment.paymentNumber, totalInstallments, dueDate);
            tvInstallment.setText(installmentText);

            double installmentRemaining = nextPendingPayment.paymentAmount - nextPendingPayment.paidAmount;

            if (installmentRemaining < 0) {
                installmentRemaining = 0;
            }

            if (selectedWallet != null) {
                if (selectedWallet.currencyCode.equalsIgnoreCase(debtLoan.currencyCode)) {
                    paymentAmount = installmentRemaining;
                } else {
                    paymentAmount = installmentRemaining / selectedWallet.exchangeRate;
                }
            } else {
                paymentAmount = installmentRemaining;
            }

            updateAmountText();
            updateSaveButtonState();
        } catch (Exception e) {
            AppLogger.e(getClass(), "updateNextInstallment", e);
        }
    }

    private void showPicker() {
        AlertDialog dialog;

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_select_source, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        view.findViewById(R.id.llCamera).setOnClickListener(v -> {
            dialog.dismiss();
            checkCameraPermissionAndOpen();
        });

        view.findViewById(R.id.llGallery).setOnClickListener(v -> {
            dialog.dismiss();
            openGallery();
        });

        view.findViewById(R.id.llFile).setOnClickListener(v -> {
            dialog.dismiss();
            openFileManager();
        });

        dialog.show();
    }

    private void openCamera() {
        try {
            File tempFile = File.createTempFile("CAM_", ".jpg", getCacheDir());

            cameraTempUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", tempFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraTempUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            cameraLauncher.launch(intent);

        } catch (Exception e) {
            AppLogger.e(getClass(), "openCamera", e);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryLauncher.launch(intent);
    }

    private void openFileManager() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        fileLauncher.launch(intent);
    }

    private int getFileIconFromUri(Uri uri) {

        String name = CommonUtils.getFileName(uri, this).toLowerCase();

        if (name.endsWith(".pdf")) return R.drawable.ic_file_pdf;
        if (name.endsWith(".doc") || name.endsWith(".docx")) return R.drawable.ic_file_doc;
        if (name.endsWith(".ppt") || name.endsWith(".pptx")) return R.drawable.ic_file_ppt;
        if (name.endsWith(".xls") || name.endsWith(".xlsx") || name.endsWith(".csv"))
            return R.drawable.ic_file_excel;
        if (name.endsWith(".zip")) return R.drawable.ic_file_zip;
        if (name.endsWith(".rar")) return R.drawable.ic_file_rar;
        if (name.endsWith(".xml")) return R.drawable.ic_file_xml;

        return R.drawable.ic_file_generic;
    }

    private void deleteLocalFile(String filePath) {
        try {
            if (TextUtils.isEmpty(filePath)) {
                return;
            }
            File file = new File(filePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                AppLogger.d(getClass(), "Old attachment deleted: " + deleted);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteLocalFile", e);
        }
    }

    private File compressImageKeepResolution(Uri uri, File outFile) throws Exception {

        InputStream input = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input);
        if (input != null) input.close();

        int quality = 95;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        do {
            byteArrayOutputStream.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
            quality -= 5;
        } while ((byteArrayOutputStream.size() / 1024) > 300 && quality >= 80);

        FileOutputStream fos = new FileOutputStream(outFile, false);
        fos.write(byteArrayOutputStream.toByteArray());
        fos.flush();
        fos.close();

        bitmap.recycle();
        return outFile;
    }

    private File saveFinalFile(Uri uri, String recordPaymentId) throws Exception {

        File uploadsDir = new File(getFilesDir(), "uploads");
        File paymentsDir = new File(uploadsDir, "payments");
        File dir = new File(paymentsDir, recordPaymentId);

        if (!dir.exists() && !dir.mkdirs()) {
            AppLogger.w(getClass(), "Failed to create payment folder");
            throw new Exception("Unable to create payment attachment directory");
        }

        String name = CommonUtils.getFileName(uri, this);
        String extension = "";

        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < name.length() - 1) {
            extension = name.substring(dotIndex).toLowerCase(Locale.ROOT);
        }

        if (extension.isEmpty()) {
            extension = ".bin";
        }

        File outFile = new File(dir, "ATT_" + UUID.randomUUID() + extension);
        String mime = getContentResolver().getType(uri);

        if (mime != null && mime.startsWith("image/")) {
            return compressImageKeepResolution(uri, outFile);
        }

        return copyUriToFile(uri, outFile);
    }

    private File copyUriToFile(Uri uri, File outFile) throws Exception {

        InputStream in = getContentResolver().openInputStream(uri);
        OutputStream out = new FileOutputStream(outFile);

        byte[] buffer = new byte[4096];
        int read;
        if (in != null) {
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }

        Objects.requireNonNull(in).close();
        out.close();

        return outFile;
    }

    private String formatInstallmentDate(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return DateHelper.getDateFromPicker(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    // LAUNCHER & PERMISSIONS
    private void checkCameraPermissionAndOpen() {

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
            return;
        }

        boolean askedBefore = PreferenceManager.INSTANCE.getPermissionCameraAsked();
        if (!askedBefore) {
            // 🟢 FIRST TIME → ask permission
            PreferenceManager.INSTANCE.setPermissionCameraAsked(true);
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            return;
        }

        // Permission NOT granted
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // 🟡 Denied once → explain + ask again
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.camera_permission_required))
                    .setMessage(getString(R.string.camera_access_required))
                    .setPositiveButton(getString(R.string.allow), (d, w) ->
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        } else {
            // 🔴 Permanently denied → settings
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.permission_required))
                    .setMessage(getString(R.string.camera_permission_disabled))
                    .setPositiveButton(getString(R.string.open_settings), (d, w) -> openAppSettings())
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && cameraTempUri != null && selectedFileUri.size() < 5) {
                    selectedFileUri.add(cameraTempUri);
                    refreshAttachmentList();
                }
            }
    );

    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {

                if (result.getResultCode() != RESULT_OK) {
                    return;
                }

                Intent data = result.getData();

                if (data == null) {
                    return;
                }

                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    int remaining = 5 - selectedFileUri.size();
                    for (int i = 0; i < clipData.getItemCount() && remaining > 0; i++) {
                        Uri uri = clipData.getItemAt(i).getUri();
                        if (uri != null && !selectedFileUri.contains(uri)) {
                            selectedFileUri.add(uri);
                            remaining--;
                        }
                    }
                } else {
                    Uri uri = data.getData();
                    if (uri != null && selectedFileUri.size() < 5 && !selectedFileUri.contains(uri)) {
                        selectedFileUri.add(uri);
                    }
                }
                refreshAttachmentList();
            }
    );

    ActivityResultLauncher<Intent> fileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK) {
                    return;
                }

                Intent data = result.getData();

                if (data == null) {
                    return;
                }

                Uri uri = data.getData();
                if (uri != null && selectedFileUri.size() < 5 && !selectedFileUri.contains(uri)) {
                    selectedFileUri.add(uri);
                    refreshAttachmentList();
                }
            }
    );

    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera(); // 🔥 permission granted → open camera
                } else {
                    Toast.makeText(this, getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show();
                }
            });

    // PAYMENT METHODS
    private void showPaymentMethodPicker() {
        try {

            prepareBottomSheet();
            tempSelectedPaymentMethod = selectedPaymentMethod;

            List<FrequencyModel> frequencyList = new ArrayList<>();
            frequencyList.add(new FrequencyModel(DebtLoanType.PAYMENT_METHOD_CASH, R.drawable.ic_payment_cash, getString(R.string.cash)));
            frequencyList.add(new FrequencyModel(DebtLoanType.PAYMENT_METHOD_UPI, R.drawable.ic_payment_upi, getString(R.string.upi)));
            frequencyList.add(new FrequencyModel(DebtLoanType.PAYMENT_METHOD_BANK_TRANSFER, R.drawable.ic_payment_bank, getString(R.string.bank_transfer)));
            frequencyList.add(new FrequencyModel(DebtLoanType.PAYMENT_METHOD_CARD, R.drawable.ic_payment_card, getString(R.string.card)));
            frequencyList.add(new FrequencyModel(DebtLoanType.PAYMENT_METHOD_CHEQUE, R.drawable.ic_payment_cheque, getString(R.string.cheque)));
            frequencyList.add(new FrequencyModel(DebtLoanType.PAYMENT_METHOD_NET_BANKING, R.drawable.ic_payment_banking, getString(R.string.net_banking)));
            frequencyList.add(new FrequencyModel(DebtLoanType.PAYMENT_METHOD_DEMAND_DRAFT, R.drawable.ic_payment_draft, getString(R.string.demand_draft)));
            frequencyList.add(new FrequencyModel(DebtLoanType.PAYMENT_METHOD_OTHER, R.drawable.ic_settings_other, getString(R.string.others)));

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_calendar_filter_layout, findViewById(android.R.id.content), false);

            AppCompatTextView tvSelectRange = bottomView.findViewById(R.id.tvSelectRange);
            RecyclerView rvSelectRange = bottomView.findViewById(R.id.rvSelectRange);
            LinearLayout layoutActions = bottomView.findViewById(R.id.layoutActions);
            AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);
            MaterialButton btnPrimary = bottomView.findViewById(R.id.btnPrimary);
            MaterialButton btnSecondary = bottomView.findViewById(R.id.btnSecondary);

            tvSelectRange.setText(R.string.select_payment_method);
            layoutActions.setVisibility(View.VISIBLE);
            tvClose.setVisibility(View.VISIBLE);

            RecyclerViewAdapter<FrequencyModel> adapter = new RecyclerViewAdapter<>(this, frequencyList, R.layout.item_calendar_filter) {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onPostBindViewHolder(ViewHolder holder, FrequencyModel frequency) {
                    boolean selected = tempSelectedPaymentMethod == frequency.frequency;

                    holder.setViewText(R.id.tvFilterName, frequency.frequencyName);
                    holder.setViewImageDrawable(R.id.ivIcon, ContextCompat.getDrawable(getApplicationContext(), frequency.icon));

                    holder.setViewVisibility(R.id.ivSelected, selected ? View.VISIBLE : View.GONE);
                    holder.setViewTypeface(R.id.tvFilterName, selected ? semiBold : medium);

                    holder.getView(R.id.rlFilterView).setOnClickListener(v -> {
                        tempSelectedPaymentMethod = frequency.frequency;
                        notifyDataSetChanged();
                    });
                }
            };

            rvSelectRange.setAdapter(adapter);
            rvSelectRange.setHasFixedSize(true);
            rvSelectRange.setItemAnimator(null);

            btnPrimary.setOnClickListener(v -> {
                selectedPaymentMethod = tempSelectedPaymentMethod;
                updatePaymentMethodFields();
                dialog.dismiss();
            });

            btnSecondary.setOnClickListener(v -> {
                selectedPaymentMethod = DebtLoanType.PAYMENT_METHOD_CASH;
                updatePaymentMethodFields();
                dialog.dismiss();
            });

            tvClose.setOnClickListener(v -> dialog.dismiss());

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showFrequencyPicker", e);
        }
    }

    private void updatePaymentMethodFields() {

        String text = switch (selectedPaymentMethod) {
            case DebtLoanType.PAYMENT_METHOD_CASH -> getString(R.string.cash);
            case DebtLoanType.PAYMENT_METHOD_UPI -> getString(R.string.upi);
            case DebtLoanType.PAYMENT_METHOD_BANK_TRANSFER -> getString(R.string.bank_transfer);
            case DebtLoanType.PAYMENT_METHOD_CARD -> getString(R.string.card);
            case DebtLoanType.PAYMENT_METHOD_CHEQUE -> getString(R.string.cheque);
            case DebtLoanType.PAYMENT_METHOD_NET_BANKING -> getString(R.string.net_banking);
            case DebtLoanType.PAYMENT_METHOD_DEMAND_DRAFT -> getString(R.string.demand_draft);
            case DebtLoanType.PAYMENT_METHOD_OTHER -> getString(R.string.others);
            default -> "";
        };

        tvPaymentMethod.setText(text);
        updateSaveButtonState();
    }

    private void selectWallets() {
        try {

            prepareBottomSheet();

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_wallet_picker_layout, findViewById(android.R.id.content), false);
            RecyclerView rvWallets = bottomView.findViewById(R.id.rvWallets);
            View viewLine = bottomView.findViewById(R.id.viewLine);
            LinearLayout layoutAddWallet = bottomView.findViewById(R.id.layoutAddWallet);
            viewLine.setVisibility(View.GONE);
            layoutAddWallet.setVisibility(View.GONE);

            RecyclerViewAdapter<WalletEntity> adapter = new RecyclerViewAdapter<>(this, walletLists, R.layout.item_switch_accounts) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, WalletEntity walletEntity) {

                    holder.setViewText(R.id.tvAccountName, walletEntity.name);

                    holder.setViewText(R.id.tvAccountBalance, getString(R.string.account_balance_format,
                            CommonUtils.getBeautifyAmount(walletEntity.currencySymbol, walletEntity.amount)));

                    holder.getView(R.id.ivSelected).setVisibility(selectedWallet != null && selectedWallet.id == walletEntity.id ? View.VISIBLE : View.GONE);
                    holder.getView(R.id.rlAccountView).setOnClickListener(v -> {
                        selectedWallet = walletEntity;
                        tvPaymentWallet.setText(getString(R.string.wallet_info, selectedWallet.name,
                                CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol, selectedWallet.amount)));

                        if (debtLoan.repaymentMethod != DebtLoanType.REPAYMENT_FLEXIBLE && nextPendingPayment != null && selectedWallet != null) {

                            double installmentRemaining = nextPendingPayment.paymentAmount - nextPendingPayment.paidAmount;

                            if (installmentRemaining < 0) {
                                installmentRemaining = 0;
                            }

                            paymentAmount = selectedWallet.currencyCode.equalsIgnoreCase(debtLoan.currencyCode)
                                    ? installmentRemaining : installmentRemaining / selectedWallet.exchangeRate;
                        }

                        updateAmountText();
                        updateSaveButtonState();
                        dialog.dismiss();
                    });

                }
            };

            rvWallets.setAdapter(adapter);
            rvWallets.setHasFixedSize(true);
            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "selectWallets", e);
        }
    }

    private void updateAmountText() {
        String symbol = selectedWallet != null ? selectedWallet.currencySymbol : debtLoan.currencySymbol;
        tvPaymentAmount.setText(CommonUtils.getBeautifyAmount(symbol, paymentAmount));
    }

    private void updateAttachmentFileFields() {
        try {
            attachmentTitleLabel.setText(getString(R.string.attachment_title, 0));
        } catch (Exception e) {
            AppLogger.e(getClass(), "", e);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshAttachmentList() {
        try {
            int count = selectedFileUri.size();
            // =========================================
            // UPDATE TITLE
            // =========================================
            attachmentTitleLabel.setText(getString(R.string.attachment_title, count));

            // =========================================
            // NO ATTACHMENTS
            // =========================================
            if (count == 0) {
                attachFileContainer.setVisibility(View.VISIBLE);
                rvAttachmentImage.setVisibility(View.GONE);
                return;
            }

            // =========================================
            // HAS ATTACHMENTS
            // =========================================

            // Hide original large dashed container
            attachFileContainer.setVisibility(View.GONE);

            // Show RecyclerView
            rvAttachmentImage.setVisibility(View.VISIBLE);

            // =========================================
            // BUILD GRID ITEMS
            // =========================================
            List<Uri> items = new ArrayList<>(selectedFileUri);
            if (count < 5) {
                items.add(Uri.parse(ADD_MORE_URI));
            }
            uriRecyclerViewAdapter.setItems(items);
        } catch (Exception e) {
            AppLogger.e(getClass(), "refreshAttachmentList", e);
        }
    }

    private void updateSaveButtonState() {
        boolean enabled = paymentAmount > 0;

        if (selectedWallet == null) {
            enabled = false;
        }

        if (paymentDate == null) {
            enabled = false;
        }

        if (startDate > 0 && paymentDate != null) {
            enabled = enabled && paymentDate.getTime() >= startDate;
        }

        if (debtLoan != null && selectedWallet != null && paymentAmount > 0) {
            double convertedAmount = convertToDebtLoanCurrency(paymentAmount, selectedWallet, debtLoan);
            if (debtLoan.repaymentMethod == DebtLoanType.REPAYMENT_FLEXIBLE) {
                if (isEdit && existingTransaction != null) {
                    double availableAmount = debtLoan.remainingAmount + existingTransaction.convertedAmount;
                    enabled = enabled && convertedAmount <= availableAmount;
                } else {
                    enabled = enabled && convertedAmount <= debtLoan.remainingAmount;
                }
            } else {
                if (isEdit && existingTransaction != null) {
                    enabled = true;
                } else if (nextPendingPayment != null) {
                    double installmentRemaining = nextPendingPayment.paymentAmount - nextPendingPayment.paidAmount;
                    if (installmentRemaining < 0) {
                        installmentRemaining = 0;
                    }
                    enabled = enabled && convertedAmount <= installmentRemaining;
                }
            }
        }

        tvSave.setEnabled(enabled);
        tvSave.setAlpha(enabled ? 1.0f : 0.5f);
    }

    private void savePayment() {
        try {
            if (debtLoan == null) {
                return;
            }

            if (isEdit) {
                updateExistingPayment();
                return;
            }

            if (paymentAmount <= 0) {
                updateSaveButtonState();
                return;
            }

            if (debtLoan.repaymentMethod != DebtLoanType.REPAYMENT_FLEXIBLE) {

                if (nextPendingPayment == null) {
                    Toast.makeText(this, getString(R.string.no_pending_installment), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedWallet == null) {
                    if (debtLoan.type == DebtLoanType.BORROW) {
                        Toast.makeText(this, getString(R.string.error_debt_update), Toast.LENGTH_SHORT).show();
                    } else if (debtLoan.type == DebtLoanType.LENT) {
                        Toast.makeText(this, getString(R.string.error_loan_update), Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                // ==========================================
                // CURRENT INSTALLMENT REMAINING
                // ==========================================
                double installmentRemaining = nextPendingPayment.paymentAmount - nextPendingPayment.paidAmount;
                if (installmentRemaining < 0) {
                    installmentRemaining = 0;
                }

                // ==========================================
                // CONVERT PAYMENT TO DEBT/LOAN CURRENCY
                // ==========================================

                double convertedAmount = convertToDebtLoanCurrency(paymentAmount, selectedWallet, debtLoan);

                if (convertedAmount > installmentRemaining) {
                    Toast.makeText(this, getString(
                            R.string.payment_amount_exceeds_remaining,
                            CommonUtils.getBeautifyAmount(
                                    debtLoan.currencySymbol,
                                    installmentRemaining
                            )
                    ), Toast.LENGTH_SHORT).show();
                    return;
                }

                long time = System.currentTimeMillis();
                DebtLoanPaymentTransactionEntity transaction = new DebtLoanPaymentTransactionEntity();
                transaction.debtLoanPaymentId = nextPendingPayment.id;
                transaction.debtLoanId = debtLoan.id;
                transaction.walletId = selectedWallet.id;
                transaction.currencyCode = selectedWallet.currencyCode;
                transaction.currencySymbol = selectedWallet.currencySymbol;
                transaction.exchangeRate = selectedWallet.currencyCode.equalsIgnoreCase(debtLoan.currencyCode) ? 1.0 : selectedWallet.exchangeRate;
                transaction.convertedAmount = convertedAmount;
                transaction.paymentDate = paymentDate != null ? paymentDate.getTime() : System.currentTimeMillis();
                transaction.amount = paymentAmount;
                transaction.paymentMethod = selectedPaymentMethod;
                transaction.reference = Objects.requireNonNull(etReference.getText()).toString().trim();
                transaction.notes = Objects.requireNonNull(etNotes.getText()).toString().trim();
                transaction.createdAt = time;
                transaction.updatedAt = time;
                transaction.isSynced = false;
                transaction.isDeleted = false;
                transaction.tempDebtLoanTransactionServerId = "DBT_" + time + "_" + nextPendingPayment.paymentNumber;
                transaction.debtLoanTransactionId = 0;

                nextPendingPayment.paidAmount += convertedAmount;
                if (nextPendingPayment.paidAmount >= nextPendingPayment.paymentAmount) {
                    nextPendingPayment.paidAmount = nextPendingPayment.paymentAmount;
                    nextPendingPayment.status = DebtLoanPaymentEntity.PAYMENT_PAID;
                    nextPendingPayment.paidDate = transaction.paymentDate;
                } else {
                    nextPendingPayment.status = DebtLoanPaymentEntity.PAYMENT_PENDING;
                }

                nextPendingPayment.updatedAt = System.currentTimeMillis();

                debtLoan.paidAmount += convertedAmount;
                if (debtLoan.paidAmount >= debtLoan.totalAmount) {
                    debtLoan.paidAmount = debtLoan.totalAmount;
                    debtLoan.remainingAmount = 0;
                } else {
                    debtLoan.remainingAmount = debtLoan.totalAmount - debtLoan.paidAmount;
                }

                debtLoan.updatedAt = System.currentTimeMillis();

                tvSave.setEnabled(false);
                debtLoanViewModel.saveScheduledPaymentTransaction(transaction, nextPendingPayment, debtLoan);
                return;
            }

            saveFlexiblePayment();
        } catch (Exception e) {
            AppLogger.e(getClass(), "savePayment", e);
            Toast.makeText(this, getString(R.string.error_update), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateExistingPayment() {
        try {
            if (existingTransaction == null) {
                return;
            }

            if (selectedWallet == null) {
                return;
            }

            if (paymentAmount <= 0) {
                updateSaveButtonState();
                return;
            }

            existingTransaction.debtLoanId = debtLoan.id;
            existingTransaction.walletId = selectedWallet.id;
            existingTransaction.currencyCode = selectedWallet.currencyCode;
            existingTransaction.currencySymbol = selectedWallet.currencySymbol;
            existingTransaction.exchangeRate = selectedWallet.exchangeRate;
            existingTransaction.amount = paymentAmount;
            existingTransaction.convertedAmount = convertToDebtLoanCurrency(paymentAmount, selectedWallet, debtLoan);
            existingTransaction.paymentMethod = selectedPaymentMethod;
            existingTransaction.paymentDate = paymentDate != null ? paymentDate.getTime() : System.currentTimeMillis();
            existingTransaction.reference = Objects.requireNonNull(etReference.getText()).toString().trim();
            existingTransaction.notes = Objects.requireNonNull(etNotes.getText()).toString().trim();
            existingTransaction.updatedAt = System.currentTimeMillis();
            existingTransaction.isDeleted = false;
            existingTransaction.isSynced = false;
            tvSave.setEnabled(false);
            debtLoanViewModel.updatePaymentTransaction(existingTransaction);
        } catch (Exception e) {
            AppLogger.e(getClass(), "updateExistingPayment", e);
            Toast.makeText(this, getString(R.string.error_update), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveFlexiblePayment() {

        try {

            if (debtLoan == null) {
                return;
            }

            if (paymentAmount <= 0) {
                return;
            }

            if (selectedWallet == null) {
                return;
            }

            double convertedAmount = convertToDebtLoanCurrency(paymentAmount, selectedWallet, debtLoan);

            if (convertedAmount > debtLoan.remainingAmount) {
                Toast.makeText(this, getString(R.string.payment_amount_exceeds_remaining, CommonUtils.getBeautifyAmount(debtLoan.currencySymbol,
                        debtLoan.remainingAmount)), Toast.LENGTH_SHORT).show();
                return;
            }

            long time = System.currentTimeMillis();
            DebtLoanPaymentTransactionEntity transaction = new DebtLoanPaymentTransactionEntity();
            transaction.debtLoanPaymentId = 0;
            transaction.debtLoanId = debtLoan.id;
            transaction.walletId = selectedWallet.id;
            transaction.currencyCode = selectedWallet.currencyCode;
            transaction.currencySymbol = selectedWallet.currencySymbol;
            transaction.exchangeRate = selectedWallet.currencyCode.equalsIgnoreCase(debtLoan.currencyCode) ? 1.0 : selectedWallet.exchangeRate;
            transaction.convertedAmount = convertedAmount;
            transaction.paymentMethod = selectedPaymentMethod;
            transaction.paymentDate = paymentDate != null ? paymentDate.getTime() : System.currentTimeMillis();
            transaction.amount = paymentAmount;
            transaction.reference = Objects.requireNonNull(etReference.getText()).toString().trim();
            transaction.notes = Objects.requireNonNull(etNotes.getText()).toString().trim();
            transaction.isSynced = false;
            transaction.isDeleted = false;
            transaction.createdAt = time;
            transaction.updatedAt = time;
            transaction.tempDebtLoanTransactionServerId = "DBT_" + time;
            transaction.debtLoanTransactionId = 0;

            debtLoan.paidAmount += convertedAmount;

            if (debtLoan.paidAmount >= debtLoan.totalAmount) {
                debtLoan.paidAmount = debtLoan.totalAmount;
                debtLoan.remainingAmount = 0;
            } else {
                debtLoan.remainingAmount = debtLoan.totalAmount - debtLoan.paidAmount;
            }

            debtLoan.updatedAt = System.currentTimeMillis();
            tvSave.setEnabled(false);
            debtLoanViewModel.saveFlexiblePaymentTransaction(transaction, debtLoan);

        } catch (Exception e) {
            AppLogger.e(getClass(), "saveFlexiblePayment", e);
        }
    }

    private void setDebtLoanStatusStyle(AppCompatTextView textView, int textColor, int backgroundColor, int strokeColor) {
        textView.setTextColor(ContextCompat.getColor(this, textColor));
        Drawable background = AppCompatResources.getDrawable(this, R.drawable.bg_badge_income);
        if (background != null) {
            background = background.mutate();
            if (background instanceof GradientDrawable drawable) {
                drawable.setColor(ContextCompat.getColor(this, backgroundColor));
                drawable.setStroke(CommonUtils.dpToPx(this, 1), ContextCompat.getColor(this, strokeColor));
            }
            textView.setBackground(background);
        }
    }

    public void openDateDialog() {
        try {
            Calendar calendar = getCalendar();

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 23);
            today.set(Calendar.MINUTE, 59);
            today.set(Calendar.SECOND, 59);
            today.set(Calendar.MILLISECOND, 999);

            // If start date is in the future, there is currently
            // no valid payment date to select.
            if (startDate > today.getTimeInMillis()) {
                Toast.makeText(this, getString(R.string.payment_date_not_available), Toast.LENGTH_SHORT).show();
                return;
            }

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.CustomDateTimePickerDialog,
                    this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            DatePicker datePicker = datePickerDialog.getDatePicker();

            // Payment date cannot be before debt start date
            if (startDate > 0) {
                datePicker.setMinDate(startDate);
            }

            // Payment date cannot be after today
            datePicker.setMaxDate(today.getTimeInMillis());

            datePickerDialog.show();

            int color = ContextCompat.getColor(this, R.color.vibrant_orange);
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(color);
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(color);

        } catch (Exception e) {
            AppLogger.e(getClass(), "openDateDialog", e);
        }
    }

    @NonNull
    private Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();

        Date date = null;

        if (selectedDateType == DATE_TYPE_PAYMENT) {
            date = paymentDate;
        }

        if (date != null) {
            calendar.setTime(date);
        }

        return calendar;
    }

    private void prepareBottomSheet() {
        // Clear focus from all EditTexts
        etReference.clearFocus();
        etNotes.clearFocus();

        // Hide keyboard
        hideKeyboard(this);
    }

    private double convertToDebtLoanCurrency(double amount, WalletEntity wallet, DebtLoanEntity debtLoan) {
        if (wallet == null || debtLoan == null || amount <= 0) {
            return 0;
        }

        if (wallet.currencyCode.equalsIgnoreCase(debtLoan.currencyCode)) {
            return amount;
        }

        return wallet.exchangeRate * amount;
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        try {
            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            Date selectedDate = calendar.getTime();

            // Payment date cannot be before start date
            if (startDate > 0 && selectedDate.getTime() < startDate) {
                Toast.makeText(this, getString(R.string.payment_date_before_start_date), Toast.LENGTH_SHORT).show();
                updateSaveButtonState();
                return;
            }

            // Payment date cannot be after today
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 23);
            today.set(Calendar.MINUTE, 59);
            today.set(Calendar.SECOND, 59);
            today.set(Calendar.MILLISECOND, 999);

            if (selectedDate.getTime() > today.getTimeInMillis()) {
                Toast.makeText(this, getString(R.string.payment_date_cannot_be_future), Toast.LENGTH_SHORT).show();
                updateSaveButtonState();
                return;
            }

            if (selectedDateType == DATE_TYPE_PAYMENT) {
                paymentDate = selectedDate;
                tvPaymentDate.setText(DateHelper.getDateFromPicker(year, month, dayOfMonth));
            }
            updateSaveButtonState();
        } catch (Exception e) {
            AppLogger.e(getClass(), "onDateSet", e);
        }
    }
}