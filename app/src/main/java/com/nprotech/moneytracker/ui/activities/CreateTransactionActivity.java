package com.nprotech.moneytracker.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.text.format.DateFormat;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.nprotech.moneytracker.R;
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
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateTransactionActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private AppCompatImageView icBack, noteImage;
    private AppCompatTextView tvSave, tvTitle, tvAmount, incomeLabel, expenseLabel, transferLabel, tvDay, tvHour, tvCategory, tvFee, tvFromWallet, tvWallet, walletLabel;
    private AppCompatEditText etDescription, etMemo;
    private ActivityResultLauncher<Intent> calculatorLauncher, categoryLauncher;
    private ConstraintLayout incomeWrapper, expenseWrapper, transferWrapper, clFromWallet, clFee, clCategory;
    private NestedScrollView scrollView;
    private RecyclerView rvNoteImage;
    private RecyclerViewAdapter<Uri> uriRecyclerViewAdapter;
    private Date date;
    private AccountEntity account;
    private WalletEntity selectedWallet, selectedFromWallet;
    private AccountViewModel accountViewModel;
    private CategoryViewModel categoryViewModel;
    private TransactionViewModel transactionViewModel;
    private WalletViewModel walletViewModel;
    private List<WalletEntity> walletLists;
    private CategoryEntity incomeCategory, expenseCategory;
    private int transactionType;
    private double transactionAmount = 0, transactionFee = 0;
    private Uri cameraTempUri;
    private final List<Uri> selectedFileUri = new ArrayList<>();
    private String tempTransactionServerId;
    private TransactionWithDetails transactionWithDetails;

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
            scrollView = findViewById(R.id.scrollView);
            tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            tvSave = toolbarWrapper.findViewById(R.id.tvSave);
            icBack = toolbarWrapper.findViewById(R.id.icBack);

            ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout);
            incomeWrapper = findViewById(R.id.incomeWrapper);
            expenseWrapper = findViewById(R.id.expenseWrapper);
            transferWrapper = findViewById(R.id.transferWrapper);
            incomeLabel = findViewById(R.id.incomeLabel);
            expenseLabel = findViewById(R.id.expenseLabel);
            transferLabel = findViewById(R.id.transferLabel);
            walletLabel = findViewById(R.id.walletLabel);

            tvDay = findViewById(R.id.tvDay);
            tvHour = findViewById(R.id.tvHour);
            tvAmount = findViewById(R.id.tvAmount);
            etDescription = findViewById(R.id.etDescription);
            tvCategory = findViewById(R.id.tvCategory);
            tvFromWallet = findViewById(R.id.tvFromWallet);
            tvWallet = findViewById(R.id.tvWallet);
            tvFee = findViewById(R.id.tvFee);
            etMemo = findViewById(R.id.etMemo);
            clCategory = findViewById(R.id.clCategory);
            clFromWallet = findViewById(R.id.clFromWallet);
            clFee = findViewById(R.id.clFee);
            noteImage = findViewById(R.id.noteImage);
            rvNoteImage = findViewById(R.id.rvNoteImage);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(scrollView, (view, insets) -> {
                Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
                view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), imeInsets.bottom);
                return insets;
            });

            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {
                tvSave.setVisibility(View.VISIBLE);

                String action = bundle.getString("action");
                transactionType = bundle.getInt("type");

                if (Objects.equals(action, "add")) {
                    switchTransMode(transactionType);
                } else if (Objects.equals(action, "edit")) {
                    constraintLayout.setVisibility(View.GONE);

                    if (transactionType == 1) {
                        tvTitle.setText(getString(R.string.income));
                    } else if (transactionType == 2) {
                        tvTitle.setText(getString(R.string.expense));
                    } else {
                        tvTitle.setText(getString(R.string.transfer));
                    }

                    transactionWithDetails = IntentUtils.getSerializableExtra(getIntent(), "transactionDetail", TransactionWithDetails.class);
                }

                accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
                categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
                transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
                walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);

                backPressed();
                makeReadOnly();
                setupListeners();
                setupLauncher();
                initializeAdapter();
                bindData(action != null && action.equals("edit"));
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData(boolean isEdit) {
        try {
            if (isEdit) {

                account = accountViewModel.getAccountDetailById((int) transactionWithDetails.transaction.accountId);
                walletLists = accountViewModel.getWalletsByAccountId((int) transactionWithDetails.transaction.accountId);

                tvSave.setText(getString(R.string.update));
                tvSave.setEnabled(true);
                enabledSaveOption(true);

                if (transactionWithDetails != null) {
                    date = new Date(transactionWithDetails.transaction.transactionDate);
                    transactionAmount = transactionWithDetails.transaction.amount;
                    transactionFee = transactionWithDetails.transaction.fee;

                    tvAmount.setText(CommonUtils.getBeautifyAmount(transactionWithDetails.currencySymbol, transactionWithDetails.transaction.amount));
                    tvFee.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, transactionWithDetails.transaction.fee));
                } else {
                    tvAmount.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, 0));
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
                    for (TransactionAttachmentEntity attachment : attachments) {
                        File file = new File(attachment.attachmentPath);
                        if (file.exists()) {
                            selectedFileUri.add(Uri.fromFile(file));
                        }
                    }
                    showPreviewFromUri(selectedFileUri);
                }
            } else {

                account = accountViewModel.getAccountDetailById((int) PreferenceManager.INSTANCE.getAccountId());
                walletLists = accountViewModel.getWalletsByAccountId((int) PreferenceManager.INSTANCE.getAccountId());

                tvSave.setEnabled(false);
                enabledSaveOption(false);

                date = DateHelper.getCurrentDateTime();

                tvAmount.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, 0));
                tvFee.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, 0));

                if (!walletLists.isEmpty()) {
                    selectedWallet = walletLists.get(0);
                    tvWallet.setText(getString(R.string.wallet_info, selectedWallet.name,
                            CommonUtils.getBeautifyAmount(selectedWallet.currencySymbol, selectedWallet.amount)));
                }
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
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.bottom_to_top);
            });

            tvDay.setOnClickListener(view -> {
                hideKeyboard(this);
                openDateDialog();
            });

            tvHour.setOnClickListener(view -> {
                hideKeyboard(this);
                openHourDialog();
            });

            tvAmount.setOnClickListener(view -> {

                hideKeyboard(this);

                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", transactionAmount);
                intent.putExtra("type", "amount");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                calculatorLauncher.launch(intent, options);
            });

            tvWallet.setOnClickListener(view -> {
                hideKeyboard(this);
                selectWallets("to");
            });

            tvFromWallet.setOnClickListener(view -> {
                hideKeyboard(this);
                selectWallets("from");
            });

            incomeWrapper.setOnClickListener(view -> {
                switchTransMode(1);
                transactionType = 1;
            });

            expenseWrapper.setOnClickListener(view -> {
                switchTransMode(2);
                transactionType = 2;
            });

            transferWrapper.setOnClickListener(view -> {
                switchTransMode(3);
                transactionType = 3;
            });

            tvCategory.setOnClickListener(view -> {

                hideKeyboard(this);

                Intent intent = new Intent(this, CategoryPickerActivity.class);
                intent.putExtra("transactionType", transactionType);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                categoryLauncher.launch(intent, options);
            });

            tvFee.setOnClickListener(view -> {

                hideKeyboard(this);

                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", transactionFee);
                intent.putExtra("type", "fee");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                calculatorLauncher.launch(intent, options);
            });

            etMemo.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    scrollView.post(() -> scrollView.smoothScrollTo(0, v.getBottom()));
                }
            });

            noteImage.setOnClickListener(view -> {
                hideKeyboard(this);
                showPicker();
            });

            tvSave.setOnClickListener(view -> saveTransaction());

            transactionViewModel.getDataSavedStatus().observe(this, aBoolean -> {
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

                Intent resultIntent = new Intent();
                resultIntent.putExtra("isSaved", false);
                resultIntent.putExtra("isUpdated", true);
                resultIntent.putExtra("tempTransactionServerId", transactionWithDetails.transaction.tempTransactionServerId);
                setResult(Activity.RESULT_OK, resultIntent);
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

                incomeWrapper.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.azure_blue));
                expenseWrapper.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.vibrant_orange));
                transferWrapper.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.vibrant_orange));

                incomeLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                expenseLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                transferLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));

                clFee.setVisibility(View.GONE);
                clFromWallet.setVisibility(View.GONE);
                clCategory.setVisibility(View.VISIBLE);

                walletLabel.setText(getString(R.string.wallet));
                if (incomeCategory != null) {
                    tvCategory.setText(incomeCategory.getName(getApplicationContext()));
                } else {
                    tvCategory.setText("");
                }
            } else if (mode == 2) {

                tvTitle.setText(getString(R.string.expense));

                incomeWrapper.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.vibrant_orange));
                expenseWrapper.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.bright_red));
                transferWrapper.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.vibrant_orange));

                incomeLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                expenseLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                transferLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));

                clFee.setVisibility(View.GONE);
                clFromWallet.setVisibility(View.GONE);
                clCategory.setVisibility(View.VISIBLE);

                walletLabel.setText(getString(R.string.wallet));
                if (expenseCategory != null) {
                    tvCategory.setText(expenseCategory.getName(getApplicationContext()));
                } else {
                    tvCategory.setText("");
                }
            } else if (mode == 3) {

                tvTitle.setText(getString(R.string.transfer));

                incomeWrapper.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.vibrant_orange));
                expenseWrapper.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.vibrant_orange));
                transferWrapper.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_grey));

                incomeLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                expenseLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                transferLabel.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));

                clFee.setVisibility(View.VISIBLE);
                clFromWallet.setVisibility(View.VISIBLE);
                clCategory.setVisibility(View.GONE);

                walletLabel.setText(getString(R.string.to_wallet));
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
                                tvAmount.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, amount));
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
                            updateSaveButtonState();
                            dialog.dismiss();
                        });
                    } else if (type.equalsIgnoreCase("from")) {

                        if (selectedFromWallet != null) {
                            holder.getView(R.id.ivSelected).setVisibility(selectedFromWallet.id == walletEntity.id ? View.VISIBLE : View.GONE);
                        }

                        holder.getView(R.id.rlAccountView).setOnClickListener(v -> {
                            selectedFromWallet = walletEntity;
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

        boolean enabled = transactionAmount > 0
                && selectedWallet != null;

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
        uriRecyclerViewAdapter = new RecyclerViewAdapter<>(this, selectedFileUri, R.layout.item_transaction_images) {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onPostBindViewHolder(ViewHolder holder, Uri uri) {

                AppCompatImageView ivImage = holder.getView(R.id.ivNotePhoto);

                String mime = getContentResolver().getType(uri);

                if (mime == null) {
                    String path = uri.getPath();
                    String extension = MimeTypeMap.getFileExtensionFromUrl(path);

                    if (!TextUtils.isEmpty(extension)) {
                        mime = MimeTypeMap.getSingleton()
                                .getMimeTypeFromExtension(extension.toLowerCase());
                    }
                }

                if (mime != null && mime.startsWith("image")) {
                    Glide.with(ivImage.getContext())
                            .load(uri)
                            .into(ivImage);
                } else {
                    ivImage.setImageResource(getFileIconFromUri(uri));
                }

                holder.getView(R.id.cardDelete).setOnClickListener(v -> {
                    selectedFileUri.remove(uri);
                    deleteAttachment(uri, transactionWithDetails != null);
                    notifyDataSetChanged();
                    noteImage.setVisibility(selectedFileUri.size() == 5 ? View.GONE : View.VISIBLE);
                });
            }
        };

        rvNoteImage.setAdapter(uriRecyclerViewAdapter);
        rvNoteImage.setHasFixedSize(true);
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
        galleryLauncher.launch(intent);
    }

    private void openFileManager() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        fileLauncher.launch(intent);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showPreviewFromUri(List<Uri> fileUris) {

        if (fileUris == null || fileUris.isEmpty()) {
            rvNoteImage.setVisibility(View.GONE);
            return;
        }

        rvNoteImage.setVisibility(View.VISIBLE);
        uriRecyclerViewAdapter.notifyDataSetChanged();

        if (selectedFileUri.size() == 5) {
            noteImage.setVisibility(View.GONE);
        } else {
            noteImage.setVisibility(View.VISIBLE);
        }
    }

    private int getFileIconFromUri(Uri uri) {

        String name = getFileNameFromUri(uri).toLowerCase();

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

    private String getFileNameFromUri(Uri uri) {
        String result = null;

        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver()
                    .query(uri, null, null, null, null)) {

                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                AppLogger.e(getClass(), "getFileNameFromUri", e);
            }
        }

        if (result == null) {
            result = uri.getLastPathSegment();
        }

        return result;
    }

    private void deleteCameraTempFile(Uri uri) {
        try {

            if (uri != null) {
                File tempFile = new File(Objects.requireNonNull(uri.getPath()));
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        AppLogger.d(getClass(), "Camera temp file deleted");
                    }
                }
            } else {
                if (cameraTempUri != null) {
                    File tempFile = new File(Objects.requireNonNull(cameraTempUri.getPath()));
                    if (tempFile.exists()) {
                        if (tempFile.delete()) {
                            AppLogger.d(getClass(), "Camera temp file deleted");
                        }
                    }
                }
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteCameraTempFile", e);
        }
    }

    private void deleteAttachment(Uri uri, boolean isEdit) {

        if (isEdit) {
            // 1. Delete from database
            transactionViewModel.deleteAttachment(uri.getPath(), transactionWithDetails.transaction.tempTransactionServerId);

            // 2. Delete local file
            deleteLocalFile(uri.getPath());
        } else {
            // Temporary camera/gallery file
            deleteCameraTempFile(uri);
        }
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

        // uploads/{transactionId}/
        File dir = new File(getFilesDir(), "uploads" + File.separator + transactionId);

        if (!dir.exists() && !dir.mkdirs()) {
            AppLogger.e(getClass(), "Failed to create transaction folder", null);
        }

        String name = getFileNameFromUri(uri);
        String extension = name.contains(".") ? name.substring(name.lastIndexOf(".")) : ".bin";

        File outFile = new File(dir, "FILE_" + System.currentTimeMillis() + extension);

        String mime = getContentResolver().getType(uri);

        if (mime != null && mime.startsWith("image")) {
            return compressImageKeepResolution(uri, outFile);
        } else {
            return copyUriToFile(uri, outFile);
        }
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

    ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {

                    selectedFileUri.add(cameraTempUri);
                    showPreviewFromUri(selectedFileUri);
                }
            });

    ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedFileUri.add(result.getData().getData());
                    showPreviewFromUri(selectedFileUri);
                }
            });

    ActivityResultLauncher<Intent> fileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedFileUri.add(result.getData().getData());
                    showPreviewFromUri(selectedFileUri);
                }
            });

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
        if (wallet != null) {
            wallet.amount = wallet.amount + transactionAmount;
        }

        if (account != null) {
            account.balance = account.balance + transactionAmount;
        }

        transactionViewModel.saveTransaction(transaction, wallet, account);
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

            double oldAmount = transactionWithDetails.transaction.amount;

            if (wallet != null) {
                // Undo old expense
                wallet.amount += oldAmount;

                // Apply new expense
                wallet.amount -= transactionAmount;
            }

            if (account != null) {
                // Undo old expense
                account.balance += oldAmount;

                // Apply new expense
                account.balance -= transactionAmount;
            }

            transactionViewModel.updateTransaction(transaction, wallet, account);
        } else {
            // -----------------------------
            // NEW TRANSACTION
            // -----------------------------

            if (wallet != null) {
                wallet.amount -= transactionAmount;
            }

            if (account != null) {
                account.balance -= transactionAmount;
            }

            transactionViewModel.saveTransaction(transaction, wallet, account);
        }
    }

    private void saveTransferTransaction() {

        TransactionEntity transaction = buildTransaction();

        transaction.type = TransactionEntity.TYPE_TRANSFER;
        transaction.fromWalletId = selectedFromWallet.id;
        transaction.categoryId = null;
        transaction.defaultCategoryId = null;

        transactionViewModel.saveTransaction(transaction, null, null);
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

    private void backPressed() {
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        finish();
                        ActivityUtils.overrideCloseTransition(CreateTransactionActivity.this, R.anim.scale_in, R.anim.bottom_to_top);
                    }
                });
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        tvDay.setText(DateHelper.getDateFromPicker(getApplicationContext(), i, i1, i2));
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