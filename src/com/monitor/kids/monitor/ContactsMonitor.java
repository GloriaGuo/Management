package com.monitor.kids.monitor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;

import com.monitor.kids.KidsApplication;

public class ContactsMonitor extends Monitor {

	private ContactsObserver contentObserver = null;

	public ContactsMonitor(Context context) {
		super(context);
		this.contentUri = ContactsContract.Data.CONTENT_URI;
	    this.contentObserver = new ContactsObserver(new Handler());
	}

	@Override
	public void startMonitoring() {
		this.contentResolver.registerContentObserver(this.contentUri, true, this.contentObserver);
	    this.monitorStatus = true;
	}

	@Override
	public void stopMonitoring() {
		this.contentResolver.unregisterContentObserver(this.contentObserver);
	    this.monitorStatus = false;
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
	
	private void updateDB() {
        Cursor cursor = KidsApplication.getContext().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null); 
        while (cursor.moveToNext()) { 
           String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)); 
           String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
           Log.v("SHAUN", "contact name" + contactName);
           String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)); 
           if (Integer.parseInt(hasPhone) > 0) { 
               // You know it has a number so now query it like this
               Cursor phones = KidsApplication.getContext().getContentResolver().query(
                       ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                       ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,
                       null,
                       null); 
               while (phones.moveToNext()) { 
                   String phoneNumber = phones.getString(phones.getColumnIndex(
                           ContactsContract.CommonDataKinds.Phone.NUMBER));  
                   Log.v("SHAUN", "phone number: " + phoneNumber);               
               } 
               phones.close(); 
           }
           int timesContacted = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.TIMES_CONTACTED));
           Log.v("SHAUN", "times contacted:" + timesContacted);
           Log.v("SHAUN", "--------------------");
        }
        cursor.close(); 
	}

    @Override
    public JSONArray extractDataForSend() {
        // example
        try {
            JSONArray data = new JSONArray();
            JSONObject raw = new JSONObject();
        
            raw.put("ColumnName", "ColumnValue");
            data.put(raw);
            return data;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }

    @Override
    public void updateStatusAfterSend(JSONArray failedList) {
        // TODO Auto-generated method stub
        
    }

}
