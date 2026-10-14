package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.TransactionTypeAmountModel;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.TransactionViewModel;
import com.nprotech.moneytracker.viewmodel.WalletViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import me.grantland.widget.AutofitTextView;

@AndroidEntryPoint
public class AccountTransactionDetailedActivity extends BaseActivity {

    private AppCompatImageView icBack, ivMore, imageView;
    private ConstraintLayout accountContainer, walletView, transactionView, recurringView;
    private AppCompatTextView tvAccountName, tvAccountBadge, tvAvailableBalance, tvWalletCount;
    private AutofitTextView tvInitial, tvIncome, tvExpense, tvTransfer;
    private int accountId = 0;
    private TransactionViewModel transactionViewModel;
    private AccountViewModel accountViewModel;
    private WalletViewModel walletViewModel;
    private AccountEntity account;
    private double incomeAmount = 0, expenseAmount = 0, transferAmount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_transaction_detail);
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
            ivMore = toolbarWrapper.findViewById(R.id.ivMore);
            accountContainer = findViewById(R.id.accountContainer);
            imageView = findViewById(R.id.imageView);
            tvAccountName = findViewById(R.id.tvAccountName);
            tvAccountBadge = findViewById(R.id.tvAccountBadge);
            tvAvailableBalance = findViewById(R.id.tvAvailableBalance);
            tvInitial = findViewById(R.id.tvInitial);
            tvIncome = findViewById(R.id.tvIncome);
            tvExpense = findViewById(R.id.tvExpense);
            tvTransfer = findViewById(R.id.tvTransfer);
            walletView = findViewById(R.id.walletView);
            transactionView = findViewById(R.id.transactionView);
            recurringView = findViewById(R.id.recurringView);
            tvWalletCount = findViewById(R.id.tvWalletCount);

            tvTitle.setText(R.string.account_details);
            ivMore.setVisibility(View.VISIBLE);

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

                accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
                transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
                walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);

                accountId = bundle.getInt("accountId");

                if (accountId > 0) {
                    bindData(accountId);
                    setupListeners();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                    finish();
                    ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData(int accountId) {
        try {
            accountViewModel.selectAccount(accountId);

            accountViewModel.loadAccount(accountId);

            accountViewModel.getAccountLiveData().observe(this, accountData -> {
                if (accountData == null) {
                    return;
                }

                account = accountData;

                int accountIcon = DataHelper.getWalletIcons().get(account.categoryIcon);

                accountContainer.setBackground(CommonUtils.createGradient(getApplicationContext(), account.accountColor, 12));
                imageView.setBackground(CommonUtils.createIconBackground(getApplicationContext(), account.accountColor, GradientDrawable.RECTANGLE, 10));
                imageView.setImageResource(accountIcon);

                tvAccountName.setText(account.name);

                if (account.isDefault) {
                    tvAccountBadge.setText(getString(R.string.text_default));
                    tvAccountBadge.setVisibility(View.VISIBLE);
                } else {
                    if (account.isArchived) {
                        CommonUtils.setDrawable(AccountTransactionDetailedActivity.this, tvAccountBadge, R.drawable.ic_calendar_archive, R.dimen.icon_10, R.color.dark_grey, Gravity.START);
                        tvAccountBadge.setText(getString(R.string.archived_on, DateHelper.getFormattedDate(account.archivedAt, "dd MMM yy")));
                        tvAccountBadge.setVisibility(View.VISIBLE);
                    } else {
                        tvAccountBadge.setVisibility(View.GONE);
                    }
                }

                tvAvailableBalance.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, account.balance));

                tvInitial.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, account.initialAmount));
                tvIncome.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, incomeAmount));
                tvExpense.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, expenseAmount));
                tvTransfer.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, transferAmount));
            });

            List<TransactionTypeAmountModel> transactionTypeAmountModelList = transactionViewModel.getTransactionAmountByTypeForAccount(accountId);

            if (transactionTypeAmountModelList != null && !transactionTypeAmountModelList.isEmpty()) {
                for (TransactionTypeAmountModel typeAmountModel : transactionTypeAmountModelList) {
                    if (typeAmountModel.getType() == 1) {
                        incomeAmount = typeAmountModel.getAmount();
                    } else if (typeAmountModel.getType() == 2) {
                        expenseAmount = typeAmountModel.getAmount();
                    } else {
                        transferAmount = typeAmountModel.getAmount();
                    }
                }
            }

            int walletCount = walletViewModel.walletCount(accountId);
            if (walletCount > 0) {
                tvWalletCount.setText(getResources().getQuantityString(R.plurals.wallet_count, walletCount, walletCount));
            } else {
                tvWalletCount.setText(getResources().getQuantityString(R.plurals.wallet_count, 0, 0));
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
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

            ivMore.setOnClickListener(view -> showOptionDialog(account));

            tvAccountName.setOnClickListener(view -> switchAccounts());

            walletView.setOnClickListener(v -> {
                startActivity(new Intent(AccountTransactionDetailedActivity.this, ManageWalletActivity.class)
                        .putExtra("accountId", account.id));
                ActivityUtils.overrideOpenTransition(AccountTransactionDetailedActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            transactionView.setOnClickListener(v -> {
                Intent intent = new Intent(AccountTransactionDetailedActivity.this, CategoryTransactionActivity.class);
                intent.putExtra("accountId", account.id);
                intent.putExtra("categoryName", "");
                intent.putExtra("isFromAccount", true);
                startActivity(intent);
                ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            recurringView.setOnClickListener(v -> {
                startActivity(new Intent(AccountTransactionDetailedActivity.this, RecurringActivity.class)
                        .putExtra("accountId", account.id));
                ActivityUtils.overrideOpenTransition(AccountTransactionDetailedActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void showOptionDialog(AccountEntity account) {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(AccountTransactionDetailedActivity.this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_account_options, findViewById(android.R.id.content), false);

            MaterialCardView colorView = bottomView.findViewById(R.id.colorView);
            AppCompatImageView ivAccountIcon = bottomView.findViewById(R.id.ivAccountIcon);
            AppCompatTextView accountName = bottomView.findViewById(R.id.accountName);
            AppCompatTextView accountCurrency = bottomView.findViewById(R.id.accountCurrency);

            LinearLayout optionEdit = bottomView.findViewById(R.id.optionEdit);
            LinearLayout optionDefault = bottomView.findViewById(R.id.optionDefault);
            View viewDefault = bottomView.findViewById(R.id.viewDefault);
            LinearLayout optionViewDetails = bottomView.findViewById(R.id.optionViewDetails);
            View viewViewDetails = bottomView.findViewById(R.id.viewViewDetails);
            LinearLayout optionArchive = bottomView.findViewById(R.id.optionArchive);
            View viewArchive = bottomView.findViewById(R.id.viewArchive);
            LinearLayout optionDeleteTransaction = bottomView.findViewById(R.id.optionDeleteTransaction);
            View viewDeleteTransactions = bottomView.findViewById(R.id.viewDeleteTransactions);
            LinearLayout optionDelete = bottomView.findViewById(R.id.optionDelete);

            colorView.setCardBackgroundColor(Color.parseColor(account.accountColor));

            int accountIcon = DataHelper.getWalletIcons().get(account.categoryIcon);

            ivAccountIcon.setImageDrawable(ContextCompat.getDrawable(AccountTransactionDetailedActivity.this, accountIcon));
            accountName.setText(account.name);
            accountCurrency.setText(getString(R.string.wallet_currency, account.currencyName, account.currencyCode));

            optionViewDetails.setVisibility(View.GONE);
            viewViewDetails.setVisibility(View.GONE);

            if (account.isDefault) {
                optionDefault.setVisibility(View.GONE);
                viewDefault.setVisibility(View.GONE);

                optionArchive.setVisibility(View.GONE);
                viewArchive.setVisibility(View.GONE);

                optionDelete.setVisibility(View.GONE);

                optionDeleteTransaction.setVisibility(View.GONE);
                viewDeleteTransactions.setVisibility(View.GONE);
            } else {
                optionDefault.setVisibility(View.VISIBLE);
                viewDefault.setVisibility(View.VISIBLE);

                optionArchive.setVisibility(View.VISIBLE);
                viewArchive.setVisibility(View.VISIBLE);

                optionDelete.setVisibility(View.VISIBLE);

                optionDeleteTransaction.setVisibility(View.VISIBLE);
                viewDeleteTransactions.setVisibility(View.VISIBLE);

                // DEFAULT
                optionDefault.setOnClickListener(v -> {
                    dialog.dismiss();
                    accountViewModel.setDefaultAccount(account.id);
                    Toast.makeText(AccountTransactionDetailedActivity.this, R.string.account_set_as_default, Toast.LENGTH_SHORT).show();
                });

                // ARCHIVE
                optionArchive.setOnClickListener(v -> {
                    dialog.dismiss();
                    showArchiveDialog(account);
                });

                // DELETE
                optionDelete.setOnClickListener(v -> {
                    dialog.dismiss();
                    showDeleteDialog(account);
                });

                // DELETE TRANSACTION
                optionDeleteTransaction.setOnClickListener(v -> {
                    dialog.dismiss();
                    showDeleteTransactionDialog(account);
                });
            }

            // EDIT
            optionEdit.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(AccountTransactionDetailedActivity.this, CreateAccountActivity.class)
                        .putExtra("isEdit", true)
                        .putExtra("accountId", account.id));
                ActivityUtils.overrideOpenTransition(AccountTransactionDetailedActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showOptionDialog", e);
        }
    }

    private void showArchiveDialog(AccountEntity account) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        AppCompatTextView tvCancel = view.findViewById(R.id.tvCancel);
        MaterialButton tvDelete = view.findViewById(R.id.tvDelete);
        MaterialCardView cardHeader = view.findViewById(R.id.cardHeader);
        AppCompatImageView headerImage = view.findViewById(R.id.headerImage);
        tvTitle.setText(R.string.archive_account);
        tvMessage.setText(R.string.archive_account_message);
        tvSubMessage.setVisibility(View.GONE);

        cardHeader.setCardBackgroundColor(getColor(R.color.light_lavender));
        headerImage.setImageDrawable(ContextCompat.getDrawable(AccountTransactionDetailedActivity.this, R.drawable.ic_archive_outline));
        headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(AccountTransactionDetailedActivity.this, R.color.primary_dark)));
        tvDelete.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(AccountTransactionDetailedActivity.this, R.color.primary_dark)));
        tvDelete.setText(getString(R.string.archive));

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        tvCancel.setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            accountViewModel.archiveAccount(account.id, true);
            Toast.makeText(AccountTransactionDetailedActivity.this, R.string.account_archived, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDeleteDialog(AccountEntity account) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        tvTitle.setText(R.string.delete_account);
        tvMessage.setText(R.string.delete_account_message);
        tvSubMessage.setVisibility(View.GONE);
        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            deleteAccount(account.id);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteAccount(int accountId) {
        try {
            if (accountViewModel.deleteAccount(accountId)) {

                // Update the selected account only if it was deleted.
                if (PreferenceManager.INSTANCE.getAccountId() == accountId) {
                    int defaultAccountId = accountViewModel.getDefaultAccountId();
                    if (defaultAccountId > 0) {
                        PreferenceManager.INSTANCE.setAccountId(defaultAccountId);
                        accountViewModel.selectAccount(defaultAccountId);
                    }
                }

                Toast.makeText(AccountTransactionDetailedActivity.this, R.string.account_deleted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AccountTransactionDetailedActivity.this, R.string.error_delete_account, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteAccount", e);
            Toast.makeText(this, R.string.error_delete_account, Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteTransactionDialog(AccountEntity account) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        AppCompatTextView tvCancel = view.findViewById(R.id.tvCancel);
        MaterialCardView cardHeader = view.findViewById(R.id.cardHeader);
        AppCompatImageView headerImage = view.findViewById(R.id.headerImage);
        tvTitle.setText(R.string.delete_account_wallets_transactions);
        tvMessage.setText(R.string.delete_wallet_account_desc);
        tvSubMessage.setVisibility(View.GONE);

        cardHeader.setCardBackgroundColor(getColor(R.color.light_lavender));
        headerImage.setImageDrawable(ContextCompat.getDrawable(AccountTransactionDetailedActivity.this, R.drawable.ic_data_delete_outline));
        headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(AccountTransactionDetailedActivity.this, R.color.primary_dark)));
        tvCancel.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(AccountTransactionDetailedActivity.this, R.color.primary_dark)));

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        tvCancel.setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            deleteWalletTransaction(account.id);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteWalletTransaction(int accountId) {
        try {
            accountViewModel.deleteWalletTransactions(accountId);
            Toast.makeText(AccountTransactionDetailedActivity.this, R.string.account_wallets_transactions_deleted, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteWalletTransaction", e);
            Toast.makeText(this, R.string.error_delete_account_trans, Toast.LENGTH_SHORT).show();
        }
    }

    private void switchAccounts() {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_account_picker_layout, findViewById(android.R.id.content), false);
            RecyclerView rvAccounts = bottomView.findViewById(R.id.rvAccounts);
            LinearLayout layoutAddAccount = bottomView.findViewById(R.id.layoutAddAccount);
            View viewLine = bottomView.findViewById(R.id.viewLine);

            viewLine.setVisibility(View.GONE);
            layoutAddAccount.setVisibility(View.GONE);

            accountViewModel.getAllAccounts();
            accountViewModel.getAccountList().observe(this, accountList -> {
                RecyclerViewAdapter<AccountEntity> accountAdapter = new RecyclerViewAdapter<>(this, accountList, R.layout.item_switch_accounts) {
                    @Override
                    public void onPostBindViewHolder(ViewHolder holder, AccountEntity accountEntity) {
                        holder.setViewText(R.id.tvAccountName, accountEntity.name);
                        holder.setViewText(R.id.tvAccountBalance, getString(R.string.account_balance_format,
                                CommonUtils.getBeautifyAmount(accountEntity.currencySymbol, accountEntity.balance)));
                        holder.getView(R.id.ivSelected).setVisibility(accountId == accountEntity.id ? View.VISIBLE : View.GONE);
                        holder.getView(R.id.rlAccountView).setOnClickListener(v -> {
                            accountId = accountEntity.id;
                            accountViewModel.selectAccount(accountEntity.id);
                            accountViewModel.loadAccount(accountEntity.id);
                            dialog.dismiss();
                        });
                    }
                };

                rvAccounts.setAdapter(accountAdapter);
                rvAccounts.setHasFixedSize(true);
            });

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "switchAccounts", e);
        }
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindData(accountId);
    }
}