package com.parent.management.service;

import java.util.HashMap;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.parent.management.ManagementApplication;
import com.parent.management.R;
import com.parent.management.monitor.AppsInstalledMonitor;
import com.parent.management.monitor.BrowserBookmarkMonitor;
import com.parent.management.monitor.BrowserHistoryMonitor;
import com.parent.management.monitor.CallLogMonitor;
import com.parent.management.monitor.ContactsMonitor;
import com.parent.management.monitor.GpsMonitor;
import com.parent.management.monitor.Monitor;
import com.parent.management.monitor.Monitor.Type;

public class MonitorService extends Service {
	
	private BrowserHistoryMonitor mBrowserHistoryMonitor;
    private BrowserBookmarkMonitor mBrowserBookmarkMonitor;
	private ContactsMonitor mContactsMonitor;
	private CallLogMonitor mCallLogMonitor;
	private GpsMonitor mGpsMonitor;
    private AppsInstalledMonitor mAppsInstalledMonitor;

	@Override
	public IBinder onBind(Intent intent) {
	    return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		if (null == ManagementApplication.monitorList) {
		    ManagementApplication.monitorList = new HashMap<Type, Monitor>();
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
	    Log.d("MonitorService", "----> service created");
    }
	
	@Override
	public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2)
	{
		Log.d("MonitorService", "----> service started");
		if (mBrowserHistoryMonitor != null && !mBrowserHistoryMonitor.isMonitorRunning()) {
			mBrowserHistoryMonitor.startMonitoring();
			ManagementApplication.monitorList.put(Type.BROWSER_HISTORY, mBrowserHistoryMonitor);
		}
        if (!mBrowserBookmarkMonitor.isMonitorRunning()) {
            mBrowserBookmarkMonitor.startMonitoring();
            ManagementApplication.monitorList.put(Type.BROWSER_BOOKMARK, mBrowserBookmarkMonitor);
        }
		if (mContactsMonitor != null && !mContactsMonitor.isMonitorRunning()) {
			mContactsMonitor.startMonitoring();
			ManagementApplication.monitorList.put(Type.CONTACTS, mContactsMonitor);
		}
		if (mGpsMonitor != null && !mGpsMonitor.isMonitorRunning()) {
		    mGpsMonitor.startMonitoring();
            ManagementApplication.monitorList.put(Type.CALL_LOG, mGpsMonitor);
		}
		if (mCallLogMonitor != null && !mCallLogMonitor.isMonitorRunning()) {
			mCallLogMonitor.startMonitoring();
			ManagementApplication.monitorList.put(Type.CALL_LOG, mCallLogMonitor);
		}
        if (mAppsInstalledMonitor != null && !mAppsInstalledMonitor.isMonitorRunning()) {
            mAppsInstalledMonitor.startMonitoring();
            ManagementApplication.monitorList.put(Type.CALL_LOG, mAppsInstalledMonitor);
        }
		
		return 1;
	}
	
	@Override
    public void onDestroy() {
	    if (mBrowserHistoryMonitor.isMonitorRunning()) {
			mBrowserHistoryMonitor.stopMonitoring();
		}
		if (mContactsMonitor.isMonitorRunning()) {
			mContactsMonitor.stopMonitoring();
		}
		if (mCallLogMonitor.isMonitorRunning()) {
			mCallLogMonitor.stopMonitoring();
		}
        if (mGpsMonitor.isMonitorRunning()) {
            mGpsMonitor.stopMonitoring();
        }
        if (mAppsInstalledMonitor.isMonitorRunning()) {
            mAppsInstalledMonitor.stopMonitoring();
        }
		ManagementApplication.monitorList.clear();
        ManagementApplication.monitorList = null;
	}
	
}
