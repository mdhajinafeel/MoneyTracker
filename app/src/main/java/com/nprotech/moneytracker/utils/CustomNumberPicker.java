package com.nprotech.moneytracker.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.annotation.Nullable;

public class CustomNumberPicker extends NumberPicker {

    private Typeface typeface;

    public CustomNumberPicker(Context context) {
        super(context);
    }

    public CustomNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCustomTypeface(@Nullable Typeface typeface) {
        this.typeface = typeface;
        applyTypeface();
    }

    private void applyTypeface() {
        if (typeface == null) {
            return;
        }
        applyTypefaceToView(this);
    }

    private void applyTypefaceToView(View view) {
        if (view instanceof EditText editText) {
            editText.setTypeface(typeface);
            editText.setTextAppearance(
                    android.R.style.TextAppearance_Material_Body1
            );
            editText.setTypeface(typeface);
            return;
        }

        if (view instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) {
                applyTypefaceToView(group.getChildAt(i));
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        applyTypeface();
    }

    @Override
    public void setValue(int value) {
        super.setValue(value);
        post(this::applyTypeface);
    }

    @Override
    public void setDisplayedValues(String[] displayedValues) {
        super.setDisplayedValues(displayedValues);
        post(this::applyTypeface);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(this::applyTypeface);
    }
}