package com.nprotech.moneytracker.repositories;

import com.nprotech.moneytracker.db.dao.BudgetDao;
import com.nprotech.moneytracker.db.entites.BudgetCategoryAmountEntity;
import com.nprotech.moneytracker.db.entites.BudgetCategoryEntity;
import com.nprotech.moneytracker.db.entites.BudgetEntity;
import com.nprotech.moneytracker.db.entites.BudgetWalletEntity;
import com.nprotech.moneytracker.models.BudgetPeriod;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class BudgetRepository {

    private final BudgetDao budgetDao;

    public BudgetRepository(BudgetDao budgetDao) {
        this.budgetDao = budgetDao;
    }

    // -------------------------
    // SAVE BUDGET
    // -------------------------
    public long saveBudget(BudgetEntity budget, Set<Integer> selectedWalletIds, Set<Integer> selectedCategoryIds, Map<Integer, Double> categoryAmounts) {

        long now = System.currentTimeMillis();
        String tempBudgetServerId = "B_" + now;

        budget.isSynced = false;
        budget.isDeleted = false;
        budget.createdAt = now;
        budget.updatedAt = now;
        budget.budgetServerId = 0;
        budget.tempBudgetServerId = tempBudgetServerId;

        long budgetId = budgetDao.insert(budget);

        if (budget.repeatEnabled) {
            budget.repeatGroupId = (int) budgetId;
            BudgetPeriod nextPeriod = calculateNextPeriod(budget.periodId, budget.startDate, budget.endDate);
            budget.nextRepeatDate = nextPeriod.startDate;
            budgetDao.update(budget);
        }

        // -----------------------------------------------------
        // WALLETS
        // -----------------------------------------------------
        if (selectedWalletIds != null && !selectedWalletIds.isEmpty()) {
            for (Integer walletId : selectedWalletIds) {
                if (walletId == null) {
                    continue;
                }

                BudgetWalletEntity wallet = new BudgetWalletEntity();
                wallet.budgetId = (int) budgetId;
                wallet.walletId = walletId;
                wallet.isSynced = false;
                wallet.isDeleted = false;
                wallet.createdAt = now;
                wallet.updatedAt = now;
                wallet.budgetWalletId = 0;
                wallet.tempBudgetWalletId = "BW_" + now;
                wallet.budgetServerId = 0;
                wallet.tempBudgetServerId = tempBudgetServerId;
                budgetDao.insertWallet(wallet);
            }
        }

        // -----------------------------------------------------
        // CATEGORIES
        // -----------------------------------------------------
        if (selectedCategoryIds != null && !selectedCategoryIds.isEmpty()) {
            for (Integer categoryId : selectedCategoryIds) {
                if (categoryId == null) {
                    continue;
                }

                BudgetCategoryEntity category = new BudgetCategoryEntity();
                category.budgetId = (int) budgetId;
                category.categoryId = categoryId;
                category.isSynced = false;
                category.isDeleted = false;
                category.createdAt = now;
                category.updatedAt = now;
                category.budgetCategoryId = 0;
                category.tempBudgetCategoryId = "BC_" + now;
                category.budgetServerId = 0;
                category.tempBudgetServerId = tempBudgetServerId;
                budgetDao.insertCategory(category);
            }
        }

        // -----------------------------------------------------
        // CATEGORY AMOUNTS
        // Only for Separate Amount method
        // -----------------------------------------------------
        if (budget.methodId == 2 && categoryAmounts != null && !categoryAmounts.isEmpty()) {
            for (Map.Entry<Integer, Double> entry : categoryAmounts.entrySet()) {
                Integer categoryId = entry.getKey();
                Double amount = entry.getValue();
                if (categoryId == null || amount == null) {
                    continue;
                }

                BudgetCategoryAmountEntity categoryAmount = new BudgetCategoryAmountEntity();
                categoryAmount.budgetId = (int) budgetId;
                categoryAmount.categoryId = categoryId;
                categoryAmount.amount = amount;
                categoryAmount.isSynced = false;
                categoryAmount.isDeleted = false;
                categoryAmount.createdAt = now;
                categoryAmount.updatedAt = now;
                categoryAmount.budgetCategoryAmountId = 0;
                categoryAmount.tempBudgetCategoryAmountId = "BCA_" + now;
                categoryAmount.budgetServerId = 0;
                categoryAmount.tempBudgetServerId = tempBudgetServerId;
                budgetDao.insertCategoryAmount(categoryAmount);
            }
        }

        return budgetId;
    }

    // =========================================================
    // UPDATE EXISTING BUDGET
    // =========================================================
    public void updateBudget(BudgetEntity budget, Set<Integer> selectedWalletIds, Set<Integer> selectedCategoryIds, Map<Integer, Double> categoryAmounts) {

        long now = System.currentTimeMillis();

        // -----------------------------------------------------
        // 1. UPDATE PARENT BUDGET ENTITY
        // -----------------------------------------------------
        budget.updatedAt = now;
        budget.isSynced = false;
        budget.isDeleted = false;
        budgetDao.update(budget);

        // -----------------------------------------------------
        // 2. UPDATE WALLETS
        // -----------------------------------------------------
        updateWallets(budget, selectedWalletIds, now);

        // -----------------------------------------------------
        // 3. UPDATE CATEGORIES
        // -----------------------------------------------------
        updateCategories(budget, selectedCategoryIds, now);

        // -----------------------------------------------------
        // 4. UPDATE CATEGORY AMOUNTS
        // -----------------------------------------------------
        updateCategoryAmounts(budget, categoryAmounts, now);
    }

    // =========================================================
    // UPDATE WALLETS
    // =========================================================
    private void updateWallets(BudgetEntity budget, Set<Integer> selectedWalletIds, long now) {
        int budgetId = budget.id;
        if (selectedWalletIds == null) {
            selectedWalletIds = new HashSet<>();
        }

        List<BudgetWalletEntity> existingWallets = budgetDao.getWallets(budgetId);
        Map<Integer, BudgetWalletEntity> existingMap = new HashMap<>();

        for (BudgetWalletEntity entity : existingWallets) {
            existingMap.put(entity.walletId, entity);
        }

        for (Integer walletId : selectedWalletIds) {
            if (walletId == null) {
                continue;
            }

            BudgetWalletEntity existing = existingMap.get(walletId);

            if (existing != null) {
                existing.isDeleted = false;
                existing.isSynced = false;
                existing.updatedAt = now;
                budgetDao.updateWallet(existing);
            } else {
                BudgetWalletEntity newWallet = new BudgetWalletEntity();
                newWallet.budgetId = budgetId;
                newWallet.walletId = walletId;
                newWallet.isSynced = false;
                newWallet.isDeleted = false;
                newWallet.createdAt = now;
                newWallet.updatedAt = now;
                newWallet.budgetWalletId = 0;
                newWallet.tempBudgetWalletId = "BW_" + now;
                newWallet.budgetServerId = budget.budgetServerId;
                newWallet.tempBudgetServerId = budget.tempBudgetServerId;
                budgetDao.insertWallet(newWallet);
            }
        }

        for (BudgetWalletEntity existing : existingWallets) {
            if (!selectedWalletIds.contains(existing.walletId) && !existing.isDeleted) {
                existing.isDeleted = true;
                existing.isSynced = false;
                existing.updatedAt = now;
                budgetDao.updateWallet(existing);
            }
        }
    }

    // =========================================================
    // UPDATE CATEGORIES
    // =========================================================
    private void updateCategories(BudgetEntity budget, Set<Integer> selectedCategoryIds, long now) {
        int budgetId = budget.id;
        if (selectedCategoryIds == null) {
            selectedCategoryIds = new HashSet<>();
        }

        List<BudgetCategoryEntity> existingCategories = budgetDao.getCategories(budgetId);
        Map<Integer, BudgetCategoryEntity> existingMap = new HashMap<>();

        for (BudgetCategoryEntity entity : existingCategories) {
            existingMap.put(entity.categoryId, entity);
        }

        for (Integer categoryId : selectedCategoryIds) {
            if (categoryId == null) {
                continue;
            }

            BudgetCategoryEntity existing = existingMap.get(categoryId);
            if (existing != null) {
                existing.isDeleted = false;
                existing.isSynced = false;
                existing.updatedAt = now;
                budgetDao.updateCategory(existing);
            } else {
                BudgetCategoryEntity newCategory = new BudgetCategoryEntity();
                newCategory.budgetId = budgetId;
                newCategory.categoryId = categoryId;
                newCategory.isSynced = false;
                newCategory.isDeleted = false;
                newCategory.createdAt = now;
                newCategory.updatedAt = now;
                newCategory.budgetCategoryId = 0;
                newCategory.tempBudgetCategoryId = "BC_" + now;
                newCategory.budgetServerId = budget.budgetServerId;
                newCategory.tempBudgetServerId = budget.tempBudgetServerId;
                budgetDao.insertCategory(newCategory);
            }
        }

        for (BudgetCategoryEntity existing : existingCategories) {
            if (!selectedCategoryIds.contains(existing.categoryId) && !existing.isDeleted) {
                existing.isDeleted = true;
                existing.isSynced = false;
                existing.updatedAt = now;
                budgetDao.updateCategory(existing);
            }
        }
    }

    // =========================================================
    // UPDATE CATEGORY AMOUNTS
    // =========================================================
    private void updateCategoryAmounts(BudgetEntity budget, Map<Integer, Double> categoryAmounts, long now) {

        int budgetId = budget.id;
        if (categoryAmounts == null) {
            categoryAmounts = new HashMap<>();
        }

        List<BudgetCategoryAmountEntity> existingAmounts = budgetDao.getCategoryAmounts(budgetId);
        Map<Integer, BudgetCategoryAmountEntity> existingMap = new HashMap<>();

        for (BudgetCategoryAmountEntity entity : existingAmounts) {
            existingMap.put(entity.categoryId, entity);
        }

        // ---------------------------------------------------------
        // ONE AMOUNT FOR ALL
        // ---------------------------------------------------------
        if (budget.methodId != 2) {
            for (BudgetCategoryAmountEntity existing : existingAmounts) {
                if (!existing.isDeleted) {
                    existing.isDeleted = true;
                    existing.isSynced = false;
                    existing.updatedAt = now;
                    budgetDao.updateCategoryAmount(existing);
                }
            }
            return;
        }

        // ---------------------------------------------------------
        // SEPARATE AMOUNTS
        // ---------------------------------------------------------
        for (Map.Entry<Integer, Double> entry : categoryAmounts.entrySet()) {
            Integer categoryId = entry.getKey();
            Double amount = entry.getValue();

            if (categoryId == null || amount == null) {
                continue;
            }

            BudgetCategoryAmountEntity existing = existingMap.get(categoryId);
            if (existing != null) {
                existing.amount = amount;
                existing.isDeleted = false;
                existing.isSynced = false;
                existing.updatedAt = now;
                budgetDao.updateCategoryAmount(existing);
            } else {
                BudgetCategoryAmountEntity newAmount = new BudgetCategoryAmountEntity();
                newAmount.budgetId = budgetId;
                newAmount.categoryId = categoryId;
                newAmount.amount = amount;
                newAmount.isSynced = false;
                newAmount.isDeleted = false;
                newAmount.createdAt = now;
                newAmount.updatedAt = now;
                newAmount.budgetCategoryAmountId = 0;
                newAmount.tempBudgetCategoryAmountId = "BCA_" + now;
                newAmount.budgetServerId = budget.budgetServerId;
                newAmount.tempBudgetServerId = budget.tempBudgetServerId;
                budgetDao.insertCategoryAmount(newAmount);
            }
        }

        // ---------------------------------------------------------
        // REMOVED CATEGORY AMOUNTS
        // ---------------------------------------------------------
        for (BudgetCategoryAmountEntity existing : existingAmounts) {
            if (!categoryAmounts.containsKey(existing.categoryId) && !existing.isDeleted) {
                existing.isDeleted = true;
                existing.isSynced = false;
                existing.updatedAt = now;
                budgetDao.updateCategoryAmount(existing);
            }
        }
    }

    public BudgetEntity getBudgetById(int budgetId) {
        return budgetDao.getBudgetById(budgetId);
    }

    public List<BudgetWalletEntity> getWallets(int budgetId) {
        return budgetDao.getWallets(budgetId);
    }

    public List<BudgetCategoryEntity> getCategories(int budgetId) {
        return budgetDao.getCategories(budgetId);
    }

    public List<BudgetCategoryAmountEntity> getCategoryAmounts(int budgetId) {
        return budgetDao.getCategoryAmounts(budgetId);
    }

    private BudgetPeriod calculateNextPeriod(int periodId, long startDate, long endDate) {
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(startDate);

        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(endDate);

        Calendar nextStart = (Calendar) end.clone();
        nextStart.add(Calendar.DAY_OF_MONTH, 1);

        Calendar nextEnd = (Calendar) nextStart.clone();

        switch (periodId) {
            case 1:
                nextEnd.add(Calendar.DAY_OF_MONTH, 6);
                break;
            case 3:
                nextEnd.add(Calendar.MONTH, 3);
                nextEnd.add(Calendar.DAY_OF_MONTH, -1);
                break;
            case 4:
                nextEnd.set(nextStart.get(Calendar.YEAR), Calendar.DECEMBER, 31);
                break;
            case 5:
                long days = getInclusiveDays(start, end);
                nextEnd.add(Calendar.DAY_OF_MONTH, (int) days - 1);
                break;
            default:
                nextEnd.set(nextStart.get(Calendar.YEAR), nextStart.get(Calendar.MONTH), nextStart.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
        }

        return new BudgetPeriod(nextStart.getTimeInMillis(), nextEnd.getTimeInMillis());
    }

    private long getInclusiveDays(Calendar start, Calendar end) {
        Calendar startDay = Calendar.getInstance();
        startDay.clear();
        startDay.set(start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH));
        Calendar endDay = Calendar.getInstance();
        endDay.clear();
        endDay.set(end.get(Calendar.YEAR), end.get(Calendar.MONTH), end.get(Calendar.DAY_OF_MONTH));
        long diff = endDay.getTimeInMillis() - startDay.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(diff) + 1;
    }

    public void processRecurringBudgets() {

        long now = System.currentTimeMillis();
        List<BudgetEntity> budgets = budgetDao.getBudgetsDueForRepeat(now);
        for (BudgetEntity budget : budgets) {
            createNextBudget(budget);
        }
    }

    private void createNextBudget(BudgetEntity current) {

        long now = System.currentTimeMillis();

        BudgetPeriod nextPeriod = calculateNextPeriod(current.periodId, current.startDate, current.endDate);
        BudgetEntity next = new BudgetEntity();
        next.name = current.name;
        next.periodId = current.periodId;
        next.methodId = current.methodId;
        next.startDate = nextPeriod.startDate;
        next.endDate = nextPeriod.endDate;
        next.amount = current.amount;
        next.alertEnabled = current.alertEnabled;
        next.alertPercentage = current.alertPercentage;
        next.repeatEnabled = true;
        next.repeatGroupId = current.repeatGroupId;

        BudgetPeriod followingPeriod = calculateNextPeriod(next.periodId, next.startDate, next.endDate);
        next.nextRepeatDate = followingPeriod.startDate;

        next.isSynced = false;
        next.isDeleted = false;
        next.createdAt = now;
        next.updatedAt = now;
        next.budgetServerId = 0;
        next.tempBudgetServerId = "B_" + now;

        long newBudgetId = budgetDao.insert(next);

        copyBudgetWallets(current.id, (int) newBudgetId, next, now);
        copyBudgetCategories(current.id, (int) newBudgetId, next, now);
        if (current.methodId == 2) {
            copyBudgetCategoryAmounts(current.id, (int) newBudgetId, next, now);
        }
    }

    private void copyBudgetWallets(int oldBudgetId, int newBudgetId, BudgetEntity newBudget, long now) {

        List<BudgetWalletEntity> oldWallets = budgetDao.getWallets(oldBudgetId);
        List<BudgetWalletEntity> newWallets = new ArrayList<>();

        for (BudgetWalletEntity old : oldWallets) {
            if (old.isDeleted) {
                continue;
            }

            BudgetWalletEntity item = new BudgetWalletEntity();
            item.budgetId = newBudgetId;
            item.walletId = old.walletId;
            item.isSynced = false;
            item.isDeleted = false;
            item.createdAt = now;
            item.updatedAt = now;
            item.budgetWalletId = 0;
            item.tempBudgetWalletId = "BW_" + now;
            item.budgetServerId = 0;
            item.tempBudgetServerId = newBudget.tempBudgetServerId;
            newWallets.add(item);
        }

        if (!newWallets.isEmpty()) {
            budgetDao.insertWallets(newWallets);
        }
    }

    private void copyBudgetCategories(int oldBudgetId, int newBudgetId, BudgetEntity newBudget, long now) {
        List<BudgetCategoryEntity> oldCategories = budgetDao.getCategories(oldBudgetId);
        List<BudgetCategoryEntity> newCategories = new ArrayList<>();

        for (BudgetCategoryEntity old : oldCategories) {
            if (old.isDeleted) {
                continue;
            }

            BudgetCategoryEntity item = new BudgetCategoryEntity();
            item.budgetId = newBudgetId;
            item.categoryId = old.categoryId;
            item.isSynced = false;
            item.isDeleted = false;
            item.createdAt = now;
            item.updatedAt = now;
            item.budgetCategoryId = 0;
            item.tempBudgetCategoryId = "BC_" + now;
            item.budgetServerId = 0;
            item.tempBudgetServerId = newBudget.tempBudgetServerId;
            newCategories.add(item);
        }

        if (!newCategories.isEmpty()) {
            budgetDao.insertCategories(newCategories);
        }
    }

    private void copyBudgetCategoryAmounts(int oldBudgetId, int newBudgetId, BudgetEntity newBudget, long now) {

        List<BudgetCategoryAmountEntity> oldAmounts = budgetDao.getCategoryAmounts(oldBudgetId);
        List<BudgetCategoryAmountEntity> newAmounts = new ArrayList<>();

        for (BudgetCategoryAmountEntity old : oldAmounts) {
            if (old.isDeleted) {
                continue;
            }

            BudgetCategoryAmountEntity item = new BudgetCategoryAmountEntity();
            item.budgetId = newBudgetId;
            item.categoryId = old.categoryId;
            item.amount = old.amount;
            item.isSynced = false;
            item.isDeleted = false;
            item.createdAt = now;
            item.updatedAt = now;
            item.budgetCategoryAmountId = 0;
            item.tempBudgetCategoryAmountId = "BCA_" + now;
            item.budgetServerId = 0;
            item.tempBudgetServerId = newBudget.tempBudgetServerId;
            newAmounts.add(item);
        }

        if (!newAmounts.isEmpty()) {
            budgetDao.insertCategoryAmounts(newAmounts);
        }
    }
}