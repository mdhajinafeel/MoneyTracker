package com.nprotech.moneytracker.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.nprotech.moneytracker.R;

import java.util.List;

public class FontSpinnerAdapter extends ArrayAdapter<String> {

    Context context;
    LayoutInflater inflater;
    List<String> items;

    public FontSpinnerAdapter(Context context, int resourceId, int textViewId, List<String> items) {
        super(context, resourceId, textViewId, items);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return rowView(parent, position);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return rowView(parent, position);
    }

    private View rowView(ViewGroup parent, int position) {
        View view;
        viewHolder viewholder;
        viewholder = new viewHolder();
        LayoutInflater from = LayoutInflater.from(parent.getContext());
        this.inflater = from;
        view = from.inflate(R.layout.item_list_drop_down, parent, false);
        viewholder.label = view.findViewById(R.id.label);
        view.setTag(viewholder);
        viewholder.label.setText(items.get(position));
        viewholder.label.setTextColor(ContextCompat.getColor(context, R.color.black));

        return view;
    }

    public static class viewHolder {
        AppCompatTextView label;

        private viewHolder() {
        }
    }
}