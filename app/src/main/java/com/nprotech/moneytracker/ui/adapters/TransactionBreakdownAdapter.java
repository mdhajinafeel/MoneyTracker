package com.nprotech.moneytracker.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.models.CategoryExpenseModel;
import com.nprotech.moneytracker.utils.CommonUtils;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TransactionBreakdownAdapter extends RecyclerViewAdapter<CategoryExpenseModel> {

    private String currencySymbol;
    private final int transactionType;
    private final Context context;

    public TransactionBreakdownAdapter(Context context, List<CategoryExpenseModel> list, String currencySymbol, int transactionType) {
        super(context, list, R.layout.item_transaction_detail);
        this.currencySymbol = currencySymbol;
        this.context = context;
        this.transactionType = transactionType;
    }

    @Override
    public void onPostBindViewHolder(ViewHolder holder, CategoryExpenseModel item) {

        ConstraintLayout itemView = holder.getView(R.id.itemView);
        ConstraintLayout colorView = holder.getView(R.id.colorView);
        AppCompatImageView imageView = holder.getView(R.id.imageView);
        AppCompatTextView amountLabel = holder.getView(R.id.amountLabel);
        AppCompatTextView detailLabel = holder.getView(R.id.detailLabel);

        if (Build.VERSION.SDK_INT >= 29) {
            colorView.getBackground().setColorFilter(new BlendModeColorFilter(Color.parseColor(item.color), BlendMode.SRC_OVER));
        } else {
            Drawable drawable = colorView.getBackground().mutate();
            DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_OVER);
            DrawableCompat.setTint(drawable, Color.parseColor(item.color));
            colorView.setBackground(drawable);
        }

        if (item.icon == 0) {
            imageView.setImageResource(R.drawable.category_0);
        } else {
            imageView.setImageResource(DataHelper.getCategoryIcons().get(item.icon));
        }

        imageView.setImageTintList(
                ContextCompat.getColorStateList(context, android.R.color.white));

        String categoryName = item.getCategoryName(context);
        if (Objects.equals(categoryName, "")) {
            categoryName = item.categoryName;
        }

        int color = ContextCompat.getColor(context, R.color.income);
        if (transactionType == TransactionEntity.TYPE_EXPENSE) {
            color = ContextCompat.getColor(context, R.color.expense);
        }

        holder.setViewText(R.id.nameLabel, categoryName);

        holder.setViewText(R.id.timeLabel, context.getResources().getQuantityString(R.plurals.transaction_count, item.transactionCount, item.transactionCount));
        amountLabel.setText(CommonUtils.getBeautifyAmount(currencySymbol, item.amount));
        amountLabel.setTextColor(color);

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMinimumFractionDigits(1);
        numberFormat.setMaximumFractionDigits(1);

        String detail = numberFormat.format(getPercentage(item)) + "%";
        detailLabel.setText(detail);

        itemView.setOnClickListener(view -> {

        });
    }

    private double getPercentage(CategoryExpenseModel item) {

        double total = 0;

        for (CategoryExpenseModel model : getItems()) {
            total += Math.abs(model.amount);
        }

        if (total == 0) {
            return 0;
        }

        return (Math.abs(item.amount) * 100.0) / total;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
        notifyDataSetChanged();
    }
}