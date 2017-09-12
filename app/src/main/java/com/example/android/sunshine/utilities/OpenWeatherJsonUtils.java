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

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * Utility functions to handle OpenWeatherMap JSON data.
 */
public final class OpenWeatherJsonUtils {

    /* Location information */
    private static final String OWM_CITY = "city";
    private static final String OWM_COORD = "coord";

    /* Location coordinate */
    private static final String OWM_LATITUDE = "lat";
    private static final String OWM_LONGITUDE = "lon";

    /* Weather information. Each day's forecast info is an element of the "list" array */
    private static final String OWM_LIST = "list";

    private static final String OWM_PRESSURE = "pressure";
    private static final String OWM_HUMIDITY = "humidity";
    private static final String OWM_WINDSPEED = "speed";
    private static final String OWM_WIND_DIRECTION = "deg";

    private static final String DS_PRESSURE = "pressure";
    private static final String DS_HUMIDITY = "humidity";
    private static final String DS_WINDSPEED = "windSpeed";
    private static final String DS_WIND_DIRECTION = "windBearing";
    private static final String DS_PRECIP_INTENSITY = "precipIntensity";
    private static final String DS_CLOUD_COVER = "cloudCover";

    /* All temperatures are children of the "temp" object */
    private static final String OWM_TEMPERATURE = "temp";

    /* Max temperature for the day */
    private static final String OWM_MAX = "max";
    private static final String OWM_MIN = "min";

    private static final String DS_MAX = "temperatureHigh";
    private static final String DS_MIN = "temperatureLow";

    private static final String OWM_WEATHER = "weather";
    private static final String OWM_WEATHER_ID = "id";

    private static final String DS_WEATHER_ID = "icon";

    private static final String OWM_MESSAGE_CODE = "cod";

    private static final String DS_LAT = "latitude";
    private static final String DS_LON = "longitude";
    private static final String DS_TIMEZONE = "timezone";

    private static final String DS_DAILY = "daily";

    private static final String DS_SUMMARY = "summary";
    private static final String DS_ICON = "icon";

