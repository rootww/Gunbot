package com.firearms.gunbot;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class GunbotSettingsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {
	GunbotDatabase m_database;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_database = new GunbotDatabase(getApplicationContext());
        
        addPreferencesFromResource(R.xml.settings);
        
        findPreference("cache_reset").setOnPreferenceClickListener(this);
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	m_database.close();
    }

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals("cache_reset")){
			showClearCacheDialog();
		}
		return false;
	}
	
	private void showClearCacheDialog(){
		new AlertDialog.Builder(this)
		.setTitle("Confirm Clear")
		.setMessage("Do you really want to clear the product cache?")
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				m_database.clearProductCache();
			}
		})
		.setNegativeButton(android.R.string.no, null)
		.show();
	}
}