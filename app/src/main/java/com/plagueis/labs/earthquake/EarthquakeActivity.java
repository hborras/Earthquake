package com.plagueis.labs.earthquake;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by hector on 4/04/15.
 */
public class EarthquakeActivity extends ActionBarActivity {

    static final private int MENU_PREFERENCES = Menu.FIRST + 1;
    static final private int MENU_UPDATE = Menu.FIRST + 2;

    private static final int SHOW_PREFERENCES = 1;

    public int minimumMagnitude = 0;
    public boolean autoUpdateCheched = false;
    public int updateFreq = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        updateFromPreferences();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_PREFERENCES, Menu.NONE, R.string.menu_preferences);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case (MENU_PREFERENCES): {
                Intent i = new Intent(this,PreferencesActivity.class);
                startActivityForResult(i, SHOW_PREFERENCES);
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SHOW_PREFERENCES){
            if(resultCode == ActionBarActivity.RESULT_OK){
                updateFromPreferences();
                FragmentManager fm = getFragmentManager();
                final EarthquakeListFragment earthquakeList =
                        (EarthquakeListFragment) fm.findFragmentById(R.id.EarthquakeListFragment);

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        earthquakeList.refreshEarthquakes();
                    }
                });
                t.start();
            }
        }
    }

    private void updateFromPreferences(){
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        int minMagIndex = prefs.getInt(PreferencesActivity.PREF_MIN_MAG_INDEX, 0);
        if (minMagIndex < 0){
            minMagIndex = 0;
        }

        int freqIndex = prefs.getInt(PreferencesActivity.PREF_UPDATE_FREQ_INDEX, 0);
        if (freqIndex < 0){
            freqIndex = 0;
        }

        autoUpdateCheched = prefs.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE, false);

        Resources r = getResources();
        // Get the option values from the arrays
        String[] minMagValues = r.getStringArray(R.array.magnitude);
        String[] freqValues = r.getStringArray(R.array.update_freq_values);

        // Convert the values to ints.
        minimumMagnitude = Integer.valueOf(minMagValues[minMagIndex]);
        updateFreq = Integer.valueOf(freqValues[freqIndex]);
    }
}
