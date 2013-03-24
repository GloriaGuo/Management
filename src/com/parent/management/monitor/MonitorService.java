package com.parent.management.monitor;

import com.parent.management.ManagementApplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MonitorService extends Service {
	
	private BrowserHistoryMonitor mBrowserHistoryMonitor;
	private ContactsMonitor mContactsMonitor;
	private CallLogMonitor mCallLogMonitor;

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("MonitorService", "----> service bind");
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		if (ManagementApplication.getConfiguration().isMonitorBrowserHistory &&
				mBrowserHistoryMonitor == null) {
			mBrowserHistoryMonitor = new BrowserHistoryMonitor(this.getApplicationContext());
		}
		if (ManagementApplication.getConfiguration().isMonitorContacts &&
				mContactsMonitor == null) {
			mContactsMonitor = new ContactsMonitor(this.getApplicationContext());
		} 
		if (ManagementApplication.getConfiguration().isMonitorCallLog &&
				mCallLogMonitor == null) {
			mCallLogMonitor = new CallLogMonitor(this.getApplicationContext());
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
		if (!mContactsMonitor.isMonitorRunning()) {
			mContactsMonitor.startMonitoring();
		}
		if (!mCallLogMonitor.isMonitorRunning()) {
			mCallLogMonitor.startMonitoring();
		}
		
		return 1;
	}
	
	@Override
    public void onDestroy() {
		super.onDestroy();
	}
	
}
