package com.monitor.kids.task;

import java.util.HashMap;

import com.monitor.kids.KidsApplication;
import com.monitor.kids.monitor.Monitor;
import com.monitor.kids.monitor.Monitor.Type;

public class AllUploadTask extends UploadTask {

    @Override
    public void create() {
        if (mMonitorList == null) {
            mMonitorList = new HashMap<Type, Monitor>();
            if (KidsApplication.commonMonitorList != null) {
                mMonitorList.putAll(KidsApplication.commonMonitorList);
            }
            if (KidsApplication.specialMonitorList != null) {
                mMonitorList.putAll(KidsApplication.specialMonitorList);
            }
        }
    }

}
