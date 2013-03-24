package com.parent.management.monitor;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;

public class ContactsMonitor extends Monitor {

	private ContactsObserver contentObserver = null;

	ContactsMonitor(Context context) {
		super(context);
		this.contentUri = ContactsContract.Data.CONTENT_URI;
	    this.contentObserver = new ContactsObserver(new Handler());
	}

	@Override
	public void startMonitoring() {
		this.contentResolver.registerContentObserver(this.contentUri, true, this.contentObserver);
	    this.monitorStatus = true;
	    Log.d("ContactsMonitor", "----> startMonitoring");
	}

	@Override
	public void stopMonitoring() {
		this.contentResolver.unregisterContentObserver(this.contentObserver);
	    this.monitorStatus = false;
	    Log.d("ContactsMonitor", "----> stopMonitoring");
	}
	
	private class ContactsObserver extends ContentObserver {

		public ContactsObserver(Handler handler) {
			super(handler);
		}
		
		@Override
        public void onChange(boolean selfChange) {
			// TODO
		}
		
	}

}
