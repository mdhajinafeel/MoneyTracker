package com.nprotech.moneytracker.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class CurvedBackgroundView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    public CurvedBackgroundView(Context context) {
        super(context);
    }

    public CurvedBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {

        super.onSizeChanged(w, h, oldWidth, oldHeight);

        LinearGradient gradient = new LinearGradient(
                0, 0,
                w, h,
                new int[]{
                        Color.parseColor("#F5EEFF"),
                        Color.parseColor("#DEC7FF"),
                        Color.parseColor("#B98EFF")
                },
                null,
                Shader.TileMode.CLAMP
        );

        paint.setShader(gradient);

        path.reset();

        float curve = 30f;

        path.moveTo(0, 0);
        path.lineTo(w - curve, 0);

        path.quadTo(w, h / 2f, w - curve, h);

        path.lineTo(0, h);

        path.close();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }
}