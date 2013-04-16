package com.parent.management.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parent.management.ManagementApplication;
import com.parent.management.R;
import com.parent.management.service.MonitorService;
import com.parent.management.service.UploadService;

public class ManagementReceiver extends BroadcastReceiver {
    private static final String TAG = ManagementApplication.getApplicationTag() + "." +
            ManagementReceiver.class.getSimpleName();

    @Override
	public void onReceive(Context context, Intent intent) {
	    
	    // Check monitor service
        if (!ManagementApplication.isServiceRunning("com.parent.management.service.MonitorService", context)) {
            
            ManagementApplication.getConfiguration().setCommonIntervalTime(
                    ManagementApplication.getContext().getResources().getInteger(
                            R.attr.default_common_interval_time));
            ManagementApplication.getConfiguration().setSpecialIntervalTime(
                    ManagementApplication.getContext().getResources().getInteger(
                            R.attr.default_special_interval_time));
            
            Log.d(TAG, "----> start monitor service");
            context.startService(new Intent(context, MonitorService.class));
            
            if (ManagementApplication.isServiceRunning("com.parent.management.service.UploadService", context)) {
                Log.d(TAG, "----> stop upload service");
                context.stopService(new Intent(context, UploadService.class));
            }
            Log.d(TAG, "----> starting upload service");
            context.startService(new Intent(context, UploadService.class));
        } else {
            Log.d(TAG, "----> Monitor service is still alive");
        }
        
	}

}
