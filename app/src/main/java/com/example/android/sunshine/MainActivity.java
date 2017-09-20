package com.example.android.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

/**
 * Created by Robert on 19/09/2017.
 */



public class MainActivity extends AppCompatActivity {

    private static ImageAnimator mImageAnimator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        ImageView background = (ImageView) findViewById(R.id.cloudView);
        ImageView foreground = (ImageView) findViewById(R.id.cloudView2);

        mImageAnimator = new ImageAnimator(this, background, foreground);
        mImageAnimator.playAnimation();

        ForecastFragment forecastFragment = new ForecastFragment();

        forecastFragment.setAllowEnterTransitionOverlap(false);
        forecastFragment.setAllowReturnTransitionOverlap(false);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .addToBackStack(null).replace(R.id.fragment_container,forecastFragment ).commit();
    }

    /**
     * This is where we inflate and set up the menu for this Activity.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     *
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

    /**
     * Callback invoked when a menu item was selected from this Activity's menu.
     *
     * @param item The menu item that was selected by the user
     *
     * @return true if you handle the menu click here, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_location) {
            startActivity(new Intent(this, LocationActivity.class));
            //openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onBackPressed() {
//
//        int count = getFragmentManager().getBackStackEntryCount();
//
//        if (count == 0) {
//            super.onBackPressed();
//            //additional code
//        } else {
//            FragmentManager fm = getSupportFragmentManager();
//
//            Fragment previous = (Fragment) fm.getBackStackEntryAt(count-1);
//            if(previous.isHidden()){
//                FragmentTransaction ft = fm.beginTransaction();
//                ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right).show(previous).commit();
//            }
//            getFragmentManager().popBackStack();
//        }
//    }
}
