package com.example.android.sunshine;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class LocationActivity extends AppCompatActivity {

    private ArrayAdapter<String> mLocationAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        ListView listView = (ListView) findViewById(R.id.PrefLocationlist);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ArrayList<String> locations = new ArrayList<>();

        locations.add("Actual Location");
        locations.add("Milano");
        locations.add("Torino");

        mLocationAdapter = new ArrayAdapter<String>(this, R.layout.list_item_pref_location, R.id.textViewPrefLocation, locations);
        listView.setAdapter(mLocationAdapter);


    }
}
