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
package com.gabra.android.sunshine.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;

import com.gabra.android.sunshine.data.CurrentWeatherContract;
import com.gabra.android.sunshine.data.HourlyWeatherContract;
import com.gabra.android.sunshine.data.SunshinePreferences;
import com.gabra.android.sunshine.data.WeatherContract;
import com.gabra.android.sunshine.utilities.NetworkUtils;
import com.gabra.android.sunshine.utilities.NotificationUtils;
import com.gabra.android.sunshine.utilities.OpenWeatherJsonUtils;

import java.net.URL;
import java.util.ArrayList;

public class SunshineSyncTask {

    public final static String LOG_TAG = SunshineSyncTask.class.getSimpleName().toString();

    /**
     * Performs the network request for updated weather, parses the JSON from that request, and
     * inserts the new weather information into our ContentProvider. Will notify the user that new
     * weather has been loaded if the user hasn't been notified of the weather within the last day
     * AND they haven't disabled notifications in the preferences screen.
     *
     * @param context Used to access utility methods and the ContentResolver
     */
    synchronized public static void syncWeather(Context context) {

        try {
            /*
             * The getUrl method will return the URL that we need to get the forecast JSON for the
             * weather. It will decide whether to create a URL based off of the latitude and
             * longitude or off of a simple location as a String.
             */
            URL weatherRequestUrl = NetworkUtils.getUrl(context);

            /* Use the URL to retrieve the JSON */
            String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);

            ArrayList<ContentValues[]> weatherArray = OpenWeatherJsonUtils
                    .getWeatherContentValuesFromJson(context, jsonWeatherResponse);
            /* Parse the JSON into a list of weather values */
            ContentValues[] currentWeatherValues = weatherArray.get(0);
            ContentValues[] dailyWeatherValues = weatherArray.get(1);
            ContentValues[] hourlyWeatherValues = weatherArray.get(2);

            /*
             * In cases where our JSON contained an error code, getWeatherContentValuesFromJson
             * would have returned null. We need to check for those cases here to prevent any
             * NullPointerExceptions being thrown. We also have no reason to insert fresh data if
             * there isn't any to insert.
             */
            if ((currentWeatherValues != null && currentWeatherValues.length != 0) ||
            (dailyWeatherValues != null && dailyWeatherValues.length != 0) ||
            (hourlyWeatherValues != null && hourlyWeatherValues.length != 0)){
                /* Get a handle on the ContentResolver to delete and insert data */
                ContentResolver sunshineContentResolver = context.getContentResolver();

                /* Delete old weather data only if we are going to update forecast for a city already present in database
                *  so DELETE from table_name WHERE city_id = location_id
                *   for ease I'm storing location_id in Shared Preference, so I can get it from there.
                * */
                String cityId = String.valueOf(SunshinePreferences.getCityId(context));
//                Log.e("Sync Task", "City id " + cityId);

                String[] selectionArgs = new String[]{cityId};

                Uri[] contentUris = new Uri[]{CurrentWeatherContract.CurrentWeatherEntry.CONTENT_URI,
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        HourlyWeatherContract.HourlyWeatherEntry.CONTENT_URI};
                String[] selectionUris = new String[]{CurrentWeatherContract.CurrentWeatherEntry.COLUMN_CITY_ID,
                        WeatherContract.WeatherEntry.COLUMN_CITY_ID,
                        HourlyWeatherContract.HourlyWeatherEntry.COLUMN_CITY_ID};




                for(int i = 0; i < contentUris.length; i++) {
                    long delRows = sunshineContentResolver.delete(
                            contentUris[i],
                            selectionUris[i] + "=?",
                            selectionArgs);

                /* Insert our new weather data into Sunshine's ContentProvider */
                    long insRows = sunshineContentResolver.bulkInsert(
                            contentUris[i],
                            weatherArray.get(i));

                }


                /*
                 * Finally, after we insert data into the ContentProvider, determine whether or not
                 * we should notify the user that the weather has been refreshed.
                 */
                boolean notificationsEnabled = SunshinePreferences.areNotificationsEnabled(context);

                /*
                 * If the last notification was shown was more than 1 day ago, we want to send
                 * another notification to the user that the weather has been updated. Remember,
                 * it's important that you shouldn't spam your users with notifications.
                 */
                long timeSinceLastNotification = SunshinePreferences
                        .getEllapsedTimeSinceLastNotification(context);

                boolean oneDayPassedSinceLastNotification = false;

                if (timeSinceLastNotification >= (DateUtils.HOUR_IN_MILLIS * 2)) {
                    oneDayPassedSinceLastNotification = true;
                }

                /*
                 * We only want to show the notification if the user wants them shown and we
                 * haven't shown a notification in the past day.
                 */
                if (notificationsEnabled && oneDayPassedSinceLastNotification) {
                    Log.e(LOG_TAG, "Notify user");
                    NotificationUtils.notifyUserOfNewWeather(context);
                }

                Log.e(LOG_TAG, "Last not is " + timeSinceLastNotification );

            /* If the code reaches this point, we have successfully performed our sync */

            }

        } catch (Exception e) {
            /* Server probably invalid */
            e.printStackTrace();
            Log.e(LOG_TAG, "Failed to sync weather: " + e);
        }
    }
}