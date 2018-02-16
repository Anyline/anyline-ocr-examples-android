package io.anyline.examples.settings;

import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import io.anyline.examples.R;



public class SettingsFragment extends PreferenceFragmentCompat
{

    private final String TAG = SettingsFragment.class.getSimpleName();
    private AlertDialog.Builder builder = null;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preferences, s);


        //RESET
        Preference resetData = findPreference(getResources().getString(R.string.pref_reset_key));
        Log.d(TAG,""+resetData.getTitle());

        if(builder == null) {
            builder = new AlertDialog.Builder(getActivity(), R.style.ResetDialogStyle);
        }
        resetData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.d(TAG,preference.getTitle()+ " clicked");
                builder.show();
                return true;
            }
        });

        //VERSION
        Preference version = findPreference(getResources().getString(R.string.pref_version_key));
        version.setSummary(((SettingsActivity)getActivity()).getVersionInfo());

    }


}
