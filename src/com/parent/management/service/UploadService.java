package com.parent.management.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import com.parent.management.ManagementApplication;
import com.parent.management.R;
import com.parent.management.jsonclient.JSONClientException;
import com.parent.management.jsonclient.JSONHttpClient;
import com.parent.management.jsonclient.JSONParams;
import com.parent.management.monitor.Monitor;
import com.parent.management.monitor.Monitor.Type;

public class UploadService extends Service {
    private static final String TAG = ManagementApplication.getApplicationTag() + "." +
            UploadService.class.getSimpleName();
    
    private static WifiManager.WifiLock mWifilock = null;
    
    protected HashMap<Type, Monitor> mMonitorList = null;
    
    private JSONHttpClient mClient = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
    public void onCreate()
	{
	    if (mMonitorList == null) {
	        mMonitorList = new HashMap<Type, Monitor>();
	        mMonitorList.putAll(ManagementApplication.commonMonitorList);
	        mMonitorList.putAll(ManagementApplication.specialMonitorList);
	    }
    }
	
	@Override
    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2)
    {
	    new Thread(new Runnable() {

            @Override
            public void run() {
                Log.d(TAG, "----> Upload Service start.");
                if (mWifilock == null)
                {
                    mWifilock = ((WifiManager)getSystemService("wifi")).createWifiLock("PM");
                    mWifilock.acquire();
                }
                //getLock(this).acquire();
                uploadJob();
                UploadService.this.stopSelf();
            }
	    
	    }).start();
        return 1;
    }
	
	@Override
    public void onDestroy() {
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
            Log.e("TEST", "mMonitorList === " + mMonitorList.toString());

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
                mClient = new JSONHttpClient(this.getResources().getString(R.string.server_address));
                mClient.setConnectionTimeout(2000);
                mClient.setSoTimeout(2000);
                
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
            commonInterval = this.getResources().getInteger(R.attr.default_common_interval_time);
            specialInterval = this.getResources().getInteger(R.attr.default_special_interval_time);
        } catch (JSONException e1) {
            Log.e(TAG, "Invalid response: " + e1.getMessage());
            commonInterval = this.getResources().getInteger(R.attr.default_common_interval_time);
            specialInterval = this.getResources().getInteger(R.attr.default_special_interval_time);
        }
        if (commonInterval != ManagementApplication.getConfiguration().getCommonIntervalTime()) {
            Log.d(TAG, "The new common interval time is " + commonInterval);
            ManagementApplication.getConfiguration().setCommonIntervalTime(commonInterval);
        }
        if (specialInterval != ManagementApplication.getConfiguration().getSpecialIntervalTime()) {
            Log.d(TAG, "The new common interval time is " + specialInterval);
            ManagementApplication.getConfiguration().setSpecialIntervalTime(specialInterval);
        }
	}
}
