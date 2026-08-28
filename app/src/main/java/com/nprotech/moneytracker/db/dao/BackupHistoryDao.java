package com.nprotech.moneytracker.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nprotech.moneytracker.db.entites.BackupHistoryEntity;

import java.util.List;

@Dao
public interface BackupHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(BackupHistoryEntity entity);

    // =========================================================
    // LIVE DATA
    // =========================================================

    @Query("""
        SELECT * FROM backup_history
        WHERE  (:attachmentType = 1  OR (:attachmentType = 2 AND includeAttachments = 1) OR (:attachmentType = 3 AND includeAttachments = 0))
        ORDER BY
            CASE WHEN :sortType = 1 THEN createdAt END DESC,
            CASE WHEN :sortType = 2 THEN createdAt END ASC,
            CASE WHEN :sortType = 3 THEN backupSize END DESC,
            CASE WHEN :sortType = 4 THEN backupSize END ASC
        LIMIT :limit OFFSET :offset
        """)
    List<BackupHistoryEntity> getAllBackups(int sortType, int attachmentType, int limit, int offset);

    @Query("DELETE FROM backup_history " + "WHERE backupId = :backupId")
    void deleteByBackupId(String backupId);
}