package com.nprotech.moneytracker.ui.adapters;

import android.content.Context;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.utils.CommonUtils;

import java.util.List;

public class WalletsAdapter extends RecyclerViewAdapter<WalletEntity> {

    private static final int TYPE_WALLET = 0;
    private static final int TYPE_ADD = 1;
    private OnAddWalletClickListener mOnAddWalletClickListener;

    public WalletsAdapter(Context context, List<WalletEntity> list) {
        super(context, list, R.layout.item_wallet_list);
        setFooterLayout(R.layout.item_wallet_add);
    }

    @Override
    public void onPostBindViewHolder(ViewHolder holder, WalletEntity item) {

        ConstraintLayout walletWrapper = holder.itemView.findViewById(R.id.walletWrapper);
        AppCompatImageView walletImage = holder.itemView.findViewById(R.id.walletImage);
        AppCompatTextView walletAccountLabel = holder.itemView.findViewById(R.id.walletAccountLabel);
        AppCompatTextView walletAmountLabel = holder.itemView.findViewById(R.id.walletAmountLabel);

        walletAccountLabel.setText(item.name);
        walletAmountLabel.setText(CommonUtils.getBeautifyAmount(item.currencySymbol, item.initialAmount));
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
}