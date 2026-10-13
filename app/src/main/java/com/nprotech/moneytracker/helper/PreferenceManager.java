package com.nprotech.moneytracker.helper;

import com.nprotech.moneytracker.constants.Constants;

import devliving.online.securedpreferencestore.SecuredPreferenceStore;

@SuppressWarnings("unused")
public enum PreferenceManager {

    INSTANCE;

    private static final String KEY_ONBOARDING_COMPLETED = "onBoardingCompleted";
    private static final String KEY_ACCOUNT_ID = "accountId";
    private static final String KEY_WALLET_ID = "walletId";
    private static final String KEY_DARK_MODE = "darkMode";
    private static final String KEY_PERMISSION_CAMERA_ASKED = "cameraPermissionAsked";
    private static final String KEY_WEEK_START_ON = "weekStartOn";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_PASSWORD_SET = "passwordSet";
    private static final String KEY_SMART_REMINDER = "smartReminder";
    private static final String KEY_STARTUP_SCREEN = "startUpScreen";
    private static final String KEY_STATISTICS_FILTER = "statisticFilter";
    private static final String KEY_BACKUP_LOCATION = "backupLocation";
    private static final String KEY_AUTO_BACKUP = "autoBackup";
    private static final String KEY_BACKUP_FREQUENCY = "backupFrequency";
    private static final String KEY_PASSWORD_HASH = "passwordHash";
    private static final String KEY_PASSWORD_SALT = "passwordSalt";
    private static final String KEY_PASSWORD_ALGORITHM = "passwordAlgorithm";
    private static final String KEY_PIN_HASH = "pinHash";
    private static final String KEY_PIN_SALT = "pinSalt";
    private static final String KEY_PIN_ALGORITHM = "pinAlgorithm";
    private static final String KEY_PIN_SET = "pinSet";
    private static final String KEY_FINGERPRINT_ENABLED = "fingerprintEnabled";
    private static final String KEY_AUTO_LOCK_SECONDS = "autoLockSeconds";
    private static final String KEY_PRIMARY_UNLOCK_METHOD = "primary_unlock_method";
    private static final String KEY_APP_LOCK_ENABLED = "appLockEnabled";

    private final SecuredPreferenceStore prefStore = SecuredPreferenceStore.getSharedInstance();

    // ===== ONBOARDING =====
    public void setOnBoardingCompleted(boolean onBoardingCompleted) {
        prefStore.edit().putBoolean(KEY_ONBOARDING_COMPLETED, onBoardingCompleted).apply();
    }

    public boolean getOnBoardingCompleted() {
        return prefStore.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }

    // ===== ACCOUNT ID =====
    public void setAccountId(int accountId) {
        prefStore.edit().putInt(KEY_ACCOUNT_ID, accountId).apply();
    }

    public int getAccountId() {
        return prefStore.getInt(KEY_ACCOUNT_ID, 0);
    }

    // ===== WALLET ID =====
    public void setWalletId(int walletId) {
        prefStore.edit().putInt(KEY_WALLET_ID, walletId).apply();
    }

    public int getWalletId() {
        return prefStore.getInt(KEY_WALLET_ID, 0);
    }

    // ===== DARK MODE =====
    public void setDarkMode(boolean darkMode) {
        prefStore.edit().putBoolean(KEY_DARK_MODE, darkMode).apply();
    }

    public boolean getDarkMode() {
        return prefStore.getBoolean(KEY_DARK_MODE, false);
    }

    // ===== CAMERA PERMISSION =====
    public void setPermissionCameraAsked(boolean permissionPrefs) {
        prefStore.edit().putBoolean(KEY_PERMISSION_CAMERA_ASKED, permissionPrefs).apply();
    }

    public boolean getPermissionCameraAsked() {
        return prefStore.getBoolean(KEY_PERMISSION_CAMERA_ASKED, false);
    }

    // ===== WEEK START ON =====
    public void setWeekStartOn(int weekStartOn) {
        prefStore.edit().putInt(KEY_WEEK_START_ON, weekStartOn).apply();
    }

    public int getWeekStartOn() {
        return prefStore.getInt(KEY_WEEK_START_ON, 0);
    }

    // ===== LANGUAGE =====
    public void setLanguage(int language) {
        prefStore.edit().putInt(KEY_LANGUAGE, language).apply();
    }

    public int getLanguage() {
        return prefStore.getInt(KEY_LANGUAGE, 0);
    }

    // ===== PASSWORD SET =====
    public void setPasswordSet(int passwordSet) {
        prefStore.edit().putInt(KEY_PASSWORD_SET, passwordSet).apply();
    }

    public int getPasswordSet() {
        return prefStore.getInt(KEY_PASSWORD_SET, 0);
    }

    // ===== SMART REMINDER =====
    public void setSmartReminder(int smartReminder) {
        prefStore.edit().putInt(KEY_SMART_REMINDER, smartReminder).apply();
    }

    public int getSmartReminder() {
        return prefStore.getInt(KEY_SMART_REMINDER, 0);
    }

    public boolean isSmartReminderEnabled() {
        int value = getSmartReminder();
        return value >= 2 && value <= 25;
    }

    public void clearSmartReminder() {
        prefStore.edit().putInt(KEY_SMART_REMINDER, -1).apply();
    }

    // ===== STARTUP SCREEN =====
    public void setStartUpScreen(int startUpScreen) {
        prefStore.edit().putInt(KEY_STARTUP_SCREEN, startUpScreen).apply();
    }

    public int getStartUpScreen() {
        return prefStore.getInt(KEY_STARTUP_SCREEN, 0);
    }

