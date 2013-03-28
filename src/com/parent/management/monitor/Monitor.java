package com.parent.management.monitor;

import org.json.JSONArray;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

public abstract class Monitor {
	
	public final ContentResolver contentResolver;
	public boolean monitorStatus = false;
	public Uri contentUri;
	
	public enum Type {
	    DEVICE_INFO,
        GPS_INFO,
	    BROWSER_HISTORY,
	    APP_USED,
	    CALL_LOG,
	    SMS,
	    BROWSER_BOOKMARK,
        CONTACTS,
        CALENDAR,
	    APP_INSTALLED
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

    public abstract JSONArray extractDataForSend();
    
    public abstract void updateStatusAfterSend(JSONArray failedList);

}
