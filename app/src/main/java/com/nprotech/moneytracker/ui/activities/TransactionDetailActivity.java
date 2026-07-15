package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.CurrencyEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.models.TransactionWithDetails;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.utils.IntentUtils;

public class TransactionDetailActivity extends BaseActivity {

    private ConstraintLayout colorView;
    private AppCompatImageView imageView;
    private AppCompatTextView nameLabel, categoryLabel, amountLabel, dateLabel, walletLabel, typeLabel, feeTitleLabel, feeLabel, memoTitleLabel, memoLabel;
    private AppCompatImageView icBack, ivDelete, ivEdit;
    private TransactionWithDetails transactionWithDetails;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            ivDelete = toolbarWrapper.findViewById(R.id.ivDelete);
            ivEdit = toolbarWrapper.findViewById(R.id.ivEdit);

            ivEdit.setVisibility(View.VISIBLE);
            ivDelete.setVisibility(View.VISIBLE);

            tvTitle.setText(R.string.detail);
            colorView = findViewById(R.id.colorView);
            imageView = findViewById(R.id.imageView);
            nameLabel = findViewById(R.id.nameLabel);
            categoryLabel = findViewById(R.id.categoryLabel);
            amountLabel = findViewById(R.id.amountLabel);
            dateLabel = findViewById(R.id.dateLabel);
            walletLabel = findViewById(R.id.walletLabel);
            typeLabel = findViewById(R.id.typeLabel);
            feeTitleLabel = findViewById(R.id.feeTitleLabel);
            feeLabel = findViewById(R.id.feeLabel);
            memoTitleLabel = findViewById(R.id.memoTitleLabel);
            memoLabel = findViewById(R.id.memoLabel);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                transactionWithDetails = IntentUtils.getSerializableExtra(getIntent(), "transactionDetail", TransactionWithDetails.class);

                bindData(transactionWithDetails);
                setupListeners();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData(TransactionWithDetails transactionWithDetail) {
        try {
            TransactionEntity transaction = transactionWithDetail.transaction;

            if (Build.VERSION.SDK_INT >= 29) {
                colorView.getBackground().setColorFilter(new BlendModeColorFilter(Color.parseColor(transactionWithDetail.color), BlendMode.SRC_OVER));
            } else {
                Drawable drawable = colorView.getBackground().mutate();
                DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_OVER);
                DrawableCompat.setTint(drawable, Color.parseColor(transactionWithDetail.color));
                colorView.setBackground(drawable);
            }

            if (transaction.type == 3) {
                imageView.setImageResource(R.drawable.ic_transfer);
            } else {
                if (transactionWithDetail.icon == null || transactionWithDetail.icon == 0) {
                    imageView.setImageResource(R.drawable.category_0);
                } else {
                    imageView.setImageResource(DataHelper.getCategoryIcons().get(transactionWithDetail.icon));
                }
            }

            nameLabel.setText(transaction.description == null || transaction.description.isEmpty() ? "---" : transaction.description);
            categoryLabel.setText(transaction.getCategoryName(getApplicationContext()));
            amountLabel.setText(CommonUtils.getBeautifyAmount(transactionWithDetail.currencySymbol, transaction.amount));
            dateLabel.setText(DateHelper.getFormattedDateTime(getApplicationContext(), transaction.transactionDate));

            String wallet = transactionWithDetail.walletName;
            walletLabel.setText(wallet);

            String type = getString(R.string.transfer);
            if (transaction.type == 1) {
                type = getString(R.string.income);
            } else if (transaction.type == 2) {
                type = getString(R.string.expense);
            }
            typeLabel.setText(type);

            if (transaction.type == 3) {
                feeTitleLabel.setVisibility(View.VISIBLE);
                feeLabel.setVisibility(View.VISIBLE);
                feeLabel.setText(CommonUtils.getBeautifyAmount(transactionWithDetail.currencySymbol, transaction.fee));

                if (transaction.memo != null && !transaction.memo.isEmpty()) {
                    memoTitleLabel.setVisibility(View.VISIBLE);
                    memoLabel.setVisibility(View.VISIBLE);

                    memoLabel.setText(transaction.memo);
                }
            } else {
                feeTitleLabel.setVisibility(View.GONE);
                feeLabel.setVisibility(View.GONE);
                memoTitleLabel.setVisibility(View.GONE);
                memoLabel.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.bottom_to_top);
            });

            ivEdit.setOnClickListener(view -> {
                startActivity(new Intent(this, CreateTransactionActivity.class)
                        .putExtra("transactionDetail", transactionWithDetails)
                        .putExtra("type", transactionWithDetails.transaction.type)
                        .putExtra("action", "edit"));
                ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            ivDelete.setOnClickListener(view -> {
                Toast.makeText(getApplicationContext(), transactionWithDetails.transaction.tempTransactionServerId, Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }
}