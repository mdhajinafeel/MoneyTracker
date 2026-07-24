package com.nprotech.moneytracker.utils;

import android.content.Context;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.nprotech.moneytracker.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChartMarkerView extends MarkerView {

    private final AppCompatTextView tvDate, tvTime, tvAmount;
    private final List<Long> periods;
    private final String filter;
    private final String currencySymbol;

    public ChartMarkerView(Context context, List<Long> periods, String filter, String currencySymbol) {
        super(context, R.layout.chart_marker_layout);

        this.periods = periods;
        this.filter = filter;
        this.currencySymbol = currencySymbol;

        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvAmount = findViewById(R.id.tvAmount);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        int index = Math.round(e.getX());

        if (index >= 0 && index < periods.size()) {

            long value = periods.get(index);

            switch (filter) {

                case "DAILY":

                    tvDate.setText(
                            new SimpleDateFormat(
                                    "dd MMM yyyy",
                                    Locale.getDefault()
                            ).format(new Date(value))
                    );

                    tvTime.setVisibility(View.VISIBLE);

                    tvTime.setText(
                            new SimpleDateFormat(
                                    "hh:mm a",
                                    Locale.getDefault()
                            ).format(new Date(value))
                    );
                    break;

                case "WEEKLY":
                case "MONTHLY":
                case "CUSTOM":

                    tvDate.setText(
                            new SimpleDateFormat(
                                    "dd MMM yyyy",
                                    Locale.getDefault()
                            ).format(new Date(value))
                    );

                    tvTime.setVisibility(View.GONE);
                    break;

                case "QUARTERLY":
                case "YEARLY":

                    tvDate.setText(
                            new SimpleDateFormat(
                                    "MMM yyyy",
                                    Locale.getDefault()
                            ).format(new Date(value))
                    );

                    tvTime.setVisibility(View.GONE);
                    break;

                case "ALL":

                    tvDate.setText(String.valueOf((int) value));
                    tvTime.setVisibility(View.GONE);
                    break;

                default:

                    tvDate.setText("");
                    tvTime.setVisibility(View.GONE);
                    break;
            }
        }

        tvAmount.setText(CommonUtils.getBeautifyAmount(currencySymbol, e.getY()));

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {

        return new MPPointF(
                -(getWidth() / 2f),
                -getHeight() - 12f
        );
    }

    @Override
    public MPPointF getOffsetForDrawingAtPoint(float posX, float posY) {

        float xOffset = -(getWidth() / 2f);
        float yOffset = -getHeight() - 12f;

        Chart<?> chart = getChartView();

        if (chart != null) {

            // Left edge
            if (posX + xOffset < 0) {
                xOffset = -posX;
            }

            // Right edge
            if (posX + getWidth() + xOffset > chart.getWidth()) {
                xOffset = chart.getWidth() - posX - getWidth();
            }

            // Top edge
            if (posY + yOffset < 0) {
                yOffset = 10f;
            }

            // Bottom edge
            if (posY + getHeight() > chart.getHeight()) {
                yOffset = -getHeight() - 12f;
            }
        }

        return new MPPointF(xOffset, yOffset);
    }
}