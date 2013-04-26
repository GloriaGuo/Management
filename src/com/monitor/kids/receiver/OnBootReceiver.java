package com.monitor.kids.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.monitor.kids.KidsApplication;
import com.monitor.kids.R;

public class OnBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
	    Log.d("OnBootReceiver", "---->on boot");
	    if (KidsApplication.getConfiguration().getIsRegisted()) {
    		AlarmManager mAlarmManager = (AlarmManager)context.getSystemService("alarm");
    	    
    		PendingIntent mPendingIntent = PendingIntent.getBroadcast(
    		        context, 0, new Intent(context, KidsReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT);
            
    	    mAlarmManager.setRepeating(
    	            AlarmManager.ELAPSED_REALTIME_WAKEUP, 
    	            10000L + SystemClock.elapsedRealtime(),
    	            KidsApplication.getContext().getResources().getInteger(
                            R.attr.default_check_alive_interval_time),
    	            mPendingIntent);
	    }
	}

}
