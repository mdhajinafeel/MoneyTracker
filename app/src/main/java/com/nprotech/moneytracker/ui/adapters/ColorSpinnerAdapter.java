package com.nprotech.moneytracker.ui.adapters;

import android.app.Activity;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.drawable.DrawableCompat;

import com.nprotech.moneytracker.R;

import java.util.List;

public class ColorSpinnerAdapter extends ArrayAdapter<String> {
    List<String> list;

    public ColorSpinnerAdapter(Activity context, int resourceId, int textViewId, List<String> list) {
        super(context, resourceId, textViewId, list);
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

            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_drop_down_color, parent, false);

            viewholder.label = view.findViewById(R.id.label);
            viewholder.colorView = view.findViewById(R.id.colorView);

            view.setTag(viewholder);
        } else {
            view = convertView;
            viewholder = (viewHolder) view.getTag();
        }

        viewholder.label.setText(list.get(position));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            viewholder.colorView.getBackground().setColorFilter(new BlendModeColorFilter(Color.parseColor(list.get(position)), BlendMode.SRC_OVER));
        } else {
            Drawable drawable = viewholder.colorView.getBackground().mutate();
            DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_OVER);
            DrawableCompat.setTint(drawable, Color.parseColor(list.get(position)));
            viewholder.colorView.setBackground(drawable);
        }
        return view;
    }

    public static class viewHolder {
        View colorView;
        AppCompatTextView label;

        private viewHolder() {
        }
    }
}