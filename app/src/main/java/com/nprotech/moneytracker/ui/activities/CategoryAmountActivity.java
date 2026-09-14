package com.nprotech.moneytracker.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.CategoryEntity;
import com.nprotech.moneytracker.db.entites.TransactionEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.ui.adapters.RecyclerViewAdapter;
import com.nprotech.moneytracker.ui.adapters.ViewHolder;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.ui.common.MaxHeightRecyclerView;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.CommonUtils;
import com.nprotech.moneytracker.utils.IntentUtils;
import com.nprotech.moneytracker.viewmodel.CategoryViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CategoryAmountActivity extends BaseActivity {

    private MaxHeightRecyclerView rvCategories;
    private ConstraintLayout addCategoryContainer;
    private AppCompatImageView icBack;
    private AppCompatTextView tvSave, tvTotalBudget, tvTotalCategories;
    private View categoryRoot;
    private final Set<Integer> selectedCategoryIds = new HashSet<>();
    private final List<CategoryEntity> selectedCategory = new ArrayList<>();
    private final List<CategoryEntity> allCategories = new ArrayList<>();
    private String currencySymbol = "";
    private ActivityResultLauncher<Intent> calculatorLauncher, categoryPickerLauncher;
    private final Map<Integer, Double> categoryAmounts = new HashMap<>();
    private int editingCategoryId = -1;
    private CategoryViewModel categoryViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_amount);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            tvSave = toolbarWrapper.findViewById(R.id.tvSave);
            icBack = toolbarWrapper.findViewById(R.id.icBack);

            tvTitle.setText(getString(R.string.set_category_amount));

            categoryRoot = findViewById(R.id.categoryRoot);
            rvCategories = findViewById(R.id.rvCategories);
            tvTotalBudget = findViewById(R.id.tvTotalBudget);
            tvTotalCategories = findViewById(R.id.tvTotalCategories);
            addCategoryContainer = findViewById(R.id.addCategoryContainer);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(categoryRoot, (view, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), systemBars.bottom);
                return insets;
            });

            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {

                categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
                currencySymbol = bundle.getString("currencySymbol", "");

                categoryViewModel.incomeCategory(TransactionEntity.TYPE_EXPENSE, true);

                tvSave.setVisibility(View.VISIBLE);
                tvSave.setText(getString(R.string.done));
                tvSave.setEnabled(false);
                tvSave.setAlpha(0.5f);

                selectedCategoryIds.clear();
                ArrayList<Integer> categoryIds = bundle.getIntegerArrayList("categoryIds");
                if (categoryIds != null) {
                    selectedCategoryIds.addAll(categoryIds);
                }

                @SuppressWarnings("unchecked")
                HashMap<Integer, Double> amounts = IntentUtils.getSerializableExtra(getIntent(), "categoryAmounts", HashMap.class);

                if (amounts != null) {
                    categoryAmounts.putAll(amounts);
                }

                categoryViewModel.getIncomeCategories().observe(this, categoryEntities -> {
                    if (!categoryEntities.isEmpty()) {
                        allCategories.clear();
                        allCategories.addAll(categoryEntities);

                        selectedCategory.clear();
                        for (CategoryEntity category : categoryEntities) {
                            if (selectedCategoryIds.contains(category.id)) {
                                selectedCategory.add(category);
                            }
                        }

                        bindCategories(selectedCategory);
                        int count = selectedCategory.size();
                        tvTotalCategories.setText(getResources().getQuantityString(R.plurals.category_amount_count, count, count));
                        CommonUtils.setDrawable(this, tvTotalCategories, R.drawable.ic_separate_method, R.dimen.icon_12, R.color.primary, Gravity.START);
                        rvCategories.setVisibility(View.VISIBLE);
                        updateDoneButtonState();
                    } else {
                        rvCategories.setVisibility(View.GONE);
                    }
                });

                setupListeners();
                setupLaunchers();
                updateTotalBudget();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finish();
                ActivityUtils.overrideCloseTransition(CategoryAmountActivity.this, R.anim.slide_in_left, R.anim.slide_out_right);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindCategories(List<CategoryEntity> expenseCategories) {
        try {

            RecyclerViewAdapter<CategoryEntity> expenseCategoryAdapter = new RecyclerViewAdapter<>(this,
                    expenseCategories, R.layout.item_category_amount) {
                @Override
                public void onPostBindViewHolder(ViewHolder holder, CategoryEntity categoryEntity) {

                    AppCompatTextView tvCategory = holder.getView(R.id.tvCategory);
                    AppCompatTextView tvCategoryAmount = holder.getView(R.id.tvCategoryAmount);
                    AppCompatImageView ivDelete = holder.getView(R.id.ivDelete);

                    holder.setViewImageResource(R.id.ivCategory, DataHelper.getCategoryIcons().get(categoryEntity.icon));
                    tvCategory.setText(categoryEntity.getName(getApplicationContext()));

                    Double amountValue = categoryAmounts.get(categoryEntity.id);
                    double amount = amountValue != null ? amountValue : 0.0;
                    tvCategoryAmount.setText(CommonUtils.getBeautifyAmount(currencySymbol, amount));

                    if (Build.VERSION.SDK_INT >= 29) {
                        holder.getView(R.id.colorView).getBackground().setColorFilter(new BlendModeColorFilter(Color.parseColor(categoryEntity.color), BlendMode.SRC_OVER));
                    } else {

                        Drawable drawable = holder.getView(R.id.colorView).getBackground().mutate();
                        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_OVER);
                        DrawableCompat.setTint(drawable, Color.parseColor(categoryEntity.color));
                        holder.getView(R.id.colorView).setBackground(drawable);
                    }

                    tvCategoryAmount.setOnClickListener(v -> {
                        editingCategoryId = categoryEntity.id;
                        Intent intent = new Intent(CategoryAmountActivity.this, CalculatorActivity.class);
                        intent.putExtra("type", "amount");
                        Double amtValue = categoryAmounts.get(categoryEntity.id);
                        double currentAmount = amtValue != null ? amtValue : 0.0;
                        intent.putExtra("amount", currentAmount);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(), R.anim.left_to_right, R.anim.scale_out);
                        calculatorLauncher.launch(intent, options);
                    });

                    ivDelete.setOnClickListener(v -> {
                        int categoryId = categoryEntity.id;

                        selectedCategoryIds.remove(categoryId);
                        categoryAmounts.remove(categoryId);
                        selectedCategory.remove(categoryEntity);

                        bindCategories(selectedCategory);

                        updateTotalBudget();

                        int count = selectedCategory.size();
                        tvTotalCategories.setText(getResources().getQuantityString(R.plurals.category_amount_count, count, count));
                        updateDoneButtonState();
                        updateRecyclerViewMaxHeight();
                    });
                }
            };

            rvCategories.setAdapter(expenseCategoryAdapter);
            rvCategories.setHasFixedSize(true);
            updateRecyclerViewMaxHeight();

            expenseCategoryAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    updateRecyclerViewMaxHeight();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    updateRecyclerViewMaxHeight();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    updateRecyclerViewMaxHeight();
                }
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindCategories", e);
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(CategoryAmountActivity.this, R.anim.scale_in, R.anim.right_to_left);
            });

            getOnBackPressedDispatcher().addCallback(this,
                    new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            finish();
                            ActivityUtils.overrideCloseTransition(CategoryAmountActivity.this, R.anim.scale_in, R.anim.right_to_left);
                        }
                    });

            tvSave.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.putIntegerArrayListExtra("categoryIds", new ArrayList<>(selectedCategoryIds));
                intent.putExtra("categoryAmounts", new HashMap<>(categoryAmounts));
                intent.putExtra("isAllCategory", selectedCategoryIds.size() == allCategories.size());
                setResult(-1, intent);
                finish();
                ActivityUtils.overrideCloseTransition(CategoryAmountActivity.this, R.anim.slide_in_left, R.anim.slide_out_right);
            });

            addCategoryContainer.setOnClickListener(v -> {
                Intent intent = new Intent(CategoryAmountActivity.this, CategoryPickerActivity.class);
                intent.putIntegerArrayListExtra("categoryIds", new ArrayList<>(selectedCategoryIds));
                intent.putExtra("transactionType", TransactionEntity.TYPE_EXPENSE);
                intent.putExtra("isFromScreen", "budget");
                categoryPickerLauncher.launch(intent);
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupLaunchers() {
        calculatorLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {

                    String type = data.getStringExtra("type");
                    if (!"amount".equalsIgnoreCase(type)) {
                        return;
                    }

                    double amount = data.getDoubleExtra("amount", 0);
                    if (editingCategoryId == -1) {
                        return;
                    }
                    categoryAmounts.put(editingCategoryId, amount);
                    if (rvCategories.getAdapter() != null) {
                        rvCategories.getAdapter().notifyDataSetChanged();
                    }

                    updateTotalBudget();
                    updateDoneButtonState();
                    editingCategoryId = -1;
                }
            }
        });

        categoryPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                ArrayList<Integer> categoryIds = result.getData().getIntegerArrayListExtra("categoryIds");

                if (categoryIds != null) {
                    selectedCategoryIds.clear();
                    selectedCategoryIds.addAll(categoryIds);

                    Iterator<Integer> iterator = categoryAmounts.keySet().iterator();
                    while (iterator.hasNext()) {
                        Integer id = iterator.next();
                        if (!selectedCategoryIds.contains(id)) {
                            iterator.remove();
                        }
                    }

                    categoryViewModel.incomeCategory(TransactionEntity.TYPE_EXPENSE, true);
                }
            }
        });
    }

    private void updateTotalBudget() {
        double totalBudget = 0;
        for (Double amount : categoryAmounts.values()) {
            if (amount != null) {
                totalBudget += amount;
            }
        }
        tvTotalBudget.setText(CommonUtils.getBeautifyAmount(currencySymbol, totalBudget));
    }

    private void updateDoneButtonState() {
        boolean allAmountsEntered = !selectedCategory.isEmpty();

        for (Integer categoryId : selectedCategoryIds) {
            Double amount = categoryAmounts.get(categoryId);

            if (amount == null || amount <= 0) {
                allAmountsEntered = false;
                break;
            }
        }

        tvSave.setEnabled(allAmountsEntered);
        tvSave.setAlpha(allAmountsEntered ? 1.0f : 0.5f);
    }

    private void updateRecyclerViewMaxHeight() {

        categoryRoot.post(() -> {

            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            View bottomDetailContainer = findViewById(R.id.bottomDetailContainer);
            View addCategoryContainer = findViewById(R.id.addCategoryContainer);

            int rootHeight = categoryRoot.getHeight();

            int toolbarHeight = toolbarWrapper.getHeight();

            int bottomDetailHeight = bottomDetailContainer.getHeight();

            int addCategoryHeight = addCategoryContainer.getHeight();

            int topMargin = CommonUtils.dpToPx(this, 10);
            int cardBottomMargin = CommonUtils.dpToPx(this, 10);
            int addTopMargin = CommonUtils.dpToPx(this, 12);
            int bottomSpace = CommonUtils.dpToPx(this, 12);

            int bottomInset = categoryRoot.getPaddingBottom();

            int availableHeight =
                    rootHeight
                            - toolbarHeight
                            - bottomDetailHeight
                            - addCategoryHeight
                            - topMargin
                            - cardBottomMargin
                            - addTopMargin
                            - bottomSpace
                            - bottomInset;

            if (availableHeight > 0) {
                rvCategories.setMaxHeight(availableHeight);
            }
        });
    }
}