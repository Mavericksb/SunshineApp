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

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;

import com.gabra.android.sunshine.data.CurrentWeatherContract;
import com.gabra.android.sunshine.data.HourlyWeatherContract;
import com.gabra.android.sunshine.data.LocationsContract;
import com.gabra.android.sunshine.data.SunshinePreferences;
import com.gabra.android.sunshine.data.WeatherContract;
import com.gabra.android.sunshine.sync.SunshineSyncUtils;
import com.gabra.android.sunshine.utilities.SunshineLocationUtils;

/**
 * The SettingsFragment serves as the display for all of the user's settings. In Sunshine, the
 * user will be able to change their preference for units of measurement from metric to imperial,
 * set their preferred weather location, and indicate whether or not they'd like to see
 * notifications.
 * <p>
 * Please note: If you are using our dummy weather services, the location returned will always be
 * Mountain View, California.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference p = prefScreen.getPreference(i);
            if (!(p instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, value);
            }
        }
    }

    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // register the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Activity activity = getActivity();

        if (key.equals(getString(R.string.pref_location_key))) {
            // we've changed the location
            // Wipe out any potential PlacePicker latlng values so that we can use this text entry.
            SunshinePreferences.resetLocationCoordinates(activity);
            SunshineSyncUtils.startImmediateSync(activity);
        } else if (key.equals(getString(R.string.pref_units_key))) {
            // units have changed. update lists of weather entries accordingly
            activity.getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
            activity.getContentResolver().notifyChange(HourlyWeatherContract.HourlyWeatherEntry.CONTENT_URI, null);
            activity.getContentResolver().notifyChange(CurrentWeatherContract.CurrentWeatherEntry.CONTENT_URI, null);
        } else if (key.equals(getString(R.string.pref_enable_geolocation_key))) {
            boolean areLocationUpdatesRequested = SunshinePreferences.getRequestUpdates(getActivity());

            Log.e("SettingsFragments", "Requested updates ? " + areLocationUpdatesRequested);
            if (!areLocationUpdatesRequested) {
                Cursor cursor = getActivity().getContentResolver().query(LocationsContract.LocationsEntry.CONTENT_URI,
                        LocationActivity.LOCATION_PROJECTION,
                        null, //LocationsContract.LocationsEntry.COLUMN_PLACEID + " =?",
                        null, //new String[]{LocationsContract.LocationsEntry.UNIQUE_GEOLOCATION_ID},
                        null);

                long position;
                int prevPos = -1;
                int nextPos = -1;
                int changePos = -1;

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        for (int i = 0; i < cursor.getCount(); i++) {
                            cursor.moveToPosition(i);
                            position = cursor.getLong(LocationActivity.INDEX_ID);
                            String placeId = cursor.getString(LocationActivity.INDEX_PLACE_ID);
                            Log.e("del geolocality", " " + placeId);
                            if (placeId.equals(LocationsContract.LocationsEntry.UNIQUE_GEOLOCATION_ID)) {
                                if (cursor.moveToNext()) {
                                    nextPos = i + 1;
                                }
                                SunshineLocationUtils.deleteLocation(getActivity(), new String[]{String.valueOf(position)});
                                break;
                            }
                            prevPos = i;
                        }

                    }

                    if(SunshinePreferences.getPlaceId(getActivity()).equals(LocationsContract.LocationsEntry.UNIQUE_GEOLOCATION_ID)) {
                        if (prevPos != -1) {
                            changePos = prevPos;
                        } else if (nextPos != -1) {
                            changePos = nextPos;
                        }

                        if (-1 != changePos) {
                            cursor.moveToPosition(changePos);
                            long id = cursor.getLong(LocationActivity.INDEX_ID);
                            String city = cursor.getString(LocationActivity.INDEX_CITY_NAME);
                            String placeId = cursor.getString(LocationActivity.INDEX_PLACE_ID);
                            double latitude = cursor.getDouble(LocationActivity.INDEX_CITY_LATITUDE);
                            double longitude = cursor.getDouble(LocationActivity.INDEX_CITY_LONGITUDE);
                            SunshinePreferences.setCityId(getActivity(), id);
                            SunshinePreferences.setLocationDetails(getActivity(), latitude, longitude, city, placeId);
                            SunshineLocationUtils.updateLastLocationUpdate(getActivity(), id);
                            SunshineSyncUtils.startImmediateSync(getActivity());
                            getActivity().getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
                            getActivity().getContentResolver().notifyChange(CurrentWeatherContract.CurrentWeatherEntry.CONTENT_URI, null);
                            getActivity().getContentResolver().notifyChange(HourlyWeatherContract.HourlyWeatherEntry.CONTENT_URI, null);
                        }
                    }
                    cursor.close();
                }

            }

        }


        Preference preference = findPreference(key);
        if (null != preference) {
            if (!(preference instanceof CheckBoxPreference)) {
                setPreferenceSummary(preference, sharedPreferences.getString(key, ""));
            }
        }
    }
}
