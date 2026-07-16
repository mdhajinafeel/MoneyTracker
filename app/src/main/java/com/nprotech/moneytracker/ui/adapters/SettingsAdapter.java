package com.nprotech.moneytracker.ui.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
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

    public SettingsAdapter(List<SettingItemModel> items, OnSettingActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_setting, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SettingItemModel item = items.get(position);
        holder.title.setText(item.title);

        holder.ivNavigation.setVisibility(item.navigationVisible ? View.VISIBLE : View.GONE);
        holder.ivPremium.setVisibility(item.isPremium ? View.VISIBLE : View.GONE);
        holder.subTitle.setVisibility(item.enabledSubTitle ? View.VISIBLE : View.GONE);

        holder.subTitle.setText(item.enabledSubTitle ? item.subTitle : null);

        if (item.isEnabled) {
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSettingClick(item);
                }
            });
        } else {
            holder.itemView.setOnClickListener(null);
        }

        holder.divider.setVisibility(position == items.size() - 1 ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ✅ Make ViewHolder public static
    public static class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatTextView title, subTitle;
        AppCompatImageView ivNavigation, ivPremium;
        View divider;

        public ViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            subTitle = itemView.findViewById(R.id.subtitle);
            ivNavigation = itemView.findViewById(R.id.ivNavigation);
            ivPremium = itemView.findViewById(R.id.ivPremium);
            divider = itemView.findViewById(R.id.divider);
        }
    }
}