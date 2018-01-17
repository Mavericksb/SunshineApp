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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.animations.SunsetAnimation;
import com.example.android.sunshine.data.HourlyWeatherContract;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.databinding.ActivityDetailBinding;
import com.example.android.sunshine.sync.SunshineSyncUtils;
import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;

import java.net.URI;

public class HourlyFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        HourlyForecastAdapter.HourlyForecastAdapterOnClickHandler {

//    private static Loader hourlyLoader;


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
    private static final int ID_TODAY_LOADER = 46;

    private HourlyForecastAdapter mHourlyForecastAdapter;
    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;

    private ProgressBar mLoadingIndicator;
    private static LoaderManager mSupportLoaderManager;
    private static LoaderManager.LoaderCallbacks mLoaderCallbacks;

    private static Uri mOldUri;
    private static Uri mOldTodayUri;
    private static Uri mUri;
    private static Uri mTodayUri;

    private static Cursor mTodayCursor;
    private static Cursor mHourlyCursor;
    private static Cursor mMergedCursor;

    private long sunrise;
    private long sunset;

    private View mItemTodaySummaryView;

    public static final String URI_WITH_DATE = "uri";
    public static final String URI_TODAY_WITH_DATE = "today_uri";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        View hourlyForecastView = inflater.inflate(R.layout.activity_hourly, container, false);
        mItemTodaySummaryView = hourlyForecastView.findViewById(R.id.today_summary);

        Uri uri = getArguments().getParcelable(URI_WITH_DATE);
        Uri todayUri = getArguments().getParcelable(URI_TODAY_WITH_DATE);

        if (uri == null) throw new NullPointerException("URI for DetailActivity cannot be null");
        if (uri != mUri) {
            mUri = uri;
            mTodayUri = todayUri;
        }

        mRecyclerView = (RecyclerView) hourlyForecastView.findViewById(R.id.recyclerview_hourly_forecast);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = (ProgressBar) hourlyForecastView.findViewById(R.id.pb_hourly_loading_indicator);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        /*
         * HourlyForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         *
         * Although passing in "this" twice may seem strange, it is actually a sign of separation
         * of concerns, which is best programming practice. The ForecastAdapter requires an
         * Android Context (which all Activities are) as well as an onClickHandler. Since our
         * ForecastFragment implements the ForecastAdapter ForecastOnClickHandler interface, "this"
         * is also an instance of that type of handler.
         */
        mHourlyForecastAdapter = new HourlyForecastAdapter(getActivity(), this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mHourlyForecastAdapter);


        showLoading();

        mLoaderCallbacks = this;
        mSupportLoaderManager = getActivity().getSupportLoaderManager();

        mSupportLoaderManager.initLoader(ID_TODAY_LOADER, null, mLoaderCallbacks);
        mSupportLoaderManager.initLoader(ID_HOURLY_FORECAST_LOADER, null, mLoaderCallbacks);

        if (!mUri.equals(mOldUri)) {
            reload();
            mOldUri = mUri;
            mOldTodayUri = mTodayUri;
        }

        return hourlyForecastView;

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

                return new CursorLoader(getActivity(),
                        mUri,
                        HOURLY_WEATHER_DETAIL_PROJECTION,
                        null, //HourlyWeatherContract.HourlyWeatherEntry.COLUMN_CITY_ID + "=?",
                        null, //selectionArgs,
                        sortOrder);
            case ID_TODAY_LOADER:
                return new CursorLoader(getActivity(),
                        mTodayUri,
                        ForecastFragment.MAIN_FORECAST_PROJECTION,
                        null, //HourlyWeatherContract.HourlyWeatherEntry.COLUMN_CITY_ID + "=?",
                        null, //selectionArgs,
                        null);
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
            case ID_HOURLY_FORECAST_LOADER:
//                mHourlyCursor = data;
                mHourlyForecastAdapter.swapCursor(data);
                break;
            case ID_TODAY_LOADER:
//                mTodayCursor = data;
                populateTodaySummary(data);
                break;
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }


