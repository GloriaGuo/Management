package com.monitor.kids.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.monitor.kids.KidsApplication;
import com.monitor.kids.service.MonitorService;
import com.monitor.kids.task.CommonUploadTask;

public class CommonUploadReceiver extends BroadcastReceiver {
    private static final String TAG = KidsApplication.getApplicationTag() + "." +
            CommonUploadReceiver.class.getSimpleName();
    
    @Override
    public void onReceive(Context context, Intent intent) {
      
        // Check monitor service
        if (!KidsApplication.isServiceRunning("com.monitor.kids.service.MonitorService", context)) {
            Log.d(TAG, "----> start monitor service");
            context.startService(new Intent(context, MonitorService.class));
        }
        
        // start upload task
        CommonUploadTask task = new CommonUploadTask();
        task.create();
        task.start();
    } 
}
