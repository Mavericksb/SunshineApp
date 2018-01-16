/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.TimeUnit;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.sunshine.utilities.SunshineDateUtils;

/**
 * This class serves as the ContentProvider for all of Sunshine's data. This class allows us to
 * bulkInsert data, query data, and delete data.
 * <p>
 * Although ContentProvider implementation requires the implementation of additional methods to
 * perform single inserts, updates, and the ability to get the type of the data from a URI.
 * However, here, they are not implemented for the sake of brevity and simplicity. If you would
 * like, you may implement them on your own. However, we are not going to be teaching how to do
 * so in this course.
 */
public class WeatherProvider extends ContentProvider {

    /*
     * These constant will be used to match URIs with the data they are looking for. We will take
     * advantage of the UriMatcher class to make that matching MUCH easier than doing something
     * ourselves, such as using regular expressions.
     */
    public static final int CODE_WEATHER = 100;
    public static final int CODE_WEATHER_WITH_DATE = 101;
    public static final int CODE_LOCATIONS = 200;
    public static final int CODE_SINGLE_LOCATION = 201;
    public static final int CODE_CURRENT_FORECAST = 300;
    public static final int CODE_HOURLY_FORECAST = 400;
    public static final int CODE_HOURLY_FORECAST_WITH_DATE = 500;


    /*
     * The URI Matcher used by this content provider. The leading "s" in this variable name
     * signifies that this UriMatcher is a static member variable of WeatherProvider and is a
     * common convention in Android programming.
     */
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private WeatherDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {


        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;

        /*
         * For each type of URI you want to add, create a corresponding code. Preferably, these are
         * constant fields in your class so that you can use them throughout the class and you no
         * they aren't going to change. In Sunshine, we use CODE_WEATHER or CODE_WEATHER_WITH_DATE.
         */

        /* This URI is content://com.example.android.sunshine/weather/ */
        matcher.addURI(authority, WeatherContract.PATH_WEATHER, CODE_WEATHER);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/#", CODE_WEATHER_WITH_DATE);

        matcher.addURI(authority, LocationsContract.PATH_LOCATIONS, CODE_LOCATIONS);
        matcher.addURI(authority, LocationsContract.PATH_LOCATIONS + "/#", CODE_SINGLE_LOCATION);

        matcher.addURI(authority, CurrentWeatherContract.PATH_CURRENT_FORECAST, CODE_CURRENT_FORECAST);

        matcher.addURI(authority, HourlyWeatherContract.PATH_HOURLY_FORECAST, CODE_HOURLY_FORECAST);
        matcher.addURI(authority, HourlyWeatherContract.PATH_HOURLY_FORECAST + "/#", CODE_HOURLY_FORECAST_WITH_DATE);

        return matcher;
    }


    @Override
    public boolean onCreate() {
        /*
         * As noted in the comment above, onCreate is run on the main thread, so performing any
         * lengthy operations will cause lag in your app. Since WeatherDbHelper's constructor is
         * very lightweight, we are safe to perform that initialization here.
         */
        mOpenHelper = new WeatherDbHelper(getContext());
        return true;
    }

    /**
     * Handles requests to insert a set of new rows. In Sunshine, we are only going to be
     * inserting multiple rows of data at a time from a weather forecast. There is no use case
     * for inserting a single row of data into our ContentProvider, and so we are only going to
     * implement bulkInsert. In a normal ContentProvider's implementation, you will probably want
     * to provide proper functionality for the insert method as well.
     *
     * @param uri    The content:// URI of the insertion request.
     * @param values An array of sets of column_name/value pairs to add to the database.
     *               This must not be {@code null}.
     *
     * @return The number of values that were inserted.
     */
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int rowsInserted = 0;

