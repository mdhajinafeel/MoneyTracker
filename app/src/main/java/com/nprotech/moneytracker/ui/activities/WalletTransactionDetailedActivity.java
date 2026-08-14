package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
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
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.constants.Constants;
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
import com.nprotech.moneytracker.utils.SimpleDividerItemDecoration;
import com.nprotech.moneytracker.viewmodel.AccountViewModel;
import com.nprotech.moneytracker.viewmodel.CategoryViewModel;
import com.nprotech.moneytracker.viewmodel.TransactionViewModel;
import com.nprotech.moneytracker.viewmodel.WalletViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;
import me.grantland.widget.AutofitTextView;

@AndroidEntryPoint
public class WalletTransactionDetailedActivity extends BaseActivity {

    private ConstraintLayout walletContainer;
    private MaterialCardView walletTransactionCard;
    private AppCompatImageView icBack, ivEdit, imageView;
    private AppCompatTextView tvWalletName, tvWalletType, tvAvailableBalance, transactionAllLabel;
    private MaterialButton btnAdjustBalance;
    private AutofitTextView tvInitial, tvIncome, tvExpense, tvTransfer;
    private LinearLayout layoutEmpty;
    private FloatingActionButton fabAddTransaction;
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
            View root = findViewById(R.id.rootView);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            ivEdit = toolbarWrapper.findViewById(R.id.ivEdit);
            walletContainer = findViewById(R.id.walletContainer);
            imageView = findViewById(R.id.imageView);
            tvWalletName = findViewById(R.id.tvWalletName);
            tvWalletType = findViewById(R.id.tvWalletType);
            tvAvailableBalance = findViewById(R.id.tvAvailableBalance);
            tvInitial = findViewById(R.id.tvInitial);
            tvIncome = findViewById(R.id.tvIncome);
            tvExpense = findViewById(R.id.tvExpense);
            tvTransfer = findViewById(R.id.tvTransfer);
            btnAdjustBalance = findViewById(R.id.btnAdjustBalance);
            rvTransactions = findViewById(R.id.rvTransactions);
            transactionAllLabel = findViewById(R.id.transactionAllLabel);
            layoutEmpty = findViewById(R.id.layoutEmpty);
            walletTransactionCard = findViewById(R.id.walletTransactionCard);
            fabAddTransaction = findViewById(R.id.fabAddTransaction);

