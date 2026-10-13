package com.nprotech.moneytracker.utils;

import android.os.Build;
import android.util.Base64;

import com.nprotech.moneytracker.helper.PreferenceManager;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordCredentialStore {

    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 256;
    private static final int ITERATIONS = 150_000;

    private PasswordCredentialStore() {
    }

    private static String getAlgorithm() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? "PBKDF2WithHmacSHA256" : "PBKDF2WithHmacSHA1";
    }

    public static void savePassword(String password) throws GeneralSecurityException {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        String algorithm = getAlgorithm();

        byte[] hash = deriveHash(password, salt, algorithm);
        PreferenceManager prefs = PreferenceManager.INSTANCE;

        // Store credentials before marking the password as configured.
        prefs.setPasswordHash(Base64.encodeToString(hash, Base64.NO_WRAP));
        prefs.setPasswordSalt(Base64.encodeToString(salt, Base64.NO_WRAP));
        prefs.setPasswordAlgorithm(algorithm);

        // 1 = password configured.
        prefs.setPasswordSet(1);
        Arrays.fill(hash, (byte) 0);
        Arrays.fill(salt, (byte) 0);
    }

    public static boolean isPasswordSet() {
        PreferenceManager prefs = PreferenceManager.INSTANCE;
        return prefs.getPasswordSet() == 1 && !prefs.getPasswordHash().isEmpty() && !prefs.getPasswordSalt().isEmpty() && !prefs.getPasswordAlgorithm().isEmpty();
    }

    public static boolean verifyPassword(String password) throws GeneralSecurityException {

        PreferenceManager prefs = PreferenceManager.INSTANCE;
        String encodedHash = prefs.getPasswordHash();
        String encodedSalt = prefs.getPasswordSalt();
        String algorithm = prefs.getPasswordAlgorithm();

        if (encodedHash.isEmpty() || encodedSalt.isEmpty() || algorithm.isEmpty()) {
            return false;
        }

        byte[] expectedHash = Base64.decode(encodedHash, Base64.NO_WRAP);
        byte[] salt = Base64.decode(encodedSalt, Base64.NO_WRAP);
        byte[] actualHash = deriveHash(password, salt, algorithm);

        try {
            return MessageDigest.isEqual(expectedHash, actualHash);
        } finally {
            Arrays.fill(expectedHash, (byte) 0);
            Arrays.fill(actualHash, (byte) 0);
            Arrays.fill(salt, (byte) 0);
        }
    }

    public static void removePassword() {
        PreferenceManager.INSTANCE.clearPasswordCredentials();
    }

    private static byte[] deriveHash(String password, byte[] salt, String algorithm) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, HASH_LENGTH);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
            return factory.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
}