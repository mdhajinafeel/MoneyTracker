package com.nprotech.moneytracker.initializer;

import android.content.Context;

import com.nprotech.moneytracker.R;
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
                categoryDao.insertAll(getCategoryTransferData());
                categoryDao.insertAll(getGoalCategoryData(context));
            } catch (Exception e) {
                AppLogger.e(context.getClass(), "loadCategories", e);
            }
        });

        executorService.shutdown();
    }

    private static List<CategoryEntity> getCategoryIncomeData() {
        String[] strArr = {"#34BFFF", "#016165", "#00C1BF", "#FFCA00", "#FFAD00", "#EE4036", "#9C005E", "#652D90", "#10B981", "#9457FA"};
        int[] iArr = {158, 160, 152, 153, 162, 159, 164, 161, 174, 146};

        List<CategoryEntity> categories = new ArrayList<>();
        int categoryOrder = 1;
        for (int i = 0; i < 10; i++) {
            categories.add(new CategoryEntity("", strArr[i], iArr[i], 1, true, categoryOrder, i + 16,
                    true, false, System.currentTimeMillis()));
            categoryOrder++;
        }

        categories.add(new CategoryEntity("", "#3485FF", 165, 1, true, categoryOrder, 26,
                true, false, System.currentTimeMillis()));
        categoryOrder++;
        categories.add(new CategoryEntity("", "#0D8EFF", 168, 1, true, categoryOrder, 29,
                true, false, System.currentTimeMillis()));
        categoryOrder++;
        categories.add(new CategoryEntity("", "#69B9FF", 169, 1, true, categoryOrder, 30,
                true, false, System.currentTimeMillis()));

        return categories;
    }

    private static List<CategoryEntity> getCategoryExpenseData() {
        String[] strArr = {"#34BFFF", "#0077C5", "#00A6A4", "#00C1BF", "#7FD000", "#FFCA00", "#43A047", "#FFAD00", "#EE4036", "#9C005E", "#652D90",
                "#9457FA", "#E31C9E", "#8B4513", "#5E4138"};
        int[] iArr = {1, 2, 144, 28, 66, 111, 175, 11, 57, 80, 78, 10, 39, 100, 146};

        List<CategoryEntity> categories = new ArrayList<>();
        int i = 0;
        int categoryOrder = 1;
        while (i < 15) {
            int i2 = i + 1;
            categories.add(new CategoryEntity("", strArr[i], iArr[i], 2, true, categoryOrder, i2, true, false,
                    System.currentTimeMillis()));
            i = i2;
            categoryOrder++;
        }

        categories.add(new CategoryEntity("", "#FF250B", 165, 2, true, categoryOrder, 26, true, false, System.currentTimeMillis()));
        categoryOrder++;
        categories.add(new CategoryEntity("", "#FF2861", 166, 2, true, categoryOrder, 27, true, false, System.currentTimeMillis()));
        categoryOrder++;
        categories.add(new CategoryEntity("", "#FF5877", 167, 2, true, categoryOrder, 28, true, false, System.currentTimeMillis()));

        return categories;
    }

    private static List<CategoryEntity> getCategoryTransferData() {

        List<CategoryEntity> categories = new ArrayList<>();

        categories.add(new CategoryEntity("", "#FF9800", 170, 3, true, 1, 31, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity("", "#00BCD4", 171, 3, true, 2, 32, true, false, System.currentTimeMillis()));

        return categories;
    }

    private static List<CategoryEntity> getGoalCategoryData(Context context) {

        List<CategoryEntity> categories = new ArrayList<>();

        categories.add(new CategoryEntity(context.getString(R.string.emergency_fund), "#F66F77", 5, 5, true, 1, 1, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.travel), "#AED8F4", 0, 5, true, 2, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.home), "#DBA877", 1, 5, true, 3, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.vehicle), "#F8D732", 2, 5, true, 4, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.education), "#424242", 3, 5, true, 5, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.medical), "#D75640", 4, 5, true, 6, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.gift), "#A04C8D", 6, 5, true, 7, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.investment), "#CA9D4E", 7, 5, true, 8, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.wedding), "#EDCF54", 8, 5, true, 9, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.gadgets), "#373535", 9, 5, true, 10, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.business), "#E6A13F", 10, 5, true, 11, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.baby_family), "#D95F45", 11, 5, true, 12, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.festival), "#141212", 12, 5, true, 13, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.laptop), "#5C81D5", 13, 5, true, 14, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.gaming), "#46609E", 14, 5, true, 15, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.shopping), "#D13434", 15, 5, true, 16, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.restaurant), "#472E11", 16, 5, true, 17, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.fitness), "#BE8EEF", 17, 5, true, 18, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.pets), "#9E8759", 18, 5, true, 19, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.religious), "#FF6F52", 19, 5, true, 20, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.charity), "#84C688", 20, 5, true, 21, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.visa), "#5158C3", 21, 5, true, 22, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.vacation), "#6A6B6B", 22, 5, true, 23, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.startup), "#943F42", 23, 5, true, 24, 0, true, false, System.currentTimeMillis()));
        categories.add(new CategoryEntity(context.getString(R.string.others), "#F1ED7B", 24, 5, true, 25, 0, true, false, System.currentTimeMillis()));

        return categories;
    }
}