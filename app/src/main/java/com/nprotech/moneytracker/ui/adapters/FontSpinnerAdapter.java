package com.nprotech.moneytracker.ui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.nprotech.moneytracker.R;

import java.util.List;

public class FontSpinnerAdapter extends ArrayAdapter<String> {

    private final Typeface typeface;

    public FontSpinnerAdapter(Context context, List<String> items) {
        super(context, android.R.layout.simple_spinner_item, items);

        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeface = ResourcesCompat.getFont(context, R.font.exo2_medium);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = view.findViewById(android.R.id.text1);
        textView.setTypeface(typeface);
        textView.setTextSize(16);
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView textView = view.findViewById(android.R.id.text1);
        textView.setTypeface(typeface);
        textView.setTextSize(16);
        return view;
    }
}