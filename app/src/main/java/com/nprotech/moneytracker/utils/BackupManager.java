package com.nprotech.moneytracker.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;

import androidx.documentfile.provider.DocumentFile;

import com.google.gson.Gson;
import com.nprotech.moneytracker.BuildConfig;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.db.MoneyTrackerDatabase;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.ChecksumHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.BackupMetadata;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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

        report(listener, 5, context.getString(R.string.preparing_database));

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

        BackupMetadata metadata = createBackupMetadata(databaseFile, databaseSize, attachmentSize, includeAttachments);

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
                // Manifest
                // -----------------------------------------
                addManifestToZip(metadata, zos);

                // -----------------------------------------
                // Database
                // -----------------------------------------
                processedBytes = addFileToZip(databaseFile, "database/" + databaseFile.getName(), zos, processedBytes, totalSize, listener, 25,
                        60, context.getString(R.string.copying_database));

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
            return new BackupResult(Uri.fromFile(backupFile), backupFile.getName(), backupFile.length(), databaseSize, includeAttachments, attachmentSize,
                    metadata.createdAt, context.getString(R.string.internal_storage), metadata.backupId);

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
                    // Manifest
                    // -----------------------------------------
                    addManifestToZip(metadata, zos);

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
            return new BackupResult(documentFile.getUri(), documentFile.getName(), documentFile.length(), databaseSize, includeAttachments, attachmentSize,
                    metadata.createdAt, context.getString(R.string.internal_storage), metadata.backupId);
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
        return context.getString(R.string.app_name).toLowerCase() + "_backup_" + date + ".zip";
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
        return context.getString(R.string.internal_storage) + "/" + context.getString(R.string.app_name) + "/Backups";
    }

    public boolean ensureBackupDirectoryExists() {
        File backupDirectory = getBackupDirectory();
        if (backupDirectory.exists()) {
            return backupDirectory.isDirectory();
        }
        return backupDirectory.mkdirs();
    }

    private boolean deleteDirectory(File directory) {

        if (directory == null || !directory.exists()) {
            return true;
        }

        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (!deleteDirectory(file)) {
                        return false;
                    }
                } else {
                    if (!file.delete()) {
                        AppLogger.e(getClass(), "deleteDirectory", new IOException("Unable to delete file: " + file.getAbsolutePath()));
                        return false;
                    }
                }
            }
        }

        if (!directory.delete()) {
            AppLogger.e(getClass(), "deleteDirectory", new IOException("Unable to delete directory: " + directory.getAbsolutePath()));
            return false;
        }

        return true;
    }

    private void notifyProgress(RestoreProgressListener listener, int progress, String message) {
        if (listener != null) {
            listener.onProgress(progress, message);
        }
    }

    private void notifyError(RestoreProgressListener listener, String message) {
        if (listener != null) {
            listener.onError(message);
        }
    }

    public interface RestoreProgressListener {
        void onProgress(int progress, String message);

        void onCompleted();

        void onError(String message);
    }

    private void deleteDatabaseFiles(File databaseFile) {

        if (databaseFile == null) {
            return;
        }

        File walFile =
                new File(
                        databaseFile.getAbsolutePath() + "-wal"
                );

        File shmFile =
                new File(
                        databaseFile.getAbsolutePath() + "-shm"
                );

        if (walFile.exists() && !walFile.delete()) {
            AppLogger.e(
                    getClass(),
                    "deleteDatabaseFiles",
                    new IOException(
                            "Unable to delete WAL file: "
                                    + walFile.getAbsolutePath()
                    )
            );
        }

        if (shmFile.exists() && !shmFile.delete()) {
            AppLogger.e(
                    getClass(),
                    "deleteDatabaseFiles",
                    new IOException(
                            "Unable to delete SHM file: "
                                    + shmFile.getAbsolutePath()
                    )
            );
        }

        if (databaseFile.exists()
                && !databaseFile.delete()) {

            AppLogger.e(
                    getClass(),
                    "deleteDatabaseFiles",
                    new IOException(
                            "Unable to delete database: "
                                    + databaseFile.getAbsolutePath()
                    )
            );
        }
    }

    private File getPendingRestoreDatabaseFile() {

        File databaseFile = getDatabaseFile();

        return new File(
                context.getCacheDir(),
                databaseFile.getName() + ".restore"
        );
    }

    private File getPendingRestoreAttachmentDirectory() {

        return new File(
                context.getCacheDir(),
                "uploads_restore"
        );
    }

    // ----------------------------------------------
    // ---------- RESTORE BACKUP --------------------
    // ----------------------------------------------

    public void restoreBackup(Uri backupUri, RestoreProgressListener listener) {

        if (backupUri == null) {
            notifyError(listener, context.getString(R.string.invalid_backup_file));
            return;
        }

        File databaseFile = getDatabaseFile();
        File tempDatabaseFile = getPendingRestoreDatabaseFile();
        File tempAttachmentDirectory = getPendingRestoreAttachmentDirectory();

        try {

            // =========================================================
            // 1. PREPARE
            // =========================================================
            notifyProgress(listener, 5, context.getString(
                    R.string.preparing_restore
            ));

            // ---------------------------------------------------------
            // Remove previous temporary database
            // ---------------------------------------------------------
            if (tempDatabaseFile.exists() && !tempDatabaseFile.delete()) {
                throw new IOException("Unable to delete temporary restore database");
            }

            // ---------------------------------------------------------
            // Remove previous temporary attachments
            // ---------------------------------------------------------
            if (tempAttachmentDirectory.exists()) {
                if (!deleteDirectory(tempAttachmentDirectory)) {
                    throw new IOException("Unable to delete temporary attachments");
                }
            }

            // Create temporary attachment directory
            if (!tempAttachmentDirectory.mkdirs() && !tempAttachmentDirectory.exists()) {
                throw new IOException("Unable to create temporary attachments directory");
            }

            // =========================================================
            // 2. EXTRACT DATABASE + ATTACHMENTS
            // =========================================================
            notifyProgress(listener, 15, context.getString(
                    R.string.extracting_database
            ));

            extractBackupContents(backupUri, tempDatabaseFile, tempAttachmentDirectory, listener);

            // =========================================================
            // 3. VALIDATE DATABASE
            // =========================================================
            notifyProgress(listener, 60, context.getString(
                    R.string.validating_database
            ));

            if (!tempDatabaseFile.exists() || tempDatabaseFile.length() == 0) {
                throw new IOException("Restored database does not exist");
            }

            validateDatabase(tempDatabaseFile);

            // =========================================================
            // 4. PREPARE APP DATABASE
            // =========================================================
            notifyProgress(listener, 70, context.getString(R.string.preparing_app_database));

            closeRoomDatabase();

            // =========================================================
            // 5. REMOVE CURRENT DATABASE
            // =========================================================
            notifyProgress(listener, 75, context.getString(
                    R.string.restoring_database
            ));

            deleteDatabaseFiles(databaseFile);

            // =========================================================
            // 6. INSTALL DATABASE
            // =========================================================
            copyFile(tempDatabaseFile, databaseFile);

            // =========================================================
            // 7. RESTORE ATTACHMENTS
            // =========================================================
            notifyProgress(listener, 85, context.getString(
                    R.string.restoring_attachments
            ));
            restoreAttachments(tempAttachmentDirectory);

            // =========================================================
            // 8. VERIFY DATABASE
            // =========================================================

            if (!databaseFile.exists() || databaseFile.length() == 0) {

                throw new IOException(
                        "Unable to install restored database"
                );
            }

            validateDatabase(databaseFile);

            // =========================================================
            // 9. DELETE TEMPORARY FILES
            // =========================================================
            if (tempDatabaseFile.exists() && !tempDatabaseFile.delete()) {
                AppLogger.e(getClass(), "restoreBackup", new IOException("Unable to delete temporary database"));
            }

            if (tempAttachmentDirectory.exists()) {
                deleteDirectory(tempAttachmentDirectory);
            }

            // =========================================================
            // 10. COMPLETE
            // =========================================================
            notifyProgress(listener, 100, context.getString(
                    R.string.restore_completed
            ));

            if (listener != null) {
                listener.onCompleted();
            }

        } catch (Exception e) {

            AppLogger.e(getClass(), "restoreBackup", e);

            if (tempDatabaseFile.exists() && !tempDatabaseFile.delete()) {
                AppLogger.e(getClass(), "restoreBackup", new IOException("Unable to delete temporary database: " + tempDatabaseFile.getAbsolutePath()));
            }

            if (tempAttachmentDirectory.exists() && !deleteDirectory(tempAttachmentDirectory)) {
                AppLogger.e(getClass(), "restoreBackup", new IOException("Unable to delete temporary attachments: "
                        + tempAttachmentDirectory.getAbsolutePath()));
            }

            notifyError(listener, context.getString(
                    R.string.unable_restore_backup
            ));
        }
    }

    private void closeRoomDatabase() {
        try {
            MoneyTrackerDatabase.closeDatabase();
        } catch (Exception e) {
            AppLogger.e(getClass(), "closeRoomDatabase", e);
        }
    }

    private void extractBackupContents(Uri backupUri, File tempDatabaseFile, File tempAttachmentDirectory,
                                       RestoreProgressListener listener) throws IOException {

        try (InputStream inputStream = context.getContentResolver().openInputStream(backupUri)) {

            if (inputStream == null) {
                throw new IOException("Unable to open backup file");
            }

            try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream))) {

                ZipEntry entry;

                byte[] buffer = new byte[8192];
                boolean databaseFound = false;
                long attachmentProcessed = 0;

                // ---------------------------------------------------------
                // First pass is not possible with ZipInputStream.
                // We therefore report attachment progress based on
                // extracted bytes.
                // ---------------------------------------------------------

                while ((entry = zipInputStream.getNextEntry()) != null) {
                    String entryName = entry.getName();
                    if (entryName == null || entryName.trim().isEmpty()) {

                        zipInputStream.closeEntry();
                        continue;
                    }

                    // =====================================================
                    // DATABASE
                    // =====================================================
                    File databaseFile = getDatabaseFile();
                    String expectedDatabaseEntry = "database/" + databaseFile.getName();

                    if (entryName.equals(expectedDatabaseEntry)) {
                        File parent = tempDatabaseFile.getParentFile();

                        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.exists()) {
                            throw new IOException("Unable to create temporary directory");
                        }

                        try (FileOutputStream outputStream = new FileOutputStream(tempDatabaseFile)) {
                            int length;
                            while ((length = zipInputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, length);
                            }
                            outputStream.flush();
                        }

                        databaseFound = true;
                        zipInputStream.closeEntry();
                        continue;
                    }

                    // =====================================================
                    // ATTACHMENTS
                    // =====================================================
                    String normalizedPath = entryName.replace("\\", "/");

                    if (normalizedPath.startsWith("attachments/")) {

                        // Ignore the root attachments directory
                        if (entry.isDirectory()) {
                            zipInputStream.closeEntry();
                            continue;
                        }

                        String relativePath = normalizedPath.substring("attachments/".length());

                        if (relativePath.isEmpty()) {
                            zipInputStream.closeEntry();
                            continue;
                        }

                        File outputFile = new File(tempAttachmentDirectory, relativePath);

                        // -------------------------------------------------
                        // Security check against Zip Slip
                        // -------------------------------------------------
                        String canonicalRoot = tempAttachmentDirectory.getCanonicalPath() + File.separator;

                        String canonicalOutput = outputFile.getCanonicalPath();
                        if (!canonicalOutput.startsWith(canonicalRoot)) {
                            throw new IOException("Invalid attachment path");
                        }

                        File parent = outputFile.getParentFile();

                        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.exists()) {
                            throw new IOException("Unable to create attachment directory");
                        }

                        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {

                            int length;
                            while ((length = zipInputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, length);

                                attachmentProcessed += length;

                                /*
                                 * Attachment progress:
                                 *
                                 * 85% -> 98%
                                 *
                                 * We don't know total uncompressed size
                                 * from every ZIP, so report progress based
                                 * on extracted files.
                                 */
                                notifyProgress(listener, Math.min(98, 85 + ((int) (attachmentProcessed / (1024.0 * 1024.0)))),
                                        context.getString(R.string.restoring_attachments));
                            }
                            outputStream.flush();
                        }
                    }
                    zipInputStream.closeEntry();
                }

                if (!databaseFound) {
                    throw new IOException("Database not found in backup");
                }
            }
        }
    }

    private void restoreAttachments(File tempAttachmentDirectory) throws IOException {

        if (tempAttachmentDirectory == null || !tempAttachmentDirectory.exists()) {
            return;
        }

        File[] files = tempAttachmentDirectory.listFiles();

        if (files == null || files.length == 0) {
            return;
        }

        File attachmentsDirectory = getAttachmentsDirectory();

        // ---------------------------------------------------------
        // Remove existing attachments
        // ---------------------------------------------------------
        if (attachmentsDirectory.exists()) {
            if (!deleteDirectory(attachmentsDirectory)) {
                throw new IOException("Unable to remove existing attachments");
            }
        }

        // ---------------------------------------------------------
        // Create attachments directory
        // ---------------------------------------------------------
        if (!attachmentsDirectory.mkdirs() && !attachmentsDirectory.exists()) {
            throw new IOException("Unable to create attachments directory");
        }

        // ---------------------------------------------------------
        // Copy restored attachments
        // ---------------------------------------------------------
        copyDirectory(tempAttachmentDirectory, attachmentsDirectory);
    }

    private void copyFile(File source, File destination) throws IOException {

        File parent = destination.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.exists()) {
            throw new IOException("Unable to create destination directory");
        }

        try (FileInputStream inputStream = new FileInputStream(source);
             FileOutputStream outputStream = new FileOutputStream(destination);
             FileChannel inputChannel = inputStream.getChannel();
             FileChannel outputChannel = outputStream.getChannel()) {
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            outputChannel.force(true);
        }
    }

    private void copyDirectory(File sourceDirectory, File destinationDirectory) throws IOException {

        if (sourceDirectory == null || !sourceDirectory.exists()) {
            return;
        }

        if (!destinationDirectory.exists() && !destinationDirectory.mkdirs() && !destinationDirectory.exists()) {
            throw new IOException("Unable to create directory: " + destinationDirectory.getAbsolutePath());
        }

        File[] files = sourceDirectory.listFiles();

        if (files == null) {
            return;
        }

        for (File sourceFile : files) {
            File destinationFile = new File(destinationDirectory, sourceFile.getName());

            if (sourceFile.isDirectory()) {
                copyDirectory(sourceFile, destinationFile);
            } else {
                copyFile(sourceFile, destinationFile);
            }
        }
    }

    private void validateDatabase(File databaseFile) throws IOException {
        SQLiteDatabase database = null;
        try {
            database = SQLiteDatabase.openDatabase(databaseFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            database.rawQuery("SELECT name FROM sqlite_master LIMIT 1", null).close();
        } catch (Exception e) {
            AppLogger.e(getClass(), "validateDatabase", e);
            throw new IOException("Invalid database: " + databaseFile.getAbsolutePath(), e);
        } finally {
            if (database != null && database.isOpen()) {
                database.close();
            }
        }
    }

    // ---------------------------------
    // ----------- METADATA -----------
    // ---------------------------------
    private BackupMetadata createBackupMetadata(File databaseFile, long databaseSize, long attachmentSize, boolean includeAttachments) throws Exception {

        BackupMetadata metadata = new BackupMetadata();

        metadata.format = BackupMetadata.FORMAT;
        metadata.formatVersion = BackupMetadata.FORMAT_VERSION;
        metadata.appId = context.getPackageName();
        metadata.appVersion = BuildConfig.VERSION_NAME;
        metadata.databaseName = databaseFile.getName();
        metadata.databaseVersion = Constants.DATABASE_VERSION;
        metadata.backupId = UUID.randomUUID().toString();
        metadata.createdAt = System.currentTimeMillis();
        metadata.includeAttachments = includeAttachments;
        metadata.databaseSize = databaseSize;
        metadata.attachmentSize = attachmentSize;
        metadata.databaseChecksum = ChecksumHelper.sha256(databaseFile);
        return metadata;
    }

    private void addManifestToZip(BackupMetadata metadata, ZipOutputStream zos) throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(metadata);
        ZipEntry entry = new ZipEntry("manifest.json");
        zos.putNextEntry(entry);
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        zos.write(data);
        zos.closeEntry();
    }

    public record BackupResult(Uri uri, String fileName, long backupSize, long databaseSize,
                               boolean isAttachmentIncluded, long attachmentSize, long createdAt,
                               String location, String backupId) {
    }
}