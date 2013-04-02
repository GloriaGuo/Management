package com.parent.management.monitor;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.ContentValues;
import android.content.Context;
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
    
    class AppsUsedInfo {
        private String appname = "";
        private String pname = "";
        private int date = 0;
        private String action = "";
        private void prettyPrint() {
            Log.v(TAG, "appname:" + appname);
            Log.v(TAG, "pname:" + pname);
            Log.v(TAG, "date:" + date);
            Log.v(TAG, "action:" + action);
            Log.v(TAG, "----------------------");
        }
    }

    private ArrayList<AppsUsedInfo> getCurrentAppsUsedInfo(boolean ifGetSysPackages) {
    	ArrayList<AppsUsedInfo> res = new ArrayList<AppsUsedInfo>();

        ActivityManager activityManager = (ActivityManager)
                ManagementApplication.getContext().getSystemService("activity");
        List<RecentTaskInfo> taskList = activityManager.getRecentTasks(Integer.MAX_VALUE,0);
        if ((taskList != null) && (taskList.size() > 0)) {
            for (RecentTaskInfo taskInfo : taskList) {
                AppsUsedInfo newInfo = new AppsUsedInfo();
                newInfo.pname = taskInfo.baseIntent.getComponent().getPackageName();

                String[] AppsInstalledProj = new String[] {
                        ManagementProvider.AppsInstalled.APP_NAME
                        };
                String AppsInstalledSel = ManagementProvider.AppsInstalled.PACKAGE_NAME
                        + " = \"" + newInfo.pname + "\"";
                Cursor appsInstalledCur = null;
                appsInstalledCur = ManagementApplication.getContext().getContentResolver().query(
                        ManagementProvider.AppsInstalled.CONTENT_URI,
                        AppsInstalledProj, AppsInstalledSel, null, null);

                if (appsInstalledCur != null && appsInstalledCur.moveToFirst() && appsInstalledCur.getCount() > 0) {
                    newInfo.appname = appsInstalledCur.getString(
                            appsInstalledCur.getColumnIndex(ManagementProvider.AppsInstalled.APP_NAME));
                }
                else {
                    newInfo.appname = "";
                }
                
                newInfo.date = 0;
                newInfo.action = taskInfo.baseIntent.getAction();
                res.add(newInfo);
            }
        }
    	
        return res; 
    }
    
    private boolean checkForChange(ArrayList<AppsUsedInfo> currentInfoList) {
        Cursor appsUsedCur = null;
    	for (AppsUsedInfo info : currentInfoList) {
    		info.prettyPrint();
            String[] appsUsedProj = new String[] {
                    ManagementProvider.AppsUsed.APP_NAME,
                    ManagementProvider.AppsUsed.DATE,
                    ManagementProvider.AppsUsed.ACTION
                    };
            String appsUsedSel = ManagementProvider.AppsUsed.PACKAGE_NAME + " = \"" + info.pname + "\"";
            appsUsedCur = ManagementApplication.getContext().getContentResolver().query(
                    ManagementProvider.AppsUsed.CONTENT_URI,
                    appsUsedProj, appsUsedSel, null, null);

            if (appsUsedCur != null && appsUsedCur.moveToFirst() && appsUsedCur.getCount() > 0) {
                final ContentValues values = new ContentValues();
                values.put(ManagementProvider.AppsUsed.APP_NAME, info.appname);
                values.put(ManagementProvider.AppsUsed.DATE, info.date);
                values.put(ManagementProvider.AppsUsed.ACTION, info.action);
                values.put(ManagementProvider.AppsUsed.IS_SENT, ManagementProvider.IS_SENT_NO);
                
                ManagementApplication.getContext().getContentResolver().update(
                        ManagementProvider.AppsUsed.CONTENT_URI,
                        values,
                        ManagementProvider.AppsUsed.PACKAGE_NAME + "=\"" + info.pname +"\"",
                        null);
                Log.v(TAG, "update one");
            } else {
                final ContentValues values = new ContentValues();
                values.put(ManagementProvider.AppsUsed.APP_NAME, info.appname);
                values.put(ManagementProvider.AppsUsed.PACKAGE_NAME, info.pname);
                values.put(ManagementProvider.AppsUsed.DATE, info.date);
                values.put(ManagementProvider.AppsUsed.ACTION, info.action);
                
                ManagementApplication.getContext().getContentResolver().insert(
                        ManagementProvider.AppsUsed.CONTENT_URI, values);
                Log.v(TAG, "insert one");
            }
            if (null != appsUsedCur) {
                appsUsedCur.close();
                appsUsedCur = null;
            }
    	}

        return true;
    }

    @Override
    public void startMonitoring() {
        // init the first data
        checkForChange(getCurrentAppsUsedInfo(false));
        Log.v(TAG, "---->started");
    }

    @Override
    public void stopMonitoring() {
        // don't need to do anything
    }

    @Override
    public JSONArray extractDataForSend() {
        try {
            JSONArray data = new JSONArray();

            String[] AppsUsedProj = new String[] {
                    ManagementProvider.AppsUsed.APP_NAME,
                    ManagementProvider.AppsUsed.PACKAGE_NAME,
                    ManagementProvider.AppsUsed.DATE,
                    ManagementProvider.AppsUsed.ACTION
                    };
            String AppsUsedSel = ManagementProvider.AppsUsed.IS_SENT
                    + " = \"" + ManagementProvider.IS_SENT_NO + "\"";
            Cursor appsUsedCur = null;
            appsUsedCur = ManagementApplication.getContext().getContentResolver().query(
                    ManagementProvider.AppsUsed.CONTENT_URI,
                    AppsUsedProj, AppsUsedSel, null, null);

            if (appsUsedCur == null) {
                Log.v(TAG, "open browserHistory native failed");
                return null;
            }
            if (appsUsedCur.moveToFirst() && appsUsedCur.getCount() > 0) {
                while (appsUsedCur.isAfterLast() == false) {
                    String an = appsUsedCur.getString(
                            appsUsedCur.getColumnIndex(ManagementProvider.AppsUsed.APP_NAME));
                    String pn = appsUsedCur.getString(
                            appsUsedCur.getColumnIndex(ManagementProvider.AppsUsed.PACKAGE_NAME));
                    int date = appsUsedCur.getInt(
                            appsUsedCur.getColumnIndex(ManagementProvider.AppsUsed.DATE));
                    String action = appsUsedCur.getString(
                            appsUsedCur.getColumnIndex(ManagementProvider.AppsUsed.ACTION));
                    JSONObject raw = new JSONObject();
                    raw.put(ManagementProvider.AppsUsed.APP_NAME, an);
                    raw.put(ManagementProvider.AppsUsed.PACKAGE_NAME, pn);
                    raw.put(ManagementProvider.AppsUsed.DATE, date);
                    raw.put(ManagementProvider.AppsUsed.ACTION, action);

                    data.put(raw);
                    appsUsedCur.moveToNext();
                }
            }
            if (null != appsUsedCur) {
                appsUsedCur.close();
            }
            
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
    }

}
