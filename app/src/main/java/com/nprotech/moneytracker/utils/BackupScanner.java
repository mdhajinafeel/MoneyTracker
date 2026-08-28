package com.nprotech.moneytracker.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.google.gson.Gson;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.models.BackupFileModel;
import com.nprotech.moneytracker.models.BackupMetadata;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BackupScanner {
    private static final String MANIFEST_FILE = "manifest.json";
    private final Context context;

    public BackupScanner(Context context) {
        this.context = context.getApplicationContext();
    }

    // =========================================================
    // LISTENER
    // =========================================================

    public interface BackupScanListener {
        void onBackupFound(BackupFileModel backup);
        void onScanCompleted();
        void onScanError(Exception e);
    }

    // =========================================================
    // SCAN
    // =========================================================

    public void scan(BackupScanListener listener) {

        Set<String> scannedRoots = new HashSet<>();
        Set<String> backupIds = new HashSet<>();

        try {

            // =================================================
            // INTERNAL STORAGE
            // =================================================
            File internalStorage = Environment.getExternalStorageDirectory();
            scanRoot(internalStorage, "Internal Storage", backupIds, scannedRoots, listener);

            // =================================================
            // SD CARD / REMOVABLE STORAGE
            // =================================================
            File[] externalFilesDirs = context.getExternalFilesDirs(null);

            if (externalFilesDirs != null) {
                for (File externalFilesDir : externalFilesDirs) {
                    if (externalFilesDir == null) {
                        continue;
                    }

                    File storageRoot = getStorageRoot(externalFilesDir);
                    if (storageRoot == null) {
                        continue;
                    }

                    try {

                        String storagePath = storageRoot.getCanonicalPath();
                        String internalPath = internalStorage.getCanonicalPath();

                        if (storagePath.equals(internalPath)) {
                            continue;
                        }

                    } catch (IOException e) {
                        continue;
                    }

                    scanRoot(storageRoot, "SD Card", backupIds, scannedRoots, listener);
                }
            }

            if (listener != null) {
                listener.onScanCompleted();
            }

        } catch (Exception e) {

            AppLogger.e(getClass(), "scan", e);

            if (listener != null) {
                listener.onScanError(e);
            }
        }
    }

    // =========================================================
    // SCAN ROOT
    // =========================================================

    private void scanRoot(File root, String location, Set<String> backupIds, Set<String> scannedRoots, BackupScanListener listener) {

        if (root == null || !root.exists() || !root.isDirectory()) {
            return;
        }

        try {
            String canonical = root.getCanonicalPath();
            if (!scannedRoots.add(canonical)) {
                return;
            }
        } catch (IOException e) {
            AppLogger.e(getClass(), "scanRoot", e);
            return;
        }

        scanDirectory(root, location, backupIds, listener);
    }

    // =========================================================
    // RECURSIVE SCAN
    // =========================================================

    private void scanDirectory(File directory, String location, Set<String> backupIds, BackupScanListener listener) {
        File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {

            try {

                // =================================================
                // DIRECTORY
                // =================================================
                if (file.isDirectory()) {

                    if ("Android".equalsIgnoreCase(file.getName())) {
                        continue;
                    }

                    scanDirectory(file, location, backupIds, listener);
                    continue;
                }

                // =================================================
                // FILE
                // =================================================
                if (!file.isFile()) {
                    continue;
                }

                // =================================================
                // ZIP CHECK
                // =================================================
                if (!isCandidateBackup(file)) {
                    continue;
                }

                // =================================================
                // READ METADATA
                // =================================================
                BackupFileModel backup = readBackup(file, location);
                if (backup == null) {
                    continue;
                }

                // =================================================
                // DUPLICATE CHECK
                // =================================================
                String backupId = backup.backupId;
                if (backupId != null && !backupId.trim().isEmpty()) {

                    if (!backupIds.add(backupId)) {
                        continue;
                    }
                }

                if (listener != null) {
                    listener.onBackupFound(backup);
                }
            } catch (Exception e) {
                AppLogger.e(getClass(), "scanDirectory", e);
            }
        }
    }

    // =========================================================
    // CANDIDATE BACKUP
    // =========================================================

    private boolean isCandidateBackup(File file) {
        String name = file.getName();
        if (!name.toLowerCase().endsWith(".zip")) {
            return false;
        }

        return name.startsWith(context.getString(R.string.app_name).toLowerCase() + "_backup");
    }

    // =========================================================
    // READ BACKUP
    // =========================================================

    private BackupFileModel readBackup(File file, String location) {

        try (FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ZipInputStream zis = new ZipInputStream(bis)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!MANIFEST_FILE.equals(entry.getName())) {
                    zis.closeEntry();
                    continue;
                }

                String json = readManifest(zis);

                if (json.trim().isEmpty()) {
                    return null;
                }

                BackupMetadata metadata = new Gson().fromJson(json, BackupMetadata.class);
                if (!isValidBackup(metadata, file)) {
                    return null;
                }

                return createBackupModel(file, location, metadata);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "readBackup", e);
        }
        return null;
    }

    // =========================================================
    // READ MANIFEST
    // =========================================================

    private String readManifest(ZipInputStream zis) throws IOException {
        StringBuilder builder = new StringBuilder();
        byte[] buffer = new byte[8192];
        int length;
        while ((length = zis.read(buffer)) != -1) {
            builder.append(new String(buffer, 0, length, StandardCharsets.UTF_8));
        }
        return builder.toString();
    }

    // =========================================================
    // VALIDATE METADATA
    // =========================================================

    private boolean isValidBackup(BackupMetadata metadata, File zipFile) {

        if (metadata == null) {
            return false;
        }

        // =====================================================
        // FORMAT
        // =====================================================
        if (!BackupMetadata.FORMAT.equals(metadata.format)) {
            return false;
        }

        // =====================================================
        // VERSION
        // =====================================================
        if (metadata.formatVersion != BackupMetadata.FORMAT_VERSION) {
            return false;
        }

        // =====================================================
        // APP ID
        // =====================================================
        if (!context.getPackageName().equals(metadata.appId)) {
            return false;
        }

        // =====================================================
        // BACKUP ID
        // =====================================================
        if (metadata.backupId == null || metadata.backupId.trim().isEmpty()) {
            return false;
        }

        // =====================================================
        // CREATED AT
        // =====================================================
        if (metadata.createdAt <= 0) {
            return false;
        }

        // =====================================================
        // DATABASE
        // =====================================================
        if (metadata.databaseName == null || metadata.databaseName.trim().isEmpty()) {
            return false;
        }

        // =====================================================
        // FILE
        // =====================================================
        return zipFile.exists() && zipFile.isFile() && zipFile.length() > 0;
    }

    // =========================================================
    // CREATE MODEL
    // =========================================================

    private BackupFileModel createBackupModel(File file, String location, BackupMetadata metadata) {
        BackupFileModel model = new BackupFileModel();
        model.backupId = metadata.backupId;
        model.fileName = file.getName();
        model.uri = Uri.fromFile(file);
        model.backupSize = file.length();
        model.databaseSize = metadata.databaseSize;
        model.attachmentSize = metadata.attachmentSize;
        model.createdAt = metadata.createdAt;
        model.includeAttachments = metadata.includeAttachments;
        model.location = location;
        model.appVersion = metadata.appVersion;
        model.databaseVersion = metadata.databaseVersion;
        model.databaseChecksum = metadata.databaseChecksum;
        return model;
    }

    // =========================================================
    // FIND STORAGE ROOT
    // =========================================================

    private File getStorageRoot(File externalFilesDir) {
        try {
            File current = externalFilesDir.getCanonicalFile();

            while (true) {

                File parent = current.getParentFile();

                if (parent == null) {
                    return current;
                }

                if ("storage".equalsIgnoreCase(parent.getName())) {
                    return current;
                }
                current = parent;
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "getStorageRoot", e);
        }
        return null;
    }
}