package com.parent.management;

import android.content.Context;

public class ManagementConfiguration {
	
    private final Context mContext;
    
    static public boolean isMonitorBrowserHistory;
    static public boolean isMonitorGpsInfo;
    static public boolean isMonitorInstalledApps;
    static public boolean isMonitorAppsUsedInfo;
    static public boolean isMonitorContacts;
    static public boolean isMonitorCallLog;
    
	/**
     * Creates a new Configuration instance 
     * @param appContext application context
     */
    ManagementConfiguration(final Context appContext) {
        this.mContext = appContext;
    }
    
	/**
     * Load configuration parameter 
     */
    void init() {
    	isMonitorBrowserHistory = mContext.getResources().getBoolean(R.attr.monitor_browser_history);
    	isMonitorGpsInfo = mContext.getResources().getBoolean(R.attr.monitor_gps_info);
    	isMonitorInstalledApps = mContext.getResources().getBoolean(R.attr.monitor_installed_apps);
    	isMonitorAppsUsedInfo = mContext.getResources().getBoolean(R.attr.monitor_apps_used_info);
    	isMonitorContacts = mContext.getResources().getBoolean(R.attr.monitor_contacts);
    	isMonitorCallLog = mContext.getResources().getBoolean(R.attr.monitor_call_log);
    }

}
