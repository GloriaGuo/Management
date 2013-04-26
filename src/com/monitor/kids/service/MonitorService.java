package com.monitor.kids.service;

import java.util.HashMap;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.monitor.kids.KidsApplication;
import com.monitor.kids.R;
import com.monitor.kids.monitor.AppsInstalledMonitor;
import com.monitor.kids.monitor.AppsUsedMonitor;
import com.monitor.kids.monitor.BrowserBookmarkMonitor;
import com.monitor.kids.monitor.BrowserHistoryMonitor;
import com.monitor.kids.monitor.CallLogMonitor;
import com.monitor.kids.monitor.ContactsMonitor;
import com.monitor.kids.monitor.GpsMonitor;
import com.monitor.kids.monitor.Monitor;
import com.monitor.kids.monitor.Monitor.Type;
import com.monitor.kids.task.AllUploadTask;

public class MonitorService extends Service {
	
	private BrowserHistoryMonitor mBrowserHistoryMonitor;
    private BrowserBookmarkMonitor mBrowserBookmarkMonitor;
	private ContactsMonitor mContactsMonitor;
	private CallLogMonitor mCallLogMonitor;
	private GpsMonitor mGpsMonitor;
    private AppsInstalledMonitor mAppsInstalledMonitor;
    private AppsUsedMonitor mAppsUsedMonitor;

	@Override
	public IBinder onBind(Intent intent) {
	    return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		if (null == KidsApplication.commonMonitorList) {
		    KidsApplication.commonMonitorList = new HashMap<Type, Monitor>();
		}
		if (null == KidsApplication.specialMonitorList) {
            KidsApplication.specialMonitorList = new HashMap<Type, Monitor>();
        }
		
		if (this.getResources().getBoolean(R.attr.monitor_browser_history) &&
		        mBrowserHistoryMonitor == null) {
		    mBrowserHistoryMonitor = new BrowserHistoryMonitor(this.getApplicationContext());
		}
        if (this.getResources().getBoolean(R.attr.monitor_browser_bookmark) &&
                mBrowserBookmarkMonitor == null) {
            mBrowserBookmarkMonitor = new BrowserBookmarkMonitor(this.getApplicationContext());
        }
		if (this.getResources().getBoolean(R.attr.monitor_contacts) &&
		        mContactsMonitor == null) {
		    mContactsMonitor = new ContactsMonitor(this.getApplicationContext());
		} 
		if (this.getResources().getBoolean(R.attr.monitor_call_log) &&
                mCallLogMonitor == null) {
		    mCallLogMonitor = new CallLogMonitor(this.getApplicationContext());
		}
        if (this.getResources().getBoolean(R.attr.monitor_gps_info) &&
                mGpsMonitor == null) {
            mGpsMonitor = new GpsMonitor(this.getApplicationContext());
        }
        if (this.getResources().getBoolean(R.attr.monitor_apps_installed) &&
                mAppsInstalledMonitor == null) {
            mAppsInstalledMonitor = new AppsInstalledMonitor(this.getApplicationContext());
        }
        if (this.getResources().getBoolean(R.attr.monitor_apps_used) &&
                mAppsUsedMonitor == null) {
            mAppsUsedMonitor = new AppsUsedMonitor(this.getApplicationContext());
        }
	    Log.d("MonitorService", "----> service created");
    }
	
	@Override
	public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2)
	{
		Log.d("MonitorService", "----> service started");
		if (mAppsInstalledMonitor != null && !mAppsInstalledMonitor.isMonitorRunning()) {
            mAppsInstalledMonitor.startMonitoring();
            KidsApplication.specialMonitorList.put(Type.APP_INSTALLED, mAppsInstalledMonitor);
        }
		if (mBrowserBookmarkMonitor != null && !mBrowserBookmarkMonitor.isMonitorRunning()) {
            mBrowserBookmarkMonitor.startMonitoring();
            KidsApplication.specialMonitorList.put(Type.BROWSER_BOOKMARK, mBrowserBookmarkMonitor);
        }
        if (mContactsMonitor != null && !mContactsMonitor.isMonitorRunning()) {
            mContactsMonitor.startMonitoring();
            KidsApplication.specialMonitorList.put(Type.CONTACTS, mContactsMonitor);
        }
        if (mBrowserHistoryMonitor != null && !mBrowserHistoryMonitor.isMonitorRunning()) {
			mBrowserHistoryMonitor.startMonitoring();
			KidsApplication.commonMonitorList.put(Type.BROWSER_HISTORY, mBrowserHistoryMonitor);
		}
        if (mCallLogMonitor != null && !mCallLogMonitor.isMonitorRunning()) {
			mCallLogMonitor.startMonitoring();
			KidsApplication.commonMonitorList.put(Type.CALL_LOG, mCallLogMonitor);
		}
        if (mAppsUsedMonitor != null && !mAppsUsedMonitor.isMonitorRunning()) {
            mAppsUsedMonitor.startMonitoring();
            KidsApplication.commonMonitorList.put(Type.APP_USED, mAppsUsedMonitor);
            KidsApplication.setAppUsedMonitorAlarm();
        }
        if (mGpsMonitor != null && !mGpsMonitor.isMonitorRunning()) {
            mGpsMonitor.startMonitoring();
            KidsApplication.commonMonitorList.put(Type.GPS_INFO, mGpsMonitor);
            KidsApplication.setGpsMonitorAlarm();
        }
        
        // start upload task
        AllUploadTask task = new AllUploadTask();
        task.create();
        task.start();
		
		return START_STICKY;
	}
	
	@Override
    public void onDestroy() {
	    Log.e("MonitorService", "It shouldn't exit...");
	    if (mBrowserHistoryMonitor != null && mBrowserHistoryMonitor.isMonitorRunning()) {
			mBrowserHistoryMonitor.stopMonitoring();
		}
		if (mContactsMonitor != null && mContactsMonitor.isMonitorRunning()) {
			mContactsMonitor.stopMonitoring();
		}
		if (mCallLogMonitor != null && mCallLogMonitor.isMonitorRunning()) {
			mCallLogMonitor.stopMonitoring();
		}
        if (mGpsMonitor != null && mGpsMonitor.isMonitorRunning()) {
            mGpsMonitor.stopMonitoring();
        }
        if (mAppsInstalledMonitor != null && mAppsInstalledMonitor.isMonitorRunning()) {
            mAppsInstalledMonitor.stopMonitoring();
        }
        if (mAppsUsedMonitor != null && mAppsUsedMonitor.isMonitorRunning()) {
            mAppsUsedMonitor.stopMonitoring();
        }
        if (KidsApplication.commonMonitorList != null) {
    		KidsApplication.commonMonitorList.clear();
            KidsApplication.commonMonitorList = null;
        }
        if (KidsApplication.specialMonitorList != null) {
            KidsApplication.specialMonitorList.clear();
            KidsApplication.specialMonitorList = null;
        }
	}
	
}
