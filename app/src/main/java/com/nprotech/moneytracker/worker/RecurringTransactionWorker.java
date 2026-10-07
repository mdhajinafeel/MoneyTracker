package com.nprotech.moneytracker.worker;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nprotech.moneytracker.MoneyTrackerApp;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.CategoryEntity;
import com.nprotech.moneytracker.db.entites.RecurringTransactionEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.repositories.AccountRepository;
import com.nprotech.moneytracker.repositories.RecurringTransactionRepository;
import com.nprotech.moneytracker.repositories.TransactionRepository;
import com.nprotech.moneytracker.repositories.WalletRepository;
import com.nprotech.moneytracker.ui.activities.MainActivity;
import com.nprotech.moneytracker.ui.activities.SplashActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class RecurringTransactionWorker extends Worker {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final AccountRepository accountRepository;
    private static final String NOTIFICATION_CHANNEL_ID = "recurring_transactions";
    private static final int NOTIFICATION_ID = 2001;

    @AssistedInject
    public RecurringTransactionWorker(@Assisted @NonNull Context context, @Assisted @NonNull WorkerParameters workerParams,
                                      RecurringTransactionRepository recurringTransactionRepository, TransactionRepository transactionRepository,
                                      WalletRepository walletRepository, AccountRepository accountRepository) {
        super(context, workerParams);
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.accountRepository = accountRepository;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            long now = System.currentTimeMillis();

            // ================================
            // 15 MINUTE REMINDERS
            // ================================
            long reminderLimit = now + (15 * 60 * 1000L);
            List<RecurringTransactionEntity> reminderTransactions = recurringTransactionRepository.getUpcomingReminderTransactions(now, reminderLimit);
            if (reminderTransactions != null) {
                for (RecurringTransactionEntity recurring : reminderTransactions) {
                    try {
                        checkRecurringReminder(recurring, now);
                    } catch (Exception e) {
                        AppLogger.e(getClass(), "checkRecurringReminder: " + recurring.id, e);
                    }
                }
            }

            // ================================
            // DUE TRANSACTIONS
            // ================================
            List<RecurringTransactionEntity> recurringTransactions = recurringTransactionRepository.getDueRecurringTransactions(now);
            if (recurringTransactions == null || recurringTransactions.isEmpty()) {
                return Result.success();
            }

            int createdCount = 0;
            double totalExpense = 0;
            double totalIncome = 0;

            for (RecurringTransactionEntity recurring : recurringTransactions) {
                try {
                    int created = processRecurringTransaction(recurring, now);

                    if (created > 0) {
                        createdCount += created;

                        if (recurring.type == TransactionEntity.TYPE_EXPENSE) {
                            totalExpense += recurring.amount + recurring.fee;
                        } else if (recurring.type == TransactionEntity.TYPE_INCOME) {
                            totalIncome += recurring.amount;
                        }
                    }
                } catch (Exception e) {
                    AppLogger.e(getClass(), "processRecurringTransaction: " + recurring.id, e);
                }
            }

            if (createdCount > 0) {
                showRecurringTransactionNotification(createdCount, totalExpense, totalIncome);
            }

            return Result.success();
        } catch (Exception e) {
            AppLogger.e(getClass(), "doWork", e);
            return Result.retry();
        }
    }

    private int processRecurringTransaction(RecurringTransactionEntity recurring, long now) {

        if (!isRecurringProcessable(recurring)) {
            return 0;
        }

        int createdCount = 0;

        while (recurring.nextRunDate > 0 && recurring.nextRunDate <= now && recurring.isActive) {
            if (recurring.untilDate > 0 && recurring.nextRunDate > recurring.untilDate) {
                recurring.status = RecurringTransactionEntity.STATUS_COMPLETED;
                recurring.isActive = false;
                recurring.updatedAt = System.currentTimeMillis();
                recurringTransactionRepository.update(recurring);
                break;
            }

            if (recurring.repeatTimes > 0 && recurring.completedTimes >= recurring.repeatTimes) {
                recurring.status = RecurringTransactionEntity.STATUS_COMPLETED;
                recurring.isActive = false;
                recurring.updatedAt = System.currentTimeMillis();
                recurringTransactionRepository.update(recurring);
                break;
            }

            if (recurring.createTransaction) {
                createTransaction(recurring);
                createdCount++;
            }

            recurring.completedTimes++;

            if (recurring.repeatTimes > 0 && recurring.completedTimes >= recurring.repeatTimes) {
                recurring.status = RecurringTransactionEntity.STATUS_COMPLETED;
                recurring.isActive = false;
                recurring.nextRunDate = 0;
                recurring.updatedAt = System.currentTimeMillis();
                recurringTransactionRepository.update(recurring);
                break;
            }

            /*
             * Calculate the next occurrence from the previous
             * occurrence, NOT from the current time.
             */
            long previousRunDate = recurring.nextRunDate;
            recurring.nextRunDate = calculateNextRunDate(previousRunDate, recurring);
            recurring.reminderShown = false;

            /*
             * If the next occurrence is beyond the until date,
             * deactivate the recurrence.
             */
            if (recurring.untilDate > 0 && recurring.nextRunDate > recurring.untilDate) {
                recurring.status = RecurringTransactionEntity.STATUS_COMPLETED;
                recurring.isActive = false;
                recurring.nextRunDate = 0;
            }

            recurring.updatedAt = System.currentTimeMillis();
            recurringTransactionRepository.update(recurring);

            /*
             * Safety protection against an invalid recurrence
             * configuration causing an infinite loop.
             */
            if (recurring.nextRunDate <= previousRunDate) {
                recurring.isActive = false;
                recurring.updatedAt = System.currentTimeMillis();
                recurringTransactionRepository.update(recurring);
                break;
            }
        }

        return createdCount;
    }

    private void checkRecurringReminder(RecurringTransactionEntity recurring, long now) {
        if (!recurring.isActive || recurring.isDeleted || recurring.status == RecurringTransactionEntity.STATUS_PAUSED
                || recurring.status == RecurringTransactionEntity.STATUS_COMPLETED) {
            return;
        }

        if (!recurring.reminder) {
            return;
        }

        if (recurring.nextRunDate <= 0) {
            return;
        }

        if (recurring.reminderShown) {
            return;
        }

        // 15 minutes before the recurring transaction
        long reminderTime = recurring.nextRunDate - (15 * 60 * 1000L);

        // Reminder window:
        // now >= reminder time
        // and transaction is not yet due
        if (now >= reminderTime && now < recurring.nextRunDate) {
            showRecurringReminderNotification(recurring);
            recurring.reminderShown = true;
            recurring.updatedAt = System.currentTimeMillis();
            recurringTransactionRepository.update(recurring);
        }
    }

    private void createTransaction(RecurringTransactionEntity recurring) {

        AccountEntity account = accountRepository.getAccountDetailById(recurring.accountId);
        if (account == null) {
            throw new IllegalStateException("Account not found: " + recurring.accountId);
        }

        if (recurring.type == TransactionEntity.TYPE_TRANSFER) {
            createTransferTransaction(recurring, account);
        } else {
            createIncomeOrExpenseTransaction(recurring, account);
        }
    }

    private void createIncomeOrExpenseTransaction(RecurringTransactionEntity recurring, AccountEntity account) {

        WalletEntity wallet = walletRepository.getWalletByWalletId(recurring.walletId);
        if (wallet == null) {
            throw new IllegalStateException("Wallet not found: " + recurring.walletId);
        }

        long transactionTime = recurring.nextRunDate;
        TransactionEntity transaction = new TransactionEntity();
        transaction.accountId = recurring.accountId;
        transaction.walletId = recurring.walletId;
        transaction.fromWalletId = 0;
        transaction.type = recurring.type;
        transaction.amount = recurring.amount;
        transaction.fee = recurring.fee;
        transaction.categoryId = recurring.categoryId;
        transaction.defaultCategoryId = recurring.defaultCategoryId;
        transaction.description = recurring.description;
        transaction.memo = recurring.notes;
        transaction.transactionDate = transactionTime;
        transaction.createdAt = transactionTime;
        transaction.updatedAt = transactionTime;
        transaction.serverId = 0;
        transaction.tempTransactionServerId = "R_" + recurring.id + "_" + transactionTime;
        transaction.parentTransactionId = "";
        transaction.isDeleted = false;
        transaction.isFromRecurring = true;

        /*
         * Income
         */
        if (recurring.type == TransactionEntity.TYPE_INCOME) {
            wallet.amount += recurring.amount;
            if (!wallet.isExclude) {
                account.balance += recurring.amount;
            }
        }

        /*
         * Expense
         */
        else if (recurring.type == TransactionEntity.TYPE_EXPENSE) {
            double totalExpense = recurring.amount + recurring.fee;
            wallet.amount -= totalExpense;
            if (!wallet.isExclude) {
                account.balance -= totalExpense;
            }
        }

        transactionRepository.saveRecurringIncomeExpenseTransaction(transaction, wallet, account);
    }

    private void createTransferTransaction(RecurringTransactionEntity recurring, AccountEntity account) {
        WalletEntity fromWallet = walletRepository.getWalletByWalletId(recurring.fromWalletId);
        WalletEntity toWallet = walletRepository.getWalletByWalletId(recurring.walletId);

        if (fromWallet == null) {
            throw new IllegalStateException("From wallet not found: " + recurring.fromWalletId);
        }

        if (toWallet == null) {
            throw new IllegalStateException("To wallet not found: " + recurring.walletId);
        }

        long transactionTime = recurring.nextRunDate;

        /*
         * Amount in account/base currency.
         */
        double accountAmount = recurring.amount * fromWallet.exchangeRate;

        /*
         * Converted amount in destination wallet currency.
         */
        double convertedAmount = accountAmount / toWallet.exchangeRate;

        /*
         * Transfer transaction.
         */
        TransactionEntity transaction = new TransactionEntity();
        transaction.accountId = recurring.accountId;
        transaction.walletId = toWallet.id;
        transaction.fromWalletId = fromWallet.id;
        transaction.type = TransactionEntity.TYPE_TRANSFER;
        transaction.amount = recurring.amount;
        transaction.fee = recurring.fee;
        transaction.categoryId = recurring.categoryId;
        transaction.defaultCategoryId = recurring.defaultCategoryId;
        transaction.description = recurring.description;
        transaction.memo = recurring.notes;
        transaction.transactionDate = transactionTime;
        transaction.createdAt = transactionTime;
        transaction.updatedAt = transactionTime;
        transaction.serverId = 0;
        transaction.tempTransactionServerId = "R_" + recurring.id + "_" + transactionTime;
        transaction.parentTransactionId = "";
        transaction.accountAmount = accountAmount;
        transaction.convertedAmount = convertedAmount;
        transaction.isDeleted = false;
        transaction.isFromRecurring = true;

        /*
         * Update wallets.
         */
        fromWallet.amount -= recurring.amount;
        toWallet.amount += convertedAmount;

        /*
         * Update account balance only when one side is excluded.
         */
        if (!fromWallet.isExclude && toWallet.isExclude) {
            account.balance -= accountAmount;
        } else if (fromWallet.isExclude && !toWallet.isExclude) {
            account.balance += accountAmount;
        }

        /*
         * Transfer fee is stored as a separate expense transaction,
         * exactly like normal transfers in the app.
         */
        TransactionEntity feeTransaction = null;

        if (recurring.fee > 0) {
            feeTransaction = createTransferFeeTransaction(recurring, fromWallet, transactionTime, transaction.tempTransactionServerId);

            /*
             * Fee is paid from the source wallet.
             */
            fromWallet.amount -= recurring.fee;

            /*
             * Fee affects account balance only when the source wallet
             * is not excluded.
             */
            if (!fromWallet.isExclude) {
                account.balance -= recurring.fee;
            }
        }

        /*
         * Use the repository's existing transfer transaction method
         * so the transfer + fee + wallet/account updates happen
         * inside the existing Room transaction.
         */
        transactionRepository.saveTransferTransaction(transaction, feeTransaction, fromWallet, toWallet, account);
    }

    private TransactionEntity createTransferFeeTransaction(RecurringTransactionEntity recurring, WalletEntity fromWallet, long transactionTime, String parentTransactionId) {

        TransactionEntity feeTransaction = new TransactionEntity();
        feeTransaction.serverId = 0;
        feeTransaction.tempTransactionServerId = "R_" + recurring.id + "_" + transactionTime + "_FEE";
        feeTransaction.accountId = recurring.accountId;
        feeTransaction.walletId = fromWallet.id;
        feeTransaction.fromWalletId = fromWallet.id;
        feeTransaction.type = TransactionEntity.TYPE_EXPENSE;
        feeTransaction.amount = recurring.fee;
        feeTransaction.fee = 0;

        CategoryEntity feeCategory = transactionRepository.getTransferFeeCategory();

        if (feeCategory != null) {
            feeTransaction.categoryId = feeCategory.id;
            feeTransaction.defaultCategoryId = feeCategory.defaultCategory;
        } else {
            feeTransaction.categoryId = Constants.DEFAULT_CATEGORY_FEE_ID;
            feeTransaction.defaultCategoryId = Constants.CATEGORY_FEE_ID;
        }

        feeTransaction.transactionDate = transactionTime;
        feeTransaction.description = "fee";
        feeTransaction.memo = "";
        feeTransaction.parentTransactionId = parentTransactionId;
        feeTransaction.createdAt = transactionTime;
        feeTransaction.updatedAt = transactionTime;
        feeTransaction.isDeleted = false;
        feeTransaction.isFromRecurring = true;
        return feeTransaction;
    }

    private long calculateNextRunDate(long previousRunDate, RecurringTransactionEntity entity) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(previousRunDate);

        return switch (entity.repeatType) {
            case Constants.REPEAT_DAILY -> calculateNextDailyRunDate(calendar, entity);
            case Constants.REPEAT_WEEKLY -> calculateNextWeeklyRunDate(calendar, entity);
            case Constants.REPEAT_MONTHLY -> calculateNextMonthlyRunDate(calendar, entity);
            case Constants.REPEAT_YEARLY -> calculateNextYearlyRunDate(calendar, entity);
            default -> 0;
        };
    }

    private long calculateNextDailyRunDate(Calendar calendar, RecurringTransactionEntity entity) {
        calendar.add(Calendar.DAY_OF_MONTH, Math.max(1, entity.repeatInterval));
        return calendar.getTimeInMillis();
    }

    private long calculateNextWeeklyRunDate(Calendar calendar, RecurringTransactionEntity entity) {

        List<Integer> weekDays = parseWeekDays(entity.repeatWeekDays);

        if (weekDays.isEmpty()) {
            /*
             * Fallback to the same weekday.
             */
            calendar.add(Calendar.WEEK_OF_YEAR, Math.max(1, entity.repeatInterval));
            return calendar.getTimeInMillis();
        }

        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

        /*
         * Find the next selected weekday in the current week.
         */
        int nextDay = -1;
        for (int day : weekDays) {
            if (day > currentDay) {
                nextDay = day;
                break;
            }
        }

        if (nextDay != -1) {
            int daysToAdd = nextDay - currentDay;
            calendar.add(Calendar.DAY_OF_MONTH, daysToAdd);
            return calendar.getTimeInMillis();
        }

        /*
         * No selected weekday remains in this week.
         * Move to the next recurrence week.
         */
        int firstDay = weekDays.get(0);

        calendar.add(Calendar.WEEK_OF_YEAR, Math.max(1, entity.repeatInterval));
        int newCurrentDay = calendar.get(Calendar.DAY_OF_WEEK);
        int daysToAdd = firstDay - newCurrentDay;
        if (daysToAdd < 0) {
            daysToAdd += 7;
        }
        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd);

        return calendar.getTimeInMillis();
    }

    private long calculateNextMonthlyRunDate(Calendar calendar, RecurringTransactionEntity entity) {

        if (entity.monthlyMode == Constants.MONTHLY_LAST_DAY) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, Math.max(1, entity.repeatInterval));
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        } else {
            int originalDay = entity.monthlyDay;
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, Math.max(1, entity.repeatInterval));
            int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            calendar.set(Calendar.DAY_OF_MONTH, Math.min(originalDay, maxDay));
        }
        return calendar.getTimeInMillis();
    }

    private long calculateNextYearlyRunDate(Calendar calendar, RecurringTransactionEntity entity) {
        int month = entity.yearlyMonth;
        int day = entity.yearlyDay;
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.YEAR, Math.max(1, entity.repeatInterval));
        calendar.set(Calendar.MONTH, month);
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, Math.min(day, maxDay));
        return calendar.getTimeInMillis();
    }

    private List<Integer> parseWeekDays(String value) {
        List<Integer> result = new ArrayList<>();

        if (value == null || value.trim().isEmpty()) {
            return result;
        }

        String[] values = value.split(",");

        for (String item : values) {
            try {
                int day = Integer.parseInt(item.trim());
                if (!result.contains(day)) {
                    result.add(day);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        Collections.sort(result);
        return result;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Recurring Transactions",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications for automatically created recurring transactions");
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showRecurringTransactionNotification(int createdCount, double totalExpense, double totalIncome) {

        Context context = getApplicationContext();

        // Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(context,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        createNotificationChannel();

        Intent openIntent;
        if (MoneyTrackerApp.isAppProcessAlive()) {
            // Existing process
            openIntent = new Intent(context, MainActivity.class);
        } else {
            // Fresh process after app was killed
            openIntent = new Intent(context, SplashActivity.class);
        }
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 2001, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String title;
        if (createdCount == 1) {
            title = context.getString(R.string.recurring_transaction_added);
        } else {
            title = context.getString(R.string.recurring_transactions_added);
        }

        String message;
        if (createdCount == 1) {
            if (totalExpense > 0 && totalIncome == 0) {
                message = context.getString(R.string.a_recurring_expense_was_added);
            } else if (totalIncome > 0 && totalExpense == 0) {
                message = context.getString(R.string.a_recurring_income_was_added);
            } else {
                message = context.getString(R.string.a_recurring_transaction_was_added);
            }
        } else {
            message = createdCount + context.getString(R.string.recurring_transactions_were_added);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_app_logo_small)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void showRecurringReminderNotification(RecurringTransactionEntity recurring) {

        Context context = getApplicationContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        createNotificationChannel();

        Intent openIntent;
        if (MoneyTrackerApp.isAppProcessAlive()) {
            // Existing process
            openIntent = new Intent(context, MainActivity.class);
        } else {
            // Fresh process after app was killed
            openIntent = new Intent(context, SplashActivity.class);
        }
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, recurring.id, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String title = context.getString(R.string.recurring_transaction_reminder);
        String message;

        if (recurring.type == TransactionEntity.TYPE_EXPENSE) {
            message = context.getString(R.string.recurring_expense_due);
        } else if (recurring.type == TransactionEntity.TYPE_INCOME) {
            message = context.getString(R.string.recurring_income_due);
        } else {
            message = context.getString(R.string.recurring_transfer_due);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_app_logo_small)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(3000 + recurring.id, builder.build());
    }

    private boolean isRecurringProcessable(RecurringTransactionEntity recurring) {
        return recurring.isActive
                && !recurring.isDeleted
                && recurring.status != RecurringTransactionEntity.STATUS_PAUSED
                && recurring.status != RecurringTransactionEntity.STATUS_COMPLETED;
    }
}