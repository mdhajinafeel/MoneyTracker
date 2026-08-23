package com.nprotech.moneytracker.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.models.SettingItemModel;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    public interface OnSettingActionListener {

        void onSettingClick(SettingItemModel item);
    }

    private final List<SettingItemModel> items;
    private final OnSettingActionListener listener;
    private final Context context;
    private final int layout;

    public SettingsAdapter(Context context, List<SettingItemModel> items, int layout, OnSettingActionListener listener) {
        this.items = items;
        this.listener = listener;
        this.context = context;
        this.layout = layout;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SettingItemModel item = items.get(position);
        holder.tvSettingName.setText(item.title);

        holder.ivNavigation.setVisibility(item.navigationVisible ? View.VISIBLE : View.GONE);
        holder.ivPremium.setVisibility(item.isPremium ? View.VISIBLE : View.GONE);

        holder.tvSettingDesc.setVisibility(item.enabledSubTitle ? View.VISIBLE : View.GONE);
        holder.tvSettingDesc.setText(item.enabledSubTitle ? item.subTitle : null);

        holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, item.icon));
        holder.imageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, item.fgColor)));

        holder.colorView.setBackgroundTintList(
                ColorStateList.valueOf(
                        ContextCompat.getColor(context, item.bgColor)
                )
        );

        if (item.isEnabled) {
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSettingClick(item);
                }
            });
        } else {
            holder.itemView.setOnClickListener(null);
        }

        if(position == getItemCount() - 1) {
            holder.divider.setAlpha(0f);
        } else {
            holder.divider.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ✅ Make ViewHolder public static
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout colorView;
        AppCompatTextView tvSettingName, tvSettingDesc;
        AppCompatImageView ivNavigation, ivPremium, imageView;
        View divider;

        public ViewHolder(View itemView) {
            super(itemView);

            colorView = itemView.findViewById(R.id.colorView);
            tvSettingName = itemView.findViewById(R.id.tvSettingName);
            tvSettingDesc = itemView.findViewById(R.id.tvSettingDesc);
            ivNavigation = itemView.findViewById(R.id.ivNavigation);
            imageView = itemView.findViewById(R.id.imageView);
            ivPremium = itemView.findViewById(R.id.ivPremium);
            divider = itemView.findViewById(R.id.divider);
        }
    }
}