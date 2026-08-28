package com.nprotech.moneytracker.viewmodel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.BackupHistoryEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.models.BackupFileModel;
import com.nprotech.moneytracker.repositories.BackupHistoryRepository;
import com.nprotech.moneytracker.utils.BackupScanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BackupHistoryViewModel extends ViewModel {

    private final BackupHistoryRepository backupHistoryRepository;
    private boolean loading = false, hasMore = true;
    private static final int PAGE_SIZE = 100;
    private int currentPage = 0, currentSortType, currentAttachmentType;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<BackupHistoryEntity>> backupHistoryList = new MutableLiveData<>(new ArrayList<>());
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public BackupHistoryViewModel(BackupHistoryRepository backupHistoryRepository) {
        this.backupHistoryRepository = backupHistoryRepository;
    }

    public LiveData<List<BackupHistoryEntity>> getBackupHistoryList() {
        return backupHistoryList;
    }

    public void loadBackupHistory(int sortType, int attachmentType) {
        mainHandler.post(() -> {
            currentSortType = sortType;
            currentAttachmentType = attachmentType;
            currentPage = 0;
            hasMore = true;
            backupHistoryList.setValue(new ArrayList<>());
            loadNextPage();
        });
    }

    public void loadNextPage() {
        if (loading || !hasMore) {
            return;
        }
        loading = true;

        final int sortType = currentSortType;
        final int attachmentType = currentAttachmentType;

        executor.execute(() -> {
            try {
                List<BackupHistoryEntity> pageData = backupHistoryRepository.getAllBackups(sortType, attachmentType, currentPage, PAGE_SIZE);
                if (pageData.size() < PAGE_SIZE) {
                    hasMore = false;
                }

                List<BackupHistoryEntity> currentList = backupHistoryList.getValue();
                if (currentList == null) {
                    currentList = new ArrayList<>();
                } else {
                    currentList = new ArrayList<>(currentList);
                }
                currentList.addAll(pageData);

                backupHistoryList.postValue(currentList);
                currentPage++;
            } catch (Exception e) {
                AppLogger.e(getClass(), "loadNextPage", e);
            } finally {
                loading = false;
            }
        });
    }

    public void scanBackups(Context context, int sortType, int attachmentFilter) {

        currentSortType = sortType;
        currentAttachmentType = attachmentFilter;

        executor.execute(() -> {
            try {
                BackupScanner scanner = new BackupScanner(context);
                scanner.scan(new BackupScanner.BackupScanListener() {
                    @Override
                    public void onBackupFound(BackupFileModel backup) {
                        saveBackupToDatabase(backup);
                    }

                    @Override
                    public void onScanCompleted() {
                        loadBackupHistory(currentSortType, currentAttachmentType);
                    }

                    @Override
                    public void onScanError(Exception e) {
                        AppLogger.e(getClass(), "scanBackups", e);
                        loadBackupHistory(currentSortType, currentAttachmentType);
                    }
                });
            } catch (Exception e) {
                AppLogger.e(getClass(), "scanBackups", e);
            }
        });
    }

    private void saveBackupToDatabase(BackupFileModel backup) {

        if (backup == null) {
            return;
        }

        if (backup.backupId == null || backup.backupId.trim().isEmpty()) {
            return;
        }

        try {
            BackupHistoryEntity entity = convertToEntity(backup);

            backupHistoryRepository.insertBackupHistory(entity);
        } catch (Exception e) {
            AppLogger.e(getClass(), "saveBackupToDatabase", e);
        }
    }

    private BackupHistoryEntity convertToEntity(BackupFileModel backup) {
        BackupHistoryEntity entity = new BackupHistoryEntity();
        entity.backupId = backup.backupId;
        entity.fileName = backup.fileName;
        entity.backupUri = backup.uri != null ? backup.uri.toString() : null;
        entity.backupSize = backup.backupSize;
        entity.databaseSize = backup.databaseSize;
        entity.attachmentSize = backup.attachmentSize;
        entity.createdAt = backup.createdAt;
        entity.includeAttachments = backup.includeAttachments;
        entity.location = backup.location;
        entity.appVersion = backup.appVersion;
        entity.databaseVersion = backup.databaseVersion;
        entity.databaseChecksum = backup.databaseChecksum;
        return entity;
    }

    public void deleteBackup(Context context, BackupFileModel backup) {

        if (backup == null) {
            return;
        }

        executor.execute(() -> {
            try {
                boolean fileDeleted = true;

                if (backup.uri != null) {
                    Uri uri = backup.uri;
                    if ("file".equalsIgnoreCase(uri.getScheme())) {

                        String path = uri.getPath();
                        if (path != null) {
                            File file = new File(path);
                            if (file.exists()) {
                                fileDeleted = file.delete();
                            }
                        }
                    } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                        try {
                            int deleted = context.getContentResolver().delete(uri, null, null);
                            fileDeleted = deleted > 0;
                        } catch (Exception e) {
                            AppLogger.e(getClass(), "deleteContentFile", e);
                            fileDeleted = false;
                        }
                    }
                }

                if (fileDeleted) {
                    backupHistoryRepository.deleteByBackupId(backup.backupId);
                    currentPage = 0;
                    hasMore = true;
                    loadNextPage();
                }
            } catch (Exception e) {
                AppLogger.e(getClass(), "deleteBackup", e);
            }
        });
    }

    @SuppressLint("EmptySuperCall")
    @Override
    protected void onCleared() {
        executor.shutdownNow();
        super.onCleared();
    }
}