package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.models.CalendarSummaryModel;
import com.nprotech.moneytracker.models.TransactionWithDetails;
import com.nprotech.moneytracker.repositories.TransactionRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CalendarViewModel extends ViewModel {

    private final TransactionRepository transactionRepository;
    private final MutableLiveData<CalendarRange> calendarRange = new MutableLiveData<>();
    private final MutableLiveData<DayRange> dayRange = new MutableLiveData<>();
    private final LiveData<List<TransactionWithDetails>> dayTransactions;
    private final LiveData<List<CalendarSummaryModel>> calendarSummary;
    private final LiveData<CalendarSummaryModel> calendarHeader;

    @Inject
    public CalendarViewModel(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;

        calendarSummary = Transformations.switchMap(calendarRange, range ->
                this.transactionRepository.getCalendarSummary(
                        range.accountId,
                        range.startDate,
                        range.endDate));

        calendarHeader = Transformations.switchMap(calendarRange, range ->
                this.transactionRepository.getCalendarHeader(
                        range.accountId,
                        range.startDate,
                        range.endDate));

        dayTransactions = Transformations.switchMap(dayRange, range ->
                transactionRepository.getTransactionsForDay(
                        range.accountId,
                        range.start,
                        range.end
                ));
    }

    public void loadCalendar(int accountId, long startDate, long endDate) {
        calendarRange.setValue(new CalendarRange(accountId, startDate, endDate));
    }

    public LiveData<List<CalendarSummaryModel>> getCalendarSummary() {
        return calendarSummary;
    }

    public LiveData<CalendarSummaryModel> getCalendarHeader() {
        return calendarHeader;
    }

    public static class CalendarRange {
        public int accountId;
        public long startDate, endDate;

        public CalendarRange(int accountId, long startDate, long endDate) {
            this.accountId = accountId;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    public LiveData<List<TransactionWithDetails>> getDayTransactions() {
        return dayTransactions;
    }

    public void loadDayTransactions(int accountId, long start, long end) {
        dayRange.setValue(new DayRange(accountId, start, end));
    }

    public static class DayRange {
        public int accountId;
        public long start, end;

        public DayRange(int accountId, long start, long end) {
            this.accountId = accountId;
            this.start = start;
            this.end = end;
        }
    }
}