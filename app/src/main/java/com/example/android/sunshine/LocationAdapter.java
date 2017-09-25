package com.example.android.sunshine;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.android.sunshine.data.CurrentWeatherContract;
import com.example.android.sunshine.data.LocationsContract;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.sync.SunshineSyncUtils;

/**
 * Created by ROBERTO on 08/09/2017.
 */

class LocationAdapter extends CursorAdapter implements View.OnClickListener {

    private static final String LOG_TAG = "Location Adapter";


    private Cursor mCursor;

    //static private ArrayList<String> placeIds;
    private Context mContext;

    private final LocationAdapterOnClickHandler mClickHandler;


    public interface LocationAdapterOnClickHandler {
        void onClick();
    }


    public LocationAdapter(Context context, Cursor cursor, LocationAdapterOnClickHandler clickHandler) {
        super(context, cursor, FLAG_REGISTER_CONTENT_OBSERVER);

        mContext = context;
        mCursor = cursor;
        mClickHandler = clickHandler;
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        return super.swapCursor(newCursor);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_pref_location, null);
        view.setFocusable(true);
        return view;
    }

    @Override
    public void onClick(View view) {
        int position = (int) view.getTag();
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        long cityId = cursor.getLong(LocationActivity.INDEX_ID);

        SunshinePreferences.setCityId(mContext, cityId);
        double latitude = Double.valueOf(cursor.getString(LocationActivity.INDEX_CITY_LATITUDE));
        double longitude = Double.valueOf(cursor.getString(LocationActivity.INDEX_CITY_LONGITUDE));
        String city = cursor.getString(LocationActivity.INDEX_CITY_NAME);
        String placeId = cursor.getString(LocationActivity.INDEX_PLACE_ID);
        SunshinePreferences.setLocationDetails(mContext, latitude, longitude, city, placeId);
        SunshineSyncUtils.startImmediateSync(mContext);
        ForecastFragment.reload();

        //HourlyActivity.reload();
        ((Activity) mContext).finish();

    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        TextView textViewPrefLocation = (TextView) view.findViewById(R.id.textViewPrefLocation);
        ImageButton deleteCity = (ImageButton) view.findViewById(R.id.button_delete_city);

        final int position = cursor.getInt(cursor.getColumnIndex(LocationsContract.LocationsEntry._ID));


        deleteCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Delete this city from location table
                Uri uri = ContentUris.withAppendedId(LocationsContract.LocationsEntry.CONTENT_URI, Long.valueOf(position));
                mContext.getContentResolver().delete(uri, null, null);

                // Delete this city's weather forecast from weather table
                String cityId = String.valueOf(SunshinePreferences.getCityId(mContext));
                Log.e("Sync Task", "City id " + cityId);

                String[] selectionArgs = new String[]{cityId};

                mContext.getContentResolver().delete(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        WeatherContract.WeatherEntry.COLUMN_CITY_ID + "=?",
                        selectionArgs);

                mContext.getContentResolver().delete(
                        CurrentWeatherContract.CurrentWeatherEntry.CONTENT_URI,
                        CurrentWeatherContract.CurrentWeatherEntry.COLUMN_CITY_ID + "=?",
                        selectionArgs);
            }
        });

        String cityName = cursor.getString(LocationActivity.INDEX_CITY_NAME);
        textViewPrefLocation.setText(cityName);

        //pass cursor position via tag to be used in onClick;
        view.setTag(cursor.getPosition());
        view.setOnClickListener(this);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }
}
