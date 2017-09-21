package com.example.android.sunshine;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.InterpolatorRes;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

/**
 * Created by Robert on 18/09/2017.
 */

public class ImageAnimator {

    private static Context mContext;
    private static Layout mLayout;
    private static ImageView mBackground;
    private static ImageView mForeground;

    public ImageAnimator(Context context, View includeBackground, ImageView background, ImageView foreground){
        mContext = context;
        //mLayout = layout;
        mBackground = background;
        mForeground = foreground;
        includeBackground.setVisibility(View.VISIBLE);
        includeBackground.setBackground(mContext.getDrawable(R.drawable.gradient_background_clear));

    }

    public void playAnimation(){

//        View view = LayoutInflater.from(mContext).inflate(R.layout.background_clouds, null);
//        view.setBackground(mContext.getDrawable(R.drawable.gradient_background_clear));


        TranslateAnimation animation = new TranslateAnimation(1200.0f, -1200.0f,
                0.0f, 0.0f);          //  new TranslateAnimation(xFrom,xTo, yFrom,yTo)
        animation.setDuration(90000);  // animation duration
        animation.setRepeatCount(ValueAnimator.INFINITE);  // animation repeat count
        animation.setRepeatMode(1);   // repeat animation (left to right, right to left )
        animation.setInterpolator(new LinearInterpolator(mContext, null));
        //animation.setFillAfter(true);


        mBackground.startAnimation(animation);  // start animation


        TranslateAnimation foregroundanimation = new TranslateAnimation(1800.0f, -1800.0f,
                0.0f, 0.0f);          //  new TranslateAnimation(xFrom,xTo, yFrom,yTo)
        foregroundanimation.setDuration(60000);  // animation duration
        foregroundanimation.setRepeatCount(ValueAnimator.INFINITE);  // animation repeat count
        foregroundanimation.setRepeatMode(1);   // repeat animation (left to right, right to left )
        foregroundanimation.setInterpolator(new LinearInterpolator(mContext, null));
        //animation.setFillAfter(true);


        mForeground.startAnimation(foregroundanimation);  // start animation
    }


}
