package com.parent.management.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.parent.management.ManagementApplication;

public class OnBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
	    Log.d("OnBootReceiver", "---->on boot");
	    if (ManagementApplication.getConfiguration().getIsRegisted()) {
    		AlarmManager mAlarmManager = (AlarmManager)context.getSystemService("alarm");
    	    
    		PendingIntent mPendingIntent = PendingIntent.getBroadcast(
    		        context, 0, new Intent(context, ManagementReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
            
    	    mAlarmManager.set(
    	            AlarmManager.ELAPSED_REALTIME_WAKEUP, 
    	            5000L + SystemClock.elapsedRealtime(), 
    	            mPendingIntent);
	    }
	}

}
