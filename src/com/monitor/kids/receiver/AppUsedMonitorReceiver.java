package com.monitor.kids.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.monitor.kids.monitor.AppsUsedMonitor;

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
