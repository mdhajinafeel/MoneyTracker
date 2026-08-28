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

    public void insertBackupHistory(BackupHistoryEntity backupHistory) {
        backupHistoryDao.insertOrUpdate(backupHistory);
    }

    public List<BackupHistoryEntity> getAllBackups(int currentSortType, int currentAttachmentType, int page, int pageSize) {
        int offset = page * pageSize;
        return backupHistoryDao.getAllBackups(currentSortType, currentAttachmentType, pageSize, offset);
    }

    public void deleteByBackupId(String backupId) {
        executorService.execute(() -> backupHistoryDao.deleteByBackupId(backupId));
    }
}