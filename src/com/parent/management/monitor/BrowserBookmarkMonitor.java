package com.parent.management.monitor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.parent.management.ManagementApplication;
import com.parent.management.db.ManagementProvider;

public class BrowserBookmarkMonitor extends Monitor {
    private static final String TAG = ManagementApplication.getApplicationTag() + "." +
            BrowserHistoryMonitor.class.getSimpleName();

    public BrowserBookmarkMonitor(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void startMonitoring() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void stopMonitoring() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public JSONArray extractDataForSend() {
        try {
            JSONArray data = new JSONArray();

            String[] browserHistoryProj = new String[] {
                    ManagementProvider.BrowserBookmark.URL,
                    ManagementProvider.BrowserBookmark.TITLE,
                    ManagementProvider.BrowserBookmark.VISIT_COUNT,
                    ManagementProvider.BrowserBookmark.LAST_VISIT };
            String browserHistorySel =
                    ManagementProvider.BrowserBookmark.IS_SENT + " = " + ManagementProvider.IS_SENT_NO;
            Cursor browserHistoryCur = ManagementApplication.getContext().getContentResolver().query(
                    ManagementProvider.BrowserBookmark.CONTENT_URI,
                    browserHistoryProj, browserHistorySel, null, null);

            if (browserHistoryCur == null) {
                Log.v(TAG, "open browserHistory native failed");
                return null;
            }
            if (browserHistoryCur.moveToFirst() && browserHistoryCur.getCount() > 0) {
                while (browserHistoryCur.isAfterLast() == false) {
                    String url = browserHistoryCur.getString(
                            browserHistoryCur.getColumnIndex(ManagementProvider.BrowserBookmark.URL));
                    String title = browserHistoryCur.getString(
                            browserHistoryCur.getColumnIndex(ManagementProvider.BrowserBookmark.TITLE));
                    int visit_count = browserHistoryCur.getInt(
                            browserHistoryCur.getColumnIndex(ManagementProvider.BrowserBookmark.VISIT_COUNT));
                    int last_visit = browserHistoryCur.getInt(
                            browserHistoryCur.getColumnIndex(ManagementProvider.BrowserBookmark.LAST_VISIT));
                    JSONObject raw = new JSONObject();
                    raw.put(ManagementProvider.BrowserBookmark.URL, url);
                    raw.put(ManagementProvider.BrowserBookmark.TITLE, title);
                    raw.put(ManagementProvider.BrowserBookmark.VISIT_COUNT, visit_count);
                    raw.put(ManagementProvider.BrowserBookmark.LAST_VISIT, last_visit);

                    data.put(raw);
                    browserHistoryCur.moveToNext();
                }
            }
            if (null != browserHistoryCur) {
                browserHistoryCur.close();
            }
            
            return data;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        final ContentValues values = new ContentValues();
        values.put(ManagementProvider.BrowserBookmark.IS_SENT, ManagementProvider.IS_SENT_YES);
        ManagementApplication.getContext().getContentResolver().update(
        		ManagementProvider.BrowserBookmark.CONTENT_URI,
                values,
                ManagementProvider.BrowserBookmark.IS_SENT + "=\"" + ManagementProvider.IS_SENT_NO +"\"",
                null);
        return null;
    }

    @Override
    public void updateStatusAfterSend(JSONArray failedList) {
        // TODO Auto-generated method stub
        
    }

}
