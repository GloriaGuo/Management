package com.monitor.kids.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.wifi.WifiManager;
import android.util.Log;

import com.monitor.kids.KidsApplication;
import com.monitor.kids.R;
import com.monitor.kids.jsonclient.JSONClientException;
import com.monitor.kids.jsonclient.JSONHttpClient;
import com.monitor.kids.jsonclient.JSONParams;
import com.monitor.kids.monitor.Monitor;
import com.monitor.kids.monitor.Monitor.Type;

public abstract class UploadTask {
    private static final String TAG = KidsApplication.getApplicationTag() + "." +
            UploadTask.class.getSimpleName();
    
    private static WifiManager.WifiLock mWifilock = null;
    
    protected HashMap<Type, Monitor> mMonitorList = null;
    
    private JSONHttpClient mClient = null;

	public abstract void create();
	
    public void start()
    {
        Log.d(TAG, "----> Upload Task start.");
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (mWifilock == null) {
                    mWifilock = ((WifiManager)KidsApplication.getContext()
                            .getSystemService("wifi")).createWifiLock("PM");
                    mWifilock.acquire();
                }
                //getLock(this).acquire();
                uploadJob();
            }
            
        }).start();
        
    }	    
	
    private void stop() {
        Log.d(TAG, "----> Upload Task stop.");
        if (mWifilock != null) {
	        mWifilock.release();
	        mWifilock = null;
        }
	    if (mMonitorList != null) {
	        mMonitorList.clear();
	        mMonitorList = null;
	    }
	}
	
	private void uploadJob() {
	    try {
    	    // prepare upload data
            JSONArray jsonParams = new JSONArray();
            
            if (mMonitorList == null ||
                    (mMonitorList != null && mMonitorList.isEmpty())) {
                return;
            }

            Iterator<Entry<Type, Monitor>> iterator = mMonitorList.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<Type, Monitor> entry = iterator.next();
                Log.d(TAG, "----> prepare upload data for : " + entry.getKey());

                JSONArray data = entry.getValue().extractDataForSend();
                if (data != null && data.length() != 0) {
                    JSONObject jsonRows = new JSONObject();
                    jsonRows.put(JSONParams.DATA_TYPE, entry.getKey().ordinal());
                    jsonRows.put(JSONParams.DATA, data);
                    jsonParams.put(jsonRows);
                }
            }
            
            if (jsonParams.length() > 0) {
                // Create client specifying JSON-RPC version 2.0
                mClient = new JSONHttpClient(KidsApplication.getContext().getResources().getString(
                        R.string.server_address));
                mClient.setConnectionTimeout(5000);
                mClient.setSoTimeout(5000);
                
                JSONArray failed = mClient.doUpload(jsonParams);
                Log.d(TAG, "Failed [" + failed.length() + "] records for this upload");
                
                HashMap<Integer, JSONArray> failedList = new HashMap<Integer, JSONArray>();
                for (int i=0; i<failed.length(); i++) {
                    JSONObject data = (JSONObject) failed.opt(i);
                    failedList.put(
                            Integer.valueOf(data.getInt(JSONParams.DATA_TYPE)), 
                            data.getJSONArray(JSONParams.RESPONSE_FAILED_LIST));
                    
                }
                
                // prepare update DB
                while (iterator.hasNext()) {
                    Entry<Type, Monitor> entry = iterator.next();
                    Integer key = Integer.valueOf(entry.getKey().ordinal());
                    
                    if (failedList.containsKey(key)) {
                        entry.getValue().updateStatusAfterSend(failedList.get(key));
                    } else {
                        entry.getValue().updateStatusAfterSend(null);
                    }
                }
                
                getConfiguration();
            }
	    } catch (JSONException e) {
	        Log.e(TAG, "Invalid JSON request: " + e.getMessage());
	    } catch (JSONClientException e1) {
	        Log.e(TAG, "Upload failed: " + e1.getMessage());
	    }
	    
	    stop();        
        return;
	}
	
	private void getConfiguration() {
	    // get the new configuration from server
        int commonInterval = 0;
        int specialInterval = 0;
        try {
            JSONObject response = mClient.doConfiguration();
            commonInterval = response.getInt(JSONParams.COMMON_INTERVAL_TIME) * 1000;
            specialInterval = response.getInt(JSONParams.SPECIAL_INTERVAL_TIME) * 1000;
        } catch (JSONClientException e) {
            Log.e(TAG, "Get Configuration Failed: " + e.getMessage());
            commonInterval = KidsApplication.getContext().getResources().getInteger(
                    R.attr.default_common_interval_time);
            specialInterval = KidsApplication.getContext().getResources().getInteger(
                    R.attr.default_special_interval_time);
        } catch (JSONException e1) {
            Log.e(TAG, "Invalid response: " + e1.getMessage());
            commonInterval = KidsApplication.getContext().getResources().getInteger(
                    R.attr.default_common_interval_time);
            specialInterval = KidsApplication.getContext().getResources().getInteger(
                    R.attr.default_special_interval_time);
        }
        if (commonInterval != KidsApplication.getConfiguration().getCommonIntervalTime()) {
            Log.d(TAG, "The new common interval time is " + commonInterval);
            KidsApplication.getConfiguration().setCommonIntervalTime(commonInterval);
        }
        if (specialInterval != KidsApplication.getConfiguration().getSpecialIntervalTime()) {
            Log.d(TAG, "The new special interval time is " + specialInterval);
            KidsApplication.getConfiguration().setSpecialIntervalTime(specialInterval);
        }
	}
}
