package com.example.android.sunshine;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
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
import android.os.Build;
import android.support.annotation.IntRange;
import android.support.annotation.InterpolatorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.transition.Fade;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
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



    private View includeBackground;

    private ImageView mBackground;
    private ImageView mForeground;
    private ImageView mCenter;

    private ImageView mSun;

    private ImageView starView;
    private ImageView starView2;

    private ImageView mFogTop;
    private ImageView mFogTop2;
    private ImageView mFogBottom;
    private ImageView mFogBottom2;

    private ImageView mRainBlur;
    private ImageView mRainBlur2;
    private ImageView mRainFirst;
    private ImageView mRainFirst2;
    private ImageView mRainSecond;
    private ImageView mRainSecond2;
    private ImageView mRainThird;
    private ImageView mRainThird2;
    private ImageView mRainForth;
    private ImageView mRainForth2;
    private ImageView mRainFifth;
    private ImageView mRainFifth2;

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
            GradientDrawable.Orientation.TR_BL,
            new int[]{0xFF5f7286, 0xFF333a4b});

    private GradientDrawable night_gradient = new GradientDrawable(
            GradientDrawable.Orientation.BL_TR,
            new int[]{0xFF000000, 0xFF0E2351});




    public ImageAnimator(Context context, View includeBackground) {
        this.includeBackground = includeBackground;
        mContext = context.getApplicationContext();
        mBackground = (ImageView) includeBackground.findViewById(R.id.cloudView);
        mCenter = (ImageView) includeBackground.findViewById(R.id.cloudView2);
        mForeground = (ImageView) includeBackground.findViewById(R.id.cloudView3);

        starView = (ImageView) includeBackground.findViewById(R.id.starView);
        starView2 = (ImageView) includeBackground.findViewById(R.id.starView2);

        mSun = (ImageView) includeBackground.findViewById(R.id.sunView2);

        mFogTop = (ImageView) includeBackground.findViewById(R.id.fog_view_top);
        mFogTop2 = (ImageView) includeBackground.findViewById(R.id.fog_view_top_2);
        mFogBottom = (ImageView) includeBackground.findViewById(R.id.fog_view_bottom);
        mFogBottom2 = (ImageView) includeBackground.findViewById(R.id.fog_view_bottom_2);

        mRainFirst = (ImageView) includeBackground.findViewById(R.id.rain_first_view);
        mRainFirst2 = (ImageView) includeBackground.findViewById(R.id.rain_first_view_2);
        mRainSecond = (ImageView) includeBackground.findViewById(R.id.rain_second_view);
        mRainSecond2 = (ImageView) includeBackground.findViewById(R.id.rain_second_view_2);
        mRainFifth = (ImageView) includeBackground.findViewById(R.id.rain_fifth_view);
        mRainFifth2 = (ImageView) includeBackground.findViewById(R.id.rain_fifth_view_2);


    }

    public void playAnimation(String weatherId, long date, long sunrise, long sunset, boolean onStart) {

        int time = 0;

        int dayTime = Integer.valueOf(SunshineDateUtils.getHourlyDetailDate(date, VIEW_DAYTIME_ANIMATION));

        Log.e("IMAGEANIMATOR", "Time " + dayTime + " date " + date + " sunrise " + sunrise + " sunset " + sunset + " weather Id " + weatherId);

        if (weatherId.contains("rain")) {
            time = OVERCAST_TIME;
        } else if (date > sunrise && date < sunset) {
            time = DAY_TIME;
        } else {
            time = NIGHT_TIME;
        }

        if (PREVIOUS_STATE == time && PREVIOUS_WEATHER_ID.equals(weatherId) && !onStart) {
            return;
        } else {
            PREVIOUS_STATE = time;
            PREVIOUS_WEATHER_ID = weatherId;
        }

        setBackground(time);

        resetViews();


        int cloudsType;
        int rainType = 0;
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
            case "light_rain":
                cloudsType = 4;
                rainType = 1;
                break;
            case "moderate_rain":
                cloudsType = 4;
                rainType = 1;
                break;
            case "heavy_rain":
                cloudsType = 4;
                rainType = 2;
                break;
            case "intense_rain":
                cloudsType = 4;
                rainType = 2;
                break;
            case "storm":
                cloudsType = 4;
                rainType = 3;
                break;
            case "violent_storm":
                cloudsType = 4;
                rainType = 3;
                break;
            default:
                cloudsType = 0;
        }

        if (cloudsType > 0) {
            setClouds(cloudsType);
        } else if ((cloudsType== 0 || cloudsType == 1) && time == DAY_TIME){
            setSun();
        }

        if(rainType > 0){
            setRain();
        }


        if (time == NIGHT_TIME) {
            setNight();
        }


    }

    private void setRain() {

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
//        if (cloudsType > 0) {

        mRainFirst.setVisibility(View.VISIBLE);
        ObjectAnimator rainFirstAnim = ObjectAnimator.ofFloat(mRainFirst, "translationY", -(height), height);
        rainFirstAnim.setDuration(800);
        rainFirstAnim.setRepeatCount(ValueAnimator.INFINITE);
        mRainFirst2.setVisibility(View.VISIBLE);
        ObjectAnimator rainFirstAnim2 = ObjectAnimator.ofFloat(mRainFirst2, "translationY", -(height), height);
        rainFirstAnim2.setDuration(1000);
        rainFirstAnim2.setCurrentPlayTime(500);
        rainFirstAnim2.setRepeatCount(ValueAnimator.INFINITE);

        mRainSecond.setVisibility(View.VISIBLE);
        ObjectAnimator rainSecondAnim = ObjectAnimator.ofFloat(mRainSecond, "translationY", -(height), height);
        rainSecondAnim.setDuration(1200);
        rainSecondAnim.setRepeatCount(ValueAnimator.INFINITE);
        mRainSecond2.setVisibility(View.VISIBLE);
        ObjectAnimator rainSecondAnim2 = ObjectAnimator.ofFloat(mRainSecond2, "translationY", -(height), height);
        rainSecondAnim2.setDuration(1200);
        rainSecondAnim2.setCurrentPlayTime(600);
        rainSecondAnim2.setRepeatCount(ValueAnimator.INFINITE);


        mRainFifth.setVisibility(View.GONE);
        ObjectAnimator rainFifthAnim = ObjectAnimator.ofFloat(mRainFifth, "translationY", -(height), height);
        rainFifthAnim.setDuration(1600);
        rainFifthAnim.setRepeatCount(ValueAnimator.INFINITE);
        mRainFifth2.setVisibility(View.GONE);
        ObjectAnimator rainFifthAnim2 = ObjectAnimator.ofFloat(mRainFifth2, "translationY", -(height), height);
        rainFifthAnim2.setDuration(1600);
        rainFifthAnim2.setCurrentPlayTime(800);
        rainFifthAnim2.setRepeatCount(ValueAnimator.INFINITE);

//            ObjectAnimator bgAnim2 = ObjectAnimator.ofFloat(mBackground, "translationX", 1200f, -1200f);
//            bgAnim2.setRepeatCount(ValueAnimator.INFINITE);
//            bgAnim2.setDuration(295000);
//
            AnimatorSet blurAs = new AnimatorSet();
            blurAs.setInterpolator(new LinearInterpolator());
            blurAs.playTogether(
                    rainFirstAnim, rainFirstAnim2,
                    rainSecondAnim, rainSecondAnim2,
                    rainFifthAnim, rainFifthAnim2);
            blurAs.start();  // start animation



    }

    private void setSun() {
//        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        int width = size.x;
//        int height = size.y;

        mSun.setVisibility(View.VISIBLE);

        RotateAnimation rotateSun = new RotateAnimation(180, 270, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateSun.setRepeatMode(Animation.REVERSE);
        rotateSun.setRepeatCount(Animation.INFINITE);
        rotateSun.setDuration(35000);

        ScaleAnimation scaleSun = new ScaleAnimation(0.94f, 1f, 0.94f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleSun.setRepeatMode(Animation.REVERSE);
        scaleSun.setRepeatCount(Animation.INFINITE);
        scaleSun.setDuration(1500);

        AnimationSet set = new AnimationSet(false);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addAnimation(rotateSun);
        set.addAnimation(scaleSun);
        mSun.startAnimation(set);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("deprecation")
    private void setBackground(int time) {

        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            switch (time) {
                case DAY_TIME:
                    includeBackground.setBackgroundDrawable(clear_day_gradient);
                    break;
                case NIGHT_TIME:
                    includeBackground.setBackgroundDrawable(night_gradient);
                    break;
                case OVERCAST_TIME:
                    includeBackground.setBackgroundDrawable(overcast_day_gradient);
                    break;
                default:
                    includeBackground.setBackgroundDrawable(clear_day_gradient);
            }
        } else {

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

    }


    private void resetViews() {
        includeBackground.setVisibility(View.VISIBLE);

        mSun.clearAnimation();
        mSun.setVisibility(View.INVISIBLE);

        starView.clearAnimation();
        starView2.clearAnimation();
        starView.setVisibility(View.GONE);
        starView2.setVisibility(View.GONE);

        mFogTop.clearAnimation();
        mFogTop2.clearAnimation();
        mFogTop.setVisibility(View.GONE);
        mFogTop2.setVisibility(View.GONE);
        mFogBottom.setVisibility(View.GONE);
        mFogBottom2.setVisibility(View.GONE);

        mRainFirst.setVisibility(View.GONE);
        mRainFirst2.setVisibility(View.GONE);
        mRainSecond.setVisibility(View.GONE);
        mRainSecond2.setVisibility(View.GONE);
        mRainFifth.setVisibility(View.GONE);
        mRainFifth2.setVisibility(View.GONE);

        mRainFirst.clearAnimation();
        mRainFirst2.clearAnimation();
        mRainSecond.clearAnimation();
        mRainSecond2.clearAnimation();
        mRainFifth.clearAnimation();
        mRainFifth2.clearAnimation();

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
            ObjectAnimator centerAnim2 = ObjectAnimator.ofFloat(mCenter, "translationX", 1500f, -1500f);
            centerAnim2.setRepeatCount(ValueAnimator.INFINITE);
            centerAnim2.setDuration(165000);

            AnimatorSet fgAs = new AnimatorSet();
            fgAs.setInterpolator(new LinearInterpolator());
            fgAs.playSequentially(centerAnim2);
            fgAs.start();  // start animation
        }
        if (cloudsType > 3) {
            mForeground.setColorFilter(0xFF626474);
            mCenter.setColorFilter(0xFF626474);
            mBackground.setColorFilter(0xFF626474);
        }
    }


}
