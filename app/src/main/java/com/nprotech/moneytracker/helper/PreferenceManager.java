package com.nprotech.moneytracker.helper;

import devliving.online.securedpreferencestore.SecuredPreferenceStore;

@SuppressWarnings("unused")
public enum PreferenceManager {

    INSTANCE;

    private static final String KEY_ACCOUNT_ID = "accountId";
    private static final String KEY_WALLET_ID = "walletId";
    private static final String KEY_DARK_MODE = "darkMode";
    private static final String KEY_PERMISSION_CAMERA_ASKED = "cameraPermissionAsked";
    private static final String KEY_WEEK_START_ON = "weekStartOn";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_PASSWORD_SET = "passwordSet";
    private static final String KEY_SMART_REMINDER = "smartReminder";
    private static final String KEY_STARTUP_SCREEN = "startUpScreen";

    private final SecuredPreferenceStore prefStore = SecuredPreferenceStore.getSharedInstance();

    // ===== ACCOUNT ID =====
    public void setAccountId(long accountId) {
        prefStore.edit().putLong(KEY_ACCOUNT_ID, accountId).apply();
    }

    public long getAccountId() {
        return prefStore.getLong(KEY_ACCOUNT_ID, 0);
    }

    // ===== WALLET ID =====
    public void setWalletId(long walletId) {
        prefStore.edit().putLong(KEY_WALLET_ID, walletId).apply();
    }

    public long getWalletId() {
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

    // ===== STARTUP SCREEN =====
    public void setStartUpScreen(int startUpScreen) {
        prefStore.edit().putInt(KEY_STARTUP_SCREEN, startUpScreen).apply();
    }

    public int getStartUpScreen() {
        return prefStore.getInt(KEY_STARTUP_SCREEN, 0);
    }
}