package com.parent.management.monitor;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;

public class NetworkLocationProvider {
    
    private Context mContext;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener = null;
    
    private long interval;
    private float distance;
    
    public NetworkLocationProvider(Context context) {
        this.mContext = context;
        this.mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }
    
    public boolean isProviderEnabled() {
        return false;
    }
    
    void requestLocationUpdates(long minTime, float minDistance, LocationListener listener) {
        this.interval = minTime;
        this.distance = minDistance;
        this.mLocationListener = listener;
    }
    
    void start() {
        
    }
    
    void stop() {
        
    }
    
}
