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
import android.graphics.Point;
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
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
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
    private ImageView mCenter;

    private ImageView mSun;

    private View includeBackground;

    private static final int VIEW_DAYTIME_ANIMATION = 2;

    private static int PREVIOUS_STATE;
    private static String PREVIOUS_WEATHER_ID;

    private static final int DAY_TIME = 1;
    private static final int NIGHT_TIME = 2;
    private static final int OVERCAST_TIME = 3;

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
        mCenter = (ImageView) includeBackground.findViewById(R.id.cloudView2);
        mForeground = (ImageView) includeBackground.findViewById(R.id.cloudView3);

        starView = (ImageView) includeBackground.findViewById(R.id.starView);
        starView2 = (ImageView) includeBackground.findViewById(R.id.starView2);

        mSun = (ImageView) includeBackground.findViewById(R.id.sunView);

    }

    public void playAnimation(String weatherId, long date, long sunrise, long sunset, boolean onStart) {

        int time = 0;

        int dayTime = Integer.valueOf(SunshineDateUtils.getHourlyDetailDate(date, VIEW_DAYTIME_ANIMATION));

        Log.e("IMAGEANIMATOR", "Time " + dayTime + " date " + date + " sunrise " + sunrise + " sunset " + sunset + " weather Id " + weatherId);


        if (date > sunrise && date < sunset) {
            time = DAY_TIME;
        } else if (weatherId.equals("overcast_clouds")) {
            time = OVERCAST_TIME;
        } else {
            time = NIGHT_TIME;
        }

        if (PREVIOUS_STATE == time && PREVIOUS_WEATHER_ID == weatherId && !onStart) {
            return;
        } else {
            PREVIOUS_STATE = time;
            PREVIOUS_WEATHER_ID = weatherId;
        }

        setBackground(time);



        resetViews();

        int cloudsType;
        switch (weatherId) {
            case "mostly_clear":
                cloudsType = 1;
                break;
            case "scattered_clouds":
                cloudsType = 2;
                break;
            case "broken_clouds":
                cloudsType = 3;
                break;
            case "overcast_clouds":
                cloudsType = 4;
                break;
            default:
                cloudsType = 0;
        }

        if (cloudsType > 0) {
            setClouds(cloudsType);
        } else if (time == DAY_TIME){
            setSun();
        }


        if (time == NIGHT_TIME) {
            setNight();
        }


    }

    private void setSun() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float width = size.x;
        float height = size.y;

        mSun.setVisibility(View.VISIBLE);

        mSun.setX(width/2);
        mSun.setY(-250.0f);
        Log.e("SetSun", " x is " + mSun.getX() + " Height is " + mSun.getY());

        RotateAnimation rotateSun = new RotateAnimation(0, 360, Animation.ABSOLUTE, mSun.getMeasuredWidth()/2, Animation.ABSOLUTE, mSun.getMeasuredHeight()/2);
        rotateSun.setInterpolator(new LinearInterpolator());
        rotateSun.setRepeatMode(Animation.RESTART);
        rotateSun.setRepeatCount(Animation.INFINITE);
        rotateSun.setDuration(200000);

        AlphaAnimation alphaSun = new AlphaAnimation(0.96f, 1f);
        alphaSun.setRepeatMode(Animation.REVERSE);
        alphaSun.setRepeatCount(Animation.INFINITE);
        alphaSun.setDuration(50);

        AnimationSet set = new AnimationSet(false);
        set.addAnimation(rotateSun);
        set.addAnimation(alphaSun);
        mSun.startAnimation(set);

    }


    private void setBackground(int time) {
        switch (time) {
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
    }


    private void resetViews() {
        includeBackground.setVisibility(View.VISIBLE);

        mSun.clearAnimation();
        mSun.setVisibility(View.GONE);

        starView.clearAnimation();
        starView2.clearAnimation();
        starView.setVisibility(View.GONE);
        starView2.setVisibility(View.GONE);

        mForeground.clearAnimation();
        mForeground.setColorFilter(0);
        mForeground.setVisibility(View.GONE);
        mCenter.clearAnimation();
        mCenter.setColorFilter(0);
        mCenter.setVisibility(View.GONE);
        mBackground.clearAnimation();
        mBackground.setColorFilter(0);
        mBackground.setVisibility(View.GONE);
    }

    private void setNight() {
        starView.setVisibility(View.VISIBLE);
        starView2.setVisibility(View.VISIBLE);
        mForeground.setColorFilter(0xFF646E84);
        mCenter.setColorFilter(0xFF646E84);
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
    }


    private void setClouds(int cloudsType) {
        if (cloudsType > 0) {
            mBackground.setVisibility(View.VISIBLE);
            ObjectAnimator bgAnim1 = ObjectAnimator.ofFloat(mBackground, "translationX", 600f, -1200f);
            bgAnim1.setDuration(195000);
            //bgAnim1.setRepeatMode(ValueAnimator.RESTART);
            ObjectAnimator bgAnim2 = ObjectAnimator.ofFloat(mBackground, "translationX", 1200f, -1200f);
            bgAnim2.setRepeatCount(ValueAnimator.INFINITE);
            bgAnim2.setDuration(295000);

            AnimatorSet bgAs = new AnimatorSet();
            bgAs.setInterpolator(new LinearInterpolator());
            bgAs.playSequentially(bgAnim1, bgAnim2);
            bgAs.start();  // start animation
        }
        if (cloudsType > 1) {
            mForeground.setVisibility(View.VISIBLE);
            ObjectAnimator fgAnim1 = ObjectAnimator.ofFloat(mForeground, "translationX", 0f, -1500f);
            fgAnim1.setDuration(55000);
            //fgAnim1.setRepeatMode(ValueAnimator.RESTART);
            ObjectAnimator fgAnim2 = ObjectAnimator.ofFloat(mForeground, "translationX", 1500f, -1500f);
            fgAnim2.setRepeatCount(ValueAnimator.INFINITE);
            fgAnim2.setDuration(110000);

            AnimatorSet fgAs = new AnimatorSet();
            fgAs.setInterpolator(new LinearInterpolator());
            fgAs.playSequentially(fgAnim1, fgAnim2);
            fgAs.start();  // start animation
        }
        if (cloudsType > 2) {
            mCenter.setVisibility(View.VISIBLE);
            ObjectAnimator centerAnim1 = ObjectAnimator.ofFloat(mCenter, "translationX", 0f, -1500f);
            centerAnim1.setDuration(83000);
            //centerAnim1.setRepeatMode(ValueAnimator.RESTART);
            ObjectAnimator centerAnim2 = ObjectAnimator.ofFloat(mCenter, "translationX", 1500f, -1500f);
            centerAnim2.setRepeatCount(ValueAnimator.INFINITE);
            centerAnim2.setDuration(165000);

            AnimatorSet fgAs = new AnimatorSet();
            fgAs.setInterpolator(new LinearInterpolator());
            fgAs.playSequentially(centerAnim2);
            fgAs.start();  // start animation
        }
        if (cloudsType > 3) {
            mForeground.setColorFilter(0xFF646E84);
            mCenter.setColorFilter(0xFF646E84);
            mBackground.setColorFilter(0xFF646E84);
        }
    }


}
