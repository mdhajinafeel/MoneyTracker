package com.nprotech.moneytracker.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.DateHelper;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.viewmodel.ProfilePhotoViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileAccountDetailActivity extends BaseActivity {

    private AppCompatImageView icBack, ivEdit;
    private AppCompatTextView tvFullName, tvEmailAddress, tvMemberSince, tvAccountStatus, tvLoaderTitle;
    private ShapeableImageView profilePhoto;
    private MaterialCardView cardEditProfilePhoto;
    private ProfilePhotoViewModel profilePhotoViewModel;
    private ActivityResultLauncher<String> profilePhotoPicker;
    private View screenLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_account_details);
        statusBarSetting();
        hideKeyboard(this);
        initComponents();
    }

    private void initComponents() {
        try {
            View root = findViewById(R.id.rootView);
            View toolbarWrapper = findViewById(R.id.toolbarWrapper);
            AppCompatTextView tvTitle = toolbarWrapper.findViewById(R.id.tvTitle);
            icBack = toolbarWrapper.findViewById(R.id.icBack);
            ivEdit = toolbarWrapper.findViewById(R.id.ivEdit);
            tvFullName = findViewById(R.id.tvFullName);
            tvEmailAddress = findViewById(R.id.tvEmailAddress);
            tvMemberSince = findViewById(R.id.tvMemberSince);
            tvAccountStatus = findViewById(R.id.tvAccountStatus);
            profilePhoto = findViewById(R.id.profilePhoto);
            cardEditProfilePhoto = findViewById(R.id.cardEditProfilePhoto);
            screenLoader = findViewById(R.id.screenLoader);
            tvLoaderTitle = findViewById(R.id.tvLoaderTitle);

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

            profilePhotoViewModel = new ViewModelProvider(this).get(ProfilePhotoViewModel.class);

            tvTitle.setText(getString(R.string.account_details));

            bindData();
            setupListeners();
            setupProfilePhotoObserver();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {
            tvFullName.setText(PreferenceManager.INSTANCE.getAuthFullName());
            tvEmailAddress.setText(PreferenceManager.INSTANCE.getAuthEmail());

            if(!PreferenceManager.INSTANCE.getAuthCreatedAt().equals(0L)) {
                tvMemberSince.setText(DateHelper.getFormattedDate(PreferenceManager.INSTANCE.getAuthCreatedAt(), "dd MMM yyyy"));
            } else {
                tvMemberSince.setText("-");
            }

            if(PreferenceManager.INSTANCE.getAuthGoogle()) {
                ivEdit.setVisibility(View.GONE);
            } else {
                ivEdit.setVisibility(View.VISIBLE);
            }

            if(PreferenceManager.INSTANCE.getAuthAccountStatus().equalsIgnoreCase("active")) {
                tvAccountStatus.setText(getString(R.string.active));
            }

            Glide.with(ProfileAccountDetailActivity.this)
                    .load(PreferenceManager.INSTANCE.getAuthProfilePhoto())
                    .placeholder(R.drawable.ic_profile_avatar)
                    .error(R.drawable.ic_profile_avatar)
                    .circleCrop()
                    .into(profilePhoto);

            profilePhotoPicker = registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            profilePhotoViewModel.uploadProfilePhoto(uri);
                        }
                    }
            );
        } catch (Exception e) {
            AppLogger.e(getClass(), "bindData", e);
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

            cardEditProfilePhoto.setOnClickListener(v -> profilePhotoPicker.launch("image/*"));

            ivEdit.setOnClickListener(v -> {
                startActivity(new Intent(ProfileAccountDetailActivity.this, ProfileEditDetailActivity.class));
                ActivityUtils.overrideOpenTransition(ProfileAccountDetailActivity.this, R.anim.top_to_bottom, R.anim.scale_out);
            });
        } catch (Exception e) {
            AppLogger.e(getClass(), "setupListeners", e);
        }
    }

    private void setupProfilePhotoObserver() {
        profilePhotoViewModel.getUploadResult().observe(this, result -> {
            if (result == null) {
                return;
            }

            switch (result.status) {

                case LOADING:
                    screenLoader.setVisibility(View.VISIBLE);
                    tvLoaderTitle.setText(R.string.uploading_photo);
                    cardEditProfilePhoto.setEnabled(false);
                    break;

                case SUCCESS:
                    screenLoader.setVisibility(View.GONE);
                    cardEditProfilePhoto.setEnabled(true);

                    Glide.with(this)
                            .load(result.photoUrl)
                            .placeholder(R.drawable.ic_profile_avatar)
                            .error(R.drawable.ic_profile_avatar)
                            .circleCrop()
                            .into(profilePhoto);

                    PreferenceManager.INSTANCE.setAuthProfilePhoto(result.photoUrl);
                    Toast.makeText(this, R.string.profile_photo_updated, Toast.LENGTH_SHORT).show();
                    break;

                case ERROR:
                    screenLoader.setVisibility(View.GONE);
                    cardEditProfilePhoto.setEnabled(true);
                    Toast.makeText(this, result.message != null ? result.message :
                            getString(R.string.could_not_upload_profile_photo), Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(ProfileAccountDetailActivity.this, R.anim.scale_in, R.anim.right_to_left);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindData();
    }
}