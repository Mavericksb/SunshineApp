package com.example.android.sunshine;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.support.annotation.IntRange;
import android.support.annotation.InterpolatorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.utilities.SunshineDateUtils;

/**
 * Created by Robert on 18/09/2017.
 */

public class ImageAnimator {

    private Context mContext;
    private ImageView mBackground;
    private ImageView mForeground;
    private View includeBackground;
    private static final int VIEW_DAYTIME_ANIMATION = 2;
    private static int PREVIOUS_STATE;

    private static final int DAY_TIME = 1;
    private static final int SUNSET_TIME = 2;
    private static final int NIGHT_TIME = 3;
    private static final int OVERCAST_TIME = 4;

    private GradientDrawable clear_day_gradient = new GradientDrawable(
            GradientDrawable.Orientation.BL_TR,
            new int[]{0xFF8deefc, 0xFF1b4297});

    private GradientDrawable overcast_day_gradient = new GradientDrawable(
            GradientDrawable.Orientation.BL_TR,
            new int[]{0xFF5f7286, 0xFF333a4b});

//    private GradientDrawable sunset_gradient = new GradientDrawable(
//            GradientDrawable.Orientation.BL_TR,
//            new int[]{0xFFdaca78, 0xFFff9700});

    private GradientDrawable night_gradient = new GradientDrawable(
            GradientDrawable.Orientation.BL_TR,
            new int[]{0xFF000000, 0xFF0E2351});

    private ImageView starView;
    private ImageView starView2;


    public ImageAnimator(Context context, View includeBackground) {
        this.includeBackground = includeBackground;
        mContext = context.getApplicationContext();
        mBackground = (ImageView) includeBackground.findViewById(R.id.cloudView);
        mForeground = (ImageView) includeBackground.findViewById(R.id.cloudView2);

        starView = (ImageView) includeBackground.findViewById(R.id.starView);
        starView2 = (ImageView) includeBackground.findViewById(R.id.starView2);

    }

    public void playAnimation(String weatherId, long date, boolean onStart) {

//        View view = LayoutInflater.from(mContext).inflate(R.layout.background_clouds, null);
//        view.setBackground(mContext.getDrawable(R.drawable.gradient_background_clear));
        int TIME = 0;

        int dayTime = Integer.valueOf(SunshineDateUtils.getHourlyDetailDate(date, VIEW_DAYTIME_ANIMATION));
        long sunrise = SunshinePreferences.getSunriseTime(mContext);
        long sunset = SunshinePreferences.getSunsetTime(mContext);

        Log.e("IMAGEANIMATOR", "Time " + dayTime + " date "  + date + " sunrise " + sunrise + " sunset " + sunset + " weather Id " + weatherId);


        if (date > sunrise && date < sunset) {
            TIME = DAY_TIME;
//        } else if (dayTime >= 18 && dayTime <= 19) {
//            TIME = SUNSET_TIME;
        } else {
            TIME = NIGHT_TIME;
        }

        if (PREVIOUS_STATE == TIME && !onStart) {
            return;
        }

        switch (TIME) {
            case DAY_TIME:
                includeBackground.setBackground(clear_day_gradient);
                break;
            case NIGHT_TIME:
                includeBackground.setBackground(night_gradient);
                break;
            case OVERCAST_TIME:
                includeBackground.setBackground(overcast_day_gradient);
                break;
            default:
                includeBackground.setBackground(clear_day_gradient);
        }

        PREVIOUS_STATE = TIME;

        includeBackground.setVisibility(View.VISIBLE);


        starView.setVisibility(View.GONE);
        starView2.setVisibility(View.GONE);

        mForeground.setColorFilter(0);
        mBackground.setColorFilter(0);

        int cloudsType;
        switch(weatherId) {
            case "mostly_clear":
                cloudsType = 1;
            case "scattered_clouds":
                cloudsType = 2;
            case "broken_clouds":
                cloudsType = 3;
            case "overcast_clouds":
                cloudsType = 4;
            default:
                cloudsType = 0;
        }

        if(cloudsType>0) {
            ObjectAnimator bgAnim1 = ObjectAnimator.ofFloat(mBackground, "translationX", 600f, -1200f);
            bgAnim1.setDuration(120000);
            bgAnim1.setRepeatMode(ValueAnimator.RESTART);
            ObjectAnimator bgAnim2 = ObjectAnimator.ofFloat(mBackground, "translationX", 1200f, -1200f);
            bgAnim2.setRepeatCount(ValueAnimator.INFINITE);
            bgAnim2.setDuration(160000);

            AnimatorSet bgAs = new AnimatorSet();
            bgAs.setInterpolator(new LinearInterpolator());
            bgAs.playSequentially(bgAnim1, bgAnim2);
            bgAs.start();  // start animation
        }
        if(cloudsType>1) {
            ObjectAnimator fgAnim1 = ObjectAnimator.ofFloat(mForeground, "translationX", 0f, -1500f);
            fgAnim1.setDuration(55000);
            fgAnim1.setRepeatMode(ValueAnimator.RESTART);
            ObjectAnimator fgAnim2 = ObjectAnimator.ofFloat(mForeground, "translationX", 1500f, -1500f);
            fgAnim2.setRepeatCount(ValueAnimator.INFINITE);
            fgAnim2.setDuration(110000);

            AnimatorSet fgAs = new AnimatorSet();
            fgAs.setInterpolator(new LinearInterpolator());
            fgAs.playSequentially(fgAnim1, fgAnim2);
            fgAs.start();  // start animation
        }


        if (TIME == NIGHT_TIME) {

            starView.setVisibility(View.VISIBLE);
            starView2.setVisibility(View.VISIBLE);
            mForeground.setColorFilter(0xFF646E84);
            mBackground.setColorFilter(0xFF646E84);

            RotateAnimation rotateAnimation1 = new RotateAnimation(0, 360, Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.5f);
            rotateAnimation1.setInterpolator(new LinearInterpolator());
            rotateAnimation1.setRepeatMode(Animation.RESTART);
            rotateAnimation1.setRepeatCount(Animation.INFINITE);
            rotateAnimation1.setDuration(400000);

            RotateAnimation rotateAnimation2 = new RotateAnimation(0, 360, Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.5f);
            rotateAnimation2.setInterpolator(new LinearInterpolator());
            rotateAnimation2.setRepeatMode(Animation.RESTART);
            rotateAnimation2.setRepeatCount(Animation.INFINITE);
            rotateAnimation2.setDuration(350000);


            AlphaAnimation alphaAnimation1 = new AlphaAnimation(0.6f, 0.7f);
            alphaAnimation1.setRepeatMode(Animation.REVERSE);
            alphaAnimation1.setRepeatCount(Animation.INFINITE);
            alphaAnimation1.setDuration(25);

            AlphaAnimation alphaAnimation2 = new AlphaAnimation(0.5f, 0.7f);
            alphaAnimation2.setRepeatMode(Animation.REVERSE);
            alphaAnimation2.setRepeatCount(Animation.INFINITE);
            alphaAnimation2.setDuration(75);

            AnimationSet set = new AnimationSet(false);
            set.addAnimation(rotateAnimation1);
            set.addAnimation(alphaAnimation1);
            starView.startAnimation(set);

            AnimationSet set2 = new AnimationSet(false);
            set2.addAnimation(rotateAnimation2);
            set2.addAnimation(alphaAnimation2);
            starView2.startAnimation(set2);

        } else {
            starView.clearAnimation();
            starView2.clearAnimation();
            starView.setVisibility(View.GONE);
            starView2.setVisibility(View.GONE);
        }

    }


}
