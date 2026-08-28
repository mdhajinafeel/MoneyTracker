package com.nprotech.moneytracker.utils;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;

import androidx.annotation.NonNull;

public class CustomTypefaceSpan extends TypefaceSpan {

    private final Typeface typeface;

    public CustomTypefaceSpan(Typeface typeface) {
        super("");
        this.typeface = typeface;
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        applyTypeface(ds);
    }

    @Override
    public void updateMeasureState(@NonNull TextPaint paint) {
        applyTypeface(paint);
    }

    private void applyTypeface(Paint paint) {
        paint.setTypeface(typeface);
    }
}