package com.nprotech.moneytracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.nprotech.moneytracker.db.entites.BackupHistoryEntity;

import java.util.List;

@Dao
public interface BackupHistoryDao {

    @Insert
    long insert(BackupHistoryEntity backupHistory);

    @Query("SELECT * FROM backup_history ORDER BY CASE WHEN :isNewest = 1 THEN createdAt END DESC, " +
            "CASE WHEN :isNewest = 0 THEN createdAt END ASC LIMIT :limit OFFSET :offset")
    List<BackupHistoryEntity> getAllBackups(boolean isNewest, int limit, int offset);

    @Query("SELECT * FROM backup_history WHERE id = :id LIMIT 1")
    LiveData<BackupHistoryEntity> getBackupById(long id);

    @Query("SELECT COUNT(*) FROM backup_history")
    LiveData<Integer> getActiveBackup();

    @Delete
    void delete(BackupHistoryEntity backupHistory);
}