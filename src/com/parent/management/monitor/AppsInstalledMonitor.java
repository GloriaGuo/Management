package com.parent.management.monitor;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.parent.management.ManagementApplication;
import com.parent.management.db.ManagementProvider;

public class AppsInstalledMonitor extends Monitor {
    private static final String TAG = ManagementApplication.getApplicationTag() + "." +
            AppsInstalledMonitor.class.getSimpleName();
    
    ArrayList<AppsInstalledInfo> mCurrentInfoList = null;

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
        private Boolean isSystemPackage = true;
        private void prettyPrint() {
            Log.v(TAG, "appname:" + appname);
            Log.v(TAG, "pname:" + pname);
            Log.v(TAG, "versionName:" + versionName);
            Log.v(TAG, "versionCode:" + versionCode);
            Log.v(TAG, "dataDir:" + dataDir);
            Log.v(TAG, "sourceDir:" + sourceDir);
            Log.v(TAG, "isSystemPackage:" + isSystemPackage);
            Log.v(TAG, "----------------------");
        }
    }

    private ArrayList<AppsInstalledInfo> getCurrentAppsInfo() {
    	ArrayList<AppsInstalledInfo> res = new ArrayList<AppsInstalledInfo>();
        PackageManager packageManager = ManagementApplication.getContext().getPackageManager();
        List<PackageInfo> packs = packageManager.getInstalledPackages(0);
        for(int i=0;i < packs.size();i++) {
            PackageInfo p = packs.get(i);
            if (p.versionName == null) {
                continue ;
            }
            AppsInstalledInfo newInfo = new AppsInstalledInfo();
            newInfo.appname = p.applicationInfo.loadLabel(packageManager).toString();
            newInfo.pname = p.packageName;
            newInfo.versionName = p.versionName;
            newInfo.versionCode = p.versionCode;
            newInfo.dataDir = p.applicationInfo.dataDir;
            newInfo.sourceDir = p.applicationInfo.sourceDir;
            if ((p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)==0)
            {
                //is not a system app
                newInfo.isSystemPackage = false;
            }
            res.add(newInfo);
        }
        return res; 
    }

    @Override
    public void startMonitoring() {
        // init the first data
        mCurrentInfoList = getCurrentAppsInfo();
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

            for (AppsInstalledInfo info : mCurrentInfoList) {
                info.prettyPrint();

                JSONObject raw = new JSONObject();
                raw.put(ManagementProvider.AppsInstalled.APP_NAME, info.appname);
                raw.put(ManagementProvider.AppsInstalled.PACKAGE_NAME, info.pname);
                raw.put(ManagementProvider.AppsInstalled.VERSION_CODE, info.versionCode);
                raw.put(ManagementProvider.AppsInstalled.VERSION_NAME, info.versionName);
                raw.put(ManagementProvider.AppsInstalled.IS_SYSTEM_PACKAGE, info.isSystemPackage);

                data.put(raw);
            }
            return data;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }

    @Override
    public void updateStatusAfterSend(JSONArray failedList) {
        // do nothing
    	return;
    }

}
