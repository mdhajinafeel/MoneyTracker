package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.CalculatorHelper;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;

import java.math.BigDecimal;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CalculatorActivity extends BaseActivity implements View.OnClickListener {

    private AppCompatImageView icBack;
    private AppCompatButton one, two, three, four, five, six, seven, eight, nine, zero, dZero, clear, divide, multiply, minus, plus, equal, dot;
    private AppCompatImageButton escape, done;
    private AppCompatTextView total;
    private String equation, type;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);

            tvTitle.setText(R.string.enter_amount);

            ViewCompat.setOnApplyWindowInsetsListener(toolbarWrapper, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                v.setPadding(v.getPaddingLeft(), top,
                        v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.wrapper),
                    (view, insets) -> {
                        int bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), bottomInset);
                        return insets;
                    });

            total = findViewById(R.id.tvTotal);
            one = findViewById(R.id.one);
            two = findViewById(R.id.two);
            three = findViewById(R.id.three);
            four = findViewById(R.id.four);
            five = findViewById(R.id.five);
            six = findViewById(R.id.six);
            seven = findViewById(R.id.seven);
            eight = findViewById(R.id.eight);
            nine = findViewById(R.id.nine);
            zero = findViewById(R.id.zero);
            dZero = findViewById(R.id.dZero);

            clear = findViewById(R.id.clear);
            divide = findViewById(R.id.divide);
            multiply = findViewById(R.id.multiply);
            escape = findViewById(R.id.escape);
            minus = findViewById(R.id.minus);
            plus = findViewById(R.id.plus);
            equal = findViewById(R.id.equal);
            dot = findViewById(R.id.dot);
            done = findViewById(R.id.done);

            equation = CalculatorHelper.getPlainAmount(BigDecimal.valueOf(getIntent().getDoubleExtra("amount", 0.0)));
            type = getIntent().getStringExtra("type");

            if (equation == null) {
                equation = "0";
            }

            backPressed();
            clickListeners();

            total.setText(CalculatorHelper.getDisplayAmount(equation));
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void clickListeners() {
        try {
            icBack.setOnClickListener(view -> {
                finish();
                ActivityUtils.overrideCloseTransition(this, R.anim.scale_in, R.anim.right_to_left);
            });

            one.setOnClickListener(this);
            two.setOnClickListener(this);
            three.setOnClickListener(this);
            four.setOnClickListener(this);
            five.setOnClickListener(this);
            six.setOnClickListener(this);
            seven.setOnClickListener(this);
            eight.setOnClickListener(this);
            nine.setOnClickListener(this);
            zero.setOnClickListener(this);
            dZero.setOnClickListener(this);
            clear.setOnClickListener(this);
            divide.setOnClickListener(this);
            multiply.setOnClickListener(this);
            plus.setOnClickListener(this);
            minus.setOnClickListener(this);
            dot.setOnClickListener(this);
            done.setOnClickListener(this);
            equal.setOnClickListener(this);
            escape.setOnClickListener(this);

            ActivityUtils.overrideCloseTransition(this, R.anim.slide_in_left, R.anim.slide_out_right);
        } catch (Exception e) {
            AppLogger.e(getClass(), "clickListeners", e);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.clear) {
            clear();
        } else if (view.getId() == R.id.divide) {
            divide();
        } else if (view.getId() == R.id.done) {
            done();
        } else if (view.getId() == R.id.dot) {
            dot();
        } else if (view.getId() == R.id.equal) {
            equal();
        } else if (view.getId() == R.id.escape) {
            escape();
        } else if (view.getId() == R.id.minus) {
            minus();
        } else if (view.getId() == R.id.multiply) {
            multiply();
        } else if (view.getId() == R.id.plus) {
            plus();
        } else {
            if (view.getTag() != null) {
                digit((String) view.getTag());
            }
        }
    }

    private void divide() {
        String validateDivide = CalculatorHelper.validateDivide(equation);
        equation = validateDivide;
        total.setText(CalculatorHelper.getFormattedNumber(validateDivide));
    }

    private void multiply() {
        String validateMultiply = CalculatorHelper.validateMultiply(equation);
        equation = validateMultiply;
        total.setText(CalculatorHelper.getFormattedNumber(validateMultiply));
    }

    private void plus() {
        String validatePlus = CalculatorHelper.validatePlus(equation);
        equation = validatePlus;
        total.setText(CalculatorHelper.getFormattedNumber(validatePlus));
    }

    private void minus() {
        String validateMinus = CalculatorHelper.validateMinus(equation);
        equation = validateMinus;
        total.setText(CalculatorHelper.getFormattedNumber(validateMinus));
    }

    private void clear() {
        equation = "0";
        total.setText(CalculatorHelper.getFormattedNumber("0"));
    }

    private void equal() {
        String validateEqual = CalculatorHelper.validateEqual(getApplicationContext(), equation);
        equation = validateEqual;
        total.setText(CalculatorHelper.getFormattedNumber(validateEqual));
    }

    private void done() {
        equal();

        Intent intent = new Intent();
        double amount;

        try {
            amount = Double.parseDouble(equation);
        } catch (Exception e) {
            amount = 0L;
        }

        intent.putExtra("amount", amount);
        intent.putExtra("type", type);
        setResult(-1, intent);
        finish();
    }

    private void escape() {
        String validateEscape = CalculatorHelper.validateEscape(equation);
        equation = validateEscape;
        total.setText(CalculatorHelper.getFormattedNumber(validateEscape));
    }

    private void dot() {
        String validateDot = CalculatorHelper.validateDot(equation);
        equation = validateDot;
        total.setText(CalculatorHelper.getFormattedNumber(validateDot));
    }

    private void digit(String s) {
        String validateDigit = CalculatorHelper.validateDigit(s, equation, false);
        equation = validateDigit;
        total.setText(CalculatorHelper.getFormattedNumber(validateDigit));
    }

    private void backPressed() {
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        finish();
                        ActivityUtils.overrideCloseTransition(CalculatorActivity.this, R.anim.scale_in, R.anim.right_to_left);
                    }
                });
    }
}