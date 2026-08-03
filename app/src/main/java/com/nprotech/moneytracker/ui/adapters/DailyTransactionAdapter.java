package com.nprotech.moneytracker.ui.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
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
    private final Context context;

    private final RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

    public DailyTransactionAdapter(Context context, List<DailyTransModel> list, String accountCurrencySymbol) {
        super(context, list, R.layout.item_transaction_header);
        this.accountCurrencySymbol = accountCurrencySymbol;
        this.context = context;
    }

    @Override
    public void onPostBindViewHolder(ViewHolder holder, DailyTransModel item) {

        AppCompatTextView dayLabel = holder.itemView.findViewById(R.id.tvDate);
        AppCompatTextView tvMonth = holder.itemView.findViewById(R.id.tvMonth);
        AppCompatTextView tvDay = holder.itemView.findViewById(R.id.tvDay);
        AppCompatTextView tvTransactionCount = holder.itemView.findViewById(R.id.tvTransactionCount);
        AppCompatTextView tvAmount = holder.itemView.findViewById(R.id.tvAmount);
        AppCompatImageView ivCollapse = holder.itemView.findViewById(R.id.ivCollapse);

        View divider = holder.getView(R.id.divider);
        View header = holder.getView(R.id.layoutHeader);
        RecyclerView rvTransactions = holder.getView(R.id.rvDetailedTransactions);

        Date dailyTransDateTime = item.getDateTime();

        dayLabel.setText(new SimpleDateFormat("dd", Locale.getDefault()).format(dailyTransDateTime));
        tvDay.setText(new SimpleDateFormat("EEEE", Locale.getDefault()).format(dailyTransDateTime));
        tvMonth.setText(new SimpleDateFormat(
                DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMM yyyy"),
                Locale.getDefault()).format(dailyTransDateTime));


        int totalColor = ContextCompat.getColor(context, R.color.expense);
        String totalAmount = CommonUtils.getBeautifyAmount(getAccountCurrencySymbol(), item.getAmount());
        if(item.getAmount() > 0) {
            totalAmount = "+" + totalAmount;
            totalColor = ContextCompat.getColor(context, R.color.income);
        } else if(item.getAmount() == 0) {
            totalColor = ContextCompat.getColor(context, R.color.transfer);
        }

        tvAmount.setText(totalAmount);
        tvAmount.setTextColor(totalColor);

        if (item.getTransactions() == null || item.getTransactions().isEmpty()) {
            rvTransactions.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
            ivCollapse.setVisibility(View.GONE);
            return;
        }

        ivCollapse.setVisibility(View.VISIBLE);

        // Create LayoutManager only once
        if (rvTransactions.getLayoutManager() == null) {
            rvTransactions.setLayoutManager(new LinearLayoutManager(context));
            rvTransactions.setNestedScrollingEnabled(false);
            rvTransactions.setHasFixedSize(true);
            rvTransactions.setRecycledViewPool(viewPool);
        }

        // Reuse adapter
        TransactionAdapter adapter = (TransactionAdapter) rvTransactions.getAdapter();

        if (adapter == null) {
            adapter = new TransactionAdapter(context, item.getTransactions(), R.layout.item_transaction_detail, true);
            rvTransactions.setAdapter(adapter);
        } else {
            adapter.setItems(item.getTransactions());
        }

        tvTransactionCount.setText(context.getResources().getQuantityString(R.plurals.transaction_count, item.getTransactions().size(), item.getTransactions().size()));

        // Restore expanded state
        rvTransactions.setVisibility(item.isExpanded() ? View.VISIBLE : View.GONE);
        divider.setVisibility(item.isExpanded() ? View.VISIBLE : View.GONE);
        ivCollapse.setRotation(item.isExpanded() ? 180f : 0f);

        header.setOnClickListener(view -> ivCollapse.performClick());

        ivCollapse.setOnClickListener(view -> {

            boolean expanded = rvTransactions.getVisibility() == View.VISIBLE;

            if (expanded) {

                rvTransactions.setVisibility(View.GONE);
                divider.setVisibility(View.GONE);

                ivCollapse.animate()
                        .rotation(0f)
                        .setDuration(150)
                        .start();

            } else {

                divider.setVisibility(View.VISIBLE);
                rvTransactions.setVisibility(View.VISIBLE);

                ivCollapse.animate()
                        .rotation(180f)
                        .setDuration(150)
                        .start();
            }
        });
    }

    public void setAccountCurrencySymbol(String accountCurrencySymbol) {
        this.accountCurrencySymbol = accountCurrencySymbol;
    }

    public String getAccountCurrencySymbol() {
        return accountCurrencySymbol;
    }
}