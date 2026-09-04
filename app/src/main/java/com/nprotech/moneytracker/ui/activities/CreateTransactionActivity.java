package com.nprotech.moneytracker.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.CategoryEntity;
import com.nprotech.moneytracker.db.entites.TransactionAttachmentEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.TransactionWithDetails;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.utils.IntentUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.CategoryViewModel;
import com.nprotech.moneytracker.viewmodel.TransactionViewModel;
import com.nprotech.moneytracker.viewmodel.WalletViewModel;

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
public class CreateTransactionActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private AppCompatImageView icBack;
    private AppCompatTextView tvSave, tvTitle, tvAmount, tvDay, tvHour, tvCategory, tvFee, tvFromWallet, tvWallet, walletTitleLabel, attachmentTitleLabel;
    private AppCompatEditText etDescription, etMemo;
    private ConstraintLayout attachFileContainer;
    private ActivityResultLauncher<Intent> calculatorLauncher, categoryLauncher;
    private MaterialCardView cardAmount, cardFromWallet, cardWallet, cardFee, cardCategory;
    private MaterialButton btnIncome, btnExpense, btnTransfer;
    private MaterialButtonToggleGroup toggleTransactionType;
    private NestedScrollView scrollView;
    private RecyclerView rvAttachmentImage;
    private RecyclerViewAdapter<Uri> uriRecyclerViewAdapter;
    private Date date;
    private long transactionDate;
    private AccountEntity account;
    private WalletEntity selectedWallet, selectedFromWallet;
    private AccountViewModel accountViewModel;
    private CategoryViewModel categoryViewModel;
    private TransactionViewModel transactionViewModel;
    private WalletViewModel walletViewModel;
    private List<WalletEntity> walletLists;
    private CategoryEntity incomeCategory, expenseCategory;
    private int transactionType;
    private double transactionAmount = 0, transactionFee = 0, existingAmount = 0;
    private Uri cameraTempUri;
    private final List<Uri> selectedFileUri = new ArrayList<>();
    private final List<String> existingAttachmentPaths = new ArrayList<>();
    private String tempTransactionServerId;
    private TransactionWithDetails transactionWithDetails;
    private Typeface medium, semiBold;
    private static final String ADD_MORE_URI = "expenixo://add_more";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_transaction);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            View rootView = findViewById(R.id.rootView);
            scrollView = findViewById(R.id.scrollView);
            tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            tvSave = toolbarWrapper.findViewById(R.id.tvSave);
            icBack = toolbarWrapper.findViewById(R.id.icBack);

            walletTitleLabel = findViewById(R.id.walletTitleLabel);

            tvDay = findViewById(R.id.tvDay);
            tvHour = findViewById(R.id.tvHour);
            tvAmount = findViewById(R.id.tvAmount);
            etDescription = findViewById(R.id.etDescription);
            tvCategory = findViewById(R.id.tvCategory);
            tvFromWallet = findViewById(R.id.tvFromWallet);
            tvWallet = findViewById(R.id.tvWallet);
            tvFee = findViewById(R.id.tvFee);
            etMemo = findViewById(R.id.etMemo);
            cardFromWallet = findViewById(R.id.cardFromWallet);
            cardWallet = findViewById(R.id.cardWallet);
            cardAmount = findViewById(R.id.cardAmount);
            cardFee = findViewById(R.id.cardFee);
            cardCategory = findViewById(R.id.cardCategory);
            attachmentTitleLabel = findViewById(R.id.attachmentTitleLabel);
            attachFileContainer = findViewById(R.id.attachFileContainer);
            rvAttachmentImage = findViewById(R.id.rvAttachmentImage);
            toggleTransactionType = findViewById(R.id.toggleTransactionType);
            btnExpense = findViewById(R.id.btnExpense);
            btnIncome = findViewById(R.id.btnIncome);
            btnTransfer = findViewById(R.id.btnTransfer);

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
                tvSave.setVisibility(View.VISIBLE);

                medium = ResourcesCompat.getFont(this, R.font.exo2_medium);
                semiBold = ResourcesCompat.getFont(this, R.font.exo2_semibold);

                String action = bundle.getString("action");
                transactionType = bundle.getInt("type");

                btnIncome.setTypeface(medium);
                btnExpense.setTypeface(medium);
                btnTransfer.setTypeface(medium);

                if (Objects.equals(action, "add")) {
                    toggleTransactionType.setVisibility(View.VISIBLE);
                    switchTransMode(2);
                } else if (Objects.equals(action, "edit")) {
                    toggleTransactionType.setVisibility(View.GONE);
                    if (transactionType == 1) {
                        tvTitle.setText(getString(R.string.income));
                    } else if (transactionType == 2) {
                        tvTitle.setText(getString(R.string.expense));
                    } else {
                        tvTitle.setText(getString(R.string.transfer));
                    }
                    tempTransactionServerId = bundle.getString("transactionId", "");
                }

                accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
                categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
                transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
                walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);

                transactionDate = bundle.getLong("transactionDate", System.currentTimeMillis());

                makeReadOnly();
                setupListeners();
                setupLauncher();
                initializeAdapter();
                bindData(action != null && action.equals("edit"));
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData(boolean isEdit) {
        try {
            if (isEdit) {

                transactionWithDetails = transactionViewModel.getTransactions(tempTransactionServerId);

                account = accountViewModel.getAccountDetailById((int) transactionWithDetails.transaction.accountId);
                walletLists = accountViewModel.getWalletsByAccountId((int) transactionWithDetails.transaction.accountId);

                tvSave.setText(getString(R.string.update));
                tvSave.setEnabled(true);
                enabledSaveOption(true);

                if (transactionWithDetails != null) {
                    date = new Date(transactionWithDetails.transaction.transactionDate);
                    transactionAmount = transactionWithDetails.transaction.amount;
                    existingAmount = transactionWithDetails.transaction.amount;
                    transactionFee = transactionWithDetails.transaction.fee;

                    updateAmountText();
                    tvFee.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, transactionWithDetails.transaction.fee));
                } else {
                    updateAmountText();
                    tvFee.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, 0));
                }

                if (!walletLists.isEmpty()) {
                    selectedWallet = null;
                    for (WalletEntity wallet : walletLists) {
                        if (wallet.id == transactionWithDetails.transaction.walletId) {
                            selectedWallet = wallet;
                            break;
                        }
                    }

                    if (selectedWallet != null) {
                        tvWallet.setText(getString(R.string.wallet_info, selectedWallet.name,
                                CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol, selectedWallet.amount)));
                    }

                    // Initialize selected category
                    if (transactionType == TransactionEntity.TYPE_INCOME) {

                        if (transactionWithDetails.transaction.defaultCategoryId > 0) {
                            incomeCategory = categoryViewModel.getCategoryById(transactionWithDetails.transaction.defaultCategoryId, true);
                        } else {
                            incomeCategory = categoryViewModel.getCategoryById(transactionWithDetails.transaction.categoryId, false);
                        }

                    } else if (transactionType == TransactionEntity.TYPE_EXPENSE) {
                        if (transactionWithDetails.transaction.defaultCategoryId > 0) {
                            expenseCategory = categoryViewModel.getCategoryById(transactionWithDetails.transaction.defaultCategoryId, true);
                        } else {
                            expenseCategory = categoryViewModel.getCategoryById(transactionWithDetails.transaction.categoryId, false);
                        }
                    }
                }

                etDescription.setText(transactionWithDetails.transaction.description);
                etMemo.setText(transactionWithDetails.transaction.memo);
                tvCategory.setText(transactionWithDetails.transaction.getCategoryName(getApplicationContext()));

                List<TransactionAttachmentEntity> attachments = transactionViewModel.getTransactionAttachments(transactionWithDetails.transaction.tempTransactionServerId);

                if (attachments != null && !attachments.isEmpty()) {
                    selectedFileUri.clear();
                    existingAttachmentPaths.clear();
                    for (TransactionAttachmentEntity attachment : attachments) {
                        File file = new File(attachment.attachmentPath);
                        if (file.exists()) {
                            selectedFileUri.add(Uri.fromFile(file));
                            existingAttachmentPaths.add(file.getAbsolutePath());
                        }
                    }
                    refreshAttachmentList();
                } else {
                    selectedFileUri.clear();
                    existingAttachmentPaths.clear();
                    attachmentTitleLabel.setText(getString(R.string.attachment_title, 0));
                }
            } else {

                account = accountViewModel.getAccountDetailById((int) PreferenceManager.INSTANCE.getAccountId());
                walletLists = accountViewModel.getWalletsByAccountId((int) PreferenceManager.INSTANCE.getAccountId());

                tvSave.setEnabled(false);
                enabledSaveOption(false);

                if (transactionDate > 0) {
                    date = new Date(transactionDate);
                } else {
                    date = DateHelper.getCurrentDateTime();
                }

                tvFee.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, 0));

                if (!walletLists.isEmpty()) {
                    selectedWallet = walletLists.get(0);
                    tvWallet.setText(getString(R.string.wallet_info, selectedWallet.name,
                            CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol, selectedWallet.amount)));
                }

                updateAmountText();
                selectedFileUri.clear();
                attachmentTitleLabel.setText(getString(R.string.attachment_title, 0));
            }

            tvDay.setText(DateHelper.getFormattedDate(date));
            tvHour.setText(DateHelper.getFormattedTime(getApplicationContext(), date));
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void makeReadOnly() {
        tvAmount.setFocusable(false);
        tvAmount.setLongClickable(false);
        tvCategory.setFocusable(false);
        tvCategory.setLongClickable(false);
        tvFromWallet.setFocusable(false);
        tvFromWallet.setLongClickable(false);
        tvWallet.setFocusable(false);
        tvWallet.setLongClickable(false);
        tvFee.setFocusable(false);
        tvFee.setLongClickable(false);
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
                            ActivityUtils.overrideCloseTransition(CreateTransactionActivity.this, R.anim.scale_in, R.anim.right_to_left);
                        }
                    });

            tvDay.setOnClickListener(view -> {
                hideKeyboard(this);
                openDateDialog();
            });

            tvHour.setOnClickListener(view -> {
                hideKeyboard(this);
                openHourDialog();
            });

            toggleTransactionType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (!isChecked) return;

                if (checkedId == R.id.btnIncome) {
                    switchTransMode(1);
                    transactionType = 1;
                } else if (checkedId == R.id.btnExpense) {
                    switchTransMode(2);
                    transactionType = 2;
                } else if (checkedId == R.id.btnTransfer) {
                    switchTransMode(3);
                    transactionType = 3;
                }
            });

            // AMOUNT
            tvAmount.setOnClickListener(view -> {

                hideKeyboard(this);

                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", transactionAmount);
                intent.putExtra("type", "amount");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                calculatorLauncher.launch(intent, options);
            });

            cardAmount.setOnClickListener(view -> tvAmount.performClick());

            // WALLET
            tvWallet.setOnClickListener(view -> {
                hideKeyboard(this);
                selectWallets("to");
            });

            cardWallet.setOnClickListener(view -> tvWallet.performClick());

            // FROM WALLET
            tvFromWallet.setOnClickListener(view -> {
                hideKeyboard(this);
                selectWallets("from");
            });

            cardFromWallet.setOnClickListener(view -> tvFromWallet.performClick());

            // CATEGORY
            tvCategory.setOnClickListener(view -> {

                hideKeyboard(this);

                Intent intent = new Intent(this, CategoryPickerActivity.class);
                intent.putExtra("transactionType", transactionType);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                categoryLauncher.launch(intent, options);
            });

            cardCategory.setOnClickListener(view -> tvCategory.performClick());

            // FEE
            tvFee.setOnClickListener(view -> {

                hideKeyboard(this);

                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", transactionFee);
                intent.putExtra("type", "fee");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                calculatorLauncher.launch(intent, options);
            });

            cardFee.setOnClickListener(view -> tvFee.performClick());

            etMemo.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    scrollView.post(() -> scrollView.smoothScrollTo(0, v.getBottom()));
                }
            });

            attachFileContainer.setOnClickListener(view -> {
                hideKeyboard(this);
                showPicker();
            });

            tvSave.setOnClickListener(view -> saveTransaction());

            transactionViewModel.getDataSavedStatus().observe(this, aBoolean -> {

                tvSave.setEnabled(true);

                if (Boolean.TRUE.equals(aBoolean)) {
                    List<TransactionAttachmentEntity> attachmentEntities = new ArrayList<>();
                    if (!selectedFileUri.isEmpty()) {
                        for (Uri uri : selectedFileUri) {
                            try {
                                File file = saveFinalFile(uri, tempTransactionServerId);

                                TransactionAttachmentEntity transactionAttachment = new TransactionAttachmentEntity();
                                transactionAttachment.tempTransactionServerId = tempTransactionServerId;
                                transactionAttachment.serverId = 0;
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
                            transactionViewModel.saveTransactionAttachment(attachmentEntities);
                        }
                    }

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("newTransaction", transactionViewModel.getTransactionById(tempTransactionServerId));
                    resultIntent.putExtra("isSaved", true);
                    resultIntent.putExtra("isUpdated", false);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
            });

            transactionViewModel.getDataUpdatedStatus().observe(this, aBoolean -> {

                if (!Boolean.TRUE.equals(aBoolean)) {
                    return;
                }
                tvSave.setEnabled(true);
                saveNewAttachments();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("isSaved", false);
                resultIntent.putExtra("isUpdated", true);
                resultIntent.putExtra("tempTransactionServerId", transactionWithDetails.transaction.tempTransactionServerId);
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    public void openDateDialog() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.CustomDateTimePickerDialog, this, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
        int color = ContextCompat.getColor(this, R.color.vibrant_orange);
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(color);
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(color);
    }

    public void openHourDialog() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this.date);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, R.style.CustomDateTimePickerDialog, this, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(getApplicationContext()));
        timePickerDialog.show();
        int color = ContextCompat.getColor(this, R.color.vibrant_orange);
        timePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(color);
        timePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(color);
    }

    private void switchTransMode(int mode) {
        try {
            if (mode == 1) {

                tvTitle.setText(getString(R.string.income));

                btnIncome.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.light_income));
                btnIncome.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.income));
                btnIncome.setTypeface(semiBold);

                btnExpense.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                btnExpense.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.text_grey));
                btnExpense.setTypeface(medium);
                btnTransfer.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                btnTransfer.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.text_grey));
                btnTransfer.setTypeface(medium);

                cardFee.setVisibility(View.GONE);
                cardFromWallet.setVisibility(View.GONE);
                cardCategory.setVisibility(View.VISIBLE);

                walletTitleLabel.setText(getString(R.string.wallet));
                if (incomeCategory != null) {
                    tvCategory.setText(incomeCategory.getName(getApplicationContext()));
                } else {
                    tvCategory.setText("");
                }
            } else if (mode == 2) {

                tvTitle.setText(getString(R.string.expense));

                btnExpense.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.light_expense));
                btnExpense.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.expense));
                btnExpense.setTypeface(semiBold);

                btnIncome.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                btnIncome.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.text_grey));
                btnIncome.setTypeface(medium);
                btnTransfer.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                btnTransfer.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.text_grey));
                btnTransfer.setTypeface(medium);

                cardFee.setVisibility(View.GONE);
                cardFromWallet.setVisibility(View.GONE);
                cardCategory.setVisibility(View.VISIBLE);

                walletTitleLabel.setText(getString(R.string.wallet));
                if (expenseCategory != null) {
                    tvCategory.setText(expenseCategory.getName(getApplicationContext()));
                } else {
                    tvCategory.setText("");
                }
            } else if (mode == 3) {

                tvTitle.setText(getString(R.string.transfer));

                btnTransfer.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.light_transfer));
                btnTransfer.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.transfer));
                btnTransfer.setTypeface(semiBold);

                btnExpense.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                btnExpense.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.text_grey));
                btnExpense.setTypeface(medium);
                btnIncome.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                btnIncome.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.text_grey));
                btnIncome.setTypeface(medium);

                cardFee.setVisibility(View.VISIBLE);
                cardFromWallet.setVisibility(View.VISIBLE);
                cardCategory.setVisibility(View.GONE);

                walletTitleLabel.setText(getString(R.string.to_wallet));
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "switchTransMode", e);
        }
    }

    private void setupLauncher() {
        calculatorLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            double amount = data.getDoubleExtra("amount", 0);
                            String type = data.getStringExtra("type");

                            if (type != null && type.equalsIgnoreCase("amount")) {
                                transactionAmount = amount;
                                updateAmountText();
                            } else if (type != null && type.equalsIgnoreCase("fee")) {
                                transactionFee = amount;
                                tvFee.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, amount));
                            }
                            updateSaveButtonState();
                        }
                    }
                });

        categoryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {

                            CategoryEntity categoryEntity = IntentUtils.getSerializableExtra(data, "category", CategoryEntity.class);
                            if (categoryEntity != null) {
                                if (transactionType == 1) {
                                    incomeCategory = categoryEntity;
                                } else {
                                    expenseCategory = categoryEntity;
                                }

                                tvCategory.setText(categoryEntity.getName(getApplicationContext()));
                                updateSaveButtonState();
                            }
                        }
                    }
                });
    }

    private void selectWallets(String type) {
        try {

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_wallet_picker_layout, findViewById(android.R.id.content), false);
            RecyclerView rvWallets = bottomView.findViewById(R.id.rvWallets);
            View viewLine = bottomView.findViewById(R.id.viewLine);
            LinearLayout layoutAddWallet = bottomView.findViewById(R.id.layoutAddWallet);
            viewLine.setVisibility(View.GONE);
            layoutAddWallet.setVisibility(View.GONE);

            RecyclerViewAdapter<WalletEntity> adapter = new RecyclerViewAdapter<>(getApplicationContext(), walletLists, R.layout.item_switch_accounts) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, WalletEntity walletEntity) {

                    holder.setViewText(R.id.tvAccountName, walletEntity.name);

                    holder.setViewText(R.id.tvAccountBalance, getString(R.string.account_balance_format,
                            CommonUtils.getBeautifyAmount(walletEntity.currencySymbol, walletEntity.amount)));

                    if (type.equalsIgnoreCase("to")) {
                        holder.getView(R.id.ivSelected).setVisibility(selectedWallet.id == walletEntity.id ? View.VISIBLE : View.GONE);

                        holder.getView(R.id.rlAccountView).setOnClickListener(v -> {
                            selectedWallet = walletEntity;

                            tvWallet.setText(getString(R.string.wallet_info, selectedWallet.name,
                                    CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol, selectedWallet.amount)));

                            updateAmountText();
                            updateSaveButtonState();
                            dialog.dismiss();
                        });
                    } else if (type.equalsIgnoreCase("from")) {

                        if (selectedFromWallet != null) {
                            holder.getView(R.id.ivSelected).setVisibility(selectedFromWallet.id == walletEntity.id ? View.VISIBLE : View.GONE);
                        }

                        holder.getView(R.id.rlAccountView).setOnClickListener(v -> {
                            selectedFromWallet = walletEntity;
                            tvFromWallet.setText(getString(R.string.wallet_info, selectedFromWallet.name,
                                    CommonUtils.getBeautifyAmount(selectedFromWallet.currencySymbol, selectedFromWallet.amount)));
                            updateSaveButtonState();
                            dialog.dismiss();
                        });
                    }
                }
            };

            rvWallets.setAdapter(adapter);
            rvWallets.setHasFixedSize(true);
            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "switchAccounts", e);
        }
    }

    private void updateSaveButtonState() {

        boolean enabled = transactionAmount > 0 && selectedWallet != null;

        switch (transactionType) {

            case TransactionEntity.TYPE_INCOME:
                enabled &= incomeCategory != null;
                break;

            case TransactionEntity.TYPE_EXPENSE:
                enabled &= expenseCategory != null;
                break;

            case TransactionEntity.TYPE_TRANSFER:
                enabled &= selectedFromWallet != null
                        && Objects.requireNonNull(selectedWallet).id != selectedFromWallet.id;
                break;
        }

        tvSave.setEnabled(enabled);
        enabledSaveOption(enabled);
    }

    private void enabledSaveOption(boolean isEnabled) {
        try {
            tvSave.setAlpha(isEnabled ? 1.0f : 0.5f); // Optional: make disabled state visible
        } catch (Exception e) {
            AppLogger.e(getClass(), "enabledSaveOption", e);
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
                                hideKeyboard(CreateTransactionActivity.this);
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
                        String fileName = CommonUtils.getFileName(uri, CreateTransactionActivity.this);
                        if (TextUtils.isEmpty(fileName)) {
                            fileName = "attachment";
                        }
                        tvAttachmentName.setText(fileName);
                        tvAttachmentName.setSelected(true);

                        // =========================================
                        // FILE SIZE
                        // =========================================
                        long fileSize = CommonUtils.getFileSize(uri, CreateTransactionActivity.this);
                        tvAttachmentSize.setText(Formatter.formatFileSize(CreateTransactionActivity.this, fileSize));

                        // =========================================
                        // DELETE
                        // =========================================
                        deleteButton.setOnClickListener(v -> {
                            int position = selectedFileUri.indexOf(uri);
                            if (position != -1) {
                                boolean isExistingAttachment = "file".equalsIgnoreCase(uri.getScheme()) && existingAttachmentPaths.contains(uri.getPath());
                                selectedFileUri.remove(position);
                                if (isExistingAttachment) {
                                    transactionViewModel.deleteAttachment(uri.getPath(), transactionWithDetails.transaction.tempTransactionServerId);
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

    private File saveFinalFile(Uri uri, String transactionId) throws Exception {

        File dir = new File(getFilesDir(), "uploads" + File.separator + transactionId);

        if (!dir.exists() && !dir.mkdirs()) {
            AppLogger.w(getClass(), "Failed to create transaction folder");
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

        File outFile = new File(dir, "FILE_" + UUID.randomUUID() + extension);

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

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera(); // 🔥 permission granted → open camera
                } else {
                    Toast.makeText(this, getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show();
                }
            });

    private void saveTransaction() {
        try {
            tvSave.setEnabled(false);
            switch (transactionType) {
                case 1:
                    saveIncomeTransaction();
                    break;
                case 2:
                    saveExpenseTransaction();
                    break;
                case 3:
                    saveTransferTransaction();
                    break;
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "saveTransaction", e);
        }
    }

    private void saveIncomeTransaction() {

        TransactionEntity transaction = buildTransaction();

        transaction.type = TransactionEntity.TYPE_INCOME;
        transaction.categoryId = incomeCategory.id;
        transaction.defaultCategoryId = incomeCategory.defaultCategory;

        WalletEntity wallet = walletViewModel.getWalletByWalletId(selectedWallet.id);

        if (transactionWithDetails != null) {

            // -----------------------------
            // EDIT EXISTING TRANSACTION
            // -----------------------------

            double oldAmount = existingAmount;
            double exchangeRate = 1;

            if (wallet != null) {
                // Undo old income
                wallet.amount -= oldAmount;

                // Apply new income
                wallet.amount += transactionAmount;

                exchangeRate = wallet.exchangeRate;
            }

            if (account != null) {
                // Undo old income
                account.balance -= (oldAmount * exchangeRate);

                // Apply new income
                account.balance += (transactionAmount * exchangeRate);
            }

            transactionViewModel.updateTransaction(transaction, wallet, account);

        } else {

            // -----------------------------
            // NEW TRANSACTION
            // -----------------------------

            double exchangeRate = 1;

            if (wallet != null) {
                wallet.amount += transactionAmount;
                exchangeRate = wallet.exchangeRate;
            }

            if (account != null) {
                account.balance += (transactionAmount * exchangeRate);
            }

            transactionViewModel.saveTransaction(transaction, wallet, account);
        }
    }

    private void saveExpenseTransaction() {

        TransactionEntity transaction = buildTransaction();

        transaction.type = TransactionEntity.TYPE_EXPENSE;
        transaction.categoryId = expenseCategory.id;
        transaction.defaultCategoryId = expenseCategory.defaultCategory;

        WalletEntity wallet = walletViewModel.getWalletByWalletId(selectedWallet.id);

        if (transactionWithDetails != null) {
            // -----------------------------
            // EDIT EXISTING TRANSACTION
            // -----------------------------

            double oldAmount = existingAmount;
            double exchangeRate = 1;

            if (wallet != null) {
                // Undo old expense
                wallet.amount += oldAmount;

                // Apply new expense
                wallet.amount -= transactionAmount;
                exchangeRate = wallet.exchangeRate;
            }

            if (account != null) {
                // Undo old expense
                account.balance += (oldAmount * exchangeRate);

                // Apply new expense
                account.balance -= (transactionAmount * exchangeRate);
            }

            transactionViewModel.updateTransaction(transaction, wallet, account);
        } else {
            // -----------------------------
            // NEW TRANSACTION
            // -----------------------------

            double exchangeRate = 1;
            if (wallet != null) {
                wallet.amount -= transactionAmount;
                exchangeRate = wallet.exchangeRate;
            }

            if (account != null) {
                account.balance -= (transactionAmount * exchangeRate);
            }

            transactionViewModel.saveTransaction(transaction, wallet, account);
        }
    }

    private void saveTransferTransaction() {

        TransactionEntity transferTransaction = buildTransaction();

        CategoryEntity transferCategory = getTransferCategoryId(Constants.DEFAULT_CATEGORY_TRANSFER_ID);

        transferTransaction.type = TransactionEntity.TYPE_TRANSFER;
        transferTransaction.fromWalletId = selectedFromWallet.id;
        transferTransaction.categoryId = transferCategory.id;
        transferTransaction.defaultCategoryId = transferCategory.defaultCategory;

        WalletEntity fromWallet = walletViewModel.getWalletByWalletId(selectedFromWallet.id);
        WalletEntity toWallet = walletViewModel.getWalletByWalletId(selectedWallet.id);

        TransactionEntity feeTransaction = null;
        TransactionEntity oldFeeTransaction;

        if (transactionWithDetails != null) {

            // ------------------------
            // EDIT TRANSFER
            // ------------------------

            WalletEntity oldFromWallet =
                    walletViewModel.getWalletByWalletId(transactionWithDetails.transaction.fromWalletId);

            WalletEntity oldToWallet =
                    walletViewModel.getWalletByWalletId(transactionWithDetails.transaction.walletId);

            // Undo old transfer

            double oldAccountAmount = existingAmount * oldFromWallet.exchangeRate;
            double oldConvertedAmount = oldAccountAmount / oldToWallet.exchangeRate;

            oldFromWallet.amount += existingAmount;
            oldToWallet.amount -= oldConvertedAmount;

            if (!oldFromWallet.isExclude && oldToWallet.isExclude) {
                account.balance += existingAmount;
            } else if (oldFromWallet.isExclude && !oldToWallet.isExclude) {
                account.balance -= existingAmount;
            }

            // Undo old fee
            oldFeeTransaction = transactionViewModel.getFeeTransaction(transactionWithDetails.transaction.tempTransactionServerId);

            if (oldFeeTransaction != null) {

                oldFromWallet.amount += oldFeeTransaction.amount;

                if (!oldFromWallet.isExclude) {
                    account.balance += (oldFeeTransaction.amount * oldFromWallet.exchangeRate);
                }
            }

            // Apply new transfer
            double accountAmount = transactionAmount * fromWallet.exchangeRate;
            double convertedAmount = accountAmount / toWallet.exchangeRate;

            fromWallet.amount -= transactionAmount;
            toWallet.amount += convertedAmount;

            if (!fromWallet.isExclude && toWallet.isExclude) {
                account.balance -= accountAmount;
            } else if (fromWallet.isExclude && !toWallet.isExclude) {
                account.balance += accountAmount;
            }

            // Apply new fee
            if (transactionFee > 0) {

                long currentTime = System.currentTimeMillis();

                fromWallet.amount -= transactionFee;

                if (!fromWallet.isExclude) {
                    account.balance -= (transactionFee * fromWallet.exchangeRate);
                }

                if (oldFeeTransaction != null) {

                    feeTransaction = oldFeeTransaction;

                    feeTransaction.walletId = fromWallet.id;
                    feeTransaction.fromWalletId = fromWallet.id;
                    feeTransaction.amount = transactionFee;
                    feeTransaction.transactionDate = transferTransaction.transactionDate;

                } else {

                    feeTransaction = new TransactionEntity();

                    CategoryEntity category = getTransferCategoryId(Constants.DEFAULT_CATEGORY_FEE_ID);

                    feeTransaction.serverId = 0;
                    feeTransaction.tempTransactionServerId = "T_" + currentTime + "_FEE";
                    feeTransaction.accountId = account.id;
                    feeTransaction.walletId = fromWallet.id;
                    feeTransaction.fromWalletId = fromWallet.id;
                    feeTransaction.type = TransactionEntity.TYPE_EXPENSE;
                    feeTransaction.amount = transactionFee;
                    feeTransaction.fee = 0;
                    feeTransaction.categoryId = category.id;
                    feeTransaction.defaultCategoryId = category.defaultCategory;
                    feeTransaction.transactionDate = transferTransaction.transactionDate;
                    feeTransaction.description = getString(R.string.fee);
                    feeTransaction.memo = "";
                    feeTransaction.parentTransactionId =
                            transferTransaction.tempTransactionServerId;
                    feeTransaction.createdAt = currentTime;
                }
                feeTransaction.updatedAt = currentTime;
            }

            transactionViewModel.updateTransferTransaction(transferTransaction, feeTransaction, oldFeeTransaction, oldFromWallet, oldToWallet, fromWallet,
                    toWallet, account);

        } else {

            // ------------------------
            // NEW TRANSFER
            // ------------------------

            double accountAmount = transactionAmount * fromWallet.exchangeRate;
            double convertedAmount = accountAmount / toWallet.exchangeRate;

            fromWallet.amount -= transactionAmount;
            toWallet.amount += convertedAmount;

            if (!fromWallet.isExclude && toWallet.isExclude) {
                account.balance -= accountAmount;
            } else if (fromWallet.isExclude && !toWallet.isExclude) {
                account.balance += accountAmount;
            }

            if (transactionFee > 0) {
                long currentTime = System.currentTimeMillis();
                CategoryEntity category = getTransferCategoryId(Constants.DEFAULT_CATEGORY_FEE_ID);

                fromWallet.amount -= transactionFee;

                if (!fromWallet.isExclude) {
                    account.balance -= (transactionFee * fromWallet.exchangeRate);
                }

                feeTransaction = new TransactionEntity();

                feeTransaction.serverId = 0;
                feeTransaction.tempTransactionServerId = "T_" + currentTime + "_FEE";
                feeTransaction.accountId = account.id;
                feeTransaction.walletId = fromWallet.id;
                feeTransaction.fromWalletId = fromWallet.id;
                feeTransaction.type = TransactionEntity.TYPE_EXPENSE;
                feeTransaction.amount = transactionFee;
                feeTransaction.categoryId = category.id;
                feeTransaction.defaultCategoryId = category.defaultCategory;
                feeTransaction.transactionDate = transferTransaction.transactionDate;
                feeTransaction.description = getString(R.string.fee);
                feeTransaction.parentTransactionId = transferTransaction.tempTransactionServerId;
                feeTransaction.createdAt = currentTime;
                feeTransaction.updatedAt = currentTime;
            }

            transactionViewModel.saveTransferTransaction(transferTransaction, feeTransaction, fromWallet, toWallet, account);
        }
    }

    private CategoryEntity getTransferCategoryId(int categoryId) {
        return categoryViewModel.getDefaultCategoryByType(categoryId, TransactionEntity.TYPE_TRANSFER);
    }

    @NonNull
    private TransactionEntity buildTransaction() {

        TransactionEntity transaction;

        long currentTime = System.currentTimeMillis();

        if (transactionWithDetails != null) {
            // Editing existing transaction
            transaction = transactionWithDetails.transaction;
            transaction.updatedAt = currentTime;
        } else {
            // Creating new transaction
            transaction = new TransactionEntity();

            transaction.serverId = 0;
            transaction.createdAt = currentTime;
            transaction.updatedAt = currentTime;
            transaction.isSynced = false;
            transaction.isDeleted = false;

            tempTransactionServerId = "T_" + currentTime;
            transaction.tempTransactionServerId = tempTransactionServerId;
        }

        transaction.accountId = account.id;
        transaction.walletId = selectedWallet.id;
        transaction.amount = transactionAmount;
        transaction.fee = transactionFee;
        transaction.transactionDate = date.getTime();
        transaction.description = Objects.requireNonNull(etDescription.getText()).toString().trim();
        transaction.memo = Objects.requireNonNull(etMemo.getText()).toString().trim();

        return transaction;
    }

    private void updateAmountText() {
        String symbol = selectedWallet != null ? selectedWallet.currencySymbol : account.currencySymbol;
        tvAmount.setText(CommonUtils.getBeautifyAmount(symbol, transactionAmount));
    }

    private void saveNewAttachments() {
        try {

            if (selectedFileUri.isEmpty()) {
                return;
            }

            List<TransactionAttachmentEntity> attachmentEntities = new ArrayList<>();

            for (Uri uri : selectedFileUri) {
                if ("file".equalsIgnoreCase(uri.getScheme()) && existingAttachmentPaths.contains(uri.getPath())) {
                    continue;
                }

                File file = saveFinalFile(uri, tempTransactionServerId);
                TransactionAttachmentEntity attachment = new TransactionAttachmentEntity();
                attachment.tempTransactionServerId = tempTransactionServerId;
                attachment.serverId = 0;
                attachment.attachmentPath = file.getAbsolutePath();
                attachment.attachmentName = CommonUtils.getFileName(uri, this);
                attachment.attachmentExtension = CommonUtils.getFileExtension(attachment.attachmentName);
                attachment.attachmentSize = file.length();
                attachment.createdAt = System.currentTimeMillis();
                attachment.updatedAt = System.currentTimeMillis();
                attachmentEntities.add(attachment);
            }

            if (!attachmentEntities.isEmpty()) {
                transactionViewModel.saveTransactionAttachment(attachmentEntities);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "saveNewAttachments", e);
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        tvDay.setText(DateHelper.getDateFromPicker(i, i1, i2));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(1, i);
        calendar.set(2, i1);
        calendar.set(5, i2);
        this.date = calendar.getTime();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        tvHour.setText(DateHelper.getTimeFromPicker(this, i, i1));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(11, i);
        calendar.set(12, i1);
        this.date = calendar.getTime();
    }
}