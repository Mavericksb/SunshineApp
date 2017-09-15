package com.example.android.sunshine;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.data.LocationsContract;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.sync.SunshineSyncUtils;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.android.gms.location.places.AutocompleteFilter.TYPE_FILTER_CITIES;

/**
 * Created by ROBERTO on 08/09/2017.
 */

class LocationAdapter extends CursorAdapter implements View.OnClickListener{

    private static final String LOG_TAG = "Location Adapter";


    private Cursor mCursor;

    //static private ArrayList<String> placeIds;
    private Context mContext;

    private final LocationAdapterOnClickHandler mClickHandler;


    public interface LocationAdapterOnClickHandler{
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
            double latitude = Double.valueOf(cursor.getString(LocationActivity.INDEX_CITY_LATITUDE));
            double longitude = Double.valueOf(cursor.getString(LocationActivity.INDEX_CITY_LONGITUDE));
        SunshinePreferences.setLocationDetails(mContext, latitude, longitude);
        SunshineSyncUtils.startImmediateSync(mContext);
        ((Activity)mContext).finish();
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        TextView textViewPrefLocation = (TextView) view.findViewById(R.id.textViewPrefLocation);
        ImageButton deleteCity = (ImageButton)view.findViewById(R.id.button_delete_city);

        final int position = cursor.getInt(cursor.getColumnIndex(LocationsContract.LocationsEntry._ID));


        deleteCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = ContentUris.withAppendedId(LocationsContract.LocationsEntry.CONTENT_URI, Long.valueOf(position));
                mContext.getContentResolver().delete(uri, null, null);
            }
        });

        String cityName = cursor.getString(LocationActivity.INDEX_CITY_NAME);
        textViewPrefLocation.setText(cityName);

        //pass cursor position via tag to be used in onClick;
        view.setTag(cursor.getPosition());
        view.setOnClickListener(this);
    }


}
