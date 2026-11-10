package com.nprotech.moneytracker.helper;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.nprotech.moneytracker.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillingHelper {

    private static final String PRODUCT_ID = "expenixo_pro";
    private final BillingClient billingClient;
    private final BillingCallback callback;
    private final Map<String, ProductDetails> productDetailsMap = new HashMap<>();
    private final Map<String, String> offerTokenMap = new HashMap<>();
    private boolean isConnected = false;
    private final Context context;

    public interface BillingCallback {
        void onPricesLoaded(String monthlyPrice, String annualPrice, long monthlyAmountMicros, long annualAmountMicros, String currencyCode);

        void onPurchaseTokenReceived(String productId, String purchaseToken);

        void onPurchasePending();

        void onPurchaseCancelled();

        void onError(String message);
    }

    public BillingHelper(@NonNull Context context, @NonNull BillingCallback callback) {
        this.callback = callback;
        this.context = context.getApplicationContext();

        billingClient = BillingClient.newBuilder(context.getApplicationContext()).setListener((billingResult, purchases) -> {
                    int responseCode = billingResult.getResponseCode();
                    if (responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                        for (Purchase purchase : purchases) {
                            handlePurchase(purchase);
                        }
                    } else if (responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                        callback.onPurchaseCancelled();
                    } else if (responseCode != BillingClient.BillingResponseCode.OK) {
                        callback.onError(billingResult.getDebugMessage());
                    }
                })
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .build();
    }

    public void connect() {
        if (billingClient.isReady()) {
            isConnected = true;
            queryProducts();
            return;
        }

        billingClient.startConnection(
                new BillingClientStateListener() {
                    @Override
                    public void onBillingSetupFinished(@NonNull BillingResult result) {
                        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            isConnected = true;
                            queryProducts();
                            restorePurchases();
                        } else {
                            callback.onError(result.getDebugMessage());
                        }
                    }

                    @Override
                    public void onBillingServiceDisconnected() {
                        isConnected = false;
                    }
                }
        );
    }

    private void queryProducts() {
        QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build();

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(Collections.singletonList(product))
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, queryResult) -> {
                    if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                        callback.onError(billingResult.getDebugMessage());
                        return;
                    }

                    productDetailsMap.clear();
                    offerTokenMap.clear();

                    for (ProductDetails details : queryResult.getProductDetailsList()) {
                        if (!PRODUCT_ID.equals(details.getProductId())) {
                            continue;
                        }
                        List<ProductDetails.SubscriptionOfferDetails> offers = details.getSubscriptionOfferDetails();
                        if (offers == null) {
                            continue;
                        }

                        for (ProductDetails.SubscriptionOfferDetails offer : offers) {
                            String basePlanId = offer.getBasePlanId();
                            if (!"monthly".equals(basePlanId) && !"annual".equals(basePlanId)) {
                                continue;
                            }

                            // Prefer the base-plan offer.
                            if (offer.getOfferId() != null) {
                                continue;
                            }

                            productDetailsMap.put(basePlanId, details);
                            offerTokenMap.put(basePlanId, offer.getOfferToken());
                        }
                    }

                    ProductDetails.PricingPhase monthlyPhase = getPricePhase("monthly");
                    ProductDetails.PricingPhase annualPhase = getPricePhase("annual");

                    if (monthlyPhase == null || annualPhase == null) {
                        callback.onError(context.getString(R.string.error_loading_subscription_prices));
                        return;
                    }

                    callback.onPricesLoaded(monthlyPhase.getFormattedPrice(), annualPhase.getFormattedPrice(), monthlyPhase.getPriceAmountMicros(),
                            annualPhase.getPriceAmountMicros(), annualPhase.getPriceCurrencyCode());
                }
        );
    }

    public void purchase(Activity activity, String basePlanId) {
        if (!isConnected || !billingClient.isReady()) {
            callback.onError(context.getString(R.string.error_google_play_not_connected));
            connect();
            return;
        }

        ProductDetails details = productDetailsMap.get(basePlanId);
        String offerToken = offerTokenMap.get(basePlanId);

        if (details == null || offerToken == null) {
            callback.onError(context.getString(R.string.error_subscription_plan_unavailable));
            queryProducts();
            return;
        }

        BillingFlowParams.ProductDetailsParams productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .setOfferToken(offerToken)
                .build();

        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(Collections.singletonList(productParams))
                .build();

        BillingResult result = billingClient.launchBillingFlow(activity, flowParams);
        if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            callback.onError(result.getDebugMessage());
        }
    }

    public void restorePurchases() {
        if (!billingClient.isReady()) {
            return;
        }

        QueryPurchasesParams params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build();

        billingClient.queryPurchasesAsync(params, (billingResult, purchases) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);
                }
            } else {
                callback.onError(billingResult.getDebugMessage());
            }
        });
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            callback.onPurchasePending();
            return;
        }

        if (purchase.getPurchaseState() != Purchase.PurchaseState.PURCHASED) {
            return;
        }

        if (!purchase.getProducts().contains(PRODUCT_ID)) {
            return;
        }

        // Send the token to your backend for Google Play verification.
        callback.onPurchaseTokenReceived(PRODUCT_ID, purchase.getPurchaseToken());

        // Acknowledge if not already acknowledged.
        // Production implementations should coordinate acknowledgement
        // with successful server-side verification.
        if (!purchase.isAcknowledged()) {
            AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();

            billingClient.acknowledgePurchase(params, result -> {
                        if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                            callback.onError(context.getString(R.string.error_purchase_acknowledgement, result.getDebugMessage()));
                        }
                    }
            );
        }
    }

    private ProductDetails.PricingPhase getPricePhase(String basePlanId) {
        ProductDetails details = productDetailsMap.get(basePlanId);

        if (details == null || details.getSubscriptionOfferDetails() == null) {
            return null;
        }

        for (ProductDetails.SubscriptionOfferDetails offer : details.getSubscriptionOfferDetails()) {
            if (basePlanId.equals(offer.getBasePlanId()) && offer.getOfferId() == null) {
                List<ProductDetails.PricingPhase> phases = offer.getPricingPhases().getPricingPhaseList();
                if (!phases.isEmpty()) {
                    return phases.get(phases.size() - 1);
                }
            }
        }

        return null;
    }

    public void disconnect() {
        isConnected = false;
        billingClient.endConnection();
    }
}