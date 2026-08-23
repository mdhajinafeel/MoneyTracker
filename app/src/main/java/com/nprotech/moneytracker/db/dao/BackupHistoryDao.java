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

    @Query("SELECT * FROM backup_history ORDER BY createdAt DESC")
    LiveData<List<BackupHistoryEntity>> getAllBackups();

    @Query("SELECT * FROM backup_history WHERE id = :id LIMIT 1")
    LiveData<BackupHistoryEntity> getBackupById(long id);

    @Delete
    void delete(BackupHistoryEntity backupHistory);
}