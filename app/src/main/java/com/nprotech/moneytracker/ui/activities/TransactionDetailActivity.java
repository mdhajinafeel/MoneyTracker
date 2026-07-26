package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.models.TransactionWithDetails;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.utils.IntentUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.TransactionViewModel;
import com.nprotech.moneytracker.viewmodel.WalletViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransactionDetailActivity extends BaseActivity {

    private ConstraintLayout colorView;
    private AppCompatImageView imageView;
    private AppCompatTextView nameLabel, categoryLabel, amountLabel, dateLabel, fromWalletLabel, walletLabel, typeLabel, feeTitleLabel, feeLabel, memoTitleLabel,
            memoLabel, categoryTitleLabel, fromWalletTitleLabel, walletTitleLabel;
    private AppCompatImageView icBack, ivDelete, ivEdit;
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
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            ivDelete = toolbarWrapper.findViewById(R.id.ivDelete);
            ivEdit = toolbarWrapper.findViewById(R.id.ivEdit);

            ivEdit.setVisibility(View.VISIBLE);
            ivDelete.setVisibility(View.VISIBLE);

            tvTitle.setText(R.string.detail);
            colorView = findViewById(R.id.colorView);
            imageView = findViewById(R.id.imageView);
            nameLabel = findViewById(R.id.nameLabel);
            categoryTitleLabel = findViewById(R.id.categoryTitleLabel);
            categoryLabel = findViewById(R.id.categoryLabel);
            amountLabel = findViewById(R.id.amountLabel);
            dateLabel = findViewById(R.id.dateLabel);
            fromWalletTitleLabel = findViewById(R.id.fromWalletTitleLabel);
            walletTitleLabel = findViewById(R.id.walletTitleLabel);
            fromWalletLabel = findViewById(R.id.fromWalletLabel);
            walletLabel = findViewById(R.id.walletLabel);
            typeLabel = findViewById(R.id.typeLabel);
            feeTitleLabel = findViewById(R.id.feeTitleLabel);
            feeLabel = findViewById(R.id.feeLabel);
            memoTitleLabel = findViewById(R.id.memoTitleLabel);
            memoLabel = findViewById(R.id.memoLabel);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {

                transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
                walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);
                accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

                transactionWithDetails = IntentUtils.getSerializableExtra(getIntent(), "transactionDetail", TransactionWithDetails.class);

                bindData(transactionWithDetails);
                setupListeners();
                setupLauncher();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData(TransactionWithDetails transactionWithDetail) {
        try {
            TransactionEntity transaction = transactionWithDetail.transaction;

            if (Build.VERSION.SDK_INT >= 29) {
                colorView.getBackground().setColorFilter(new BlendModeColorFilter(Color.parseColor(transactionWithDetail.color), BlendMode.SRC_OVER));
            } else {
                Drawable drawable = colorView.getBackground().mutate();
                DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_OVER);
                DrawableCompat.setTint(drawable, Color.parseColor(transactionWithDetail.color));
                colorView.setBackground(drawable);
            }

            if (transaction.type == 3) {
                imageView.setImageResource(R.drawable.ic_transfer);
            } else {
                if (transactionWithDetail.icon == null || transactionWithDetail.icon == 0) {
                    imageView.setImageResource(R.drawable.category_0);
                } else {
                    imageView.setImageResource(DataHelper.getCategoryIcons().get(transactionWithDetail.icon));
                }
            }

            nameLabel.setText(transaction.description == null || transaction.description.isEmpty() ? "---" : transaction.description);
            categoryLabel.setText(transaction.getCategoryName(getApplicationContext()));
            amountLabel.setText(CommonUtils.getBeautifyAmount(transactionWithDetail.currencySymbol, transaction.amount));
            dateLabel.setText(DateHelper.getFormattedDateTime(getApplicationContext(), transaction.transactionDate));

            String wallet = transactionWithDetail.walletName;
            walletLabel.setText(wallet);

            String type = getString(R.string.transfer);
            if (transaction.type == 1) {
                type = getString(R.string.income);
            } else if (transaction.type == 2) {
                type = getString(R.string.expense);
            }
            typeLabel.setText(type);

            if (transaction.type == 3) {

                categoryTitleLabel.setVisibility(View.GONE);
                categoryLabel.setVisibility(View.GONE);

                fromWalletTitleLabel.setVisibility(View.VISIBLE);
                fromWalletLabel.setVisibility(View.VISIBLE);

                walletTitleLabel.setText(getString(R.string.to_wallet));

                feeTitleLabel.setVisibility(View.VISIBLE);
                feeLabel.setVisibility(View.VISIBLE);
                feeLabel.setText(CommonUtils.getBeautifyAmount(transactionWithDetail.currencySymbol, transaction.fee));

                String fromWalletName = transactionWithDetail.fromWalletName;
                fromWalletLabel.setText(fromWalletName);

                if (transaction.memo != null && !transaction.memo.isEmpty()) {
                    memoTitleLabel.setVisibility(View.VISIBLE);
                    memoLabel.setVisibility(View.VISIBLE);

                    memoLabel.setText(transaction.memo);
                }
            } else {

                categoryTitleLabel.setVisibility(View.VISIBLE);
                categoryLabel.setVisibility(View.VISIBLE);

                fromWalletTitleLabel.setVisibility(View.GONE);
                fromWalletLabel.setVisibility(View.GONE);

                walletTitleLabel.setText(getString(R.string.wallet));

                feeTitleLabel.setVisibility(View.GONE);
                feeLabel.setVisibility(View.GONE);
                memoTitleLabel.setVisibility(View.GONE);
                memoLabel.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.bottom_to_top);
            });

            ivEdit.setOnClickListener(view -> {
                Intent intent = new Intent(this, CreateTransactionActivity.class);
                intent.putExtra("transactionDetail", transactionWithDetails);
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
                    ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.bottom_to_top);
                } else {
                    Toast.makeText(this, getString(R.string.trans_delete_failed), Toast.LENGTH_SHORT).show();
                }
            });

            getOnBackPressedDispatcher().addCallback(
                    this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            finish();
                            ActivityUtils.overrideCloseTransition(TransactionDetailActivity.this, R.anim.scale_in, R.anim.bottom_to_top);
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
                    break;

                case TransactionEntity.TYPE_EXPENSE:
                    if (wallet != null) {
                        wallet.amount += transaction.amount;
                        exchangeRate = wallet.exchangeRate;
                    }
                    if (account != null) {
                        account.balance += (transaction.amount * exchangeRate);
                    }
                    break;

                case TransactionEntity.TYPE_TRANSFER:
                    WalletEntity fromWallet = walletViewModel.getWalletByWalletId(transaction.fromWalletId);

                    // Reverse transfer
                    if (fromWallet != null) {
                        fromWallet.amount += transaction.amount;
                    }

                    if (wallet != null) {
                        wallet.amount -= transaction.amount;
                    }

                    // Reverse account effect for excluded wallets
                    if (account != null && fromWallet != null && wallet != null) {
                        if (!fromWallet.isExclude && wallet.isExclude) {
                            account.balance += (transaction.amount * fromWallet.exchangeRate);
                        } else if (fromWallet.isExclude && !wallet.isExclude) {
                            account.balance -= (transaction.amount * wallet.exchangeRate);
                        }
                    }

                    // Reverse fee transaction
                    TransactionEntity feeTransaction = transactionViewModel.getFeeTransaction(transaction.tempTransactionServerId);

                    if (feeTransaction != null) {

                        if (fromWallet != null) {
                            fromWallet.amount += feeTransaction.amount;
                        }

                        if (account != null && fromWallet != null && !fromWallet.isExclude) {
                            account.balance += (feeTransaction.amount * fromWallet.exchangeRate);
                        }

                        // Delete only fee transaction
                        transactionViewModel.deleteFeeTransaction(feeTransaction);
                    }
                    break;
            }

            transactionViewModel.deleteTransaction(transaction, wallet, account);
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteTransaction", e);
        }
    }
}