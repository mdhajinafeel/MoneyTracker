package com.nprotech.moneytracker.ui.activities;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.models.SubscriptionBenefit;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.utils.SimpleDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManageSubscriptionActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView tvUpgradeTitle, tvBadgeActive, lblUnlockPremium;
    private AppCompatTextView tvPlan, tvPrice, tvStartDate, tvRenewalDate;
    private MaterialCardView cardRestorePurchases, cardChangePlan, cardPayment, cardSupport, cardCancelSubscription, cardManagePlay, cardPremiumBenefits;

    private RecyclerView rvPremiumBenefits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_subscriptions);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View rootView = findViewById(R.id.rootView);
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            tvUpgradeTitle = findViewById(R.id.tvUpgradeTitle);
            lblUnlockPremium = findViewById(R.id.lblUnlockPremium);
            tvBadgeActive = findViewById(R.id.tvBadgeActive);
            rvPremiumBenefits = findViewById(R.id.rvPremiumBenefits);
            cardRestorePurchases = findViewById(R.id.cardRestorePurchases);
            cardChangePlan = findViewById(R.id.cardChangePlan);
            cardPayment = findViewById(R.id.cardPayment);
            cardSupport = findViewById(R.id.cardSupport);
            cardCancelSubscription = findViewById(R.id.cardCancelSubscription);
            cardManagePlay = findViewById(R.id.cardManagePlay);
            cardPremiumBenefits = findViewById(R.id.cardPremiumBenefits);
            tvPlan = findViewById(R.id.tvPlan);
            tvPrice = findViewById(R.id.tvPrice);
            tvStartDate = findViewById(R.id.tvStartDate);
            tvRenewalDate = findViewById(R.id.tvRenewalDate);

            tvTitle.setText(R.string.manage_subscription);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            bindData();
            setupListeners();
            setupPremiumBenefits();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {
            tvUpgradeTitle.setText(getString(R.string.app_premium, getString(R.string.app_name)));
            lblUnlockPremium.setText(getString(R.string.unlock_the_full_power, getString(R.string.app_name)));
            CommonUtils.setDrawable(ManageSubscriptionActivity.this, tvBadgeActive, R.drawable.ic_complete, R.dimen.icon_12, R.color.dark_income, Gravity.START);
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> finishWithTransitions());

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finishWithTransitions();
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void setupPremiumBenefits() {

        List<SubscriptionBenefit> benefits = new ArrayList<>();
        benefits.add(new SubscriptionBenefit(getString(R.string.transactions), getString(R.string.manage_income_expenses),
                R.drawable.ic_transaction, R.color.feature_transactions, R.color.feature_transactions_bg));
        benefits.add(new SubscriptionBenefit(getString(R.string.accounts), getString(R.string.organize_financial_accounts),
                R.drawable.ic_bank, R.color.feature_accounts, R.color.feature_accounts_bg));
        benefits.add(new SubscriptionBenefit(getString(R.string.wallets), getString(R.string.manage_multiple_wallets),
                R.drawable.ic_wallet, R.color.feature_wallets, R.color.feature_wallets_bg));
        benefits.add(new SubscriptionBenefit(getString(R.string.categories), getString(R.string.organize_income_and_expenses),
                R.drawable.ic_category_outline, R.color.feature_currencies, R.color.feature_currencies_bg));
        benefits.add(new SubscriptionBenefit(getString(R.string.goals), getString(R.string.set_financial_goals_track_progress),
                R.drawable.ic_more_goal, R.color.feature_goals, R.color.feature_goals_bg));
        benefits.add(new SubscriptionBenefit(getString(R.string.budgets), getString(R.string.plan_and_monitor_spending),
                R.drawable.ic_budget_outline, R.color.feature_budgets, R.color.feature_budgets_bg));
        benefits.add(new SubscriptionBenefit(getString(R.string.debt_loans), getString(R.string.track_money_borrowed_lent),
                R.drawable.ic_more_debt, R.color.feature_debt_loans, R.color.feature_debt_loans_bg));
        benefits.add(new SubscriptionBenefit(getString(R.string.recurring_transactions), getString(R.string.automate_recurring_financial_entries),
                R.drawable.ic_more_recurring, R.color.feature_recurring, R.color.feature_recurring_bg));
        benefits.add(new SubscriptionBenefit(getString(R.string.ad_free_experience), getString(R.string.enjoy_expenixo_without_advertisements),
                R.drawable.ic_no_ads, R.color.feature_ad_free, R.color.feature_ad_free_bg));
        benefits.add(new SubscriptionBenefit(getString(R.string.cloud_backup_sync), getString(R.string.back_up_and_restore_financial_data),
                R.drawable.ic_cloud, R.color.feature_cloud_backup, R.color.feature_cloud_backup_bg));

        RecyclerViewAdapter<SubscriptionBenefit> subscriptionBenefitAdapter = new RecyclerViewAdapter<>(ManageSubscriptionActivity.this, benefits,
                R.layout.item_subscription_benefit) {
            @Override
            public void onPostBindViewHolder(ViewHolder holder, SubscriptionBenefit subscriptionBenefit) {
                AppCompatImageView ivBenefitIcon = holder.getView(R.id.ivBenefitIcon);
                AppCompatTextView tvBenefitTitle = holder.getView(R.id.tvBenefitTitle);
                AppCompatTextView tvBenefitDescription = holder.getView(R.id.tvBenefitDescription);

                ivBenefitIcon.setImageDrawable(ContextCompat.getDrawable(ManageSubscriptionActivity.this, subscriptionBenefit.iconRes));
                ivBenefitIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(ManageSubscriptionActivity.this,
                        subscriptionBenefit.foregroundColor)));
                ivBenefitIcon.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ManageSubscriptionActivity.this,
                        subscriptionBenefit.backgroundColor)));

                tvBenefitTitle.setText(subscriptionBenefit.title);
                tvBenefitTitle.setTextColor(ContextCompat.getColor(ManageSubscriptionActivity.this, subscriptionBenefit.foregroundColor));

                tvBenefitDescription.setText(subscriptionBenefit.description);
            }
        };

        rvPremiumBenefits.setLayoutManager(new LinearLayoutManager(this));
        rvPremiumBenefits.setNestedScrollingEnabled(false);
        rvPremiumBenefits.setHasFixedSize(true);
        rvPremiumBenefits.setAdapter(subscriptionBenefitAdapter);
        rvPremiumBenefits.addItemDecoration(new SimpleDividerItemDecoration(this));
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(ManageSubscriptionActivity.this, R.anim.scale_in, R.anim.right_to_left);
    }
}