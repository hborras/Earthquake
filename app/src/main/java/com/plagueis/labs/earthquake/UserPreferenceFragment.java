package com.plagueis.labs.earthquake;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by hector on 5/04/15.
 */
public class UserPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.userpreferences);
    }


}
