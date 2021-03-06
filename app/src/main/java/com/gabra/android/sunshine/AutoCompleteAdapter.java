package com.gabra.android.sunshine;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.android.gms.location.places.AutocompleteFilter.TYPE_FILTER_CITIES;

/**
 * Created by ROBERTO on 08/09/2017.
 */

class AutoCompleteAdapter extends ArrayAdapter<AutoCompleteAdapter.PlaceAutocomplete> implements Filterable {

    private static final String LOG_TAG = "Autocomplete Adapter";

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private ArrayList<PlaceAutocomplete> mResultList;


    private AutocompleteFilter mAutoCompleteFilter = new AutocompleteFilter.Builder()
//            .setCountry("it")
            .setTypeFilter(TYPE_FILTER_CITIES)
            .build();


    //static private ArrayList<String> placeIds;
    private Context mContext;


    public AutoCompleteAdapter(Context context, int layoutResId, int textViewResId) {
        super(context,layoutResId, textViewResId);

        mContext = context;

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(context, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(context, null);



    }

    @Override
    public int getCount() {
        return mResultList.size();
    }

    @Nullable
    @Override
    public PlaceAutocomplete getItem(int index) {
        return mResultList.get(index);
    }


    @Override
    public String toString() {
        return super.toString();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint != null){

                    mResultList = autoComplete(constraint.toString());
                    if(mResultList!=null) {
                        filterResults.values = mResultList;
                        filterResults.count = mResultList.size();
                    }
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

    private ArrayList<PlaceAutocomplete> autoComplete(CharSequence constraint){

        try {
            Task<AutocompletePredictionBufferResponse> result = mGeoDataClient.getAutocompletePredictions(
                    constraint.toString(),
                    null,
                    mAutoCompleteFilter);
            // Wait for predictions, set the timeout.
            AutocompletePredictionBufferResponse autocompletePredictions = Tasks.await(result, 60, TimeUnit.SECONDS);

            if(result.isSuccessful()){
                Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
                ArrayList resultList = new ArrayList<>(autocompletePredictions.getCount());
                while (iterator.hasNext()) {
                    AutocompletePrediction prediction = iterator.next();
                    resultList.add(new PlaceAutocomplete(prediction.getPlaceId(),
                            prediction.getPrimaryText(null)+" "+prediction.getSecondaryText(null), prediction.getPrimaryText(null)));
                }
                // Buffer release
                autocompletePredictions.release();
                return resultList;
            } else {
                Exception exception = result.getException();
                Log.e("OnCompleteAutocomplete", "Exception " + exception);
                autocompletePredictions.release();
                return null;
            }
        } catch (ExecutionException e) {
            Log.e(LOG_TAG, "AutoComplete request failed: " + e);
            return null;
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "AutoComplete request interrupted: " + e);
            return null;
        } catch (TimeoutException e){
            Log.e(LOG_TAG, "AutoComplete request interrupted: " + e);
            return null;
        }
    }


    class PlaceAutocomplete {

        public CharSequence placeId;
        public CharSequence description;
        public CharSequence city;

        PlaceAutocomplete(CharSequence placeId, CharSequence description, CharSequence city) {
            this.placeId = placeId;
            this.description = description;
            this.city = city;
        }

        @Override
        public String toString() {
            return description.toString();
        }
    }

}
