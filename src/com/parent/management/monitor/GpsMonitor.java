package com.parent.management.monitor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
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
    Context mContext = null;
    
    public GpsMonitor(Context context) {
        super(context);
        this.contentUri = CallLog.Calls.CONTENT_URI;
        mContext = ManagementApplication.getContext();
    }

    @Override
    public void startMonitoring() {
        final LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE );

        if (!manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            turnGPSOn();
        }
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        
        String bestProvider = mLocationManager.getBestProvider(criteria, true);
        
        Location currentLocation = mLocationManager.getLastKnownLocation(bestProvider);
        updateLocation(currentLocation);
        
        mLocationManager.requestLocationUpdates(bestProvider, 500, 0, locationListener);
        this.monitorStatus = true;
        Log.v(TAG, "---->started");
    }

    @Override
    public void stopMonitoring() {
        mLocationManager.removeUpdates(locationListener);
        this.monitorStatus = false;
        Log.d(TAG, "----> stopMonitoring");
    }
    
    private final LocationListener locationListener = new LocationListener() {    

        public void onLocationChanged(Location location) {    
            updateLocation(location);    
        }    

        public void onProviderDisabled(String provider){ 
            updateLocation(null);    
            Log.i(TAG, "Provider now is disabled.."); 
        }    

        public void onProviderEnabled(String provider){ 
            Log.i(TAG, "Provider now is enabled.."); 
        }    

        public void onStatusChanged(String provider, int status,Bundle extras){ }
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
            // TODO Auto-generated catch block
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
}
