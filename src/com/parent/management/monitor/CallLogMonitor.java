package com.parent.management.monitor;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.CallLog;
import android.util.Log;


public class CallLogMonitor extends Monitor {
	
	private CallLogObserver contentObserver = null;

	public CallLogMonitor(Context context) {
		super(context);
		this.contentUri = CallLog.Calls.CONTENT_URI;
	    this.contentObserver = new CallLogObserver(new Handler());
	}

	@Override
	public void startMonitoring() {
		this.contentResolver.registerContentObserver(this.contentUri, true, this.contentObserver);
	    this.monitorStatus = true;
	    Log.d("CallLogMonitor", "----> startMonitoring");
	}

	@Override
	public void stopMonitoring() {
		this.contentResolver.unregisterContentObserver(this.contentObserver);
		this.monitorStatus = false;
	    Log.d("CallLogMonitor", "----> stopMonitoring");
	}
	
	private class CallLogObserver extends ContentObserver {

		public CallLogObserver(Handler handler) {
			super(handler);
		}
		
		@Override
        public void onChange(boolean selfChange) {
			// TODO
		}
		
	}

}
