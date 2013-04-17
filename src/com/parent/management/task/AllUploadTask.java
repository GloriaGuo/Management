package com.parent.management.task;

import java.util.HashMap;

import com.parent.management.ManagementApplication;
import com.parent.management.monitor.Monitor;
import com.parent.management.monitor.Monitor.Type;

public class AllUploadTask extends UploadTask {

    @Override
    public void create() {
        if (mMonitorList == null) {
            mMonitorList = new HashMap<Type, Monitor>();
            if (ManagementApplication.commonMonitorList != null) {
                mMonitorList.putAll(ManagementApplication.commonMonitorList);
            }
            if (ManagementApplication.specialMonitorList != null) {
                mMonitorList.putAll(ManagementApplication.specialMonitorList);
            }
        }
    }

}
