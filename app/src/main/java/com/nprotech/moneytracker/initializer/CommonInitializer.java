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
        list.add(new CommonDataEntity(IConstants.DAY, 1, "", R.string.monday, true));
        list.add(new CommonDataEntity(IConstants.DAY, 2, "", R.string.tuesday, true));
        list.add(new CommonDataEntity(IConstants.DAY, 3, "", R.string.wednesday, true));
        list.add(new CommonDataEntity(IConstants.DAY, 4, "", R.string.thursday, true));
        list.add(new CommonDataEntity(IConstants.DAY, 5, "", R.string.friday, true));
        list.add(new CommonDataEntity(IConstants.DAY, 6, "", R.string.saturday, true));
        list.add(new CommonDataEntity(IConstants.DAY, 7, "", R.string.sunday, true));

        // Months
        list.add(new CommonDataEntity(IConstants.MONTH, 1, "", R.string.january, true));
        list.add(new CommonDataEntity(IConstants.MONTH, 2, "", R.string.february, true));
        list.add(new CommonDataEntity(IConstants.MONTH, 3, "", R.string.march, true));
        list.add(new CommonDataEntity(IConstants.MONTH, 4, "", R.string.april, true));
        list.add(new CommonDataEntity(IConstants.MONTH, 5, "", R.string.may, true));
        list.add(new CommonDataEntity(IConstants.MONTH, 6, "", R.string.june, true));
        list.add(new CommonDataEntity(IConstants.MONTH, 7, "", R.string.july, true));
        list.add(new CommonDataEntity(IConstants.MONTH, 8, "", R.string.august, true));
        list.add(new CommonDataEntity(IConstants.MONTH, 9, "", R.string.september, true));
        list.add(new CommonDataEntity(IConstants.MONTH, 10, "", R.string.october, true));
        list.add(new CommonDataEntity(IConstants.MONTH, 11, "", R.string.november, true));
        list.add(new CommonDataEntity(IConstants.MONTH, 12, "", R.string.december, true));

        // Languages
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 1, "system", R.string.system_default, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 2, "en", R.string.english, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 3, "ar", R.string.arabic, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 4, "bn", R.string.bengali, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 5, "cs", R.string.czech, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 6, "de", R.string.german, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 7, "el", R.string.greek, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 8, "es", R.string.spanish, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 9, "fa", R.string.persian, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 10, "fr", R.string.french, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 11, "hi", R.string.hindi, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 12, "id", R.string.indonesian, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 13, "it", R.string.italian, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 14, "ja", R.string.japanese, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 15, "ko", R.string.korean, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 16, "ms", R.string.malay, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 17, "nl", R.string.dutch, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 18, "pl", R.string.polish, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 19, "pt", R.string.portuguese, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 20, "ro", R.string.romanian, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 21, "ru", R.string.russian, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 22, "ta", R.string.tamil, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 23, "te", R.string.telugu, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 24, "th", R.string.thai, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 25, "tr", R.string.turkish, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 26, "uk", R.string.ukrainian, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 27, "vi", R.string.vietnamese, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 28, "zh-CN", R.string.chinese_simplified, true));
        list.add(new CommonDataEntity(IConstants.LANGUAGE, 29, "zh-TW", R.string.chinese_traditional, true));

        return list;
    }
}