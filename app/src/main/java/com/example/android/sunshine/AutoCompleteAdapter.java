package com.example.android.sunshine;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

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

/**
 * Created by ROBERTO on 07/09/2017.
 */

class AutoCompleteAdapter extends ArrayAdapter implements Filterable, AdapterView.OnItemClickListener {

    private static final String LOG_TAG = "Autocomplete Adapter";
    private static final String URL_PLACE_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyBahRT-ypkg3p658o_fVkshRlDGpG3_al8";

    private ArrayList<String> resultList;
    private Context mContext;


    public interface LocationAdapterOnClickHandler{
        void onClickItem();
    }


    public AutoCompleteAdapter(Context context, int textViewResId) {
        super(context,textViewResId);

        mContext = context;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String string = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(mContext, string, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Nullable
    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint != null){
                    // Retrieve the autocomplete results.
                    resultList = autoComplete(constraint.toString());

                    // Assign the data to the FilterResults
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

    public ArrayList autoComplete(String input){

        ArrayList resultList = null;
        HttpURLConnection connection = null;

        StringBuilder jsonResult = new StringBuilder();
        try
        {
            StringBuilder sb = new StringBuilder(URL_PLACE_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:it");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            connection = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(connection.getInputStream());
            int read;
            char[] buff = new char[1024];
            while((read = in.read(buff)) != -1){
                jsonResult.append(buff, 0, read);
            }} catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        try {

            JSONObject jsonObj = new JSONObject(jsonResult.toString());
            JSONArray jsonArray = jsonObj.getJSONArray("predictions");
            resultList = new ArrayList(jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                resultList.add(jsonArray.getJSONObject(i).getString("description"));
            }

            }catch(JSONException e) {
            Log.e(LOG_TAG, "Error reading Json Response", e);
        }
        return resultList;

    }
}
