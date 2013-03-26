package com.parent.management.monitor;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public abstract class Monitor {
	
	public final ContentResolver contentResolver;
	public boolean monitorStatus = false;
	public Uri contentUri;
	
	public enum Type {
	    BROWSER_HISTORY,
	    GPS_INFO,
	    APP_INSTALLED,
	    APP_USED,
	    CONTACTS,
	    CALL_LOG
	}
	
	Monitor(Context context) {
		contentResolver = context.getApplicationContext().getContentResolver();
	}
	
	public boolean isMonitorRunning()
	{
	    return this.monitorStatus;
	}

	public abstract void startMonitoring();

	public abstract void stopMonitoring();
	
	public abstract Cursor extraData();

}
