package com.parent.management.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.parent.management.R;
import com.parent.management.monitor.BrowserHistoryMonitor;
import com.parent.management.monitor.CallLogMonitor;
import com.parent.management.monitor.ContactsMonitor;
import com.parent.management.monitor.GpsMonitor;

public class MonitorService extends Service {
	
	private BrowserHistoryMonitor mBrowserHistoryMonitor;
	private ContactsMonitor mContactsMonitor;
	private CallLogMonitor mCallLogMonitor;
	private GpsMonitor mGpsMonitor;

	@Override
	public IBinder onBind(Intent intent) {
	    Log.d("MonitorService", "----> service bind");
	    return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		if (this.getResources().getBoolean(R.attr.monitor_browser_history) &&
		        mBrowserHistoryMonitor == null) {
		    mBrowserHistoryMonitor = new BrowserHistoryMonitor(this.getApplicationContext());
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
	    Log.d("MonitorService", "----> service created");
    }
	
	@Override
	public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2)
	{
		Log.d("MonitorService", "----> service started");
		if (!mBrowserHistoryMonitor.isMonitorRunning()) {
			mBrowserHistoryMonitor.startMonitoring();
		}
        if (!mBrowserHistoryMonitor.isMonitorRunning()) {
            mBrowserHistoryMonitor.startMonitoring();
        }
		if (!mContactsMonitor.isMonitorRunning()) {
			mContactsMonitor.startMonitoring();
		}
		if (!mGpsMonitor.isMonitorRunning()) {
		    mGpsMonitor.startMonitoring();
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
	}
	
}
