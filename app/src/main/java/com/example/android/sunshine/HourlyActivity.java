/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.sunshine.data.HourlyWeatherContract;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.databinding.ActivityDetailBinding;
import com.example.android.sunshine.sync.SunshineSyncUtils;
import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;

public class HourlyActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        HourlyForecastAdapter.HourlyForecastAdapterOnClickHandler{

    /*
     * In this Activity, you can share the selected day's forecast. No social sharing is complete
     * without using a hashtag. #BeTogetherNotTheSame
     */
    private static final String HOURLY_FORECAST_SHARE_HASHTAG = " #SunshineApp";

    /*
     * The columns of data that we are interested in displaying within our DetailActivity's
     * weather display.
     */
    public static final String[] HOURLY_WEATHER_DETAIL_PROJECTION = {
            HourlyWeatherContract.HourlyWeatherEntry.COLUMN_DATE,
            HourlyWeatherContract.HourlyWeatherEntry.COLUMN_SUMMARY,
            HourlyWeatherContract.HourlyWeatherEntry.COLUMN_PRECIP_INTENSITY,
            HourlyWeatherContract.HourlyWeatherEntry.COLUMN_PRECIP_PROBABILITY,
            HourlyWeatherContract.HourlyWeatherEntry.COLUMN_TEMPERATURE,
            HourlyWeatherContract.HourlyWeatherEntry.COLUMN_HUMIDITY,
            HourlyWeatherContract.HourlyWeatherEntry.COLUMN_PRESSURE,
            HourlyWeatherContract.HourlyWeatherEntry.COLUMN_WIND_SPEED,
            HourlyWeatherContract.HourlyWeatherEntry.COLUMN_DEGREES,
            HourlyWeatherContract.HourlyWeatherEntry.COLUMN_WEATHER_ID
    };

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able
     * to access the data from our query. If the order of the Strings above changes, these
     * indices must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_SUMMARY = 1;
    public static final int INDEX_WEATHER_PRECIP_INTENSITY = 2;
    public static final int INDEX_WEATHER_PRECIP_PROBABILITY = 3;
    public static final int INDEX_WEATHER_TEMPERATURE = 4;
    public static final int INDEX_WEATHER_HUMIDITY = 5;
    public static final int INDEX_WEATHER_PRESSURE = 6;
    public static final int INDEX_WEATHER_WIND_SPEED = 7;
    public static final int INDEX_WEATHER_DEGREES = 8;
    public static final int INDEX_WEATHER_CONDITION_ID = 9;

    /*
     * This ID will be used to identify the Loader responsible for loading the weather details
     * for a particular day. In some cases, one Activity can deal with many Loaders. However, in
     * our case, there is only one. We will still use this ID to initialize the loader and create
     * the loader for best practice. Please note that 353 was chosen arbitrarily. You can use
     * whatever number you like, so long as it is unique and consistent.
     */
    private static final int ID_HOURLY_FORECAST_LOADER = 453;

    private HourlyForecastAdapter mHourlyForecastAdapter;
    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;

    private ProgressBar mLoadingIndicator;
    private static LoaderManager mSupportLoaderManager;
    private static LoaderManager.LoaderCallbacks mLoaderCallbacks;

