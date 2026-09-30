package com.nprotech.moneytracker.viewmodel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.db.entites.DebtLoanEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanPaymentEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanPaymentTransactionEntity;
import com.nprotech.moneytracker.db.entites.DebtLoanTransactionAttachmentEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.models.DebtLoanPaymentWithDetails;
import com.nprotech.moneytracker.models.DebtLoanTransactionWithDetails;
import com.nprotech.moneytracker.models.DebtLoanWithDetails;
import com.nprotech.moneytracker.repositories.DebtLoanRepository;
import com.nprotech.moneytracker.wrapper.SingleLiveEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.hilt.android.lifecycle.HiltViewModel;
import jakarta.inject.Inject;

@HiltViewModel
public class DebtLoanViewModel extends ViewModel {

    private final DebtLoanRepository debtLoanRepository;
    private final SingleLiveEvent<Boolean> dataSavedStatus = new SingleLiveEvent<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean loading = false, hasMore = true;
    private static final int PAGE_SIZE = 100;
    private int currentPage = 0, debtLoanType, currentSortType, currentStatusType;
    private final MutableLiveData<List<DebtLoanWithDetails>> debtLoanList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> transactionDeleted = new MutableLiveData<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private int transactionId = 0, paymentId = 0;

    @Inject
    public DebtLoanViewModel(DebtLoanRepository debtLoanRepository) {
        this.debtLoanRepository = debtLoanRepository;
    }

    public void saveDebtLoan(DebtLoanEntity entity, List<DebtLoanPaymentEntity> payments, Context context) {
        executor.execute(() -> {
            long debtLoanId = debtLoanRepository.saveDebtLoan(entity, payments, context);

            if (debtLoanId > 0) {
                dataSavedStatus.postValue(true);
            } else {
                dataSavedStatus.postValue(false);
            }
        });
    }

    public LiveData<Boolean> getDataSavedStatus() {
        return dataSavedStatus;
    }

    public void updateDebtLoan(DebtLoanEntity entity, List<DebtLoanPaymentEntity> payments) {
        executor.execute(() -> {
            int result = debtLoanRepository.updateDebtLoanAndPayments(entity, payments);
            dataSavedStatus.postValue(result > 0);
        });
    }

    public void updatePaymentTransaction(DebtLoanPaymentTransactionEntity transaction) {
        executor.execute(() -> {
            int result = debtLoanRepository.updatePaymentTransaction(transaction);
            dataSavedStatus.postValue(result > 0);
        });
    }

