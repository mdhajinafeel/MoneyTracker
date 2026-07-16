package com.nprotech.moneytracker.ui.adapters;

import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.os.ConfigurationCompat;
import androidx.core.text.TextUtilsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.CommonDataEntity;

import java.util.List;
import java.util.Locale;

public class SettingOptionsAdapter extends RecyclerView.Adapter<SettingOptionsAdapter.SettingOptionViewHolder> {

    private final List<CommonDataEntity> list;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public SettingOptionsAdapter(List<CommonDataEntity> list) {
        this.list = list;

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).selected) {
                selectedPosition = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public SettingOptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_setting_options, parent, false);
        return new SettingOptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingOptionViewHolder holder, int position) {

        CommonDataEntity item = list.get(position);

        holder.rbOption.setText(item.nameResId);
        holder.rbOption.setChecked(position == selectedPosition);

        Configuration configuration = holder.itemView.getResources().getConfiguration();
        Locale locale = ConfigurationCompat.getLocales(configuration).get(0);

        if (locale == null) {
            locale = Locale.getDefault();
        }

        int layoutDirection = TextUtilsCompat.getLayoutDirectionFromLocale(locale);
        holder.rbOption.setLayoutDirection(layoutDirection);

        View.OnClickListener listener = v -> {

            int adapterPosition = holder.getBindingAdapterPosition();

            if (adapterPosition == RecyclerView.NO_POSITION) {
                return;
            }

            if (selectedPosition == adapterPosition) {
                return;
            }

            int previousPosition = selectedPosition;
            selectedPosition = adapterPosition;

            if (previousPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousPosition);
            }

            notifyItemChanged(selectedPosition);
        };

        holder.itemView.setOnClickListener(listener);
        holder.rbOption.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public CommonDataEntity getSelectedItem() {
        if (selectedPosition == RecyclerView.NO_POSITION) {
            return null;
        }
        return list.get(selectedPosition);
    }

    public static class SettingOptionViewHolder extends RecyclerView.ViewHolder {

        AppCompatRadioButton rbOption;

        public SettingOptionViewHolder(@NonNull View itemView) {
            super(itemView);
            rbOption = itemView.findViewById(R.id.rbOption);
        }
    }
}