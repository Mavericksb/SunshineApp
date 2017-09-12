package com.example.android.sunshine;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.sync.SunshineSyncUtils;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


/**
 * Created by ROBERTO on 07/09/2017.
 */

public class LocationActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

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

//            Toast city = Toast.makeText(this, "Città " + current + " Id " + placeId, Toast.LENGTH_SHORT);
//            city.show();
        Task<PlaceBufferResponse> placeResponse = mGeoDataClient.getPlaceById(placeId);


        placeResponse.addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if(task.isSuccessful()){
                    PlaceBufferResponse placeBuffer = task.getResult();

                    for(Place currentPlace : placeBuffer){
                        mLatitude = currentPlace.getLatLng().latitude;
                        mLongitude = currentPlace.getLatLng().longitude;
                    }

                    SunshinePreferences.setLocationDetails(LocationActivity.this, mLatitude, mLongitude);

                    // Coordinates changed so I need to fetch data from server
                    SunshineSyncUtils.startImmediateSync(getApplicationContext());

                    //Release buffer to avoid memory leaks
                    placeBuffer.release();
                } else{
                    Exception exception = task.getException();
                    Log.e("OnCompleteAutocomplete", "Exception " + exception);

                }
            }
        });


    }

}
