package com.parent.management.monitor;

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

import com.parent.management.ManagementApplication;
import com.parent.management.db.ManagementProvider;

public class AppsUsedMonitor extends Monitor {
    private static final String TAG = ManagementApplication.getApplicationTag() + "." +
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
        AppUsedInfo newInfo = new AppUsedInfo();

        ActivityManager activityManager = (ActivityManager)ManagementApplication.getContext().getSystemService(
                Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo>  taskList = activityManager.getRunningTasks(1);
        if ((taskList != null) && (taskList.size() > 0)) {
            
            RunningTaskInfo taskInfo = (RunningTaskInfo) taskList.get(0);

            if (ManagementApplication.getContext().getPackageName() != taskInfo.topActivity.getPackageName()) {
                newInfo.pname = taskInfo.topActivity.getPackageName();
                
                PackageManager pm = ManagementApplication.getContext().getPackageManager();
                ApplicationInfo appInfo = null;
                try {
                    appInfo = pm.getApplicationInfo(newInfo.pname, 0);
                } catch (NameNotFoundException e) {
                    Log.e(TAG, "Get app info failed from package name: " + e.getMessage());
                }
                newInfo.appname = (String) pm.getApplicationLabel(appInfo);
                
                newInfo.date = System.currentTimeMillis();
            }
        }
    	
        return newInfo; 
    }
    
    private void checkForChange() {
        AppUsedInfo currentActiveApp = getCurrentActiveApp();
        if (currentActiveApp.pname.equals(ManagementApplication.getContext().getPackageName())) {
        	return;
        }
        
        Cursor appsUsedCur = null;
    	
        String[] appsUsedProj = new String[] {
                ManagementProvider.AppsUsed.APP_NAME,
                ManagementProvider.AppsUsed.PACKAGE_NAME,
                ManagementProvider.AppsUsed.DATE,
                };
        String orderBy = ManagementProvider.AppsUsed.DATE + " DESC"; 
        appsUsedCur = ManagementApplication.getContext().getContentResolver().query(
                ManagementProvider.AppsUsed.CONTENT_URI,
                appsUsedProj, null, null, orderBy);

        if (appsUsedCur == null) {
            Log.v(TAG, "open app used db failed");
            return;
        }
        
        String previousPackage = "";
        if (appsUsedCur.moveToFirst()) {
            previousPackage = appsUsedCur.getString(appsUsedCur.getColumnIndex(
                    ManagementProvider.AppsUsed.PACKAGE_NAME));
        }
        if (!appsUsedCur.moveToFirst() || !previousPackage.equals(currentActiveApp.pname)) {
            final ContentValues values = new ContentValues();
            values.put(ManagementProvider.AppsUsed.APP_NAME, currentActiveApp.appname);
            values.put(ManagementProvider.AppsUsed.PACKAGE_NAME, currentActiveApp.pname);
            values.put(ManagementProvider.AppsUsed.DATE, currentActiveApp.date);
            
            ManagementApplication.getContext().getContentResolver().insert(
                    ManagementProvider.AppsUsed.CONTENT_URI, values);
            
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
                    ManagementProvider.AppsUsed.APP_NAME,
                    ManagementProvider.AppsUsed.PACKAGE_NAME,
                    ManagementProvider.AppsUsed.DATE,
                    };
            String AppsUsedSel = ManagementProvider.AppsUsed.IS_SENT
                    + " = \"" + ManagementProvider.IS_SENT_NO + "\"";
            Cursor appsUsedCur = null;
            appsUsedCur = ManagementApplication.getContext().getContentResolver().query(
                    ManagementProvider.AppsUsed.CONTENT_URI,
                    AppsUsedProj, AppsUsedSel, null, null);

            if (appsUsedCur == null) {
                Log.v(TAG, "open appUsed db failed");
                return null;
            }
            if (appsUsedCur.moveToFirst() && appsUsedCur.getCount() > 0) {
                while (appsUsedCur.isAfterLast() == false) {
                    String an = appsUsedCur.getString(
                            appsUsedCur.getColumnIndex(ManagementProvider.AppsUsed.APP_NAME));
                    String pn = appsUsedCur.getString(
                            appsUsedCur.getColumnIndex(ManagementProvider.AppsUsed.PACKAGE_NAME));
                    long date = appsUsedCur.getLong(
                            appsUsedCur.getColumnIndex(ManagementProvider.AppsUsed.DATE));
                    JSONObject raw = new JSONObject();
                    raw.put(ManagementProvider.AppsUsed.APP_NAME, an);
                    raw.put(ManagementProvider.AppsUsed.PACKAGE_NAME, pn);
                    raw.put(ManagementProvider.AppsUsed.DATE, date);

                    data.put(raw);
                    appsUsedCur.moveToNext();
                }
            }
            if (null != appsUsedCur) {
                appsUsedCur.close();
            }
            
            Log.v(TAG, "data === " + data.toString());
            
            final ContentValues values = new ContentValues();
            values.put(ManagementProvider.AppsUsed.IS_SENT, ManagementProvider.IS_SENT_YES);
            ManagementApplication.getContext().getContentResolver().update(
                    ManagementProvider.AppsUsed.CONTENT_URI,
                    values,
                    ManagementProvider.AppsUsed.IS_SENT + "=\"" + ManagementProvider.IS_SENT_NO +"\"",
                    null);
            
            return data;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
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
    				String pname = obj.optString(ManagementProvider.AppsUsed.PACKAGE_NAME);
    		        final ContentValues values = new ContentValues();
    		        values.put(ManagementProvider.AppsUsed.IS_SENT, ManagementProvider.IS_SENT_NO);
    		        ManagementApplication.getContext().getContentResolver().update(
    		        		ManagementProvider.AppsUsed.CONTENT_URI,
    		                values,
    		                ManagementProvider.AppsUsed.PACKAGE_NAME + "=\"" + pname +"\"",
    		                null);
    			}
    		}
    	}
        String appsUsedSel = ManagementProvider.AppsUsed.IS_SENT
        		+ " = \"" + ManagementProvider.IS_SENT_YES + "\"";
    	ManagementApplication.getContext().getContentResolver().delete(
    			ManagementProvider.AppsUsed.CONTENT_URI,
    			appsUsedSel, null);
    }

}
