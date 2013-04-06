package com.parent.management;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class ManagementConfiguration {
	
    private final Context mContext;
    
    private final SharedPreferences mSharedPreferences;
    
    private static final String SHARED_PREFS_NAME = "management.preferences";
    
    public static final String PREFERENCE_KEY_INTERVAL_TIME = "interval_time";
    
    /**
     * Creates a new Configuration instance 
     * @param appContext application context
     */
    ManagementConfiguration(final Context appContext) {
        this.mContext = appContext;
        mSharedPreferences = mContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);  
    }
    
	public int getIntervalTime() {
        return this.mSharedPreferences.getInt(
                PREFERENCE_KEY_INTERVAL_TIME, 
                mContext.getResources().getInteger(R.attr.default_interval_time));
    }
    
    public void setIntervalTime(int time) {
        mSharedPreferences.edit().putInt(
                PREFERENCE_KEY_INTERVAL_TIME, time).commit();
    }
    
    /**
     * Registers a new listener, whose callback will be triggered each time the
     * internal shared preferences are modified
     * @param listener to be registered
     */
    public void registerPreferenceChangeListener(
        final OnSharedPreferenceChangeListener listener) {
        this.mSharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

}
