package com.nprotech.moneytracker.utils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

public class GoalRingDrawable extends Drawable {

    private final Paint backgroundPaint;
    private final Paint progressPaint;
    private final RectF rectF = new RectF();

    private final int strokeWidth;

    public GoalRingDrawable(int backgroundColor,
                            int progressColor,
                            int strokeWidth) {

        this.strokeWidth = strokeWidth;

        // Background ring
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setStrokeCap(Paint.Cap.BUTT);
        backgroundPaint.setColor(backgroundColor);

        // Progress ring
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.BUTT);
        progressPaint.setColor(progressColor);
    }

    @Override
    protected boolean onLevelChange(int level) {
        invalidateSelf();
        return true;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

        Rect bounds = getBounds();

        float halfStroke = strokeWidth / 2f;

        rectF.set(
                bounds.left + halfStroke,
                bounds.top + halfStroke,
                bounds.right - halfStroke,
                bounds.bottom - halfStroke
        );

        // Draw complete background ring
        canvas.drawArc(
                rectF,
                0f,
                360f,
                false,
                backgroundPaint
        );

        // Progress = Drawable level / 10000
        float progress = getLevel() / 10000f;

        // Convert progress to degrees
        float sweepAngle = 360f * progress;

        if (sweepAngle > 0f) {

            // Start from TOP (-90 degrees)
            // Positive sweep = clockwise
            canvas.drawArc(
                    rectF,
                    -90f,
                    sweepAngle,
                    false,
                    progressPaint
            );
        }
    }

    @Override
    public void setAlpha(int alpha) {
        backgroundPaint.setAlpha(alpha);
        progressPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(android.graphics.ColorFilter colorFilter) {
        backgroundPaint.setColorFilter(colorFilter);
        progressPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return android.graphics.PixelFormat.TRANSLUCENT;
    }
}