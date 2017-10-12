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

    private ImageView mRainFirst;
    private ImageView mRainFirst2;
    private ImageView mRainSecond;
    private ImageView mRainSecond2;
    private ImageView mRainFifth;
    private ImageView mRainFifth2;

    private static final int VIEW_DAYTIME_ANIMATION = 2;

    private static int PREVIOUS_STATE;
    private static String PREVIOUS_WEATHER_ID;

    private static final int DAY_TIME = 1;
    private static final int NIGHT_TIME = 2;
    private static final int OVERCAST_TIME = 3;
    private static final int OVERCAST_TIME_NIGHT = 4;
    private static final int CLOUDY_DAY_TIME = 5;
    private static final int CLOUDY_NIGHT_TIME = 6;

    private GradientDrawable clear_day_gradient = new GradientDrawable(
            GradientDrawable.Orientation.BL_TR,
            new int[]{0xFF8deefc, 0xFF1b4297});

    private GradientDrawable overcast_day_gradient = new GradientDrawable(
            GradientDrawable.Orientation.TR_BL,
            new int[]{0xFF899fb6, 0xFF333a4b});

    private GradientDrawable cloudy_day_gradient = new GradientDrawable(
            GradientDrawable.Orientation.TR_BL,
            new int[]{0xFF465f97, 0xFF6e9da4});

    private GradientDrawable night_gradient = new GradientDrawable(
            GradientDrawable.Orientation.BL_TR,
            new int[]{0xFF000000, 0xFF0E2351});

    private GradientDrawable overcast_night_gradient = new GradientDrawable(
            GradientDrawable.Orientation.TR_BL,
            new int[]{0xFF293749, 0xFF111829});

    private GradientDrawable cloudy_night_gradient = new GradientDrawable(
            GradientDrawable.Orientation.TR_BL,
            new int[]{0xFF293749, 0xFF111829});

    private int mWidth;
    private int mHeight;


    public ImageAnimator(Context context, View includeBackground) {
        this.includeBackground = includeBackground;
        mContext = context.getApplicationContext();
        mBackground = (ImageView) includeBackground.findViewById(R.id.cloudView);
        mCenter = (ImageView) includeBackground.findViewById(R.id.cloudView2);
        mForeground = (ImageView) includeBackground.findViewById(R.id.cloudView3);

        starView = (ImageView) includeBackground.findViewById(R.id.starView);

        mSun = (ImageView) includeBackground.findViewById(R.id.sunView2);

        mRainFirst = (ImageView) includeBackground.findViewById(R.id.rain_first_view);
        mRainFirst2 = (ImageView) includeBackground.findViewById(R.id.rain_first_view_2);
        mRainSecond = (ImageView) includeBackground.findViewById(R.id.rain_second_view);
        mRainSecond2 = (ImageView) includeBackground.findViewById(R.id.rain_second_view_2);
        mRainFifth = (ImageView) includeBackground.findViewById(R.id.rain_fifth_view);
        mRainFifth2 = (ImageView) includeBackground.findViewById(R.id.rain_fifth_view_2);


        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mWidth = size.x;
        mHeight = size.y;

    }

    public void stopAnimation(){
        int DAYTIME = DAY_TIME;
        resetViews();
        setBackground(DAYTIME);
    }
    public void playAnimation(String weatherId, long date, long sunrise, long sunset, boolean onStart) {

        int DAYTIME = 0;

        int dayTime = Integer.valueOf(SunshineDateUtils.getHourlyDetailDate(date, VIEW_DAYTIME_ANIMATION));

        Log.e("IMAGEANIMATOR", "Time " + dayTime + " date " + date + " sunrise " + sunrise + " sunset " + sunset + " weather Id " + weatherId);


        if (date > sunrise && date < sunset) {
            if (weatherId.contains("rain") || (weatherId.equals("overcast_clouds"))) {
                DAYTIME = OVERCAST_TIME;
            } else if (weatherId.equals("scattered_clouds") || weatherId.equals("broken_clouds")){
                DAYTIME = CLOUDY_DAY_TIME;
            } else {
                DAYTIME = DAY_TIME;
            }
        } else {
            if (weatherId.contains("rain") || (weatherId.equals("overcast_clouds"))) {
                DAYTIME = OVERCAST_TIME_NIGHT;
            } else if (weatherId.equals("scattered_clouds") || weatherId.equals("broken_clouds")){
                DAYTIME = CLOUDY_NIGHT_TIME;
            } else {
                DAYTIME = NIGHT_TIME;
            }
        }

        if (PREVIOUS_STATE == DAYTIME && PREVIOUS_WEATHER_ID.equals(weatherId) && !onStart) {
            return;
        } else {
            PREVIOUS_STATE = DAYTIME;
            PREVIOUS_WEATHER_ID = weatherId;
        }

        setBackground(DAYTIME);

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
        } else if ((cloudsType== 0 || cloudsType == 1) && DAYTIME == DAY_TIME){
            setSun();
        }

        if(rainType > 0){
            setRain();
        }

        if (DAYTIME == NIGHT_TIME || DAYTIME == OVERCAST_TIME_NIGHT || DAYTIME == CLOUDY_NIGHT_TIME) {
            setNight();
        }
    }

    private void setRain() {

        mRainFirst.setVisibility(View.VISIBLE);
        ObjectAnimator rainFirstAnim = ObjectAnimator.ofFloat(mRainFirst, "translationY", -(mHeight), mHeight);
        rainFirstAnim.setDuration(800);
        rainFirstAnim.setRepeatCount(ValueAnimator.INFINITE);
        mRainFirst2.setVisibility(View.VISIBLE);
        ObjectAnimator rainFirstAnim2 = ObjectAnimator.ofFloat(mRainFirst2, "translationY", -(mHeight), mHeight);
        rainFirstAnim2.setDuration(1000);
        rainFirstAnim2.setCurrentPlayTime(500);
        rainFirstAnim2.setRepeatCount(ValueAnimator.INFINITE);

        mRainSecond.setVisibility(View.VISIBLE);
        ObjectAnimator rainSecondAnim = ObjectAnimator.ofFloat(mRainSecond, "translationY", -(mHeight), mHeight);
        rainSecondAnim.setDuration(1200);
        rainSecondAnim.setRepeatCount(ValueAnimator.INFINITE);
        mRainSecond2.setVisibility(View.VISIBLE);
        ObjectAnimator rainSecondAnim2 = ObjectAnimator.ofFloat(mRainSecond2, "translationY", -(mHeight), mHeight);
        rainSecondAnim2.setDuration(1200);
        rainSecondAnim2.setCurrentPlayTime(600);
        rainSecondAnim2.setRepeatCount(ValueAnimator.INFINITE);

        mRainFifth.setVisibility(View.GONE);
        ObjectAnimator rainFifthAnim = ObjectAnimator.ofFloat(mRainFifth, "translationY", -(mHeight), mHeight);
        rainFifthAnim.setDuration(1600);
        rainFifthAnim.setRepeatCount(ValueAnimator.INFINITE);
        mRainFifth2.setVisibility(View.GONE);
        ObjectAnimator rainFifthAnim2 = ObjectAnimator.ofFloat(mRainFifth2, "translationY", -(mHeight), mHeight);
        rainFifthAnim2.setDuration(1600);
        rainFifthAnim2.setCurrentPlayTime(800);
        rainFifthAnim2.setRepeatCount(ValueAnimator.INFINITE);


            AnimatorSet blurAs = new AnimatorSet();
            blurAs.setInterpolator(new LinearInterpolator());
            blurAs.playTogether(
                    rainFirstAnim, rainFirstAnim2,
                    rainSecondAnim, rainSecondAnim2,
                    rainFifthAnim, rainFifthAnim2);
            blurAs.start();  // start animation

    }

    private void setSun() {
        mSun.setVisibility(View.VISIBLE);

        RotateAnimation rotateSun = new RotateAnimation(180, 270, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateSun.setRepeatMode(Animation.REVERSE);
        rotateSun.setRepeatCount(Animation.INFINITE);
        rotateSun.setDuration(30000);

        ScaleAnimation scaleSun = new ScaleAnimation(0.94f, 1f, 0.94f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleSun.setRepeatMode(Animation.REVERSE);
        scaleSun.setRepeatCount(Animation.INFINITE);
        scaleSun.setDuration(1200);

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
                case OVERCAST_TIME_NIGHT:
                    includeBackground.setBackgroundDrawable(overcast_night_gradient);
                    break;
                case CLOUDY_DAY_TIME:
                    includeBackground.setBackgroundDrawable(cloudy_day_gradient);
                    break;
                case CLOUDY_NIGHT_TIME:
                    includeBackground.setBackgroundDrawable(cloudy_night_gradient);
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
                case OVERCAST_TIME_NIGHT:
                    includeBackground.setBackground(overcast_night_gradient);
                    break;
                case CLOUDY_DAY_TIME:
                    includeBackground.setBackground(cloudy_day_gradient);
                    break;
                case CLOUDY_NIGHT_TIME:
                    includeBackground.setBackground(cloudy_night_gradient);
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

        starView.setVisibility(View.GONE);

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
        mForeground.setColorFilter(0xFF646E84);
        mCenter.setColorFilter(0xFF646E84);
        mBackground.setColorFilter(0xFF646E84);

        RotateAnimation rotateAnimation1 = new RotateAnimation(0, 360, Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.5f);
        rotateAnimation1.setInterpolator(new LinearInterpolator());
        rotateAnimation1.setRepeatMode(Animation.RESTART);
        rotateAnimation1.setRepeatCount(Animation.INFINITE);
        rotateAnimation1.setDuration(350000);

        AlphaAnimation alphaAnimation1 = new AlphaAnimation(0.6f, 0.7f);
        alphaAnimation1.setRepeatMode(Animation.REVERSE);
        alphaAnimation1.setRepeatCount(Animation.INFINITE);
        alphaAnimation1.setDuration(25);

        AnimationSet set = new AnimationSet(false);
        set.addAnimation(rotateAnimation1);
        set.addAnimation(alphaAnimation1);
        starView.startAnimation(set);
    }


    private void setClouds(int cloudsType) {
        if (cloudsType > 0) {
            mBackground.setVisibility(View.VISIBLE);
            ObjectAnimator bgAnim1 = ObjectAnimator.ofFloat(mBackground, "translationX", mWidth, -mWidth);
            bgAnim1.setInterpolator(new LinearInterpolator());
            bgAnim1.setRepeatCount(ValueAnimator.INFINITE);
            bgAnim1.setDuration(290000);
            bgAnim1.setCurrentPlayTime(200000);
            bgAnim1.start();
        }
        if (cloudsType > 1) {
            mForeground.setVisibility(View.VISIBLE);
            TranslateAnimation ta2 = new TranslateAnimation(mWidth*2, -mWidth*2, 0, 0);
            ta2.setInterpolator(new LinearInterpolator());
            ta2.setDuration(110000);
            ta2.setRepeatCount(Animation.INFINITE);
            ta2.setRepeatMode(Animation.RESTART);

            mForeground.startAnimation(ta2);
        }
        if (cloudsType > 2) {
            mCenter.setVisibility(View.VISIBLE);
            Double d = mWidth*1.9;
            ObjectAnimator centerAnim1 = ObjectAnimator.ofFloat(mCenter, "translationX", d.intValue(), -d.intValue());
            centerAnim1.setInterpolator(new LinearInterpolator());
            centerAnim1.setRepeatCount(ValueAnimator.INFINITE);
            centerAnim1.setDuration(150000);
            centerAnim1.setCurrentPlayTime(75000);
            centerAnim1.start();
        }
        if (cloudsType > 3) {
            mBackground.setVisibility(View.GONE);
            mForeground.setColorFilter(0xFFa0a3ba);
            mBackground.setColorFilter(0xFF8e91a7);
            mCenter.setColorFilter(0xFF8e91a7);
        }
    }


}
