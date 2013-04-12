package com.parent.management.monitor;

import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.net.Uri;
import android.provider.Settings;

import com.parent.management.ManagementApplication;

public class ManagementLocationManager{
    private static final String TAG = ManagementApplication.getApplicationTag() + "." +
            ManagementLocationManager.class.getSimpleName();

    private Context mContext = null;
    private GpsLocationProvider mGpsProvider = null;
    private NetworkLocationProvider mNetworkLocationProvider = null;
    
    public static int LOCATION_GPS = 1;
    public static int LOCATION_NETWORK = 2;
    
    ManagementLocationManager(Context context, int type) {
        this.mContext = context;
        
        if ((type & LOCATION_GPS) == LOCATION_GPS) {
            mGpsProvider = new GpsLocationProvider(mContext);
        }
        if ((type & LOCATION_NETWORK) == LOCATION_NETWORK) {
            mNetworkLocationProvider = new NetworkLocationProvider(mContext);
        }
    }
    
    public boolean isProviderEnabled(int type) {
        if ((type & LOCATION_GPS) == LOCATION_GPS) {
            return mGpsProvider.isProviderEnabled();
        } else {
            return mNetworkLocationProvider.isProviderEnabled();
        }
    }
    
    public void requestLocationUpdates(long minTime, float minDistance, LocationListener listener) {
        if (mGpsProvider != null) {
            mGpsProvider.requestLocationUpdates(minTime, minDistance, listener);
        }
        if (mNetworkLocationProvider != null) {
            mNetworkLocationProvider.requestLocationUpdates(minTime, minDistance, listener);
        }
    }
    
    public void stopLocate() {
        if (mGpsProvider != null) {
            mGpsProvider.stopGps();
        }
        if (mNetworkLocationProvider != null) {
            
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
