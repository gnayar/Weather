package com.example.weather;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	protected Method mLoadHeaders = null;
	protected Method mHasHeaders = null;
	public static final String tempScale = "temp_scale";

	/**
	 * Checks to see if using new v11+ way of handling PrefFragments.
	 * 
	 * @return Returns false pre-v11, else checks to see if using headers.
	 */
	public boolean isNewV11Prefs() {
		if (mHasHeaders != null && mLoadHeaders != null) {
			try {
				return (Boolean) mHasHeaders.invoke(this);
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}
		return false;
	}

	@Override
	public void onCreate(Bundle aSavedState) {
		// onBuildHeaders() will be called during super.onCreate()
		try {
			mLoadHeaders = getClass().getMethod("loadHeadersFromResource",
					int.class, List.class);
			mHasHeaders = getClass().getMethod("hasHeaders");
		} catch (NoSuchMethodException e) {
		}
		super.onCreate(aSavedState);
		final Context context = this;
		if (!isNewV11Prefs()) {
			addPreferencesFromResource(R.xml.preferences);
		}
		final Preference pref = (Preference) findPreference("temp_scale");        
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

		    public boolean onPreferenceClick(Preference preference) {
		    	Intent i= new Intent (context,MainActivity.class);
				startActivity(i);
		        return true; 
		    }
		});
	}

	@Override
	public void onBuildHeaders(List<Header> aTarget) {

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		if (key.equals(tempScale)) {
			Preference connectionPref = findPreference(key);
			   Intent i= new Intent (this,MainActivity.class);
			   startActivity(i);
		}
	}
}