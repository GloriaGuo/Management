package com.monitor.kids.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.monitor.kids.KidsApplication;
import com.monitor.kids.monitor.GpsMonitor;
import com.monitor.kids.monitor.Monitor.Type;

public class GpsMonitorReceiver extends BroadcastReceiver {
    
    private GpsMonitor mGpsMonitor = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (null == mGpsMonitor) {
            if(null != KidsApplication.commonMonitorList) {
                mGpsMonitor = (GpsMonitor) KidsApplication.commonMonitorList.get(Type.GPS_INFO);
            }
        }
        if (null != mGpsMonitor) {
            mGpsMonitor.startMonitoring();
        }
    }
}
