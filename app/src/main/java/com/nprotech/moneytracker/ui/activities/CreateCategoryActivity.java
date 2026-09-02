package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.db.entites.CategoryEntity;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DataHelper;
import com.nprotech.moneytracker.ui.adapters.ColorSpinnerAdapter;
import com.nprotech.moneytracker.ui.adapters.FontSpinnerAdapter;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.viewmodel.CategoryViewModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateCategoryActivity extends BaseActivity {

    private AppCompatImageView icBack, ivCategoryIcon;
    private AppCompatTextView tvTitle, tvSave, tvStatus, statusDescLabel;
    private AppCompatEditText etCategoryName;
    private AppCompatSpinner typeSpinner, colorSpinner;
    private SwitchCompat switchStatus, switchIncludeView;
    private ArrayList<String> categoryColorLists;
    private int categoryIcon = 0, categoryId = 0, defaultCategory, type = 1;
    private boolean isEdit = false;
    private ActivityResultLauncher<Intent> categoryIconLauncher;
    private CategoryEntity categoryEntity;
    private CategoryViewModel categoryViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_category);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            View rootView = findViewById(R.id.rootView);
            tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            tvSave = toolbarWrapper.findViewById(R.id.tvSave);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            etCategoryName = findViewById(R.id.etCategoryName);
            typeSpinner = findViewById(R.id.typeSpinner);
            colorSpinner = findViewById(R.id.colorSpinner);
            ivCategoryIcon = findViewById(R.id.ivCategoryIcon);
            switchIncludeView = findViewById(R.id.switchIncludeView);
            tvStatus = findViewById(R.id.tvStatus);
            statusDescLabel = findViewById(R.id.statusDescLabel);
            switchStatus = findViewById(R.id.switchStatus);

            tvSave.setVisibility(View.VISIBLE);

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

            categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                type = bundle.getInt("selectedType");
                isEdit = bundle.getBoolean("isEdit");
                categoryId = bundle.getInt("categoryId", 0);

                initializeAdapters();
                bindData(isEdit);
                setupLauncher();
                setupListeners();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.parsing_error), Toast.LENGTH_SHORT).show();
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData(boolean isEdit) {
        try {
            if (isEdit) {
                tvTitle.setText(getString(R.string.edit_category));
                tvSave.setText(getString(R.string.update));

                categoryEntity = categoryViewModel.getCategoryById(categoryId, false);

                if (categoryEntity != null) {

                    defaultCategory = categoryEntity.defaultCategory;
                    String categoryName = categoryEntity.name.trim();
                    if (defaultCategory > 0) {
                        if(!categoryName.isEmpty()) {
                            etCategoryName.setText(categoryName);
                        }else {
                            etCategoryName.setText(DataHelper.getDefaultCategory(this, defaultCategory));
                        }

                        typeSpinner.setEnabled(false);
                        typeSpinner.setAlpha(0.5f);
                    } else {
                        etCategoryName.setText(categoryName);
                        typeSpinner.setEnabled(true);
                        typeSpinner.setAlpha(1f);
                    }

                    typeSpinner.setSelection(categoryEntity.type - 1);

                    updateStatus(categoryEntity.active);

                    int colorPosition = categoryColorLists.indexOf(categoryEntity.color);
                    if (colorPosition >= 0) colorSpinner.setSelection(colorPosition);

                    categoryIcon = categoryEntity.icon;
                    ivCategoryIcon.setImageResource(DataHelper.getCategoryIcons().get(categoryIcon));

                    switchIncludeView.setChecked(categoryEntity.isIncludeReport);
                }
            } else {
                tvTitle.setText(getString(R.string.add_category));
                categoryIcon = 102;
                categoryId = 0;
                typeSpinner.setSelection(type - 1);
                updateStatus(true);
            }

            updateSaveButtonState();
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void updateStatus(boolean isActive) {
        if(isActive) {
            tvStatus.setText(getString(R.string.active));
            statusDescLabel.setText(getString(R.string.status_active_hint));
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.dark_income));
            switchStatus.setChecked(true);
        } else {
            tvStatus.setText(R.string.inactive);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.expense));
            statusDescLabel.setText(getString(R.string.status_inactive_hint));
            switchStatus.setChecked(false);
        }
    }

    private void initializeAdapters() {
        try {
            List<String> walletTypes = Arrays.asList(getString(R.string.income), getString(R.string.expense));

            FontSpinnerAdapter fontSpinnerAdapter = new FontSpinnerAdapter(this, R.layout.list_drop_down_color, R.id.label, walletTypes);
            typeSpinner.setAdapter(fontSpinnerAdapter);

            categoryColorLists = new ArrayList<>();
            categoryColorLists = DataHelper.getCategoryColorList();
            ColorSpinnerAdapter colorSpinnerAdapter = new ColorSpinnerAdapter(this, R.layout.list_drop_down_color, R.id.label, categoryColorLists);
            colorSpinner.setAdapter(colorSpinnerAdapter);

            try {
                Field popupField = AppCompatSpinner.class.getDeclaredField("mPopup");
                popupField.setAccessible(true);

                ListPopupWindow popup = (ListPopupWindow) popupField.get(colorSpinner);
                if (popup != null) {
                    popup.setHeight(getResources().getDimensionPixelSize(R.dimen.spinner_dropdown_max_height));
                }
            } catch (Exception e) {
                AppLogger.e(getClass(), "ListPopupWindow", e);
            }

        } catch (Exception e) {
            AppLogger.e(getClass(), "initializeAdapters", e);
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> finishWithTransition());

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finishWithTransition();
                }
            });

            etCategoryName.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {

                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    updateSaveButtonState();
                }
            });

            ivCategoryIcon.setOnClickListener(view -> {
                hideKeyboard(this);
                Intent intent = new Intent(this, IconPickerActivity.class);
                intent.putExtra("selectedColor", categoryColorLists.get(colorSpinner.getSelectedItemPosition()));
                intent.putExtra("iconType", "category");
                intent.putExtra("selectedIcon", categoryIcon);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.left_to_right, R.anim.scale_out);
                categoryIconLauncher.launch(intent, options);
            });

            tvSave.setOnClickListener(v -> saveCategory());
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void setupLauncher() {

        categoryIconLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            int selectedCategoryIcon = data.getIntExtra("categoryIcon", 0);
                            ivCategoryIcon.setImageResource(DataHelper.getCategoryIcons().get(selectedCategoryIcon));
                            categoryIcon = selectedCategoryIcon;
                        }
                    }
                });
    }

    private void updateSaveButtonState() {
        boolean enabled = !Objects.requireNonNull(etCategoryName.getText()).toString().isEmpty();
        tvSave.setEnabled(enabled);
        tvSave.setAlpha(enabled ? 1.0f : 0.5f); // Optional: make disabled state visible
    }

    private void saveCategory() {
        try {

            int selectedType = typeSpinner.getSelectedItemPosition() + 1;
            String name = Objects.requireNonNull(etCategoryName.getText()).toString().trim();

            if (isEdit) {

                categoryEntity.name = name;
                categoryEntity.color = categoryColorLists.get(colorSpinner.getSelectedItemPosition());
                categoryEntity.type = typeSpinner.getSelectedItemPosition() + 1;
                categoryEntity.active = switchStatus.isChecked();
                categoryEntity.icon = categoryIcon;
                categoryEntity.defaultCategory = defaultCategory;
                categoryEntity.isDeleted = false;
                categoryEntity.isIncludeReport = switchIncludeView.isChecked();
                categoryEntity.updatedAt = System.currentTimeMillis();

                categoryViewModel.updateCategory(categoryEntity);
                Toast.makeText(getApplicationContext(), getString(R.string.category_updated), Toast.LENGTH_SHORT).show();
            } else {

                CategoryEntity category = new CategoryEntity();
                category.name = name;
                category.color = categoryColorLists.get(colorSpinner.getSelectedItemPosition());
                category.type = typeSpinner.getSelectedItemPosition() + 1;
                category.active = switchStatus.isChecked();
                category.ordering = categoryViewModel.getMaxOrder(selectedType);
                category.icon = categoryIcon;
                category.defaultCategory = 0;
                category.isDeleted = false;
                category.isIncludeReport = switchIncludeView.isChecked();
                category.updatedAt = System.currentTimeMillis();

                categoryViewModel.saveCategory(category);
                Toast.makeText(getApplicationContext(), getString(R.string.category_created), Toast.LENGTH_SHORT).show();
            }

            setResult(RESULT_OK);
            finish();
            ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
        } catch (Exception e) {
            AppLogger.e(getClass(), "saveCategory", e);
        }
    }

    private void finishWithTransition() {
        finish();
        ActivityUtils.overrideCloseTransition(CreateCategoryActivity.this, R.anim.scale_in, R.anim.right_to_left);
    }
}