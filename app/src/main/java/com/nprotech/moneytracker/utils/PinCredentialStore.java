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

public class PinCredentialStore {

    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 256;
    private static final int ITERATIONS = 150_000;

    private PinCredentialStore() {
    }

    private static String getAlgorithm() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? "PBKDF2WithHmacSHA256" : "PBKDF2WithHmacSHA1";
    }

    public static void savePin(String pin) throws GeneralSecurityException {

        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);

        String algorithm = getAlgorithm();
        byte[] hash = deriveHash(pin, salt, algorithm);

        try {
            PreferenceManager.INSTANCE.savePinCredentials(
                    Base64.encodeToString(hash, Base64.NO_WRAP),
                    Base64.encodeToString(salt, Base64.NO_WRAP), algorithm);
        } finally {
            Arrays.fill(hash, (byte) 0);
            Arrays.fill(salt, (byte) 0);
        }
    }

    public static boolean isPinSet() {
        PreferenceManager prefs = PreferenceManager.INSTANCE;
        return prefs.isPinSet()
                && !prefs.getPinHash().isEmpty()
                && !prefs.getPinSalt().isEmpty()
                && !prefs.getPinAlgorithm().isEmpty();
    }

    public static boolean verifyPin(String pin) throws GeneralSecurityException {

        PreferenceManager prefs = PreferenceManager.INSTANCE;

        String storedHash = prefs.getPinHash();
        String storedSalt = prefs.getPinSalt();
        String algorithm = prefs.getPinAlgorithm();

        if (storedHash.isEmpty() || storedSalt.isEmpty() || algorithm.isEmpty()) {
            return false;
        }

        byte[] expected = Base64.decode(storedHash, Base64.NO_WRAP);
        byte[] salt = Base64.decode(storedSalt, Base64.NO_WRAP);
        byte[] actual = deriveHash(pin, salt, algorithm);

        try {
            return MessageDigest.isEqual(expected, actual);
        } finally {
            Arrays.fill(expected, (byte) 0);
            Arrays.fill(actual, (byte) 0);
            Arrays.fill(salt, (byte) 0);
        }
    }

    public static void removePin() {
        PreferenceManager.INSTANCE.clearPinCredentials();
    }

    private static byte[] deriveHash(String pin, byte[] salt, String algorithm) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, HASH_LENGTH);
        try {
            return SecretKeyFactory.getInstance(algorithm)
                    .generateSecret(spec)
                    .getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
}