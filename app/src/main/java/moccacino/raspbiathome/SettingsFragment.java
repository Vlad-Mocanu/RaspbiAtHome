package moccacino.raspbiathome;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences_fragment from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}
