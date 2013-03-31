/**
 * Parent Management
 *
 * Created by Gloria
 */

package com.parent.management;

import java.io.File;
import java.util.HashMap;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.parent.management.monitor.Monitor;
import com.parent.management.monitor.Monitor.Type;
import com.parent.management.receiver.ManagementReceiver;

public class ManagementApplication extends android.app.Application {
	
	/**
     * Log tag for this application.
     */
    protected static String mApplicationTag = "PM";
    private static String mInternalPath = null;
    
    public static final boolean DEBUG = true;
    
    public static HashMap<Type, Monitor> monitorList = null;
	
    /**
     * The application context
     */
    protected static Context mContext = null;
    /**
     * Application configuration
     */
    private static ManagementConfiguration mConfiguration = null;
    
    private static PendingIntent mPendingIntent = null;
    
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
    public static ManagementConfiguration getConfiguration() {
        return mConfiguration;
    }
    
    /**
     * Gets the PendingIntent
     * @return the current PendingIntent
     */
    public static PendingIntent getPendingIntent() {
        return mPendingIntent;
    }
    
    public static Context getContext() {
        return mContext;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        mContext = this;
        
        // Clean installed apk file automatically
        final File apk = new File(Environment.getExternalStorageDirectory() + "/Download/update.apk");
        if (apk.exists()) {
            // Found update apk in storage, delete it
            Log.i(mApplicationTag, "Cleaning existing update file " 
                + apk.getAbsolutePath());
            apk.delete();
        } 
        
        // Configuration
        mConfiguration = new ManagementConfiguration(mContext);
        mInternalPath = mContext.getDir(".management",Context.MODE_PRIVATE).getAbsolutePath();

        mPendingIntent = PendingIntent.getBroadcast(
                mContext, 0, new Intent(mContext, ManagementReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
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
}
