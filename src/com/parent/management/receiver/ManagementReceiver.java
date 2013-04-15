package com.parent.management.receiver;

import java.util.Iterator;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parent.management.ManagementApplication;
import com.parent.management.service.CommonUploadService;
import com.parent.management.service.MonitorService;
import com.parent.management.service.SpecialUploadService;
import com.parent.management.service.UploadService;

public class ManagementReceiver extends BroadcastReceiver {
    private static final String TAG = ManagementApplication.getApplicationTag() + "." +
            ManagementReceiver.class.getSimpleName();

    public static boolean isServiceRunning(String paramString, Context context)
    {
        Iterator<RunningServiceInfo> mIterator = ((ActivityManager)context.
                getSystemService(Context.ACTIVITY_SERVICE)).getRunningServices(5000).iterator();
      
        while (mIterator.hasNext()) {
            RunningServiceInfo si = (RunningServiceInfo) mIterator.next();
            if (si.service.getClassName().equals(paramString)) {
                return true;
            }
        }
        return false;
    }
    
	@Override
	public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive:" + intent.toString());
	    
	    // Check monitor service
        if (!isServiceRunning("com.parent.management.service.MonitorService", context)) {
            Log.d(TAG, "----> start monitor service");
            context.startService(new Intent(context, MonitorService.class));
        }
        
        String action = intent.getAction();
        Log.d(TAG, "----> action == " + action);
        
        if (action != null && action.equals(ManagementApplication.MANAGEMENT_RECEIVER_FILTER_ACTIONS_COMMON)) {
            if (isServiceRunning("com.parent.management.service.CommonUploadService", context)) {
                Log.d(TAG, "----> stop common upload service");
                context.stopService(new Intent(context, CommonUploadService.class));
            }
            Log.d(TAG, "----> starting common upload service");
            context.startService(new Intent(context, CommonUploadService.class));
        } else if (action != null && action.equals(ManagementApplication.MANAGEMENT_RECEIVER_FILTER_ACTIONS_SPECIAL)) {
            if (isServiceRunning("com.parent.management.service.SpecialUploadService", context)) {
                Log.d(TAG, "----> stop special upload service");
                context.stopService(new Intent(context, SpecialUploadService.class));
            }
            Log.d(TAG, "----> starting special upload service");
            context.startService(new Intent(context, SpecialUploadService.class));            
        } else {
            if (isServiceRunning("com.parent.management.service.UploadService", context)) {
                Log.d(TAG, "----> stop upload service");
                context.stopService(new Intent(context, UploadService.class));
            }
            Log.d(TAG, "----> starting upload service");
            context.startService(new Intent(context, UploadService.class));
        }
        
	}

}
