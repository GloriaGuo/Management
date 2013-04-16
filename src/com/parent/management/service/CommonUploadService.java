package com.parent.management.service;

import java.util.HashMap;

import android.content.Intent;

import com.parent.management.ManagementApplication;
import com.parent.management.monitor.Monitor;
import com.parent.management.monitor.Monitor.Type;

public class CommonUploadService extends UploadService {

    @Override
    public void onCreate()
    {
        if (mMonitorList == null) {
            mMonitorList = new HashMap<Type, Monitor>();
            if (ManagementApplication.commonMonitorList != null) {
                mMonitorList.putAll(ManagementApplication.commonMonitorList);
            }
        }
    }
    
    @Override
    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2)
    {
        return super.onStartCommand(paramIntent, paramInt1, paramInt2);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
}
