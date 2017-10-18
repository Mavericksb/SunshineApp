package com.example.android.sunshine;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.os.ResultReceiver;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
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
import com.example.android.sunshine.data.LocationsContract;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.sync.Constants;
import com.example.android.sunshine.sync.FetchAddressIntentService;
import com.example.android.sunshine.sync.SunshineSyncUtils;
import com.example.android.sunshine.utilities.SunshineLocationUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Date;

import static com.example.android.sunshine.data.LocationsContract.LocationsEntry.UNIQUE_GEOLOCATION_ID;

/**
 * Created by Robert on 19/09/2017.
 */


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG = this.getClass().getSimpleName();

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    public static final String FORECAST_TAG = "forecast_fragment";
    public static final String HOURLY_TAG = "hourly_fragment";

    private static final int ID_CURRENT_LOADER_BACKGROUND = 101;
    private static final int ID_WEATHER_LOADER_BACKGROUND = 102;
    private static LoaderManager mSupportLoaderManager;
    private static LoaderManager.LoaderCallbacks mLoaderCallbacks;

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

    public static LocationRequest mLocationBalancedRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    /**
     * Callback for Location events.
     */
    public static LocationCallback mLocationCallback;
    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;
    private AddressResultReceiver mResultReceiver;
    private String mLastUpdateTime;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private Boolean mRequestingLocationUpdates;
    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mToolbarCityName = (TextView) findViewById(R.id.toolbar_city_name);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
            R.string.navigation_drawer_open, R.string.navigation_drawer_closed) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                resumeLocationUpdates();
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mRequestingLocationUpdates = SunshinePreferences.getRequestUpdates(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        mLastUpdateTime = "";
        mResultReceiver = new AddressResultReceiver(new Handler());

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();


        View mIncludeBackground = findViewById(R.id.include_background);
        mIncludeBackground.setVisibility(View.INVISIBLE);

        mImageAnimator = new ImageAnimator(this, mIncludeBackground);

        FragmentManager fm = getSupportFragmentManager();
        Fragment forecastFragment = fm.findFragmentByTag(MainActivity.FORECAST_TAG);
        Fragment hourlyFragment = fm.findFragmentByTag(MainActivity.HOURLY_TAG);
        FragmentTransaction ft = fm.beginTransaction();

        if (forecastFragment == null) {
            Log.e("On create", "I'm here");
            forecastFragment = new ForecastFragment();

            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, forecastFragment, FORECAST_TAG).commit();
        } else {
            ft.show(forecastFragment);
        }

        mSupportLoaderManager = getSupportLoaderManager();
        mLoaderCallbacks = this;
        getSupportLoaderManager().initLoader(ID_WEATHER_LOADER_BACKGROUND, null, this);
        getSupportLoaderManager().initLoader(ID_CURRENT_LOADER_BACKGROUND, null, this);

        if (mWeatherId != null && mDateTime != -1) {
            mImageAnimator.playAnimation(mWeatherId, mDateTime, mSunrise, mSunset, true);
        }
        SunshineSyncUtils.initialize(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeLocationUpdates();
        updateLocationUI();
    }

    private void resumeLocationUpdates(){
        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
        if (SunshinePreferences.getRequestUpdates(this) && checkPermissions()) {
            Log.e(TAG, "Start Updating");
            startLocationUpdates();
        } else if (SunshinePreferences.getRequestUpdates(this) && !checkPermissions()) {
            Log.e(TAG, "Request Permissions");
            requestPermissions();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Remove location updates to save battery.
        stopLocationUpdates();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
//        boolean drawerOpen = mDrawerLayout.isDrawerOpen();
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }


    /**
     * This is where we inflate and set up the menu for this Activity.
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

//        if (id == R.id.action_settings) {
            if (mDrawerToggle.onOptionsItemSelected(item)) {
                return true;
            }
//        }
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
                mCurrentBackgroundCursor = null;
                mForecastBackgroundCursor = null;
                mToolbarCityName.setText(SunshinePreferences.getCityName(this));
            } else {
                mImageAnimator.stopAnimation();
                mToolbarCityName.setText("Add a location");
            }
        }
    }

    public static void reload() {
        mSupportLoaderManager.restartLoader(ID_CURRENT_LOADER_BACKGROUND, null, mLoaderCallbacks);
        mSupportLoaderManager.restartLoader(ID_WEATHER_LOADER_BACKGROUND, null, mLoaderCallbacks);
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mCurrentLocation = locationResult.getLastLocation();
                // In some rare cases the location returned can be null
                if (mCurrentLocation == null) {
                    return;
                }

                if (!Geocoder.isPresent()) {
                    Toast.makeText(MainActivity.this,
                            R.string.no_geocoder_available,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // Start service and update UI to reflect new location
                startIntentService();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                updateLocationUI();
            }
        };
    }


    private void createLocationRequest() {
        mLocationBalancedRequest = new LocationRequest();
        mLocationBalancedRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationBalancedRequest.setFastestInterval(1000 * 60 * 15);
        mLocationBalancedRequest.setInterval(1000 * 60 * 60);
    }


    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationBalancedRequest);
        mLocationSettingsRequest = builder.build();
    }


    private void startLocationUpdates() {

// Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest).
                addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.e(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationBalancedRequest,
                                mLocationCallback, Looper.myLooper());

                        updateLocationUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.e(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.e(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }
                        updateLocationUI();
                    }
                });

    }


    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.e(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                SunshinePreferences.setRequestUpdates(MainActivity.this, false);
                mRequestingLocationUpdates = false;
            }
        });
    }


    private void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mCurrentLocation);
        startService(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.e(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e(TAG, "User chose not to make required location settings changes.");
                        SunshinePreferences.setRequestUpdates(this, false);
                        mRequestingLocationUpdates = false;
                        updateLocationUI();
                        break;
                }
                break;
        }
    }


    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.e(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.e(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.e(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates) {
                    Log.e(TAG, "Permission granted, updates requested, starting location updates");
                    startLocationUpdates();
                }
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless.

                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                SunshinePreferences.setRequestUpdates(this, false);
            }
        }
    }

    /**
     * Shows a {@link Snackbar}.
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId, View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_LONG)
                .setAction(getString(actionStringId), listener).show();
    }

     /* Sets the value of the UI fields for the location latitude, longitude and last update time*/
    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            Log.e("UPDATE GEO", "LAT " + mCurrentLocation.getLatitude() + " LON " +
                    mCurrentLocation.getLongitude() + " LAST UPDATE " + mLastUpdateTime);
        }
    }


    private class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            // Display the address string or an error message sent from the intent service.
            String mLocality = resultData.getString(Constants.RESULT_DATA_KEY);

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {

                Uri queryUri = LocationsContract.LocationsEntry.CONTENT_URI;
                String[] projection = new String[]{LocationsContract.LocationsEntry.COLUMN_PLACEID};
                String selection = LocationsContract.LocationsEntry.COLUMN_PLACEID + "=?";
                String[] selectionArgs = new String[]{UNIQUE_GEOLOCATION_ID};

                Cursor locationCursor = getContentResolver().query(queryUri, projection, selection, selectionArgs, null);

                if(locationCursor!=null) {
                    int isGeolocalityPresent = locationCursor.getCount();
                    if (isGeolocalityPresent == 0) {
                        SunshineLocationUtils.insertLocation(MainActivity.this, mLocality, mCurrentLocation.getLatitude(),
                                mCurrentLocation.getLongitude(), UNIQUE_GEOLOCATION_ID);
                    } else {
                        SunshineLocationUtils.updateLocation(MainActivity.this, mLocality, mCurrentLocation.getLatitude(),
                                mCurrentLocation.getLongitude());
                    }

                    locationCursor.close();
                } else {
                    Toast noAddress = Toast.makeText(MainActivity.this, "" + getString(R.string.no_address_found), Toast.LENGTH_LONG);
                    noAddress.show();
                }
            }
        }
    }




}








