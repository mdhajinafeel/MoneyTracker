package com.nprotech.moneytracker.ui.adapters;

import android.content.Context;
import android.text.format.DateFormat;

import androidx.appcompat.widget.AppCompatTextView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.models.DailyTransModel;
import com.nprotech.moneytracker.utils.CommonUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyTransactionAdapter extends RecyclerViewAdapter<DailyTransModel> {

    public DailyTransactionAdapter(Context context, List<DailyTransModel> list) {
        super(context, list, R.layout.item_transaction_header);
    }

    @Override
    public void onPostBindViewHolder(ViewHolder holder, DailyTransModel item) {

        AppCompatTextView dayLabel = holder.itemView.findViewById(R.id.dayLabel);
        AppCompatTextView monthLabel = holder.itemView.findViewById(R.id.monthLabel);
        AppCompatTextView weekLabel = holder.itemView.findViewById(R.id.weekLabel);
        AppCompatTextView amountLabel = holder.itemView.findViewById(R.id.amountLabel);

        Date dailyTransDateTime = item.getDateTime();

        dayLabel.setText(new SimpleDateFormat("dd", Locale.getDefault()).format(dailyTransDateTime));
        monthLabel.setText(new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMM yyyy"), Locale.getDefault()).format(dailyTransDateTime));
        weekLabel.setText(new SimpleDateFormat("EEEE", Locale.getDefault()).format(dailyTransDateTime));
        amountLabel.setText(CommonUtils.getBeautifyAmount(item.getCurrencySymbol(), item.getAmount() * -1));
    }
}