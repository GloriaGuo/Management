package com.parent.management.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

/**
 * Broadcast receiver on gps info changes
 */
public class GpsInfoReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		LocationManager mLocationManager = (LocationManager) 
				context.getSystemService(Context.LOCATION_SERVICE);
	}

}
