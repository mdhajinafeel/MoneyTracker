package com.nprotech.moneytracker.ui.adapters;

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
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.models.TransactionWithDetails;
import com.nprotech.moneytracker.ui.activities.TransactionDetailActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TransactionAdapter extends RecyclerViewAdapter<TransactionWithDetails> {

    private final Context context;

    public TransactionAdapter(Context context, List<TransactionWithDetails> list, int layoutId) {
        super(context, list, layoutId);
        this.context = context;
    }

    @Override
    public void onPostBindViewHolder(ViewHolder holder, TransactionWithDetails item) {

        TransactionEntity transaction = item.transaction;
        TransactionEntity feeTransaction = item.feeTransaction;

        ConstraintLayout itemView = holder.getView(R.id.itemView);
        ConstraintLayout colorView = holder.getView(R.id.colorView);
        AppCompatImageView imageView = holder.getView(R.id.imageView);
        AppCompatTextView amountLabel = holder.getView(R.id.amountLabel);
        AppCompatTextView feeAmountLabel = holder.getView(R.id.feeAmountLabel);
        AppCompatTextView feeLabel = holder.getView(R.id.feeLabel);
        AppCompatTextView tvBadgeDetail = holder.getView(R.id.tvBadgeDetail);
        AppCompatTextView tvBadgeFee = holder.getView(R.id.tvBadgeFee);

        if (Build.VERSION.SDK_INT >= 29) {
            colorView.getBackground().setColorFilter(new BlendModeColorFilter(Color.parseColor(item.color), BlendMode.SRC_OVER));
        } else {
            Drawable drawable = colorView.getBackground().mutate();
            DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_OVER);
            DrawableCompat.setTint(drawable, Color.parseColor(item.color));
            colorView.setBackground(drawable);
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

        imageView.setImageTintList(ContextCompat.getColorStateList(context, android.R.color.white));

        String categoryName = transaction.getCategoryName(context);
        if (Objects.equals(categoryName, "")) {
            categoryName = item.categoryName;
        }

        double amount;
        int color = ContextCompat.getColor(context, R.color.income);
        Drawable badge = ContextCompat.getDrawable(context, R.drawable.bg_badge_income);
        String badgeText = context.getString(R.string.income);
        if (transaction.type == TransactionEntity.TYPE_INCOME) {
            amount = transaction.amount;
        } else if (transaction.type == TransactionEntity.TYPE_TRANSFER) {
            amount = transaction.amount;
            color = ContextCompat.getColor(context, R.color.transfer);
            badge = ContextCompat.getDrawable(context, R.drawable.bg_badge_transfer);
            badgeText = context.getString(R.string.transfer);
        } else {
            amount = transaction.amount * -1;
            color = ContextCompat.getColor(context, R.color.expense);
            badge = ContextCompat.getDrawable(context, R.drawable.bg_badge_expense);
            badgeText = context.getString(R.string.expense);
        }

        holder.setViewText(R.id.nameLabel, categoryName);

        if (item.fromWalletName != null && !item.fromWalletName.isEmpty()) {
            holder.setViewText(R.id.detailLabel, item.fromWalletName + context.getString(R.string.text_arrow) + item.walletName);

            tvBadgeDetail.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_badge_transfer));
            tvBadgeDetail.setText(context.getString(R.string.transfer));
            tvBadgeDetail.setTextColor(ContextCompat.getColor(context, R.color.transfer));
        } else {
            holder.setViewText(R.id.detailLabel, transaction.description == null || transaction.description.isEmpty() ? "---" : transaction.description);

            tvBadgeDetail.setBackgroundDrawable(badge);
            tvBadgeDetail.setText(badgeText);
            tvBadgeDetail.setTextColor(color);
        }

        if (feeTransaction != null) {
            feeTransaction.amount = feeTransaction.amount * -1;
            feeLabel.setText(feeTransaction.getCategoryName(context));
            feeAmountLabel.setText(CommonUtils.getBeautifyAmount(item.currencySymbol, feeTransaction.amount));
            feeAmountLabel.setTextColor(ContextCompat.getColor(context, R.color.expense));

            tvBadgeFee.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_badge_expense));
            tvBadgeFee.setText(context.getString(R.string.expense));
            tvBadgeFee.setTextColor(ContextCompat.getColor(context, R.color.expense));

            tvBadgeFee.setVisibility(View.VISIBLE);
            feeLabel.setVisibility(View.VISIBLE);
            feeAmountLabel.setVisibility(View.VISIBLE);
        } else {

            tvBadgeFee.setVisibility(View.GONE);
            feeLabel.setVisibility(View.GONE);
            feeAmountLabel.setVisibility(View.GONE);
        }

        holder.setViewText(R.id.timeLabel, new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(transaction.transactionDate)));
        amountLabel.setText(CommonUtils.getBeautifyAmount(item.currencySymbol, amount));
        amountLabel.setTextColor(color);

        itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, TransactionDetailActivity.class);
            intent.putExtra("transactionDetail", item);
            context.startActivity(intent);

            if (context instanceof Activity) {
                ActivityUtils.overrideOpenTransition((Activity) context, R.anim.top_to_bottom, R.anim.scale_out);
            }
        });
    }
}