    public void saveFlexiblePaymentTransaction(DebtLoanPaymentTransactionEntity transaction, DebtLoanEntity debtLoan) {
        executor.execute(() -> {
            int result = debtLoanRepository.saveFlexiblePaymentTransaction(transaction, debtLoan);
            transactionId = result;
            paymentId = 0;
            dataSavedStatus.postValue(result > 0);
        });
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void saveScheduledPaymentTransaction(DebtLoanPaymentTransactionEntity transaction, DebtLoanPaymentEntity scheduledPayment, DebtLoanEntity debtLoan) {
        executor.execute(() -> {
            int result = debtLoanRepository.saveScheduledPaymentTransaction(transaction, scheduledPayment, debtLoan);
            transactionId = result;
            paymentId = scheduledPayment.debtLoanPaymentId;
            dataSavedStatus.postValue(result > 0);
        });
    }

    public void deleteDebtLoan(int debtLoanId) {
        executor.execute(() -> {
            boolean result = debtLoanRepository.deleteDebtLoan(debtLoanId);
            dataSavedStatus.postValue(result);
            if (result) {
                mainHandler.post(() -> loadDebtLoanHistory(debtLoanType, currentSortType, currentStatusType));
            }
        });
    }

    public LiveData<DebtLoanWithDetails> getDebtLoanById(int id) {
        return debtLoanRepository.getDebtLoanById(id);
    }

    public LiveData<List<DebtLoanWithDetails>> getDebtLoanList() {
        return debtLoanList;
    }

    public void loadDebtLoanHistory(int type, int sortType, int statusType) {
        mainHandler.post(() -> {
            debtLoanType = type;
            currentSortType = sortType;
            currentStatusType = statusType;
            currentPage = 0;
            hasMore = true;
            debtLoanList.setValue(new ArrayList<>());
            loadNextPage();
        });
    }

    public void loadNextPage() {
        if (loading || !hasMore) {
            return;
        }
        loading = true;

        final int type = debtLoanType;
        final int sortType = currentSortType;
        final int statusType = currentStatusType;

        executor.execute(() -> {
            try {
                List<DebtLoanWithDetails> pageData = debtLoanRepository.getAllDebtLoans(type, sortType, statusType, currentPage, PAGE_SIZE);
                if (pageData.size() < PAGE_SIZE) {
                    hasMore = false;
                }

                List<DebtLoanWithDetails> currentList = debtLoanList.getValue();
                if (currentList == null) {
                    currentList = new ArrayList<>();
                } else {
                    currentList = new ArrayList<>(currentList);
                }
                currentList.addAll(pageData);

                debtLoanList.postValue(currentList);
                currentPage++;
            } catch (Exception e) {
                AppLogger.e(getClass(), "loadNextPage", e);
            } finally {
                loading = false;
            }
        });
    }

    public int getTotalInstallments(int debtLoanId) {
        return debtLoanRepository.getTotalInstallments(debtLoanId);
    }

    public DebtLoanPaymentEntity getNextPendingPayment(int debtLoanId) {
        return debtLoanRepository.getNextPendingPayment(debtLoanId);
    }

    public LiveData<List<DebtLoanTransactionWithDetails>> getPaymentTransactionByDebtLoanId(int debtLoanId) {
        return debtLoanRepository.getPaymentTransactionByDebtLoanId(debtLoanId);
    }

    public LiveData<List<DebtLoanTransactionWithDetails>> getPaymentTransactionByDebtLoanId(int debtLoanId, int debtLoanPaymentId) {
        return debtLoanRepository.getPaymentTransactionByDebtLoanId(debtLoanId, debtLoanPaymentId);
    }

    public LiveData<List<DebtLoanPaymentWithDetails>> getPaymentsByDebtLoanId(int debtLoanId) {
        return debtLoanRepository.getPaymentsByDebtLoanId(debtLoanId);
    }

    public void saveTransactionAttachment(List<DebtLoanTransactionAttachmentEntity> transactionAttachments) {
        debtLoanRepository.saveTransactionAttachment(transactionAttachments);
    }

    public LiveData<DebtLoanTransactionWithDetails> getPaymentDetailById(int debtLoanTransactionId, int debtLoanId, int debtLoanPaymentId) {
        return debtLoanRepository.getPaymentDetailById(debtLoanTransactionId, debtLoanId, debtLoanPaymentId);
    }

    public List<DebtLoanTransactionAttachmentEntity> getAttachments(int debtLoanId, int debtLoanPaymentId, int debtLoanTransactionId) {
        return debtLoanRepository.getAttachments(debtLoanId, debtLoanPaymentId, debtLoanTransactionId);
    }

    public LiveData<DebtLoanPaymentWithDetails> getPaymentDetailByDebtLoanId(int debtLoanId, int debtLoanPaymentId) {
        return debtLoanRepository.getPaymentDetailByDebtLoanId(debtLoanId, debtLoanPaymentId);
    }

    public LiveData<DebtLoanPaymentTransactionEntity> getTransactionById(int transactionId, int debtLoanId) {
        return debtLoanRepository.getTransactionById(transactionId, debtLoanId);
    }

    public void deleteAttachment(String attachmentPath, String tempDebtLoanServerId, int debtLoanId, int debtLoanPaymentId, int debtLoanPaymentTransactionId) {
        debtLoanRepository.deleteAttachment(attachmentPath, tempDebtLoanServerId, debtLoanId, debtLoanPaymentId, debtLoanPaymentTransactionId);
    }

    public LiveData<Boolean> getTransactionDeleted() {
        return transactionDeleted;
    }

    public void deletePaymentTransaction(int transactionId, int debtLoanId, int debeLoanPaymentId) {
        executor.execute(() -> {
            int result = debtLoanRepository.deletePaymentTransaction(transactionId, debtLoanId, debeLoanPaymentId);
            transactionDeleted.postValue(result > 0);
        });
    }

    public LiveData<DebtLoanPaymentEntity> getPaymentDataById(int paymentId) {
        return debtLoanRepository.getPaymentDataById(paymentId);
    }

    @SuppressLint("EmptySuperCall")
    @Override
    protected void onCleared() {
        executor.shutdown();
        super.onCleared();
    }
}