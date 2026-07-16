package com.nprotech.moneytracker.initializer;

import android.content.Context;

import com.nprotech.moneytracker.db.MoneyTrackerDatabase;
import com.nprotech.moneytracker.db.dao.CategoryDao;
import com.nprotech.moneytracker.db.entites.CategoryEntity;
import com.nprotech.moneytracker.helper.AppLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryInitializer {

    public static void loadCategories(Context context) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {

            CategoryDao categoryDao = MoneyTrackerDatabase.getInstance(context).categoryDao();

            // Already inserted
            if (categoryDao.getCategoriesCount() > 0) {
                return;
            }

            try {
                categoryDao.insertAll(getCategoryIncomeData());
                categoryDao.insertAll(getCategoryExpenseData());
            } catch (Exception e) {
                AppLogger.e(context.getClass(), "loadCategories", e);
            }
        });

        executorService.shutdown();
    }

    private static List<CategoryEntity> getCategoryIncomeData() {
        String[] strArr = {"#34BFFF", "#016165", "#00C1BF", "#FFCA00", "#FFAD00", "#EE4036", "#9C005E", "#652D90", "#9457FA"};
        int[] iArr = {158, 160, 152, 153, 162, 159, 164, 161, 146};

        List<CategoryEntity> categories = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            categories.add(new CategoryEntity("", strArr[i], iArr[i], 1, true, i, i + 15));
        }

        categories.add(new CategoryEntity("", "#3485FF", 165, 1, true, 0, 24));
        categories.add(new CategoryEntity("", "#0D8EFF", 168, 1, true, 0, 27));
        categories.add(new CategoryEntity("", "#69B9FF", 169, 1, true, 0, 28));

        return categories;
    }

    private static List<CategoryEntity> getCategoryExpenseData() {
        String[] strArr = {"#34BFFF", "#0077C5", "#00A6A4", "#00C1BF", "#7FD000", "#FFCA00", "#FFAD00", "#EE4036", "#9C005E", "#652D90", "#9457FA", "#E31C9E", "#8B4513", "#5E4138"};
        int[] iArr = {1, 2, 144, 28, 66, 111, 11, 57, 80, 78, 10, 39, 100, 146};

        List<CategoryEntity> categories = new ArrayList<>();
        int i = 0;
        while (i < 14) {
            int i2 = i + 1;
            categories.add(new CategoryEntity("", strArr[i], iArr[i], 2, true, i, i2));
            i = i2;
        }

        categories.add(new CategoryEntity("", "#FF250B", 165, 2, true, 0, 24));
        categories.add(new CategoryEntity("", "#FF2861", 166, 2, true, 0, 25));
        categories.add(new CategoryEntity("", "#FF5877", 167, 2, true, 0, 26));

        return categories;
    }
}