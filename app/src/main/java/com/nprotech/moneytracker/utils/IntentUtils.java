package com.nprotech.moneytracker.utils;

import android.content.Intent;
import android.os.Build;

import java.io.Serializable;

public final class IntentUtils {

    private IntentUtils() {
        // Prevent instantiation
    }

    public static <T extends Serializable> T getSerializableExtra(Intent intent, String key, Class<T> clazz) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return intent.getSerializableExtra(key, clazz);
        }

        @SuppressWarnings("deprecation")
        Serializable value = intent.getSerializableExtra(key);

        return clazz.cast(value);
    }
}