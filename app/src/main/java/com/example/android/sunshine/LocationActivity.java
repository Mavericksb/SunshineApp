package com.example.android.sunshine;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.sunshine.data.LocationsContract;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.sync.SunshineSyncUtils;
import com.example.android.sunshine.utilities.SunshineLocationUtils;

import java.util.ArrayList;

public class LocationActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private static final int LOCATION_LOADER_ID = 13;

    private LocationAdapter mLocationAdapter;
    private ImageButton mButtonAddCity;
    private ImageButton mDeleteCity;
    private OnClickListener mOnClickListener;
    private View mViewGeolocality;
    private TextView mTextViewGeolocality;
    private TextView mTextViewFindMe;

    private static Cursor mGeolocalityCursor;

    public static final String[] LOCATION_PROJECTION = {
            LocationsContract.LocationsEntry._ID,
            LocationsContract.LocationsEntry.COLUMN_NAME,
            LocationsContract.LocationsEntry.COLUMN_LATITUDE,
            LocationsContract.LocationsEntry.COLUMN_LONGITUDE,
            LocationsContract.LocationsEntry.COLUMN_PLACEID,
            LocationsContract.LocationsEntry.COLUMN_LAST_UPDATE

    };

    public static final int INDEX_ID = 0;
    public static final int INDEX_CITY_NAME = 1;
    public static final int INDEX_CITY_LATITUDE = 2;
    public static final int INDEX_CITY_LONGITUDE = 3;
    public static final int INDEX_PLACE_ID = 4;
    public static final int INDEX_LAST_UPDATE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ListView listView = (ListView) findViewById(R.id.PrefLocationlist);

        mButtonAddCity = (ImageButton) findViewById(R.id.button_add_city);


        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id = view.getId();
                switch(id){
                    case R.id.button_add_city:
                        Intent launchAutoComplete = new Intent(getApplicationContext(), AutoCompleteActivity.class);
                        startActivity(launchAutoComplete);
                        Log.e("PREF CITY ID?", "" + SunshinePreferences.getCityId(getApplicationContext()));
                        break;
                    case R.id.view_geolocality:
                        Uri queryUrl = LocationsContract.LocationsEntry.CONTENT_URI;
                        Cursor cursor = getContentResolver().query(queryUrl,
                                LOCATION_PROJECTION,
                                LocationsContract.LocationsEntry.COLUMN_PLACEID + " = ? ",
                                new String[]{LocationsContract.LocationsEntry.UNIQUE_GEOLOCATION_ID},
                                null
                                );
                        if(cursor != null && SunshinePreferences.getRequestUpdates(LocationActivity.this)) {
                            cursor.moveToFirst();
                            long cityId = cursor.getLong(LocationActivity.INDEX_ID);
                            SunshinePreferences.setCityId(LocationActivity.this, cityId);

                            double latitude = Double.valueOf(cursor.getString(LocationActivity.INDEX_CITY_LATITUDE));
                            double longitude = Double.valueOf(cursor.getString(LocationActivity.INDEX_CITY_LONGITUDE));
                            String city = cursor.getString(LocationActivity.INDEX_CITY_NAME);
                            String placeId = cursor.getString(LocationActivity.INDEX_PLACE_ID);
                            SunshinePreferences.setLocationDetails(LocationActivity.this, latitude, longitude, city, placeId);
                            long lastUpdate = cursor.getLong(LocationActivity.INDEX_LAST_UPDATE);
                            Log.e("Location Adapter", " Last Update is " + lastUpdate);

                            long timeSinceLastUpdate = SunshinePreferences
                                    .getEllapsedTimeSinceLastLocationUpdate(LocationActivity.this, lastUpdate);

                            boolean halfHourPassedSinceLastNotification = false;

                            if (timeSinceLastUpdate >= (DateUtils.MINUTE_IN_MILLIS * 30)) {
                                halfHourPassedSinceLastNotification = true;
                            }

                            if (lastUpdate == -1 || halfHourPassedSinceLastNotification) {
                                SunshineLocationUtils.updateLastLocationUpdate(LocationActivity.this, cityId);
                                SunshineSyncUtils.startImmediateSync(LocationActivity.this);
                            } else {
                                MainActivity.reload();
                                ForecastFragment.reload();
                            }
                            cursor.close();
                        } else {
                            SunshinePreferences.setRequestUpdates(LocationActivity.this, true);
                        }
                        finish();
                        break;
                    default:
                        break;
                }
            }
        };

        mButtonAddCity.setOnClickListener(mOnClickListener);
        mViewGeolocality = findViewById(R.id.view_geolocality);
        mViewGeolocality.setOnClickListener(mOnClickListener);
        mTextViewGeolocality = (TextView)findViewById(R.id.textViewGeolocality);
        mTextViewFindMe = (TextView)findViewById(R.id.text_view_find_me);


        mLocationAdapter = new LocationAdapter(this, null);

        listView.setAdapter(mLocationAdapter);

        getSupportLoaderManager().initLoader(LOCATION_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {

            case LOCATION_LOADER_ID:
                /* URI for all rows of weather data in our weather table */
                Uri locationQueryUri = LocationsContract.LocationsEntry.CONTENT_URI;

                String sortOrder = LocationsContract.LocationsEntry._ID + " DESC";

                Cursor geoLocalityCursor = getContentResolver().query(LocationsContract.LocationsEntry.CONTENT_URI,
                        LOCATION_PROJECTION,
                        LocationsContract.LocationsEntry.COLUMN_PLACEID + " = ? ",
                        new String[]{LocationsContract.LocationsEntry.UNIQUE_GEOLOCATION_ID},
                        sortOrder);
                if(geoLocalityCursor!=null) {
                    geoLocalityCursor.moveToFirst();
                    if(SunshinePreferences.getRequestUpdates(LocationActivity.this)) {
                        mTextViewFindMe.setText("Current");
                        mTextViewGeolocality.setText("(" + geoLocalityCursor.getString(INDEX_CITY_NAME) + ")");
                    } else {
                        mTextViewFindMe.setText("Find me");
                        mTextViewGeolocality.setText("");
                    }
                    geoLocalityCursor.close();
                }
                return new CursorLoader(this,
                        locationQueryUri,
                        LOCATION_PROJECTION,
                        LocationsContract.LocationsEntry.COLUMN_PLACEID + " != ? ",
                        new String[]{LocationsContract.LocationsEntry.UNIQUE_GEOLOCATION_ID},
                        sortOrder);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mLocationAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mLocationAdapter.swapCursor(null);
    }

}
