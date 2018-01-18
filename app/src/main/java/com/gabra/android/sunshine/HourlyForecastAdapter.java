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
package com.gabra.android.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gabra.android.sunshine.utilities.SunshineDateUtils;
import com.gabra.android.sunshine.utilities.SunshineWeatherUtils;

import java.util.Locale;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.support.v7.widget.RecyclerView}.
 */
class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.HourlyForecastAdapterViewHolder> {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;

    /*
 * Below, we've defined an interface to handle clicks on items within this Adapter. In the
 * constructor of our ForecastAdapter, we receive an instance of a class that has implemented
 * said interface. We store that instance in this variable to call the onClick method whenever
 * an item is clicked in the list.
 */
    final private HourlyForecastAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface HourlyForecastAdapterOnClickHandler {
        void onClick(long date);
    }
    /*
     * Flag to determine if we want to use a separate view for the list item that represents
     * today. This flag will be true when the phone is in portrait mode and false when the phone
     * is in landscape. This flag will be set in the constructor of the adapter by accessing
     * boolean resources.
     */
    private boolean mUseTodayLayout;

    private Cursor mCursor;

    /**
     * Creates a ForecastAdapter.
     *
     * @param context      Used to talk to the UI and app resources
     */
    public HourlyForecastAdapter(@NonNull Context context, HourlyForecastAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
        mUseTodayLayout = mContext.getResources().getBoolean(R.bool.use_today_layout);
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (like ours does) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new ForecastAdapterViewHolder that holds the View for each list item
     */
    @Override
    public HourlyForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        int layoutId;

        switch (viewType) {

            case VIEW_TYPE_TODAY: {
                layoutId = R.layout.list_item_hourly_summary;
                break;
            }

            case VIEW_TYPE_FUTURE_DAY: {
                layoutId = R.layout.list_item_hourly_forecast;
                break;
            }

            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }

        View view = LayoutInflater.from(mContext).inflate(layoutId, viewGroup, false);


        view.setFocusable(true);

        return new HourlyForecastAdapterViewHolder(view, viewType);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param hourlyForecastAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(HourlyForecastAdapterViewHolder hourlyForecastAdapterViewHolder, int position) {

                setDataHourly(hourlyForecastAdapterViewHolder, position);
    }

    private void setDataHourly(HourlyForecastAdapterViewHolder hourlyForecastAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);

        String weatherId = mCursor.getString(HourlyFragment.INDEX_WEATHER_CONDITION_ID);
        int weatherImageId = SunshineWeatherUtils.getDSSmallArtResourceIdForWeatherCondition(weatherId);

        hourlyForecastAdapterViewHolder.iconView.setImageResource(weatherImageId);

        /****************
         * Weather Date *
         ****************/
         /* Read date from the cursor */
        long dateInMillis = mCursor.getLong(HourlyFragment.INDEX_WEATHER_DATE);
         /* Get human readable string using our utility method */
        //String dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis, false);
        String dateString = SunshineDateUtils.getHourlyDetailDate(dateInMillis, VIEW_TYPE_FUTURE_DAY);

         /* Display friendly date string */
        hourlyForecastAdapterViewHolder.dateView.setText(dateString);

        /***********************
         * Weather Description *
         ***********************/
        String description = SunshineWeatherUtils.getDSStringForWeatherCondition(mContext, weatherId);
         /* Create the accessibility (a11y) String from the weather description */
        String descriptionA11y = mContext.getString(R.string.a11y_forecast, description);

         /* Set the text and content description (for accessibility purposes) */
        hourlyForecastAdapterViewHolder.descriptionView.setText(description);
        hourlyForecastAdapterViewHolder.descriptionView.setContentDescription(descriptionA11y);

        double temperature = mCursor.getDouble(HourlyFragment.INDEX_WEATHER_TEMPERATURE);
        String tempString = SunshineWeatherUtils.formatTemperature(mContext, temperature);
         /* Create the accessibility (a11y) String from the weather description */
        String highA11y = mContext.getString(R.string.a11y_temp, tempString);

         /* Set the text and content description (for accessibility purposes) */
        hourlyForecastAdapterViewHolder.tempView.setText(tempString);
        hourlyForecastAdapterViewHolder.tempView.setContentDescription(highA11y);

        double precipInt = mCursor.getDouble(HourlyFragment.INDEX_WEATHER_PRECIP_INTENSITY);

        String precipIntensityString = SunshineWeatherUtils.getPrecipIntensity(mContext, precipInt);
        hourlyForecastAdapterViewHolder.precipIntensity.setText(precipIntensityString);
        if(precipInt > 0.2) {
            hourlyForecastAdapterViewHolder.precipIntensityValue.setVisibility(View.VISIBLE);
            hourlyForecastAdapterViewHolder.precipIntensity.setVisibility(View.VISIBLE);
            hourlyForecastAdapterViewHolder.noPrecip.setVisibility(View.GONE);
            String precipIntensityValue = String.format(Locale.getDefault(), "%.2fmm", precipInt);
            hourlyForecastAdapterViewHolder.precipIntensityValue.setText(precipIntensityValue);
        } else {
            hourlyForecastAdapterViewHolder.precipIntensity.setVisibility(View.GONE);
            hourlyForecastAdapterViewHolder.precipIntensityValue.setVisibility(View.GONE);
            hourlyForecastAdapterViewHolder.noPrecip.setVisibility(View.VISIBLE);
        }

        double wind = mCursor.getDouble(HourlyFragment.INDEX_WEATHER_WIND_SPEED);
        String windSpeed = String.format(Locale.getDefault(),"%.1fKm/h", wind);
        hourlyForecastAdapterViewHolder.windSpeed.setText(windSpeed);


    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    /**
     * Returns an integer code related to the type of View we want the ViewHolder to be at a given
     * position. This method is useful when we want to use different layouts for different items
     * depending on their position. In Sunshine, we take advantage of this method to provide a
     * different layout for the "today" layout. The "today" layout is only shown in portrait mode
     * with the first item in the list.
     *
     * @param position index within our RecyclerView and Cursor
     * @return the view type (today or future day)
     */
    @Override
    public int getItemViewType(int position) {

            return VIEW_TYPE_FUTURE_DAY;
    }

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    class HourlyForecastAdapterViewHolder extends RecyclerView.ViewHolder  {
        final ImageView iconView;

        final TextView dateView;
        final TextView descriptionView;
        final TextView tempView;
        TextView precipIntensity;
        TextView precipIntensityValue;
        TextView windSpeed;
        View noPrecip;


        HourlyForecastAdapterViewHolder(View view, int viewType) {
            super(view);


            iconView = (ImageView) view.findViewById(R.id.hourly_weather_icon);
            dateView = (TextView) view.findViewById(R.id.hourly_date);
            descriptionView = (TextView) view.findViewById(R.id.hourly_weather_description);
            tempView = (TextView) view.findViewById(R.id.hourly_temperature);
            precipIntensity = (TextView) view.findViewById(R.id.hourly_precip_intensity);
            precipIntensityValue = (TextView) view.findViewById(R.id.hourly_precip_intensity_value);
            windSpeed = (TextView) view.findViewById(R.id.hourly_wind_speed);
            noPrecip = (View) view.findViewById(R.id.hourly_no_precip);


//            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click. We fetch the date that has been
         * selected, and then call the onClick handler registered with this adapter, passing that
         * date.
         *
         * @param v the View that was clicked
         */
//        @Override
//        public void onClick(View v) {
//            int adapterPosition = getAdapterPosition();
//            mCursor.moveToPosition(adapterPosition);
//            long dateInMillis = mCursor.getLong(HourlyFragment.INDEX_WEATHER_DATE);
//        }
    }
}