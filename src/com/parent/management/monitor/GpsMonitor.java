package com.parent.management.monitor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;

import com.parent.management.ManagementApplication;
import com.parent.management.db.ManagementProvider;


public class GpsMonitor extends Monitor {
    private static final String TAG = ManagementApplication.getApplicationTag() + "." +
            GpsMonitor.class.getSimpleName();
    LocationManager mLocationManager;
    
    public GpsMonitor(Context context) {
        super(context);
        this.contentUri = CallLog.Calls.CONTENT_URI;
    }

    @Override
    public void startMonitoring() {
        mLocationManager = (LocationManager) ManagementApplication.getContext().getSystemService(Context.LOCATION_SERVICE);
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
        Log.d(TAG, "----> startMonitoring");
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
            String gps_text = "GPS info:" + location.toString() + "\n\tLongitude:"
                    + location.getLongitude() + "\n\tLatitude:" + location.getLatitude();
            Log.i(TAG, gps_text);

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
            
            ManagementApplication.getContext().getContentResolver().insert(
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
            String GpsSel = ManagementProvider.Gps.IS_SENT + " = " + ManagementProvider.IS_SENT_NO;
            Cursor gpsCur = ManagementApplication.getContext().getContentResolver().query(
                    ManagementProvider.Gps.CONTENT_URI,
                    GpsProj, GpsSel, null, null);

            if (gpsCur == null) {
                Log.v(TAG, "open browserHistory native failed");
                return null;
            }
            if (gpsCur.moveToFirst() && gpsCur.getCount() > 0) {
                while (gpsCur.isAfterLast() == false) {
                    String alt = gpsCur.getString(
                            gpsCur.getColumnIndex(ManagementProvider.Gps.ALTITUDE));
                    String lat = gpsCur.getString(
                            gpsCur.getColumnIndex(ManagementProvider.Gps.LATIDUDE));
                    String lon = gpsCur.getString(
                            gpsCur.getColumnIndex(ManagementProvider.Gps.LONGITUDE));
                    String spd = gpsCur.getString(
                            gpsCur.getColumnIndex(ManagementProvider.Gps.SPEED));
                    String date = gpsCur.getString(
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
            gpsCur.close();
            
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
