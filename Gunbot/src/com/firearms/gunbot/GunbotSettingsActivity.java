package com.firearms.gunbot;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class GunbotSettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}