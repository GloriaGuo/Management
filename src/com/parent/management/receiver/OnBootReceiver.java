package com.parent.management.receiver;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.parent.management.ManagementApplication;

public class OnBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		AlarmManager mAlarmManager = (AlarmManager)context.getSystemService("alarm");
	    
	    mAlarmManager.setRepeating(
	            AlarmManager.ELAPSED_REALTIME_WAKEUP, 
	            5000L + SystemClock.elapsedRealtime(), 
	            ManagementApplication.getConfiguration().getIntervalTime(),
	            ManagementApplication.getPendingIntent());
	}

}
