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
package com.example.android.sunshine.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.android.sunshine.R;

public final class SunshinePreferences {

    /*
     * In order to uniquely pinpoint the location on the map when we launch the map intent, we
     * store the latitude and longitude. We will also use the latitude and longitude to create
     * queries for the weather.
     */
    public static final String PREF_COORD_LAT = "coord_lat";
    public static final String PREF_COORD_LONG = "coord_long";
    // Actual city name
    public static final String PREF_CITY = "city";
    // Unique selected city Id based on _ID of locations table
    public static final String PREF_CITY_ID = "city_id";
    // place Id of actual city
    public static final String PREF_PLACE_ID = "place_id";

    public static final String SUNRISE_TIME = "sunrise_time";
    public static final String SUNSET_TIME = "sunset_time";



    /**
     * Helper method to handle setting location details in Preferences (city name, latitude,
     * longitude)
     * <p>
     * When the location details are updated, the database should to be cleared.
     *
     * @param context  Context used to get the SharedPreferences
     * @param lat      the latitude of the city
     * @param lon      the longitude of the city
     */
    public static void setLocationDetails(Context context, double lat, double lon, String city, String placeId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString(PREF_PLACE_ID, placeId);
        editor.putString(PREF_CITY, city);
        editor.putLong(PREF_COORD_LAT, Double.doubleToRawLongBits(lat));
        editor.putLong(PREF_COORD_LONG, Double.doubleToRawLongBits(lon));
        editor.apply();
    }

    public static void resetLocationDetails(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.remove(PREF_PLACE_ID);
        editor.remove(PREF_CITY);
        editor.remove(PREF_COORD_LAT);
        editor.remove(PREF_COORD_LONG);
        editor.apply();
    }