//        if (mTodayCursor != null && mHourlyCursor != null) {
//            if (mTodayCursor.getCount() != 0 && mHourlyCursor.getCount() != 0) {
//                mTodayCursor.moveToFirst();
//                mHourlyCursor.moveToFirst();
////                sunrise = mTodayCursor.getLong(ForecastFragment.INDEX_SUNRISE_TIME);
////                sunset = mTodayCursor.getLong(ForecastFragment.INDEX_SUNSET_TIME);
//
//
////                mMergedCursor = new MergeCursor(new Cursor[]{mTodayCursor, mHourlyCursor});
//                populateTodaySummary(mTodayCursor);
//                mHourlyForecastAdapter.swapCursor(mHourlyCursor);
//                mTodayCursor = null;
//                mHourlyCursor = null;
//                showWeatherDataView();
//            }
            showWeatherDataView();
            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            mRecyclerView.smoothScrollToPosition(mPosition);
        }

    private void populateTodaySummary(Cursor mCursor) {

        if(getActivity() == null){
            return;
        }
//        Context context = getActivity();
//
        mCursor.moveToFirst();

        TextView summaryHighTemp = (TextView) mItemTodaySummaryView.findViewById(R.id.high_temperature);
        TextView summaryLowTemp = (TextView) mItemTodaySummaryView.findViewById(R.id.low_temperature);
        TextView summaryHumidity = (TextView) mItemTodaySummaryView.findViewById(R.id.hourlyTextViewHumidity);
        TextView summaryWindSpeed = (TextView) mItemTodaySummaryView.findViewById(R.id.hourlyTextViewWind);
        TextView summaryPressure = (TextView) mItemTodaySummaryView.findViewById(R.id.hourlyTextViewPressure);
        TextView evolutionSummary = (TextView) mItemTodaySummaryView.findViewById(R.id.hourly_summary_text_view);
        ImageView iconView = (ImageView) mItemTodaySummaryView.findViewById(R.id.hourly_weather_icon);
        TextView dateView = (TextView) mItemTodaySummaryView.findViewById(R.id.hourly_date);
        TextView descriptionView = (TextView) mItemTodaySummaryView.findViewById(R.id.hourly_weather_description);
        TextView sunriseTime = (TextView) mItemTodaySummaryView.findViewById(R.id.sunrise_time);
        TextView sunsetTime = (TextView) mItemTodaySummaryView.findViewById(R.id.sunset_time);
        ImageView sunsetIcon = (ImageView) mItemTodaySummaryView.findViewById(R.id.imageview_sunset);

        SunsetAnimation sunsetAnimation = new SunsetAnimation(sunsetIcon, 70);
        sunsetAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        sunsetAnimation.setRepeatCount(Animation.INFINITE);
        sunsetAnimation.setDuration(10000);
        sunsetIcon.startAnimation(sunsetAnimation);

        long sunrise = mCursor.getLong(ForecastFragment.INDEX_SUNRISE_TIME);
        String sunriseString = SunshineDateUtils.getHourlyDetailDate(sunrise, 1);
        sunriseTime.setText(sunriseString);

        long sunset = mCursor.getLong(ForecastFragment.INDEX_SUNSET_TIME);
        String sunsetString = SunshineDateUtils.getHourlyDetailDate(sunset, 1);
        sunsetTime.setText(sunsetString);


        String weatherId = mCursor.getString(ForecastFragment.INDEX_WEATHER_CONDITION_ID);
        int weatherImageId = SunshineWeatherUtils.getDSLargeArtResourceIdForWeatherCondition(weatherId);

        iconView.setImageResource(weatherImageId);

        /****************
         * Weather Date *
         ****************/
         /* Read date from the cursor */
        long dateInMillis = mCursor.getLong(ForecastFragment.INDEX_WEATHER_DATE);
         /* Get human readable string using our utility method */
        //String dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis, false);
        String dateString = SunshineDateUtils.getDailyDetailDate(getActivity(), dateInMillis, 1);
        dateView.setText(dateString.toUpperCase());

        /***********************
         * Weather Description *
         ***********************/
        if(weatherId!=null) {
            String description = SunshineWeatherUtils.getDSStringForWeatherCondition(getActivity(), weatherId);
         /* Create the accessibility (a11y) String from the weather description */
            String descriptionA11y = getActivity().getString(R.string.a11y_forecast, description);


         /* Set the text and content description (for accessibility purposes) */
            descriptionView.setText(description);
            descriptionView.setContentDescription(descriptionA11y);
        }

        double highTemp = mCursor.getDouble(ForecastFragment.INDEX_WEATHER_MAX_TEMP);
        String highTempString = "Max: " + SunshineWeatherUtils.formatTemperature(getActivity(), highTemp) + " ";
         /* Create the accessibility (a11y) String from the weather description */
        summaryHighTemp.setText(highTempString);

        double lowTemp = mCursor.getDouble(ForecastFragment.INDEX_WEATHER_MIN_TEMP);
        String lowTempString = "Min: " + SunshineWeatherUtils.formatTemperature(getActivity(), lowTemp);
         /* Create the accessibility (a11y) String from the weather description */
        summaryLowTemp.setText(lowTempString);

        int windSpeed = (int) mCursor.getDouble(ForecastFragment.INDEX_WIND_SPEED);
        String windSpeedString = windSpeed + " Kmh";
        summaryWindSpeed.setText(windSpeedString);

        int humidity = (int) mCursor.getDouble(ForecastFragment.INDEX_CURRENT_HUMIDITY);
        String humidityString = humidity + "%";
        summaryHumidity.setText(humidityString);

        double pressure = mCursor.getDouble(ForecastFragment.INDEX_PRESSURE);
        String pressureString = pressure + " hPa";
        summaryPressure.setText(pressureString);

        String evolution = mCursor.getString(ForecastFragment.INDEX_SUMMARY);
        evolutionSummary.setText(evolution);
        evolutionSummary.setSelected(true);

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
        Intent weatherDetailIntent = new Intent(getActivity(), DetailActivity.class);
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


    public static void reload() {
        mSupportLoaderManager.restartLoader(ID_TODAY_LOADER, null, mLoaderCallbacks);
        mSupportLoaderManager.restartLoader(ID_HOURLY_FORECAST_LOADER, null, mLoaderCallbacks);
    }

//    @Override
//    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
//        if (nextAnim == 0) {
//            return super.onCreateAnimation(transit, enter, nextAnim);
//        }
//
//        Animation anim = android.view.animation.AnimationUtils.loadAnimation(getContext(), nextAnim);
//        anim.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {}
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                // Do any process intensive work that can wait until after fragment has loaded
//
//
//
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {}
//        });
//        return anim;
//    }

}
