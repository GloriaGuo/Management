/**
 * Parent Management
 *
 * Created by Gloria
 */

package com.parent.management;

import java.io.File;
import java.util.List;

import com.parent.management.monitor.BrowserHistoryMonitor;
import com.parent.management.monitor.Monitor;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class ManagementApplication extends android.app.Application {
	
	/**
     * Log tag for this application.
     */
    protected static String mApplicationTag = "PM";
    private static String mInternalPath = null;
    
    public static final boolean DEBUG = true;
    
    public static final List<Monitor> monitorList = null;
	
    /**
     * The application context
     */
    protected static Context mContext = null;
    /**
     * Application configuration
     */
    private static ManagementConfiguration mConfiguration = null;
    
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

        BrowserHistoryMonitor tmp_monitor = new BrowserHistoryMonitor(mContext);
        tmp_monitor.startMonitoring();
    }
    
    /**
     * Gets the internal storage path
     * @return the internal storage path
     */
    public static String getInternalPath() {
        return mInternalPath;
    }
}
