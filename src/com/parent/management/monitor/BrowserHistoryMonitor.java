package com.parent.management.monitor;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Browser;
import android.util.Log;

public class BrowserHistoryMonitor extends Monitor {
	
	private BrowserHistoryObserver contentObserver = null;

	BrowserHistoryMonitor(Context context) {
		super(context);
	    this.contentUri = Browser.BOOKMARKS_URI;
	    this.contentObserver = new BrowserHistoryObserver(new Handler());
	}

	@Override
	public void startMonitoring() {
		this.contentResolver.registerContentObserver(this.contentUri, true, this.contentObserver);
	    this.monitorStatus = true;
	    Log.d("BrowserHistoryMonitor", "----> startMonitoring");
	}

	@Override
	public void stopMonitoring() {
        this.contentResolver.unregisterContentObserver(this.contentObserver);
        this.monitorStatus = false;
	    Log.d("BrowserHistoryMonitor", "----> stopMonitoring");
	}
	
	private class BrowserHistoryObserver extends ContentObserver {

		public BrowserHistoryObserver(Handler handler) {
			super(handler);
		}
		
		@Override
        public void onChange(boolean selfChange) {
			// TODO
		}
		
	}

}
