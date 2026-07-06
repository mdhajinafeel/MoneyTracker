package com.nprotech.moneytracker.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.models.TransactionWithDetails;
import com.nprotech.moneytracker.ui.activities.TransactionDetailActivity;
import com.nprotech.moneytracker.utils.CommonUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TransactionAdapter extends RecyclerViewAdapter<TransactionWithDetails> {

    private final String currencySymbol;
    private final Context context;

    public TransactionAdapter(Context context, List<TransactionWithDetails> list, String currencySymbol) {
        super(context, list, R.layout.item_transaction_detail);
        this.currencySymbol = currencySymbol;
        this.context = context;
    }

    @Override
    public void onPostBindViewHolder(ViewHolder holder, TransactionWithDetails item) {

        TransactionEntity transaction = item.transaction;
        ConstraintLayout itemView = holder.getView(R.id.itemView);
        ConstraintLayout colorView = holder.getView(R.id.colorView);
        AppCompatImageView imageView = holder.getView(R.id.imageView);
        AppCompatTextView amountLabel = holder.getView(R.id.amountLabel);

        if (Build.VERSION.SDK_INT >= 29) {
            colorView.getBackground().setColorFilter(new BlendModeColorFilter(Color.parseColor(item.color), BlendMode.SRC_OVER));
        } else {
            colorView.getBackground().setColorFilter(Color.parseColor(item.color), PorterDuff.Mode.SRC_OVER);
        }

        if (transaction.type == 3) {
            imageView.setImageResource(R.drawable.ic_transfer);
        } else {
            if (item.icon == null || item.icon == 0) {
                imageView.setImageResource(R.drawable.category_0);
            } else {
                imageView.setImageResource(DataHelper.getCategoryIcons().get(item.icon));
            }
        }

        String categoryName = transaction.getCategoryName(context);
        if (Objects.equals(categoryName, "")) {
            categoryName = item.categoryName;
        }

        double amount;
        if (transaction.type == 1) {
            amount = transaction.amount;
        } else {
            amount = transaction.amount * -1;
        }

        holder.setViewText(R.id.nameLabel, categoryName);
        holder.setViewText(R.id.detailLabel, transaction.description == null || transaction.description.isEmpty() ? "---" : transaction.description);
        holder.setViewText(R.id.timeLabel, new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(transaction.transactionDate)));
        amountLabel.setText(CommonUtils.getBeautifyAmount(currencySymbol, amount));
        amountLabel.setTextColor(ContextCompat.getColor(context, R.color.bright_red));

        itemView.setOnClickListener(view -> context.startActivity(new Intent(context, TransactionDetailActivity.class)
                .putExtra("transactionDetail", item)));
    }
}