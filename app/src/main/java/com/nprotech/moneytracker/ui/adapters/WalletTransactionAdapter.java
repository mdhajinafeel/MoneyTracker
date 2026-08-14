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

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.models.TransactionCategoryModel;
import com.nprotech.moneytracker.ui.activities.CategoryTransactionActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;

import java.util.List;
import java.util.Objects;

public class WalletTransactionAdapter extends RecyclerViewAdapter<TransactionCategoryModel> {

    private final Context context;

    public WalletTransactionAdapter(Context context, List<TransactionCategoryModel> list) {
        super(context, list, R.layout.item_wallet_transaction);
        this.context = context;
    }

    @Override
    public void onPostBindViewHolder(ViewHolder holder, TransactionCategoryModel item) {

        ConstraintLayout itemView = holder.getView(R.id.itemView);
        ConstraintLayout colorView = holder.getView(R.id.colorView);
        AppCompatImageView imageView = holder.getView(R.id.imageView);
        AppCompatTextView amountLabel = holder.getView(R.id.amountLabel);

        if (item != null) {
            if (Build.VERSION.SDK_INT >= 29) {
                colorView.getBackground().setColorFilter(new BlendModeColorFilter(Color.parseColor(item.getColor()), BlendMode.SRC_OVER));
            } else {
                Drawable drawable = colorView.getBackground().mutate();
                DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_OVER);
                DrawableCompat.setTint(drawable, Color.parseColor(item.getColor()));
                colorView.setBackground(drawable);
            }

            if (item.getIcon() == 0) {
                imageView.setImageResource(R.drawable.category_0);
            } else {
                imageView.setImageResource(DataHelper.getCategoryIcons().get(item.getIcon()));
            }

            String categoryName = item.getCategory(context);
            if (Objects.equals(categoryName, "")) {
                categoryName = item.getCategoryName();
            }

            double amount = item.getAmount();
            int color = ContextCompat.getColor(context, R.color.income);
            if (item.getType() == TransactionEntity.TYPE_EXPENSE) {
                amount = amount * -1;
                color = ContextCompat.getColor(context, R.color.expense);
            }

            holder.setViewText(R.id.nameLabel, categoryName);
            int count = item.getTransactionCount();
            holder.setViewText(R.id.transLabel, context.getResources().getQuantityString(R.plurals.transaction_count, count, count));
            amountLabel.setText(CommonUtils.getBeautifyAmount(item.getCurrencySymbol(), amount));
            amountLabel.setTextColor(color);

            itemView.setOnClickListener(view -> {
                Intent intent = new Intent(context, CategoryTransactionActivity.class);
                intent.putExtra("type", item.getType());
                intent.putExtra("categoryId", item.getCategoryId());
                intent.putExtra("walletId", item.getWalletId());

                if (item.getCategoryName() != null && !item.getCategoryName().isEmpty()) {
                    intent.putExtra("categoryName", item.getCategoryName());
                } else {
                    intent.putExtra("categoryName", item.getCategory(context));
                }
                context.startActivity(intent);

                if (context instanceof Activity) {
                    ActivityUtils.overrideOpenTransition((Activity) context, R.anim.top_to_bottom, R.anim.scale_out);
                }
            });
        }
    }
}