    private static final String DS_DATA_ARRAY = "data";


    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the weather over various days from the forecast.
     * <p/>
     * Later on, we'll be parsing the JSON into structured data within the
     * getFullWeatherDataFromJson function, leveraging the data we have stored in the JSON. For
     * now, we just convert the JSON into human-readable strings.
     *
     * @param forecastJsonStr JSON response from server
     * @return Array of Strings describing weather data
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static ContentValues[] getWeatherContentValuesFromJson(Context context, String forecastJsonStr)
            throws JSONException {

        JSONObject forecastJson = new JSONObject(forecastJsonStr);

        /* Is there an error? */
        if (forecastJson.has(OWM_MESSAGE_CODE)) {
            int errorCode = forecastJson.getInt(OWM_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        String latitude = forecastJson.getString(DS_LAT);
        String longitude = forecastJson.getString(DS_LON);
        String timeZone = forecastJson.getString(DS_TIMEZONE);


        JSONObject daily = forecastJson.getJSONObject(DS_DAILY);
        String weekSummary = daily.getString(DS_SUMMARY);
        String weekIcon = daily.getString(DS_ICON);

        JSONArray jsonWeatherArray = daily.getJSONArray(DS_DATA_ARRAY);

//        JSONArray jsonWeatherArray = forecastJson.getJSONArray(OWM_LIST);
//
//        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
//
//        JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
//        double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
//        double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

        //SunshinePreferences.setLocationDetails(context, cityLatitude, cityLongitude);

        ContentValues[] weatherContentValues = new ContentValues[jsonWeatherArray.length()];

        /*
         * OWM returns daily forecasts based upon the local time of the city that is being asked
         * for, which means that we need to know the GMT offset to translate this data properly.
         * Since this data is also sent in-order and the first day is always the current day, we're
         * going to take advantage of that to get a nice normalized UTC date for all of our weather.
         */
//        long now = System.currentTimeMillis();
//        long normalizedUtcStartDay = SunshineDateUtils.normalizeDate(now);

        long normalizedUtcStartDay = SunshineDateUtils.getNormalizedUtcDateForToday();

        for (int i = 0; i < jsonWeatherArray.length(); i++) {

            long dateTimeMillis;
            double pressure;
            double humidity;
            double windSpeed;
            double windDirection;
            double precipIntensity;
            double cloudCover;

            double high;
            double low;

            String id;
            String weatherId;

            /* Get the JSON object representing the day */
            JSONObject dayForecast = jsonWeatherArray.getJSONObject(i);

            /*
             * We ignore all the datetime values embedded in the JSON and assume that
             * the values are returned in-order by day (which is not guaranteed to be correct).
             */
            dateTimeMillis = normalizedUtcStartDay + SunshineDateUtils.DAY_IN_MILLIS * i;

            pressure = dayForecast.getDouble(DS_PRESSURE);
            humidity = (dayForecast.getDouble(DS_HUMIDITY)) * 100;
            windSpeed = dayForecast.getDouble(DS_WINDSPEED);
            windDirection = dayForecast.getDouble(DS_WIND_DIRECTION);
            precipIntensity = dayForecast.getDouble(DS_PRECIP_INTENSITY);
            cloudCover = dayForecast.getDouble(DS_CLOUD_COVER);

            /*
             * Description is in a child array called "weather", which is 1 element long.
             * That element also contains a weather code.
             */
//            JSONObject weatherObject =
//                    dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);

            id = dayForecast.getString(DS_WEATHER_ID);
            if (id.equals("rain") || id.equals("wind") || id.equals("cloudy")) {
                weatherId = extractWeatherId(id, precipIntensity, cloudCover, windSpeed);
            } else {
                weatherId = id;
            }


            /*
             * Temperatures are sent by Open Weather Map in a child object called "temp".
             *
             * Editor's Note: Try not to name variables "temp" when working with temperature.
             * It confuses everybody. Temp could easily mean any number of things, including
             * temperature, temporary variable, temporary folder, temporary employee, or many
             * others, and is just a bad variable name.
             */
//            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            high = dayForecast.getDouble(DS_MAX);
            low = dayForecast.getDouble(DS_MIN);

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTimeMillis);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            weatherContentValues[i] = weatherValues;
        }

        return weatherContentValues;
    }

    private static String extractWeatherId(String id, double precipIntensity, double cloudCover, double windSpeed) {

        String exactId = "";

        switch (id) {
            case "rain":
                if (windSpeed > 50 && precipIntensity > 0.25) {
                    if (windSpeed > 75 && precipIntensity > 0.50) {
                        exactId = "violent_storm";
                    } else {
                        exactId = "storm";
                    }
                    return exactId;
                }

                if (precipIntensity < 0.25) {
                    exactId = "light_" + id;
                } else if (precipIntensity < 0.50) {
                    exactId = "moderate_" + id;
                } else if (precipIntensity < 0.75) {
                    exactId = "heavy_" + id;
                } else {
                    exactId = "intense_" + id;
                }
                return exactId;

            case "wind":
                if (windSpeed <= 1) {
                    exactId = "calm";
                } else if (windSpeed <= 5) {
                    exactId = "wind";
                } else if (windSpeed <= 11) {
                    exactId = "light_breeze";
                } else if (windSpeed <= 19) {
                    exactId = "gentle_breeze";
                } else if (windSpeed <= 28) {
                    exactId = "breeze";
                } else if (windSpeed <= 38) {
                    exactId = "fresh_breeze";
                } else if (windSpeed <= 49) {
                    exactId = "strong_breeze";
                } else if (windSpeed <= 61) {
                    exactId = "high_wind";
                } else if (windSpeed <= 74) {
                    exactId = "gale";
                } else {
                    exactId = "severe_gale";
                }
                return exactId;
            case "cloudy":
                if (cloudCover < 0.25) {
                    exactId = "mostly_clear";
                } else if (cloudCover < 0.50) {
                    exactId = "scattered_clouds" + id;
                } else if (cloudCover < 0.80) {
                    exactId = "broken_clouds";
                } else {
                    exactId = "overcast_clouds";
                }
                return exactId;

            default:
                return exactId;
        }

    }
}