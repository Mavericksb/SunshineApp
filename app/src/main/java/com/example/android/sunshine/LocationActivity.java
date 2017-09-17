package com.example.android.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.example.android.sunshine.data.LocationsContract;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.WeatherContract;

import java.util.ArrayList;

public class LocationActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        LocationAdapter.LocationAdapterOnClickHandler{

    private static final int LOCATION_LOADER_ID = 13;

    private LocationAdapter mLocationAdapter;
    private ImageButton mButtonAddCity;
    private ImageButton mDeleteCity;
    private OnClickListener mOnClickListener;

    private static final String[] LOCATION_PROJECTION = {
            LocationsContract.LocationsEntry._ID,
            LocationsContract.LocationsEntry.COLUMN_NAME,
            LocationsContract.LocationsEntry.COLUMN_LATITUDE,
            LocationsContract.LocationsEntry.COLUMN_LONGITUDE,
            LocationsContract.LocationsEntry.COLUMN_PLACEID
    };

    public static final int INDEX_ID = 0;
    public static final int INDEX_CITY_NAME = 1;
    public static final int INDEX_CITY_LATITUDE = 2;
    public static final int INDEX_CITY_LONGITUDE = 3;
    public static final int INDEX_PLACE_ID = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

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
                    default:
                        break;
                }
            }
        };

        mButtonAddCity.setOnClickListener(mOnClickListener);

        mLocationAdapter = new LocationAdapter(this, null, this);

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

                return new CursorLoader(this,
                        locationQueryUri,
                        LOCATION_PROJECTION,
                        null,
                        null,
                        null);

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

    @Override
    public void onClick() {

    }

}
