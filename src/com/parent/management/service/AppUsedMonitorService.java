package com.parent.management.service;

import com.parent.management.ManagementApplication;
import com.parent.management.R;
import com.parent.management.monitor.AppsUsedMonitor;
import com.parent.management.monitor.Monitor.Type;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AppUsedMonitorService extends Service {
    
    private static final String TAG = ManagementApplication.getApplicationTag() + "." +
            AppUsedMonitorService.class.getSimpleName();
    
    private AppsUsedMonitor mAppsUsedMonitor;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate()
    {
        if (this.getResources().getBoolean(R.attr.monitor_apps_used) &&
                mAppsUsedMonitor == null) {
            mAppsUsedMonitor = new AppsUsedMonitor(this.getApplicationContext());
        }
        Log.d(TAG, "----> service created");
    }
    
    @Override
    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2)
    {
        if (mAppsUsedMonitor != null) {
            mAppsUsedMonitor.startMonitoring();
        }
        return 1;
    }
    
    @Override
    public void onDestroy() {
        
    }

}
