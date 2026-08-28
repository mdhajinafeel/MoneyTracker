package com.nprotech.moneytracker.db.entites;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "backup_history",
        indices = {@Index(value = "backupId", unique = true)}
)
public class BackupHistoryEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String backupId;
    public String fileName;
    public String backupUri;
    public long backupSize;
    public long databaseSize;
    public long attachmentSize;
    public long createdAt;
    public boolean includeAttachments;
    public String location;
    public String appVersion;
    public int databaseVersion;
    public String databaseChecksum;
}