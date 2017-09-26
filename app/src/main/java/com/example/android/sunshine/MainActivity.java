package com.example.android.sunshine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.android.sunshine.data.CurrentWeatherContract;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.sync.SunshineSyncUtils;

import java.lang.ref.WeakReference;

/**
 * Created by Robert on 19/09/2017.
 */



public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String FORECAST_TAG = "forecast_fragment";
    public static final String HOURLY_TAG = "hourly_fragment";

    private static final int ID_LOADER_BACKGROUND = 101;

    private ImageAnimator mImageAnimator;

    private static String mWeatherId;
    private static long mDateTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        View mIncludeBackground = (View) findViewById(R.id.include_background);

        mIncludeBackground.setVisibility(View.INVISIBLE);

        mImageAnimator = new ImageAnimator(this, mIncludeBackground);

        FragmentManager fm = getSupportFragmentManager();
        Fragment forecastFragment = fm.findFragmentByTag(MainActivity.HOURLY_TAG);

        FragmentTransaction ft = fm.beginTransaction();

        if(forecastFragment == null) {
            forecastFragment = new ForecastFragment();

            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, forecastFragment, FORECAST_TAG).commit();
        } else {
            ft.show(forecastFragment);
        }

        Loader loader = getSupportLoaderManager().initLoader(ID_LOADER_BACKGROUND, null, this);

        SunshineSyncUtils.initialize(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSupportLoaderManager().restartLoader(ID_LOADER_BACKGROUND, null, this);
    }

    public static void setBackground(String weatherId, long date ){
        Log.e("String Weather", " " + weatherId);
        mWeatherId = weatherId;
        mDateTime = date;
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_location) {
            startActivity(new Intent(this, LocationActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this,
                CurrentWeatherContract.CurrentWeatherEntry.CONTENT_URI,
                new String[]{CurrentWeatherContract.CurrentWeatherEntry.COLUMN_WEATHER_ID, CurrentWeatherContract.CurrentWeatherEntry.COLUMN_DATE},
                CurrentWeatherContract.CurrentWeatherEntry.COLUMN_CITY_ID + "=?",
                new String[]{String.valueOf(SunshinePreferences.getCityId(this))},
                null
                );
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {

        if(data!=null && data.getCount() != 0){
            data.moveToFirst();
            mImageAnimator.playAnimation(data.getString(data.getColumnIndex(CurrentWeatherContract.CurrentWeatherEntry.COLUMN_WEATHER_ID)),
                    (data.getLong(data.getColumnIndex(CurrentWeatherContract.CurrentWeatherEntry.COLUMN_DATE))));
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}


