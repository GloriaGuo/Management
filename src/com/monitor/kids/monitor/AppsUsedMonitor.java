package com.monitor.kids.monitor;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.util.Log;

import com.monitor.kids.KidsApplication;
import com.monitor.kids.db.KidsProvider;

public class AppsUsedMonitor extends Monitor {
    private static final String TAG = KidsApplication.getApplicationTag() + "." +
            AppsUsedMonitor.class.getSimpleName();

    public AppsUsedMonitor(Context context) {
        super(context);
    }
    
    class AppUsedInfo {
        private String appname = "";
        private String pname = "";
        private long date = 0;
        private void prettyPrint() {
            Log.v(TAG, "appname:" + appname);
            Log.v(TAG, "pname:" + pname);
            Log.v(TAG, "date:" + date);
            Log.v(TAG, "----------------------");
        }
    }

    private AppUsedInfo getCurrentActiveApp() {
        AppUsedInfo newInfo = null;

        ActivityManager activityManager = (ActivityManager)KidsApplication.getContext().getSystemService(
                Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo>  taskList = activityManager.getRunningTasks(1);
        
        if ((taskList != null) && (taskList.size() > 0)) {
            RunningTaskInfo taskInfo = (RunningTaskInfo) taskList.get(0);
            if (isNeedFilterOut(taskInfo.topActivity.getPackageName())) {
                return null;
            }
            
            newInfo = new AppUsedInfo();
            newInfo.pname = taskInfo.topActivity.getPackageName();
            
            PackageManager pm = KidsApplication.getContext().getPackageManager();
            ApplicationInfo appInfo = null;
            try {
                appInfo = pm.getApplicationInfo(newInfo.pname, 0);
            } catch (NameNotFoundException e) {
                Log.e(TAG, "Get app info failed from package name: " + e.getMessage());
            }
            newInfo.appname = (String) pm.getApplicationLabel(appInfo);
            
            newInfo.date = System.currentTimeMillis();
        }
    	
        return newInfo; 
    }
    
    private boolean isNeedFilterOut(String packageName) {
        if (KidsApplication.getContext().getPackageName() == packageName) {
            return true;
        }
        if(packageName.toLowerCase().contains("launcher")) {
            // TODO: should be configured in xml
            return true;
        }
        return false;
    }
    
    private void checkForChange() {
        AppUsedInfo currentActiveApp = getCurrentActiveApp();
        if (null == currentActiveApp) {
        	return;
        }
        
        Cursor appsUsedCur = null;
    	
        String[] appsUsedProj = new String[] {
                KidsProvider.AppsUsed.APP_NAME,
                KidsProvider.AppsUsed.PACKAGE_NAME,
                KidsProvider.AppsUsed.DATE,
                };
        String orderBy = KidsProvider.AppsUsed.DATE + " DESC"; 
        appsUsedCur = KidsApplication.getContext().getContentResolver().query(
                KidsProvider.AppsUsed.CONTENT_URI,
                appsUsedProj, null, null, orderBy);

        if (appsUsedCur == null) {
            Log.v(TAG, "open app used db failed");
            return;
        }
        
        String previousPackage = "";
        if (appsUsedCur.moveToFirst()) {
            previousPackage = appsUsedCur.getString(appsUsedCur.getColumnIndex(
                    KidsProvider.AppsUsed.PACKAGE_NAME));
        }
        if (!appsUsedCur.moveToFirst() || !previousPackage.equals(currentActiveApp.pname)) {
            final ContentValues values = new ContentValues();
            values.put(KidsProvider.AppsUsed.APP_NAME, currentActiveApp.appname);
            values.put(KidsProvider.AppsUsed.PACKAGE_NAME, currentActiveApp.pname);
            values.put(KidsProvider.AppsUsed.DATE, currentActiveApp.date);
            
            KidsApplication.getContext().getContentResolver().insert(
                    KidsProvider.AppsUsed.CONTENT_URI, values);
            
            currentActiveApp.prettyPrint();
            Log.d(TAG, "insert one");
        }

        if (null != appsUsedCur) {
            appsUsedCur.close();
            appsUsedCur = null;
        }

        return;
    }

    @Override
    public void startMonitoring() {
        // init the first data
        this.monitorStatus = true;
        checkForChange();
    }

    @Override
    public void stopMonitoring() {
        // don't need to do anything
        this.monitorStatus = false;
    }

    @Override
    public JSONArray extractDataForSend() {
        try {
            JSONArray data = new JSONArray();

            String[] AppsUsedProj = new String[] {
            		KidsProvider.AppsUsed._ID,
                    KidsProvider.AppsUsed.APP_NAME,
                    KidsProvider.AppsUsed.PACKAGE_NAME,
                    KidsProvider.AppsUsed.DATE,
                    };
            String AppsUsedSel = KidsProvider.AppsUsed.IS_SENT
                    + " = \"" + KidsProvider.IS_SENT_NO + "\"";
            Cursor appsUsedCur = null;
            appsUsedCur = KidsApplication.getContext().getContentResolver().query(
                    KidsProvider.AppsUsed.CONTENT_URI,
                    AppsUsedProj, AppsUsedSel, null, null);

            if (appsUsedCur == null) {
                Log.v(TAG, "open appUsed db failed");
                return null;
            }
            if (appsUsedCur.moveToFirst() && appsUsedCur.getCount() > 0) {
                while (appsUsedCur.isAfterLast() == false) {
                	long id = appsUsedCur.getLong(appsUsedCur.getColumnIndex(KidsProvider.AppsUsed._ID));
                    String an = appsUsedCur.getString(
                            appsUsedCur.getColumnIndex(KidsProvider.AppsUsed.APP_NAME));
                    String pn = appsUsedCur.getString(
                            appsUsedCur.getColumnIndex(KidsProvider.AppsUsed.PACKAGE_NAME));
                    long date = appsUsedCur.getLong(
                            appsUsedCur.getColumnIndex(KidsProvider.AppsUsed.DATE));
                    JSONObject raw = new JSONObject();
                    raw.put(KidsProvider.AppsUsed._ID, id);
                    raw.put(KidsProvider.AppsUsed.APP_NAME, an);
                    raw.put(KidsProvider.AppsUsed.PACKAGE_NAME, pn);
                    raw.put(KidsProvider.AppsUsed.DATE, date);

                    data.put(raw);
                    appsUsedCur.moveToNext();
                }
            }
            if (null != appsUsedCur) {
                appsUsedCur.close();
            }
            
            Log.v(TAG, "data === " + data.toString());
            
            final ContentValues values = new ContentValues();
            values.put(KidsProvider.AppsUsed.IS_SENT, KidsProvider.IS_SENT_YES);
            KidsApplication.getContext().getContentResolver().update(
                    KidsProvider.AppsUsed.CONTENT_URI,
                    values,
                    KidsProvider.AppsUsed.IS_SENT + "=\"" + KidsProvider.IS_SENT_NO +"\"",
                    null);
            
            return data;
        } catch (JSONException e) {
        	Log.e(TAG, "Upload error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    @Override
    public void updateStatusAfterSend(JSONArray failedList) {
    	if (null != failedList && failedList.length() != 0) {
    		for (int i = 0; i < failedList.length(); ++i) {
    			JSONObject obj = failedList.optJSONObject(i);
    			if (null != obj) {
    				long id = obj.optLong(KidsProvider.AppsUsed._ID);
    		        final ContentValues values = new ContentValues();
    		        values.put(KidsProvider.AppsUsed.IS_SENT, KidsProvider.IS_SENT_NO);
    		        KidsApplication.getContext().getContentResolver().update(
    		        		KidsProvider.AppsUsed.CONTENT_URI,
    		                values,
    		                KidsProvider.AppsUsed._ID + "=\"" + id +"\"",
    		                null);
    			}
    		}
    	}
        String appsUsedSel = KidsProvider.AppsUsed.IS_SENT
        		+ " = \"" + KidsProvider.IS_SENT_YES + "\"";
    	KidsApplication.getContext().getContentResolver().delete(
    			KidsProvider.AppsUsed.CONTENT_URI,
    			appsUsedSel, null);
    }

}
