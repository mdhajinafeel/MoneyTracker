package com.nprotech.moneytracker.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import com.nprotech.moneytracker.db.entites.WalletEntity;
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
import com.nprotech.moneytracker.viewmodel.WalletViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManageWalletActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView tvAllWallets, tvSorting;
    private WalletViewModel walletViewModel;
    private RecyclerView rvWallets;
    private RecyclerViewAdapter<WalletEntity> walletAdapter;
    private ConstraintLayout createWalletContainer;
    private MaterialCardView cardArchivedWallet;
    private FloatingActionButton fabAddWallet;
    private SettingType selectedSortType = SettingType.DEFAULT_WALLET_FIRST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_wallets);
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
            tvAllWallets = findViewById(R.id.tvAllWallets);
            tvSorting = findViewById(R.id.tvSorting);
            rvWallets = findViewById(R.id.rvWallets);
            createWalletContainer = findViewById(R.id.createWalletContainer);
            cardArchivedWallet = findViewById(R.id.cardArchivedWallet);
            fabAddWallet = findViewById(R.id.fabAddWallet);
            tvTitle.setText(R.string.wallets);

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

            walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);

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
            walletViewModel.selectAccount((int) PreferenceManager.INSTANCE.getAccountId());

            tvAllWallets.setText(getResources().getQuantityString(R.plurals.wallet_count, 0, 0));
            CommonUtils.setDrawable(this, tvSorting, R.drawable.ic_filter, R.dimen.icon_12, R.color.primary_dark, Gravity.START);
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAdapter() {
        try {
            walletAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(), R.layout.item_wallet_manage) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, WalletEntity wallet) {
                    MaterialCardView cardWalletIcon = holder.getView(R.id.cardWalletIcon);
                    AppCompatImageView ivWalletIcon = holder.getView(R.id.ivWalletIcon);
                    AppCompatImageView ivMore = holder.getView(R.id.ivMore);
                    AppCompatTextView tvWalletName = holder.getView(R.id.tvWalletName);
                    AppCompatTextView tvWalletCurrency = holder.getView(R.id.tvWalletCurrency);
                    AppCompatTextView tvWalletBadge = holder.getView(R.id.tvWalletBadge);
                    AppCompatTextView tvWalletAmount = holder.getView(R.id.tvWalletAmount);

                    cardWalletIcon.setCardBackgroundColor(Color.parseColor(wallet.walletColor));

                    int walletIcon = DataHelper.getWalletIcons().get(wallet.categoryIcon);

                    ivWalletIcon.setImageDrawable(ContextCompat.getDrawable(ManageWalletActivity.this, walletIcon));
                    tvWalletName.setText(wallet.name);
                    tvWalletCurrency.setText(getString(R.string.wallet_currency, wallet.currencyName, wallet.currencyCode));

                    if (wallet.isDefault) {
                        tvWalletBadge.setVisibility(View.VISIBLE);
                    } else {
                        tvWalletBadge.setVisibility(View.GONE);
                    }

                    tvWalletAmount.setText(CommonUtils.getBeautifyAmount(wallet.currencySymbol, wallet.amount));

                    holder.getView(R.id.tvWalletArchiveBadge).setVisibility(View.GONE);

                    ivMore.setOnClickListener(v -> showOptionDialog(wallet));
                }
            };

            rvWallets.setAdapter(walletAdapter);
            rvWallets.setItemAnimator(null);
            rvWallets.setHasFixedSize(true);
            rvWallets.setLayoutManager(new LinearLayoutManager(this));
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapter", e);
        }
    }

    private void observeData() {
        try {
            walletViewModel.getWallets().observe(this, wallets -> {
                if (wallets == null || wallets.isEmpty()) {
                    walletAdapter.setItems(new ArrayList<>());

                    tvAllWallets.setText(getResources().getQuantityString(R.plurals.wallet_count, 0, 0));
                } else {
                    walletAdapter.setItems(wallets);
                    int count = wallets.size();
                    tvAllWallets.setText(getResources().getQuantityString(R.plurals.wallet_count, count, count));
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

            tvSorting.setOnClickListener(v -> showWalletFilterDialog());

            fabAddWallet.setOnClickListener(v -> {
                startActivity(new Intent(ManageWalletActivity.this, CreateWalletActivity.class)
                        .putExtra("isEdit", false));
                ActivityUtils.overrideOpenTransition(ManageWalletActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            createWalletContainer.setOnClickListener(v -> {
                startActivity(new Intent(ManageWalletActivity.this, CreateWalletActivity.class)
                        .putExtra("isEdit", false));
                ActivityUtils.overrideOpenTransition(ManageWalletActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            cardArchivedWallet.setOnClickListener(v -> {
                startActivity(new Intent(ManageWalletActivity.this, WalletArchivedActivity.class));
                ActivityUtils.overrideOpenTransition(ManageWalletActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setUpListeners", e);
        }
    }

    private void showOptionDialog(WalletEntity wallet) {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(ManageWalletActivity.this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_wallet_options, findViewById(android.R.id.content), false);

            MaterialCardView colorView = bottomView.findViewById(R.id.colorView);
            AppCompatImageView ivTransactionIcon = bottomView.findViewById(R.id.ivTransactionIcon);
            AppCompatTextView walletName = bottomView.findViewById(R.id.walletName);
            AppCompatTextView walletCurrency = bottomView.findViewById(R.id.walletCurrency);

            LinearLayout optionEdit = bottomView.findViewById(R.id.optionEdit);
            LinearLayout optionDefault = bottomView.findViewById(R.id.optionDefault);
            View viewDefault = bottomView.findViewById(R.id.viewDefault);
            LinearLayout optionViewDetails = bottomView.findViewById(R.id.optionViewDetails);
            LinearLayout optionArchive = bottomView.findViewById(R.id.optionArchive);
            View viewArchive = bottomView.findViewById(R.id.viewArchive);
            LinearLayout optionDeleteTransaction = bottomView.findViewById(R.id.optionDeleteTransaction);
            LinearLayout optionDelete = bottomView.findViewById(R.id.optionDelete);

            colorView.setCardBackgroundColor(Color.parseColor(wallet.walletColor));

            int walletIcon = DataHelper.getWalletIcons().get(wallet.categoryIcon);

            ivTransactionIcon.setImageDrawable(ContextCompat.getDrawable(ManageWalletActivity.this, walletIcon));
            walletName.setText(wallet.name);
            walletCurrency.setText(getString(R.string.wallet_currency, wallet.currencyName, wallet.currencyCode));

            if (wallet.isDefault) {
                optionDefault.setVisibility(View.GONE);
                viewDefault.setVisibility(View.GONE);

                optionArchive.setVisibility(View.GONE);
                viewArchive.setVisibility(View.GONE);

                optionDelete.setVisibility(View.GONE);
            } else {
                optionDefault.setVisibility(View.VISIBLE);
                viewDefault.setVisibility(View.VISIBLE);

                optionArchive.setVisibility(View.VISIBLE);
                viewArchive.setVisibility(View.VISIBLE);

                optionDelete.setVisibility(View.VISIBLE);

                // DEFAULT
                optionDefault.setOnClickListener(v -> {
                    dialog.dismiss();
                    walletViewModel.setDefaultWallet(wallet.id, (int) PreferenceManager.INSTANCE.getAccountId());
                    Toast.makeText(ManageWalletActivity.this, R.string.wallet_set_as_default, Toast.LENGTH_SHORT).show();
                });

                // ARCHIVE
                optionArchive.setOnClickListener(v -> {
                    dialog.dismiss();
                    showArchiveDialog(wallet);
                });

                // DELETE
                optionDelete.setOnClickListener(v -> {
                    dialog.dismiss();
                    showDeleteDialog(wallet);
                });
            }

            // EDIT
            optionEdit.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(ManageWalletActivity.this, CreateWalletActivity.class)
                        .putExtra("isEdit", true)
                        .putExtra("walletId", wallet.id));
                ActivityUtils.overrideOpenTransition(ManageWalletActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            // VIEW DETAILS
            optionViewDetails.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(ManageWalletActivity.this, WalletTransactionDetailedActivity.class)
                        .putExtra("walletId", wallet.id)
                        .putExtra("isFromManageWallet", true));
                ActivityUtils.overrideOpenTransition(ManageWalletActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            // DELETE TRANSACTION
            optionDeleteTransaction.setOnClickListener(v -> {
                dialog.dismiss();
                showDeleteTransactionDialog(wallet);
            });

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showOptionDialog", e);
        }
    }

    private void showArchiveDialog(WalletEntity wallet) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        AppCompatTextView tvCancel = view.findViewById(R.id.tvCancel);
        MaterialButton tvDelete = view.findViewById(R.id.tvDelete);
        MaterialCardView cardHeader = view.findViewById(R.id.cardHeader);
        AppCompatImageView headerImage = view.findViewById(R.id.headerImage);
        tvTitle.setText(R.string.archive_wallet);
        tvMessage.setText(R.string.archive_wallet_message);
        tvSubMessage.setVisibility(View.GONE);

        cardHeader.setCardBackgroundColor(getColor(R.color.light_lavender));
        headerImage.setImageDrawable(ContextCompat.getDrawable(ManageWalletActivity.this, R.drawable.ic_archive_outline));
        headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(ManageWalletActivity.this, R.color.primary_dark)));
        tvDelete.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ManageWalletActivity.this, R.color.primary_dark)));
        tvDelete.setText(getString(R.string.archive));

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        tvCancel.setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            walletViewModel.archiveWallet(wallet.id, (int) PreferenceManager.INSTANCE.getAccountId(), true);
            Toast.makeText(ManageWalletActivity.this, R.string.wallet_archived, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDeleteTransactionDialog(WalletEntity wallet) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        AppCompatTextView tvCancel = view.findViewById(R.id.tvCancel);
        MaterialCardView cardHeader = view.findViewById(R.id.cardHeader);
        AppCompatImageView headerImage = view.findViewById(R.id.headerImage);
        tvTitle.setText(R.string.delete_wallet_transactions);
        tvMessage.setText(R.string.delete_wallet_transactions_desc);
        tvSubMessage.setVisibility(View.GONE);

        cardHeader.setCardBackgroundColor(getColor(R.color.light_lavender));
        headerImage.setImageDrawable(ContextCompat.getDrawable(ManageWalletActivity.this, R.drawable.ic_data_delete_outline));
        headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(ManageWalletActivity.this, R.color.primary_dark)));
        tvCancel.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ManageWalletActivity.this, R.color.primary_dark)));

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        tvCancel.setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            deleteWalletTransaction(wallet.id);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteWalletTransaction(int walletId) {
        try {
            walletViewModel.deleteWalletTransactions(walletId, (int) PreferenceManager.INSTANCE.getAccountId());
            Toast.makeText(ManageWalletActivity.this, R.string.wallet_trans_deleted, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteWallet", e);
            Toast.makeText(this, R.string.error_delete_wallet_trans, Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteDialog(WalletEntity wallet) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        tvTitle.setText(R.string.delete_wallet);
        tvMessage.setText(R.string.delete_wallet_confirmation);
        tvSubMessage.setVisibility(View.GONE);
        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            deleteWallet(wallet.id);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteWallet(int walletId) {
        try {
            walletViewModel.deleteWallet(walletId, (int) PreferenceManager.INSTANCE.getAccountId());
            Toast.makeText(ManageWalletActivity.this, R.string.wallet_deleted, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteWallet", e);
            Toast.makeText(this, R.string.error_delete_wallet, Toast.LENGTH_SHORT).show();
        }
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(ManageWalletActivity.this, R.anim.scale_in, R.anim.right_to_left);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showWalletFilterDialog() {
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
            sortByList.add(new SettingItemModel(SettingType.DEFAULT_WALLET_FIRST, 0, 0, 0,
                    getString(R.string.default_first), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.NEWEST_WALLET_FIRST, 0, 0, 0,
                    getString(R.string.newest_first), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.OLDEST_WALLET_FIRST, 0, 0, 0,
                    getString(R.string.oldest_first), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.NAME_A_Z, 0, 0, 0,
                    getString(R.string.name_a_z), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.NAME_Z_A, 0, 0, 0,
                    getString(R.string.name_z_a), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.LARGEST_WALLET_FIRST, 0, 0, 0,
                    getString(R.string.highest_balance_first), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.SMALLEST_WALLET_FIRST, 0, 0, 0,
                    getString(R.string.lowest_balance_first), true, false, null, true, false, 0));

            RecyclerViewAdapter<SettingItemModel> sortByAdapter = new RecyclerViewAdapter<>(this, sortByList, R.layout.item_backup_filter_option) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, SettingItemModel item) {
                    holder.setViewText(R.id.tvFilterName, item.title);
                    AppCompatImageView ivSelected = holder.getView(R.id.ivSelected);
                    boolean selected = item.settingType == selectedSortType;
                    ivSelected.setVisibility(selected ? View.VISIBLE : View.GONE);
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
                applyWalletFilters();
                dialog.dismiss();
            });

            btnSecondary.setOnClickListener(v -> {
                selectedSortType = SettingType.NEWEST_WALLET_FIRST;
                applyWalletFilters();
                dialog.dismiss();
            });

            tvClose.setOnClickListener(v -> dialog.dismiss());

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showBackupFilterDialog", e);
        }
    }

    private void applyWalletFilters() {
        try {
            walletViewModel.setWalletSort(getSortValue(selectedSortType));
            updateSortingLabel();
        } catch (Exception e) {
            AppLogger.e(getClass(), "applyBackupFilters", e);
        }
    }

    private void updateSortingLabel() {
        try {
            switch (selectedSortType) {
                case DEFAULT_WALLET_FIRST:
                    tvSorting.setText(getString(R.string.default_first));
                    break;

                case NEWEST_WALLET_FIRST:
                    tvSorting.setText(getString(R.string.newest_first));
                    break;

                case OLDEST_WALLET_FIRST:
                    tvSorting.setText(getString(R.string.oldest_first));
                    break;

                case NAME_A_Z:
                    tvSorting.setText(getString(R.string.name_a_z));
                    break;

                case NAME_Z_A:
                    tvSorting.setText(getString(R.string.name_z_a));
                    break;

                case LARGEST_WALLET_FIRST:
                    tvSorting.setText(getString(R.string.highest_balance_first));
                    break;

                case SMALLEST_WALLET_FIRST:
                    tvSorting.setText(getString(R.string.lowest_balance_first));
                    break;
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "updateSortingLabel", e);
        }
    }

    private int getSortValue(SettingType type) {
        if (type == SettingType.DEFAULT_WALLET_FIRST) {
            return 1;
        } else if (type == SettingType.NEWEST_WALLET_FIRST) {
            return 2;
        } else if (type == SettingType.OLDEST_WALLET_FIRST) {
            return 3;
        } else if (type == SettingType.NAME_A_Z) {
            return 4;
        } else if (type == SettingType.NAME_Z_A) {
            return 5;
        } else if (type == SettingType.LARGEST_WALLET_FIRST) {
            return 6;
        } else if (type == SettingType.SMALLEST_WALLET_FIRST) {
            return 7;
        }

        return 1;
    }
}