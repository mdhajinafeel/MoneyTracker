package com.nprotech.moneytracker.ui.activities;

import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.models.TransactionTypeAmountModel;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.TransactionViewModel;
import com.nprotech.moneytracker.viewmodel.WalletViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WalletTransactionDetailedActivity extends BaseActivity {

    private ConstraintLayout imageWrapper, llTransactionHeader;
    private LinearLayout nameWrapper;
    private AppCompatImageView icBack, imageView;
    private AppCompatTextView nameLabel, amountLabel, initialLabel, incomeLabel, expenseLabel, transferLabel;
    private MaterialButton btnAdjustBalance;
    private RecyclerView rvTransactions;
    private WalletViewModel walletViewModel;
    private TransactionViewModel transactionViewModel;
    private double incomeAmount = 0, expenseAmount = 0, transferAmount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_transaction_detail);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            NestedScrollView scrollView = findViewById(R.id.scrollView);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            imageWrapper = findViewById(R.id.imageWrapper);
            imageView = findViewById(R.id.imageView);
            nameWrapper = findViewById(R.id.nameWrapper);
            llTransactionHeader = findViewById(R.id.llTransactionHeader);
            nameLabel = findViewById(R.id.nameLabel);
            amountLabel = findViewById(R.id.amountLabel);
            initialLabel = findViewById(R.id.initialLabel);
            incomeLabel = findViewById(R.id.incomeLabel);
            expenseLabel = findViewById(R.id.expenseLabel);
            transferLabel = findViewById(R.id.transferLabel);
            btnAdjustBalance = findViewById(R.id.btnAdjustBalance);
            rvTransactions = findViewById(R.id.rvTransactions);

            tvTitle.setText(R.string.wallet_details);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(scrollView, (view, insets) -> {
                Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
                view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), imeInsets.bottom);
                return insets;
            });

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {

                walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);
                transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

                int walletId = bundle.getInt("walletId");

                if(walletId > 0) {
                    initializeAdapters();
                    bindData(walletId);
                    setupLauncher();
                    setupListeners();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void initializeAdapters() {
        try {

        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    private void bindData(int walletId) {
        try {

            WalletEntity wallet = walletViewModel.getWalletByWalletId(walletId);
            List<TransactionTypeAmountModel> transactionTypeAmountModelList = transactionViewModel.getTransactionAmountByType(walletId);

            if(transactionTypeAmountModelList != null && !transactionTypeAmountModelList.isEmpty()) {
                for (TransactionTypeAmountModel typeAmountModel : transactionTypeAmountModelList) {
                    if(typeAmountModel.getType() == 1) {
                        incomeAmount = typeAmountModel.getAmount();
                    } else if(typeAmountModel.getType() == 2) {
                        expenseAmount = typeAmountModel.getAmount();
                    } else {
                        transferAmount = typeAmountModel.getAmount();
                    }
                }
            }

            int walletIcon = DataHelper.getWalletIcons().get(wallet.categoryIcon);

            if (Build.VERSION.SDK_INT >= 29) {
                imageWrapper.getBackground().setColorFilter(new BlendModeColorFilter(Color.parseColor(wallet.walletColor), BlendMode.SRC_OVER));
            } else {
                imageWrapper.getBackground().setColorFilter(Color.parseColor(wallet.walletColor), PorterDuff.Mode.SRC_OVER);
            }

            imageView.setImageResource(walletIcon);
            nameLabel.setText(wallet.name);
            amountLabel.setText(CommonUtils.getBeautifyAmount(wallet.currencySymbol, wallet.amount));
            initialLabel.setText(CommonUtils.getBeautifyAmount(wallet.currencySymbol, wallet.initialAmount));
            incomeLabel.setText(CommonUtils.getBeautifyAmount(wallet.currencySymbol, incomeAmount));
            expenseLabel.setText(CommonUtils.getBeautifyAmount(wallet.currencySymbol, expenseAmount));
            transferLabel.setText(CommonUtils.getBeautifyAmount(wallet.currencySymbol, transferAmount));
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                overridePendingTransition(R.anim.scale_in, R.anim.right_to_left);
            });

            nameWrapper.setOnClickListener(view -> {

            });

            getOnBackPressedDispatcher().addCallback(this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            finish();
                            overridePendingTransition(R.anim.scale_in, R.anim.right_to_left);
                        }
                    });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void setupLauncher() {
        try {

        } catch (Exception e) {
            AppLogger.e(getClass(), "setupLauncher", e);
        }
    }
}