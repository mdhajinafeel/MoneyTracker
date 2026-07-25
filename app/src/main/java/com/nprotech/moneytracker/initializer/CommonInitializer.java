package com.nprotech.moneytracker.initializer;

import android.content.Context;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.IConstants;
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
        list.add(new CommonDataEntity(IConstants.DAY, 1, "", R.string.sunday, true, true));
        list.add(new CommonDataEntity(IConstants.DAY, 2, "", R.string.monday, true, false));
        list.add(new CommonDataEntity(IConstants.DAY, 3, "", R.string.tuesday, true, false));
        list.add(new CommonDataEntity(IConstants.DAY, 4, "", R.string.wednesday, true, false));
        list.add(new CommonDataEntity(IConstants.DAY, 5, "", R.string.thursday, true, false));
        list.add(new CommonDataEntity(IConstants.DAY, 6, "", R.string.friday, true, false));
        list.add(new CommonDataEntity(IConstants.DAY, 7, "", R.string.saturday, true, false));

        // Months
        list.add(new CommonDataEntity(IConstants.MONTH, 1, "", R.string.january, true, true));
        list.add(new CommonDataEntity(IConstants.MONTH, 2, "", R.string.february, true, false));
        list.add(new CommonDataEntity(IConstants.MONTH, 3, "", R.string.march, true, false));
        list.add(new CommonDataEntity(IConstants.MONTH, 4, "", R.string.april, true, false));
        list.add(new CommonDataEntity(IConstants.MONTH, 5, "", R.string.may, true, false));
        list.add(new CommonDataEntity(IConstants.MONTH, 6, "", R.string.june, true, false));
        list.add(new CommonDataEntity(IConstants.MONTH, 7, "", R.string.july, true, false));
        list.add(new CommonDataEntity(IConstants.MONTH, 8, "", R.string.august, true, false));
        list.add(new CommonDataEntity(IConstants.MONTH, 9, "", R.string.september, true, false));
        list.add(new CommonDataEntity(IConstants.MONTH, 10, "", R.string.october, true, false));
        list.add(new CommonDataEntity(IConstants.MONTH, 11, "", R.string.november, true, false));
        list.add(new CommonDataEntity(IConstants.MONTH, 12, "", R.string.december, true, false));

        // Languages
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 1, "system", R.string.system_default, true, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 2, "en", R.string.english, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 3, "ar", R.string.arabic, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 4, "bn", R.string.bengali, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 5, "cs", R.string.czech, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 6, "de", R.string.german, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 7, "el", R.string.greek, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 8, "es", R.string.spanish, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 9, "fa", R.string.persian, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 10, "fr", R.string.french, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 11, "hi", R.string.hindi, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 12, "id", R.string.indonesian, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 13, "it", R.string.italian, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 14, "ja", R.string.japanese, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 15, "ko", R.string.korean, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 16, "ms", R.string.malay, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 17, "nl", R.string.dutch, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 18, "pl", R.string.polish, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 19, "pt", R.string.portuguese, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 20, "ro", R.string.romanian, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 21, "ru", R.string.russian, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 22, "ta", R.string.tamil, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 23, "te", R.string.telugu, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 24, "th", R.string.thai, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 25, "tr", R.string.turkish, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 26, "uk", R.string.ukrainian, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 27, "vi", R.string.vietnamese, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 28, "zh-CN", R.string.chinese_simplified, true, false));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 29, "zh-TW", R.string.chinese_traditional, true, false));

        // Smart Reminder
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 1, "", R.string.not_set, true, true));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 2, "", R.string.time_0, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 3, "", R.string.time_1, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 4, "", R.string.time_2, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 5, "", R.string.time_3, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 6, "", R.string.time_4, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 7, "", R.string.time_5, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 8, "", R.string.time_6, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 9, "", R.string.time_7, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 10, "", R.string.time_8, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 11, "", R.string.time_9, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 12, "", R.string.time_10, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 13, "", R.string.time_11, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 14, "", R.string.time_12, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 15, "", R.string.time_13, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 16, "", R.string.time_14, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 17, "", R.string.time_15, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 18, "", R.string.time_16, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 19, "", R.string.time_17, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 20, "", R.string.time_18, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 21, "", R.string.time_19, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 22, "", R.string.time_20, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 23, "", R.string.time_21, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 24, "", R.string.time_22, true, false));
        list.add(new CommonDataEntity(IConstants.SMART_REMINDER, 25, "", R.string.time_23, true, false));

        // Startup Screen
        list.add(new CommonDataEntity(IConstants.STARTUP_SCREEN, 1, "", R.string.transaction, true, true));
        list.add(new CommonDataEntity(IConstants.STARTUP_SCREEN, 2, "", R.string.calendar, true, true));
        list.add(new CommonDataEntity(IConstants.STARTUP_SCREEN, 3, "", R.string.statistic, true, true));
        list.add(new CommonDataEntity(IConstants.STARTUP_SCREEN, 4, "", R.string.more, true, true));

        return list;
    }
}