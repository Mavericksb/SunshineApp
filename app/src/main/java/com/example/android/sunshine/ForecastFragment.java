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

import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
            WeatherContract.WeatherEntry.COLUMN_SUNRISE_TIME,
            WeatherContract.WeatherEntry.COLUMN_SUNSET_TIME
    };

    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_CONDITION_ID = 3;
    public static final int INDEX_SUNRISE_TIME = 4;
    public static final int INDEX_SUNSET_TIME = 5;

    private static final int ID_CURRENT_LOADER = 43;
    private static final int ID_FORECAST_LOADER = 44;

    private static Cursor mCurrentCursor;
    private static Cursor mForecastCursor;
    private static Cursor mMergedCursor;

    private ForecastAdapter mForecastAdapter;
    private RecyclerView mRecyclerView;
    private View mEmptyView;

    private ProgressBar mLoadingIndicator;

    private static LoaderManager mSupportLoaderManager;
    private static LoaderManager.LoaderCallbacks mLoaderCallbacks;

    private static FragmentManager fm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View forecastView = inflater.inflate(R.layout.activity_forecast, container, false);

        mEmptyView = forecastView.findViewById(R.id.empty_view);
        mEmptyView.setVisibility(View.INVISIBLE);

        mRecyclerView = (RecyclerView) forecastView.findViewById(R.id.recyclerview_forecast);
        mLoadingIndicator = (ProgressBar) forecastView.findViewById(R.id.pb_loading_indicator);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mForecastAdapter = new ForecastAdapter(getActivity(), this);
        mRecyclerView.setAdapter(mForecastAdapter);

        fm = getActivity().getSupportFragmentManager();

        return forecastView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mLoaderCallbacks = this;
        mSupportLoaderManager = getActivity().getSupportLoaderManager();
        mSupportLoaderManager.initLoader(ID_CURRENT_LOADER, null, this);
        mSupportLoaderManager.initLoader(ID_FORECAST_LOADER, null, this);
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

        showLoading();

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
                        null,
                        null,
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

        int loaderId = loader.getId();
        switch (loaderId) {
            case ID_CURRENT_LOADER:
                mCurrentCursor = data;
                break;
            case ID_FORECAST_LOADER:
                mForecastCursor = data;
                break;
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }

        if (mCurrentCursor != null && mForecastCursor != null) {
            if (mCurrentCursor.getCount() != 0 && mForecastCursor.getCount() != 0) {
                mCurrentCursor.moveToFirst();
                mForecastCursor.moveToFirst();
                long sunriseTime = mForecastCursor.getLong(INDEX_SUNRISE_TIME);
                long sunsetTime = mForecastCursor.getLong(INDEX_SUNSET_TIME);
                SunshinePreferences.setRiseSetTime(getActivity(), sunriseTime, sunsetTime);

                mMergedCursor = new MergeCursor(new Cursor[]{mCurrentCursor, mForecastCursor});
                mForecastAdapter.swapCursor(mMergedCursor);
                showWeatherDataView();
            } else {
                showEmptyView();
            }
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

    private void showEmptyView() {
        /* First, hide the loading indicator */
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        /* make sure the weather data is invisible too */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* finally, make empty view visible */
        mEmptyView.setVisibility(View.VISIBLE);
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
        mEmptyView.setVisibility(View.INVISIBLE);
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
        mEmptyView.setVisibility(View.INVISIBLE);
        /* Finally, show the loading indicator */
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }


    public static void reload() {
        Fragment hourlyFragment = fm.findFragmentByTag(MainActivity.HOURLY_TAG);

        if(hourlyFragment!=null) {
            HourlyFragment.reload();
        }
        mSupportLoaderManager.restartLoader(ID_CURRENT_LOADER, null, mLoaderCallbacks);
        mSupportLoaderManager.restartLoader(ID_FORECAST_LOADER, null, mLoaderCallbacks);
    }


}
