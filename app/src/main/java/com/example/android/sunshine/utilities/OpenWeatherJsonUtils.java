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

import com.example.android.sunshine.data.CurrentWeatherContract;
import com.example.android.sunshine.data.HourlyWeatherContract;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

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
    private static final String DS_PRECIP_PROBABILITY = "precipProbability";
    private static final String DS_PRECIP_TYPE = "precipType";
    private static final String DS_CLOUD_COVER = "cloudCover";

    /* All temperatures are children of the "temp" object */
    private static final String OWM_TEMPERATURE = "temp";

    /* Max temperature for the day */
    private static final String OWM_MAX = "max";
    private static final String OWM_MIN = "min";

    private static final String DS_MAX = "temperatureHigh";
    private static final String DS_MIN = "temperatureLow";
    private static final String DS_TEMPERATURE = "temperature";

    private static final String OWM_WEATHER = "weather";
    private static final String OWM_WEATHER_ID = "id";

    private static final String DS_WEATHER_ID = "icon";

    private static final String OWM_MESSAGE_CODE = "cod";

    private static final String DS_LAT = "latitude";
    private static final String DS_LON = "longitude";
    private static final String DS_TIMEZONE = "timezone";
    private static final String DS_TIME = "time";

    private static final String DS_CURRENTLY = "currently";
    private static final String DS_DAILY = "daily";
    private static final String DS_HOURLY = "hourly";

    private static final String DS_SUMMARY = "summary";
    private static final String DS_ICON = "icon";

    private static final String DS_DATA_ARRAY = "data";

    private static final String DS_SUNRISE_TIME = "sunriseTime";
    private static final String DS_SUNSET_TIME = "sunsetTime";
    private static final String DS_MOONPHASE = "moonPhase";

    private static String mTimeZone;


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
    public static ArrayList<ContentValues[]> getWeatherContentValuesFromJson(Context context, String forecastJsonStr)
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

//        String latitude = forecastJson.getString(DS_LAT);
//        String longitude = forecastJson.getString(DS_LON);
        mTimeZone = forecastJson.getString(DS_TIMEZONE);


        JSONObject daily = forecastJson.getJSONObject(DS_DAILY);
        JSONObject hourly = forecastJson.getJSONObject(DS_HOURLY);

        JSONObject currentlyWeatherData = forecastJson.getJSONObject(DS_CURRENTLY);
        JSONArray jsonDailyWeatherArray = daily.getJSONArray(DS_DATA_ARRAY);
        JSONArray jsonHourlyWeatherArray = hourly.getJSONArray(DS_DATA_ARRAY);

