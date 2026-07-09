package com.nprotech.moneytracker.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.AccountCurrencyMappingEntity;

import java.util.List;

public class CurrencySpinnerAdapter extends ArrayAdapter<AccountCurrencyMappingEntity> {

    Context context;
    LayoutInflater inflater;
    List<AccountCurrencyMappingEntity> list;

    public CurrencySpinnerAdapter(Activity context, int resourceId, int textViewId, List<AccountCurrencyMappingEntity> list) {
        super(context, resourceId, textViewId, list);
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return rowView(convertView, position, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return rowView(convertView, position, parent);
    }

    private View rowView(View convertView, int position, ViewGroup parent) {
        View view;
        viewHolder viewholder;
        if (convertView == null) {
            viewholder = new viewHolder();
            LayoutInflater from = LayoutInflater.from(getContext());
            this.inflater = from;
            view = from.inflate(R.layout.item_list_drop_down, parent, false);
            viewholder.label = view.findViewById(R.id.label);
            view.setTag(viewholder);
        } else {
            view = convertView;
            viewholder = (viewHolder) convertView.getTag();
        }

        if (position == this.list.size() - 1) {
            viewholder.label.setText(list.get(position).currencyName);
            viewholder.label.setTextColor(ContextCompat.getColor(context, R.color.vibrant_orange));
        } else {
            viewholder.label.setTextColor(ContextCompat.getColor(context, R.color.black));
            viewholder.label.setText(getContext().getString(R.string.currency_display, list.get(position).currencyCode, list.get(position).currencyName));
        }
        return view;
    }

    public static class viewHolder {
        AppCompatTextView label;

        private viewHolder() {
        }
    }
}