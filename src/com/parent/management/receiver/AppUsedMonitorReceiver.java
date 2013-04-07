package com.parent.management.receiver;

import com.parent.management.monitor.AppsUsedMonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppUsedMonitorReceiver extends BroadcastReceiver {
    
    private AppsUsedMonitor mAppsUsedMonitor = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mAppsUsedMonitor == null) {
            mAppsUsedMonitor = new AppsUsedMonitor(context);
        }
        
        mAppsUsedMonitor.startMonitoring();
        mAppsUsedMonitor.stopMonitoring();
    }
}
