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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.gabra.android.sunshine.utilities.SunshineDateUtils;
import com.gabra.android.sunshine.utilities.SunshineWeatherUtils;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.support.v7.widget.RecyclerView}.
 */
class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastAdapterViewHolder> {

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
    final private ForecastAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface ForecastAdapterOnClickHandler {
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
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public ForecastAdapter(@NonNull Context context, ForecastAdapterOnClickHandler clickHandler) {
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
    public ForecastAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        int layoutId;

        switch (viewType) {

            case VIEW_TYPE_TODAY: {
                layoutId = R.layout.list_item_forecast_today;
                break;
            }

            case VIEW_TYPE_FUTURE_DAY: {
                layoutId = R.layout.forecast_list_item;
                break;
            }

            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }

        View view = LayoutInflater.from(mContext).inflate(layoutId, viewGroup, false);


        view.setFocusable(true);

        return new ForecastAdapterViewHolder(view, viewType);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param forecastAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ForecastAdapterViewHolder forecastAdapterViewHolder, int position) {

        int viewType = getItemViewType(position);

        switch (viewType) {

            case VIEW_TYPE_TODAY:
                setDataToday(forecastAdapterViewHolder, position);
                break;

            case VIEW_TYPE_FUTURE_DAY:
                setDataFutureDay(forecastAdapterViewHolder, position);
                break;

            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }
    }



    private void setDataToday(final ForecastAdapterViewHolder forecastAdapterViewHolder, int position) {
        mCursor.moveToPosition(position);

        /****************
         * Weather Icon *
         ****************/
        String weatherId = mCursor.getString(ForecastFragment.INDEX_CURRENT_WEATHER_CONDITION_ID);
        int weatherImageId = SunshineWeatherUtils
                        .getDSLargeArtResourceIdForWeatherCondition(weatherId);

        forecastAdapterViewHolder.iconView.setImageResource(weatherImageId);

        /****************
         * Weather Date *
         ****************/
         /* Read date from the cursor */
        long dateInMillis = mCursor.getLong(ForecastFragment.INDEX_CURRENT_WEATHER_DATE);
         /* Get human readable string using our utility method */
        //String dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis, false);
        String dateString = SunshineDateUtils.getDailyDetailDate(mContext, dateInMillis, VIEW_TYPE_TODAY);

         /* Display friendly date string */
        forecastAdapterViewHolder.dateView.setText(dateString);

        /***********************
         * Weather Description *
         ***********************/
        String description = SunshineWeatherUtils.getDSStringForWeatherCondition(mContext, weatherId);
         /* Create the accessibility (a11y) String from the weather description */
        String descriptionA11y = mContext.getString(R.string.a11y_forecast, description);

         /* Set the text and content description (for accessibility purposes) */
        forecastAdapterViewHolder.descriptionView.setText(description);
        forecastAdapterViewHolder.descriptionView.setContentDescription(descriptionA11y);

        double temperature = mCursor.getDouble(ForecastFragment.INDEX_CURRENT_WEATHER_TEMP);

        String temperatureString = SunshineWeatherUtils.formatTemperature(mContext, temperature);
         /* Create the accessibility (a11y) String from the weather description */
        String temperatureA11y = mContext.getString(R.string.a11y_temp, temperatureString);

         /* Set the text and content description (for accessibility purposes) */
        forecastAdapterViewHolder.currentTemperature.setText(temperatureString);
        forecastAdapterViewHolder.currentTemperature.setContentDescription(temperatureA11y);

        int humidity = (int) mCursor.getDouble(ForecastFragment.INDEX_CURRENT_HUMIDITY);
        String humidityString = humidity + "%";
        forecastAdapterViewHolder.currentHumidity.setText(humidityString);

        int windSpeed = (int) mCursor.getDouble(ForecastFragment.INDEX_CURRENT_WIND_SPEED);
        String windSpeedString = windSpeed + " Kmh";
        forecastAdapterViewHolder.currentWindSpeed.setText(windSpeedString);

        double pressure = mCursor.getDouble(ForecastFragment.INDEX_CURRENT_PRESSURE);
        String pressureString = pressure + " hPa";
        forecastAdapterViewHolder.currentPressure.setText(pressureString);

        final String dailySummary = mCursor.getString(ForecastFragment.INDEX_DAILY_SUMMARY);
        final String weekSummary = mCursor.getString(ForecastFragment.INDEX_WEEK_SUMMARY);

        forecastAdapterViewHolder.evolutionSummary.setText(dailySummary);

        forecastAdapterViewHolder.evolutionButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    forecastAdapterViewHolder.evolutionSummary.setText(weekSummary);
                } else {
                    forecastAdapterViewHolder.evolutionSummary.setText(dailySummary);
                }
            }
        });
        forecastAdapterViewHolder.evolutionSummary.setSelected(true);



        /**************************
         * High (max) temperature *
         **************************/
         /* Read high temperature from the cursor (in degrees celsius) */
         /* GO TO NEXT POSITION IN CMERGED CURSOR, WHICH IS DATA FROM WHEATHER CURSOR */
        mCursor.moveToNext();
        double highInCelsius = mCursor.getDouble(ForecastFragment.INDEX_WEATHER_MAX_TEMP);
         /*
          * If the user's preference for weather is fahrenheit, formatTemperature will convert
          * the temperature. This method will also append either °C or °F to the temperature
          * String.
          */
        String highString = "Max: " + SunshineWeatherUtils.formatTemperature(mContext, highInCelsius) + " ";