    private Uri mUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hourly);
        getSupportActionBar().setElevation(0f);

        mUri = getIntent().getData();
        if (mUri == null) throw new NullPointerException("URI for DetailActivity cannot be null");



        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_hourly_forecast);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_hourly_loading_indicator);

        /*
         * A LinearLayoutManager is responsible for measuring and positioning item views within a
         * RecyclerView into a linear list. This means that it can produce either a horizontal or
         * vertical list depending on which parameter you pass in to the LinearLayoutManager
         * constructor. In our case, we want a vertical list, so we pass in the constant from the
         * LinearLayoutManager class for vertical lists, LinearLayoutManager.VERTICAL.
         *
         * There are other LayoutManagers available to display your data in uniform grids,
         * staggered grids, and more! See the developer documentation for more details.
         *
         * The third parameter (shouldReverseLayout) should be true if you want to reverse your
         * layout. Generally, this is only true with horizontal lists that need to support a
         * right-to-left layout.
         */
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        /*
         * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         *
         * Although passing in "this" twice may seem strange, it is actually a sign of separation
         * of concerns, which is best programming practice. The ForecastAdapter requires an
         * Android Context (which all Activities are) as well as an onClickHandler. Since our
         * MainActivity implements the ForecastAdapter ForecastOnClickHandler interface, "this"
         * is also an instance of that type of handler.
         */
        mHourlyForecastAdapter = new HourlyForecastAdapter(this, this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mHourlyForecastAdapter);


        showLoading();

        /*
         * Ensures a loader is initialized and active. If the loader doesn't already exist, one is
         * created and (if the activity/fragment is currently started) starts the loader. Otherwise
         * the last created loader is re-used.
         */
        mSupportLoaderManager = getSupportLoaderManager();
        mLoaderCallbacks = this;
        mSupportLoaderManager.initLoader(ID_HOURLY_FORECAST_LOADER, null, this);

        SunshineSyncUtils.initialize(this);

    }

    /**
     * Called by the {@link android.support.v4.app.LoaderManagerImpl} when a new Loader needs to be
     * created. This Activity only uses one loader, so we don't necessarily NEED to check the
     * loaderId, but this is certainly best practice.
     *
     * @param loaderId The loader ID for which we need to create a loader
     * @param bundle   Any arguments supplied by the caller
     * @return A new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {


        switch (loaderId) {

            case ID_HOURLY_FORECAST_LOADER:

                /* Sort order: Ascending by date */
                String sortOrder = HourlyWeatherContract.HourlyWeatherEntry.COLUMN_DATE + " ASC";

                return new CursorLoader(this,
                        mUri,
                        HOURLY_WEATHER_DETAIL_PROJECTION,
                        null, //HourlyWeatherContract.HourlyWeatherEntry.COLUMN_CITY_ID + "=?",
                        null, //selectionArgs,
                        sortOrder);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    /**
     * Called when a Loader has finished loading its data.
     *
     * NOTE: There is one small bug in this code. If no data is present in the cursor do to an
     * initial load being performed with no access to internet, the loading indicator will show
     * indefinitely, until data is present from the ContentProvider. This will be fixed in a
     * future version of the course.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {


        mHourlyForecastAdapter.swapCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);
        if (data.getCount() != 0) showWeatherDataView();
        else{
            Toast noData = Toast.makeText(this, "There is no forecast for this day", Toast.LENGTH_SHORT);
            noData.show();
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus making its data unavailable.
     * The application should at this point remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        /*
         * Since this Loader's data is now invalid, we need to clear the Adapter that is
         * displaying the data.
         */
        mHourlyForecastAdapter.swapCursor(null);
    }

    /**
     * This method is for responding to clicks from our list.
     *
     * @param date Normalized UTC time that represents the local date of the weather in GMT time.
     * @see WeatherContract.WeatherEntry#COLUMN_DATE
     */
    @Override
    public void onClick(long date) {
        Intent weatherDetailIntent = new Intent(this, DetailActivity.class);
        Uri uriForDateClicked = WeatherContract.WeatherEntry.buildWeatherUriWithDate(date);
        weatherDetailIntent.setData(uriForDateClicked);
        startActivity(weatherDetailIntent);
    }

    /**
     * This method will make the View for the weather data visible and hide the error message and
     * loading indicator.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each view is currently visible or invisible.
     */
    private void showWeatherDataView() {
        /* First, hide the loading indicator */
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        /* Finally, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the loading indicator visible and hide the weather View and error
     * message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each view is currently visible or invisible.
     */
    private void showLoading() {
        /* Then, hide the weather data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Finally, show the loading indicator */
        mLoadingIndicator.setVisibility(View.VISIBLE);
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

    public static void reload(){
        mSupportLoaderManager.restartLoader(ID_HOURLY_FORECAST_LOADER, null, mLoaderCallbacks);
    }


}
