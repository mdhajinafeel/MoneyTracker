package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.BackupHistoryEntity;
import com.nprotech.moneytracker.repositories.BackupHistoryRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class BackupHistoryViewModel extends ViewModel {

    private final BackupHistoryRepository backupHistoryRepository;

    @Inject
    public BackupHistoryViewModel(BackupHistoryRepository backupHistoryRepository) {
        this.backupHistoryRepository = backupHistoryRepository;
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

    public LiveData<List<BackupHistoryEntity>> getAllBackups() {
        return backupHistoryRepository.getAllBackups();
    }

    // ---------------------------------------------------------
    // Delete
    // ---------------------------------------------------------

    public void delete(BackupHistoryEntity backupHistory) {
        backupHistoryRepository.delete(backupHistory);
    }
}