package com.nprotech.moneytracker.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.utils.CommonUtils;

import java.util.List;

public class WalletsAdapter extends RecyclerViewAdapter<WalletEntity> {

    private OnAddWalletClickListener mOnAddWalletClickListener;
    private OnWalletClickListener mOnWalletClickListener;
    private final Context context;

    public WalletsAdapter(Context context, List<WalletEntity> list) {
        super(context, list, R.layout.item_wallet_list);
        setFooterLayout(R.layout.item_wallet_add);
        this.context = context;
    }

    @Override
    public void onPostBindViewHolder(ViewHolder holder, WalletEntity item) {

        ConstraintLayout walletWrapper = holder.itemView.findViewById(R.id.walletWrapper);
        AppCompatImageView walletImage = holder.itemView.findViewById(R.id.walletImage);
        AppCompatTextView walletAccountLabel = holder.itemView.findViewById(R.id.walletAccountLabel);
        AppCompatTextView walletTypeLabel = holder.itemView.findViewById(R.id.walletTypeLabel);
        AppCompatTextView walletAmountLabel = holder.itemView.findViewById(R.id.walletAmountLabel);

        int walletIcon = DataHelper.getWalletIcons().get(item.categoryIcon);

        walletWrapper.setBackground(CommonUtils.createGradient(context, item.walletColor, 8));

        walletImage.setBackground(CommonUtils.createIconBackground(context, item.walletColor, GradientDrawable.RECTANGLE, 10));
        walletImage.setImageResource(walletIcon);
        walletImage.setColorFilter(Color.WHITE);

        walletAccountLabel.setText(item.name);
        walletTypeLabel.setText(DataHelper.getWalletTypeName(context, item.walletType));
        walletAmountLabel.setText(CommonUtils.getBeautifyAmount(item.currencySymbol, item.amount));

        holder.itemView.setOnClickListener(v -> {
            if (mOnWalletClickListener != null) {
                mOnWalletClickListener.onWalletClick(item);
            }
        });
    }

    @Override
    public void onFooterBind(ViewHolder holder) {

        holder.itemView.setOnClickListener(v -> {
            if (mOnAddWalletClickListener != null) {
                mOnAddWalletClickListener.onAddWalletClick();
            }
        });
    }

    public interface OnAddWalletClickListener {
        void onAddWalletClick();
    }

    public void setOnAddWalletClickListener(OnAddWalletClickListener listener) {
        mOnAddWalletClickListener = listener;
    }

    public interface OnWalletClickListener {
        void onWalletClick(WalletEntity wallet);
    }

    public void setOnWalletClickListener(OnWalletClickListener listener) {
        this.mOnWalletClickListener = listener;
    }
}