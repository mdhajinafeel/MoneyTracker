package com.nprotech.moneytracker.helper;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class ChecksumHelper {

    private ChecksumHelper() {
    }

    public static String sha256(File file) throws Exception {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (FileInputStream inputStream = new FileInputStream(file)) {

            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, length);
            }
        }

        byte[] hash = digest.digest();

        StringBuilder builder = new StringBuilder();

        for (byte value : hash) {
            builder.append(String.format("%02x", value));
        }

        return builder.toString();
    }
}