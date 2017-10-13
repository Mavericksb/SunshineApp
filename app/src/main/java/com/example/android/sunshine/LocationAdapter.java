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
import com.example.android.sunshine.data.HourlyWeatherContract;
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
        //ForecastFragment.reload();

        //HourlyActivity.reload();
        ((Activity) mContext).finish();

    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView textViewPrefLocation = (TextView) view.findViewById(R.id.textViewPrefLocation);
        ImageButton deleteCity = (ImageButton) view.findViewById(R.id.button_delete_city);

        final int cursorPosition = cursor.getPosition();
        final int position = cursor.getInt(cursor.getColumnIndex(LocationsContract.LocationsEntry._ID));
        final String[] stringPosition = new String[]{String.valueOf(position)};

        final String cityName = cursor.getString(cursor.getColumnIndex(LocationsContract.LocationsEntry.COLUMN_NAME));
        textViewPrefLocation.setText(cityName);

        //pass cursor position via tag to be used in onClick;
        view.setTag(cursor.getPosition());
        view.setOnClickListener(this);


        deleteCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View buttonView) {

                Log.e("ACTUAL onClick", "Cur pos " + cursorPosition + " cur ext position is " + position + " city is " + cityName);
                if (cursor.moveToPosition(cursorPosition)) {
                    if (cursor.moveToPrevious()) {
                        Log.e("PREV onClick", "First cur pos " + cursor.getPosition() + "  id is " + cursor.getInt(cursor.getColumnIndex(LocationsContract.LocationsEntry._ID)) + " city is " + cursor.getString(cursor.getColumnIndex(LocationsContract.LocationsEntry.COLUMN_NAME)));
                        long cityId = cursor.getLong(LocationActivity.INDEX_ID);
                        SunshinePreferences.setCityId(mContext, cityId);

                        double latitude = Double.valueOf(cursor.getString(LocationActivity.INDEX_CITY_LATITUDE));
                        double longitude = Double.valueOf(cursor.getString(LocationActivity.INDEX_CITY_LONGITUDE));
                        String city = cursor.getString(LocationActivity.INDEX_CITY_NAME);
                        String placeId = cursor.getString(LocationActivity.INDEX_PLACE_ID);
                        SunshinePreferences.setLocationDetails(mContext, latitude, longitude, city, placeId);

                        SunshineSyncUtils.startImmediateSync(mContext);
                    } else if (cursor.moveToPosition(cursorPosition)) {
                        if (cursor.moveToNext()) {
                            Log.e("NEXT onClick", "First cur pos " + cursor.getPosition() + "  id is " + cursor.getInt(cursor.getColumnIndex(LocationsContract.LocationsEntry._ID)) + " city is " + cursor.getString(cursor.getColumnIndex(LocationsContract.LocationsEntry.COLUMN_NAME)));
                            long cityId = cursor.getLong(LocationActivity.INDEX_ID);
                            SunshinePreferences.setCityId(mContext, cityId);

                            double latitude = Double.valueOf(cursor.getString(LocationActivity.INDEX_CITY_LATITUDE));
                            double longitude = Double.valueOf(cursor.getString(LocationActivity.INDEX_CITY_LONGITUDE));
                            String city = cursor.getString(LocationActivity.INDEX_CITY_NAME);
                            String placeId = cursor.getString(LocationActivity.INDEX_PLACE_ID);
                            SunshinePreferences.setLocationDetails(mContext, latitude, longitude, city, placeId);

                            SunshineSyncUtils.startImmediateSync(mContext);
                        } else {
                            Log.e("NO onClick", "Reset");
                            SunshinePreferences.resetLocationCoordinates(mContext);
                            SunshinePreferences.resetCityId(mContext);
                            //SunshineSyncUtils.startImmediateSync(mContext);
                        }
                    }
                }

                //Delete this city from location table
                Uri uri = ContentUris.withAppendedId(LocationsContract.LocationsEntry.CONTENT_URI, Long.valueOf(position));
                mContext.getContentResolver().delete(uri, null, null);

                // Delete this city's weather forecast from weather table


                mContext.getContentResolver().delete(
                        WeatherContract.WeatherEntry.CONTENT_URI,
                        WeatherContract.WeatherEntry.COLUMN_CITY_ID + "=?",
                        stringPosition);

                mContext.getContentResolver().delete(
                        CurrentWeatherContract.CurrentWeatherEntry.CONTENT_URI,
                        CurrentWeatherContract.CurrentWeatherEntry.COLUMN_CITY_ID + "=?",
                        stringPosition);

                mContext.getContentResolver().delete(
                        HourlyWeatherContract.HourlyWeatherEntry.CONTENT_URI,
                        HourlyWeatherContract.HourlyWeatherEntry.COLUMN_CITY_ID + "=?",
                        stringPosition);



            }
        });
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }
}
