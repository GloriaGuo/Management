package com.parent.management.monitor;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;

public class GpsLocationProvider {
    
    private Context mContext;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener = null;
    
    public GpsLocationProvider(Context context) {
        this.mContext = context;
        this.mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    public boolean isProviderEnabled() {
        return this.mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    
    void requestLocationUpdates(long minTime, float minDistance, LocationListener listener) {
        this.mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, minTime, minDistance, listener);
        this.mLocationListener = listener;
    }

    public void stopGps() {
        this.mLocationManager.removeUpdates(this.mLocationListener);
    }
    
}
