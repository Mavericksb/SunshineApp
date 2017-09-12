package com.example.android.sunshine;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

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

public class LocationActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    // The entry points to the Places API.


    private static final String LOG_TAG = "Location Autocomplete";
    private GeoDataClient mGeoDataClient;

    private AutoCompleteTextView mAutoComplete;
    private AutoCompleteAdapter mAutoCompleteAdapter;

    private double mLatitude;
    private double mLongitude;
    private String mCity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        mGeoDataClient = Places.getGeoDataClient(this, null);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAutoComplete = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        mAutoCompleteAdapter = new AutoCompleteAdapter(this, R.layout.list_item_location_autocomplete);
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
        String placeId = String.valueOf(item.placeId);
        mCity = (String) item.city;
        SunshinePreferences.setLocationName(this, mCity);
        final boolean isInitializing = false;

        getCoordinates(placeId, getApplicationContext());
    }

    public void getCoordinates(String placeId, Context context) {


        try {
            Task<PlaceBufferResponse> placeResponse = mGeoDataClient.getPlaceById(placeId);
            PlaceBufferResponse placeBufferResponse = Tasks.await(placeResponse, 30, TimeUnit.SECONDS);


            if (placeResponse.isSuccessful()) {
                Iterator<Place> iterator = placeBufferResponse.iterator();
                while (iterator.hasNext()) {
                    Place place = iterator.next();
                    mLatitude = place.getLatLng().latitude;
                    mLongitude = place.getLatLng().longitude;
                }
                SunshinePreferences.setLocationDetails(context, mLatitude, mLongitude);
                placeBufferResponse.release();
                    // Coordinates changed so I need to fetch data from server
                    SunshineSyncUtils.startImmediateSync(context);
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
    }
}


//            placeResponse.addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
//                @Override
//                public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
//                    if(task.isSuccessful()){
//                        PlaceBufferResponse placeBuffer = task.getResult();
//
//                        for(Place currentPlace : placeBuffer){
//                            mLatitude = currentPlace.getLatLng().latitude;
//                            mLongitude = currentPlace.getLatLng().longitude;
//                        }
//
//                        SunshinePreferences.setLocationDetails(context, mLatitude, mLongitude);
//
//                        if(!isInitializing) {
//                            // Coordinates changed so I need to fetch data from server
//                            SunshineSyncUtils.startImmediateSync(context);
//                        }
//                        coords[0] = mLatitude;
//                        coords[1] = mLongitude;
//
//
//                        //Release buffer to avoid memory leaks
//                        placeBuffer.release();
//                    } else{
//                        Exception exception = task.getException();
//                        Log.e("OnCompleteAutocomplete", "Exception " + exception);
//
//                    }
//                }
//            });
//        return coords;
//    }


