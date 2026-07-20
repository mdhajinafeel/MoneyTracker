package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.models.BalanceRangeModel;
import com.nprotech.moneytracker.models.BalanceSummaryModel;
import com.nprotech.moneytracker.models.CalendarRangeModel;
import com.nprotech.moneytracker.models.CalendarSummaryModel;
import com.nprotech.moneytracker.models.CategoryExpenseModel;
import com.nprotech.moneytracker.repositories.TransactionRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class StatisticsViewModel extends ViewModel {

    private final TransactionRepository transactionRepository;
    private final LiveData<CalendarSummaryModel> calendarHeader;
    private final MutableLiveData<CalendarRangeModel> calendarRange = new MutableLiveData<>();
    private final MutableLiveData<BalanceRangeModel> balanceRange = new MutableLiveData<>();
    private final LiveData<BalanceSummaryModel> balanceSummary;
    private final MutableLiveData<List<CategoryExpenseModel>> categoryExpense = new MutableLiveData<>();

    @Inject
    public StatisticsViewModel(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;

        calendarHeader = Transformations.switchMap(calendarRange, range ->
                this.transactionRepository.getCalendarHeader(
                        range.accountId,
                        range.startDate,
                        range.endDate));

        balanceSummary = Transformations.switchMap(balanceRange,
                range -> transactionRepository.getBalanceSummary(
                        range.accountId,
                        range.startDate,
                        range.endDate));
    }

    public void loadCalendar(int accountId, long startDate, long endDate) {
        calendarRange.setValue(new CalendarRangeModel(accountId, startDate, endDate));
    }

    public void loadBalanceSummary(int accountId, long startDate, long endDate) {
        balanceRange.setValue(new BalanceRangeModel(accountId, startDate, endDate));
    }

    public LiveData<CalendarSummaryModel> getCalendarHeader() {
        return calendarHeader;
    }

    public LiveData<BalanceSummaryModel> getBalanceSummary() {
        return balanceSummary;
    }

    public LiveData<List<CategoryExpenseModel>> getCategoryExpense() {
        return categoryExpense;
    }

    public void loadCategoryExpense(int accountId, long start, long end) {
        transactionRepository.getExpenseByCategory(accountId, start, end).observeForever(categoryExpense::setValue);
    }
}