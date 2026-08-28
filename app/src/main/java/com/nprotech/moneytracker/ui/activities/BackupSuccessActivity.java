package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;

import java.io.File;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BackupSuccessActivity extends BaseActivity {

    private AppCompatImageView icBack, ivShare;
    private AppCompatTextView tvBackupLocation, tvBackupFileName, tvBackupDateTime, tvBackupSize, tvBackupAttachments;
    private MaterialButton btnBackToHome, btnGoToRestore;
    private Uri backupUri;
    private String backupFileName, backupLocation;
    private long backupSize, backupCreatedAt;
    private boolean includeAttachments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_success);
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
            ivShare = toolbarWrapper.findViewById(R.id.ivShare);
            tvBackupLocation = findViewById(R.id.tvBackupLocation);
            tvBackupFileName = findViewById(R.id.tvBackupFileName);
            tvBackupDateTime = findViewById(R.id.tvBackupDateTime);
            tvBackupSize = findViewById(R.id.tvBackupSize);
            tvBackupAttachments = findViewById(R.id.tvBackupAttachments);
            btnBackToHome = findViewById(R.id.btnBackToHome);
            btnGoToRestore = findViewById(R.id.btnGoToRestore);

            tvTitle.setText(getString(R.string.backup_successful));
            ivShare.setVisibility(View.VISIBLE);

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

            if (!readBackupResult()) {
                return;
            }

            bindBackupDetails();
            setupListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private boolean readBackupResult() {
        Intent intent = getIntent();

        if (intent == null) {
            goBackToHome();
            return false;
        }

        String uriString = intent.getStringExtra("backupUri");

        if (uriString == null || uriString.trim().isEmpty()) {
            goBackToHome();
            return false;
        }

        try {
            backupUri = Uri.parse(uriString);
            backupFileName = intent.getStringExtra("fileName");
            backupLocation = intent.getStringExtra("location");
            backupSize = intent.getLongExtra("backupSize", 0);
            backupCreatedAt = intent.getLongExtra("createdAt", 0);
            includeAttachments = intent.getBooleanExtra("includeAttachments", false);

            if (backupFileName == null || backupFileName.trim().isEmpty()) {
                backupFileName = getString(R.string.app_backup, getString(R.string.app_name));
            }

            return true;
        } catch (Exception e) {
            AppLogger.e(getClass(), "readBackupResult", e);
            goBackToHome();
            return false;
        }
    }

    private void bindBackupDetails() {

        try {

            // -------------------------------------------------
            // Location
            // -------------------------------------------------
            if (backupLocation != null && !backupLocation.trim().isEmpty()) {
                tvBackupLocation.setText(backupLocation);
            } else {
                tvBackupLocation.setText(R.string.selected_location);
            }

            // -------------------------------------------------
            // File name
            // -------------------------------------------------
            tvBackupFileName.setText(backupFileName);

            // -------------------------------------------------
            // Date / Time
            // -------------------------------------------------
            tvBackupDateTime.setText(DateHelper.getFormattedDate(backupCreatedAt, "dd MMM yyyy • hh:mm a"));

            // -------------------------------------------------
            // Backup Size
            // -------------------------------------------------
            tvBackupSize.setText(Formatter.formatFileSize(this, backupSize));

            // -------------------------------------------------
            // Attachments
            // -------------------------------------------------
            if (includeAttachments) {
                tvBackupAttachments.setText(R.string.included);
            } else {
                tvBackupAttachments.setText(R.string.not_included);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindBackupDetails", e);
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

            ivShare.setOnClickListener(v -> shareBackup());

            btnGoToRestore.setOnClickListener(v -> openRestoreScreen());

            btnBackToHome.setOnClickListener(v -> finishWithTransition());
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void finishWithTransition() {
        try {
            Intent intent = new Intent(BackupSuccessActivity.this, ManageBackupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            ActivityUtils.overrideCloseTransition(BackupSuccessActivity.this, R.anim.scale_in, R.anim.right_to_left);
        } catch (Exception e) {
            AppLogger.e(getClass(), "finishWithTransition", e);
            finish();
        }
    }

    private void shareBackup() {

        if (backupUri == null) {Toast.makeText(this, R.string.backup_file_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri shareUri = backupUri;

            if ("file".equalsIgnoreCase(backupUri.getScheme())) {
                String path = backupUri.getPath();
                if (path == null || path.trim().isEmpty()) {
                    Toast.makeText(this, R.string.backup_file_not_found, Toast.LENGTH_SHORT).show();
                    return;
                }

                File backupFile = new File(path);

                if (!backupFile.exists() || !backupFile.isFile()) {
                    Toast.makeText(this, R.string.backup_file_not_found, Toast.LENGTH_SHORT).show();
                    return;
                }

                shareUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", backupFile);
            }

            if (!"content".equalsIgnoreCase(shareUri.getScheme())) {
                Toast.makeText(this, R.string.backup_file_not_found, Toast.LENGTH_SHORT).show();
                return;
            }

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/zip");
            shareIntent.putExtra(Intent.EXTRA_STREAM, shareUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_backup)));
        } catch (Exception e) {
            AppLogger.e(getClass(), "shareBackup", e);
            Toast.makeText(this, R.string.unable_to_share_backup, Toast.LENGTH_SHORT).show();
        }
    }

    private void goBackToHome() {
        try {
            Intent intent = new Intent(BackupSuccessActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            ActivityUtils.overrideCloseTransition(BackupSuccessActivity.this, R.anim.scale_in, R.anim.right_to_left);
        } catch (Exception e) {
            AppLogger.e(getClass(), "goBackToHome", e);
            finish();
        }
    }

    private void openRestoreScreen() {
        try {
            Intent intent = new Intent(BackupSuccessActivity.this, BackupRestoreActivity.class);
            startActivity(intent);
            finish();
            ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
        } catch (Exception e) {
            AppLogger.e(getClass(), "openRestoreScreen", e);
        }
    }
}