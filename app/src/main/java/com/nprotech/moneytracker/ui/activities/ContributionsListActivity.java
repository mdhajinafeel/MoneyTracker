package com.nprotech.moneytracker.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.GoalContributionType;
import com.nprotech.moneytracker.db.entites.GoalContributionEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.models.GoalContributionWithCurrency;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.GoalViewModel;

import java.util.ArrayList;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ContributionsListActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private MaterialCardView contributionCard;
    private RecyclerView rvTransactions;
    private ConstraintLayout emptyWrapper;
    private GoalViewModel goalViewModel;
    private RecyclerViewAdapter<GoalContributionWithCurrency> contributionRecyclerViewAdapter;
    private int goalId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contributions_list);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            View root = findViewById(R.id.rootView);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            contributionCard = findViewById(R.id.contributionCard);
            rvTransactions = findViewById(R.id.rvTransactions);
            emptyWrapper = findViewById(R.id.emptyWrapper);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {

                if (Objects.equals(bundle.getString("type"), "goal")) {
                    tvTitle.setText(getString(R.string.goal_contribution));
                }
                goalId = bundle.getInt("goalId", 0);
                goalViewModel = new ViewModelProvider(this).get(GoalViewModel.class);

                initializeAdapters();
                bindData();
                observeData();
                setupListeners();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {
            if(goalId > 0) {
                goalViewModel.loadGoalContributions(goalId);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void initializeAdapters() {
        try {

            contributionRecyclerViewAdapter = new RecyclerViewAdapter<>(this, new ArrayList<>(), R.layout.item_goal_contributions) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, GoalContributionWithCurrency goalContribution) {

                    GoalContributionEntity contribution = goalContribution.getContribution();

                    AppCompatImageView ivContributionIcon = holder.getView(R.id.ivContributionIcon);
                    AppCompatTextView tvContributionType = holder.getView(R.id.tvContributionType);
                    AppCompatTextView tvContributionDate = holder.getView(R.id.tvContributionDate);
                    AppCompatTextView tvContributionAmount = holder.getView(R.id.tvContributionAmount);

                    boolean withdrawal = contribution.getType() == GoalContributionType.WITHDRAW;

                    // --------------------------------
                    // Type
                    // --------------------------------
                    if (contribution.getType() == GoalContributionType.AUTO_SAVE) {
                        tvContributionType.setText(R.string.auto_save);
                        ivContributionIcon.setImageResource(R.drawable.ic_refresh);

                        ivContributionIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.light_income));
                        ivContributionIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.income));
                    } else if (contribution.getType() == GoalContributionType.ADD) {
                        tvContributionType.setText(R.string.manual);
                        ivContributionIcon.setImageResource(R.drawable.ic_wallet);

                        ivContributionIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.light_lavender));
                        ivContributionIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primary_dark));
                    } else if (contribution.getType() == GoalContributionType.INITIAL) {
                        tvContributionType.setText(R.string.initial_amount);
                        ivContributionIcon.setImageResource(R.drawable.ic_money_bag);

                        ivContributionIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.light_orange));
                        ivContributionIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.orange));
                    } else if (contribution.getType() == GoalContributionType.WITHDRAW) {
                        tvContributionType.setText(R.string.withdrawal);
                        ivContributionIcon.setImageResource(R.drawable.ic_withdraw);

                        ivContributionIcon.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.light_expense));
                        ivContributionIcon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.expense));
                    }

                    // --------------------------------
                    // Date
                    // --------------------------------
                    tvContributionDate.setText(DateHelper.getFormattedDate(contribution.getDate(), "dd MMM yyyy, hh:mm a"));

                    // --------------------------------
                    // Amount
                    // --------------------------------

                    String amount = CommonUtils.getBeautifyAmount(goalContribution.getCurrencySymbol(), contribution.getAmount());
                    if (withdrawal) {
                        tvContributionAmount.setText(getString(R.string.minus_amount, amount));
                        tvContributionAmount.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.expense));
                    } else {
                        tvContributionAmount.setText(getString(R.string.plus_amount, amount));
                        tvContributionAmount.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.income));
                    }

                    int position = holder.getBindingAdapterPosition();

                    if (position == getItemCount() - 1) {
                        holder.getView(R.id.divider).setAlpha(0f);
                    } else {
                        holder.getView(R.id.divider).setAlpha(1f);
                    }

                    holder.getView(R.id.layoutView).setOnClickListener(v -> {

                    });
                }
            };

            rvTransactions.setHasFixedSize(true);
            rvTransactions.setLayoutManager(new LinearLayoutManager(this));
            rvTransactions.setItemAnimator(null);
            rvTransactions.setNestedScrollingEnabled(false);
            rvTransactions.setAdapter(contributionRecyclerViewAdapter);

            rvTransactions.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm == null)
                        return;
                    int last = lm.findLastVisibleItemPosition();
                    if (last >= contributionRecyclerViewAdapter.getItemCount() - 5) {
                        goalViewModel.loadNextPage();
                    }
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    private void observeData() {
        try {
            goalViewModel.getGoalContributionList().observe(this, list -> {
                if (list == null || list.isEmpty()) {
                    contributionCard.setVisibility(View.GONE);
                    emptyWrapper.setVisibility(View.VISIBLE);
                } else {
                    emptyWrapper.setVisibility(View.GONE);
                    contributionCard.setVisibility(View.VISIBLE);
                    contributionRecyclerViewAdapter.setItems(list);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "observeData", e);
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            });

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finish();
                    ActivityUtils.overrideCloseTransition(ContributionsListActivity.this, R.anim.scale_in, R.anim.right_to_left);
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }
}