package com.nprotech.moneytracker.ui.adapters;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.nprotech.moneytracker.R;
import com.nprotech.moneytracker.models.OnboardingModel;

import java.util.List;

public class OnboardingAdapter extends RecyclerViewAdapter<OnboardingModel> {


    public OnboardingAdapter(Context context, List<OnboardingModel> list) {
        super(context, list, R.layout.item_onboarding);
    }

    @Override
    public void onPostBindViewHolder(ViewHolder holder, OnboardingModel model) {

        Animation fadeAnimation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_up);

        AppCompatImageView image = holder.itemView.findViewById(R.id.imgOnboarding);
        AppCompatTextView title = holder.itemView.findViewById(R.id.txtTitle);
        AppCompatTextView description = holder.itemView.findViewById(R.id.txtDescription);

        image.setImageResource(model.image);
        title.setText(model.title);
        description.setText(model.description);

        image.startAnimation(fadeAnimation);
        title.startAnimation(fadeAnimation);
        description.startAnimation(fadeAnimation);
    }
}