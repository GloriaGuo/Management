package com.parent.management;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class ManagementConfiguration {
	
    private final Context mContext;
    
    private final SharedPreferences mSharedPreferences;
    
    private static final String SHARED_PREFS_NAME = "management.preferences";
    
    public static final String PREFERENCE_KEY_IS_REGISTED = "is_registed";
    public static final String PREFERENCE_KEY_COMMON_INTERVAL_TIME = "common_interval";
    public static final String PREFERENCE_KEY_SPECIAL_INTERVAL_TIME = "special_interval";
    
    /**
     * Creates a new Configuration instance 
     * @param appContext application context
     */
    ManagementConfiguration(final Context appContext) {
        this.mContext = appContext;
        mSharedPreferences = mContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);  
    }
    
    public boolean getIsRegisted() {
        return this.mSharedPreferences.getBoolean(PREFERENCE_KEY_IS_REGISTED, false);
    }
    
    public void setIsRegisted(boolean result) {
        mSharedPreferences.edit().putBoolean(
                PREFERENCE_KEY_IS_REGISTED, result).commit();
    }
    
	public int getCommonIntervalTime() {
        return this.mSharedPreferences.getInt(
                PREFERENCE_KEY_COMMON_INTERVAL_TIME, 0);
    }
    
    public void setCommonIntervalTime(int time) {
        mSharedPreferences.edit().putInt(
                PREFERENCE_KEY_COMMON_INTERVAL_TIME, time).commit();
    }
    
    public int getSpecialIntervalTime() {
        return this.mSharedPreferences.getInt(
                PREFERENCE_KEY_SPECIAL_INTERVAL_TIME, 0);
    }
    
    public void setSpecialIntervalTime(int time) {
        mSharedPreferences.edit().putInt(
                PREFERENCE_KEY_SPECIAL_INTERVAL_TIME, time).commit();
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
