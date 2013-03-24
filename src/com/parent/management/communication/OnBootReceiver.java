package com.parent.management.communication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class OnBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		AlarmManager localAlarmManager = (AlarmManager)context.getSystemService("alarm");
	    PendingIntent mPendingIntent = PendingIntent.getBroadcast(
	    		context, 0, new Intent(context, CommunicationReceiver.class), 134217728);
	    /*int i = PreferencesHelper.GetIntervalForSerice(context);
	    if (i < 60000)
	        i = 1800000;
	    localAlarmManager.setRepeating(2, 5000L + SystemClock.elapsedRealtime(), i, mPendingIntent);*/
	}

}
