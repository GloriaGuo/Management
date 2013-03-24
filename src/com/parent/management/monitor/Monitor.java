package com.parent.management.monitor;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

public abstract class Monitor {
	
	public final ContentResolver contentResolver;
	public boolean monitorStatus = false;
	public Uri contentUri;
	
	Monitor(Context context) {
		contentResolver = context.getApplicationContext().getContentResolver();
	}
	
	public boolean isMonitorRunning()
	{
	    return this.monitorStatus;
	}

	public abstract void startMonitoring();

	public abstract void stopMonitoring();

}