//        JSONArray jsonWeatherArray = forecastJson.getJSONArray(OWM_LIST);
//
//        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
//
//        JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
//        double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
//        double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

        //SunshinePreferences.setLocationDetails(context, cityLatitude, cityLongitude);

        ArrayList<ContentValues[]> weatherArray = new ArrayList<>(3);

        ContentValues[] currentWeatherContentValues = getCurrentWeatherData(context, currentlyWeatherData);
        ContentValues[] dailyWeatherContentValues = getDailyWeatherValues(context, jsonDailyWeatherArray);
        ContentValues[] hourlyWeatherContentValues = getHourlyWeatherValues(context, jsonHourlyWeatherArray);

        weatherArray.add(currentWeatherContentValues);
        weatherArray.add(dailyWeatherContentValues);
        weatherArray.add(hourlyWeatherContentValues);

        return weatherArray;
    }

    private static ContentValues[] getCurrentWeatherData(Context context, JSONObject dayForecast) throws JSONException {

        //long normalizedUtcStartDay = SunshineDateUtils.getNormalizedUtcDateForToday();

        ContentValues[] currentForecastValues = new ContentValues[1];

        long dateTimeMillis;
        double pressure;
        double humidity;
        double windSpeed;
        double windDirection;
        double precipIntensity;
        double precipProbability;
        double cloudCover;

        double temperature;


        String weatherId;
        long cityId;


        dateTimeMillis = SunshineDateUtils.normalizeDate(dayForecast.getLong(DS_TIME));

        pressure = dayForecast.getDouble(DS_PRESSURE);
        humidity = (dayForecast.getDouble(DS_HUMIDITY)) * 100;
        windSpeed = dayForecast.getDouble(DS_WINDSPEED);
        windDirection = dayForecast.getDouble(DS_WIND_DIRECTION);
        precipIntensity = dayForecast.getDouble(DS_PRECIP_INTENSITY);
        precipProbability = dayForecast.getDouble(DS_PRECIP_PROBABILITY);
        cloudCover = dayForecast.getDouble(DS_CLOUD_COVER);

        weatherId = dayForecast.getString(DS_WEATHER_ID);
        if (weatherId.equals("rain") || weatherId.equals("wind") || weatherId.equals("cloudy")) {
            weatherId = extractWeatherId(weatherId, precipIntensity, cloudCover, windSpeed);
        }

        temperature = dayForecast.getDouble(DS_TEMPERATURE);


        cityId = SunshinePreferences.getCityId(context);

        ContentValues currentWeatherValues = new ContentValues();
        currentWeatherValues.put(CurrentWeatherContract.CurrentWeatherEntry.COLUMN_DATE, dateTimeMillis);
        currentWeatherValues.put(CurrentWeatherContract.CurrentWeatherEntry.COLUMN_HUMIDITY, humidity);
        currentWeatherValues.put(CurrentWeatherContract.CurrentWeatherEntry.COLUMN_PRESSURE, pressure);
        currentWeatherValues.put(CurrentWeatherContract.CurrentWeatherEntry.COLUMN_WIND_SPEED, windSpeed);
        currentWeatherValues.put(CurrentWeatherContract.CurrentWeatherEntry.COLUMN_DEGREES, windDirection);
        currentWeatherValues.put(CurrentWeatherContract.CurrentWeatherEntry.COLUMN_TEMPERATURE, temperature);
        currentWeatherValues.put(CurrentWeatherContract.CurrentWeatherEntry.COLUMN_PRECIP_INTENSITY, precipIntensity);
        currentWeatherValues.put(CurrentWeatherContract.CurrentWeatherEntry.COLUMN_PRECIP_PROBABILITY, precipProbability);
        currentWeatherValues.put(CurrentWeatherContract.CurrentWeatherEntry.COLUMN_WEATHER_ID, weatherId);
        currentWeatherValues.put(CurrentWeatherContract.CurrentWeatherEntry.COLUMN_CITY_ID, cityId);

        currentForecastValues[0] = currentWeatherValues;


        return currentForecastValues;
    }

    private static ContentValues[] getHourlyWeatherValues(Context context, JSONArray jsonHourlyWeatherArray) throws JSONException {


        //long normalizedUtcStartDay = SunshineDateUtils.getNormalizedUtcDateForToday();

        ContentValues[] hourlyWeatherValues = new ContentValues[jsonHourlyWeatherArray.length()];

        for (int i = 0; i < jsonHourlyWeatherArray.length(); i++) {

            long dateTimeMillis;
            double pressure;
            double humidity;
            double windSpeed;
            double windDirection;
            double precipIntensity;
            double precipProbability;
            double cloudCover;

            double temperature;

            String summary;
            String weatherId;
            long cityId;

            /* Get the JSON object representing the day */
            JSONObject dayForecast = jsonHourlyWeatherArray.getJSONObject(i);

            /*
             * We ignore all the datetime values embedded in the JSON and assume that
             * the values are returned in-order by day (which is not guaranteed to be correct).
             */
            dateTimeMillis = SunshineDateUtils.getNormalizedHourlyUtcDate((dayForecast.getLong(DS_TIME)*1000), mTimeZone); //  + SunshineDateUtils.DAY_IN_MILLIS * i;

            pressure = dayForecast.getDouble(DS_PRESSURE);
            humidity = (dayForecast.getDouble(DS_HUMIDITY)) * 100;
            windSpeed = dayForecast.getDouble(DS_WINDSPEED);
            windDirection = dayForecast.getDouble(DS_WIND_DIRECTION);
            precipIntensity = dayForecast.getDouble(DS_PRECIP_INTENSITY);
            precipProbability = dayForecast.getDouble(DS_PRECIP_PROBABILITY);
            cloudCover = dayForecast.getDouble(DS_CLOUD_COVER);

            summary = dayForecast.getString(DS_SUMMARY);

            /*
             * Description is in a child array called "weather", which is 1 element long.
             * That element also contains a weather code.
             */

            weatherId = dayForecast.getString(DS_WEATHER_ID);
            if (weatherId.equals("rain") || weatherId.equals("wind") || weatherId.equals("cloudy")) {
                weatherId = extractWeatherId(weatherId, precipIntensity, cloudCover, windSpeed);
            }

            /*
             * Temperatures are sent by Open Weather Map in a child object called "temp".
             */
            temperature = dayForecast.getDouble(DS_TEMPERATURE);

            cityId = SunshinePreferences.getCityId(context);

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(HourlyWeatherContract.HourlyWeatherEntry.COLUMN_DATE, dateTimeMillis);
            weatherValues.put(HourlyWeatherContract.HourlyWeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(HourlyWeatherContract.HourlyWeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(HourlyWeatherContract.HourlyWeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(HourlyWeatherContract.HourlyWeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(HourlyWeatherContract.HourlyWeatherEntry.COLUMN_TEMPERATURE, temperature);
            weatherValues.put(HourlyWeatherContract.HourlyWeatherEntry.COLUMN_WEATHER_ID, weatherId);
            weatherValues.put(HourlyWeatherContract.HourlyWeatherEntry.COLUMN_CITY_ID, cityId);
            weatherValues.put(HourlyWeatherContract.HourlyWeatherEntry.COLUMN_SUMMARY, summary);
            weatherValues.put(HourlyWeatherContract.HourlyWeatherEntry.COLUMN_PRECIP_INTENSITY, precipIntensity);
            weatherValues.put(HourlyWeatherContract.HourlyWeatherEntry.COLUMN_PRECIP_PROBABILITY, precipProbability);


            hourlyWeatherValues[i] = weatherValues;
        }

        return hourlyWeatherValues;

    }


    private static ContentValues[] getDailyWeatherValues(Context context, JSONArray jsonDailyWeatherArray) throws JSONException {


        //long normalizedUtcStartDay = SunshineDateUtils.getNormalizedUtcDateForToday();

        ContentValues[] dailyWeatherValues = new ContentValues[jsonDailyWeatherArray.length()];

        for (int i = 0; i < jsonDailyWeatherArray.length(); i++) {

            long dateTimeMillis;
            double pressure;
            double humidity;
            double windSpeed;
            double windDirection;
            double precipIntensity;
            double preciptProbability;
            String precipType;
            long sunrise;
            long sunset;
            double moonphase;
            double cloudCover;

            double high;
            double low;

            String weatherId;
            long cityId;

            /* Get the JSON object representing the day */
            JSONObject dayForecast = jsonDailyWeatherArray.getJSONObject(i);

            dateTimeMillis = SunshineDateUtils.getNormalizedUtcDateForToday((dayForecast.getLong(DS_TIME)*1000), mTimeZone);

            pressure = dayForecast.getDouble(DS_PRESSURE);
            humidity = (dayForecast.getDouble(DS_HUMIDITY)) * 100;
            windSpeed = dayForecast.getDouble(DS_WINDSPEED);
            windDirection = dayForecast.getDouble(DS_WIND_DIRECTION);
            precipIntensity = dayForecast.getDouble(DS_PRECIP_INTENSITY);
            preciptProbability = dayForecast.getDouble(DS_PRECIP_PROBABILITY);
            if(dayForecast.has(DS_PRECIP_TYPE)){
                precipType = dayForecast.getString(DS_PRECIP_TYPE);
            } else {
                precipType = "";
            }
            cloudCover = dayForecast.getDouble(DS_CLOUD_COVER);

            sunrise = dayForecast.getLong(DS_SUNRISE_TIME);
            sunset = dayForecast.getLong(DS_SUNRISE_TIME);
            moonphase = dayForecast.getDouble(DS_MOONPHASE);
            /*
             * Description is in a child array called "weather", which is 1 element long.
             * That element also contains a weather code.
             */
//            JSONObject weatherObject =
//                    dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);

            weatherId = dayForecast.getString(DS_WEATHER_ID);
            if (weatherId.equals("rain") || weatherId.equals("wind") || weatherId.equals("cloudy")) {
                weatherId = extractWeatherId(weatherId, precipIntensity, cloudCover, windSpeed);
            }

            /*
             * Temperatures are sent by Open Weather Map in a child object called "temp".
             */
//            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            high = dayForecast.getDouble(DS_MAX);
            low = dayForecast.getDouble(DS_MIN);

            cityId = SunshinePreferences.getCityId(context);

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTimeMillis);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_CITY_ID, cityId);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRECIP_INTENSITY, precipIntensity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRECIP_PROBABILITY, preciptProbability);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRECIP_TYPE, precipType);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SUNSET_TIME, sunset);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SUNRISE_TIME, sunrise);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MOON_PHASE, moonphase);

            dailyWeatherValues[i] = weatherValues;
        }

        return dailyWeatherValues;

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