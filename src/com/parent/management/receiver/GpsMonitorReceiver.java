package com.parent.management.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parent.management.ManagementApplication;
import com.parent.management.monitor.GpsMonitor;
import com.parent.management.monitor.Monitor.Type;

public class GpsMonitorReceiver extends BroadcastReceiver {
    private static final String TAG = ManagementApplication.getApplicationTag() + "." +
            GpsMonitorReceiver.class.getSimpleName();
    
    private GpsMonitor mGpsMonitor = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mGpsMonitor == null) {
//            mGpsMonitor = new GpsMonitor(context);
            if(null != ManagementApplication.commonMonitorList) {
                mGpsMonitor = (GpsMonitor) ManagementApplication.commonMonitorList.get(Type.GPS_INFO);
            }
        }
        
        mGpsMonitor.startMonitoring();
//        mGpsMonitor.stopMonitoring();
    }
}
