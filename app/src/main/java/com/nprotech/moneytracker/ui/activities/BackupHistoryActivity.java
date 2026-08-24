package com.nprotech.moneytracker.ui.activities;

import android.os.Bundle;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.BackupHistoryEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.BackupHistoryViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BackupHistoryActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView tvTotalBackup, lblTotalBackup, tvAllBackup, tvSorting;
    private RecyclerView rvBackupHistory;
    private ConstraintLayout emptyWrapper;
    private RecyclerViewAdapter<BackupHistoryEntity> backupHistoryRecyclerViewAdapter;
    private BackupHistoryViewModel backupHistoryViewModel;
    private boolean isNewestFirst = true;

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
            bindData();
            observeData();
            setupListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {

            CommonUtils.setDrawable(this, tvSorting, R.drawable.ic_sort_desc, R.dimen.icon_12, R.color.primary_dark, Gravity.END);

            backupHistoryViewModel.backupCount().observe(this, count -> {
                if (count == null) {
                    count = 0;
                }

                tvTotalBackup.setText(String.valueOf(count));
                lblTotalBackup.setText(getResources().getQuantityString(R.plurals.total_backup_count, count, count));
                tvAllBackup.setText(getResources().getQuantityString(R.plurals.backup_count, count, count));
            });

            backupHistoryViewModel.loadBackupHistory(isNewestFirst);
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void observeData() {
        try {
            backupHistoryViewModel.getBackupHistoryList().observe(this, list -> {
                if (list == null || list.isEmpty()) {
                    rvBackupHistory.setVisibility(View.GONE);
                    emptyWrapper.setVisibility(View.VISIBLE);
                } else {
                    emptyWrapper.setVisibility(View.GONE);
                    rvBackupHistory.setVisibility(View.VISIBLE);
                    backupHistoryRecyclerViewAdapter.setItems(list);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "observeData", e);
        }
    }

    private void initializeAdapter() {
        try {
            backupHistoryRecyclerViewAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(), R.layout.item_backup_history) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, BackupHistoryEntity history) {
                    holder.setViewText(R.id.tvBackupName, getString(R.string.app_backup, getString(R.string.app_name)));

                    CommonUtils.setDrawable(BackupHistoryActivity.this, holder.getView(R.id.tvBackupDate), R.drawable.ic_calendar, R.dimen.icon_12, R.color.dark_grey, Gravity.START);
                    CommonUtils.setDrawable(BackupHistoryActivity.this, holder.getView(R.id.tvBackupTime), R.drawable.ic_time, R.dimen.icon_12, R.color.dark_grey, Gravity.START);
                    CommonUtils.setDrawable(BackupHistoryActivity.this, holder.getView(R.id.tvBackupSize), R.drawable.ic_database, R.dimen.icon_12, R.color.privacy_dark, Gravity.START);

                    holder.setViewText(R.id.tvBackupDate, DateHelper.getFormattedDate(history.createdAt, "dd MMM yyyy"));
                    holder.setViewText(R.id.tvBackupTime, DateHelper.getFormattedDate(history.createdAt, "hh:mm a"));
                    holder.setViewText(R.id.tvBackupSize, Formatter.formatFileSize(BackupHistoryActivity.this, history.backupSize));

                    AppCompatTextView tvBackupAttachment = holder.getView(R.id.tvBackupAttachment);
                    AppCompatTextView tvBackupLocation = holder.getView(R.id.tvBackupLocation);

                    if(history.includeAttachments) {
                        CommonUtils.setDrawable(BackupHistoryActivity.this, tvBackupAttachment, R.drawable.ic_complete, R.dimen.icon_12, R.color.dark_income, Gravity.START);
                        tvBackupAttachment.setText(R.string.attachments_included);
                    } else {
                        CommonUtils.setDrawable(BackupHistoryActivity.this, tvBackupAttachment, R.drawable.ic_cancel, R.dimen.icon_12, R.color.bright_red, Gravity.START);
                        tvBackupAttachment.setText(R.string.no_attachments);
                    }

                    CommonUtils.setDrawable(BackupHistoryActivity.this, tvBackupLocation, R.drawable.ic_folder_outline, R.dimen.icon_12, R.color.backup_dark, Gravity.START);
                    tvBackupLocation.setText(history.location);
                }
            };

            rvBackupHistory.setHasFixedSize(true);
            rvBackupHistory.setLayoutManager(new LinearLayoutManager(this));
            rvBackupHistory.setItemAnimator(null);
            rvBackupHistory.setNestedScrollingEnabled(false);
            rvBackupHistory.setAdapter(backupHistoryRecyclerViewAdapter);

            rvBackupHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm == null)
                        return;
                    int last = lm.findLastVisibleItemPosition();
                    if (last >= backupHistoryRecyclerViewAdapter.getItemCount() - 5) {
                        backupHistoryViewModel.loadNextPage(isNewestFirst);
                    }
                }
            });
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

            tvSorting.setOnClickListener(v -> {
                isNewestFirst = !isNewestFirst;
                backupHistoryViewModel.loadBackupHistory(isNewestFirst);

                if(isNewestFirst) {
                    CommonUtils.setDrawable(this, tvSorting, R.drawable.ic_sort_desc, R.dimen.icon_12, R.color.primary_dark, Gravity.END);
                    tvSorting.setText(getString(R.string.newest_first));
                } else {
                    CommonUtils.setDrawable(this, tvSorting, R.drawable.ic_sort_asc, R.dimen.icon_12, R.color.primary_dark, Gravity.END);
                    tvSorting.setText(getString(R.string.oldest_first));
                }
            });
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
}