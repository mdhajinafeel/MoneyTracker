package com.nprotech.moneytracker.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;

import androidx.documentfile.provider.DocumentFile;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupManager {

    private final Context context;

    public BackupManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Listener used to report backup progress.
     */
    public interface ProgressListener {
        void onProgress(int progress, String message);
    }

    /**
     * Main backup method.
     */
    public BackupResult createBackup(boolean includeAttachments, ProgressListener listener) throws Exception {

        File databaseFile = getDatabaseFile();

        if (!databaseFile.exists()) {
            throw new IOException("Database file not found.");
        }

        report(listener, 5, "Preparing database...");

        /*
         * Make sure the latest WAL changes are
         * checkpointed before copying.
         */
        checkpointDatabase(databaseFile);

        report(listener, 15, context.getString(R.string.database_prepared));

        File attachmentsDirectory = getAttachmentsDirectory();
        long databaseSize = databaseFile.length();
        long attachmentSize = 0;

        if (includeAttachments && attachmentsDirectory.exists()) {
            report(listener, 20, context.getString(R.string.calculating_attachments));
            attachmentSize = calculateDirectorySize(attachmentsDirectory);
        }

        long totalSize = databaseSize + attachmentSize;

        if (totalSize <= 0) {
            totalSize = 1;
        }

        File backupFile;
        String savedLocation = PreferenceManager.INSTANCE.getBackupLocation();
        Uri backupFolderUri = null;
        if (!savedLocation.trim().isEmpty()) {
            backupFolderUri = Uri.parse(savedLocation);
        }

        if (backupFolderUri == null) {
            File backupDirectory = getBackupDirectory();
            if (!backupDirectory.exists()) {
                if (!backupDirectory.mkdirs()) {
                    throw new IOException(context.getString(R.string.unable_backup_directory));
                }
            }

            backupFile = new File(backupDirectory, generateBackupFileName());
            report(listener, 25, context.getString(R.string.creating_backup_file));
            long processedBytes = 0;

            try (FileOutputStream fos = new FileOutputStream(backupFile); ZipOutputStream zos = new ZipOutputStream(fos)) {

                // -----------------------------------------
                // Database
                // -----------------------------------------
                processedBytes = addFileToZip(databaseFile, "database/" + databaseFile.getName(), zos, processedBytes, totalSize, listener, 25, 60, "Copying database...");

                // -----------------------------------------
                // Attachments
                // -----------------------------------------
                if (includeAttachments && attachmentsDirectory.exists()) {
                    addDirectoryToZip(attachmentsDirectory, "attachments/", zos, processedBytes, totalSize, listener);
                }
            }

            report(listener, 98, context.getString(R.string.finalizing_backup));

            if (!backupFile.exists() || backupFile.length() == 0) {
                throw new IOException(context.getString(R.string.backup_file_is_empty));
            }

            report(listener, 100, context.getString(R.string.backup_completed));
            return new BackupResult(Uri.fromFile(backupFile), backupFile.getName(), backupFile.length(), databaseSize, includeAttachments, attachmentSize);

        } else {
            DocumentFile folder = DocumentFile.fromTreeUri(context, backupFolderUri);
            if (folder == null || !folder.exists() || !folder.isDirectory() || !folder.canWrite()) {
                throw new IOException(context.getString(R.string.folder_is_not_writable));
            }

            String backupFileName = generateBackupFileName();
            report(listener, 25, context.getString(R.string.creating_backup_file));

            // -----------------------------------------
            // Create ZIP inside selected folder
            // -----------------------------------------
            DocumentFile documentFile = folder.createFile("application/zip", backupFileName);
            if (documentFile == null) {
                throw new IOException(context.getString(R.string.unable_backup));
            }

            long processedBytes = 0;

            try {

                ContentResolver resolver = context.getContentResolver();
                OutputStream outputStream = resolver.openOutputStream(documentFile.getUri(), "w");

                if (outputStream == null) {
                    throw new IOException(context.getString(R.string.unable_open_backup_file));
                }

                try (OutputStream os = outputStream; ZipOutputStream zos = new ZipOutputStream(os)) {

                    // -----------------------------------------
                    // Database
                    // -----------------------------------------
                    processedBytes = addFileToZip(databaseFile, "database/" + databaseFile.getName(), zos, processedBytes, totalSize,
                                    listener, 25, 60, context.getString(
                                            R.string.copying_database
                                    ));

                    // -----------------------------------------
                    // Attachments
                    // -----------------------------------------
                    if (includeAttachments && attachmentsDirectory.exists()) {
                        addDirectoryToZip(attachmentsDirectory, "attachments/", zos, processedBytes, totalSize, listener);
                    }
                }

            } catch (Exception e) {
                try {
                    documentFile.delete();
                } catch (Exception deleteException) {
                    AppLogger.e(getClass(), "deleteIncompleteBackup", deleteException);
                }
                throw e;
            }

            report(listener, 98, context.getString(
                            R.string.finalizing_backup
                    ));

            // -----------------------------------------
            // Verify
            // -----------------------------------------

            if (!documentFile.exists() || documentFile.length() <= 0) {
                throw new IOException(context.getString(
                                R.string.backup_file_is_empty
                        ));
            }

            report(listener, 100, context.getString(
                            R.string.backup_completed
                    ));
            return new BackupResult(documentFile.getUri(), documentFile.getName(), documentFile.length(), databaseSize, includeAttachments, attachmentSize);
        }
    }

    /**
     * Get database.
     */
    private File getDatabaseFile() {
        return context.getDatabasePath(context.getString(R.string.app_name) + "_db".toLowerCase());
    }

    /**
     * Get attachments directory.
     * Change this path if your existing attachment
     * storage uses another directory.
     */
    private File getAttachmentsDirectory() {
        return new File(context.getFilesDir(), "uploads");
    }

    /**
     * Get backup directory.
     */
    private File getBackupDirectory() {
        File internalStorage = Environment.getExternalStorageDirectory();
        return new File(internalStorage, context.getString(R.string.app_name) + "/Backups");
    }

    /**
     * Generate unique backup filename.
     */
    private String generateBackupFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
        String date = format.format(new Date());
        return context.getString(R.string.app_name).toLowerCase() + "_Backup_" + date + ".zip";
    }

    /**
     * Checkpoint SQLite WAL.
     */
    private void checkpointDatabase(File databaseFile) {
        SQLiteDatabase database = null;
        try {
            database = SQLiteDatabase.openDatabase(databaseFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
            database.execSQL("PRAGMA wal_checkpoint(FULL)");

        } catch (Exception e) {
            AppLogger.e(getClass(), "checkpointDatabase", e);

        } finally {
            if (database != null && database.isOpen()) {
                database.close();
            }
        }
    }

    /**
     * Calculate directory size recursively.
     */
    private long calculateDirectorySize(File directory) {
        if (directory == null || !directory.exists()) {
            return 0;
        }

        if (directory.isFile()) {
            return directory.length();
        }

        long size = 0;
        File[] files = directory.listFiles();

        if (files == null) {
            return 0;
        }

        for (File file : files) {
            if (file.isFile()) {
                size += file.length();
            } else if (file.isDirectory()) {
                size += calculateDirectorySize(file);
            }
        }

        return size;
    }

    /**
     * Add one file to ZIP.
     */
    private long addFileToZip(File file, String zipPath, ZipOutputStream zos, long processedBytes, long totalBytes, ProgressListener listener,
                              int progressStart, int progressEnd, String message) throws Exception {

        ZipEntry entry = new ZipEntry(zipPath);
        zos.putNextEntry(entry);
        try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) != -1) {
                zos.write(buffer, 0, length);
                processedBytes += length;
                updateProgress(processedBytes, totalBytes, listener, progressStart, progressEnd, message);
            }
        }
        zos.closeEntry();

        return processedBytes;
    }

    /**
     * Add directory recursively.
     */
    private long addDirectoryToZip(File directory, String zipPath, ZipOutputStream zos, long processedBytes, long totalBytes,
                                   ProgressListener listener) throws Exception {

        File[] files = directory.listFiles();

        if (files == null) {
            return processedBytes;
        }

        for (File file : files) {
            String childPath = zipPath + file.getName();
            if (file.isDirectory()) {
                processedBytes = addDirectoryToZip(file, childPath + "/", zos, processedBytes, totalBytes, listener);
            } else {
                processedBytes = addFileToZip(file, childPath, zos, processedBytes, totalBytes,
                        listener, 60, 95, context.getString(R.string.copying_attachments));
            }
        }

        return processedBytes;
    }

    /**
     * Calculate and report progress.
     */
    private void updateProgress(long processedBytes, long totalBytes, ProgressListener listener, int start, int end, String message) {

        if (listener == null) {
            return;
        }

        double ratio = (double) processedBytes / (double) totalBytes;
        ratio = Math.max(0, Math.min(1, ratio));
        int progress = start + (int) (ratio * (end - start));
        listener.onProgress(progress, message);
    }

    private void report(ProgressListener listener, int progress, String message) {
        if (listener != null) {
            listener.onProgress(progress, message);
        }
    }

    public long getDatabaseSize() {
        File databaseFile = getDatabaseFile();
        if (!databaseFile.exists()) {
            return 0;
        }
        return databaseFile.length();
    }

    public long getAttachmentSize() {
        File attachmentsDirectory = getAttachmentsDirectory();
        return calculateDirectorySize(attachmentsDirectory);
    }

    public String getBackupDirectoryDisplayPath() {

        File externalFilesDir = context.getExternalFilesDir(null);
        if (externalFilesDir == null) {
            return "Internal Storage/Expenixo/Backups";
        }

        File backupDirectory = getBackupDirectory();
        String basePath = externalFilesDir.getAbsolutePath();
        String backupPath = backupDirectory.getAbsolutePath();

        if (backupPath.startsWith(basePath)) {
            String relativePath = backupPath.substring(basePath.length());
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }
            return "Internal Storage/" + relativePath;
        }

        return backupPath;
    }

    public boolean ensureBackupDirectoryExists() {
        File backupDirectory = getBackupDirectory();
        if (backupDirectory.exists()) {
            return backupDirectory.isDirectory();
        }
        return backupDirectory.mkdirs();
    }

    public record BackupResult(Uri uri, String fileName, long backupSize, long databaseSize, boolean isAttachmentIncluded, long attachmentSize) {
    }
}