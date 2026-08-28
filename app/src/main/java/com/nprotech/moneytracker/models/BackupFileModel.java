package com.nprotech.moneytracker.models;

import android.net.Uri;

public class BackupFileModel {

    public String backupId, fileName, location, appVersion, databaseChecksum;
    public Uri uri;
    public long backupSize, databaseSize, attachmentSize, createdAt;
    public boolean includeAttachments;
    public int databaseVersion;
}