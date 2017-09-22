/*
 * Copyright (C) 2016 The Android Open Source Project
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

import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentContainer;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.android.sunshine.data.CurrentWeatherContract;
import com.example.android.sunshine.data.HourlyWeatherContract;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.sync.SunshineSyncUtils;

public class ForecastFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ForecastAdapter.ForecastAdapterOnClickHandler {

    private final String TAG = ForecastFragment.class.getSimpleName();

    /*
     * The columns of data that we are interested in displaying within our ForecastFragment's list of
     * weather data.
     */
    public static final String[] CURRENT_FORECAST_PROJECTION = {
            CurrentWeatherContract.CurrentWeatherEntry.COLUMN_DATE,
            CurrentWeatherContract.CurrentWeatherEntry.COLUMN_TEMPERATURE,
            CurrentWeatherContract.CurrentWeatherEntry.COLUMN_WEATHER_ID,
    };

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able to
     * access the data from our query. If the order of the Strings above changes, these indices
     * must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_CURRENT_WEATHER_DATE = 0;
    public static final int INDEX_CURRENT_WEATHER_TEMP = 1;
    public static final int INDEX_CURRENT_WEATHER_CONDITION_ID = 2;

    /*
     * The columns of data that we are interested in displaying within our ForecastFragment's list of
     * weather data.
     */
    public static final String[] MAIN_FORECAST_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
    };

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able to
     * access the data from our query. If the order of the Strings above changes, these indices
     * must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_CONDITION_ID = 3;


    /*
     * This ID will be used to identify the Loader responsible for loading our weather forecast. In
     * some cases, one Activity can deal with many Loaders. However, in our case, there is only one.
     * We will still use this ID to initialize the loader and create the loader for best practice.
     * Please note that 44 was chosen arbitrarily. You can use whatever number you like, so long as
     * it is unique and consistent.
     */
    private static final int ID_CURRENT_LOADER = 43;
    private static final int ID_FORECAST_LOADER = 44;

    private static Cursor mCurrentCursor;

    private static ForecastAdapter mForecastAdapter;
    private static RecyclerView mRecyclerView;
    private static int mPosition = RecyclerView.NO_POSITION;

    private static ProgressBar mLoadingIndicator;
    private static LoaderManager mSupportLoaderManager;
    private static LoaderManager.LoaderCallbacks mLoaderCallbacks;

    private static ImageAnimator mImageAnimator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Log.e("ForecastFragment", "On create ForecastFragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.e("ForecastFragment", "On create View ForecastFragment instance NULL");
        View forecastView = inflater.inflate(R.layout.activity_forecast, container, false);

        mRecyclerView = (RecyclerView) forecastView.findViewById(R.id.recyclerview_forecast);

        mLoadingIndicator = (ProgressBar) forecastView.findViewById(R.id.pb_loading_indicator);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);

        mForecastAdapter = new ForecastAdapter(getActivity(), this);

        mRecyclerView.setAdapter(mForecastAdapter);

        showLoading();

        mLoaderCallbacks = this;
        mSupportLoaderManager = getActivity().getSupportLoaderManager();
        mSupportLoaderManager.initLoader(ID_CURRENT_LOADER, null, this);
        mSupportLoaderManager.initLoader(ID_FORECAST_LOADER, null, this);

        SunshineSyncUtils.initialize(getActivity());

        return forecastView;
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

            case ID_CURRENT_LOADER:

                return new CursorLoader(getActivity(),
                        CurrentWeatherContract.CurrentWeatherEntry.CONTENT_URI,
                        CURRENT_FORECAST_PROJECTION,
                        null,
                        null,
                        null);

            case ID_FORECAST_LOADER:
                /* URI for all rows of weather data in our weather table */
                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
                /* Sort order: Ascending by date */
                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

                return new CursorLoader(getActivity(),
                        forecastQueryUri,
                        MAIN_FORECAST_PROJECTION,
                        null, //selection,
                        null, //selectionArgs,
                        sortOrder);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    /**
     * Called when a Loader has finished loading its data.
     * <p>
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


        if (loader.getId() == ID_CURRENT_LOADER) {
            mCurrentCursor = data;

            return;
        }

        Cursor merged = new MergeCursor(new Cursor[]{mCurrentCursor, data});

        mForecastAdapter.swapCursor(merged);
        if (data.getCount() != 0) showWeatherDataView();

//        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
//        mRecyclerView.smoothScrollToPosition(mPosition);

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
        mForecastAdapter.swapCursor(null);
    }

    /**
     * This method is for responding to clicks from our list.
     *
     * @param date Normalized UTC time that represents the local date of the weather in GMT time.
     * @see WeatherContract.WeatherEntry#COLUMN_DATE
     */
    @Override
    public void onClick(long date) {
        FragmentManager fm = getActivity().getSupportFragmentManager();

        Bundle args = new Bundle();
        Uri uriForDateClicked = HourlyWeatherContract.HourlyWeatherEntry.buildWeatherUriWithDate(date);
        args.putParcelable(HourlyFragment.URI_WITH_DATE, uriForDateClicked);

        //Log.e("FORECAST FRAGMENT", "I'm here!");
        Fragment hourlyFragment = fm.findFragmentByTag(MainActivity.HOURLY_TAG);


        FragmentTransaction ft = fm.beginTransaction();
        if (hourlyFragment == null) {
            hourlyFragment = new HourlyFragment();
            hourlyFragment.setArguments(args);
            ft
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .addToBackStack(null)
                    .replace(R.id.fragment_container, hourlyFragment, MainActivity.HOURLY_TAG).commit();
        } else {
            hourlyFragment.setArguments(args);
            ft.show(hourlyFragment);
        }


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


    public static void reload() {
        mSupportLoaderManager.restartLoader(ID_FORECAST_LOADER, null, mLoaderCallbacks);
    }


}