    /**
     * Helper method to handle setting location details in Preferences (city name)
     * <p>
     * When the location details are updated, the database should to be cleared.
     *
     * @param context  Context used to get the SharedPreferences
     * @param city     the name of the city
     */
    public static void setCityName(Context context, String city ) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString(PREF_CITY, city);
        editor.apply();
    }

    public static void setRiseSetTime(Context context, long sunrise, long sunset ) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putLong(SUNRISE_TIME, sunrise);
        editor.putLong(SUNSET_TIME, sunset);
        editor.apply();
    }

    public static long getSunriseTime(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return sp.getLong(SUNRISE_TIME, 0);
    }

    public static long getSunsetTime(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return sp.getLong(SUNSET_TIME, 0);
    }

    /**
     * Helper method to handle setting location details in Preferences (city name)
     * <p>
     * When the location details are updated, the database should to be cleared.
     *
     * @param context  Context used to get the SharedPreferences
     */
    public static String getCityName(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return sp.getString(PREF_CITY, "");
    }

    public static void setCityId(Context context, long locationId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putLong(PREF_CITY_ID, locationId);
        editor.apply();
    }

    public static void resetCityId(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.remove(PREF_CITY_ID);
        editor.apply();
    }

    public static long getCityId(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return sp.getLong(PREF_CITY_ID, 0);
    }

    public static void setPlaceId(Context context, String placeId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString(PREF_PLACE_ID, placeId);
    }

    public static String getPlaceId(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        return sp.getString(PREF_PLACE_ID, "");
    }

    /**
     * Resets the location coordinates stores in SharedPreferences.
     *
     * @param context Context used to get the SharedPreferences
     */
    public static void resetLocationCoordinates(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        editor.remove(PREF_COORD_LAT);
        editor.remove(PREF_COORD_LONG);
        editor.apply();
    }

    /**
     * Returns the actual value of Request Location set in preferences.
     */

    public static boolean getRequestUpdates(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String requestUpdatesKey = context.getString(R.string.pref_enable_geolocation_key);
        boolean requestUpdatesDefaultValue = context.getResources().getBoolean(R.bool.get_location_by_default);

        return sp.getBoolean(requestUpdatesKey, requestUpdatesDefaultValue);
    }

    /**
     * Returns the actual value of Request Location set in preferences.
     */

    public static void setRequestUpdates(Context context, boolean requestLocationUpdates) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        String requestUpdatesKey = context.getString(R.string.pref_enable_geolocation_key);


        editor.putBoolean(requestUpdatesKey, requestLocationUpdates);
        editor.apply();
    }

    public static double[] getPreferredLocationCoords(Context context) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String latKey = context.getString(R.string.pref_latitude_key);
        String latDef = context.getString(R.string.pref_latitude_default);

        String lonKey = context.getString(R.string.pref_longitude_key);
        String lonDef = context.getString(R.string.pref_longitude_default);

        String keyForLocation = context.getString(R.string.pref_location_key);
        String defaultLocation = context.getString(R.string.pref_location_default);

        String placeIdKey = context.getString(R.string.pref_place_id_key);
        String placeIdDefault = context.getString(R.string.pref_place_id_default);

        String location = sp.getString(keyForLocation, defaultLocation);
        String placeId = sp.getString(placeIdKey, placeIdDefault);

        double latitude = Double.valueOf(sp.getString(latKey, latDef));
        double longitude = Double.valueOf(sp.getString(lonKey, lonDef));

        double[] coords = {latitude, longitude};

        setLocationDetails(context, latitude, longitude, location, placeId);

        return coords;
    }

    /**
     * Returns true if the user has selected metric temperature display.
     *
     * @param context Context used to get the SharedPreferences
     * @return true if metric display should be used, false if imperial display should be used
     */
    public static boolean isMetric(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String keyForUnits = context.getString(R.string.pref_units_key);
        String defaultUnits = context.getString(R.string.pref_units_metric);
        String preferredUnits = sp.getString(keyForUnits, defaultUnits);
        String metric = context.getString(R.string.pref_units_metric);

        boolean userPrefersMetric = false;
        if (metric.equals(preferredUnits)) {
            userPrefersMetric = true;
        }

        return userPrefersMetric;
    }

    /**
     * Returns the location coordinates associated with the location. Note that there is a
     * possibility that these coordinates may not be set, which results in (0,0) being returned.
     * Interestingly, (0,0) is in the middle of the ocean off the west coast of Africa.
     *
     * @param context used to access SharedPreferences
     * @return an array containing the two coordinate values for the user's preferred location
     */
    public static double[] getLocationCoordinates(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        double[] preferredCoordinates = new double[2];

        /*
         * This is a hack we have to resort to since you can't store doubles in SharedPreferences.
         *
         * Double.doubleToLongBits returns an integer corresponding to the bits of the given
         * IEEE 754 double precision value.
         *
         * Double.longBitsToDouble does the opposite, converting a long (that represents a double)
         * into the double itself.
         */
        preferredCoordinates[0] = Double
                 .longBitsToDouble(sp.getLong(PREF_COORD_LAT, Double.doubleToRawLongBits(0.0)));
        preferredCoordinates[1] = Double
                .longBitsToDouble(sp.getLong(PREF_COORD_LONG, Double.doubleToRawLongBits(0.0)));

        return preferredCoordinates;
    }

    /**
     * Returns true if the latitude and longitude values are available. The latitude and
     * longitude will not be available until the lesson where the PlacePicker API is taught.
     *
     * @param context used to get the SharedPreferences
     * @return true if lat/long are saved in SharedPreferences
     */
    public static boolean isLocationLatLonAvailable(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        boolean spContainLatitude = sp.contains(PREF_COORD_LAT);
        boolean spContainLongitude = sp.contains(PREF_COORD_LONG);

        boolean spContainBothLatitudeAndLongitude = false;
        if (spContainLatitude && spContainLongitude) {
            spContainBothLatitudeAndLongitude = true;
        }

        return spContainBothLatitudeAndLongitude;
    }

    /**
     * Returns true if the user prefers to see notifications from Sunshine, false otherwise. This
     * preference can be changed by the user within the SettingsFragment.
     *
     * @param context Used to access SharedPreferences
     * @return true if the user prefers to see notifications, false otherwise
     */
    public static boolean areNotificationsEnabled(Context context) {
        /* Key for accessing the preference for showing notifications */
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);

        /*
         * In Sunshine, the user has the ability to say whether she would like notifications
         * enabled or not. If no preference has been chosen, we want to be able to determine
         * whether or not to show them. To do this, we reference a bool stored in bools.xml.
         */
        boolean shouldDisplayNotificationsByDefault = context
                .getResources()
                .getBoolean(R.bool.show_notifications_by_default);

        /* As usual, we use the default SharedPreferences to access the user's preferences */
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        /* If a value is stored with the key, we extract it here. If not, use a default. */
        boolean shouldDisplayNotifications = sp
                .getBoolean(displayNotificationsKey, shouldDisplayNotificationsByDefault);

        return shouldDisplayNotifications;
    }

    /**
     * Returns the last time that a notification was shown (in UNIX time)
     *
     * @param context Used to access SharedPreferences
     * @return UNIX time of when the last notification was shown
     */
    public static long getLastNotificationTimeInMillis(Context context) {
        /* Key for accessing the time at which Sunshine last displayed a notification */
        String lastNotificationKey = context.getString(R.string.pref_last_notification);

        /* As usual, we use the default SharedPreferences to access the user's preferences */
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        /*
         * Here, we retrieve the time in milliseconds when the last notification was shown. If
         * SharedPreferences doesn't have a value for lastNotificationKey, we return 0. The reason
         * we return 0 is because we compare the value returned from this method to the current
         * system time. If the difference between the last notification time and the current time
         * is greater than one day, we will show a notification again. When we compare the two
         * values, we subtract the last notification time from the current system time. If the
         * time of the last notification was 0, the difference will always be greater than the
         * number of milliseconds in a day and we will show another notification.
         */
        long lastNotificationTime = sp.getLong(lastNotificationKey, 0);

        return lastNotificationTime;
    }

    /**
     * Returns the elapsed time in milliseconds since the last notification was shown. This is used
     * as part of our check to see if we should show another notification when the weather is
     * updated.
     *
     * @param context Used to access SharedPreferences as well as use other utility methods
     * @return Elapsed time in milliseconds since the last notification was shown
     */
    public static long getEllapsedTimeSinceLastNotification(Context context) {
        long lastNotificationTimeMillis =
                SunshinePreferences.getLastNotificationTimeInMillis(context);
        long timeSinceLastNotification = System.currentTimeMillis() - lastNotificationTimeMillis;
        return timeSinceLastNotification;
    }


    public static long getEllapsedTimeSinceLastLocationUpdate(Context context, long lastLocationUpdate) {
        long timeSinceLastLocationUpdate = System.currentTimeMillis() - lastLocationUpdate;
        return timeSinceLastLocationUpdate;
    }

    /**
     * Saves the time that a notification is shown. This will be used to get the ellapsed time
     * since a notification was shown.
     *
     * @param context Used to access SharedPreferences
     * @param timeOfNotification Time of last notification to save (in UNIX time)
     */
    public static void saveLastNotificationTime(Context context, long timeOfNotification) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        editor.putLong(lastNotificationKey, timeOfNotification);
        editor.apply();
    }
}