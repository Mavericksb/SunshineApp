package com.example.android.sunshine;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.data.CurrentWeatherContract;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.sync.SunshineSyncUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


import java.lang.ref.WeakReference;

/**
 * Created by Robert on 19/09/2017.
 */



public class MainActivity extends AppCompatActivity implements

        LoaderManager.LoaderCallbacks<Cursor> {

    private FusedLocationProviderClient mFusedLocationClient;

    public static final String FORECAST_TAG = "forecast_fragment";
    public static final String HOURLY_TAG = "hourly_fragment";

    private static final int ID_CURRENT_LOADER_BACKGROUND = 101;
    private static final int ID_WEATHER_LOADER_BACKGROUND = 102;

    private static Cursor mCurrentBackgroundCursor;
    private static Cursor mForecastBackgroundCursor;

    private ImageAnimator mImageAnimator;

    private static String mWeatherId = null;
    private static long mDateTime = -1;
    private static long mSunrise = -1;
    private static long mSunset = -1;


    public static final String[] CURRENT_FORECAST_PROJECTION = {
            CurrentWeatherContract.CurrentWeatherEntry.COLUMN_WEATHER_ID,
            CurrentWeatherContract.CurrentWeatherEntry.COLUMN_DATE,
    };

    public static final int INDEX_WEATHER_ID = 0;
    public static final int INDEX_DATE = 1;

    public static final String[] MAIN_FORECAST_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_SUNRISE_TIME,
            WeatherContract.WeatherEntry.COLUMN_SUNSET_TIME
    };

    public static final int INDEX_SUNRISE_TIME = 0;
    public static final int INDEX_SUNSET_TIME = 1;

    TextView mToolbarCityName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbarCityName = (TextView) findViewById(R.id.toolbar_city_name);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (checkLocationPermission()) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                Log.e("Last Location", "Lat is " + location.getLatitude() + " Lon is " + location.getLongitude());
                            }
                        }
                    });
        }

        View mIncludeBackground = findViewById(R.id.include_background);

        mIncludeBackground.setVisibility(View.INVISIBLE);

        mImageAnimator = new ImageAnimator(this, mIncludeBackground);

        FragmentManager fm = getSupportFragmentManager();
        Fragment forecastFragment = fm.findFragmentByTag(MainActivity.HOURLY_TAG);

        FragmentTransaction ft = fm.beginTransaction();

        if (forecastFragment == null) {
            forecastFragment = new ForecastFragment();

            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, forecastFragment, FORECAST_TAG).commit();
        } else {
            ft.show(forecastFragment);
        }

        getSupportLoaderManager().initLoader(ID_WEATHER_LOADER_BACKGROUND, null, this);
        getSupportLoaderManager().initLoader(ID_CURRENT_LOADER_BACKGROUND, null, this);


        if (mWeatherId != null && mDateTime != -1) {
            mImageAnimator.playAnimation(mWeatherId, mDateTime, mSunrise, mSunset, true);
        }

        SunshineSyncUtils.initialize(this);
    }

    public boolean checkLocationPermission() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * This is where we inflate and set up the menu for this Activity.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.forecast, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_location) {
            startActivity(new Intent(this, LocationActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {

        switch (loaderId) {


            case ID_CURRENT_LOADER_BACKGROUND:
                return new CursorLoader(this,
                        CurrentWeatherContract.CurrentWeatherEntry.CONTENT_URI,
                        CURRENT_FORECAST_PROJECTION,
                        null,
                        null,
                        null);
            case ID_WEATHER_LOADER_BACKGROUND:

                /* URI for all rows of weather data in our weather table */
                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
                /* Sort order: Ascending by date */
                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
                return new CursorLoader(this,
                        forecastQueryUri,
                        MAIN_FORECAST_PROJECTION,
                        null, //selection,
                        null, //selectionArgs,
                        sortOrder);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor data) {

        int loaderId = loader.getId();
        switch (loaderId) {
            case ID_CURRENT_LOADER_BACKGROUND:
                mCurrentBackgroundCursor = data;
                break;
            case ID_WEATHER_LOADER_BACKGROUND:
                mForecastBackgroundCursor = data;
                break;
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }

        if (mCurrentBackgroundCursor != null && mForecastBackgroundCursor != null) {
            if (mCurrentBackgroundCursor.getCount() != 0 && mForecastBackgroundCursor.getCount() != 0) {
                mCurrentBackgroundCursor.moveToFirst();
                mForecastBackgroundCursor.moveToFirst();
                mWeatherId = mCurrentBackgroundCursor.getString(INDEX_WEATHER_ID);
                mDateTime = mCurrentBackgroundCursor.getLong(INDEX_DATE);
                mSunrise = mForecastBackgroundCursor.getLong(INDEX_SUNRISE_TIME);
                mSunset = mForecastBackgroundCursor.getLong(INDEX_SUNSET_TIME);
                mImageAnimator.playAnimation(mWeatherId, mDateTime, mSunrise, mSunset, false);
                mForecastBackgroundCursor = null;
                mToolbarCityName.setText(SunshinePreferences.getCityName(this));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}

class SettingsClient extends GoogleApi<Api.ApiOptions.NoOptions>{

    protected SettingsClient(@NonNull Context context, Api<Api.ApiOptions.NoOptions> api, Looper looper) {
        super(context, api, looper);

        LocationRequest mLocationBalancedRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        .setFastestInterval(1000 * 60 * 5)
                .setInterval(1000 * 60 * 60);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationBalancedRequest);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(context).checkLocationSettings(builder.build());

    }

    public void onComplete(Task<LocationSettingsResponse> task) {
        try {
            LocationSettingsResponse response = task.getResult(ApiException.class);
            // All location settings are satisfied. The client can initialize location
            // requests here.
             
        } catch (ApiException exception) {
            switch (exception.getStatusCode()) {
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied. But could be fixed by showing the
                    // user a dialog.
                    try {
                        // Cast to a resolvable exception.
                        ResolvableApiException resolvable = (ResolvableApiException) exception;
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        resolvable.startResolutionForResult(
                                OuterClass.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    } catch (ClassCastException e) {
                        // Ignore, should be an impossible error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // Location settings are not satisfied. However, we have no way to fix the
                    // settings so we won't show the dialog.
                     ...
                    break;
            }
        }
    }
});
}