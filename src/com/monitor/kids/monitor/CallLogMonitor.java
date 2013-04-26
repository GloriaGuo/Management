package com.monitor.kids.monitor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.CallLog;

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
	}

	@Override
	public void stopMonitoring() {
		this.contentResolver.unregisterContentObserver(this.contentObserver);
		this.monitorStatus = false;
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

    @Override
    public JSONArray extractDataForSend() {
        // example
        try {
            JSONArray data = new JSONArray();
            JSONObject raw = new JSONObject();
        
            raw.put("ColumnName", "ColumnValue");
            data.put(raw);
            return data;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }

    @Override
    public void updateStatusAfterSend(JSONArray failedList) {
        // TODO Auto-generated method stub
        
    }

}
