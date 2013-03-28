package com.parent.management.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.util.Log;

import com.parent.management.ManagementApplication;
import com.parent.management.db.ManagementProvider;

public class AppsInstalledMonitor extends Monitor {
    private static final String TAG = ManagementApplication.getApplicationTag() + "." +
            BrowserHistoryMonitor.class.getSimpleName();

    public AppsInstalledMonitor(Context context) {
        super(context);
    }
    
    class AppsInstalledInfo {
        private String appname = "";
        private String pname = "";
        private String versionName = "";
        private int versionCode = 0;
        private String dataDir = "";
        private String sourceDir = "";
        private void prettyPrint() {
            Log.v(TAG, "appname:" + appname);
            Log.v(TAG, "pname:" + pname);
            Log.v(TAG, "versionName:" + versionName);
            Log.v(TAG, "versionCode:" + versionCode);
            Log.v(TAG, "dataDir:" + dataDir);
            Log.v(TAG, "sourceDir:" + sourceDir);
            Log.v(TAG, "----------------------");
        }
    }

    private ArrayList<AppsInstalledInfo> getCurrentAppsInfo(boolean ifGetSysPackages) {
    	ArrayList<AppsInstalledInfo> res = new ArrayList<AppsInstalledInfo>();
        PackageManager packageManager = ManagementApplication.getContext().getPackageManager();
        List<PackageInfo> packs = packageManager.getInstalledPackages(0);
        for(int i=0;i < packs.size();i++) {
            PackageInfo p = packs.get(i);
            if ((!ifGetSysPackages) && (p.versionName == null)) {
                continue ;
            }
            AppsInstalledInfo newInfo = new AppsInstalledInfo();
            newInfo.appname = p.applicationInfo.loadLabel(packageManager).toString();
            newInfo.pname = p.packageName;
            newInfo.versionName = p.versionName;
            newInfo.versionCode = p.versionCode;
            newInfo.dataDir = p.applicationInfo.dataDir;
            newInfo.sourceDir = p.applicationInfo.sourceDir;
//            newInfo.prettyPrint();
            res.add(newInfo);
        }
        return res; 
    }
    
    private boolean mergeToDb(ArrayList<AppsInstalledInfo> currentInfoList) {

        String curPackageName = "";
        String curAppName = "";
        String curVersionName = "";
        Integer curVersionCode = 0;
    	for (AppsInstalledInfo info : currentInfoList) {
    		info.prettyPrint();
            String[] appsInstalledProj = new String[] {
                    ManagementProvider.AppsInstalled.APP_NAME,
                    ManagementProvider.AppsInstalled.PACKAGE_NAME,
                    ManagementProvider.AppsInstalled.VERSION_NAME,
                    ManagementProvider.AppsInstalled.VERSION_CODE};
            String appsInstalledSel = ManagementProvider.AppsInstalled.PACKAGE_NAME + " = " + info.pname;
            Cursor appsInstalledCur = ManagementApplication.getContext().getContentResolver().query(
                    ManagementProvider.AppsInstalled.CONTENT_URI,
                    appsInstalledProj, appsInstalledSel, null, null);
            
            if (appsInstalledCur == null) {
                Log.v(TAG, "open appsInstalled failed");
                return false;
            }

            if (appsInstalledCur.moveToFirst() && appsInstalledCur.getCount() > 0) {
                curAppName = appsInstalledCur.getString(appsInstalledCur.getColumnIndex(
                        ManagementProvider.AppsInstalled.APP_NAME));
                curVersionName = appsInstalledCur.getString(appsInstalledCur.getColumnIndex(
                        ManagementProvider.AppsInstalled.VERSION_NAME));
                curVersionCode = appsInstalledCur.getInt(appsInstalledCur.getColumnIndex(
                        ManagementProvider.AppsInstalled.VERSION_CODE));
                if ( curAppName != info.appname || curVersionName != info.versionName
                		|| curVersionCode != info.versionCode) {
                    final ContentValues values = new ContentValues();
                    values.put(ManagementProvider.AppsInstalled.APP_NAME, info.appname);
                    values.put(ManagementProvider.AppsInstalled.VERSION_NAME, info.versionName);
                    values.put(ManagementProvider.AppsInstalled.VERSION_CODE, info.versionCode);
                    values.put(ManagementProvider.AppsInstalled.IS_SENT, ManagementProvider.IS_SENT_NO);
                    
                    ManagementApplication.getContext().getContentResolver().update(
                            ManagementProvider.AppsInstalled.CONTENT_URI,
                            values,
                            ManagementProvider.AppsInstalled.PACKAGE_NAME + "=\"" + info.pname +"\"",
                            null);
                    Log.v(TAG, "update one");
                }
            } else {
                final ContentValues values = new ContentValues();
                values.put(ManagementProvider.AppsInstalled.PACKAGE_NAME, info.pname);
                values.put(ManagementProvider.AppsInstalled.APP_NAME, info.appname);
                values.put(ManagementProvider.AppsInstalled.VERSION_NAME, info.versionName);
                values.put(ManagementProvider.AppsInstalled.VERSION_CODE, info.versionCode);
                
                ManagementApplication.getContext().getContentResolver().insert(
                        ManagementProvider.AppsInstalled.CONTENT_URI, values);
                Log.v(TAG, "insert one");
            }

            appsInstalledCur.close();
    	}
        
        return true;
    }

    @Override
    public void startMonitoring() {
        // init the first data
        mergeToDb(getCurrentAppsInfo(false));
    }

    @Override
    public void stopMonitoring() {
        // don't need to do anything
    }

    @Override
    public Cursor extraData() {
        return null;
    }

}
