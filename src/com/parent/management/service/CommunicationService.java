package com.parent.management.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;

public class CommunicationService extends Service {
    
    private static WifiManager.WifiLock mWifilock = null;
    private static PowerManager.WakeLock mLockStatic = null;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public void onCreate()
    {

    }
	
	@Override
    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2)
    {
	    if (mWifilock == null)
	    {
	        mWifilock = ((WifiManager)getSystemService("wifi")).createWifiLock("PM");
	        mWifilock.acquire();
	    }
	    //getLock(this).acquire();
//	    SendJob();
	    mWifilock.release();
        return 1;
    }
	
	@Override
    public void onDestroy() {
  
	}
	
	private static PowerManager.WakeLock getLock(Context context)
	{
	    if (mLockStatic == null)
	    {
	        mLockStatic = ((PowerManager)context.getSystemService("power")).newWakeLock(1, "PM");
	        mLockStatic.setReferenceCounted(true);
	    }
	    return mLockStatic;
	}
}
