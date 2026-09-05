package com.nprotech.moneytracker.ui.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.Constants;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.models.CategoryExpenseModel;
import com.nprotech.moneytracker.ui.activities.CategoryTransactionActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
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
        super(context, list, R.layout.item_transaction_period_detail);
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
        AppCompatTextView timeLabel = holder.getView(R.id.timeLabel);
        AppCompatTextView tvBadgeDetail = holder.getView(R.id.tvBadgeDetail);
        View divider = holder.getView(R.id.divider);

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

        double amount;
        int color = ContextCompat.getColor(context, R.color.income);
        Drawable badge = ContextCompat.getDrawable(context, R.drawable.bg_badge_income);
        String badgeText = context.getString(R.string.income);
        if (transactionType == TransactionEntity.TYPE_INCOME) {
            amount = item.amount;
        } else if (transactionType == TransactionEntity.TYPE_TRANSFER) {
            amount = item.amount;
            color = ContextCompat.getColor(context, R.color.transfer);
            badge = ContextCompat.getDrawable(context, R.drawable.bg_badge_transfer);
            badgeText = context.getString(R.string.transfer);
        } else {
            amount = item.amount * -1;
            color = ContextCompat.getColor(context, R.color.expense);
            badge = ContextCompat.getDrawable(context, R.drawable.bg_badge_expense);
            badgeText = context.getString(R.string.expense);
        }
        holder.setViewText(R.id.nameLabel, categoryName);

        tvBadgeDetail.setBackgroundDrawable(badge);
        tvBadgeDetail.setText(badgeText);
        tvBadgeDetail.setTextColor(color);

        timeLabel.setText(context.getResources().getQuantityString(R.plurals.transaction_count, item.transactionCount, item.transactionCount));
        amountLabel.setText(CommonUtils.getBeautifyAmount(currencySymbol, amount));
        amountLabel.setTextColor(color);

        timeLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0);

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMinimumFractionDigits(1);
        numberFormat.setMaximumFractionDigits(1);

        String detail = numberFormat.format(getPercentage(item)) + "%";
        detailLabel.setText(detail);

        itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, CategoryTransactionActivity.class);

            String catName;
            if(item.isFee) {
                intent.putExtra("type", TransactionEntity.TYPE_TRANSFER);
                intent.putExtra("categoryId", Constants.DEFAULT_CATEGORY_TRANSFER_ID);
                catName = context.getString(R.string.transfer);
            } else {
                intent.putExtra("type", item.type);
                intent.putExtra("categoryId", item.categoryId);

                catName = item.getCategoryName(context);
                if (Objects.equals(catName, "")) {
                    catName = item.categoryName;
                }
            }

            intent.putExtra("categoryName", catName);
            intent.putExtra("walletId", item.walletId);
            context.startActivity(intent);

            if (context instanceof Activity) {
                ActivityUtils.overrideOpenTransition((Activity) context, R.anim.top_to_bottom, R.anim.scale_out);
            }
        });

        divider.setVisibility(View.GONE);
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