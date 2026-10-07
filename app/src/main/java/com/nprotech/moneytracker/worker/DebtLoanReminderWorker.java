package com.nprotech.moneytracker.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.DebtLoanPaymentEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DebtLoanWorkManagerHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.repositories.DebtLoanRepository;

import java.util.List;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class DebtLoanReminderWorker extends Worker {

    private static final String CHANNEL_ID = "debt_loan_reminders";
    private static final String CHANNEL_NAME = "Debt & Loan Reminders";
    private final DebtLoanRepository repository;
    private final Context context;

    @AssistedInject
    public DebtLoanReminderWorker(@Assisted @NonNull Context context, @Assisted @NonNull WorkerParameters params, DebtLoanRepository repository) {
        super(context, params);
        this.context = context;
        this.repository = repository;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            long now = System.currentTimeMillis();
            List<DebtLoanPaymentEntity> duePayments = repository.getDueReminderPayments(now);
            for (DebtLoanPaymentEntity payment : duePayments) {
                try {
                    if (!isReminderAlreadyShown(payment)) {
                        showReminderNotification(payment);
                        markReminderShown(payment);
                    }
                } catch (Exception e) {
                    AppLogger.e(getClass(), "showReminderNotification", e);
                }
            }

            scheduleNext();
            return Result.success();
        } catch (Exception e) {
            AppLogger.e(getClass(), "doWork", e);
            return Result.retry();
        }
    }

    private void showReminderNotification(DebtLoanPaymentEntity payment) {

        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) {
            return;
        }

        createNotificationChannel(manager);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_app_logo_small)
                        .setContentTitle(context.getString(R.string.payment_reminder))
                        .setContentText(context.getString(R.string.your_debt_loan_payment_is_due_soon))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        manager.notify(getNotificationId(payment), builder.build());
    }

    private boolean isReminderAlreadyShown(DebtLoanPaymentEntity payment) {
        String key = getReminderKey(payment);
        return PreferenceManager.INSTANCE.getDebtLoanReminder(key);
    }

    private void markReminderShown(DebtLoanPaymentEntity payment) {
        String key = getReminderKey(payment);
        PreferenceManager.INSTANCE.setDebtLoanReminder(key, true);
    }

    private void createNotificationChannel(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Reminders for Debt & Loan payments");
            manager.createNotificationChannel(channel);
        }
    }

    private int getNotificationId(DebtLoanPaymentEntity payment) {
        if (payment.paymentNumber > 0) {
            return 50000 + payment.debtLoanId * 1000 + payment.paymentNumber;
        }
        return 50000 + payment.debtLoanId;
    }

    private void scheduleNext() {
        Long nextRunDate = repository.getEarliestReminderDate();
        if (nextRunDate == null) {
            return;
        }
        long delay = nextRunDate - System.currentTimeMillis();
        DebtLoanWorkManagerHelper.scheduleReminder(getApplicationContext(), Math.max(delay, 0));
    }

    private String getReminderKey(DebtLoanPaymentEntity payment) {
        return "reminder_" + payment.debtLoanId + "_" + payment.paymentNumber + "_" + payment.paymentDate;
    }
}