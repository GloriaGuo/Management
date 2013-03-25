package com.parent.management.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CommunicationService extends Service {

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
        return 1;
    }
	
	@Override
    public void onDestroy() {
  
	}
}
