package com.parent.management.db;

import java.io.File;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.parent.management.ManagementApplication;
import com.streamwide.vvmclient.android.R;
import com.streamwide.vvmclient.android.storage.Message;
import com.streamwide.vvmclient.android.storage.VVMProvider.Messages;
import com.streamwide.vvmclient.android.tools.VVMApplication;

public class ManagementProvider extends ContentProvider {
	
	/**
	 * ParentManagement authority for content URIs
	 */
	public static final String AUTHORITY = "com.parent.provider.management";
	public static final String DATABASE_NAME = "MANAGEMENT";
	public static final int DATABASE_VERSION = 1;
	public static final String TAG = "Management.Provider";
	public static final String EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory() + "/" + ".management";
	
	/**
	 * BrowserHistory wrapper class for content provider
	 */
	public static final class BrowserHistory implements BaseColumns {
        public static final String TABLE_NAME = "BrowserHistory";
        public static final String ID = "Id";
        public static final String URL = "URL";
        public static final String TITLE = "Title";
        public static final String VISIT_COUNT = "Visitcount";
	    public static final String LAST_VISIT = "Lastvisit";
		public static final String IS_SENT = "IsSend";
	}
	
	/**
	 * GpsInfo wrapper class for content provider
	 */
	public static final class Gps implements BaseColumns {
        public static final String TABLE_NAME = "Gps";
        public static final String LATIDUDE = "Latitude";
        public static final String LONGITUDE = "Longitude";
        public static final String SPEED = "Speed";
        public static final String TIME = "Time";
	}
	
	/**
	 * InstalledApps wrapper class for content provider
	 */
	public static final class AppsInstalled implements BaseColumns {
        public static final String TABLE_NAME = "AppsInstalled";
	}
	
	/**
	 * AppsUsedInfo wrapper class for content provider
	 */
	public static final class AppsUsed implements BaseColumns {
        public static final String TABLE_NAME = "AppsUsed";
	
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

	private static class ManagementDatabaseHelper extends SQLiteOpenHelper {
		// Set to false to fall back to internal db
		private final static boolean EXTERNAL_DB = true;

		private SQLiteDatabase mDatabase = null;
		private boolean mIsInitializing = false;

//		private String getDatabaseName(){
//			return VVMProvider.getStoragePath() + "/" + DATABASE_NAME +".sqlite";
//		}

		ManagementDatabaseHelper(final Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			if (! EXTERNAL_DB) {
				return;
			}
		}

		private static final String INTEGER = " INTEGER ";
		private static final String TEXT = " TEXT ";
		private static final String COMMA = ",";

		@Override
		public void onCreate(final SQLiteDatabase db) {

			try {
			    createBrowserHistoryTable(db);
			    createGpsTable(db);
			    createAppsInstalledTable(db);
			    createAppsUsedTable(db);
			} catch (SQLException sqle) {
				Log.e(TAG, "unable to create Message content provider : "
						+ sqle.getMessage());
				throw sqle;
			}
		}

        private void createBrowserHistoryTable(final SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + BrowserHistory.TABLE_NAME + " ("
                + BrowserHistory.ID + INTEGER + "PRIMARY KEY,"
                + BrowserHistory.URL + TEXT + COMMA
                + BrowserHistory.TITLE + TEXT + COMMA
                + BrowserHistory.VISIT_COUNT + INTEGER + COMMA 
                + BrowserHistory.LAST_VISIT + INTEGER + COMMA                
                + BrowserHistory.IS_SENT + INTEGER + COMMA
                + ");");
        }
        
        private void createGpsTable(final SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + Gps.TABLE_NAME + " ("
                + Gps.LATIDUDE  + INTEGER + "PRIMARY KEY,"
                + Gps.LONGITUDE + TEXT + COMMA 
                + Gps.SPEED + TEXT + COMMA 
                + Gps.TIME + INTEGER + COMMA
                + ");");
        }
        
        private void createAppsInstalledTable(final SQLiteDatabase db) {
        }

        private void createAppsUsedTable(final SQLiteDatabase db) {
        }
        
