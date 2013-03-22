package com.parent.management.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class ManagementProvider extends ContentProvider {
	
	/**
	 * ParentManagement authority for content URIs
	 */
	public static final String AUTHORITY = "com.parent.provider.management";
	
	/**
	 * BrowserHistory wrapper class for content provider
	 */
	public static final class BrowserHistory implements BaseColumns {
        public static final String BROWSER_HISTORY_TABLE_NAME = "BrowserHistory";
		public static final String IS_SENT = "IsSend";
	    public static final String LAST_VISIT = "Lastvisit";
		public static final String TITLE = "Title";
		public static final String URL = "URL";
		public static final String VISIT_COUNT = "Visitcount";
	}
	
	/**
	 * GpsInfo wrapper class for content provider
	 */
	public static final class GpsInfo implements BaseColumns {
	
	}
	
	/**
	 * InstalledApps wrapper class for content provider
	 */
	public static final class InstalledApps implements BaseColumns {
	
	}
	
	/**
	 * AppsUsedInfo wrapper class for content provider
	 */
	public static final class AppsUsedInfo implements BaseColumns {
	
	}
	
	/**
	 * Contacts wrapper class for content provider
	 */
	public static final class Contacts implements BaseColumns {
	
	}

	/**
	 * CallLog wrapper class for content provider
	 */
	public static final class CallLog implements BaseColumns {
	
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
