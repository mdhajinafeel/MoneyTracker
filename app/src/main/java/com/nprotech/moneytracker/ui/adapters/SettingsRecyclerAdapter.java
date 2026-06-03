package com.nprotech.moneytracker.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.github.angads25.toggle.widget.LabeledSwitch;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.SettingItemModel;

import java.util.List;

public class SettingsRecyclerAdapter extends RecyclerView.Adapter<SettingsRecyclerAdapter.ViewHolder> {

    public interface OnSettingActionListener {
        void onSwitchToggle(SettingItemModel item, boolean isChecked, LabeledSwitch switchButton);

        void onSettingClick(SettingItemModel item);
    }

    private final List<SettingItemModel> items;
    private final Context context;
    private final OnSettingActionListener listener;

    public SettingsRecyclerAdapter(Context context, List<SettingItemModel> items, OnSettingActionListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_setting, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SettingItemModel item = items.get(position);
        holder.title.setText(item.title);

        if (item.switchVisible) {
            holder.switchButton.setVisibility(View.VISIBLE);
            holder.switchButton.setOnToggledListener(null);
            holder.ivNavigation.setVisibility(View.GONE);

            if (item.settingId == 1)
                holder.switchButton.setOn(PreferenceManager.INSTANCE.getDarkMode());

            holder.switchButton.setOnToggledListener((buttonView, isChecked) -> {
                if (listener != null) listener.onSwitchToggle(item, isChecked, holder.switchButton);
            });

        } else {
            holder.switchButton.setVisibility(View.GONE);
            holder.ivNavigation.setVisibility(View.VISIBLE);

            if(item.isEnabled) {
                holder.itemView.setOnClickListener(view -> {
                    if (listener != null) {
                        listener.onSettingClick(item);
                    }
                });
            }
        }

        if(item.enabledSubTitle) {
            holder.subTitle.setVisibility(View.VISIBLE);
            holder.subTitle.setText(item.subTitle);
        } else {
            holder.subTitle.setVisibility(View.GONE);
            holder.subTitle.setText(null);
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
        LabeledSwitch switchButton;
        AppCompatImageView ivNavigation;
        View divider;

        public ViewHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            subTitle = itemView.findViewById(R.id.subtitle);
            switchButton = itemView.findViewById(R.id.toggleSwitch);
            ivNavigation = itemView.findViewById(R.id.ivNavigation);
            divider = itemView.findViewById(R.id.divider);
        }
    }
}