    // ===== STATISTICS FILTER =====
    public void setStatisticsFilter(int statisticsFilter) {
        prefStore.edit().putInt(KEY_STATISTICS_FILTER, statisticsFilter).apply();
    }

    public int getStatisticsFilter() {
        return prefStore.getInt(KEY_STATISTICS_FILTER, 0);
    }

    // ===== BACKUP LOCATION =====
    public void setBackupLocation(String backupLocation) {
        prefStore.edit().putString(KEY_BACKUP_LOCATION, backupLocation).apply();
    }

    public String getBackupLocation() {
        return prefStore.getString(KEY_BACKUP_LOCATION, "");
    }

    // ===== AUTO BACKUP =====
    public void setAutoBackupEnabled(boolean enabled) {
        prefStore.edit().putBoolean(KEY_AUTO_BACKUP, enabled).apply();
    }

    public boolean isAutoBackupEnabled() {
        return prefStore.getBoolean(KEY_AUTO_BACKUP, false);
    }

    // ===== BACKUP FREQUENCY =====
    public void setBackupFrequency(int frequency) {
        prefStore.edit().putInt(KEY_BACKUP_FREQUENCY, frequency).apply();
    }

    public int getBackupFrequency() {
        return prefStore.getInt(KEY_BACKUP_FREQUENCY, Constants.BACKUP_FREQUENCY_DAILY);
    }

    // ===== DEBT & LOAN REMINDER =====
    public boolean getDebtLoanReminder(String key) {
        return prefStore.getBoolean(key, false);
    }

    public void setDebtLoanReminder(String key, boolean value) {
        prefStore.edit().putBoolean(key, value).apply();
    }

    // ===== PASSWORD CREDENTIALS =====
    public void setPasswordHash(String hash) {
        prefStore.edit().putString(KEY_PASSWORD_HASH, hash).apply();
    }

    public String getPasswordHash() {
        return prefStore.getString(KEY_PASSWORD_HASH, "");
    }

    public void setPasswordSalt(String salt) {
        prefStore.edit().putString(KEY_PASSWORD_SALT, salt).apply();
    }

    public String getPasswordSalt() {
        return prefStore.getString(KEY_PASSWORD_SALT, "");
    }

    public void setPasswordAlgorithm(String algorithm) {
        prefStore.edit().putString(KEY_PASSWORD_ALGORITHM, algorithm).apply();
    }

    public String getPasswordAlgorithm() {
        return prefStore.getString(KEY_PASSWORD_ALGORITHM, "");
    }

    public void clearPasswordCredentials() {
        prefStore.edit().remove(KEY_PASSWORD_HASH)
                .remove(KEY_PASSWORD_SALT)
                .remove(KEY_PASSWORD_ALGORITHM).apply();
        setPasswordSet(0);
    }

    // ===== PIN CREDENTIALS =====
    public void savePinCredentials(String hash, String salt, String algorithm) {
        prefStore.edit()
                .putString(KEY_PIN_HASH, hash)
                .putString(KEY_PIN_SALT, salt)
                .putString(KEY_PIN_ALGORITHM, algorithm)
                .putBoolean(KEY_PIN_SET, true)
                .apply();
    }

    public String getPinHash() {
        return prefStore.getString(KEY_PIN_HASH, "");
    }

    public String getPinSalt() {
        return prefStore.getString(KEY_PIN_SALT, "");
    }

    public String getPinAlgorithm() {
        return prefStore.getString(KEY_PIN_ALGORITHM, "");
    }

    public boolean isPinSet() {
        return prefStore.getBoolean(KEY_PIN_SET, false);
    }

    public void clearPinCredentials() {
        prefStore.edit()
                .remove(KEY_PIN_HASH)
                .remove(KEY_PIN_SALT)
                .remove(KEY_PIN_ALGORITHM)
                .putBoolean(KEY_PIN_SET, false)
                .apply();
    }

    // ===== FINGERPRINT =====
    public void setFingerprintEnabled(boolean enabled) {
        prefStore.edit().putBoolean(KEY_FINGERPRINT_ENABLED, enabled).apply();
    }

    public boolean isFingerprintEnabled() {
        return prefStore.getBoolean(KEY_FINGERPRINT_ENABLED, false);
    }

    // ===== AUTO LOCK =====
    public void setAutoLockSeconds(int seconds) {
        prefStore.edit().putInt(KEY_AUTO_LOCK_SECONDS, seconds).apply();
    }

    public int getAutoLockSeconds() {
        return prefStore.getInt(KEY_AUTO_LOCK_SECONDS, Constants.AUTO_LOCK_NEVER);
    }

    // ===== PRIMARY UNLOCK =====
    public void setPrimaryUnlockMethod(String method) {
        if (!Constants.PRIMARY_METHOD_PASSWORD.equals(method) && !Constants.PRIMARY_METHOD_PIN.equals(method)
                && !Constants.PRIMARY_METHOD_FINGERPRINT.equals(method)) {
            return;
        }
        prefStore.edit().putString(KEY_PRIMARY_UNLOCK_METHOD, method).apply();
    }

    public String getPrimaryUnlockMethod() {
        return prefStore.getString(KEY_PRIMARY_UNLOCK_METHOD, Constants.PRIMARY_METHOD_PASSWORD);
    }

    // ===== APP LOCK =====

    public void setAppLockEnabled(boolean enabled) {
        prefStore.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply();
    }

    public boolean isAppLockEnabled() {
        return prefStore.getBoolean(KEY_APP_LOCK_ENABLED, false);
    }
}