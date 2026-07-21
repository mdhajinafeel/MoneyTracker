package com.nprotech.moneytracker.utils;

import android.content.Context;
import android.graphics.Canvas;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet;
import com.github.mikephil.charting.renderer.PieChartRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.DataHelper;

import java.util.List;

public class CustomPieChartRenderer extends PieChartRenderer {

    private static final float DEG_TO_RAD = (float) (Math.PI / 180f);
    private static final float DOT_RADIUS_DP = 3f;

    private final PieChart pieChart;
    private final Context context;
    private final ChartAnimator animator;

    public CustomPieChartRenderer(Context context, PieChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(chart, animator, viewPortHandler);
        this.context = context;
        this.pieChart = chart;
        this.animator = animator;
    }

    @Override
    public void drawValues(Canvas canvas) {

        super.drawValues(canvas);

        PieData pieData = pieChart.getData();
        if (pieData == null) {
            return;
        }

        MPPointF center = pieChart.getCenterCircleBox();

        float radius = pieChart.getRadius();
        float rotationAngle = pieChart.getRotationAngle();
        float[] drawAngles = pieChart.getDrawAngles();
        float[] absoluteAngles = pieChart.getAbsoluteAngles();

        float phaseX = animator.getPhaseX();
        float phaseY = animator.getPhaseY();

        float holePercent = pieChart.getHoleRadius() / 100f;
        float roundedSliceOffset = (radius - (radius * holePercent)) / 2f;
        if (pieChart.isDrawHoleEnabled() && !pieChart.isDrawSlicesUnderHoleEnabled() && pieChart.isDrawRoundedSlicesEnabled()) {
            rotationAngle += roundedSliceOffset * 360f / (2f * (float) Math.PI * radius);
        }

        float labelRadius = radius - roundedSliceOffset;
        List<IPieDataSet> dataSets = pieData.getDataSets();
        canvas.save();
        int globalIndex = 0;

        for (IPieDataSet dataSet : dataSets) {
            float sliceSpace = getSliceSpace(dataSet);
            for (int i = 0; i < dataSet.getEntryCount(); i++) {

                float startAngle = globalIndex == 0 ? 0 : absoluteAngles[globalIndex - 1] * phaseX;
                float angle = startAngle + ((drawAngles[globalIndex] - ((sliceSpace / (DEG_TO_RAD * labelRadius)) / 2f)) / 2f);

                if (dataSet.getValueLineColor() != 1122867) {

                    float transformedAngle = (angle * phaseY + rotationAngle) * DEG_TO_RAD;
                    float cos = (float) Math.cos(transformedAngle);
                    float sin = (float) Math.sin(transformedAngle);
                    float offset = dataSet.getValueLinePart1OffsetPercentage() / 100f;
                    float lineRadius;

                    if (pieChart.isDrawHoleEnabled()) {
                        float holeRadius = radius * holePercent;
                        lineRadius = ((radius - holeRadius) * offset) + holeRadius;
                    } else {
                        lineRadius = radius * offset;
                    }

                    float x = center.x + cos * lineRadius;
                    float y = center.y + sin * lineRadius;

                    if (dataSet.isUsingSliceColorAsValueLineColor()) {
                        mRenderPaint.setColor(dataSet.getColor(i));
                    }

                    canvas.drawCircle(x, y, CommonUtils.convertDpToPixel(context, DOT_RADIUS_DP), mRenderPaint);
                }

                globalIndex++;
            }
        }
        pieChart.setHoleColor(DataHelper.getAttributeColor(context, R.color.ashy));
        MPPointF.recycleInstance(center);
        canvas.restore();
    }
}