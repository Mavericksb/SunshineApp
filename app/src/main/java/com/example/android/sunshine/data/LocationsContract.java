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

import android.net.Uri;
import android.provider.BaseColumns;

import com.example.android.sunshine.utilities.SunshineDateUtils;

/**
 * Defines table and column names for the weather database. This class is not necessary, but keeps
 * the code organized.
 */
public class LocationsContract {


    public static final String CONTENT_AUTHORITY = "com.example.android.sunshine";


    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static final String PATH_LOCATIONS = "locations";

    /* Inner class that defines the table contents of the weather table */
    public static final class LocationsEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the Weather table from the content provider */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_LOCATIONS)
                .build();

        /* Used internally as the name of our weather table. */
        public static final String TABLE_NAME = "locations";

        /* Location name (e.g. New York) */
        public static final String COLUMN_NAME = "name";

        /* Weather ID as returned by API, used to identify the icon to be used */
        public static final String COLUMN_PLACEID = "place_id";

        /* Min and max temperatures in °C for the day (stored as floats in the database) */
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_LAST_UPDATE = "last_update";

        public static final String UNIQUE_GEOLOCATION_ID = "unique_geolocation_id";
        public static final long WEATHER_UPDATE_NEEDED = -1;
    }
}