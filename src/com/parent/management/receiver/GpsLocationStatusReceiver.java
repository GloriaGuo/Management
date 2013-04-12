package com.parent.management.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parent.management.ManagementApplication;
import com.parent.management.monitor.GpsMonitor;
import com.parent.management.monitor.Monitor;


public class GpsLocationStatusReceiver extends BroadcastReceiver {
    private static final String TAG = ManagementApplication.getApplicationTag() + "." +
            GpsLocationStatusReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ManagementApplication.commonMonitorList != null) {
            Log.i(TAG, "Provider status is changed. update provider.");
            GpsMonitor gpsMonitor = (GpsMonitor) ManagementApplication.commonMonitorList.get(Monitor.Type.GPS_INFO);
//            gpsMonitor.updateProvider(null);
        }
    }

}
