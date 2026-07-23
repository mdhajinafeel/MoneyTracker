package com.nprotech.moneytracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.nprotech.moneytracker.enums.CalendarFilterType;
import com.nprotech.moneytracker.models.BalanceRangeModel;
import com.nprotech.moneytracker.models.BalanceSummaryModel;
import com.nprotech.moneytracker.models.BreakdownChartModel;
import com.nprotech.moneytracker.models.BreakdownFilter;
import com.nprotech.moneytracker.models.CalendarRangeModel;
import com.nprotech.moneytracker.models.CalendarSummaryModel;
import com.nprotech.moneytracker.models.CategoryExpenseModel;
import com.nprotech.moneytracker.repositories.TransactionRepository;

import java.util.Date;
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
    private final MutableLiveData<List<CategoryExpenseModel>> categoryIncome = new MutableLiveData<>();
    private final MutableLiveData<List<CategoryExpenseModel>> categoryExpenseTransaction = new MutableLiveData<>();
    private final MutableLiveData<List<CategoryExpenseModel>> categoryIncomeTransaction = new MutableLiveData<>();
    private final MutableLiveData<BreakdownFilter> breakdownFilter = new MutableLiveData<>();
    private final LiveData<List<BreakdownChartModel>> breakdownChart;

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

        breakdownChart = Transformations.switchMap(breakdownFilter, filter -> {

            switch (filter.filter) {

                case DAILY:
                    return transactionRepository.getHourlyBreakdown(
                            filter.accountId,
                            filter.transactionType,
                            filter.startDate,
                            filter.endDate);

                case WEEKLY:
                case MONTHLY:
                    return transactionRepository.getDailyBreakdown(
                            filter.accountId,
                            filter.transactionType,
                            filter.startDate,
                            filter.endDate);

                case QUARTERLY:
                case YEARLY:
                    return transactionRepository.getMonthlyBreakdown(
                            filter.accountId,
                            filter.transactionType,
                            filter.startDate,
                            filter.endDate);

                case ALL:
                    return transactionRepository.getYearlyBreakdown(
                            filter.accountId,
                            filter.transactionType);

                case CUSTOM:

                    long days = (filter.endDate - filter.startDate) / 86400000L;

                    if (days <= 31) {

                        return transactionRepository.getDailyBreakdown(
                                filter.accountId,
                                filter.transactionType,
                                filter.startDate,
                                filter.endDate);

                    } else if (days <= 365) {

                        return transactionRepository.getMonthlyBreakdown(
                                filter.accountId,
                                filter.transactionType,
                                filter.startDate,
                                filter.endDate);

                    } else {

                        return transactionRepository.getYearlyBreakdown(
                                filter.accountId,
                                filter.transactionType);
                    }
            }

            return new MutableLiveData<>();
        });
    }

    public void loadCalendar(int accountId, long startDate, long endDate) {
        calendarRange.setValue(new CalendarRangeModel(accountId, startDate, endDate, ""));
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

    public void loadCategoryTransaction(int accountId, long start, long end) {
        transactionRepository.getExpenseByCategory(accountId, start, end).observeForever(categoryExpense::setValue);
        transactionRepository.getIncomeByCategory(accountId, start, end).observeForever(categoryIncome::setValue);
    }

    public void loadCategoryExpense(int transactionType, int accountId, long start, long end) {
        transactionRepository.getExpenseByCategory(accountId, start, end).observeForever(categoryExpense::setValue);
        transactionRepository.getTransactionListByCategory(transactionType, accountId, start, end).observeForever(categoryExpenseTransaction::setValue);
    }

    public void loadCategoryIncome(int transactionType, int accountId, long start, long end) {
        transactionRepository.getIncomeByCategory(accountId, start, end).observeForever(categoryIncome::setValue);
        transactionRepository.getTransactionListByCategory(transactionType, accountId, start, end).observeForever(categoryIncomeTransaction::setValue);
    }

    public LiveData<List<CategoryExpenseModel>> getCategoryIncome() {
        return categoryIncome;
    }

    public LiveData<List<CategoryExpenseModel>> getCategoryIncomeTransaction() {
        return categoryIncomeTransaction;
    }

    public LiveData<List<CategoryExpenseModel>> getCategoryExpenseTransaction() {
        return categoryExpenseTransaction;
    }

    public LiveData<BreakdownFilter> getBreakdownFilter() {
        return breakdownFilter;
    }

    public void setBreakdownFilter(BreakdownFilter filter) {
        breakdownFilter.setValue(filter);
    }

    public void loadBreakdown(int accountId,
                              int transactionType,
                              CalendarFilterType filter,
                              Date date,
                              long startDate,
                              long endDate) {

        breakdownFilter.setValue(new BreakdownFilter(
                accountId,
                transactionType,
                filter,
                date,
                startDate,
                endDate));
    }

    public LiveData<List<BreakdownChartModel>> getBreakdownChart() {
        return breakdownChart;
    }
}