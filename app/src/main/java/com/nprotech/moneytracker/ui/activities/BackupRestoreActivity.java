package com.nprotech.moneytracker.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.Formatter;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import com.google.android.material.progressindicator.CircularProgressIndicator;
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
import com.nprotech.moneytracker.utils.BackupManager;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.BackupHistoryViewModel;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BackupRestoreActivity extends BaseActivity {

    private AppCompatImageView icBack, ivDeviceSelected, ivAttachSelected, ivFileDeleted;
    private AppCompatTextView lblDesc, tvSorting, lblRestoreBottomHint, lblRestoreTips, lblSelectBackupTitle, tvSelectedFileName, tvFileDate, tvFileTime, tvFileSize,
            tvFileAttachment, tvBackupProgress, tvBackupPercentage;
    private MaterialCardView cardDevice, cardAttach, backupProgressContainer;
    private CircularProgressIndicator backupProgressBar;
    private ProgressBar backupLinearProgress;
    private MaterialButton btnRestoreBackup, btnBrowseFile, btnChangeFile;
    private RecyclerView rvBackupHistory;
    private ConstraintLayout emptyWrapper, noFileContainer, selectedFileContainer, attachFileWrapper, loadingWrapper;
    private RecyclerViewAdapter<BackupFileModel> backupHistoryRecyclerViewAdapter;
    private BackupHistoryViewModel backupHistoryViewModel;
    private int selectedOptions = 1;
    private BackupFileModel selectedBackup = null;
    private SettingType selectedSortType = SettingType.NEWEST_FIRST;
    private SettingType selectedAttachmentType = SettingType.ALL_BACKUP;
    private ActivityResultLauncher<Intent> backupFilePickerLauncher;
    private Uri selectedBackupFileUri;
    private BackupManager backupManager;
    private Uri preselectedBackupUri;
    private boolean isFromHistory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_restore);
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
            lblDesc = findViewById(R.id.lblDesc);
            rvBackupHistory = findViewById(R.id.rvBackupHistory);
            emptyWrapper = findViewById(R.id.emptyWrapper);
            tvSorting = findViewById(R.id.tvSorting);
            cardDevice = findViewById(R.id.cardDevice);
            cardAttach = findViewById(R.id.cardAttach);
            ivDeviceSelected = findViewById(R.id.ivDeviceSelected);
            ivAttachSelected = findViewById(R.id.ivAttachSelected);
            btnRestoreBackup = findViewById(R.id.btnRestoreBackup);
            lblRestoreBottomHint = findViewById(R.id.lblRestoreBottomHint);
            lblRestoreTips = findViewById(R.id.lblRestoreTips);
            noFileContainer = findViewById(R.id.noFileContainer);
            selectedFileContainer = findViewById(R.id.selectedFileContainer);
            attachFileWrapper = findViewById(R.id.attachFileWrapper);
            lblSelectBackupTitle = findViewById(R.id.lblSelectBackupTitle);
            btnBrowseFile = findViewById(R.id.btnBrowseFile);
            btnChangeFile = findViewById(R.id.btnChangeFile);
            ivFileDeleted = findViewById(R.id.ivFileDeleted);
            tvSelectedFileName = findViewById(R.id.tvSelectedFileName);
            tvFileDate = findViewById(R.id.tvFileDate);
            tvFileTime = findViewById(R.id.tvFileTime);
            tvFileSize = findViewById(R.id.tvFileSize);
            tvFileAttachment = findViewById(R.id.tvFileAttachment);
            backupProgressContainer = findViewById(R.id.backupProgressContainer);
            backupProgressBar = findViewById(R.id.backupProgressBar);
            backupLinearProgress = findViewById(R.id.backupLinearProgress);
            tvBackupProgress = findViewById(R.id.tvBackupProgress);
            tvBackupPercentage = findViewById(R.id.tvBackupPercentage);
            loadingWrapper = findViewById(R.id.loadingWrapper);

            tvTitle.setText(getString(R.string.restore_backup));

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
            backupManager = new BackupManager(this);

            initializeAdapter();
            readPreselectedBackup();
            bindData();
            observeData();
            setupListeners();
            setupLauncher();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {

        try {

            lblDesc.setText(getString(R.string.restore_data_desc, getString(R.string.app_name)));

            CommonUtils.setDrawable(this, tvSorting, R.drawable.ic_filter, R.dimen.icon_12, R.color.primary_dark, Gravity.START);
            CommonUtils.setDrawable(this, lblRestoreBottomHint, R.drawable.ic_lock, R.dimen.icon_8, R.color.dark_grey, Gravity.START);

            String tipsAttachFile = getString(R.string.tips_attach_file, getString(R.string.app_name));
            SpannableString spannableString = new SpannableString(tipsAttachFile);
            String boldText = getString(R.string.tips);
            int start = tipsAttachFile.indexOf(boldText);
            if (start >= 0) {
                spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, start + boldText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            lblRestoreTips.setText(spannableString);

            // =====================================================
            // Source UI
            // =====================================================
            updateSourceSelection();

            // =====================================================
            // Start scanning
            // =====================================================
            showScanning();

            backupHistoryViewModel.scanBackups(this, getSortValue(selectedSortType), getAttachmentFilterValue(selectedAttachmentType));
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void observeData() {

        try {

            backupHistoryViewModel.getBackupHistoryList().observe(this, entities -> {
                        try {
                            if (entities == null || entities.isEmpty()) {
                                selectedBackup = null;
                                if (loadingWrapper.getVisibility() != View.VISIBLE) {
                                    rvBackupHistory.setVisibility(View.GONE);
                                    emptyWrapper.setVisibility(View.VISIBLE);
                                }

                                btnRestoreBackup.setEnabled(false);
                                btnRestoreBackup.setAlpha(0.5f);
                                return;
                            }

                            List<BackupFileModel> list = new ArrayList<>();

                            for (BackupHistoryEntity entity : entities) {
                                if (entity == null) {
                                    continue;
                                }
                                BackupFileModel model = convertToBackupFileModel(entity);
                                if (model != null) {
                                    list.add(model);
                                }
                            }

                            if (list.isEmpty()) {
                                selectedBackup = null;
                                loadingWrapper.setVisibility(View.GONE);
                                rvBackupHistory.setVisibility(View.GONE);
                                emptyWrapper.setVisibility(View.VISIBLE);
                                btnRestoreBackup.setEnabled(false);
                                btnRestoreBackup.setAlpha(0.5f);
                                return;
                            }

                            loadingWrapper.setVisibility(View.GONE);
                            emptyWrapper.setVisibility(View.GONE);
                            rvBackupHistory.setVisibility(View.VISIBLE);

                            if (isFromHistory) {
                                selectPreselectedBackup(list);
                                if (selectedBackup == null) {
                                    selectedBackup = list.get(0);
                                }
                            } else {
                                selectedBackup = list.get(0);
                            }

                            backupHistoryRecyclerViewAdapter.setItems(list);
                            updateRestoreButtonState();
                        } catch (Exception e) {

                            AppLogger.e(
                                    getClass(),
                                    "observeData - DB",
                                    e
                            );
                        }
                    }
            );
        } catch (Exception e) {
            AppLogger.e(getClass(), "observeData", e);
        }
    }

    private BackupFileModel convertToBackupFileModel(BackupHistoryEntity entity) {
        if (entity == null) {
            return null;
        }

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

    private void initializeAdapter() {
        try {
            backupHistoryRecyclerViewAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(), R.layout.item_backup_history) {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onPostBindViewHolder(ViewHolder holder, BackupFileModel backup) {
                    AppCompatImageView ivChecked = holder.getView(R.id.ivChecked);
                    MaterialCardView cardBackup = holder.getView(R.id.cardBackup);

                    boolean isSelected = selectedBackup != null && selectedBackup.backupId != null && selectedBackup.backupId.equals(backup.backupId);

                    ivChecked.setVisibility(View.VISIBLE);
                    holder.setViewVisibility(R.id.ivMore, View.GONE);

                    if (isSelected) {
                        ivChecked.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_radio_checked));
                        cardBackup.setCardBackgroundColor(getColor(R.color.very_light_lavender));
                        cardBackup.setStrokeColor(getColor(R.color.primary_light));
                        cardBackup.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.stroke_width_0_5));
                        cardBackup.setCardElevation(0);
                    } else {
                        ivChecked.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_radio_unchecked));
                        cardBackup.setCardBackgroundColor(getColor(R.color.white));
                        cardBackup.setStrokeColor(getColor(R.color.colorTransparent));
                        cardBackup.setStrokeWidth(0);
                        cardBackup.setCardElevation(getResources().getDimensionPixelSize(R.dimen.corner_elevation_4));
                    }

                    holder.setViewText(R.id.tvBackupName, getString(R.string.app_backup, getString(R.string.app_name)));

                    CommonUtils.setDrawable(BackupRestoreActivity.this, holder.getView(R.id.tvBackupDate), R.drawable.ic_calendar,
                            R.dimen.icon_12, R.color.dark_grey, Gravity.START);

                    CommonUtils.setDrawable(BackupRestoreActivity.this, holder.getView(R.id.tvBackupTime), R.drawable.ic_time,
                            R.dimen.icon_12, R.color.dark_grey, Gravity.START);

                    CommonUtils.setDrawable(BackupRestoreActivity.this, holder.getView(R.id.tvBackupSize), R.drawable.ic_database,
                            R.dimen.icon_12, R.color.privacy_dark, Gravity.START);

                    holder.setViewText(R.id.tvBackupDate, DateHelper.getFormattedDate(backup.createdAt, "dd MMM yyyy"));
                    holder.setViewText(R.id.tvBackupTime, DateHelper.getFormattedDate(backup.createdAt, "hh:mm a"));
                    holder.setViewText(R.id.tvBackupSize, Formatter.formatFileSize(BackupRestoreActivity.this, backup.backupSize));

                    AppCompatTextView tvBackupAttachment = holder.getView(R.id.tvBackupAttachment);
                    AppCompatTextView tvBackupLocation = holder.getView(R.id.tvBackupLocation);
                    if (backup.includeAttachments) {
                        CommonUtils.setDrawable(BackupRestoreActivity.this, tvBackupAttachment, R.drawable.ic_complete,
                                R.dimen.icon_12, R.color.dark_income, Gravity.START);
                        tvBackupAttachment.setText(R.string.attachments_included);

                    } else {

                        CommonUtils.setDrawable(BackupRestoreActivity.this, tvBackupAttachment, R.drawable.ic_cancel,
                                R.dimen.icon_12, R.color.bright_red, Gravity.START);
                        tvBackupAttachment.setText(R.string.no_attachments);
                    }

                    CommonUtils.setDrawable(BackupRestoreActivity.this, tvBackupLocation, R.drawable.ic_folder_outline,
                            R.dimen.icon_12, R.color.backup_dark, Gravity.START);

                    tvBackupLocation.setText(backup.location);

                    cardBackup.setOnClickListener(v -> {
                                selectedBackup = backup;
                                notifyDataSetChanged();
                                updateRestoreButtonState();
                            }
                    );
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

            cardDevice.setOnClickListener(v -> {
                if (selectedOptions != 1) {
                    selectedOptions = 1;
                    updateSourceSelection();
                }
            });

            cardAttach.setOnClickListener(v -> {
                if (selectedOptions != 2) {
                    selectedOptions = 2;
                    updateSourceSelection();
                }
            });

            btnRestoreBackup.setOnClickListener(v -> showRestoreDialog());

            btnBrowseFile.setOnClickListener(v -> openBackupFilePicker());

            btnChangeFile.setOnClickListener(v -> openBackupFilePicker());

            ivFileDeleted.setOnClickListener(v -> {
                selectedFileContainer.setVisibility(View.GONE);
                noFileContainer.setVisibility(View.VISIBLE);

                btnRestoreBackup.setEnabled(false);
                btnRestoreBackup.setAlpha(0.5f);
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void setupLauncher() {
        try {
            backupFilePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() != RESULT_OK || result.getData() == null || result.getData().getData() == null) {
                            return;
                        }

                        Intent data = result.getData();
                        Uri uri = data.getData();

                        try {
                            int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                            if ((takeFlags & Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0) {
                                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            }

                        } catch (SecurityException e) {
                            AppLogger.e(getClass(), "backupFilePickerLauncher", e);
                        }

                        validateSelectedBackupFile(uri);
                    }
            );
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupLauncher", e);
        }
    }

    private void openBackupFilePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/zip", "application/x-zip-compressed"});
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            backupFilePickerLauncher.launch(intent);
        } catch (Exception e) {
            AppLogger.e(getClass(), "openBackupFilePicker", e);
        }
    }

    private void validateSelectedBackupFile(Uri uri) {
        String fileName = getFileName(uri);
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            Toast.makeText(this, R.string.only_zip_files_supported, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean databaseFound = false;
        boolean attachmentsIncluded = false;

        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {

            if (inputStream == null) {
                return;
            }

            try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    String entryName = entry.getName();
                    // ---------------------------------------------
                    // Database
                    // ---------------------------------------------
                    String entryFileName = new File(entryName).getName();
                    if (entryFileName.equals(getString(R.string.app_name) + "_db".toLowerCase())) {
                        databaseFound = true;
                    }

                    // ---------------------------------------------
                    // Attachments folder
                    // ---------------------------------------------
                    String normalizedPath = entryName.toLowerCase(Locale.ROOT);
                    if (normalizedPath.startsWith("attachments/")) {
                        attachmentsIncluded = true;
                    }
                    zipInputStream.closeEntry();
                }
            }

        } catch (Exception e) {
            AppLogger.e(getClass(), "validateSelectedBackupFile", e);
            Toast.makeText(this, R.string.invalid_backup_file, Toast.LENGTH_SHORT).show();
            return;
        }

        // ---------------------------------------------
        // Database is mandatory
        // ---------------------------------------------
        if (!databaseFound) {
            Toast.makeText(this, R.string.invalid_backup_file, Toast.LENGTH_SHORT).show();
            return;
        }

        selectedBackupFileUri = uri;
        showSelectedBackupFile(uri, attachmentsIncluded);
        updateRestoreButtonState();
    }

    private void showSelectedBackupFile(Uri uri, boolean attachmentsIncluded) {
        try {
            noFileContainer.setVisibility(View.GONE);
            selectedFileContainer.setVisibility(View.VISIBLE);

            // ---------------------------------------------
            // File name
            // ---------------------------------------------
            String fileName = getFileName(uri);
            if (fileName != null) {
                tvSelectedFileName.setText(getString(R.string.app_backup, getString(R.string.app_name)));
            }

            // ---------------------------------------------
            // File size
            // ---------------------------------------------
            long fileSize = getFileSize(uri);
            tvFileSize.setText(Formatter.formatFileSize(this, fileSize));

            // ---------------------------------------------
            // Date / Time
            // ---------------------------------------------
            long backupDate = getBackupDateFromFileName(fileName);
            if (backupDate > 0) {
                tvFileDate.setText(DateHelper.getFormattedDate(backupDate, "dd MMM yyyy"));
                tvFileTime.setText(DateHelper.getFormattedDate(backupDate, "hh:mm a"));
            }

            // ---------------------------------------------
            // Attachments
            // ---------------------------------------------
            if (attachmentsIncluded) {
                CommonUtils.setDrawable(BackupRestoreActivity.this, tvFileAttachment, R.drawable.ic_complete, R.dimen.icon_12, R.color.dark_income, Gravity.START);
                tvFileAttachment.setText(R.string.attachments_included);
            } else {
                CommonUtils.setDrawable(BackupRestoreActivity.this, tvFileAttachment, R.drawable.ic_cancel, R.dimen.icon_12, R.color.bright_red, Gravity.START);
                tvFileAttachment.setText(R.string.no_attachments);
            }

        } catch (Exception e) {
            AppLogger.e(getClass(), "showSelectedBackupFile", e);
        }
    }

    private void finishWithTransition() {
        try {
            if (!isFromHistory) {
                Intent intent = new Intent(BackupRestoreActivity.this, ManageBackupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
            finish();
            ActivityUtils.overrideCloseTransition(BackupRestoreActivity.this, R.anim.scale_in, R.anim.right_to_left);
        } catch (Exception e) {
            AppLogger.e(getClass(), "finishWithTransition", e);
            finish();
        }
    }

    private void updateSourceSelection() {
        try {

            if (selectedOptions == 1) {

                // =====================================================
                // DEVICE STORAGE SELECTED
                // =====================================================
                cardDevice.setCardBackgroundColor(getColor(R.color.very_light_lavender));
                cardDevice.setStrokeColor(getColor(R.color.primary_light));
                cardDevice.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.stroke_width_1));

                cardAttach.setCardBackgroundColor(getColor(R.color.white));
                cardAttach.setStrokeColor(getColor(R.color.colorTransparent));
                cardAttach.setStrokeWidth(0);

                ivDeviceSelected.setVisibility(View.VISIBLE);
                ivAttachSelected.setVisibility(View.GONE);

                lblSelectBackupTitle.setText(getString(R.string.available_backups));
                tvSorting.setVisibility(View.VISIBLE);
                rvBackupHistory.setVisibility(View.VISIBLE);
                attachFileWrapper.setVisibility(View.GONE);
                selectedFileContainer.setVisibility(View.GONE);
                noFileContainer.setVisibility(View.GONE);
                loadingWrapper.setVisibility(View.GONE);

                updateRestoreButtonState();
            } else {

                // =====================================================
                // ATTACH FILE SELECTED
                // =====================================================

                cardAttach.setCardBackgroundColor(getColor(R.color.very_light_lavender));
                cardAttach.setStrokeColor(getColor(R.color.primary_light));
                cardAttach.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.stroke_width_1));

                cardDevice.setCardBackgroundColor(getColor(R.color.white));
                cardDevice.setStrokeColor(getColor(R.color.colorTransparent));
                cardDevice.setStrokeWidth(0);

                ivDeviceSelected.setVisibility(View.GONE);
                ivAttachSelected.setVisibility(View.VISIBLE);

                lblSelectBackupTitle.setText(getString(R.string.select_backup_file));
                tvSorting.setVisibility(View.GONE);
                rvBackupHistory.setVisibility(View.GONE);
                emptyWrapper.setVisibility(View.GONE);
                attachFileWrapper.setVisibility(View.VISIBLE);
                noFileContainer.setVisibility(View.VISIBLE);
                selectedFileContainer.setVisibility(View.GONE);

                btnRestoreBackup.setEnabled(false);
                btnRestoreBackup.setAlpha(0.5f);

                loadingWrapper.setVisibility(View.GONE);
            }

            CommonUtils.setDrawable(BackupRestoreActivity.this, tvFileDate, R.drawable.ic_calendar, R.dimen.icon_12, R.color.dark_grey, Gravity.START);
            CommonUtils.setDrawable(BackupRestoreActivity.this, tvFileTime, R.drawable.ic_time, R.dimen.icon_12, R.color.dark_grey, Gravity.START);
            CommonUtils.setDrawable(BackupRestoreActivity.this, tvFileSize, R.drawable.ic_database, R.dimen.icon_12, R.color.privacy_dark, Gravity.START);
        } catch (Exception e) {
            AppLogger.e(getClass(), "updateSourceSelection", e);
        }
    }

    private void updateRestoreButtonState() {

        boolean enabled;

        if (selectedOptions == 1) {
            enabled = selectedBackup != null && selectedBackup.uri != null;

        } else {
            enabled = selectedBackupFileUri != null;
        }

        btnRestoreBackup.setEnabled(enabled);
        btnRestoreBackup.setAlpha(enabled ? 1f : 0.5f);
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

    private String getFileName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri,
                new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    return cursor.getString(index);
                }
            }

        } catch (Exception e) {
            AppLogger.e(getClass(), "getFileName", e);

        }
        return null;
    }

    private long getFileSize(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, new String[]{OpenableColumns.SIZE}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (index >= 0 && !cursor.isNull(index)) {
                    return cursor.getLong(index);
                }
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "getFileSize", e);
        }
        return 0;
    }

    private long getBackupDateFromFileName(String fileName) {
        try {
            if (fileName == null || fileName.isEmpty()) {
                return 0;
            }

            String name = fileName;

            if (name.toLowerCase(Locale.ROOT).endsWith(".zip")) {
                name = name.substring(0, name.length() - 4);
            }

            // Expected:
            // expenixo_backup_2026-08-26_15-27-30
            int backupIndex = name.indexOf("_backup_");

            if (backupIndex < 0) {
                return 0;
            }

            String datePart = name.substring(backupIndex + "_backup_".length());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
            format.setLenient(false);
            Date date = format.parse(datePart);
            return date != null ? date.getTime() : 0;
        } catch (Exception e) {
            AppLogger.e(getClass(), "getBackupDateFromFileName", e);

            return 0;
        }
    }

    private void restoreBackup() {
        try {
            if (selectedOptions == 1) {

                // =====================================================
                // DEVICE STORAGE
                // =====================================================
                if (selectedBackup == null || selectedBackup.uri == null) {
                    Toast.makeText(this, R.string.invalid_backup_file, Toast.LENGTH_SHORT).show();
                    return;
                }

                restoreFromAttachedFile(selectedBackup.uri);
            } else {

                // =====================================================
                // ATTACH FILE
                // =====================================================
                if (selectedBackupFileUri == null) {
                    Toast.makeText(this, R.string.invalid_backup_file, Toast.LENGTH_SHORT).show();
                    return;
                }

                restoreFromAttachedFile(selectedBackupFileUri);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "restoreBackup", e);
            Toast.makeText(this, R.string.invalid_backup_file, Toast.LENGTH_SHORT).show();
        }
    }

    private void restoreFromAttachedFile(Uri backupUri) {

        if (backupUri == null) {
            Toast.makeText(this, R.string.invalid_backup_file, Toast.LENGTH_SHORT).show();
            return;
        }

        showRestoreProgressScreen();
        updateRestoreProgress(0, getString(R.string.preparing_restore));

        backupProgressContainer.post(() -> new Thread(() -> backupManager.restoreBackup(backupUri,
                new BackupManager.RestoreProgressListener() {
                    @Override
                    public void onProgress(int progress, String message) {
                        runOnUiThread(() ->
                                updateRestoreProgress(progress, message));
                    }

                    @Override
                    public void onCompleted() {
                        runOnUiThread(() -> {
                            Toast.makeText(BackupRestoreActivity.this, R.string.backup_restored_successfully, Toast.LENGTH_SHORT).show();
                            restartApp();
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            hideRestoreProgress();
                            Toast.makeText(BackupRestoreActivity.this, message, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        )).start());
    }

    private void restartApp() {
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
        Runtime.getRuntime().exit(0);
    }

    private void showRestoreProgressScreen() {

        // Hide source selection
        cardDevice.setVisibility(View.GONE);
        cardAttach.setVisibility(View.GONE);

        // Hide backup selection
        lblSelectBackupTitle.setVisibility(View.GONE);
        tvSorting.setVisibility(View.GONE);
        rvBackupHistory.setVisibility(View.GONE);
        emptyWrapper.setVisibility(View.GONE);
        attachFileWrapper.setVisibility(View.GONE);

        // Show progress
        backupProgressContainer.setVisibility(View.VISIBLE);

        backupProgressBar.setVisibility(View.VISIBLE);
        backupLinearProgress.setVisibility(View.VISIBLE);
        tvBackupProgress.setVisibility(View.VISIBLE);
        tvBackupPercentage.setVisibility(View.VISIBLE);

        // Disable restore
        btnRestoreBackup.setEnabled(false);
        btnRestoreBackup.setAlpha(0.5f);
    }

    private void hideRestoreProgress() {
        backupProgressContainer.setVisibility(View.GONE);

        btnRestoreBackup.setEnabled(true);
        btnRestoreBackup.setAlpha(1f);
    }

    private void updateRestoreProgress(int progress, String message) {
        backupProgressBar.setProgress(progress);
        backupLinearProgress.setProgress(progress);
        tvBackupPercentage.setText(getString(R.string.backup_progress_percentage, progress));
        tvBackupProgress.setText(message);
    }

    private void showScanning() {
        loadingWrapper.setVisibility(View.VISIBLE);
        rvBackupHistory.setVisibility(View.GONE);
        emptyWrapper.setVisibility(View.GONE);
    }

    private void showRestoreDialog() {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);
        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        AppCompatTextView tvDelete = view.findViewById(R.id.tvDelete);
        tvTitle.setText(R.string.restore_backup_dialog);
        tvMessage.setText(getString(R.string.restore_backup_message, getString(R.string.app_name)));
        tvSubMessage.setText(R.string.action_undone);
        tvDelete.setText(getString(R.string.restore));
        tvSubMessage.setVisibility(View.VISIBLE);
        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            restoreBackup();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void readPreselectedBackup() {

        try {

            isFromHistory = getIntent().getBooleanExtra("isFromHistory", false);

            selectedBackup = null;
            preselectedBackupUri = null;

            if (!isFromHistory) {
                return;
            }

            String uriString = getIntent().getStringExtra("backupUri");
            if (uriString == null || uriString.trim().isEmpty()) {
                return;
            }
            preselectedBackupUri = Uri.parse(uriString);
        } catch (Exception e) {

            AppLogger.e(
                    getClass(),
                    "readPreselectedBackup",
                    e
            );
        }
    }

    private void selectPreselectedBackup(List<BackupFileModel> list) {

        if (list == null || list.isEmpty()) {
            return;
        }

        if (preselectedBackupUri != null) {
            for (BackupFileModel backup : list) {

                if (backup == null || backup.uri == null) {

                    continue;
                }

                if (preselectedBackupUri.toString().equals(backup.uri.toString())) {
                    selectedBackup = backup;
                    return;
                }
            }
        }

        selectedBackup = null;
    }
}