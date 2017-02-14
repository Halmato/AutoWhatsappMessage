package com.twinc.halmato.autowhatsappmessage;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.twinc.halmato.autowhatsappmessage.Notifications.NotificationScheduler;

/**
 * Created by Tiaan on 12/8/2016.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_fragment);

        // set texts correctly
        onSharedPreferenceChanged(null, "");

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        String notificationActiveKey = getResources().getString(R.string.notifications_settings_key);
        String selectedApplicationKey = getResources().getString(R.string.selected_application_key);

        if(key.equals(notificationActiveKey)) {    // This means the notificationActive was changed

            if(!sharedPreferences.getBoolean(key,true)) {

                NotificationScheduler.removeAllScheduledNotifications(getActivity());
            }

        } else if(key.equals(selectedApplicationKey)) {

            // Change default application
            MainActivity.setSelectedApplication(sharedPreferences.getString(selectedApplicationKey,""));

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getView().setBackgroundColor(Color.WHITE);
        getView().setClickable(true);
    }



}