            tvTitle.setText(R.string.wallet_details);
            ivEdit.setVisibility(View.VISIBLE);

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
                    finish();
                    ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
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
            rvTransactions.addItemDecoration(new SimpleDividerItemDecoration(this));
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
                    walletTransactionCard.setVisibility(View.GONE);
                    rvTransactions.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                    transactionAllLabel.setVisibility(View.GONE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    walletTransactionCard.setVisibility(View.VISIBLE);
                    rvTransactions.setVisibility(View.VISIBLE);
                    transactionAllLabel.setVisibility(View.VISIBLE);

                    walletTransactionAdapter.setItems(transactionCategoryModels);
                }
            });

            int walletIcon = DataHelper.getWalletIcons().get(wallet.categoryIcon);

            walletContainer.setBackground(CommonUtils.createGradient(getApplicationContext(), wallet.walletColor, 12));

            imageView.setBackground(CommonUtils.createIconBackground(getApplicationContext(), wallet.walletColor, GradientDrawable.RECTANGLE, 10));
            imageView.setImageResource(walletIcon);

            tvWalletName.setText(wallet.name);
            tvWalletType.setText(DataHelper.getWalletTypeName(getApplicationContext(), wallet.walletType));
            tvAvailableBalance.setText(CommonUtils.getBeautifyAmount(wallet.currencySymbol, wallet.amount));
            tvInitial.setText(CommonUtils.getBeautifyAmount(wallet.currencySymbol, wallet.initialAmount));
            tvIncome.setText(CommonUtils.getBeautifyAmount(wallet.currencySymbol, incomeAmount));
            tvExpense.setText(CommonUtils.getBeautifyAmount(wallet.currencySymbol, expenseAmount));
            tvTransfer.setText(CommonUtils.getBeautifyAmount(wallet.currencySymbol, transferAmount));
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

            ivEdit.setOnClickListener(view -> {
                startActivity(new Intent(WalletTransactionDetailedActivity.this, CreateWalletActivity.class)
                        .putExtra("isEdit", true)
                        .putExtra("walletId", walletId));
                ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            btnAdjustBalance.setOnClickListener(view -> {
                hideKeyboard(this);
                Intent intent = new Intent(this, CalculatorActivity.class);
                intent.putExtra("amount", wallet.amount);
                intent.putExtra("type", "amount");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.left_to_right, R.anim.scale_out);
                calculatorLauncher.launch(intent, options);
            });

            tvWalletName.setOnClickListener(view -> switchWallets());

            transactionAllLabel.setOnClickListener(view -> {
                Intent intent = new Intent(WalletTransactionDetailedActivity.this, CategoryTransactionActivity.class);
                intent.putExtra("type", 0);
                intent.putExtra("categoryId", 0);
                intent.putExtra("walletId", walletId);
                intent.putExtra("categoryName", "");
                startActivity(intent);

                ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
            });

            fabAddTransaction.setOnClickListener(v -> {
                v.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(120)
                        .withEndAction(() ->
                                v.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(120)
                                        .start())
                        .start();

                startActivity(new Intent(WalletTransactionDetailedActivity.this, CreateTransactionActivity.class)
                        .putExtra("action", "add")
                        .putExtra("type", TransactionEntity.TYPE_EXPENSE));
                ActivityUtils.overrideOpenTransition(this, R.anim.top_to_bottom, R.anim.scale_out);
            });

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

    private void showAdjustBalanceDialog(double actualBalance) {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_adjust_balance, null, false);

        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView amountLabel = view.findViewById(R.id.amountLabel);
        ConstraintLayout topWrapper = view.findViewById(R.id.topWrapper);
        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        AppCompatRadioButton rbAdjustTransaction = view.findViewById(R.id.rbAdjustTransaction);
        AppCompatRadioButton rbChangeInitial = view.findViewById(R.id.rbChangeInitial);
        AppCompatEditText etReason = view.findViewById(R.id.etReason);

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
                adjustWalletBalance(wallet, actualBalance, Objects.requireNonNull(etReason.getText()).toString().trim());
            } else if (rbChangeInitial.isChecked()) {
                changeInitialAmount(wallet, actualBalance);
            }
            dialog.dismiss();
            bindData(walletId);
        });

        dialog.show();
    }

    public void adjustWalletBalance(WalletEntity wallet, double enteredBalance, String note) {

        double currentBalance = wallet.amount;
        double difference = enteredBalance - currentBalance;
        long currentTime = System.currentTimeMillis();

        if (difference == 0) {
            return;
        }

        // Update wallet balance
        wallet.amount = enteredBalance;
        walletViewModel.updateWallet(wallet);

        // Update account balance
        AccountEntity account = accountViewModel.getAccountDetailById(wallet.accountId);
        account.balance = (account.balance + difference);
        accountViewModel.updateAccount(account);

        // Save adjustment transaction
        TransactionEntity transaction = new TransactionEntity();
        transaction.walletId = wallet.id;
        transaction.amount = Math.abs(difference);
        transaction.transactionDate = currentTime;
        transaction.description = getString(R.string.adjustment);
        transaction.tempTransactionServerId = "T_" + currentTime;
        transaction.accountId = PreferenceManager.INSTANCE.getAccountId();
        transaction.createdAt = currentTime;
        transaction.updatedAt = currentTime;

        if (difference > 0) {
            transaction.type = TransactionEntity.TYPE_INCOME;

            CategoryEntity category = categoryViewModel.getDefaultCategoryByType(Constants.DEFAULT_CATEGORY_ADJUST_ID, TransactionEntity.TYPE_INCOME);
            transaction.categoryId = category.id;
            transaction.defaultCategoryId = category.defaultCategory;

        } else {
            transaction.type = TransactionEntity.TYPE_EXPENSE;

            CategoryEntity category = categoryViewModel.getDefaultCategoryByType(Constants.DEFAULT_CATEGORY_ADJUST_ID, TransactionEntity.TYPE_EXPENSE);
            transaction.categoryId = category.id;
            transaction.defaultCategoryId = category.defaultCategory;
        }

        transaction.description = getString(R.string.adjustment);

        if (note != null) {
            transaction.memo = note;
        }

        transactionViewModel.saveTransaction(transaction);
    }

    public void changeInitialAmount(WalletEntity wallet, double newInitialAmount) {

        double oldInitialAmount = wallet.initialAmount;
        double difference = newInitialAmount - oldInitialAmount;

        if (difference == 0) {
            return;
        }

        // Update initial amount
        wallet.initialAmount = newInitialAmount;

        // Update wallet balance
        wallet.amount = (wallet.amount + difference);
        walletViewModel.updateWallet(wallet);

        // Update account balance
        AccountEntity account = accountViewModel.getAccountDetailById(wallet.accountId);
        account.balance = (account.balance + difference);
        accountViewModel.updateAccount(account);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindData(walletId);
    }
}