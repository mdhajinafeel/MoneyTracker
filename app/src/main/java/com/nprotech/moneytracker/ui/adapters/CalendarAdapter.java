package com.nprotech.moneytracker.ui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.CalendarHelper;
import com.nprotech.moneytracker.models.CalendarDayModel;
import com.nprotech.moneytracker.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class CalendarAdapter extends RecyclerViewAdapter<CalendarDayModel> {

    private int weekStartOn;
    private final Context context;
    private final Typeface medium, semiBold;

    public interface OnDateClickListener {
        void onDateClick(CalendarDayModel day);
    }

    private OnDateClickListener onDateClickListener;

    public CalendarAdapter(Context context, int weekStartOn) {
        super(context, new ArrayList<>(), R.layout.item_calendar_day);
        this.weekStartOn = weekStartOn;
        this.context = context;

        medium = ResourcesCompat.getFont(context, R.font.exo2_medium);
        semiBold = ResourcesCompat.getFont(context, R.font.exo2_semibold);
    }

    @Override
    public void onPostBindViewHolder(ViewHolder holder, CalendarDayModel item) {

        holder.setViewText(R.id.tvDay, String.valueOf(item.day));

        if (item.hasTransaction) {
            holder.setViewVisibility(R.id.tvIncome, View.VISIBLE);
            holder.setViewVisibility(R.id.tvExpense, View.VISIBLE);
            holder.setViewVisibility(R.id.tvTotal, View.VISIBLE);

            holder.setViewText(R.id.tvIncome, "+" + CommonUtils.formatCompact(item.income));
            holder.setViewText(R.id.tvExpense, "-" + CommonUtils.formatCompact(item.expense));
            holder.setViewText(R.id.tvTotal, CommonUtils.formatCompact(item.total));
        }
        else {
            holder.setViewVisibility(R.id.tvIncome, View.GONE);
            holder.setViewVisibility(R.id.tvExpense, View.GONE);
            holder.setViewVisibility(R.id.tvTotal, View.GONE);
        }

        holder.setViewBackgroundResource(R.id.tvDay, android.R.color.transparent);

        // Highlight current date
        if (CalendarHelper.isSameDay(item.date, new java.util.Date())) {
            holder.setViewBackgroundResource(R.id.tvDay, R.color.blue_alpha);
            holder.setViewTextColor(R.id.tvDay, ContextCompat.getColor(context, R.color.midnight_blue));
            holder.setViewTypeface(R.id.tvDay, semiBold);
        } else {
            holder.setViewTypeface(R.id.tvDay, medium);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onDateClickListener != null) {
                onDateClickListener.onDateClick(item);
            }
        });
    }

    @Override
    public void onHeaderBind(ViewHolder holder) {
        List<String> days = CalendarHelper.getShortWeekDays(holder.itemView.getContext(), weekStartOn);
        holder.setViewText(R.id.tvDay1, days.get(0));
        holder.setViewText(R.id.tvDay2, days.get(1));
        holder.setViewText(R.id.tvDay3, days.get(2));
        holder.setViewText(R.id.tvDay4, days.get(3));
        holder.setViewText(R.id.tvDay5, days.get(4));
        holder.setViewText(R.id.tvDay6, days.get(5));
        holder.setViewText(R.id.tvDay7, days.get(6));
    }

    public void setWeekStartOn(int weekStartOn) {
        this.weekStartOn = weekStartOn;
        notifyItemChanged(0); // Refresh header
    }

    public void setOnDateClickListener(OnDateClickListener listener) {
        this.onDateClickListener = listener;
    }
}