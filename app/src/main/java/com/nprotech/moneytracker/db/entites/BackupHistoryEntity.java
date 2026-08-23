package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "backup_history")
public class BackupHistoryEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String backupUri;
    public String fileName;
    public long backupSize;
    public long databaseSize;
    public long attachmentSize;
    public boolean includeAttachments;
    public long createdAt;
    public String location;
}