//        Spannable highSpan = new SpannableString(highString);
//        highSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.high_temp_text)), highSpan.length()-1, highSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );

         /* Create the accessibility (a11y) String from the weather description */
        String highA11y = mContext.getString(R.string.a11y_high_temp, highString);

         /* Set the text and content description (for accessibility purposes) */
        forecastAdapterViewHolder.highTempView.setText(highString);
        forecastAdapterViewHolder.highTempView.setContentDescription(highA11y);

        /*************************
         * Low (min) temperature *
         *************************/
         /* Read low temperature from the cursor (in degrees celsius) */
        double lowInCelsius = mCursor.getDouble(ForecastFragment.INDEX_WEATHER_MIN_TEMP);

         /*
          * If the user's preference for weather is fahrenheit, formatTemperature will convert
          * the temperature. This method will also append either °C or °F to the temperature
          * String.
          */
        String lowString = "Min:" + SunshineWeatherUtils.formatTemperature(mContext, lowInCelsius);
//        Spannable lowSpan = new SpannableString(lowString);
//        lowSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.low_temp_text)), lowSpan.length()-1, lowSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
        String lowA11y = mContext.getString(R.string.a11y_low_temp, lowString);

         /* Set the text and content description (for accessibility purposes) */
        forecastAdapterViewHolder.lowTempView.setText(lowString);
        forecastAdapterViewHolder.lowTempView.setContentDescription(lowA11y);
    }

    private void setDataFutureDay(ForecastAdapterViewHolder forecastAdapterViewHolder, int position) {

//        if((position+1) <= mCursor.getCount()) {
            mCursor.moveToPosition(position+1);
//        } else{
//            return;
//        }

        /****************
         * Weather Icon *
         ****************/
        String weatherId = mCursor.getString(ForecastFragment.INDEX_WEATHER_CONDITION_ID);
        int weatherImageId = SunshineWeatherUtils
                        .getDSSmallArtResourceIdForWeatherCondition(weatherId);


        forecastAdapterViewHolder.iconView.setImageResource(weatherImageId);

        /****************
         * Weather Date *
         ****************/
         /* Read date from the cursor */
        long dateInMillis = mCursor.getLong(ForecastFragment.INDEX_WEATHER_DATE);
         /* Get human readable string using our utility method */
        //String dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis, false);
        String dateString = SunshineDateUtils.getDailyDetailDate(mContext, dateInMillis, VIEW_TYPE_FUTURE_DAY);

         /* Display friendly date string */
        forecastAdapterViewHolder.dateView.setText(dateString);

        /***********************
         * Weather Description *
         ***********************/
        String description = SunshineWeatherUtils.getDSStringForWeatherCondition(mContext, weatherId);
         /* Create the accessibility (a11y) String from the weather description */
        String descriptionA11y = mContext.getString(R.string.a11y_forecast, description);

         /* Set the text and content description (for accessibility purposes) */
        forecastAdapterViewHolder.descriptionView.setText(description);
        forecastAdapterViewHolder.descriptionView.setContentDescription(descriptionA11y);

        /**************************
         * High (max) temperature *
         **************************/
         /* Read high temperature from the cursor (in degrees celsius) */
        double highInCelsius = mCursor.getDouble(ForecastFragment.INDEX_WEATHER_MAX_TEMP);
         /*
          * If the user's preference for weather is fahrenheit, formatTemperature will convert
          * the temperature. This method will also append either °C or °F to the temperature
          * String.
          */
        String highString = SunshineWeatherUtils.formatTemperature(mContext, highInCelsius);
         /* Create the accessibility (a11y) String from the weather description */
        String highA11y = mContext.getString(R.string.a11y_high_temp, highString);

         /* Set the text and content description (for accessibility purposes) */
        forecastAdapterViewHolder.highTempView.setText(highString);
        forecastAdapterViewHolder.highTempView.setContentDescription(highA11y);

        /*************************
         * Low (min) temperature *
         *************************/
         /* Read low temperature from the cursor (in degrees celsius) */
        double lowInCelsius = mCursor.getDouble(ForecastFragment.INDEX_WEATHER_MIN_TEMP);

         /*
          * If the user's preference for weather is fahrenheit, formatTemperature will convert
          * the temperature. This method will also append either °C or °F to the temperature
          * String.
          */
        String lowString = SunshineWeatherUtils.formatTemperature(mContext, lowInCelsius);
        String lowA11y = mContext.getString(R.string.a11y_low_temp, lowString);

         /* Set the text and content description (for accessibility purposes) */
        forecastAdapterViewHolder.lowTempView.setText(lowString);
        forecastAdapterViewHolder.lowTempView.setContentDescription(lowA11y);
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount()-1;
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
        if (mUseTodayLayout && position == 0) {
            return VIEW_TYPE_TODAY;
        } else {
            return VIEW_TYPE_FUTURE_DAY;
        }
    }

    /**
     * Swaps the cursor used by the ForecastAdapter for its weather data. This method is called by
     * ForecastFragment after a load has finished, as well as when the Loader responsible for loading
     * the weather data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param newCursor the new cursor to use as ForecastAdapter's data source
     */
    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    class ForecastAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView iconView;

        final TextView dateView;
        final TextView descriptionView;
        final TextView highTempView;
        final TextView lowTempView;
        TextView currentTemperature;
        TextView currentHumidity;
        TextView currentWindSpeed;
        TextView currentPressure;
        TextView evolutionSummary;
        ToggleButton evolutionButton;

        ForecastAdapterViewHolder(View view, int viewType) {
            super(view);

            if(viewType==VIEW_TYPE_TODAY) {
                    currentTemperature = (TextView) view.findViewById(R.id.current_temperature);
                    currentHumidity = (TextView) view.findViewById(R.id.textViewHumidity);
                    currentWindSpeed = (TextView) view.findViewById(R.id.textViewWind);
                    currentPressure = (TextView) view.findViewById(R.id.textViewPressure);
                    evolutionSummary = (TextView) view.findViewById(R.id.summary_text_view);
                evolutionButton = (ToggleButton) view.findViewById(R.id.day_week_button);

                }


            iconView = (ImageView) view.findViewById(R.id.weather_icon);
            dateView = (TextView) view.findViewById(R.id.date);
            descriptionView = (TextView) view.findViewById(R.id.weather_description);
            highTempView = (TextView) view.findViewById(R.id.high_temperature);
            lowTempView = (TextView) view.findViewById(R.id.low_temperature);

            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click. We fetch the date that has been
         * selected, and then call the onClick handler registered with this adapter, passing that
         * date.
         *
         * @param v the View that was clicked
         */
        @Override
        public void onClick(View v) {
            // we get the Adapter Position, +1 because remember first position is for Current Forecast.
            int adapterPosition = getAdapterPosition()+1;
            mCursor.moveToPosition(adapterPosition);
            long dateInMillis = mCursor.getLong(ForecastFragment.INDEX_WEATHER_DATE);
            mClickHandler.onClick(dateInMillis);

        }
    }
}