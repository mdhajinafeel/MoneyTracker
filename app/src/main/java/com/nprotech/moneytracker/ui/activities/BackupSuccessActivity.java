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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.BackupHistoryEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.viewmodel.BackupHistoryViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BackupSuccessActivity extends BaseActivity {

    private AppCompatImageView icBack, ivShare;
    private AppCompatTextView tvBackupLocation, tvBackupFileName, tvBackupDateTime, tvBackupSize, tvBackupAttachments;
    private MaterialButton btnBackToHome, btnGoToRestore;
    private BackupHistoryViewModel backupHistoryViewModel;
    private long backupHistoryId = 0;
    private BackupHistoryEntity backupHistory;

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

            backupHistoryViewModel = new ViewModelProvider(this).get(BackupHistoryViewModel.class);

            if (!readBackupHistoryId()) {
                return;
            }

            observeBackupHistory();
            setupListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindBackupHistory(BackupHistoryEntity history) {
        try {
            // -------------------------------------------------
            // Location
            // -------------------------------------------------

            tvBackupLocation.setText(
                    history.location
            );

            // -------------------------------------------------
            // File name
            // -------------------------------------------------
            tvBackupFileName.setText(history.fileName);

            // -------------------------------------------------
            // Date / Time
            // -------------------------------------------------
            tvBackupDateTime.setText(DateHelper.getFormattedDate(history.createdAt, "dd MMM yyyy • hh:mm a"));

            // -------------------------------------------------
            // Backup Size
            // -------------------------------------------------
            tvBackupSize.setText(Formatter.formatFileSize(this, history.backupSize));

            // -------------------------------------------------
            // Attachments
            // -------------------------------------------------
            if (history.includeAttachments) {
                tvBackupAttachments.setText(R.string.included);
            } else {
                tvBackupAttachments.setText(R.string.not_included);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindBackupHistory", e);
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

            btnGoToRestore.setOnClickListener(v -> {

            });

            btnBackToHome.setOnClickListener(v -> finishWithTransition());
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private boolean readBackupHistoryId() {

        Bundle bundle = getIntent().getExtras();

        if (bundle == null) {
            goBackToHome();
            return false;
        }

        backupHistoryId = bundle.getLong("backHistoryId", 0);
        if (backupHistoryId <= 0) {
            goBackToHome();
            return false;
        }

        return true;
    }

    private void observeBackupHistory() {

        backupHistoryViewModel.getBackupById(backupHistoryId).observe(this, history -> {
                    if (history == null) {
                        goBackToHome();
                        return;
                    }
                    backupHistory = history;
                    bindBackupHistory(history);
                }
        );
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

    // =========================================================
    // SHARE BACKUP
    // =========================================================

    private void shareBackup() {
        if (backupHistory == null) {
            Toast.makeText(this, R.string.backup_file_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri backupUri = Uri.parse(backupHistory.backupUri);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/zip");
            shareIntent.putExtra(Intent.EXTRA_STREAM, backupUri);
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
//            Intent intent = new Intent(BackupSuccessActivity.this, RestoreBackupActivity.class);
//            startActivity(intent);
//            ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
        } catch (Exception e) {
            AppLogger.e(getClass(), "openRestoreScreen", e);
        }
    }
}