package com.nprotech.moneytracker.ui.activities;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.BuildConfig;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.BillingHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.PremiumFeatureModel;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;

import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UpgradePremiumActivity extends BaseActivity {

    private AppCompatImageView icBack, ivMonthlySelected, ivYearlySelected;
    private View featureWallet, featureCloud, featureOthers;
    private AppCompatTextView tvMonthlyPrice, tvYearlyPrice, lblEquivalentMonth, tvSavePrice;
    private MaterialCardView cardMonth, cardYear;
    private LinearLayout featureRowsContainer;
    private MaterialButton btnUpgradePro;
    private String selectedPlan = "monthly"; // Default selection
    private BillingHelper billingHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade_premium);
        statusBarDarkSetting();
        hideKeyboard(this);
        initComponents();

        // TODO debug
        if (BuildConfig.DEBUG) {
            showTestPrices();
        } else {
            initializeBilling();
        }
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            View rootView = findViewById(R.id.rootView);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            featureWallet = findViewById(R.id.featureWallet);
            featureCloud = findViewById(R.id.featureCloud);
            featureOthers = findViewById(R.id.featureOthers);
            tvMonthlyPrice = findViewById(R.id.tvMonthlyPrice);
            tvYearlyPrice = findViewById(R.id.tvYearlyPrice);
            lblEquivalentMonth = findViewById(R.id.lblEquivalentMonth);
            tvSavePrice = findViewById(R.id.tvSavePrice);
            featureRowsContainer = findViewById(R.id.featureRowsContainer);
            cardMonth = findViewById(R.id.cardMonth);
            cardYear = findViewById(R.id.cardYear);
            btnUpgradePro = findViewById(R.id.btnUpgradePro);
            ivMonthlySelected = findViewById(R.id.ivMonthlySelected);
            ivYearlySelected = findViewById(R.id.ivYearlySelected);

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

            tvTitle.setText(R.string.upgrade_premium);

            bindData();
            setupFeatureComparison();
            setupListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {
            List<PremiumFeatureModel> features = Arrays.asList(
                    new PremiumFeatureModel(R.drawable.ic_wallet, getString(R.string.unlimited_wallets)),
                    new PremiumFeatureModel(R.drawable.ic_cloud, getString(R.string.cloud_backup)),
                    new PremiumFeatureModel(R.drawable.ic_calendar_all, getString(R.string.no_limits_anywhere)));

            bindFeature(featureWallet, features.get(0));
            bindFeature(featureCloud, features.get(1));
            bindFeature(featureOthers, features.get(2));
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    //TODO prices
    private void showTestPrices() {
        tvMonthlyPrice.setText(R.string.text_49);
        tvYearlyPrice.setText(R.string.text_399);
        lblEquivalentMonth.setText(getString(R.string.approx_monthly_price, "₹33.25"));
        tvSavePrice.setVisibility(View.VISIBLE);
        tvSavePrice.setText(R.string.text_189);

            btnUpgradePro.setEnabled(true);
            btnUpgradePro.setAlpha(1.0f);
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

            cardMonth.setOnClickListener(v -> selectPlan("monthly"));
            cardYear.setOnClickListener(v -> selectPlan("annual"));

            selectPlan(selectedPlan);

            // TODO upgrade pro selected
            btnUpgradePro.setOnClickListener(v -> {
                if (BuildConfig.DEBUG) {

                    String price;
                    String period;

                    if ("annual".equals(selectedPlan)) {
                        price = "₹399";
                        period = "Annual plan";
                    } else {
                        price = "₹49";
                        period = "/month";
                    }

                    PreferenceManager.INSTANCE.setProPlanDetails(selectedPlan, price, period);

                    PreferenceManager.INSTANCE.setDebugProEnabled(true);
                    String planName = "monthly".equals(selectedPlan) ? getString(R.string.calendar_monthly) : getString(R.string.annual);
                    Toast.makeText(this, planName + " plan selected (test mode)", Toast.LENGTH_SHORT).show();

                    // Close the upgrade screen and return to the previous screen.
                    finishWithTransitions();
                    return;
                }

                if (billingHelper != null) {
                    billingHelper.purchase(this, selectedPlan);
                } else {
                    Toast.makeText(UpgradePremiumActivity.this, R.string.error_google_play_not_connected, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void bindFeature(View featureView, PremiumFeatureModel feature) {
        AppCompatImageView ivIcon = featureView.findViewById(R.id.ivIcon);
        AppCompatTextView tvFeature = featureView.findViewById(R.id.tvFeature);
        ivIcon.setImageResource(feature.icon());
        tvFeature.setText(feature.title());
    }

    private record FeatureRow(String name, String freeValue, @DrawableRes int proIcon, @DrawableRes int featureIcon, @ColorRes int featureColor) {
    }

    private void setupFeatureComparison() {
        List<FeatureRow> features = Arrays.asList(
                new FeatureRow(getString(R.string.transactions), getString(R.string.unlimited), R.drawable.ic_complete, R.drawable.ic_transaction, R.color.feature_transactions),
                new FeatureRow(getString(R.string.accounts), getString(R.string.up_to_3), R.drawable.ic_complete, R.drawable.ic_bank, R.color.feature_accounts),
                new FeatureRow(getString(R.string.wallets), getString(R.string.up_to_3), R.drawable.ic_complete, R.drawable.ic_wallet, R.color.feature_wallets),
                new FeatureRow(getString(R.string.currencies), getString(R.string.up_to_3), R.drawable.ic_complete, R.drawable.ic_exchange, R.color.feature_currencies),
                new FeatureRow(getString(R.string.budgets), getString(R.string.up_to_3), R.drawable.ic_complete, R.drawable.ic_budget_outline, R.color.feature_budgets),
                new FeatureRow(getString(R.string.goals), getString(R.string.up_to_3), R.drawable.ic_complete, R.drawable.ic_more_goal, R.color.feature_goals),
                new FeatureRow(getString(R.string.debt_loans), getString(R.string.up_to_3), R.drawable.ic_complete, R.drawable.ic_more_debt, R.color.feature_debt_loans),
                new FeatureRow(getString(R.string.recurring_transactions), getString(R.string.up_to_3), R.drawable.ic_complete, R.drawable.ic_more_recurring, R.color.feature_recurring),
                new FeatureRow(getString(R.string.cloud_backup_sync), getString(R.string.manual_backup), R.drawable.ic_complete, R.drawable.ic_cloud, R.color.feature_cloud_backup),
                new FeatureRow(getString(R.string.ad_free_experience), getString(R.string.ads_supported), R.drawable.ic_complete, R.drawable.ic_no_ads, R.color.feature_ad_free)
        );

        featureRowsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < features.size(); i++) {
            FeatureRow feature = features.get(i);

            View row = inflater.inflate(R.layout.layout_pro_features, featureRowsContainer, false);
            AppCompatTextView tvFeatureName = row.findViewById(R.id.tvFeatureName);
            AppCompatTextView tvFreeValue = row.findViewById(R.id.tvFreeValue);
            AppCompatImageView ivIconPro = row.findViewById(R.id.ivIconPro);
            AppCompatImageView ivIcon = row.findViewById(R.id.ivIcon);

            ivIcon.setImageResource(feature.featureIcon);
            ivIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), feature.featureColor)));
            tvFeatureName.setText(feature.name);
            tvFreeValue.setText(feature.freeValue);
            ivIconPro.setImageResource(feature.proIcon);

            featureRowsContainer.addView(row);

            // Divider between rows, except after the last row.
            if (i < features.size() - 1) {
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(ContextCompat.getColor(this, R.color.lavendar_web));
                featureRowsContainer.addView(divider);
            }
        }
    }

    private void selectPlan(String plan) {
        selectedPlan = plan;
        boolean isMonthly = "monthly".equals(plan);
        updatePlanCard(cardMonth, isMonthly);
        updatePlanCard(cardYear, !isMonthly);
    }

    private void updatePlanCard(MaterialCardView card, boolean selected) {
        int selectedColor = ContextCompat.getColor(this, R.color.vibrant_orange);
        int normalColor = ContextCompat.getColor(this, R.color.light_lavender);
        card.setStrokeWidth((int) (selected ? 1 * getResources().getDisplayMetrics().density : getResources().getDisplayMetrics().density));
        card.setStrokeColor(selected ? selectedColor : normalColor);
        card.setCardBackgroundColor(ContextCompat.getColor(this, selected ? R.color.light_lavender : R.color.white));
        card.setClickable(true);
        card.setFocusable(true);

        if(selected) {
            ivMonthlySelected.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_circle_outline));
            ivYearlySelected.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_complete));
        } else {
            ivMonthlySelected.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_complete));
            ivYearlySelected.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_circle_outline));
        }
    }

    // TODO
    private void onSubscriptionVerificationResult(boolean isActive) {
        runOnUiThread(() -> {
            PreferenceManager.INSTANCE.setVerifiedProActive(isActive);
            if (isActive) {
                Toast.makeText(UpgradePremiumActivity.this, R.string.pro_activated, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(UpgradePremiumActivity.this, R.string.subscription_not_verified, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeBilling() {
        billingHelper = new BillingHelper(getApplicationContext(), new BillingHelper.BillingCallback() {
            @Override
            public void onPricesLoaded(String monthlyPrice, String annualPrice, long monthlyAmountMicros, long annualAmountMicros, String currencyCode) {
                runOnUiThread(() -> {
                    tvMonthlyPrice.setText(monthlyPrice);
                    tvYearlyPrice.setText(annualPrice);

                    btnUpgradePro.setEnabled(true);
                    btnUpgradePro.setAlpha(1.0f);

                    double monthlyAmount = monthlyAmountMicros / 1_000_000.0;
                    double annualAmount = annualAmountMicros / 1_000_000.0;
                    double equivalentMonthly = annualAmount / 12.0;
                    double savings = (monthlyAmount * 12.0) - annualAmount;

                    lblEquivalentMonth.setText(getString(R.string.approx_monthly_price, CommonUtils.formatCurrency(getApplicationContext(),
                            equivalentMonthly, currencyCode)));

                    if (savings > 0) {
                        tvSavePrice.setVisibility(View.VISIBLE);
                        tvSavePrice.setText(getString(R.string.save_per_year, CommonUtils.formatCurrency(getApplicationContext(), savings, currencyCode)));
                    } else {
                        tvSavePrice.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onPurchaseTokenReceived(String productId, String purchaseToken) {
                // TODO: Send the token to your backend
                // to verify the subscription with Google Play.
                runOnUiThread(() -> Toast.makeText(UpgradePremiumActivity.this, R.string.verifying_your_pro_subscription, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onPurchasePending() {
                runOnUiThread(() -> Toast.makeText(UpgradePremiumActivity.this, R.string.your_payment_is_pending, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onPurchaseCancelled() {
                // The user canceled the purchase.
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(UpgradePremiumActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });

        billingHelper.connect();
    }

    private void checkProStatus() {
        boolean isPro = PreferenceManager.INSTANCE.isProEnabled();
        if (isPro) {
            Toast.makeText(this, R.string.expenixo_pro_enabled, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.free_plan_active, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (billingHelper != null) {
            billingHelper.disconnect();
        }
        super.onDestroy();
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
    }
}