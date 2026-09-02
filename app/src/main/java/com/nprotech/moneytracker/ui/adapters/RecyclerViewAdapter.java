package com.nprotech.moneytracker.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class RecyclerViewAdapter<T> extends RecyclerView.Adapter<ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private static final int VIEW_TYPE_FOOTER = 2;
    private int mFooterLayoutId = -1;
    private final Context mContext;
    private final List<T> mDataList;
    private final List<T> mFullList;
    private final int mLayoutId;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private int mHeaderLayoutId = -1; // Optional

    public RecyclerViewAdapter(Context context, List<T> dataList, int layoutId) {
        mContext = context;
        mDataList = new ArrayList<>(dataList);
        mFullList = new ArrayList<>(dataList); // copy
        mLayoutId = layoutId;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        mOnItemLongClickListener = onItemLongClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        int layoutToUse;

        if (viewType == VIEW_TYPE_HEADER) {
            layoutToUse = mHeaderLayoutId;
        } else if (viewType == VIEW_TYPE_FOOTER) {
            layoutToUse = mFooterLayoutId;
        } else {
            layoutToUse = mLayoutId;
        }

        View view = LayoutInflater.from(mContext).inflate(layoutToUse, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            onHeaderBind(holder);
            return;
        }

        if (getItemViewType(position) == VIEW_TYPE_FOOTER) {
            onFooterBind(holder);
            return;
        }

        int actualPosition = hasHeader() ? position - 1 : position;

        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(v -> mOnItemClickListener.onItemClick(v, actualPosition));
        }

        if (mOnItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> mOnItemLongClickListener.onItemLongClick(v, actualPosition));
        }

        onPostBindViewHolder(holder, mDataList.get(actualPosition));
    }

    @Override
    public int getItemCount() {
        return mDataList.size()
                + (hasHeader() ? 1 : 0)
                + (hasFooter() ? 1 : 0);
    }

    public abstract void onPostBindViewHolder(ViewHolder holder, T t);

    public T getItem(int position) {
        return mDataList.get(hasHeader() ? position - 1 : position);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }

    public void add(T t) {
        mDataList.add(t);
        notifyItemInserted(mDataList.size() - 1);
    }

    public void addAll(List<T> items) {
        for (T t : items) {
            add(t);
        }
        mFullList.addAll(items);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filter(FilterCondition<T> condition) {
        mDataList.clear();

        for (T item : mFullList) {
            if (condition.apply(item)) {
                mDataList.add(item);
            }
        }

        notifyDataSetChanged();
    }

    public void setHeaderLayout(int headerLayoutId) {
        this.mHeaderLayoutId = headerLayoutId;
    }

    public boolean hasHeader() {
        return mHeaderLayoutId != -1;
    }

    @Override
    public int getItemViewType(int position) {

        if (hasHeader() && position == 0) {
            return VIEW_TYPE_HEADER;
        }

        if (hasFooter() && position == getItemCount() - 1) {
            return VIEW_TYPE_FOOTER;
        }

        return VIEW_TYPE_ITEM;
    }

    public interface FilterCondition<T> {
        boolean apply(T item);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<T> items) {
        mDataList.clear();
        mDataList.addAll(items);

        mFullList.clear();          // 🔥 important for filter
        mFullList.addAll(items);

        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void replaceItems(List<T> items) {
        mDataList.clear();
        mDataList.addAll(items);
        notifyDataSetChanged();
    }

    public List<T> getItems() {
        return mFullList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void remove(T item) {
        mDataList.remove(item);
        mFullList.remove(item);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mDataList.remove(position);
        notifyItemRemoved(position);
    }

    public void setFooterLayout(int footerLayoutId) {
        mFooterLayoutId = footerLayoutId;
    }

    public boolean hasFooter() {
        return mFooterLayoutId != -1;
    }

    public void onFooterBind(ViewHolder holder) {
    }

    public void onHeaderBind(ViewHolder holder) {
    }
}