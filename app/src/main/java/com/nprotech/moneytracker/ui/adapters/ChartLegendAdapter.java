package com.nprotech.moneytracker.ui.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.models.CategoryExpenseModel;

import java.util.ArrayList;
import java.util.Locale;

public class ChartLegendAdapter extends RecyclerViewAdapter<CategoryExpenseModel> {

    private final Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private final Typeface medium, semiBold;

    public ChartLegendAdapter(Context context) {
        super(context, new ArrayList<>(), R.layout.item_chart_legend);
        this.context = context;

        medium = ResourcesCompat.getFont(context, R.font.exo2_medium);
        semiBold = ResourcesCompat.getFont(context, R.font.exo2_semibold);
    }

    @Override
    public void onPostBindViewHolder(ViewHolder holder, CategoryExpenseModel categoryExpenseModel) {

        View color = holder.getView(R.id.pieStatView1);

        AppCompatTextView tvName = holder.getView(R.id.pieStatLabel1);
        AppCompatTextView tvPercent = holder.getView(R.id.pieStatPercentLabel1);

        try {
            color.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(categoryExpenseModel.color)));
        } catch (Exception e) {
            color.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        }

        String name = categoryExpenseModel.categoryName;

        if (categoryExpenseModel.defaultCategoryId > 0) {
            name = DataHelper.getDefaultCategory(context, categoryExpenseModel.defaultCategoryId);
        }

        tvName.setText(name);
        tvPercent.setText(String.format(Locale.getDefault(), "%.1f%%", categoryExpenseModel.percentage));

        if (holder.getAbsoluteAdapterPosition() == selectedPosition) {
            tvName.setTypeface(semiBold);
            tvPercent.setTypeface(semiBold);
        } else {
            tvName.setTypeface(medium);
            tvPercent.setTypeface(medium);
        }
    }

    public void setSelectedPosition(int position) {
        int old = selectedPosition;
        selectedPosition = position;

        if (old != RecyclerView.NO_POSITION) {
            notifyItemChanged(old);
        }

        if (selectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPosition);
        }
    }
}