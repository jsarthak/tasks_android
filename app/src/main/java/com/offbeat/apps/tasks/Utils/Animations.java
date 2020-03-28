package com.offbeat.apps.tasks.Utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.widget.ViewAnimator;

public class Animations {

    Context mContext;

    public Animations(Context mContext) {
        this.mContext = mContext;
    }

    public void circleReveal(final View view, final boolean isShow, int duration){
        int width = view.getWidth();
        int cx = width;
        int cy = view.getHeight()/2;

        Animator animator;
        if (isShow){
            animator = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, width);
        } else{
            animator = ViewAnimationUtils.createCircularReveal(view, cx, cy,  width,0);
        }
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!isShow){
                    view.setVisibility(View.INVISIBLE);
                }
            }
        });
        if (isShow){
            view.setVisibility(View.VISIBLE);
        }
        animator.start();




    }
}