        switch (sUriMatcher.match(uri)) {


            case CODE_WEATHER:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
//                        long weatherDate =
//                                value.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
//                        if (!SunshineDateUtils.isDateNormalized(weatherDate)) {
//                            throw new IllegalArgumentException("Date must be normalized to insert");
//                        }

                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;


            case CODE_CURRENT_FORECAST:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(CurrentWeatherContract.CurrentWeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;

            case CODE_HOURLY_FORECAST:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
//                        long weatherDate =
//                                value.getAsLong(HourlyWeatherContract.HourlyWeatherEntry.COLUMN_DATE);
//                        if (!SunshineDateUtils.isDateNormalized(weatherDate)) {
//                            throw new IllegalArgumentException("Date must be normalized to insert");
//                        }

                        long _id = db.insert(HourlyWeatherContract.HourlyWeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;


            default:
                return super.bulkInsert(uri, values);


        }
        return rowsInserted;
    }

    /**
     * Handles query requests from clients. We will use this method in Sunshine to query for all
     * of our weather data as well as to query for the weather on a particular day.
     *
     * @param uri           The URI to query
     * @param projection    The list of columns to put into the cursor. If null, all columns are
     *                      included.
     * @param selection     A selection criteria to apply when filtering rows. If null, then all
     *                      rows are included.
     * @param selectionArgs You may include ?s in selection, which will be replaced by
     *                      the values from selectionArgs, in order that they appear in the
     *                      selection.
     * @param sortOrder     How the rows in the cursor should be sorted.
     * @return A Cursor containing the results of the query. In our implementation,
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor cursor;

        /*
         * Here's the switch statement that, given a URI, will determine what kind of request is
         * being made and query the database accordingly.
         */
        switch (sUriMatcher.match(uri)) {

            /*
             * When sUriMatcher's match method is called with a URI that looks something like this
             *
             *      content://com.example.android.sunshine/weather/1472214172
             *
             * sUriMatcher's match method will return the code that indicates to us that we need
             * to return the weather for a particular date. The date in this code is encoded in
             * milliseconds and is at the very end of the URI (1472214172) and can be accessed
             * programmatically using Uri's getLastPathSegment method.
             *
             * In this case, we want to return a cursor that contains one row of weather data for
             * a particular date.
             */
            case CODE_WEATHER_WITH_DATE: {
                String normalizedUtcDateString = uri.getLastPathSegment();
                String cityId = String.valueOf(SunshinePreferences.getCityId(getContext()));
                String[] selectionArguments = new String[]{normalizedUtcDateString, cityId};

                cursor = mOpenHelper.getReadableDatabase().query(
                        /* Table we are going to query */
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        WeatherContract.WeatherEntry.COLUMN_DATE + "=?" +
                                " AND " + WeatherContract.WeatherEntry.COLUMN_CITY_ID + "=?",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            }

            case CODE_WEATHER: {
                String cityId = String.valueOf(SunshinePreferences.getCityId(getContext()));
                String select = WeatherContract.WeatherEntry.COLUMN_CITY_ID + "=?";
                String[] selectArgs = new String[]{cityId};

                cursor = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        select,
                        selectArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }

            case CODE_LOCATIONS:
            {
                String selectedId = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{selectedId};
                cursor = mOpenHelper.getReadableDatabase().query(
                        LocationsContract.LocationsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            }

            case CODE_SINGLE_LOCATION:
            {
                String selectedId = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{selectedId};

                cursor = mOpenHelper.getReadableDatabase().query(
                        LocationsContract.LocationsEntry.TABLE_NAME,
                        projection,
                        LocationsContract.LocationsEntry._ID + "=?",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);

                break;
            }

            case CODE_HOURLY_FORECAST:
                cursor = mOpenHelper.getReadableDatabase().query(
                        HourlyWeatherContract.HourlyWeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_HOURLY_FORECAST_WITH_DATE: {
                String normalizedUtcDateString = uri.getLastPathSegment();
                String cityId = String.valueOf(SunshinePreferences.getCityId(getContext()));
                long normalizedUtcDate = Long.valueOf(normalizedUtcDateString);
                long endDayDate = normalizedUtcDate + SunshineDateUtils.DAY_IN_MILLIS + java.util.concurrent.TimeUnit.HOURS.toMillis(1);
                String endDayDateString = String.valueOf(endDayDate);

                String[] selectionArguments = new String[]{cityId, normalizedUtcDateString, endDayDateString};
                cursor = mOpenHelper.getReadableDatabase().query(
                        HourlyWeatherContract.HourlyWeatherEntry.TABLE_NAME,
                        projection,
                        HourlyWeatherContract.HourlyWeatherEntry.COLUMN_CITY_ID + "=?" +
                                " AND " + HourlyWeatherContract.HourlyWeatherEntry.COLUMN_DATE + " >= ? " +
                                " AND " + HourlyWeatherContract.HourlyWeatherEntry.COLUMN_DATE + " <= ?",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case CODE_CURRENT_FORECAST: {
                String cityId = String.valueOf(SunshinePreferences.getCityId(getContext()));
                String select = WeatherContract.WeatherEntry.COLUMN_CITY_ID + "=?";
                String[] selectArgs = new String[]{cityId};
                cursor = mOpenHelper.getReadableDatabase().query(
                        CurrentWeatherContract.CurrentWeatherEntry.TABLE_NAME,
                        projection,
                        select,
                        selectArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Deletes data at a given URI with optional arguments for more fine tuned deletions.
     *
     * @param uri           The full URI to query
     * @param selection     An optional restriction to apply to rows when deleting.
     * @param selectionArgs Used in conjunction with the selection statement
     * @return The number of rows deleted
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        /* Users of the delete method will expect the number of rows deleted to be returned. */
        int numRowsDeleted;

        /*
         * If we pass null as the selection to SQLiteDatabase#delete, our entire table will be
         * deleted. However, if we do pass null and delete all of the rows in the table, we won't
         * know how many rows were deleted. According to the documentation for SQLiteDatabase,
         * passing "1" for the selection will delete all rows and return the number of rows
         * deleted, which is what the caller of this method expects.
         */

        switch (sUriMatcher.match(uri)) {

            case CODE_WEATHER:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
//                Log.e("DELETING ", "uri " + uri + " SEL: " + selection + " SELSARGS: " +  selectionArgs[0].toString() + " ROWSDEL: " + numRowsDeleted);
                break;
            case CODE_LOCATIONS:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        LocationsContract.LocationsEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case CODE_SINGLE_LOCATION:
                String[] selectionArguments = new String[]{String.valueOf(ContentUris.parseId(uri))};
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        LocationsContract.LocationsEntry.TABLE_NAME,
                        LocationsContract.LocationsEntry._ID + "=?",
                        selectionArguments);
                break;
            case CODE_CURRENT_FORECAST:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        CurrentWeatherContract.CurrentWeatherEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
//                Log.e("DELETING ", "uri " + uri + " SEL: " + selection + " SELSARGS: " +  selectionArgs[0].toString() + " ROWSDEL: " + numRowsDeleted);
                break;
            case CODE_HOURLY_FORECAST:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        HourlyWeatherContract.HourlyWeatherEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
//                Log.e("DELETING ", "uri " + uri + " SEL: " + selection + " SELSARGS: " +  selectionArgs[0].toString() + " ROWSDEL: " + numRowsDeleted);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        /* If we actually deleted any rows, notify that a change has occurred to this URI */
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    /**
     * In Sunshine, we aren't going to do anything with this method. However, we are required to
     * override it as WeatherProvider extends ContentProvider and getType is an abstract method in
     * ContentProvider. Normally, this method handles requests for the MIME type of the data at the
     * given URI. For example, if your app provided images at a particular URI, then you would
     * return an image URI from this method.
     *
     * @param uri the URI to query.
     * @return nothing in Sunshine, but normally a MIME type string, or null if there is no type.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("We are not implementing getType in Sunshine.");
    }

    /**
     * In Sunshine, we aren't going to do anything with this method. However, we are required to
     * override it as WeatherProvider extends ContentProvider and insert is an abstract method in
     * ContentProvider. Rather than the single insert method, we are only going to implement
     * {@link WeatherProvider#bulkInsert}.
     *
     * @param uri    The URI of the insertion request. This must not be null.
     * @param values A set of column_name/value pairs to add to the database.
     *               This must not be null
     * @return nothing in Sunshine, but normally the URI for the newly inserted item.
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {

            case CODE_LOCATIONS:
                Uri insertedUri;
                long rowId;
                rowId = db.insert(LocationsContract.LocationsEntry.TABLE_NAME, null, values);
                if (-1 == rowId) {
                    return null;
                }
                getContext().getContentResolver().notifyChange(uri, null);
                Log.e("Inserted", " " + ContentUris.withAppendedId(uri, rowId));
                return ContentUris.withAppendedId(uri, rowId);

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {

            case CODE_LOCATIONS:

                int rowId;
                rowId = db.update(LocationsContract.LocationsEntry.TABLE_NAME, values, selection, selectionArgs);
                if (-1 == rowId) {
                    return -1;
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return rowId;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * You do not need to call this method. This is a method specifically to assist the testing
     * framework in running smoothly. You can read more at:
     * http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
     */
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}