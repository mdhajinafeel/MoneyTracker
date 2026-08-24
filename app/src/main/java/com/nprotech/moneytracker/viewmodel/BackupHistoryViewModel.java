package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.BackupHistoryEntity;
import com.nprotech.moneytracker.repositories.BackupHistoryRepository;

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
    private int currentPage = 0;
    private final LiveData<Integer> backupCount;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<BackupHistoryEntity>> backupHistoryList = new MutableLiveData<>(new ArrayList<>());

    @Inject
    public BackupHistoryViewModel(BackupHistoryRepository backupHistoryRepository) {
        this.backupHistoryRepository = backupHistoryRepository;
        backupCount = backupHistoryRepository.getActiveBackup();
    }

    public long insertBackupHistory(BackupHistoryEntity backupHistory) {
        return backupHistoryRepository.insertBackupHistory(backupHistory);
    }

    // ---------------------------------------------------------
    // Get single backup
    // ---------------------------------------------------------

    public LiveData<BackupHistoryEntity> getBackupById(long id) {
        return backupHistoryRepository.getBackupById(id);
    }

    // ---------------------------------------------------------
    // Get all backups
    // ---------------------------------------------------------

    public void loadNextPage(boolean isNewest) {

        if (loading || !hasMore)
            return;

        loading = true;

        executor.execute(() -> {
            List<BackupHistoryEntity> page = backupHistoryRepository.getAllBackups(isNewest, currentPage, PAGE_SIZE);

            if (page.size() < PAGE_SIZE) {
                hasMore = false;
            }

            backupHistoryList.postValue(page);
            currentPage++;
            loading = false;
        });
    }

    public void loadBackupHistory(boolean isNewest) {
        currentPage = 0;
        hasMore = true;
        backupHistoryList.setValue(new ArrayList<>());
        loadNextPage(isNewest);
    }

    public LiveData<List<BackupHistoryEntity>> getBackupHistoryList() {
        return backupHistoryList;
    }

    public LiveData<Integer> backupCount() {
        return backupCount;
    }

    // ---------------------------------------------------------
    // Delete
    // ---------------------------------------------------------

    public void delete(BackupHistoryEntity backupHistory) {
        backupHistoryRepository.delete(backupHistory);
    }
}