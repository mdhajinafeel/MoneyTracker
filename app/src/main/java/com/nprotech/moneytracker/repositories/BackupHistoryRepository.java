package com.nprotech.moneytracker.repositories;

import androidx.lifecycle.LiveData;

import com.nprotech.moneytracker.db.dao.BackupHistoryDao;
import com.nprotech.moneytracker.db.entites.BackupHistoryEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackupHistoryRepository {

    private final BackupHistoryDao backupHistoryDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public BackupHistoryRepository(BackupHistoryDao backupHistoryDao) {
        this.backupHistoryDao = backupHistoryDao;
    }

    public long insertBackupHistory(BackupHistoryEntity backupHistory) {
        return backupHistoryDao.insert(backupHistory);
    }

    // ---------------------------------------------------------
    // Get single backup
    // ---------------------------------------------------------

    public LiveData<BackupHistoryEntity> getBackupById(long id) {
        return backupHistoryDao.getBackupById(id);
    }

    // ---------------------------------------------------------
    // Get all
    // ---------------------------------------------------------

    public LiveData<List<BackupHistoryEntity>> getAllBackups() {
        return backupHistoryDao.getAllBackups();
    }

    // ---------------------------------------------------------
    // Delete
    // ---------------------------------------------------------

    public void delete(BackupHistoryEntity backupHistory) {
        executorService.execute(() -> backupHistoryDao.delete(backupHistory));
    }
}