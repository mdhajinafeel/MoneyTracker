package com.nprotech.moneytracker.ui.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.models.TransactionWithDetails;
import com.nprotech.moneytracker.utils.CommonUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void statusBarDarkSetting() {

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Window window = getWindow();

        // Keep classic layout (content below status bar)
        WindowCompat.setDecorFitsSystemWindows(window, true);

        // Set status bar icon color
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(window, window.getDecorView());

        // false = light icons (white)
        // true = dark icons (black)
        controller.setAppearanceLightStatusBars(true);

        // Android 14 and below
        setStatusBarColorCompat(window);
    }

    @SuppressWarnings("deprecation")
    protected void statusBarSetting() {

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Window window = getWindow();

        WindowCompat.setDecorFitsSystemWindows(window, true);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());

        // White status bar icons
        controller.setAppearanceLightStatusBars(false);

        // White navigation bar icons
        controller.setAppearanceLightNavigationBars(false);

        setStatusBarColorCompat(window);

        // Navigation bar color
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.primary_dark)
        );
    }

    @SuppressWarnings("deprecation")
    private void setStatusBarColorCompat(Window window) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.setStatusBarColor(
                    ContextCompat.getColor(this, R.color.vibrant_orange)
            );
        }
    }

    public void hideKeyboard(Context ctx) {
        InputMethodManager inputManager =
                (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null) return;
        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void showTransactionActions(TransactionWithDetails item, Runnable onViewDetails, Runnable onEdit, Runnable onDuplicate,
                                       Runnable onDelete, boolean isDetails) {
        try {

            TransactionEntity transaction = item.transaction;
            TransactionEntity feeTransaction = item.feeTransaction;

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_transaction_options, findViewById(android.R.id.content), false);

            MaterialCardView colorView = bottomView.findViewById(R.id.colorView);
            AppCompatImageView ivTransactionIcon = bottomView.findViewById(R.id.ivTransactionIcon);
            AppCompatTextView nameLabel = bottomView.findViewById(R.id.nameLabel);
            AppCompatTextView amountLabel = bottomView.findViewById(R.id.amountLabel);
            AppCompatTextView feeLabel = bottomView.findViewById(R.id.feeLabel);
            AppCompatTextView tvBadgeFee = bottomView.findViewById(R.id.tvBadgeFee);
            AppCompatTextView feeAmountLabel = bottomView.findViewById(R.id.feeAmountLabel);
            AppCompatTextView detailLabel = bottomView.findViewById(R.id.detailLabel);
            AppCompatTextView tvBadgeDetail = bottomView.findViewById(R.id.tvBadgeDetail);
            AppCompatTextView timeLabel = bottomView.findViewById(R.id.timeLabel);

            colorView.setCardBackgroundColor(Color.parseColor(item.color));
            if (transaction.type == TransactionEntity.TYPE_TRANSFER) {
                ivTransactionIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_transfer));
            } else {
                if (item.icon == null || item.icon == 0) {
                    ivTransactionIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.category_0));
                } else {
                    ivTransactionIcon.setImageDrawable(ContextCompat.getDrawable(this, DataHelper.getCategoryIcons().get(item.icon)));
                }
            }

            String categoryName = transaction.getCategoryName(this);
            if (Objects.equals(categoryName, "")) {
                categoryName = item.categoryName;
            }
            nameLabel.setText(categoryName);

            double amount;
            int color = ContextCompat.getColor(this, R.color.income);
            Drawable badge = ContextCompat.getDrawable(this, R.drawable.bg_badge_income);
            String badgeText = getString(R.string.income);
            if (transaction.type == TransactionEntity.TYPE_INCOME) {
                amount = transaction.amount;
            } else if (transaction.type == TransactionEntity.TYPE_TRANSFER) {
                amount = transaction.amount;
                color = ContextCompat.getColor(this, R.color.transfer);
                badge = ContextCompat.getDrawable(this, R.drawable.bg_badge_transfer);
                badgeText = getString(R.string.transfer);
            } else {
                amount = transaction.amount * -1;
                color = ContextCompat.getColor(this, R.color.expense);
                badge = ContextCompat.getDrawable(this, R.drawable.bg_badge_expense);
                badgeText = getString(R.string.expense);
            }

            amountLabel.setText(CommonUtils.getBeautifyAmount(item.currencySymbol, amount));
            amountLabel.setTextColor(color);

            CommonUtils.setDrawable(this, timeLabel, R.drawable.ic_time, R.dimen.icon_14, R.color.color_matte_black, Gravity.START);
            timeLabel.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(transaction.transactionDate)));

            if (item.fromWalletName != null && !item.fromWalletName.isEmpty()) {
                detailLabel.setText(getString(R.string.from_to_wallet_name, item.fromWalletName, item.walletName));

                tvBadgeDetail.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_transfer));
                tvBadgeDetail.setText(getString(R.string.transfer));
                tvBadgeDetail.setTextColor(ContextCompat.getColor(this, R.color.transfer));
            } else {

                if (transaction.description == null || transaction.description.isEmpty()) {
                    detailLabel.setVisibility(View.GONE);

                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvBadgeDetail.getLayoutParams();
                    params.setMarginStart(0);
                    tvBadgeDetail.setLayoutParams(params);
                } else {
                    detailLabel.setVisibility(View.VISIBLE);
                    detailLabel.setText(transaction.description);

                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvBadgeDetail.getLayoutParams();
                    params.setMarginStart(getResources().getDimensionPixelSize(R.dimen.margin_8));
                    tvBadgeDetail.setLayoutParams(params);
                }

                tvBadgeDetail.setBackgroundDrawable(badge);
                tvBadgeDetail.setText(badgeText);
                tvBadgeDetail.setTextColor(color);
            }

            if (feeTransaction != null) {
                feeTransaction.amount = feeTransaction.amount * -1;
                feeLabel.setText(feeTransaction.getCategoryName(this));
                feeAmountLabel.setText(CommonUtils.getBeautifyAmount(item.currencySymbol, feeTransaction.amount));
                feeAmountLabel.setTextColor(ContextCompat.getColor(this, R.color.expense));

                tvBadgeFee.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_badge_expense));
                tvBadgeFee.setText(getString(R.string.expense));
                tvBadgeFee.setTextColor(ContextCompat.getColor(this, R.color.expense));

                tvBadgeFee.setVisibility(View.VISIBLE);
                feeLabel.setVisibility(View.VISIBLE);
                feeAmountLabel.setVisibility(View.VISIBLE);
            } else {

                tvBadgeFee.setVisibility(View.GONE);
                feeLabel.setVisibility(View.GONE);
                feeAmountLabel.setVisibility(View.GONE);
            }

            bottomView.findViewById(R.id.optionEdit).setOnClickListener(v -> {
                dialog.dismiss();
                onEdit.run();
            });

            bottomView.findViewById(R.id.optionDuplicate).setOnClickListener(v -> {
                dialog.dismiss();
                onDuplicate.run();
            });

            if(isDetails) {
                bottomView.findViewById(R.id.optionViewDetails).setVisibility(View.GONE);
                bottomView.findViewById(R.id.viewViewDetails).setVisibility(View.GONE);
            } else {
                bottomView.findViewById(R.id.optionViewDetails).setVisibility(View.VISIBLE);
                bottomView.findViewById(R.id.viewViewDetails).setVisibility(View.VISIBLE);

                bottomView.findViewById(R.id.optionViewDetails).setOnClickListener(v -> {
                    dialog.dismiss();
                    onViewDetails.run();
                });
            }



            bottomView.findViewById(R.id.optionDelete).setOnClickListener(v -> {
                dialog.dismiss();
                onDelete.run();
            });

            dialog.setContentView(bottomView);
            dialog.show();
        } catch (Exception e) {
            AppLogger.e(getClass(), "showOptionDialog", e);
        }
    }
}