package com.nprotech.moneytracker.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;

import java.util.Date;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class IncomeBreakdownFragment extends Fragment {

    private ConstraintLayout emptyWrapper;
    private RecyclerView rvIncomeBreakdown;
    private int accountId;
    private Date transactionDate;
    private long loadedStart, loadedEnd;

    public static IncomeBreakdownFragment newInstance(int accountId, Date transactionDate) {
        IncomeBreakdownFragment fragment = new IncomeBreakdownFragment();

        Bundle args = new Bundle();
        args.putInt("accountId", accountId);
        args.putLong("transactionDate", transactionDate.getTime());

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            accountId = getArguments().getInt("accountId");
            long time = getArguments().getLong("transactionDate");
            transactionDate = new Date(time);
            loadedStart = getArguments().getLong("loadedStart");
            loadedEnd = getArguments().getLong("loadedEnd");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income_breakdown, container, false);
        try {
            rvIncomeBreakdown = view.findViewById(R.id.rvIncomeBreakdown);
            emptyWrapper = view.findViewById(R.id.emptyWrapper);
        } catch (Exception e) {
            AppLogger.e(getClass(), "onCreateView", e);
        }
        return view;
    }
}