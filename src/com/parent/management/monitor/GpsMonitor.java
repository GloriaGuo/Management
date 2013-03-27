package com.parent.management.monitor;
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

            double latidude = location.getLatitude();
            double lontitude = location.getLongitude();
            float speed = location.getSpeed();
            long time = location.getTime();

            final ContentValues values = new ContentValues();
            values.put(ManagementProvider.Gps.LATIDUDE, latidude);
            values.put(ManagementProvider.Gps.LONGITUDE, lontitude);
            values.put(ManagementProvider.Gps.SPEED, speed);
            values.put(ManagementProvider.Gps.TIME, time);
            
            ManagementApplication.getContext().getContentResolver().insert(
                    ManagementProvider.Gps.CONTENT_URI, values);
            Log.v(TAG, "insert gps: latidude=" + latidude + ";lontitude=" + lontitude
                    + ";speed=" + speed + ";time=" + time);
            
            
        } else {
            Log.w(TAG, "not get Location");
        }
    }

    @Override
    public Cursor extraData() {
        // TODO Auto-generated method stub
        return null;
    }

}
