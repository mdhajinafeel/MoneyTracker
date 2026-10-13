package com.nprotech.moneytracker.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.utils.PinCredentialStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingPinActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView tvPinHeading, tvCreatePinHint;
    private MaterialButton btnCreatePin, btnRemovePin;
    private String enteredPin = "";
    private String firstPin = "";
    private boolean savingPin = false;
    private final List<View> pinDots = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final int STAGE_CURRENT_PIN = 0;
    private static final int STAGE_NEW_PIN = 1;
    private static final int STAGE_CONFIRM_PIN = 2;
    private int currentStage = STAGE_NEW_PIN;
    private boolean isChangeMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_create_pin);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            View rootView = findViewById(R.id.rootView);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            btnCreatePin = findViewById(R.id.btnCreatePin);
            btnRemovePin = findViewById(R.id.btnRemovePin);
            tvPinHeading = findViewById(R.id.tvPinHeading);
            tvCreatePinHint = findViewById(R.id.tvCreatePinHint);
            pinDots.add(findViewById(R.id.pinDot1));
            pinDots.add(findViewById(R.id.pinDot2));
            pinDots.add(findViewById(R.id.pinDot3));
            pinDots.add(findViewById(R.id.pinDot4));
            pinDots.add(findViewById(R.id.pinDot5));
            pinDots.add(findViewById(R.id.pinDot6));

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

            tvTitle.setText(R.string.text_pin);

            bindData();
            setupListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {
            isChangeMode = PinCredentialStore.isPinSet();
            btnRemovePin.setVisibility(isChangeMode ? View.VISIBLE : View.GONE);
            currentStage = isChangeMode ? STAGE_CURRENT_PIN : STAGE_NEW_PIN;
            tvCreatePinHint.setText(getString(R.string.create_pin_hint, getString(R.string.app_name)));
            updatePinScreen();
            enteredPin = "";
            firstPin = "";
            savingPin = false;
            updatePinDots();
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
        }
    }

    private void updatePinScreen() {
        switch (currentStage) {
            case STAGE_CURRENT_PIN:
                tvPinHeading.setText(R.string.enter_current_pin);
                btnCreatePin.setText(R.string.verify_pin);
                tvCreatePinHint.setText(R.string.enter_current_pin_hint);
                break;
            case STAGE_NEW_PIN:
                tvPinHeading.setText(R.string.create_digit_pin);
                btnCreatePin.setText(R.string.create_pin);
                tvCreatePinHint.setText(R.string.create_pin_hint);
                break;
            case STAGE_CONFIRM_PIN:
                tvPinHeading.setText(R.string.confirm_digit_pin);
                btnCreatePin.setText(R.string.confirm_pin);
                tvCreatePinHint.setText(R.string.confirm_pin_hint);
                break;
        }
    }

    private void setupListeners() {
        try {
            icBack.setOnClickListener(view -> finishWithTransitions());

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finishWithTransitions();
                }
            });

            int[] keyIds = {
                    R.id.key0, R.id.key1, R.id.key2,
                    R.id.key3, R.id.key4, R.id.key5,
                    R.id.key6, R.id.key7, R.id.key8,
                    R.id.key9
            };

            for (int id : keyIds) {
                View key = findViewById(id);

                if (key != null) {
                    key.setOnClickListener(v -> {
                        if (savingPin || enteredPin.length() >= 6) {
                            return;
                        }

                        Object tag = v.getTag();
                        String digit;

                        if (tag != null) {
                            digit = tag.toString();
                        } else {
                            // Read the digit from the key's child TextView.
                            if (v instanceof MaterialCardView) {
                                android.view.View child = ((MaterialCardView) v).getChildAt(0);
                                if (!(child instanceof AppCompatTextView)) {
                                    return;
                                }

                                digit = ((AppCompatTextView) child).getText().toString();
                            } else {
                                return;
                            }
                        }

                        if (digit.matches("\\d")) {
                            enteredPin += digit;
                            updatePinDots();
                        }
                    });
                }
            }

            View deleteKey = findViewById(R.id.keyDelete);
            deleteKey.setOnClickListener(v -> {
                if (savingPin || enteredPin.isEmpty()) {
                    return;
                }

                enteredPin = enteredPin.substring(0, enteredPin.length() - 1);
                updatePinDots();
            });

            btnCreatePin.setOnClickListener(v -> createPin());

            btnRemovePin.setOnClickListener(v -> showRemovePinDialog());
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void updatePinDots() {
        int enteredCount = enteredPin.length();

        for (int i = 0; i < pinDots.size(); i++) {
            View dot = pinDots.get(i);
            dot.setBackgroundResource(i < enteredCount ? R.drawable.bg_pin_dot_filled : R.drawable.bg_pin_dot);
        }

        boolean complete = enteredCount == 6;
        btnCreatePin.setEnabled(complete && !savingPin);
        btnCreatePin.setAlpha(complete && !savingPin ? 1f : 0.4f);
    }

    private void createPin() {
        try {
            if (savingPin) {
                return;
            }

            if (enteredPin.length() != 6) {
                Toast.makeText(this, R.string.enter_pin_digits, Toast.LENGTH_SHORT).show();
                return;
            }

            String pin = enteredPin;

            // Step 1: Verify the existing PIN.
            if (currentStage == STAGE_CURRENT_PIN) {
                verifyCurrentPin(pin);
                return;
            }

            // Step 2: Collect the new PIN.
            if (currentStage == STAGE_NEW_PIN) {
                firstPin = pin;
                enteredPin = "";
                currentStage = STAGE_CONFIRM_PIN;
                updatePinScreen();
                updatePinDots();
                return;
            }

            // Step 3: Confirm the new PIN.
            if (currentStage == STAGE_CONFIRM_PIN) {
                if (!firstPin.equals(pin)) {
                    firstPin = "";
                    enteredPin = "";
                    currentStage = STAGE_NEW_PIN;
                    updatePinScreen();
                    updatePinDots();
                    Toast.makeText(this, R.string.pins_do_not_match, Toast.LENGTH_LONG).show();
                    return;
                }
                savePin(pin);
            }
        } catch (Exception e) {
            AppLogger.e(getClass(), "createPin", e);
        }
    }

    private void verifyCurrentPin(String pin) {
        savingPin = true;
        btnCreatePin.setEnabled(false);
        btnCreatePin.setAlpha(0.4f);
        btnCreatePin.setText(R.string.verifying_pin);
        btnRemovePin.setVisibility(View.GONE);
        executor.execute(() -> {
            boolean isValid = false;
            Exception error = null;

            try {
                isValid = PinCredentialStore.verifyPin(pin);
            } catch (Exception e) {
                error = e;
            }

            final boolean verified = isValid;
            final Exception verificationError = error;

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                savingPin = false;

                if (verificationError != null) {
                    AppLogger.e(getClass(), "verifyCurrentPin", verificationError);
                    enteredPin = "";
                    updatePinScreen();
                    updatePinDots();
                    Toast.makeText(this, R.string.unable_verify_pin, Toast.LENGTH_LONG).show();
                    return;
                }

                if (!verified) {
                    enteredPin = "";
                    updatePinDots();
                    Toast.makeText(this, R.string.incorrect_current_pin, Toast.LENGTH_LONG).show();
                    return;
                }

                // Current PIN is correct. Allow the user to create a new PIN.
                enteredPin = "";
                firstPin = "";
                currentStage = STAGE_NEW_PIN;

                updatePinScreen();
                updatePinDots();
            });
        });
    }

    private void savePin(String pin) {
        savingPin = true;
        btnCreatePin.setEnabled(false);
        btnCreatePin.setAlpha(0.4f);
        btnCreatePin.setText(R.string.text_saving);

        executor.execute(() -> {
            Exception error = null;

            try {
                PinCredentialStore.savePin(pin);
            } catch (Exception e) {
                error = e;
            }

            Exception finalError = error;

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                savingPin = false;

                if (finalError != null) {
                    AppLogger.e(getClass(), "savePin", finalError);
                    btnCreatePin.setText(currentStage == STAGE_CONFIRM_PIN ? getString(R.string.confirm_pin) : getString(R.string.create_pin));
                    updatePinDots();
                    Toast.makeText(this, R.string.unable_save_pin, Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(this, isChangeMode ? R.string.pin_changed : R.string.pin_created, Toast.LENGTH_SHORT).show();
                finishWithTransitions();
            });
        });
    }

    private void showRemovePinDialog() {

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_confirmation, null, false);

        AppCompatTextView tvTitle = view.findViewById(R.id.tvTitle);
        AppCompatTextView tvMessage = view.findViewById(R.id.tvMessage);
        AppCompatTextView tvSubMessage = view.findViewById(R.id.tvSubMessage);
        MaterialButton tvDelete = view.findViewById(R.id.tvDelete);

        tvDelete.setText(getString(R.string.text_remove));
        tvTitle.setText(getString(R.string.remove_pin));
        tvMessage.setText(R.string.remove_pin_message);
        tvSubMessage.setVisibility(View.GONE);

        dialog.setView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        view.findViewById(R.id.tvCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.tvDelete).setOnClickListener(v -> {
            removePin();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void removePin() {
        try {
            PinCredentialStore.removePin();
            Toast.makeText(this, R.string.pin_removed, Toast.LENGTH_SHORT).show();
            finishWithTransitions();
        } catch (Exception e) {
            AppLogger.e(getClass(), "removePin", e);
        }
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(SettingPinActivity.this, R.anim.scale_in, R.anim.right_to_left);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}