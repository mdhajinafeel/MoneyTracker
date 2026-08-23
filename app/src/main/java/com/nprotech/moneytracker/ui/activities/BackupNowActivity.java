package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.BackupHistoryEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.BackupManager;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.BackupHistoryViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BackupNowActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView lblDesc, lblSecure, lblPrivate, lblLocalOnly, lblIncluded, lblDatabaseDesc, lblBackupHint, tvBackupSize, tvAttachmentSize, tvEstimatedSize, tvSaveLocation, tvBackupPercentage, tvBackupTitle, tvBackupProgress;
    private SwitchCompat switchAttachInclude;
    private MaterialButton btnChangeLocation, btnStartBackup;
    private MaterialCardView backupProgressContainer;
    private CircularProgressIndicator backupProgressBar;
    private ProgressBar backupLinearProgress;
    private ExecutorService backupExecutor;
    private ActivityResultLauncher<Intent> folderPickerLauncher;
    private BackupHistoryViewModel backupHistoryViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_now);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            View root = findViewById(R.id.rootView);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            lblDesc = findViewById(R.id.lblDesc);
            lblSecure = findViewById(R.id.lblSecure);
            lblPrivate = findViewById(R.id.lblPrivate);
            lblLocalOnly = findViewById(R.id.lblLocalOnly);
            lblIncluded = findViewById(R.id.lblIncluded);
            lblDatabaseDesc = findViewById(R.id.lblDatabaseDesc);
            lblBackupHint = findViewById(R.id.lblBackupHint);
            backupProgressContainer = findViewById(R.id.backupProgressContainer);
            switchAttachInclude = findViewById(R.id.switchAttachInclude);
            backupProgressBar = findViewById(R.id.backupProgressBar);
            tvBackupPercentage = findViewById(R.id.tvBackupPercentage);
            tvBackupTitle = findViewById(R.id.tvBackupTitle);
            tvBackupProgress = findViewById(R.id.tvBackupProgress);
            tvBackupSize = findViewById(R.id.tvBackupSize);
            tvAttachmentSize = findViewById(R.id.tvAttachmentSize);
            tvEstimatedSize = findViewById(R.id.tvEstimatedSize);
            tvSaveLocation = findViewById(R.id.tvSaveLocation);
            backupLinearProgress = findViewById(R.id.backupLinearProgress);
            btnChangeLocation = findViewById(R.id.btnChangeLocation);
            btnStartBackup = findViewById(R.id.btnStartBackup);
            tvTitle.setText(R.string.backup_now);

            // -----------------------------------------
            // Toolbar insets
            // -----------------------------------------
            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            // -----------------------------------------
            // Bottom system inset
            // -----------------------------------------
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            backupHistoryViewModel = new ViewModelProvider(this).get(BackupHistoryViewModel.class);

            bindData();
            setupListeners();
            setupLauncher();
            backupExecutor = Executors.newSingleThreadExecutor();
            hideBackupProgress();

            if (hasStorageAccess()) {
                prepareBackupDirectory();
            } else {
                requestStorageAccess();
            }

            updateBackupDetails();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {
            CommonUtils.setDrawable(this, lblSecure, R.drawable.ic_privacy_outline, R.dimen.icon_10, R.color.primary, Gravity.START);
            CommonUtils.setDrawable(this, lblPrivate, R.drawable.ic_lock, R.dimen.icon_10, R.color.primary, Gravity.START);
            CommonUtils.setDrawable(this, lblLocalOnly, R.drawable.ic_local, R.dimen.icon_10, R.color.primary, Gravity.START);
            CommonUtils.setDrawable(this, lblIncluded, R.drawable.ic_complete, R.dimen.icon_10, R.color.primary, Gravity.END);
            CommonUtils.setDrawable(this, lblBackupHint, R.drawable.ic_lock_key, R.dimen.icon_10, R.color.primary, Gravity.START);
            lblDesc.setText(getString(R.string.keep_data_safe_hint, getString(R.string.app_name)));
            lblDatabaseDesc.setText(getString(R.string.database_required_desc, getString(R.string.app_name)));
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
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

            switchAttachInclude.setOnCheckedChangeListener((buttonView, isChecked) -> updateBackupDetails());

            btnChangeLocation.setOnClickListener(v -> openBackupLocationPicker());

            btnStartBackup.setOnClickListener(v -> startBackup());
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void startBackup() {
        try {
            // -----------------------------------------
            // Prevent duplicate clicks
            // -----------------------------------------
            btnStartBackup.setClickable(false);

            // -----------------------------------------
            // Disable attachment switch
            // -----------------------------------------
            switchAttachInclude.setEnabled(false);

            // -----------------------------------------
            // Get attachment preference
            // -----------------------------------------
            boolean includeAttachments = switchAttachInclude.isChecked();

            // -----------------------------------------
            // Show progress UI
            // -----------------------------------------
            showBackupProgress();

            // -----------------------------------------
            // Reset progress
            // -----------------------------------------
            updateBackupProgress(0, "Preparing backup...");

            // -----------------------------------------
            // Run backup in background
            // -----------------------------------------
            backupExecutor.execute(() -> {
                try {
                    BackupManager backupManager = new BackupManager(BackupNowActivity.this);
                    BackupManager.BackupResult backupResult = backupManager.createBackup(includeAttachments, this::updateBackupProgress);

                    // ---------------------------------
                    // Verify backup
                    // ---------------------------------
                    if (backupResult == null || backupResult.uri() == null || backupResult.databaseSize() <= 0) {
                        runOnUiThread(() -> {
                            hideBackupProgress();
                            btnStartBackup.setClickable(true);
                            switchAttachInclude.setEnabled(true);
                            Toast.makeText(BackupNowActivity.this, R.string.backup_not_created, Toast.LENGTH_SHORT).show();
                        });

                        return;
                    }

                    saveBackupHistory(backupResult);
                } catch (Exception e) {
                    AppLogger.e(getClass(), "startBackup", e);
                    runOnUiThread(() -> {
                        hideBackupProgress();
                        btnStartBackup.setClickable(true);
                        switchAttachInclude.setEnabled(true);
                        showBackupError(e);
                    });
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "startBackup", e);
        }
    }

    private void showBackupError(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = getString(R.string.unable_backup);
        }
        Toast.makeText(this, getString(R.string.backup_failed_message) + message, Toast.LENGTH_SHORT).show();
    }

    private void finishWithTransition() {
        finish();
        ActivityUtils.overrideCloseTransition(BackupNowActivity.this, R.anim.scale_in, R.anim.right_to_left);
    }

    private void updateBackupDetails() {
        try {
            BackupManager backupManager = new BackupManager(BackupNowActivity.this);
            long databaseSize = backupManager.getDatabaseSize();
            long attachmentSize = 0;

            if (switchAttachInclude.isChecked()) {
                attachmentSize = backupManager.getAttachmentSize();
            }

            long estimatedSize = databaseSize + attachmentSize;

            tvBackupSize.setText(Formatter.formatFileSize(this, databaseSize));
            tvAttachmentSize.setText(Formatter.formatFileSize(this, attachmentSize));
            tvEstimatedSize.setText(Formatter.formatFileSize(this, estimatedSize));

            // -----------------------------------------
            // Save Location
            // -----------------------------------------
            Uri savedLocation = getSavedBackupLocation();

            if (savedLocation != null) {
                tvSaveLocation.setText(getBackupLocationDisplayPath(savedLocation));
            } else {
                tvSaveLocation.setText(backupManager.getBackupDirectoryDisplayPath());
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "updateBackupDetails", e);
        }
    }

    private void setupLauncher() {
        folderPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

            if (result.getResultCode() != RESULT_OK) {
                return;
            }

            Intent data = result.getData();

            if (data == null) {
                return;
            }

            Uri selectedUri = data.getData();
            if (selectedUri == null) {
                return;
            }

            handleSelectedBackupLocation(selectedUri);
        });
    }

    private void openBackupLocationPicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Uri initialUri = getSavedBackupLocation();
                if (initialUri == null) {
                    initialUri = getCurrentBackupFolderUri();
                }
                if (initialUri != null) {
                    intent.putExtra("android.provider.extra.INITIAL_URI", initialUri);
                }
            }
            folderPickerLauncher.launch(intent);
        } catch (Exception e) {
            AppLogger.e(getClass(), "openBackupLocationPicker", e);
        }
    }

    private Uri getCurrentBackupFolderUri() {

        try {
            return DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", "primary:" + getString(R.string.app_name) + "/Backups");
        } catch (Exception e) {
            AppLogger.e(getClass(), "getCurrentBackupFolderUri", e);
            return null;
        }
    }

    private void handleSelectedBackupLocation(Uri selectedUri) {
        try {
            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

            getContentResolver().takePersistableUriPermission(selectedUri, takeFlags);

            PreferenceManager.INSTANCE.setBackupLocation(selectedUri.toString());

            tvSaveLocation.setText(getBackupLocationDisplayPath(selectedUri));
            Toast.makeText(this, R.string.backup_location_changed, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "handleSelectedBackupLocation", e);
            Toast.makeText(this, R.string.unable_backup_directory, Toast.LENGTH_SHORT).show();
        }
    }

    private Uri getSavedBackupLocation() {

        String location =
                PreferenceManager.INSTANCE
                        .getBackupLocation();

        if (location.trim().isEmpty()) {

            return null;
        }

        try {

            return Uri.parse(location);

        } catch (Exception e) {

            AppLogger.e(
                    getClass(),
                    "getSavedBackupLocation",
                    e
            );

            return null;
        }
    }

    private String getBackupLocationDisplayPath(Uri uri) {

        try {

            String documentId = DocumentsContract.getTreeDocumentId(uri);

            if (documentId == null) {
                return getString(R.string.selected_location);
            }

            int separator = documentId.indexOf(':');

            if (separator >= 0) {
                String path = documentId.substring(separator + 1);

                if (path.startsWith("/")) {
                    path = path.substring(1);
                }

                return "Internal Storage/" + path;
            }

        } catch (Exception e) {
            AppLogger.e(getClass(), "getBackupLocationDisplayPath", e);
        }

        return getString(R.string.selected_location);
    }

    private void updateBackupProgress(int progress, String message) {

        int safeProgress = Math.max(0, Math.min(100, progress));

        runOnUiThread(() -> {

            // Circular
            backupProgressBar.setProgress(safeProgress);

            // Linear
            backupLinearProgress.setProgress(safeProgress);

            // Percentage
            tvBackupPercentage.setText(getString(R.string.backup_progress_percentage, safeProgress));

            // Current step
            tvBackupProgress.setText(message);

            // Title
            tvBackupTitle.setText(R.string.creating_backup);
        });
    }

    private void saveBackupHistory(BackupManager.BackupResult result) {
        if (result == null || result.uri() == null) {
            return;
        }

        String location = getBackupLocationDisplayPath(result.uri());
        BackupHistoryEntity backupHistory = new BackupHistoryEntity();
        backupHistory.backupUri = result.uri().toString();
        backupHistory.fileName = result.fileName();
        backupHistory.backupSize = result.backupSize();
        backupHistory.databaseSize = result.databaseSize();
        backupHistory.includeAttachments = result.isAttachmentIncluded();
        backupHistory.attachmentSize = result.attachmentSize();
        backupHistory.createdAt = System.currentTimeMillis();
        backupHistory.location = location;

        long backHistoryId = backupHistoryViewModel.insertBackupHistory(backupHistory);
        if(backHistoryId > 0) {
            runOnUiThread(() -> openSuccessScreen(backHistoryId));
        } else {
            finishWithTransition();
        }
    }

    private void openSuccessScreen(long backHistoryId) {
        Intent intent = new Intent(BackupNowActivity.this, BackupSuccessActivity.class);
        intent.putExtra("backHistoryId", backHistoryId);
        startActivity(intent);
        ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
    }

    private void showBackupProgress() {
        backupProgressContainer.setVisibility(View.VISIBLE);
    }

    private void hideBackupProgress() {
        backupProgressContainer.setVisibility(View.GONE);
    }

    private boolean hasStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        return true;
    }

    private void requestStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    private void prepareBackupDirectory() {
        try {
            // If user already selected a custom location,
            // don't touch the default directory.
            if (getSavedBackupLocation() != null) {
                return;
            }

            BackupManager backupManager = new BackupManager(BackupNowActivity.this);
            if (!backupManager.ensureBackupDirectoryExists()) {
                Toast.makeText(this, R.string.unable_backup_directory, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "prepareBackupDirectory", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasStorageAccess()) {
            prepareBackupDirectory();
            updateBackupDetails();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backupExecutor != null) {
            backupExecutor.shutdownNow();
            backupExecutor = null;
        }
    }
}