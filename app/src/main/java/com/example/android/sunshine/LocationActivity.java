package com.example.android.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import com.example.android.sunshine.data.LocationsContract;
import com.example.android.sunshine.data.WeatherContract;

import java.util.ArrayList;

public class LocationActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private static final int LOCATION_LOADER_ID = 13;

    private LocationAdapter mLocationAdapter;
    private ImageButton mButtonAddCity;

    private static final String[] LOCATION_PROJECTION = {
            LocationsContract.LocationsEntry._ID,
            LocationsContract.LocationsEntry.COLUMN_NAME,
            LocationsContract.LocationsEntry.COLUMN_LATITUDE,
            LocationsContract.LocationsEntry.COLUMN_LONGITUDE
    };

    public static final int INDEX_CITY_NAME = 1;
    public static final int INDEX_CITY_LATITUDE = 2;
    public static final int INDEX_CITY_LONGITUDE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        ListView listView = (ListView) findViewById(R.id.PrefLocationlist);

        mButtonAddCity = (ImageButton) findViewById(R.id.button_add_acity);

        mButtonAddCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchAutoComplete = new Intent(getApplicationContext(), AutoCompleteActivity.class);
                startActivity(launchAutoComplete);
            }
        });


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
}
