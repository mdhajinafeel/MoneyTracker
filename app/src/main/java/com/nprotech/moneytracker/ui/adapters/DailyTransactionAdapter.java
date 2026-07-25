package com.nprotech.moneytracker.ui.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.models.DailyTransModel;
import com.nprotech.moneytracker.utils.CommonUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyTransactionAdapter extends RecyclerViewAdapter<DailyTransModel> {

    private String accountCurrencySymbol;

    public DailyTransactionAdapter(Context context, List<DailyTransModel> list, String accountCurrencySymbol) {
        super(context, list, R.layout.item_transaction_header);
        this.accountCurrencySymbol = accountCurrencySymbol;

    }

    @Override
    public void onPostBindViewHolder(ViewHolder holder, DailyTransModel item) {

        AppCompatTextView dayLabel = holder.itemView.findViewById(R.id.dayLabel);
        AppCompatTextView monthLabel = holder.itemView.findViewById(R.id.monthLabel);
        AppCompatTextView weekLabel = holder.itemView.findViewById(R.id.weekLabel);
        AppCompatTextView amountLabel = holder.itemView.findViewById(R.id.amountLabel);
        RecyclerView rvTransactions = holder.getView(R.id.rvDetailedTransactions);

        Date dailyTransDateTime = item.getDateTime();

        double amount = item.getAmount();

        dayLabel.setText(new SimpleDateFormat("dd", Locale.getDefault()).format(dailyTransDateTime));
        monthLabel.setText(new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMM yyyy"), Locale.getDefault()).format(dailyTransDateTime));
        weekLabel.setText(new SimpleDateFormat("EEEE", Locale.getDefault()).format(dailyTransDateTime));
        amountLabel.setText(CommonUtils.getBeautifyAmount(getAccountCurrencySymbol(), amount));

        if (item.getTransactions() == null || item.getTransactions().isEmpty()) {
            rvTransactions.setVisibility(View.GONE);
            return;
        }

        rvTransactions.setVisibility(View.VISIBLE);
        rvTransactions.setLayoutManager(new LinearLayoutManager(rvTransactions.getContext()));
        rvTransactions.setNestedScrollingEnabled(false);
        rvTransactions.setAdapter(new TransactionAdapter(rvTransactions.getContext(), item.getTransactions(), item.getCurrencySymbol()));
    }

    public void setAccountCurrencySymbol(String accountCurrencySymbol) {
        this.accountCurrencySymbol = accountCurrencySymbol;
    }

    public String getAccountCurrencySymbol() {
        return accountCurrencySymbol;
    }
}