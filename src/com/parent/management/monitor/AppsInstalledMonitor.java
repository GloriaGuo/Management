package com.parent.management.monitor;

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
    
    public AppsInstalledMonitor(Context context) {
        super(context);
    }
    
    @Override
    public void startMonitoring() {
        // init the first data
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

            PackageManager packageManager = ManagementApplication.getContext().getPackageManager();
            List<PackageInfo> packs = packageManager.getInstalledPackages(0);
            for(int i=0; i < packs.size(); i++) {
                PackageInfo p = packs.get(i);
                if (p.versionName == null) {
                    continue ;
                }
                
                JSONObject raw = new JSONObject();
                raw.put(ManagementProvider.AppsInstalled.APP_NAME, 
                        p.applicationInfo.loadLabel(packageManager).toString());
                raw.put(ManagementProvider.AppsInstalled.PACKAGE_NAME, p.packageName);
                raw.put(ManagementProvider.AppsInstalled.VERSION_CODE, p.versionCode);
                raw.put(ManagementProvider.AppsInstalled.VERSION_NAME, p.versionName);
                raw.put(ManagementProvider.AppsInstalled.IS_SYSTEM_PACKAGE, 
                        p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM);

                data.put(raw);
            }
            return data;
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON parameters: " + e.getMessage());
        }
        
        return null;
    }

    @Override
    public void updateStatusAfterSend(JSONArray failedList) {
        // do nothing
    	return;
    }

}
