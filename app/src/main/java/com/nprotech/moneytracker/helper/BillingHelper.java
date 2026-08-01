package com.nprotech.moneytracker.helper;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.QueryProductDetailsParams;

import java.util.Collections;
import java.util.List;

public class BillingHelper {

    public interface PriceListener {
        void onPriceLoaded(String price);

        void onError();
    }

    private final BillingClient billingClient;

    public BillingHelper(Activity activity) {

        billingClient = BillingClient.newBuilder(activity)
                .setListener((billingResult, purchases) -> {
                    // Purchase updates
                })
                .enablePendingPurchases(
                        PendingPurchasesParams.newBuilder()
                                .enableOneTimeProducts()
                                .build()
                )
                .build();
    }

    public void loadSubscriptionPrice(String productId,
                                      PriceListener listener) {

        billingClient.startConnection(new BillingClientStateListener() {

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                    listener.onError();
                    return;
                }

                QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build();

                QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                        .setProductList(Collections.singletonList(product))
                        .build();

                billingClient.queryProductDetailsAsync(params, (result, queryResult) -> {
                    if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                        listener.onError();
                        return;
                    }
                    List<ProductDetails> products =
                            queryResult.getProductDetailsList();

                    if (products.isEmpty()) {
                        listener.onError();
                        return;
                    }

                    ProductDetails details = products.get(0);

                    List<ProductDetails.SubscriptionOfferDetails> offers =
                            details.getSubscriptionOfferDetails();

                    if (offers == null || offers.isEmpty()) {
                        listener.onError();
                        return;
                    }

                    ProductDetails.PricingPhase pricing = offers.get(0)
                            .getPricingPhases()
                            .getPricingPhaseList()
                            .get(0);

                    listener.onPriceLoaded(
                            pricing.getFormattedPrice()
                    );
                });
            }

            @Override
            public void onBillingServiceDisconnected() {
                listener.onError();
            }
        });
    }

    public void destroy() {
        billingClient.endConnection();
    }
}