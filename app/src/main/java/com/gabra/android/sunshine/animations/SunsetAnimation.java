package com.gabra.android.sunshine.animations;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by Robert on 15/01/2018.
 */

public class SunsetAnimation extends Animation {

    private View view;
    private float cx, cy;           // center x,y position of circular path
    private float prevX, prevY;     // previous x,y position of image during animation
    private float r;                // radius of circle

    private float prevDx, prevDy;
    private Context mContext;
    private int mWidth;
    private int mHeight;
    private float distance;


    /**
     * @param view - View that will be animated
     * @param r - radius of circular path
     */
    public SunsetAnimation(Context context, View view, float r){
        this.view = view;
        this.r = r;
        mContext = context;
//        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        mWidth = size.x;
//        mHeight = size.y;
//        DisplayMetrics dm = new DisplayMetrics();
//        int dp = dm.densityDpi;
        distance = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, r,
                mContext.getResources().getDisplayMetrics()
        );
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        // calculate position of image center
        int cxImage = width /2 ;
        int cyImage = height / 2;
        cx = view.getLeft() + cxImage;
        cy = view.getTop() + cyImage;

        // set previous position to center
        prevX = cx;
        prevY = cy + cyImage*2;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        if(interpolatedTime == 0){
            t.getMatrix().setTranslate(prevDx, prevDy);
            return;
        }

        float angleDeg = (interpolatedTime * 180f) % 180;
        float angleRad = (float) Math.toRadians(angleDeg);

        // r = radius, cx and cy = center point, a = angle (radians)
        float x = (float) (cx + distance * Math.cos(angleRad));
        float y = (float) (cy + distance * Math.sin(angleRad));


        float dx = prevX - x;
        float dy = prevY - y;

        prevX = x;
        prevY = y;

        prevDx = dx;
        prevDy = dy;

        t.getMatrix().setTranslate(dx, dy);
    }
}

