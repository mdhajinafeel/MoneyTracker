package com.nprotech.moneytracker.models;

public class BackupMetadata {

    public static final String FORMAT = "APPLICATION_FILE_BACKUP";
    public static final int FORMAT_VERSION = 1;

    public String format, appId, appVersion, databaseName, backupId, databaseChecksum;
    public int formatVersion, databaseVersion;
    public boolean includeAttachments;
    public long databaseSize, attachmentSize, createdAt;
}