package com.parent.management.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


/**
 * Broadcast receiver on network status changes
 */
public class NetworkStatusReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final NetworkInfo mNetInfo = ((ConnectivityManager) 
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (mNetInfo != null && mNetInfo.isConnected()) {
        	// TODO
        }
	}

}
