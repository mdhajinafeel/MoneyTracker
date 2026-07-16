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
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.IConstants;
import com.nprotech.moneytracker.db.entites.AccountEntity;
import com.nprotech.moneytracker.db.entites.CategoryEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.db.entites.WalletEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.TransactionCategoryModel;
import com.nprotech.moneytracker.models.TransactionTypeAmountModel;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.adapters.WalletTransactionAdapter;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.CategoryViewModel;
import com.nprotech.moneytracker.viewmodel.TransactionViewModel;
import com.nprotech.moneytracker.viewmodel.WalletViewModel;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WalletTransactionDetailedActivity extends BaseActivity {

    private ConstraintLayout imageWrapper;
    private LinearLayout nameWrapper, layoutEmpty;
    private AppCompatImageView icBack, imageView;
    private AppCompatTextView nameLabel, amountLabel, initialLabel, incomeLabel, expenseLabel, transferLabel, transactionAllLabel;
    private MaterialButton btnAdjustBalance;
    private RecyclerView rvTransactions;
    private CategoryViewModel categoryViewModel;
    private WalletViewModel walletViewModel;
    private TransactionViewModel transactionViewModel;
    private AccountViewModel accountViewModel;
    private WalletTransactionAdapter walletTransactionAdapter;
    private int walletId = 0;
    private LiveData<List<TransactionCategoryModel>> transactionCategoryLiveData;
    private ActivityResultLauncher<Intent> calculatorLauncher;
    private WalletEntity wallet;

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
            nameLabel = findViewById(R.id.nameLabel);
            amountLabel = findViewById(R.id.amountLabel);
            initialLabel = findViewById(R.id.initialLabel);
            incomeLabel = findViewById(R.id.incomeLabel);
            expenseLabel = findViewById(R.id.expenseLabel);
            transferLabel = findViewById(R.id.transferLabel);
            btnAdjustBalance = findViewById(R.id.btnAdjustBalance);
            rvTransactions = findViewById(R.id.rvTransactions);
            transactionAllLabel = findViewById(R.id.transactionAllLabel);
            layoutEmpty = findViewById(R.id.layoutEmpty);

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

            ViewCompat.setOnApplyWindowInsetsListener(scrollView, (view, insets) -> {
                Insets navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
                Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

                int bottom = Math.max(navInsets.bottom, imeInsets.bottom);

                view.setPadding(
                        view.getPaddingLeft(),
                        view.getPaddingTop(),
                        view.getPaddingRight(),
                        bottom + getResources().getDimensionPixelSize(R.dimen.bottom_navigation_height)
                );

                return insets;
            });

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {

                categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
                accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
                walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);
                transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

                walletId = bundle.getInt("walletId");

                if (walletId > 0) {
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
            rvTransactions.setLayoutManager(new LinearLayoutManager(this));
            walletTransactionAdapter = new WalletTransactionAdapter(WalletTransactionDetailedActivity.this, new ArrayList<>());
            rvTransactions.setAdapter(walletTransactionAdapter);
            rvTransactions.setNestedScrollingEnabled(false);
        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    private void bindData(int walletId) {
        try {

            double incomeAmount = 0, expenseAmount = 0, transferAmount = 0;

            walletViewModel.selectAccount((int) PreferenceManager.INSTANCE.getAccountId());

            wallet = walletViewModel.getWalletByWalletId(walletId);
            if (wallet == null) {
                return;
            }

            List<TransactionTypeAmountModel> transactionTypeAmountModelList = transactionViewModel.getTransactionAmountByType(walletId);

            if (transactionTypeAmountModelList != null && !transactionTypeAmountModelList.isEmpty()) {
                for (TransactionTypeAmountModel typeAmountModel : transactionTypeAmountModelList) {
                    if (typeAmountModel.getType() == 1) {
                        incomeAmount = typeAmountModel.getAmount();
                    } else if (typeAmountModel.getType() == 2) {
                        expenseAmount = typeAmountModel.getAmount();
                    } else {
                        transferAmount = typeAmountModel.getAmount();
                    }
                }
            }

            // Remove previous observer
            if (transactionCategoryLiveData != null) {
                transactionCategoryLiveData.removeObservers(this);
            }

            // Observe current wallet transactions
            transactionCategoryLiveData = transactionViewModel.getTransactionAmountByCategory(walletId);

            transactionCategoryLiveData.observe(this, transactionCategoryModels -> {
                if (transactionCategoryModels == null || transactionCategoryModels.isEmpty()) {
                    rvTransactions.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                    transactionAllLabel.setVisibility(View.GONE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    rvTransactions.setVisibility(View.VISIBLE);
                    transactionAllLabel.setVisibility(View.VISIBLE);

                    walletTransactionAdapter.setItems(transactionCategoryModels);
                }
            });

            int walletIcon = DataHelper.getWalletIcons().get(wallet.categoryIcon);

            if (Build.VERSION.SDK_INT >= 29) {
                imageWrapper.getBackground().setColorFilter(new BlendModeColorFilter(Color.parseColor(wallet.walletColor), BlendMode.SRC_OVER));
            } else {
                Drawable drawable = imageWrapper.getBackground().mutate();
                DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_OVER);
                DrawableCompat.setTint(drawable, Color.parseColor(wallet.walletColor));
                imageWrapper.setBackground(drawable);
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
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            });

            btnAdjustBalance.setOnClickListener(view -> {
                hideKeyboard(this);
                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", wallet.amount);
                intent.putExtra("type", "amount");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.left_to_right, R.anim.scale_out);
                calculatorLauncher.launch(intent, options);
            });

            nameWrapper.setOnClickListener(view -> switchWallets());

            getOnBackPressedDispatcher().addCallback(this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            finish();
                            ActivityUtils.overrideCloseTransition(WalletTransactionDetailedActivity.this, R.anim.scale_in, R.anim.right_to_left);
                        }
                    });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void switchWallets() {
        try {

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View bottomView = getLayoutInflater().inflate(R.layout.bottom_wallet_picker_layout, findViewById(android.R.id.content), false);
            RecyclerView rvWallets = bottomView.findViewById(R.id.rvWallets);
            LinearLayout layoutAddWallet = bottomView.findViewById(R.id.layoutAddWallet);

            walletViewModel.getWallets().observe(this, walletEntities -> {
                RecyclerViewAdapter<WalletEntity> adapter = new RecyclerViewAdapter<>(getApplicationContext(), walletEntities, R.layout.item_switch_accounts) {
                    @Override
                    public void onPostBindViewHolder(ViewHolder holder, WalletEntity wallet) {

                        holder.setViewText(R.id.tvAccountName, wallet.name);

                        holder.setViewText(R.id.tvAccountBalance, getString(R.string.account_balance_format,
                                CommonUtils.getBeautifyAmount(wallet.currencySymbol, wallet.amount)));

                        holder.getView(R.id.ivSelected).setVisibility(walletId == wallet.id ? View.VISIBLE : View.GONE);

                        holder.getView(R.id.rlAccountView).setOnClickListener(v -> {
                            walletId = wallet.id;
                            bindData(walletId);
                            dialog.dismiss();
                        });
                    }
                };

                rvWallets.setAdapter(adapter);
                rvWallets.setHasFixedSize(true);
            });

            layoutAddWallet.setOnClickListener(v -> {
                startActivity(new Intent(WalletTransactionDetailedActivity.this, CreateWalletActivity.class)
                        .putExtra("isEdit", false));
                ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            dialog.setContentView(bottomView);
            dialog.show();

        } catch (Exception e) {
            AppLogger.e(getClass(), "switchAccounts", e);
        }
    }

    private void setupLauncher() {
        try {
            calculatorLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                double amount = data.getDoubleExtra("amount", 0);
                                String type = data.getStringExtra("type");
                                if (type != null && type.equalsIgnoreCase("amount")) {
                                    if (amount != wallet.amount) {
                                        showAdjustBalanceDialog(amount);
                                    }
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupLauncher", e);
        }
    }

    private void showAdjustBalanceDialog(double actualBalance) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_adjust_balance, null, false);

        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView amountLabel = view.findViewById(R.id.amountLabel);
        ConstraintLayout topWrapper = view.findViewById(R.id.topWrapper);
        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        AppCompatRadioButton rbAdjustTransaction = view.findViewById(R.id.rbAdjustTransaction);
        AppCompatRadioButton rbChangeInitial = view.findViewById(R.id.rbChangeInitial);

        dialog.setView(view);

        double difference = actualBalance - wallet.amount;
        amountLabel.setText(CommonUtils.getBeautifyAmount(wallet.currencySymbol, difference));

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbAdjustTransaction) {
                tvMessage.setText(getString(R.string.adjust_by_transaction_hint));
                topWrapper.setVisibility(View.VISIBLE);
            } else {
                tvMessage.setText(getString(R.string.change_initial_amount_hint));
                topWrapper.setVisibility(View.GONE);
            }
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvOk).setOnClickListener(v -> {

            if (rbAdjustTransaction.isChecked()) {
                adjustWalletBalance(wallet, actualBalance);
            } else if (rbChangeInitial.isChecked()) {
                changeInitialAmount(wallet, actualBalance);
            }
            dialog.dismiss();
            bindData(walletId);
        });

        dialog.show();
    }

    private void adjustWalletBalance(WalletEntity wallet, double actualBalance) {

        double difference = actualBalance - wallet.amount;

        if (difference == 0) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        TransactionEntity transaction = new TransactionEntity();
        transaction.walletId = wallet.id;
        transaction.amount = Math.abs(difference);
        transaction.transactionDate = System.currentTimeMillis();
        transaction.description = getString(R.string.adjustment);
        transaction.tempTransactionServerId = "T_"+ currentTime;
        transaction.accountId = PreferenceManager.INSTANCE.getAccountId();
        transaction.createdAt = currentTime;
        transaction.updatedAt = currentTime;
        transaction.isSynced = false;
        transaction.isDeleted = false;

        if (difference > 0) {
            transaction.type = TransactionEntity.TYPE_INCOME;

            CategoryEntity category = categoryViewModel.getDefaultCategoryByType(IConstants.DEFAULT_CATEGORY_ID, 1);
            transaction.categoryId = category.id;
            transaction.defaultCategoryId = category.defaultCategory;
        } else {
            transaction.type = TransactionEntity.TYPE_EXPENSE;

            CategoryEntity category = categoryViewModel.getDefaultCategoryByType(IConstants.DEFAULT_CATEGORY_ID, 2);
            transaction.categoryId = category.id;
            transaction.defaultCategoryId = category.defaultCategory;
        }

        // Update wallet balance
        wallet.amount = actualBalance;

        // Update account balance
        AccountEntity account = accountViewModel.getAccountDetailById(wallet.accountId);
        if (account != null) {
            account.balance += difference;
        }

        transactionViewModel.saveTransaction(transaction, wallet, account);
    }

    private void changeInitialAmount(WalletEntity wallet, double actualBalance) {

        double difference = actualBalance - wallet.amount;

        if (difference == 0) {
            return;
        }

        wallet.initialAmount += difference;
        wallet.amount = actualBalance;

        AccountEntity account = accountViewModel.getAccountDetailById(wallet.accountId);
        if (account != null) {
            account.balance += difference;
            walletViewModel.updateWalletAndAccount(wallet, account);
        } else {
            walletViewModel.updateWallet(wallet);
        }
    }
}