package com.nprotech.moneytracker.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.BackupHistoryEntity;
import com.nprotech.moneytracker.enums.SettingType;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.models.BackupFileModel;
import com.nprotech.moneytracker.models.SettingItemModel;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.BackupHistoryViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BackupHistoryActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView tvTotalBackup, lblTotalBackup, tvAllBackup, tvSorting;
    private RecyclerView rvBackupHistory;
    private ConstraintLayout emptyWrapper, loadingWrapper;
    private RecyclerViewAdapter<BackupFileModel> backupHistoryRecyclerViewAdapter;
    private BackupHistoryViewModel backupHistoryViewModel;
    private SettingType selectedSortType = SettingType.NEWEST_FIRST;
    private SettingType selectedAttachmentType = SettingType.ALL_BACKUP;
    private boolean storageSettingsOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_history);
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
            tvTotalBackup = findViewById(R.id.tvTotalBackup);
            lblTotalBackup = findViewById(R.id.lblTotalBackup);
            tvAllBackup = findViewById(R.id.tvAllBackup);
            tvSorting = findViewById(R.id.tvSorting);
            rvBackupHistory = findViewById(R.id.rvBackupHistory);
            emptyWrapper = findViewById(R.id.emptyWrapper);
            loadingWrapper = findViewById(R.id.loadingWrapper);

            tvTitle.setText(getString(R.string.backup_history));

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

            backupHistoryViewModel = new ViewModelProvider(this).get(BackupHistoryViewModel.class);

            initializeAdapter();
            observeData();
            setupListeners();
            bindData();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {

            tvTotalBackup.setText("0");
            lblTotalBackup.setText(getResources().getQuantityString(R.plurals.total_backup_count, 0, 0));
            tvAllBackup.setText(getResources().getQuantityString(R.plurals.backup_count, 0, 0));

            CommonUtils.setDrawable(this, tvSorting, R.drawable.ic_filter, R.dimen.icon_12, R.color.primary_dark, Gravity.START);

            if (!hasStorageAccess()) {
                hideScanning();
                requestStorageAccess();
                return;
            }

            loadBackupHistory();
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void loadBackupHistory() {

        backupHistoryViewModel.loadBackupHistory(getSortValue(selectedSortType), getAttachmentFilterValue(selectedAttachmentType));

        showScanning();

        backupHistoryViewModel.scanBackups(this, getSortValue(selectedSortType), getAttachmentFilterValue(selectedAttachmentType));
    }

    private void observeData() {
        try {
            backupHistoryViewModel.getBackupHistoryList().observe(this, entities -> {

                        if (entities == null || entities.isEmpty()) {

                            if (loadingWrapper.getVisibility() != View.VISIBLE) {
                                rvBackupHistory.setVisibility(View.GONE);
                                emptyWrapper.setVisibility(View.VISIBLE);
                            }

                            tvTotalBackup.setText("0");
                            lblTotalBackup.setText(getResources().getQuantityString(R.plurals.total_backup_count, 0, 0));
                            tvAllBackup.setText(getResources().getQuantityString(R.plurals.backup_count, 0, 0));

                            return;
                        }

                        List<BackupFileModel> list = new ArrayList<>();

                        for (BackupHistoryEntity entity : entities) {
                            if (entity == null) {
                                continue;
                            }

                            BackupFileModel model = convertToBackupFileModel(entity);
                            list.add(model);
                        }

                        if (list.isEmpty()) {
                            loadingWrapper.setVisibility(View.GONE);
                            rvBackupHistory.setVisibility(View.GONE);
                            emptyWrapper.setVisibility(View.VISIBLE);
                            return;
                        }

                        loadingWrapper.setVisibility(View.GONE);
                        emptyWrapper.setVisibility(View.GONE);
                        rvBackupHistory.setVisibility(View.VISIBLE);
                        backupHistoryRecyclerViewAdapter.replaceItems(list);

                        int count = list.size();

                        tvTotalBackup.setText(String.valueOf(count));
                        lblTotalBackup.setText(getResources()
                                .getQuantityString(
                                        R.plurals.total_backup_count,
                                        count,
                                        count
                                ));
                        tvAllBackup.setText(getResources()
                                .getQuantityString(
                                        R.plurals.backup_count,
                                        count,
                                        count
                                ));
                    }
            );
        } catch (Exception e) {
            AppLogger.e(getClass(), "observeData", e);
        }
    }

    private void initializeAdapter() {
        try {
            backupHistoryRecyclerViewAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(), R.layout.item_backup_history) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, BackupFileModel history) {
                    holder.setViewText(R.id.tvBackupName, getString(R.string.app_backup, getString(R.string.app_name)));

                    CommonUtils.setDrawable(BackupHistoryActivity.this, holder.getView(R.id.tvBackupDate), R.drawable.ic_calendar, R.dimen.icon_12, R.color.dark_grey, Gravity.START);
                    CommonUtils.setDrawable(BackupHistoryActivity.this, holder.getView(R.id.tvBackupTime), R.drawable.ic_time, R.dimen.icon_12, R.color.dark_grey, Gravity.START);
                    CommonUtils.setDrawable(BackupHistoryActivity.this, holder.getView(R.id.tvBackupSize), R.drawable.ic_database, R.dimen.icon_12, R.color.privacy_dark, Gravity.START);

                    holder.setViewText(R.id.tvBackupDate, DateHelper.getFormattedDate(history.createdAt, "dd MMM yyyy"));
                    holder.setViewText(R.id.tvBackupTime, DateHelper.getFormattedDate(history.createdAt, "hh:mm a"));
                    holder.setViewText(R.id.tvBackupSize, Formatter.formatFileSize(BackupHistoryActivity.this, history.backupSize));

                    AppCompatTextView tvBackupAttachment = holder.getView(R.id.tvBackupAttachment);
                    AppCompatTextView tvBackupLocation = holder.getView(R.id.tvBackupLocation);

                    if (history.includeAttachments) {
                        CommonUtils.setDrawable(BackupHistoryActivity.this, tvBackupAttachment, R.drawable.ic_complete, R.dimen.icon_12, R.color.dark_income, Gravity.START);
                        tvBackupAttachment.setText(R.string.attachments_included);
                    } else {
                        CommonUtils.setDrawable(BackupHistoryActivity.this, tvBackupAttachment, R.drawable.ic_cancel, R.dimen.icon_12, R.color.bright_red, Gravity.START);
                        tvBackupAttachment.setText(R.string.no_attachments);
                    }

                    CommonUtils.setDrawable(BackupHistoryActivity.this, tvBackupLocation, R.drawable.ic_folder_outline, R.dimen.icon_12, R.color.backup_dark, Gravity.START);
                    tvBackupLocation.setText(history.location);

                    holder.getView(R.id.ivMore).setOnClickListener(v -> showOptionDialog(history));
                }
            };

            rvBackupHistory.setHasFixedSize(true);
            rvBackupHistory.setLayoutManager(new LinearLayoutManager(this));
            rvBackupHistory.setItemAnimator(null);
            rvBackupHistory.setNestedScrollingEnabled(false);
            rvBackupHistory.setAdapter(backupHistoryRecyclerViewAdapter);
            rvBackupHistory.addOnScrollListener(
                    new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            if (dy <= 0) {
                                return;
                            }

                            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                            if (layoutManager == null) {
                                return;
                            }

                            int visibleItemCount = layoutManager.getChildCount();
                            int totalItemCount = layoutManager.getItemCount();
                            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                            if ((visibleItemCount + firstVisibleItemPosition + 10) >= totalItemCount) {
                                backupHistoryViewModel.loadNextPage();
                            }
                        }
                    }
            );
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapter", e);
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> finishWithTransition());

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finishWithTransition();
                }
            });

            tvSorting.setOnClickListener(v -> showBackupFilterDialog());
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void finishWithTransition() {
        try {
            finish();
            ActivityUtils.overrideCloseTransition(BackupHistoryActivity.this, R.anim.scale_in, R.anim.right_to_left);
        } catch (Exception e) {
            AppLogger.e(getClass(), "finishWithTransition", e);
            finish();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showBackupFilterDialog() {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_backup_filter_option, findViewById(android.R.id.content), false);

            RecyclerView rvSortBy = bottomView.findViewById(R.id.rvSortBy);
            RecyclerView rvAttachments = bottomView.findViewById(R.id.rvAttachments);
            MaterialButton btnPrimary = bottomView.findViewById(R.id.btnPrimary);
            MaterialButton btnSecondary = bottomView.findViewById(R.id.btnSecondary);
            AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);

            // ---------------------------------------------------------
            // Sort options
            // ---------------------------------------------------------
            List<SettingItemModel> sortByList = new ArrayList<>();
            sortByList.add(new SettingItemModel(SettingType.NEWEST_FIRST, 0, 0, 0,
                    getString(R.string.newest_first), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.OLDEST_FIRST, 0, 0, 0,
                    getString(R.string.oldest_first), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.LARGEST_FIRST, 0, 0, 0,
                    getString(R.string.largest_first), true, false, null, true, false, 0));
            sortByList.add(new SettingItemModel(SettingType.SMALLEST_FIRST, 0, 0, 0,
                    getString(R.string.smallest_first), true, false, null, true, false, 0));

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

            // ---------------------------------------------------------
            // Filter options
            // ---------------------------------------------------------
            List<SettingItemModel> filterList = new ArrayList<>();
            filterList.add(new SettingItemModel(SettingType.ALL_BACKUP, 0, 0, 0,
                    getString(R.string.all_backups), true, false, null, true, false, 0));
            filterList.add(new SettingItemModel(SettingType.WITH_ATTACHMENTS, 0, 0, 0,
                    getString(R.string.with_attachments), true, false, null, true, false, 0));
            filterList.add(new SettingItemModel(SettingType.WITHOUT_ATTACHMENTS, 0, 0, 0,
                    getString(R.string.without_attachments), true, false, null, true, false, 0));

            RecyclerViewAdapter<SettingItemModel> filterAdapter = new RecyclerViewAdapter<>(this, filterList, R.layout.item_backup_filter_option) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, SettingItemModel item) {
                    holder.setViewText(R.id.tvFilterName, item.title);
                    AppCompatImageView ivSelected = holder.getView(R.id.ivSelected);
                    boolean selected = item.settingType == selectedAttachmentType;
                    ivSelected.setVisibility(selected ? View.VISIBLE : View.GONE);
                    holder.getView(R.id.rlFilterView).setOnClickListener(v -> {
                        selectedAttachmentType = item.settingType;
                        notifyDataSetChanged();
                    });
                }
            };
            rvAttachments.setAdapter(filterAdapter);
            rvAttachments.setHasFixedSize(true);
            rvAttachments.setItemAnimator(null);

            btnPrimary.setOnClickListener(v -> {
                applyBackupFilters();
                dialog.dismiss();
            });

            btnSecondary.setOnClickListener(v -> {
                selectedSortType = SettingType.NEWEST_FIRST;
                selectedAttachmentType = SettingType.ALL_BACKUP;
                applyBackupFilters();
                dialog.dismiss();
            });

            tvClose.setOnClickListener(v -> dialog.dismiss());

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showBackupFilterDialog", e);
        }
    }

    private void applyBackupFilters() {

        try {
            backupHistoryViewModel.loadBackupHistory(getSortValue(selectedSortType), getAttachmentFilterValue(selectedAttachmentType));
            updateSortingLabel();
        } catch (Exception e) {
            AppLogger.e(getClass(), "applyBackupFilters", e);
        }
    }

    private void updateSortingLabel() {
        try {
            switch (selectedSortType) {
                case NEWEST_FIRST:
                    tvSorting.setText(getString(R.string.newest_first));
                    break;

                case OLDEST_FIRST:
                    tvSorting.setText(getString(R.string.oldest_first));
                    break;

                case LARGEST_FIRST:
                    tvSorting.setText(getString(R.string.largest_first));
                    break;

                case SMALLEST_FIRST:
                    tvSorting.setText(getString(R.string.smallest_first));
                    break;
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "updateSortingLabel", e);
        }
    }

    private int getSortValue(SettingType type) {
        if (type == SettingType.NEWEST_FIRST) {
            return 1;
        } else if (type == SettingType.OLDEST_FIRST) {
            return 2;
        } else if (type == SettingType.LARGEST_FIRST) {
            return 3;
        } else if (type == SettingType.SMALLEST_FIRST) {
            return 4;
        }

        return 1;
    }

    private int getAttachmentFilterValue(SettingType type) {

        if (type == SettingType.ALL_BACKUP) {
            return 1;
        } else if (type == SettingType.WITH_ATTACHMENTS) {
            return 2;
        } else if (type == SettingType.WITHOUT_ATTACHMENTS) {
            return 3;
        }

        return 1;
    }

    private void showScanning() {
        loadingWrapper.setVisibility(View.VISIBLE);
        rvBackupHistory.setVisibility(View.GONE);
        emptyWrapper.setVisibility(View.GONE);
    }

    private void hideScanning() {
        loadingWrapper.setVisibility(View.GONE);
    }

    private void showOptionDialog(BackupFileModel backup) {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_restore_options, findViewById(android.R.id.content), false);
            LinearLayout optionDelete = bottomView.findViewById(R.id.optionDelete);
            LinearLayout optionRestore = bottomView.findViewById(R.id.optionRestore);
            LinearLayout optionShare = bottomView.findViewById(R.id.optionShare);

            // SHARE
            optionShare.setOnClickListener(v -> {
                dialog.dismiss();
                shareBackup(backup);
            });

            // RESTORE
            optionRestore.setOnClickListener(v -> {
                dialog.dismiss();
                showRestoreConfirmationDialog(backup);
            });

            // DELETE
            optionDelete.setOnClickListener(view -> {
                dialog.dismiss();
                showDeleteDialog(backup);
            });

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showOptionDialog", e);
        }
    }

    private void showRestoreConfirmationDialog(BackupFileModel backup) {
        if (backup == null || backup.uri == null) {
            Toast.makeText(this, R.string.invalid_backup_file, Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);

        MaterialCardView cardHeader = view.findViewById(R.id.cardHeader);
        AppCompatImageView headerImage = view.findViewById(R.id.headerImage);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        AppCompatTextView tvDelete = view.findViewById(R.id.tvDelete);
        tvTitle.setText(R.string.restore_backup_dialog);
        tvMessage.setText(getString(R.string.restore_backup_message, getString(R.string.app_name)));
        tvSubMessage.setText(getString(R.string.action_undone));
        tvSubMessage.setVisibility(View.VISIBLE);
        tvDelete.setText(getString(R.string.restore));
        tvDelete.setTextColor(ContextCompat.getColor(this, R.color.text_primary));

        cardHeader.setCardBackgroundColor(ContextCompat.getColor(this, R.color.backup_light));
        headerImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_refresh_time));
        headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.backup_dark)));

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());

        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            dialog.dismiss();
            openRestoreScreen(backup);
        });

        dialog.show();
    }

    private void openRestoreScreen(BackupFileModel backup) {

        try {
            if (backup == null || backup.uri == null) {
                Toast.makeText(this, R.string.invalid_backup_file, Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(BackupHistoryActivity.this, BackupRestoreActivity.class);
            intent.putExtra("backupUri", backup.uri.toString());
            intent.putExtra("backupFileName", backup.fileName);
            intent.putExtra("isFromHistory", true);
            startActivity(intent);
            ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
        } catch (Exception e) {
            AppLogger.e(getClass(), "openRestoreScreen", e);
            Toast.makeText(this, R.string.invalid_backup_file, Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteDialog(BackupFileModel backup) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        tvTitle.setText(R.string.delete_backup);
        tvMessage.setText(R.string.delete_backup_confirmation);
        tvSubMessage.setVisibility(View.GONE);
        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            deleteBackup(backup);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteBackup(BackupFileModel backup) {
        try {

            if (backup == null || backup.uri == null) {
                Toast.makeText(this, R.string.error_delete_backup, Toast.LENGTH_SHORT).show();
                return;
            }

            // =====================================================
            // FILE URI
            // =====================================================
            Uri uri = backup.uri;

            // =====================================================
            // FILE PATH
            // =====================================================
            File backupFile = new File(Objects.requireNonNull(uri.getPath()));

            if (!backupFile.exists()) {
                Toast.makeText(this, R.string.error_delete_backup, Toast.LENGTH_SHORT).show();
                return;
            }

            // =====================================================
            // DELETE
            // =====================================================
            boolean deleted = backupFile.delete();

            if (!deleted) {
                Toast.makeText(this, R.string.error_delete_backup, Toast.LENGTH_SHORT).show();
                return;
            }

            // =====================================================
            // SUCCESS
            // =====================================================
            Toast.makeText(this, R.string.backup_deleted, Toast.LENGTH_SHORT).show();

            // =====================================================
            // REMOVE FROM VIEWMODEL
            // =====================================================

            backupHistoryViewModel.deleteBackup(this, backup);
        } catch (Exception e) {
            AppLogger.e(getClass(), "deleteBackup", e);
            Toast.makeText(this, R.string.error_delete_backup, Toast.LENGTH_SHORT).show();
        }
    }

    private BackupFileModel convertToBackupFileModel(BackupHistoryEntity entity) {

        BackupFileModel model = new BackupFileModel();
        model.backupId = entity.backupId;
        model.fileName = entity.fileName;
        model.backupSize = entity.backupSize;
        model.databaseSize = entity.databaseSize;
        model.attachmentSize = entity.attachmentSize;
        model.createdAt = entity.createdAt;
        model.includeAttachments = entity.includeAttachments;
        model.location = entity.location;
        model.appVersion = entity.appVersion;
        model.databaseVersion = entity.databaseVersion;
        model.databaseChecksum = entity.databaseChecksum;
        if (entity.backupUri != null && !entity.backupUri.trim().isEmpty()) {
            model.uri = Uri.parse(entity.backupUri);
        }
        return model;
    }

    private void shareBackup(BackupFileModel backup) {

        if (backup == null || backup.uri == null) {
            Toast.makeText(this, R.string.backup_file_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File backupFile = new File(Objects.requireNonNull(backup.uri.getPath()));

            if (!backupFile.exists() || !backupFile.isFile()) {
                Toast.makeText(this, R.string.backup_file_not_found, Toast.LENGTH_SHORT).show();
                return;
            }

            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", backupFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/zip");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_backup)));
        } catch (Exception e) {
            AppLogger.e(getClass(), "shareBackup", e);
            Toast.makeText(this, R.string.unable_to_share_backup, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        return true;
    }

    private void requestStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            storageSettingsOpened = true;
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    private void showAccessDialog() {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);

        MaterialCardView cardHeader = view.findViewById(R.id.cardHeader);
        AppCompatImageView headerImage = view.findViewById(R.id.headerImage);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvDelete = view.findViewById(R.id.tvDelete);
        tvTitle.setText(R.string.storage_access_required);
        tvMessage.setText(getString(R.string.storage_access_required_message, getString(R.string.app_name)));
        tvDelete.setText(R.string.allow_access);
        tvDelete.setTextColor(ContextCompat.getColor(this, R.color.text_primary));

        cardHeader.setCardBackgroundColor(ContextCompat.getColor(this, R.color.category_light));
        headerImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_folder_access));
        headerImage.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.category_dark)));

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        view.findViewById(R.id.tvCancel).setVisibility(View.GONE);
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            dialog.dismiss();
            requestStorageAccess();
        });

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!storageSettingsOpened) {
            return;
        }

        storageSettingsOpened = false;

        if (hasStorageAccess()) {
            loadBackupHistory();
        } else {
            hideScanning();
            showAccessDialog();
        }
    }
}