        private void clearDB(final SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + BrowserHistory.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + Gps.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + AppsInstalled.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + AppsUsed.TABLE_NAME);
        }
        
        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
                final int newVersion) {
//            if (newVersion < oldVersion) {
//                // DOWNGRADE:
//                Log.w(TAG, "Downgrading database from version " + oldVersion
//                    + "to version " + newVersion);
//                // lets drop all
//                clearDB(db);
//                // and recreate everything
//                onCreate(db);
//            } else if (oldVersion < newVersion) {
//                // UPGRADE:
//                Log.w(TAG, "Upgrading database from version " + oldVersion
//                    + "to version " + newVersion);
//                // Database version 4+: added greeting active status (on upgrade, keep messages)
//                Log.w(TAG, "Upgrading to DBv4+, re-creating greeting table");
//                // drop greetings table
//                db.execSQL("DROP TABLE IF EXISTS " + GREETINGS_TABLE_NAME);
//                // create new greetings table
//                createGreetingsTable(db);
//                
//             // Database version 5+: added media library database
//                Log.w(TAG, "Upgrading to DBv5+, creating media library table");
//                db.execSQL("DROP TABLE IF EXISTS " + MEDIALIB_TABLE_NAME);
//                createMediaLibTable(db);
//            }
        	
            // lets drop all
            clearDB(db);
            // and recreate everything
            onCreate(db);

        }
        
		synchronized void reset() {
			Log.i(TAG, "OpenHelper: reset");
			mDatabase = null;
			mIsInitializing = false;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.sqlite.SQLiteOpenHelper#getReadableDatabase()
		 */
		@Override
		public synchronized SQLiteDatabase getReadableDatabase() {
			if (! EXTERNAL_DB) {
				return super.getReadableDatabase();
			}

			final String mount = Environment.getExternalStorageState();
			if (ManagementApplication.DEBUG) {
				Log.d(TAG, 
						"getReadableDatabase, checking external storage: " + mount);
			}
			if (!Environment.MEDIA_MOUNTED.equals(mount)) {
				// lost our storage, reset everything
				reset();
				return null;
			}

			if (mDatabase != null && mDatabase.isOpen()) {
				return mDatabase; // The database is already open for business
			}

			if (mIsInitializing) {
				throw new IllegalStateException("getReadableDatabase called recursively");
			}

			try {
				return getWritableDatabase();
			} catch (SQLiteException e) {
				Log.e(TAG, "Couldn't open " + " for writing (will try read-only):", e);
			}

			SQLiteDatabase db = null;
			try {
				mIsInitializing = true;
				// Create external storage path if needed
				final File target = new File(ManagementProvider.EXTERNAL_STORAGE_PATH);
				target.mkdirs();
				db = SQLiteDatabase.openDatabase(getDatabaseName(), null, SQLiteDatabase.OPEN_READONLY);
				if (db.getVersion() != DATABASE_VERSION) {
					Log.e(TAG, "Can't upgrade read-only database from version " + db.getVersion() + " to "
							+ DATABASE_VERSION + ": " + getDatabaseName());
					return null;
				}
				onOpen(db);
				Log.w(TAG, "Opened " + getDatabaseName() + " in read-only mode");
				mDatabase = db;
			} catch (SQLiteException sqle) {
				reset();
			} finally {
				mIsInitializing = false;
			}
			if (db != null && ! db.equals(mDatabase)) {
				db.close();
			}
			return mDatabase;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.sqlite.SQLiteOpenHelper#getWritableDatabase()
		 */
		@Override
		public synchronized SQLiteDatabase getWritableDatabase() {
			if (! EXTERNAL_DB) {
				return super.getWritableDatabase();
			}

			final String mount = Environment.getExternalStorageState();
			if (ManagementApplication.DEBUG) {
				Log.d(TAG, "getWritableDatabase, checking external storage: " + mount);
			}
			if (!Environment.MEDIA_MOUNTED.equals(mount)) {
				// lost our storage, reset everything
				reset();
				return null;
			}

			if (mDatabase != null && mDatabase.isOpen() && !mDatabase.isReadOnly()) {
				return mDatabase; // The database is already open for business
			}

			if (mIsInitializing) {
				throw new IllegalStateException("getWritableDatabase called recursively");
			}

			// If we have a read-only database open, someone could be using it
			// (though they shouldn't), which would cause a lock to be held on
			// the file, and our attempts to open the database read-write would
			// fail waiting for the file lock. To prevent that, we acquire the
			// lock on the read-only database, which shuts out other users.

			boolean success = false;
			SQLiteDatabase db = null;
			try {
				mIsInitializing = true;
				// Create external storage path if needed
				final File target = new File(ManagementProvider.EXTERNAL_STORAGE_PATH);
				target.mkdirs();
				db = SQLiteDatabase.openOrCreateDatabase(ManagementProvider.getStoragePath() + "/" + DATABASE_NAME +".sqlite", null);
				final int version = db.getVersion();
				if (version != DATABASE_VERSION) {
					db.beginTransaction();
					try {
						if (version == 0) {
							onCreate(db);
						} else {
							onUpgrade(db, version, DATABASE_VERSION);
						}
						db.setVersion(DATABASE_VERSION);
						db.setTransactionSuccessful();
					} finally {
						db.endTransaction();
					}
				}

				onOpen(db);
				success = true;
				
				return db;
			} finally {
				mIsInitializing = false;
				if (success) {
					if (mDatabase != null) {
						mDatabase.close();
					}
					mDatabase = db;
				} else {
					if (db != null) {
						db.close();
					}
				}
			}
		}
	}
	
	private ManagementDatabaseHelper mOpenHelper;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	public static String getStoragePath() {
		// TODO Selecet Internal or external storage 
//		if("internal".equals(
//				ManagementApplication.getServiceConfiguration().getParameter("storage_type"))) {
//			// Use internal storage
//			return ManagementApplication.getInternalPath();
//		} else {
//			return EXTERNAL_STORAGE_PATH;
//		}
		return EXTERNAL_STORAGE_PATH;
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
	private Uri insertInBrowserHistory(final ContentValues initialValues){
		ContentValues values;
		if (null == initialValues  || null == mOpenHelper ) {
			return null;
		} else {
			values = new ContentValues(initialValues);
		}

		final Long now = Long.valueOf(System.currentTimeMillis());
		

		try {
			final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			final long rowId = db.insert(BrowserHistory.TABLE_NAME, null, values);
			if (rowId > 0) {
				final Uri insertUri = ContentUris.withAppendedId( Messages.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(insertUri, null);
				return insertUri;
			}
		} catch (NullPointerException npe) {
			//we catch npe that may happen if the sd card is not present
			Log.e(TAG, "NullPointerException while trying to insert entry in " + BrowserHistory.TABLE_NAME +" database");
        } catch (SQLiteException sqlioe) {
            //we catch disk io that may happen if SD card full or faulty
            Log.e(TAG, "SQLiteException  while trying to insert entry in " + BrowserHistory.TABLE_NAME +" database");
            // arm flag indicating that the application cannot write the db
//            VVMApplication.readOnlyMode(true);
        }
		return null;
	}

	@Override
	public boolean onCreate() {
		try {
			mOpenHelper = new ManagementDatabaseHelper(getContext());
		} catch (SQLiteDiskIOException e) {
			return false;
		}
		return true;
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
