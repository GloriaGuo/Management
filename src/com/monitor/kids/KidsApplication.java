/**
 * Monitor For Kids
 *
 * Created by Gloria
 */

package com.monitor.kids;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.monitor.kids.monitor.Monitor;
import com.monitor.kids.monitor.Monitor.Type;
import com.monitor.kids.receiver.AppUsedMonitorReceiver;
import com.monitor.kids.receiver.CommonUploadReceiver;
import com.monitor.kids.receiver.GpsMonitorReceiver;
import com.monitor.kids.receiver.SpecialUploadReceiver;

public class KidsApplication extends android.app.Application {
	
	/**
     * Log tag for this application.
     */
    protected static String mApplicationTag = "PM";
    private static String mInternalPath = null;
    
    public static final boolean DEBUG = true;
    
    public static HashMap<Type, Monitor> commonMonitorList = null;
    public static HashMap<Type, Monitor> specialMonitorList = null;
    
    public static String KIDS_RECEIVER_FILTER_ACTIONS_COMMON = "common_upload";
    public static String KIDS_RECEIVER_FILTER_ACTIONS_SPECIAL = "special_upload";
	
    /**
     * The application context
     */
    protected static Context mContext = null;
    /**
     * Application configuration
     */
    private static KidsConfiguration mConfiguration = null;
    
    private static PendingIntent mCommonPendingIntent = null;
    private static PendingIntent mSpecialPendingIntent = null;


    /**
     * @return the application tag (used in application logs)
     */
    public static String getApplicationTag() {
        return mApplicationTag;
    }
    
    /**
     * Gets the configuration
     * @return the current configuration
     */
    public static KidsConfiguration getConfiguration() {
        return mConfiguration;
    }
    
    /**
     * Gets the PendingIntent
     * @return the current PendingIntent
     */
    public static PendingIntent getPendingIntent(String action) {
        if (action.equals(KIDS_RECEIVER_FILTER_ACTIONS_COMMON)) {
            return mCommonPendingIntent;
        } else {
            return mSpecialPendingIntent;
        }
    }
    
    public static Context getContext() {
        return mContext;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        mContext = this;
        
        // Clean installed apk file automatically
        final File apk = new File(Environment.getExternalStorageDirectory() + "/Download/kids.apk");
        if (apk.exists()) {
            // Found update apk in storage, delete it
            Log.i(mApplicationTag, "Cleaning existing update file " 
                + apk.getAbsolutePath());
            apk.delete();
        } 
        
        // Configuration
        mConfiguration = new KidsConfiguration(mContext);
        mInternalPath = mContext.getDir(".kids",Context.MODE_PRIVATE).getAbsolutePath();

        mConfiguration.registerPreferenceChangeListener(this.mSettingsListener);
    }

    /**
     * Gets the internal storage path
     * @return the internal storage path
     */
    public static String getInternalPath() {
        return mInternalPath;
    }
    
    /**
     * Gets the IMEI
     */
    public static String getIMEI() {
        TelephonyManager tm = (TelephonyManager)mContext.getSystemService(TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }
    
    /**
     * Gets the MAC when there's no available IMEI
     */
    public static String getMAC() {
        WifiManager wifi = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);  
        WifiInfo info = wifi.getConnectionInfo();  
        return info.getMacAddress();
    }
    
    /**
     * Gets the IMEI
     */
    public static String getIMSI() {
        TelephonyManager tm = (TelephonyManager)mContext.getSystemService(TELEPHONY_SERVICE);
        return tm.getSubscriberId();
    }
    
    public static String getVersion() {
        String version = "";
        try {
            final PackageInfo info = mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), 0);
            
            version = info.versionName;
        } catch (NameNotFoundException e) {
            Log.e(mApplicationTag, "Cannot get the app version: " + e.getMessage());
        }
        
        return version;
    }
    
    public static void setAppUsedMonitorAlarm() {
        AlarmManager mAlarmManager = (AlarmManager)KidsApplication.getContext().
                getSystemService("alarm");
        
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(
                mContext, 0, new Intent(mContext, AppUsedMonitorReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
        
        mAlarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP, 
                5000L + SystemClock.elapsedRealtime(), 
                1000,
                mPendingIntent);
    }
    
    public static boolean isServiceRunning(String paramString, Context context)
    {
        Iterator<RunningServiceInfo> mIterator = ((ActivityManager)context.
                getSystemService(Context.ACTIVITY_SERVICE)).getRunningServices(5000).iterator();
      
        while (mIterator.hasNext()) {
            RunningServiceInfo si = (RunningServiceInfo) mIterator.next();
            if (si.service.getClassName().equals(paramString)) {
                return true;
            }
        }
        return false;
    }

    public static void setGpsMonitorAlarm() {
        AlarmManager mAlarmManager = (AlarmManager)KidsApplication.getContext().
                getSystemService("alarm");
        
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(
                mContext, 0, new Intent(mContext, GpsMonitorReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
        
        mAlarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP, 
                10000L + SystemClock.elapsedRealtime(), 
                5000,
                mPendingIntent);
    }
    
    private final OnSharedPreferenceChangeListener mSettingsListener = 
            new OnSharedPreferenceChangeListener() {
                /* (non-Javadoc)
                 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
                 */
                @Override
                public void onSharedPreferenceChanged(
                        final SharedPreferences sharedPreferences, final String key) {
                    KidsApplication.getConfiguration();
                    if (key.equals(KidsConfiguration.PREFERENCE_KEY_COMMON_INTERVAL_TIME)) {
                        Log.d(mApplicationTag, "----> common interval time changed.");
                        setUploadCommonAlarm();
                    }
                    if (key.equals(KidsConfiguration.PREFERENCE_KEY_SPECIAL_INTERVAL_TIME)) {
                        Log.d(mApplicationTag, "----> special interval time changed.");
                        setUploadSpecialAlarm();
                    }
                }
            };

    private void setUploadCommonAlarm() {
        AlarmManager mAlarmManager = (AlarmManager)KidsApplication.getContext().
                getSystemService("alarm");
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(
                mContext, 0, new Intent(mContext, CommonUploadReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
        
        mAlarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP, 
                KidsApplication.getConfiguration().getCommonIntervalTime() + 
                        SystemClock.elapsedRealtime(), 
                KidsApplication.getConfiguration().getCommonIntervalTime(),
                mPendingIntent);
    }
    
    private void setUploadSpecialAlarm() {
        AlarmManager mAlarmManager = (AlarmManager)KidsApplication.getContext().
                getSystemService("alarm");
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(
                mContext, 0, new Intent(mContext, SpecialUploadReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
        
        mAlarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP, 
                KidsApplication.getConfiguration().getSpecialIntervalTime() + 
                        SystemClock.elapsedRealtime(), 
                KidsApplication.getConfiguration().getSpecialIntervalTime(),
                mPendingIntent);
    }
}
