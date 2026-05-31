package com.nprotech.moneytracker.initializer;

import android.content.Context;
import android.content.res.Resources;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nprotech.moneytracker.db.MoneyTrackerDatabase;
import com.nprotech.moneytracker.db.dao.CurrencyDao;
import com.nprotech.moneytracker.db.entites.CurrencyEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.models.CurrencyJsonModel;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CurrencyInitializer {

    public static void loadCurrencies(Context context) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {

            CurrencyDao currencyDao = MoneyTrackerDatabase.getInstance(context).currencyDao();

            // Already inserted
            if (currencyDao.getCurrencyCount() > 0) {
                return;
            }

            try (InputStream is = context.getAssets().open("currencies.json")) {

                int size = is.available();
                byte[] buffer = new byte[size];
                int bytesRead = is.read(buffer);

                List<CurrencyJsonModel> jsonList = getCurrencyJsonModels(bytesRead, size, buffer);
                List<CurrencyEntity> entities = new ArrayList<>();

                String defaultCurrencyCode = getDefaultCurrencyCode(context);

                for (CurrencyJsonModel item : jsonList) {
                    boolean isDefault = item.code.equalsIgnoreCase(defaultCurrencyCode);
                    entities.add(new CurrencyEntity(item.code, item.name, item.symbol, isDefault));
                }

                currencyDao.insertAll(entities);
            } catch (Exception e) {
                AppLogger.e(context.getClass(), "loadCurrencies", e);
            }
        });

        executorService.shutdown();
    }

    @SuppressWarnings("CharsetObjectCanBeUsed")
    private static List<CurrencyJsonModel> getCurrencyJsonModels(int bytesRead, int size, byte[] buffer) throws IOException {
        if (bytesRead != size) {
            throw new IOException(
                    "Failed to read entire file."
            );
        }
        String json = new String(buffer, "UTF-8");
        Gson gson = new Gson();
        Type type = new TypeToken<List<CurrencyJsonModel>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    private static String getDefaultCurrencyCode(Context context) {
        try {
            // 1. SIM Country (Preferred)
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if (telephonyManager != null) {
                String simCountry = telephonyManager.getSimCountryIso();

                if (simCountry != null && !simCountry.isEmpty()) {
                    Locale locale = new Locale("", simCountry.toUpperCase());
                    Currency currency = Currency.getInstance(locale);
                    return currency.getCurrencyCode();
                }
            }
        } catch (Exception e) {
            AppLogger.e(context.getClass(), "getDefaultCurrencyCode", e);
        }

        // 2. Locale Fallback
        try {
            Locale locale;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                locale = Resources.getSystem().getConfiguration().getLocales().get(0);
            } else {
                locale = Resources.getSystem().getConfiguration().locale;
            }

            Currency currency = Currency.getInstance(locale);
            return currency.getCurrencyCode();
        } catch (Exception e) {
            return "USD";
        }
    }
}