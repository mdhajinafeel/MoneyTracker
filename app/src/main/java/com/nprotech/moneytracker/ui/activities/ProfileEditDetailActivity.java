package com.nprotech.moneytracker.ui.activities;

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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.helper.AppLogger;
import com.nprotech.moneytracker.helper.PreferenceManager;
import com.nprotech.moneytracker.ui.common.BaseActivity;
import com.nprotech.moneytracker.utils.ActivityUtils;
import com.nprotech.moneytracker.viewmodel.ProfileDetailsViewModel;
import com.nprotech.moneytracker.viewmodel.ProfilePhotoViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileEditDetailActivity extends BaseActivity {

    private AppCompatImageView icBack;
    private AppCompatTextView tvLoaderTitle;
    private TextInputEditText etFullName;
    private ShapeableImageView profilePhoto;
    private MaterialCardView cardEditProfilePhoto;
    private ProfilePhotoViewModel profilePhotoViewModel;
    private ProfileDetailsViewModel profileDetailsViewModel;
    private ActivityResultLauncher<String> profilePhotoPicker;
    private MaterialButton btnUpdateDetail;
    private View screenLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit_details);
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
            etFullName = findViewById(R.id.etFullName);
            profilePhoto = findViewById(R.id.profilePhoto);
            cardEditProfilePhoto = findViewById(R.id.cardEditProfilePhoto);
            screenLoader = findViewById(R.id.screenLoader);
            btnUpdateDetail = findViewById(R.id.btnUpdateDetail);
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
            profileDetailsViewModel = new ViewModelProvider(this).get(ProfileDetailsViewModel.class);

            tvTitle.setText(R.string.edit_profile);

            bindData();
            setupListeners();
            setupProfilePhotoObserver();
            setupProfileDetailsObserver();
        } catch (Exception e) {
            AppLogger.e(getClass(), "initComponents", e);
        }
    }

    private void bindData() {
        try {
            etFullName.setText(PreferenceManager.INSTANCE.getAuthFullName());

            Glide.with(ProfileEditDetailActivity.this)
                    .load(PreferenceManager.INSTANCE.getAuthProfilePhoto())
                    .placeholder(R.drawable.ic_profile_avatar)
                    .error(R.drawable.ic_profile_avatar)
                    .circleCrop()
                    .into(profilePhoto);

            profilePhotoPicker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    profilePhotoViewModel.uploadProfilePhoto(uri);
                }
            });
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

            cardEditProfilePhoto.setOnClickListener(v -> {
                hideKeyboard(this);
                profilePhotoPicker.launch("image/*");
            });

            btnUpdateDetail.setOnClickListener(v -> updateProfileDetails());
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

    private void setupProfileDetailsObserver() {

        profileDetailsViewModel.getLoading().observe(this, loading -> {
            boolean isLoading = Boolean.TRUE.equals(loading);
            screenLoader.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnUpdateDetail.setEnabled(!isLoading);
            cardEditProfilePhoto.setEnabled(!isLoading);
            if (isLoading) {
                tvLoaderTitle.setText(R.string.updating_profile);
            }
        });

        profileDetailsViewModel.getUpdateResult().observe(this, result -> {
            if (result == null || isFinishing()) {
                return;
            }
            if (result.getData() != null && result.getData().getFullName() != null) {
                PreferenceManager.INSTANCE.setAuthFullName(result.getData().getFullName());
                etFullName.setText(result.getData().getFullName());
            }

            Toast.makeText(this, result.getMessage() != null ? result.getMessage() :
                    getString(R.string.profile_updated_successfully), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finishWithTransitions();
        });

        profileDetailsViewModel.getErrorMessage().observe(this, message -> {
            if (message == null || message.trim().isEmpty() || isFinishing()) {
                return;
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }

    private void updateProfileDetails() {
        try {

            hideKeyboard(this);

            String fullName = etFullName.getText() == null ? "" : etFullName.getText().toString().trim();

            if (fullName.isEmpty()) {
                etFullName.setError(getString(R.string.enter_full_name));
                etFullName.requestFocus();
                return;
            }

            if (fullName.length() < 2 || fullName.length() > 150) {
                etFullName.setError(getString(R.string.invalid_full_name));
                etFullName.requestFocus();
                return;
            }

            String currentName = PreferenceManager.INSTANCE.getAuthFullName();

            if (fullName.equals(currentName)) {
                Toast.makeText(this, R.string.no_profile_changes, Toast.LENGTH_SHORT).show();
                return;
            }

            profileDetailsViewModel.updateProfile(fullName);

        } catch (Exception e) {
            AppLogger.e(getClass(), "updateProfileDetails", e);
        }
    }

    private void finishWithTransitions() {
        finish();
        ActivityUtils.overrideCloseTransition(ProfileEditDetailActivity.this, R.anim.scale_in, R.anim.right_to_left);
    }
}