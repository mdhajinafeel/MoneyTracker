package com.nprotech.moneytracker.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.enums.SettingType;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.SettingItemModel;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManageAccountActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView tvAllAccounts, tvSorting;
    private AccountViewModel accountViewModel;
    private RecyclerView rvAccounts;
    private RecyclerViewAdapter<AccountEntity> accountAdapter;
    private ConstraintLayout createAccountContainer;
    private MaterialCardView cardArchivedAccount;
    private FloatingActionButton fabAddAccount;
    private SettingType selectedSortType = SettingType.DEFAULT_ACCOUNT_FIRST;
    private Typeface medium, semiBold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_accounts);
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
            tvAllAccounts = findViewById(R.id.tvAllAccounts);
            tvSorting = findViewById(R.id.tvSorting);
            rvAccounts = findViewById(R.id.rvAccounts);
            createAccountContainer = findViewById(R.id.createAccountContainer);
            cardArchivedAccount = findViewById(R.id.cardArchivedAccount);
            fabAddAccount = findViewById(R.id.fabAddAccount);
            tvTitle.setText(R.string.accounts);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                        int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                        v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                        return insets;
                    }
            );

            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                        v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                        return insets;
                    }
            );

            accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

            medium = ResourcesCompat.getFont(this, R.font.exo2_medium);
            semiBold = ResourcesCompat.getFont(this, R.font.exo2_semibold);

            bindData();
            initializeAdapter();
            observeData();
            setUpListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {
            accountViewModel.selectAccount(PreferenceManager.INSTANCE.getAccountId());
            tvAllAccounts.setText(getResources().getQuantityString(R.plurals.account_count, 0, 0));
            CommonUtils.setDrawable(this, tvSorting, R.drawable.ic_filter, R.dimen.icon_12, R.color.primary_dark, Gravity.START);
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAdapter() {
        try {
            accountAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(), R.layout.item_account_manage) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, AccountEntity account) {
                    MaterialCardView cardAccountIcon = holder.getView(R.id.cardAccountIcon);
                    AppCompatImageView ivAccountIcon = holder.getView(R.id.ivAccountIcon);
                    AppCompatImageView ivMore = holder.getView(R.id.ivMore);
                    AppCompatTextView tvAccountName = holder.getView(R.id.tvAccountName);
                    AppCompatTextView tvAccountCurrency = holder.getView(R.id.tvAccountCurrency);
                    AppCompatTextView tvAccountBadge = holder.getView(R.id.tvAccountBadge);
                    AppCompatTextView tvAccountAmount = holder.getView(R.id.tvAccountAmount);

                    cardAccountIcon.setCardBackgroundColor(Color.parseColor(account.accountColor));
                    int accountIcon = DataHelper.getWalletIcons().get(account.categoryIcon);
                    ivAccountIcon.setImageDrawable(ContextCompat.getDrawable(ManageAccountActivity.this, accountIcon));
                    tvAccountName.setText(account.name);
                    tvAccountCurrency.setText(getString(R.string.wallet_currency, account.currencyName, account.currencyCode));
                    tvAccountCurrency.setSelected(true);

                    if (account.isDefault) {
                        tvAccountBadge.setVisibility(View.VISIBLE);
                    } else {
                        tvAccountBadge.setVisibility(View.GONE);
                    }

                    tvAccountAmount.setText(CommonUtils.getBeautifyAmount(account.currencySymbol, account.balance));

                    holder.getView(R.id.tvAccountArchiveBadge).setVisibility(View.GONE);

                    ivMore.setOnClickListener(v -> showOptionDialog(account));
                }
            };

            rvAccounts.setAdapter(accountAdapter);
            rvAccounts.setItemAnimator(null);
            rvAccounts.setHasFixedSize(true);
            rvAccounts.setLayoutManager(new LinearLayoutManager(this));
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapter", e);
        }
    }

    private void observeData() {
        try {
            accountViewModel.getAccounts().observe(this, accounts -> {
                if (accounts == null || accounts.isEmpty()) {
                    accountAdapter.setItems(new ArrayList<>());
                    tvAllAccounts.setText(getResources().getQuantityString(R.plurals.account_count, 0, 0));
                } else {
                    accountAdapter.setItems(accounts);
                    int count = accounts.size();
                    tvAllAccounts.setText(getResources().getQuantityString(R.plurals.account_count, count, count));
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "observeData", e);
        }
    }

    private void setUpListeners() {
        try {
            icBack.setOnClickListener(view -> finishWithTransitions());

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finishWithTransitions();
                }
            });

            tvSorting.setOnClickListener(v -> showAccountFilterDialog());

            fabAddAccount.setOnClickListener(v -> {
                startActivity(new Intent(ManageAccountActivity.this, CreateAccountActivity.class)
                        .putExtra("isEdit", false)
                        .putExtra("accountId", 0));
                ActivityUtils.overrideOpenTransition(ManageAccountActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            createAccountContainer.setOnClickListener(v -> {
                startActivity(new Intent(ManageAccountActivity.this, CreateAccountActivity.class)
                        .putExtra("isEdit", false)
                        .putExtra("accountId", 0));
                ActivityUtils.overrideOpenTransition(ManageAccountActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            cardArchivedAccount.setOnClickListener(v -> {
                startActivity(new Intent(ManageAccountActivity.this, AccountArchivedActivity.class));
                ActivityUtils.overrideOpenTransition(ManageAccountActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setUpListeners", e);
        }
    }

    private void showOptionDialog(AccountEntity account) {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(ManageAccountActivity.this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_account_options, findViewById(android.R.id.content), false);

            MaterialCardView colorView = bottomView.findViewById(R.id.colorView);
            AppCompatImageView ivAccountIcon = bottomView.findViewById(R.id.ivAccountIcon);
            AppCompatTextView accountName = bottomView.findViewById(R.id.accountName);
            AppCompatTextView accountCurrency = bottomView.findViewById(R.id.accountCurrency);

            LinearLayout optionEdit = bottomView.findViewById(R.id.optionEdit);
            LinearLayout optionDefault = bottomView.findViewById(R.id.optionDefault);
            View viewDefault = bottomView.findViewById(R.id.viewDefault);
            LinearLayout optionViewDetails = bottomView.findViewById(R.id.optionViewDetails);
            LinearLayout optionArchive = bottomView.findViewById(R.id.optionArchive);
            View viewArchive = bottomView.findViewById(R.id.viewArchive);
            LinearLayout optionDeleteTransaction = bottomView.findViewById(R.id.optionDeleteTransaction);
            View viewDeleteTransactions = bottomView.findViewById(R.id.viewDeleteTransactions);
            LinearLayout optionDelete = bottomView.findViewById(R.id.optionDelete);

            colorView.setCardBackgroundColor(Color.parseColor(account.accountColor));

            int accountIcon = DataHelper.getWalletIcons().get(account.categoryIcon);

            ivAccountIcon.setImageDrawable(ContextCompat.getDrawable(ManageAccountActivity.this, accountIcon));
            accountName.setText(account.name);
            accountCurrency.setText(getString(R.string.wallet_currency, account.currencyName, account.currencyCode));

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
                    Toast.makeText(ManageAccountActivity.this, R.string.account_set_as_default, Toast.LENGTH_SHORT).show();
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
                startActivity(new Intent(ManageAccountActivity.this, CreateAccountActivity.class)
                        .putExtra("isEdit", true)
                        .putExtra("accountId", account.id));
                ActivityUtils.overrideOpenTransition(ManageAccountActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            // VIEW DETAILS
            optionViewDetails.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(ManageAccountActivity.this, AccountTransactionDetailedActivity.class)
                        .putExtra("accountId", account.id)
                        .putExtra("isFromManageAccount", true));
                ActivityUtils.overrideOpenTransition(ManageAccountActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
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
        headerImage.setImageDrawable(ContextCompat.getDrawable(ManageAccountActivity.this, R.drawable.ic_archive_outline));
        headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(ManageAccountActivity.this, R.color.primary_dark)));
        tvDelete.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ManageAccountActivity.this, R.color.primary_dark)));
        tvDelete.setText(getString(R.string.archive));

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        tvCancel.setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            accountViewModel.archiveAccount(account.id, true);
            Toast.makeText(ManageAccountActivity.this, R.string.account_archived, Toast.LENGTH_SHORT).show();
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

                Toast.makeText(ManageAccountActivity.this, R.string.account_deleted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ManageAccountActivity.this, R.string.error_delete_account, Toast.LENGTH_SHORT).show();
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
        headerImage.setImageDrawable(ContextCompat.getDrawable(ManageAccountActivity.this, R.drawable.ic_data_delete_outline));
        headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(ManageAccountActivity.this, R.color.primary_dark)));
        tvCancel.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ManageAccountActivity.this, R.color.primary_dark)));

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
            Toast.makeText(ManageAccountActivity.this, R.string.account_wallets_transactions_deleted, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteWalletTransaction", e);
            Toast.makeText(this, R.string.error_delete_account_trans, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showAccountFilterDialog() {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_filter_option, findViewById(android.R.id.content), false);

            RecyclerView rvSortBy = bottomView.findViewById(R.id.rvSortBy);
            AppCompatTextView tvAttachments = bottomView.findViewById(R.id.tvAttachments);
            RecyclerView rvAttachments = bottomView.findViewById(R.id.rvAttachments);
            MaterialButton btnPrimary = bottomView.findViewById(R.id.btnPrimary);
            MaterialButton btnSecondary = bottomView.findViewById(R.id.btnSecondary);
            AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);

            tvAttachments.setVisibility(View.GONE);
            rvAttachments.setVisibility(View.GONE);

            // ---------------------------------------------------------
            // Sort options
            // ---------------------------------------------------------
            List<SettingItemModel> sortByList = new ArrayList<>();
            sortByList.add(new SettingItemModel(SettingType.DEFAULT_ACCOUNT_FIRST, 0, 0, 0,
                    getString(R.string.default_first), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.NEWEST_ACCOUNT_FIRST, 0, 0, 0,
                    getString(R.string.newest_first), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.OLDEST_ACCOUNT_FIRST, 0, 0, 0,
                    getString(R.string.oldest_first), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.ACCOUNT_NAME_A_Z, 0, 0, 0,
                    getString(R.string.name_a_z), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.ACCOUNT_NAME_Z_A, 0, 0, 0,
                    getString(R.string.name_z_a), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.LARGEST_ACCOUNT_FIRST, 0, 0, 0,
                    getString(R.string.highest_balance_first), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.SMALLEST_ACCOUNT_FIRST, 0, 0, 0,
                    getString(R.string.lowest_balance_first), true, false, null, true, false, 0));

            RecyclerViewAdapter<SettingItemModel> sortByAdapter = new RecyclerViewAdapter<>(this, sortByList, R.layout.item_backup_filter_option) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, SettingItemModel item) {
                    AppCompatTextView tvFilterName = holder.getView(R.id.tvFilterName);
                    AppCompatImageView ivSelected = holder.getView(R.id.ivSelected);
                    boolean selected = item.settingType == selectedSortType;

                    tvFilterName.setText(item.title);
                    ivSelected.setVisibility(selected ? View.VISIBLE : View.GONE);
                    tvFilterName.setTypeface(selected ? semiBold : medium);

                    holder.getView(R.id.rlFilterView).setOnClickListener(v -> {
                        selectedSortType = item.settingType;
                        notifyDataSetChanged();
                    });
                }
            };
            rvSortBy.setAdapter(sortByAdapter);
            rvSortBy.setHasFixedSize(true);
            rvSortBy.setItemAnimator(null);

            btnPrimary.setOnClickListener(v -> {
                applyAccountFilters();
                dialog.dismiss();
            });

            btnSecondary.setOnClickListener(v -> {
                selectedSortType = SettingType.DEFAULT_ACCOUNT_FIRST;
                applyAccountFilters();
                dialog.dismiss();
            });

            tvClose.setOnClickListener(v -> dialog.dismiss());

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showAccountFilterDialog", e);
        }
    }

    private void applyAccountFilters() {
        try {
            accountViewModel.setAccountSort(getSortValue(selectedSortType));
            updateSortingLabel();
        } catch (Exception e) {
            AppLogger.e(getClass(), "applyAccountFilters", e);
        }
    }

    private void updateSortingLabel() {
        try {
            switch (selectedSortType) {
                case DEFAULT_ACCOUNT_FIRST:
                    tvSorting.setText(getString(R.string.default_first));
                    break;

                case NEWEST_ACCOUNT_FIRST:
                    tvSorting.setText(getString(R.string.newest_first));
                    break;

                case OLDEST_ACCOUNT_FIRST:
                    tvSorting.setText(getString(R.string.oldest_first));
                    break;

                case ACCOUNT_NAME_A_Z:
                    tvSorting.setText(getString(R.string.name_a_z));
                    break;

                case ACCOUNT_NAME_Z_A:
                    tvSorting.setText(getString(R.string.name_z_a));
                    break;

                case LARGEST_ACCOUNT_FIRST:
                    tvSorting.setText(getString(R.string.highest_balance_first));
                    break;

                case SMALLEST_ACCOUNT_FIRST:
                    tvSorting.setText(getString(R.string.lowest_balance_first));
                    break;
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "updateSortingLabel", e);
        }
    }

    private int getSortValue(SettingType type) {
        if (type == SettingType.DEFAULT_ACCOUNT_FIRST) {
            return 1;
        } else if (type == SettingType.NEWEST_ACCOUNT_FIRST) {
            return 2;
        } else if (type == SettingType.OLDEST_ACCOUNT_FIRST) {
            return 3;
        } else if (type == SettingType.ACCOUNT_NAME_A_Z) {
            return 4;
        } else if (type == SettingType.ACCOUNT_NAME_Z_A) {
            return 5;
        } else if (type == SettingType.LARGEST_ACCOUNT_FIRST) {
            return 6;
        } else if (type == SettingType.SMALLEST_ACCOUNT_FIRST) {
            return 7;
        }

        return 1;
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(ManageAccountActivity.this, R.anim.scale_in, R.anim.right_to_left);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}