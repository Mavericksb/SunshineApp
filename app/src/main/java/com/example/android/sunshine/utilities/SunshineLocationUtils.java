package com.example.android.sunshine.utilities;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.example.android.sunshine.AutoCompleteActivity;
import com.example.android.sunshine.data.CurrentWeatherContract;
import com.example.android.sunshine.data.HourlyWeatherContract;
import com.example.android.sunshine.data.LocationsContract;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.sync.SunshineSyncUtils;

/**
 * Created by Robert on 14/10/2017.
 */

public class SunshineLocationUtils {

    public static void insertLocation(Context context, String mCity, double mLatitude, double mLongitude, String mPlaceId) {
        //Create a Content value table pairing
        ContentValues values = new ContentValues();

        values.put(LocationsContract.LocationsEntry.COLUMN_NAME, mCity);
        values.put(LocationsContract.LocationsEntry.COLUMN_LATITUDE, mLatitude);
        values.put(LocationsContract.LocationsEntry.COLUMN_LONGITUDE, mLongitude);
        values.put(LocationsContract.LocationsEntry.COLUMN_PLACEID, mPlaceId);
        values.put(LocationsContract.LocationsEntry.COLUMN_LAST_UPDATE, LocationsContract.LocationsEntry.WEATHER_UPDATE_NEEDED);

        Uri newUri = context.getContentResolver().insert(LocationsContract.LocationsEntry.CONTENT_URI, values);

        if (newUri == null) {
            Toast notSucceded = Toast.makeText(context, "Failed to add city", Toast.LENGTH_SHORT);
            notSucceded.show();
        }
    }

    public static void deleteLocation(Context mContext, String[] stringPosition) {
        //Delete this city from location table
        mContext.getContentResolver().delete(
                LocationsContract.LocationsEntry.CONTENT_URI,
                LocationsContract.LocationsEntry._ID + "=?",
                stringPosition);
        // Delete this city's weather forecast from weather table


        mContext.getContentResolver().delete(
                WeatherContract.WeatherEntry.CONTENT_URI,
                WeatherContract.WeatherEntry.COLUMN_CITY_ID + "=?",
                stringPosition);

        mContext.getContentResolver().delete(
                CurrentWeatherContract.CurrentWeatherEntry.CONTENT_URI,
                CurrentWeatherContract.CurrentWeatherEntry.COLUMN_CITY_ID + "=?",
                stringPosition);

        mContext.getContentResolver().delete(
                HourlyWeatherContract.HourlyWeatherEntry.CONTENT_URI,
                HourlyWeatherContract.HourlyWeatherEntry.COLUMN_CITY_ID + "=?",
                stringPosition);
    }


    public static void updateLocation(Context context, String mCity, double mLatitude, double mLongitude) {
        //Create a Content value table pairing
        ContentValues values = new ContentValues();

        values.put(LocationsContract.LocationsEntry.COLUMN_NAME, mCity);
        values.put(LocationsContract.LocationsEntry.COLUMN_LATITUDE, mLatitude);
        values.put(LocationsContract.LocationsEntry.COLUMN_LONGITUDE, mLongitude);

        int newUri = context.getContentResolver().update(LocationsContract.LocationsEntry.CONTENT_URI,
                values,
                LocationsContract.LocationsEntry.COLUMN_PLACEID + "=?",
                new String[]{LocationsContract.LocationsEntry.UNIQUE_GEOLOCATION_ID});

        if (newUri == -1) {
            Toast notSucceded = Toast.makeText(context, "Failed to add city", Toast.LENGTH_SHORT);
            notSucceded.show();
        }
    }

    public static void updateLastLocationUpdate(Context context, long cityId) {
        //Create a Content value table pairing
        ContentValues values = new ContentValues();

        values.put(LocationsContract.LocationsEntry.COLUMN_LAST_UPDATE, System.currentTimeMillis());

        int newUri = context.getContentResolver().update(LocationsContract.LocationsEntry.CONTENT_URI,
                values,
                LocationsContract.LocationsEntry._ID + "=?",
                new String[]{String.valueOf(cityId)});

        if (newUri == -1) {
            Toast notSucceded = Toast.makeText(context, "Failed to modify last update time", Toast.LENGTH_SHORT);
            notSucceded.show();
        }
    }

}
