package com.nprotech.moneytracker.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.CalendarHelper;
import com.nprotech.moneytracker.models.CalendarDayModel;
import com.nprotech.moneytracker.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class CalendarAdapter extends RecyclerViewAdapter<CalendarDayModel> {

    private int weekStartOn;

    public CalendarAdapter(Context context, int weekStartOn) {
        super(context, new ArrayList<>(), R.layout.item_calendar_day);
        this.weekStartOn = weekStartOn;
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

        if (!item.currentMonth) {
            holder.setViewTextColor(R.id.tvDay, Color.LTGRAY);
        } else {
            holder.setViewTextColor(R.id.tvDay, Color.BLACK);
        }
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

    @SuppressLint("NotifyDataSetChanged")
    public void refresh() {
        notifyDataSetChanged();
    }
}