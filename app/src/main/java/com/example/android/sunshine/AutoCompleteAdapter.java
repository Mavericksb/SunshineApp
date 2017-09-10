package com.example.android.sunshine;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

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

class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    private static final String LOG_TAG = "Autocomplete Adapter";

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;


    private AutocompleteFilter mAutoCompleteFilter = new AutocompleteFilter.Builder()
//            .setCountry("it")
            .setTypeFilter(TYPE_FILTER_CITIES)
            .build();


    private ArrayList<String> resultList;
    static private ArrayList<String> placeIds;
    private Context mContext;


    public AutoCompleteAdapter(Context context, int textViewResId) {
        super(context,textViewResId);
        mContext = context;

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(context, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(context, null);



    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String pos = getItem(position);
        View view = convertView;
        if(view == null){
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            view = layoutInflater.inflate(R.layout.list_item_location_autocomplete, null);
        }
        TextView tv = (TextView) view.findViewById(R.id.textViewListLocation);
        tv.setText(pos.toString());

        return view;
    }


    @Override
    public int getCount() {
        return resultList.size();
    }

    @Nullable
    @Override
    public String getItem(int index) {
        return resultList.get(index); //completeArray.get(index).getPlaceId().toString();
    }

    static public String getPlaceId(int index) {
        return placeIds.get(index);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint != null){

                    resultList = autoComplete(constraint.toString());


                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if(filterResults != null && filterResults.count > 0){
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    synchronized public ArrayList<String> autoComplete(CharSequence constraint){

        final ArrayList<String> results = new ArrayList<>();
        final ArrayList<String> placeId = new ArrayList<>();

        try {
            Task<AutocompletePredictionBufferResponse> result = mGeoDataClient.getAutocompletePredictions(
                    constraint.toString(),
                    null,
                    mAutoCompleteFilter);
            result.addOnCompleteListener(new OnCompleteListener<AutocompletePredictionBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<AutocompletePredictionBufferResponse> task) {
                    if (task.isSuccessful()) {
                        AutocompletePredictionBufferResponse autocompletePredictions = task.getResult();
                        for (AutocompletePrediction placeLikelihood : autocompletePredictions) {

                            String city = placeLikelihood.getPrimaryText(null).toString() + " " + placeLikelihood.getSecondaryText(null).toString();
                            results.add(city);
                            placeId.add(placeLikelihood.getPlaceId());
                        }
                        notifyDataSetChanged();
                        autocompletePredictions.release();
                    } else{
                        Exception exception = task.getException();
                        Log.e("OnCompleteAutocomplete", "Exception " + exception);

                    }
                }
            });
            // Block on a task and get the result synchronously.
            Tasks.await(result, 30, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            Log.e(LOG_TAG, "AutoComplete request failed: " + e);
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "AutoComplete request interrupted: " + e);
        } catch (TimeoutException e){
            Log.e(LOG_TAG, "AutoComplete request interrupted: " + e);
        }


        placeIds = placeId;
        return results;
    }


}
