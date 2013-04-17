package com.parent.management.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parent.management.ManagementApplication;
import com.parent.management.service.MonitorService;
import com.parent.management.task.SpecialUploadTask;

public class SpecialUploadReceiver extends BroadcastReceiver {
    private static final String TAG = ManagementApplication.getApplicationTag() + "." +
            SpecialUploadReceiver.class.getSimpleName();
    
    @Override
    public void onReceive(Context context, Intent intent) {
      
        // Check monitor service
        if (!ManagementApplication.isServiceRunning("com.parent.management.service.MonitorService", context)) {
            Log.d(TAG, "----> start monitor service");
            context.startService(new Intent(context, MonitorService.class));
        }
        
        // start upload task
        SpecialUploadTask task = new SpecialUploadTask();
        task.create();
        task.start();
    } 

}
