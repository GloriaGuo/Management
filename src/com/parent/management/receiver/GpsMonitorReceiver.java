package com.parent.management.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.parent.management.ManagementApplication;
import com.parent.management.monitor.GpsMonitor;
import com.parent.management.monitor.Monitor.Type;

public class GpsMonitorReceiver extends BroadcastReceiver {
    
    private GpsMonitor mGpsMonitor = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mGpsMonitor == null) {
            if(null != ManagementApplication.commonMonitorList) {
                mGpsMonitor = (GpsMonitor) ManagementApplication.commonMonitorList.get(Type.GPS_INFO);
            }
        }
        
        mGpsMonitor.startMonitoring();
    }
}
