package com.nprotech.moneytracker.repositories;

import com.nprotech.moneytracker.db.dao.BackupHistoryDao;
import com.nprotech.moneytracker.db.entites.BackupHistoryEntity;

import java.util.List;

public class BackupHistoryRepository {

    private final BackupHistoryDao backupHistoryDao;

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
        backupHistoryDao.deleteByBackupId(backupId);
    }
}