package com.example.android.sunshine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.data.CurrentWeatherContract;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.sync.SunshineSyncUtils;

import java.lang.ref.WeakReference;

/**
 * Created by Robert on 19/09/2017.
 */



public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String FORECAST_TAG = "forecast_fragment";
    public static final String HOURLY_TAG = "hourly_fragment";

    private static final int ID_CURRENT_LOADER_BACKGROUND = 101;
    private static final int ID_WEATHER_LOADER_BACKGROUND = 102;

    private static Cursor mCurrentBackgroundCursor;
    private static Cursor mForecastBackgroundCursor;

    private ImageAnimator mImageAnimator;

    private static String mWeatherId = null;
    private static long mDateTime = -1;
    private static long mSunrise = -1;
    private static long mSunset = -1;


    public static final String[] CURRENT_FORECAST_PROJECTION = {
            CurrentWeatherContract.CurrentWeatherEntry.COLUMN_WEATHER_ID,
            CurrentWeatherContract.CurrentWeatherEntry.COLUMN_DATE,
    };

    public static final int INDEX_WEATHER_ID = 0;
    public static final int INDEX_DATE = 1;

    public static final String[] MAIN_FORECAST_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_SUNRISE_TIME,
            WeatherContract.WeatherEntry.COLUMN_SUNSET_TIME
    };

    public static final int INDEX_SUNRISE_TIME = 0;
    public static final int INDEX_SUNSET_TIME = 1;

    TextView mToolbarCityName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbarCityName = (TextView) findViewById(R.id.toolbar_city_name);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            Window w = getWindow(); // in Activity's onCreate() for instance
//            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        }

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

        getSupportLoaderManager().initLoader(ID_WEATHER_LOADER_BACKGROUND, null, this);
        getSupportLoaderManager().initLoader(ID_CURRENT_LOADER_BACKGROUND, null, this);


        if(mWeatherId != null && mDateTime != -1) {
            mImageAnimator.playAnimation(mWeatherId, mDateTime, mSunrise, mSunset, true);
        }

        SunshineSyncUtils.initialize(this);
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
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {

        switch (loaderId) {


            case ID_CURRENT_LOADER_BACKGROUND:
                return new CursorLoader(this,
                        CurrentWeatherContract.CurrentWeatherEntry.CONTENT_URI,
                        CURRENT_FORECAST_PROJECTION,
                        null,
                        null,
                        null);
            case ID_WEATHER_LOADER_BACKGROUND:

                /* URI for all rows of weather data in our weather table */
                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
                /* Sort order: Ascending by date */
                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
                return new CursorLoader(this,
                        forecastQueryUri,
                        MAIN_FORECAST_PROJECTION,
                        null, //selection,
                        null, //selectionArgs,
                        sortOrder);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {

        int loaderId = loader.getId();
        switch (loaderId) {
            case ID_CURRENT_LOADER_BACKGROUND:
                mCurrentBackgroundCursor = data;
                break;
            case ID_WEATHER_LOADER_BACKGROUND:
                mForecastBackgroundCursor = data;
                break;
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }

        if (mCurrentBackgroundCursor != null && mForecastBackgroundCursor != null) {
            if (mCurrentBackgroundCursor.getCount() != 0 && mForecastBackgroundCursor.getCount() != 0) {
                mCurrentBackgroundCursor.moveToFirst();
                mForecastBackgroundCursor.moveToFirst();
                mWeatherId = mCurrentBackgroundCursor.getString(INDEX_WEATHER_ID);
                mDateTime = mCurrentBackgroundCursor.getLong(INDEX_DATE);
                mSunrise = mForecastBackgroundCursor.getLong(INDEX_SUNRISE_TIME);
                mSunset = mForecastBackgroundCursor.getLong(INDEX_SUNSET_TIME);
                mImageAnimator.playAnimation(mWeatherId, mDateTime, mSunrise, mSunset, false);
                mForecastBackgroundCursor=null;
                mToolbarCityName.setText(SunshinePreferences.getCityName(this));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}


