package com.nprotech.moneytracker.helper;

import devliving.online.securedpreferencestore.SecuredPreferenceStore;

@SuppressWarnings("unused")
public enum PreferenceManager {

    INSTANCE;

    private static final String KEY_ACCOUNT_ID = "accountId";
    private static final String KEY_WALLET_ID = "walletId";
    private static final String KEY_DARK_MODE = "darkMode";
    private static final String KEY_PERMISSION_CAMERA_ASKED = "cameraPermissionAsked";

    private final SecuredPreferenceStore prefStore = SecuredPreferenceStore.getSharedInstance();

    // ===== Account Id =====
    public void setAccountId(long accountId) {
        prefStore.edit().putLong(KEY_ACCOUNT_ID, accountId).apply();
    }

    public long getAccountId() {
        return prefStore.getLong(KEY_ACCOUNT_ID, 0);
    }

    // ===== Wallet Id =====
    public void setWalletId(long walletId) {
        prefStore.edit().putLong(KEY_WALLET_ID, walletId).apply();
    }

    public long getWalletId() {
        return prefStore.getInt(KEY_WALLET_ID, 0);
    }

    // ===== DarkMode =====
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
}