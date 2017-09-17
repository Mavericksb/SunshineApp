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
package com.example.android.sunshine.utilities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.android.sunshine.AutoCompleteActivity;
import com.example.android.sunshine.data.LocationsContract;
import com.example.android.sunshine.data.SunshinePreferences;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the weather servers.
 */
public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    /*
     * Sunshine was originally built to use OpenWeatherMap's API. However, we wanted to provide
     * a way to much more easily test the app and provide more varied weather data. After all, in
     * Mountain View (Google's HQ), it gets very boring looking at a forecast of perfectly clear
     * skies at 75°F every day... (UGH!) The solution we came up with was to host our own fake
     * weather server. With this server, there are two URL's you can use. The first (and default)
     * URL will return dynamic weather data. Each time the app refreshes, you will get different,
     * completely random weather data. This is incredibly useful for testing the robustness of your
     * application, as different weather JSON will provide edge cases for some of your methods.
     *
     * If you'd prefer to test with the weather data that you will see in the videos on Udacity,
     * you can do so by setting the FORECAST_BASE_URL to STATIC_WEATHER_URL below.
     */


    private static final String DYNAMIC_WEATHER_URL =
            "https://api.darksky.net/forecast/86e612f54bf44d5992b101d39f9640f4/";

    private static final String STATIC_WEATHER_URL =
            "https://andfun-weather.udacity.com/staticweather";

    private static final String FORECAST_BASE_URL = STATIC_WEATHER_URL;


    /*
     * NOTE: These values only effect responses from OpenWeatherMap, NOT from the fake weather
     * server. They are simply here to allow us to teach you how to build a URL if you were to use
     * a real API.If you want to connect your app to OpenWeatherMap's API, feel free to! However,
     * we are not going to show you how to do so in this course.
     */

    /* The format we want our API to return */
    private static final String format = "json";
    /* The units we want our API to return */
    private static final String units = "ca";
    /* The number of days we want our API to return */
    private static final int numDays = 14;

    /* The query parameter allows us to provide a location string to the API */
    private static final String QUERY_PARAM = "q";

    private static final String LAT_PARAM = "lat";
    private static final String LON_PARAM = "lon";

    private static final String EXCLUDE_PARAM = "exclude";
    private static final String exclude = "minutely,alerts,flags";

    private static final String LANG_PARAM = "lang";
    private static final String language = "it";


    /* The format parameter allows us to designate whether we want JSON or XML from our API */
    private static final String FORMAT_PARAM = "mode";
    /* The units parameter allows us to designate whether we want metric units or imperial units */
    private static final String UNITS_PARAM = "units";
    /* The days parameter allows us to designate how many days of weather data we want */
    private static final String DAYS_PARAM = "cnt";

    /**
     * Retrieves the proper URL to query for the weather data. The reason for both this method as
     * well as {@link #buildUrlWithLocationQuery(String)} is two fold.
     * <p>
     * 1) You should be able to just use one method when you need to create the URL within the
     * app instead of calling both methods.
     * 2) Later in Sunshine, you are going to add an alternate method of allowing the user
     * to select their preferred location. Once you do so, there will be another way to form
     * the URL using a latitude and longitude rather than just a location String. This method
     * will "decide" which URL to build and return it.
     *
     * @param context used to access other Utility methods
     * @return URL to query weather service
     */
    public static URL getUrl(Context context) {

        if (SunshinePreferences.isLocationLatLonAvailable(context)) {
            double[] preferredCoordinates = SunshinePreferences.getLocationCoordinates(context);
            double latitude = preferredCoordinates[0];
            double longitude = preferredCoordinates[1];
            return buildUrlWithLatitudeLongitude(latitude, longitude);
        } else {
            double[] coords = SunshinePreferences.getPreferredLocationCoords(context);
            double latitude = coords[0];
            double longitude = coords[1];
            String city = SunshinePreferences.getCityName(context);
            String placeId = SunshinePreferences.getPlaceId(context);
            // Creating first table entry for LOCATION table to store it in preferred location.
            ContentValues values = new ContentValues();

            values.put(LocationsContract.LocationsEntry.COLUMN_NAME, city);
            values.put(LocationsContract.LocationsEntry.COLUMN_LATITUDE, latitude);
            values.put(LocationsContract.LocationsEntry.COLUMN_LONGITUDE, longitude);
            values.put(LocationsContract.LocationsEntry.COLUMN_PLACEID, placeId);

            Uri newUri = context.getContentResolver().insert(LocationsContract.LocationsEntry.CONTENT_URI, values);

            if(newUri==null){
                Toast notSucceded = Toast.makeText(context, "Failed to add city", Toast.LENGTH_SHORT);
                notSucceded.show();
            } else {
                // Get the Location Table ID for this city and store it in Shared preference in order to be
                // used whenever Weather table creates the data for a given city to store its city reference under
                // CITY_ID
                long cityId = ContentUris.parseId(newUri);
                //Set City Id to relate WEATHER table entry to this given preferred city.
                SunshinePreferences.setCityId(context, cityId);
            }

            return buildUrlWithLatitudeLongitude(latitude, longitude);
        }



    }

    /**
     * Builds the URL used to talk to the weather server using latitude and longitude of a
     * location.
     *
     * @param latitude  The latitude of the location
     * @param longitude The longitude of the location
     * @return The Url to use to query the weather server.
     */
    private static URL buildUrlWithLatitudeLongitude(Double latitude, Double longitude) {
        String URL_WITH_COORDS = DYNAMIC_WEATHER_URL + String.valueOf(latitude) + "," + String.valueOf(longitude);
        Uri weatherQueryUri = Uri.parse(URL_WITH_COORDS).buildUpon()
                .appendQueryParameter(EXCLUDE_PARAM, exclude)
                .appendQueryParameter(LANG_PARAM, language)
                .appendQueryParameter(UNITS_PARAM, units)
                .build();
        Log.e("Build URL", "Url : " + weatherQueryUri);

        try {
            URL weatherQueryUrl = new URL(weatherQueryUri.toString());
            Log.v(TAG, "URL: " + weatherQueryUrl);
            return weatherQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Builds the URL used to talk to the weather server using a location. This location is based
     * on the query capabilities of the weather provider that we are using.
     *
     * @param locationQuery The location that will be queried for.
     * @return The URL to use to query the weather server.
     */
    private static URL buildUrlWithLocationQuery(String locationQuery) {
        Uri weatherQueryUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, locationQuery)
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                .build();

        try {
            URL weatherQueryUrl = new URL(weatherQueryUri.toString());
            Log.v(TAG, "URL: " + weatherQueryUrl);
            return weatherQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response, null if no response
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            String response = null;
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            return response;
        } finally {
            urlConnection.disconnect();
        }
    }
}