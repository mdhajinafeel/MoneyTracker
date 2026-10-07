package com.nprotech.moneytracker.initializer;

import android.content.Context;

import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.db.MoneyTrackerDatabase;
import com.nprotech.moneytracker.db.dao.CommonDataDao;
import com.nprotech.moneytracker.db.entites.CommonDataEntity;
import com.nprotech.moneytracker.helper.AppLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommonInitializer {

    public static void loadCommonData(Context context) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {

            CommonDataDao commonDataDao = MoneyTrackerDatabase.getInstance(context).commonDataDao();

            // Already inserted
            if (commonDataDao.getCommonDataCount() > 0) {
                return;
            }

            try {
                commonDataDao.insertAll(getData());
            } catch (Exception e) {
                AppLogger.e(context.getClass(), "loadCommonData", e);
            }
        });

        executorService.shutdown();
    }


    public static List<CommonDataEntity> getData() {

        List<CommonDataEntity> list = new ArrayList<>();

        // Days
        list.add(new CommonDataEntity(Constants.DAY, 1, "", "sunday", true, true));
        list.add(new CommonDataEntity(Constants.DAY, 2, "", "monday", true, false));
        list.add(new CommonDataEntity(Constants.DAY, 3, "", "tuesday", true, false));
        list.add(new CommonDataEntity(Constants.DAY, 4, "", "wednesday", true, false));
        list.add(new CommonDataEntity(Constants.DAY, 5, "", "thursday", true, false));
        list.add(new CommonDataEntity(Constants.DAY, 6, "", "friday", true, false));
        list.add(new CommonDataEntity(Constants.DAY, 7, "", "saturday", true, false));

        // Months
        list.add(new CommonDataEntity(Constants.MONTH, 1, "", "january", true, true));
        list.add(new CommonDataEntity(Constants.MONTH, 2, "", "february", true, false));
        list.add(new CommonDataEntity(Constants.MONTH, 3, "", "march", true, false));
        list.add(new CommonDataEntity(Constants.MONTH, 4, "", "april", true, false));
        list.add(new CommonDataEntity(Constants.MONTH, 5, "", "may", true, false));
        list.add(new CommonDataEntity(Constants.MONTH, 6, "", "june", true, false));
        list.add(new CommonDataEntity(Constants.MONTH, 7, "", "july", true, false));
        list.add(new CommonDataEntity(Constants.MONTH, 8, "", "august", true, false));
        list.add(new CommonDataEntity(Constants.MONTH, 9, "", "september", true, false));
        list.add(new CommonDataEntity(Constants.MONTH, 10, "", "october", true, false));
        list.add(new CommonDataEntity(Constants.MONTH, 11, "", "november", true, false));
        list.add(new CommonDataEntity(Constants.MONTH, 12, "", "december", true, false));

        // Languages
        list.add(new CommonDataEntity(Constants.LANGUAGE, 1, "system", "system_default", true, true));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 2, "en", "english", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 3, "ar", "arabic", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 4, "bn", "bengali", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 5, "cs", "czech", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 6, "de", "german", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 7, "el", "greek", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 8, "es", "spanish", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 9, "fa", "persian", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 10, "fr", "french", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 11, "hi", "hindi", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 12, "id", "indonesian", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 13, "it", "italian", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 14, "ja", "japanese", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 15, "ko", "korean", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 16, "ms", "malay", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 17, "nl", "dutch", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 18, "pl", "polish", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 19, "pt", "portuguese", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 20, "ro", "romanian", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 21, "ru", "russian", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 22, "ta", "tamil", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 23, "te", "telugu", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 24, "th", "thai", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 25, "tr", "turkish", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 26, "uk", "ukrainian", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 27, "vi", "vietnamese", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 28, "zh-CN", "chinese_simplified", true, false));
        list.add(new CommonDataEntity(Constants.LANGUAGE, 29, "zh-TW", "chinese_traditional", true, false));

        // Smart Reminder
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 1, "", "not_set", true, true));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 2, "", "time_0", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 3, "", "time_1", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 4, "", "time_2", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 5, "", "time_3", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 6, "", "time_4", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 7, "", "time_5", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 8, "", "time_6", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 9, "", "time_7", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 10, "", "time_8", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 11, "", "time_9", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 12, "", "time_10", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 13, "", "time_11", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 14, "", "time_12", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 15, "", "time_13", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 16, "", "time_14", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 17, "", "time_15", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 18, "", "time_16", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 19, "", "time_17", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 20, "", "time_18", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 21, "", "time_19", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 22, "", "time_20", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 23, "", "time_21", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 24, "", "time_22", true, false));
        list.add(new CommonDataEntity(Constants.SMART_REMINDER, 25, "", "time_23", true, false));

        // Startup Screen
        list.add(new CommonDataEntity(Constants.STARTUP_SCREEN, 1, "", "transaction", true, true));
        list.add(new CommonDataEntity(Constants.STARTUP_SCREEN, 2, "", "calendar", true, true));
        list.add(new CommonDataEntity(Constants.STARTUP_SCREEN, 3, "", "statistic", true, true));
        list.add(new CommonDataEntity(Constants.STARTUP_SCREEN, 4, "", "more", true, true));

        return list;
    }
}