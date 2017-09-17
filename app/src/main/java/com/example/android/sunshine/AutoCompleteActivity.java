package com.example.android.sunshine;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.android.sunshine.data.LocationsContract;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.sync.SunshineSyncUtils;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * Created by ROBERTO on 07/09/2017.
 */

public class AutoCompleteActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    // The entry points to the Places API.


    private static final String LOG_TAG = "Location Autocomplete";
    private GeoDataClient mGeoDataClient;

    private AutoCompleteTextView mAutoComplete;
    private AutoCompleteAdapter mAutoCompleteAdapter;

    private double mLatitude;
    private double mLongitude;
    private String mCity;
    private String mPlaceId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autocomplete);

        mGeoDataClient = Places.getGeoDataClient(this, null);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAutoComplete = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        mAutoCompleteAdapter = new AutoCompleteAdapter(this, R.layout.list_item_location_autocomplete, R.id.textViewListLocation);

        mAutoCompleteAdapter.setDropDownViewResource(R.layout.list_item_location_autocomplete);
        mAutoComplete.setAdapter(mAutoCompleteAdapter);
        mAutoComplete.setOnItemClickListener(this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
         * Normally, calling setDisplayHomeAsUpEnabled(true) (we do so in onCreate here) as well as
         * declaring the parent activity in the AndroidManifest is all that is required to get the
         * up button working properly. However, in this case, we want to navigate to the previous
         * screen the user came from when the up button was clicked, rather than a single
         * designated Activity in the Manifest.
         *
         * We use the up button's ID (android.R.id.home) to listen for when the up button is
         * clicked and then call onBackPressed to navigate to the previous Activity when this
         * happens.
         */
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        AutoCompleteAdapter.PlaceAutocomplete item = mAutoCompleteAdapter.getItem(position);
        mPlaceId = String.valueOf(item.placeId);
        mCity = (String) item.city;

        new GetCoordsTask().execute();
    }


    private class GetCoordsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Task<PlaceBufferResponse> placeResponse = mGeoDataClient.getPlaceById(mPlaceId);
                PlaceBufferResponse placeBufferResponse = Tasks.await(placeResponse, 30, TimeUnit.SECONDS);


                if (placeResponse.isSuccessful()) {
                    Iterator<Place> iterator = placeBufferResponse.iterator();
                    while (iterator.hasNext()) {
                        Place place = iterator.next();
                        mLatitude = place.getLatLng().latitude;
                        mLongitude = place.getLatLng().longitude;
                    }

                    placeBufferResponse.release();

                } else {
                    Exception exception = placeResponse.getException();
                    Log.e("OnCompleteAutocomplete", "Exception " + exception);
                    placeBufferResponse.release();
                }
            } catch (ExecutionException e) {
                Log.e(LOG_TAG, "AutoComplete request failed: " + e);
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "AutoComplete request interrupted: " + e);
            } catch (TimeoutException e) {
                Log.e(LOG_TAG, "AutoComplete request interrupted: " + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //Create a Content value table pairing
            ContentValues values = new ContentValues();

            values.put(LocationsContract.LocationsEntry.COLUMN_NAME, mCity);
            values.put(LocationsContract.LocationsEntry.COLUMN_LATITUDE, mLatitude);
            values.put(LocationsContract.LocationsEntry.COLUMN_LONGITUDE, mLongitude);
            values.put(LocationsContract.LocationsEntry.COLUMN_PLACEID, mPlaceId);

            Uri newUri = getContentResolver().insert(LocationsContract.LocationsEntry.CONTENT_URI, values);

                if(newUri==null){
                    Toast notSucceded = Toast.makeText(getApplicationContext(), "Failed to add city", Toast.LENGTH_SHORT);
                    notSucceded.show();
                } else {
                    // Get the Location Table ID for this city and store it in Shared preference in order to be
                    // used whenever Weather table creates the data for a given city to store its city reference under
                    // CITY_ID
                    long cityId = ContentUris.parseId(newUri);
                    SunshinePreferences.setCityId(AutoCompleteActivity.this, cityId);

                    SunshinePreferences.setLocationDetails(AutoCompleteActivity.this, mLatitude, mLongitude, mCity, mPlaceId);
                    // Coordinates changed so I need to fetch data from server
                    SunshineSyncUtils.startImmediateSync(AutoCompleteActivity.this);
                    finish();
                }

            }
        }

}




