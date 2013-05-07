package com.monitor.kids.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.monitor.kids.KidsApplication;
import com.monitor.kids.R;
import com.monitor.kids.service.MonitorService;

public class KidsReceiver extends BroadcastReceiver {
    private static final String TAG = KidsApplication.getApplicationTag() + "." +
            KidsReceiver.class.getSimpleName();

    @Override
	public void onReceive(Context context, Intent intent) {
        String action = intent.getAction(); 
        Log.d(TAG, "----> Into KidsReceiver, action === " + action);
        
        if (KidsApplication.getConfiguration().getIsRegisted()) {
            // Check monitor service
            if (!KidsApplication.isServiceRunning("com.monitor.kids.service.MonitorService", context)) {
                
                KidsApplication.getConfiguration().setCommonIntervalTime(
                        KidsApplication.getContext().getResources().getInteger(
                                R.attr.default_common_interval_time));
                KidsApplication.getConfiguration().setSpecialIntervalTime(
                        KidsApplication.getContext().getResources().getInteger(
                                R.attr.default_special_interval_time));
                
                Log.d(TAG, "----> start monitor service");
                context.startService(new Intent(context, MonitorService.class));
            } else {
                Log.d(TAG, "----> Monitor service is still alive");
            }
        }
        
	}

}
