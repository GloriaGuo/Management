package com.parent.management.receiver;

import java.util.Iterator;

import com.parent.management.service.CommunicationService;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ManagementReceiver extends BroadcastReceiver {

    public static boolean isServiceRunning(String paramString, Context context)
    {
      Iterator mIterator = ((ActivityManager)context.getSystemService("activity"))
              .getRunningServices(2147483647).iterator();
      return false;
      
    }
    
	@Override
	public void onReceive(Context context, Intent intent) {
	    Log.d("ManagementReceiver", "----> starting communication service");
	    context.startService(new Intent(context, CommunicationService.class));
	}

}
