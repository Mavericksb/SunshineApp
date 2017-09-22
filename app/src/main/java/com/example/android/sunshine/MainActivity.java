package com.example.android.sunshine;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Robert on 19/09/2017.
 */



public class MainActivity extends AppCompatActivity {

    private static ImageAnimator mImageAnimator;

    public static final String FORECAST_TAG = "forecast_fragment";
    public static final String HOURLY_TAG = "hourly_fragment";

    private static ImageView mBackground;
    private static ImageView mForeground;
    private static View mIncludeBackground;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIncludeBackground = (View) findViewById(R.id.include_background);
        mIncludeBackground.setVisibility(View.INVISIBLE);

        mBackground = (ImageView) findViewById(R.id.cloudView);
        mForeground = (ImageView) findViewById(R.id.cloudView2);
        mImageAnimator = new ImageAnimator(this, mIncludeBackground, mBackground, mForeground);
//

//        mImageAnimator.playAnimation();


        if(savedInstanceState == null) {
            ForecastFragment forecastFragment = new ForecastFragment();

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .add(R.id.fragment_container, forecastFragment, FORECAST_TAG).commit();
        }
    }

    /**
     * This is where we inflate and set up the menu for this Activity.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     *
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.forecast, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    /**
     * Callback invoked when a menu item was selected from this Activity's menu.
     *
     * @param item The menu item that was selected by the user
     *
     * @return true if you handle the menu click here, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_location) {
            startActivity(new Intent(this, LocationActivity.class));
            //openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void startBackground(Context context, String weatherId ){
        Log.e("String Weather", " " + weatherId);

        mImageAnimator.playAnimation();
    }
}
