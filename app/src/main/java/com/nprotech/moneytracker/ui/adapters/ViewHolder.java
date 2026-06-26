package com.nprotech.moneytracker.ui.adapters;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.SparseArray;
import android.view.View;
import android.widget.Checkable;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {

    private final SparseArray<View> mViews;

    public ViewHolder(View itemView) {
        super(itemView);
        this.mViews = new SparseArray<>();
    }

    public ViewHolder(View itemView, int forPagination) {
        super(itemView);
        this.mViews = new SparseArray<>();
    }

    // ✅ FIXED: No unchecked cast warning
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);

        if (view == null) {
            view = itemView.findViewById(viewId);
            mViews.put(viewId, view);
        }

        //noinspection unchecked
        return (T) view;
    }

    // 🔹 TEXT
    public void setViewText(int viewId, CharSequence text) {
        AppCompatTextView view = (AppCompatTextView) getView(viewId);
        if (view != null) view.setText(text);
    }

    public ViewHolder setViewText(int viewId, int resId) {
        AppCompatTextView view = (AppCompatTextView) getView(viewId);
        if (view != null) view.setText(resId);
        return this;
    }

    public void setViewTextColor(int viewId, int color) {
        AppCompatTextView view = (AppCompatTextView) getView(viewId);
        if (view != null) view.setTextColor(color);
    }

    public ViewHolder setViewTextSize(int viewId, float size) {
        AppCompatTextView view = (AppCompatTextView) getView(viewId);
        if (view != null) view.setTextSize(size);
        return this;
    }

    public ViewHolder setViewTypeface(int viewId, Typeface typeface) {
        AppCompatTextView view = (AppCompatTextView) getView(viewId);
        if (view != null) view.setTypeface(typeface);
        return this;
    }

    // 🔹 IMAGE
    public void setViewImageResource(int viewId, int resId) {
        AppCompatImageView view = (AppCompatImageView) getView(viewId);
        if (view != null) view.setImageResource(resId);
    }

    public ViewHolder setViewImageBitmap(int viewId, Bitmap bitmap) {
        AppCompatImageView view = (AppCompatImageView) getView(viewId);
        if (view != null) view.setImageBitmap(bitmap);
        return this;
    }

    public ViewHolder setViewImageDrawable(int viewId, Drawable drawable) {
        AppCompatImageView view = (AppCompatImageView) getView(viewId);
        if (view != null) view.setImageDrawable(drawable);
        return this;
    }

    public ViewHolder setViewImageURI(int viewId, Uri uri) {
        AppCompatImageView view = (AppCompatImageView) getView(viewId);
        if (view != null) view.setImageURI(uri);
        return this;
    }

    // 🔹 CLICK
    public ViewHolder setViewOnClickListener(int viewId, View.OnClickListener listener) {
        View view = getView(viewId);
        if (view != null) view.setOnClickListener(listener);
        return this;
    }

    public ViewHolder setViewOnLongClickListener(int viewId, View.OnLongClickListener listener) {
        View view = getView(viewId);
        if (view != null) view.setOnLongClickListener(listener);
        return this;
    }

    // 🔹 VISIBILITY
    public ViewHolder setViewVisibility(int viewId, int visibility) {
        View view = getView(viewId);
        if (view != null) view.setVisibility(visibility);
        return this;
    }

    // 🔹 CHECKED
    public ViewHolder setViewChecked(int viewId, boolean checked) {
        View view = getView(viewId);
        if (view instanceof Checkable) {
            ((Checkable) view).setChecked(checked);
        }
        return this;
    }

    // 🔹 SELECTED
    public ViewHolder setSelected(int viewId, boolean selected) {
        View view = getView(viewId);
        if (view != null) view.setSelected(selected);
        return this;
    }
}