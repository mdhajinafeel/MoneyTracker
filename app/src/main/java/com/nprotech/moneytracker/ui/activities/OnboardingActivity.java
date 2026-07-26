package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.models.OnboardingModel;
import com.nprotech.moneytracker.ui.adapters.OnboardingAdapter;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;

import java.util.ArrayList;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;
import me.relex.circleindicator.CircleIndicator3;

@AndroidEntryPoint
public class OnboardingActivity extends BaseActivity {

    private ViewPager2 viewPager;
    private CircleIndicator3 indicator;
    private AppCompatTextView btnNext, txtSkip;
    private OnboardingAdapter onboardingAdapter;
    private ArrayList<OnboardingModel> onboardingList;
    private boolean isNavigating = false;
    private int currentPage = 0;
    private ViewPager2.OnPageChangeCallback pageCallback;
    private static final int INTENT_FLAGS = Intent.FLAG_ACTIVITY_NEW_TASK
            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            | Intent.FLAG_ACTIVITY_CLEAR_TOP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        statusBarDarkSetting();
        hideKeyboard(this);

        initComponents();

        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt("current_page", 0);
            viewPager.setCurrentItem(currentPage, false);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_page", viewPager.getCurrentItem());
    }

    private void initComponents() {
        try {
            viewPager = findViewById(R.id.viewPager);
            indicator = findViewById(R.id.indicator);
            btnNext = findViewById(R.id.btnNext);
            txtSkip = findViewById(R.id.txtSkip);

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutBottom), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, 0, 0, systemBars.bottom);
                return insets;
            });

            if (PreferenceManager.INSTANCE.getOnBoardingCompleted()) {
                openNextScreen();
                return;
            }

            loadData();
            setupViewPager();
            setupListeners();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void loadData() {

        onboardingList = new ArrayList<>();
        onboardingList.add(new OnboardingModel(R.drawable.walkthrough_1, getString(R.string.walkthrough_1_title), getString(R.string.walkthrough_1_desc)));
        onboardingList.add(new OnboardingModel(R.drawable.walkthrough_2, getString(R.string.walkthrough_2_title), getString(R.string.walkthrough_2_desc)));
        onboardingList.add(new OnboardingModel(R.drawable.walkthrough_3, getString(R.string.walkthrough_3_title), getString(R.string.walkthrough_3_desc)));
        onboardingList.add(new OnboardingModel(R.drawable.walkthrough_4, getString(R.string.walkthrough_4_title), getString(R.string.walkthrough_4_desc)));
        onboardingList.add(new OnboardingModel(R.drawable.walkthrough_5, getString(R.string.walkthrough_5_title), getString(R.string.walkthrough_5_desc)));
        onboardingList.add(new OnboardingModel(R.drawable.walkthrough_6, getString(R.string.walkthrough_6_title), getString(R.string.walkthrough_6_desc)));
    }

    private void setupViewPager() {
        onboardingAdapter = new OnboardingAdapter(this, onboardingList);
        viewPager.setAdapter(onboardingAdapter);
        indicator.setViewPager(viewPager);

        viewPager.setOffscreenPageLimit(3);
        viewPager.setUserInputEnabled(true);
        RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        recyclerView.setPadding(0, 0, 0, 0);
        recyclerView.setClipToPadding(false);

        viewPager.setPageTransformer((page, position) -> {

            float absPos = Math.abs(position);

            float scale = 0.90f + (1 - absPos) * 0.10f;
            page.setScaleX(scale);
            page.setScaleY(scale);

            float alpha = 0.60f + (1 - absPos) * 0.40f;
            page.setAlpha(alpha);

            page.setTranslationX(-position * 40f);
        });

        pageCallback = new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
                if (position == onboardingAdapter.getItemCount() - 1) {
                    btnNext.setText(R.string.get_started);
                    txtSkip.setVisibility(View.INVISIBLE);
                } else {
                    btnNext.setText(R.string.next);
                    txtSkip.setVisibility(View.VISIBLE);
                }
                viewPager.post(() -> animateCurrentPage());
            }
        };

        viewPager.registerOnPageChangeCallback(pageCallback);

        viewPager.post(this::animateCurrentPage);
    }

    private void openNextScreen() {
        boolean isAccountExists = PreferenceManager.INSTANCE.getAccountId() > 0;
        Intent intent;
        if (isAccountExists) {
            intent = new Intent(this, MainActivity.class)
                    .putExtra("isFromLogin", false);
        } else {
            intent = new Intent(this, AddAccountActivity.class);
        }

        intent.addFlags(INTENT_FLAGS);
        startActivity(intent);
        ActivityUtils.overrideOpenTransition(this, android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void animateCurrentPage() {

        RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);

        View page = Objects.requireNonNull(recyclerView.getLayoutManager()).findViewByPosition(viewPager.getCurrentItem());

        if (page == null) return;

        View image = page.findViewById(R.id.imgOnboarding);
        View title = page.findViewById(R.id.txtTitle);
        View description = page.findViewById(R.id.txtDescription);
        View button = page.findViewById(R.id.btnNext);

        animateView(image, 0);
        animateView(title, 100);
        animateView(description, 200);
        animateView(button, 300);
    }

    private void animateView(View view, long delay) {

        if (view == null) return;

        view.setAlpha(0f);
        view.setTranslationY(40f);

        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(350)
                .start();
    }

    private void setupListeners() {

        btnNext.setOnClickListener(v -> {
            if (isNavigating)
                return;
            isNavigating = true;
            int position = viewPager.getCurrentItem();
            if (position < onboardingAdapter.getItemCount() - 1) {
                viewPager.setCurrentItem(position + 1, true);
                btnNext.postDelayed(() -> isNavigating = false, 300);
            } else {
                PreferenceManager.INSTANCE.setOnBoardingCompleted(true);
                openNextScreen();
            }
        });

        txtSkip.setOnClickListener(v -> {
            if (isNavigating)
                return;
            isNavigating = true;
            PreferenceManager.INSTANCE.setOnBoardingCompleted(true);
            openNextScreen();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (pageCallback != null) {
            viewPager.unregisterOnPageChangeCallback(pageCallback);
        }
    }
}