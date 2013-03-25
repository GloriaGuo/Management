package com.parent.management;

import android.content.Context;
import android.content.SharedPreferences;

public class ManagementConfiguration {
	
    private final Context mContext;
    
    private final SharedPreferences mSharedPreferences;
    
    private static final String SHARED_PREFS_NAME = "management.preferences";
    
    private static final String PREFERENCE_KEY_INTERVAL_TIME = "interval_time";
    
    /**
     * Creates a new Configuration instance 
     * @param appContext application context
     */
    ManagementConfiguration(final Context appContext) {
        this.mContext = appContext;
        mSharedPreferences = mContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);  
    }
    
	public int getIntervalTime() {
        return this.mSharedPreferences.getInt(PREFERENCE_KEY_INTERVAL_TIME, 1800000);
    }
    
    public void setIntervalTime(int time) {
        mSharedPreferences.edit().putInt(
                PREFERENCE_KEY_INTERVAL_TIME, time).commit();
    }

}
