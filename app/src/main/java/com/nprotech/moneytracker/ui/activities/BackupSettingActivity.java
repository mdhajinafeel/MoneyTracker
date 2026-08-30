package com.nprotech.moneytracker.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.FrequencyModel;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.BackupManager;
import com.nprotech.moneytracker.worker.BackupWorker;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BackupSettingActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView tvSaveLocation, tvFrequency;
    private MaterialButton btnChangeLocation;
    private SwitchCompat switchAutoBackup;
    private ConstraintLayout autoFrequencyContainer;
    private ActivityResultLauncher<Intent> folderPickerLauncher;
    private int selectedFrequency = Constants.BACKUP_FREQUENCY_DAILY;
    private Typeface medium, semiBold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_setting);
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
            tvSaveLocation = findViewById(R.id.tvSaveLocation);
            btnChangeLocation = findViewById(R.id.btnChangeLocation);
            autoFrequencyContainer = findViewById(R.id.autoFrequencyContainer);
            switchAutoBackup = findViewById(R.id.switchAutoBackup);
            tvFrequency = findViewById(R.id.tvFrequency);

            tvTitle.setText(getString(R.string.backup_settings));

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

            medium = ResourcesCompat.getFont(this, R.font.exo2_medium);
            semiBold = ResourcesCompat.getFont(this, R.font.exo2_semibold);

            loadBackupSettings();
            setupListeners();
            setupLauncher();

            if (hasStorageAccess()) {
                prepareBackupDirectory();
                updateBackupDetails();
            } else {
                requestStorageAccess();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
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

            switchAutoBackup.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        try {
                            updateFrequencyEnabledState(isChecked);
                            PreferenceManager.INSTANCE.setAutoBackupEnabled(isChecked);
                            if (isChecked) {
                                int frequency = PreferenceManager.INSTANCE.getBackupFrequency();
                                BackupWorker.schedule(getApplicationContext(), frequency);
                            } else {
                                BackupWorker.cancel(getApplicationContext());
                            }
                            updateBackupDetails();
                        } catch (Exception e) {
                            AppLogger.e(getClass(), "switchAutoBackup", e);
                        }
                    }
            );

            btnChangeLocation.setOnClickListener(v -> openBackupLocationPicker());

            autoFrequencyContainer.setOnClickListener(v -> showFrequencyDialog());
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
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

    private void finishWithTransition() {
        try {
            finish();
            ActivityUtils.overrideCloseTransition(BackupSettingActivity.this, R.anim.scale_in, R.anim.right_to_left);
        } catch (Exception e) {
            AppLogger.e(getClass(), "finishWithTransition", e);
            finish();
        }
    }

    private Uri getSavedBackupLocation() {

        String location = PreferenceManager.INSTANCE.getBackupLocation();
        if (location.trim().isEmpty()) {
            return null;
        }

        try {
            return Uri.parse(location);
        } catch (Exception e) {
            AppLogger.e(getClass(), "getSavedBackupLocation", e);
            return null;
        }
    }

    private void prepareBackupDirectory() {
        try {
            if (getSavedBackupLocation() != null) {
                return;
            }

            BackupManager backupManager = new BackupManager(BackupSettingActivity.this);

            if (!backupManager.ensureBackupDirectoryExists()) {
                Toast.makeText(this, R.string.unable_backup_directory, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "prepareBackupDirectory", e);
        }
    }

    private void updateBackupDetails() {
        try {
            BackupManager backupManager = new BackupManager(BackupSettingActivity.this);

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

    private void openBackupLocationPicker() {
        try {
            if (!hasStorageAccess()) {
                requestStorageAccess();
            } else {
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
            }
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

    @SuppressLint("NotifyDataSetChanged")
    private void showFrequencyDialog() {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_calendar_filter_layout, findViewById(android.R.id.content), false);

            AppCompatTextView tvSelectRange = bottomView.findViewById(R.id.tvSelectRange);
            AppCompatTextView tvSelectRangeDesc = bottomView.findViewById(R.id.tvSelectRangeDesc);
            RecyclerView rvSelectRange = bottomView.findViewById(R.id.rvSelectRange);
            MaterialButton btnPrimary = bottomView.findViewById(R.id.btnPrimary);
            MaterialButton btnSecondary = bottomView.findViewById(R.id.btnSecondary);
            AppCompatTextView tvClose = bottomView.findViewById(R.id.tvClose);
            LinearLayout layoutActions = bottomView.findViewById(R.id.layoutActions);

            tvSelectRange.setText(getString(R.string.backup_frequency));
            tvSelectRangeDesc.setText(getString(R.string.frequency_desc));
            tvSelectRangeDesc.setVisibility(View.VISIBLE);
            layoutActions.setVisibility(View.VISIBLE);
            tvClose.setVisibility(View.VISIBLE);

            final int originalFrequency = PreferenceManager.INSTANCE.getBackupFrequency();
            selectedFrequency = originalFrequency;

            List<FrequencyModel> frequencyList = new ArrayList<>();
            frequencyList.add(new FrequencyModel(Constants.BACKUP_FREQUENCY_DAILY, R.drawable.ic_calendar_daily, getString(R.string.every_day)));
            frequencyList.add(new FrequencyModel(Constants.BACKUP_FREQUENCY_WEEKLY, R.drawable.ic_calendar_weekly, getString(R.string.every_week)));
            frequencyList.add(new FrequencyModel(Constants.BACKUP_FREQUENCY_MONTHLY, R.drawable.ic_calendar_monthly, getString(R.string.every_month)));

            RecyclerViewAdapter<FrequencyModel> backupFrequencyAdapter = new RecyclerViewAdapter<>(this, frequencyList, R.layout.item_calendar_filter) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, FrequencyModel frequencyModel) {
                    holder.setViewText(R.id.tvFilterName, frequencyModel.frequencyName);
                    AppCompatImageView ivSelected = holder.getView(R.id.ivSelected);
                    holder.setViewImageResource(R.id.ivIcon, frequencyModel.icon);
                    boolean selected = frequencyModel.frequency == selectedFrequency;
                    if (selected) {
                        holder.setViewTypeface(R.id.tvFilterName, semiBold);
                    } else {
                        holder.setViewTypeface(R.id.tvFilterName, medium);
                    }
                    ivSelected.setVisibility(selected ? View.VISIBLE : View.GONE);
                    holder.getView(R.id.rlFilterView).setOnClickListener(v -> {
                        selectedFrequency = frequencyModel.frequency;
                        notifyDataSetChanged();
                    });
                }
            };

            rvSelectRange.setAdapter(backupFrequencyAdapter);
            rvSelectRange.setItemAnimator(null);
            rvSelectRange.setHasFixedSize(true);

            btnPrimary.setOnClickListener(v -> {
                PreferenceManager.INSTANCE.setBackupFrequency(selectedFrequency);
                updateFrequencyUI();
                if (PreferenceManager.INSTANCE.isAutoBackupEnabled()) {
                    BackupWorker.schedule(getApplicationContext(), selectedFrequency);
                }
                dialog.dismiss();
            });

            btnSecondary.setOnClickListener(v -> {
                selectedFrequency = originalFrequency;
                dialog.dismiss();
            });

            tvClose.setOnClickListener(v -> {
                selectedFrequency = originalFrequency;
                dialog.dismiss();
            });

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showFrequencyDialog", e);
        }
    }

    private void loadBackupSettings() {
        try {
            boolean autoBackupEnabled = PreferenceManager.INSTANCE.isAutoBackupEnabled();
            selectedFrequency = PreferenceManager.INSTANCE.getBackupFrequency();
            switchAutoBackup.setChecked(autoBackupEnabled);
            updateFrequencyUI();
            updateFrequencyEnabledState(autoBackupEnabled);
        } catch (Exception e) {
            AppLogger.e(getClass(), "loadBackupSettings", e);
        }
    }

    private void updateFrequencyUI() {
        switch (selectedFrequency) {
            case Constants.BACKUP_FREQUENCY_WEEKLY:
                tvFrequency.setText(getString(R.string.every_week));
                break;
            case Constants.BACKUP_FREQUENCY_MONTHLY:
                tvFrequency.setText(getString(R.string.every_month));
                break;
            case Constants.BACKUP_FREQUENCY_DAILY:
            default:
                tvFrequency.setText(getString(R.string.every_day));
                break;
        }
    }

    private void updateFrequencyEnabledState(boolean enabled) {
        autoFrequencyContainer.setAlpha(enabled ? 1f : 0.5f);
        autoFrequencyContainer.setEnabled(enabled);
        tvFrequency.setTextColor(ContextCompat.getColor(this, enabled ? R.color.backup_dark : R.color.black));
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

    @Override
    protected void onResume() {
        super.onResume();
        if (hasStorageAccess()) {
            prepareBackupDirectory();
            updateBackupDetails();
        }
    }
}