package com.monitor.kids.monitor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.Browser;
import android.util.Log;

import com.monitor.kids.KidsApplication;
import com.monitor.kids.db.KidsProvider;
import com.monitor.kids.db.KidsProvider.BrowserHistory;

public class BrowserHistoryMonitor extends Monitor {
    private static final String TAG = KidsApplication.getApplicationTag() + "." +
            BrowserHistoryMonitor.class.getSimpleName();

	private BrowserDBObserver contentObserver = null;

	public BrowserHistoryMonitor(Context context) {
		super(context);
	    this.contentUri = Browser.BOOKMARKS_URI;
	    this.contentObserver = new BrowserDBObserver(new Handler());
	}

	@Override
	public void startMonitoring() {
		this.contentResolver.registerContentObserver(this.contentUri, true, this.contentObserver);
	    this.monitorStatus = true;
        checkForChange();
	}

	@Override
	public void stopMonitoring() {
        this.contentResolver.unregisterContentObserver(this.contentObserver);
        this.monitorStatus = false;
        Log.d(TAG, "----> stopMonitoring");
	}
	
	private class BrowserInfo {
	    long id;
	    String title;
	    String url;
	    int visitCount;
	    long lastVisit;
	    private void prettyPrint() {
            Log.v(TAG, "id=" + id + ";title=" + title + ";url=" + url + ";count=" + visitCount + ";last=" + lastVisit);
	    }
	}
	
	private class BrowserDBObserver extends ContentObserver {

		public BrowserDBObserver(Handler handler) {
			super(handler);
		}
		
		@Override
        public void onChange(boolean selfChange) {
		    checkForChange();
		}
		
	}

    private void checkForChange() {
    	long lastVisitOverallTmp = KidsApplication.getConfiguration().getLastVisitBrowserHistory();
        String[] browserProj = new String[] {
                Browser.BookmarkColumns._ID,
                Browser.BookmarkColumns.TITLE,
                Browser.BookmarkColumns.URL,
                Browser.BookmarkColumns.VISITS,
                Browser.BookmarkColumns.DATE };
        String browserHistorySel = Browser.BookmarkColumns.BOOKMARK + " = 0";
        String orderBy = Browser.BookmarkColumns.VISITS + " DESC"; 
        Cursor browserCur = KidsApplication.getContext().getContentResolver().query(
                Browser.BOOKMARKS_URI, browserProj, browserHistorySel, null, orderBy);
        
        if (browserCur == null) {
            Log.v(TAG, "open browser db failed");
            return;
        }
        if (browserCur.moveToFirst() && browserCur.getCount() > 0) {
            while (browserCur.isAfterLast() == false) {
                BrowserInfo browserInfo = new BrowserInfo();
                browserInfo.id = browserCur.getLong(browserCur.getColumnIndex(Browser.BookmarkColumns._ID));
                browserInfo.title = browserCur.getString(browserCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
                browserInfo.url = browserCur.getString(browserCur.getColumnIndex(Browser.BookmarkColumns.URL));
                browserInfo.visitCount = browserCur.getInt(browserCur.getColumnIndex(Browser.BookmarkColumns.VISITS));
                browserInfo.lastVisit = browserCur.getLong(browserCur.getColumnIndex(Browser.BookmarkColumns.DATE));
                if (lastVisitOverallTmp < browserInfo.lastVisit) {
                	lastVisitOverallTmp = browserInfo.lastVisit;
                    if (!updateBrowserHistoryDB(browserInfo)) {
                        break;
                    }
                }

                browserCur.moveToNext();
            }
        }
        if (null != browserCur) {
            browserCur.close();
        }
        if (KidsApplication.getConfiguration().getLastVisitBrowserHistory() < lastVisitOverallTmp) {
        	KidsApplication.getConfiguration().setLastVisitBrowserHistory(lastVisitOverallTmp);
        }
    }

    private boolean updateBrowserHistoryDB(BrowserInfo browserInfo) {
        return updateLocalBrowserDB(browserInfo, BrowserHistory.TABLE_NAME, BrowserHistory.CONTENT_URI);
    }

    private boolean updateLocalBrowserDB(final BrowserInfo browserInfo, final String table, final Uri uri) {
        final ContentValues values = new ContentValues();
        values.put(KidsProvider.BrowserDB.URL, browserInfo.url);
        values.put(KidsProvider.BrowserDB.TITLE, browserInfo.title);
        values.put(KidsProvider.BrowserDB.VISIT_COUNT, browserInfo.visitCount);
        values.put(KidsProvider.BrowserDB.LAST_VISIT, browserInfo.lastVisit);
        
        KidsApplication.getContext().getContentResolver().insert(
                uri, values);
        browserInfo.prettyPrint();
        Log.v(TAG, "insert one");
        return true;
    }


    @Override
    public JSONArray extractDataForSend() {
        try {
            JSONArray data = new JSONArray();

            String[] browserHistoryProj = new String[] {
            		KidsProvider.BrowserHistory._ID,
                    KidsProvider.BrowserHistory.URL,
                    KidsProvider.BrowserHistory.TITLE,
                    KidsProvider.BrowserHistory.LAST_VISIT};
            String browserHistorySel = KidsProvider.BrowserHistory.IS_SENT
                    + " = \"" + KidsProvider.IS_SENT_NO + "\"";
            Cursor browserHistoryCur = KidsApplication.getContext().getContentResolver().query(
                    KidsProvider.BrowserHistory.CONTENT_URI,
                    browserHistoryProj, browserHistorySel, null, null);

            if (browserHistoryCur == null) {
                Log.v(TAG, "open browserHistory native failed");
                return null;
            }
            if (browserHistoryCur.moveToFirst() && browserHistoryCur.getCount() > 0) {
                while (browserHistoryCur.isAfterLast() == false) {
                	long id = browserHistoryCur.getLong(
                			browserHistoryCur.getColumnIndex(KidsProvider.BrowserHistory._ID));
                    String url = browserHistoryCur.getString(
                            browserHistoryCur.getColumnIndex(KidsProvider.BrowserHistory.URL));
                    String title = browserHistoryCur.getString(
                            browserHistoryCur.getColumnIndex(KidsProvider.BrowserHistory.TITLE));
                    long last_visit = browserHistoryCur.getLong(
                            browserHistoryCur.getColumnIndex(KidsProvider.BrowserHistory.LAST_VISIT));

                    JSONObject raw = new JSONObject();
                    raw.put(KidsProvider.BrowserHistory._ID, id);
                    raw.put(KidsProvider.BrowserHistory.URL, url);
                    raw.put(KidsProvider.BrowserHistory.TITLE, title);
                    raw.put(KidsProvider.BrowserHistory.LAST_VISIT, last_visit);

                    data.put(raw);
                    browserHistoryCur.moveToNext();
                }
            }
            if (null != browserHistoryCur) {
                browserHistoryCur.close();
            }
            
            final ContentValues values = new ContentValues();
            values.put(KidsProvider.BrowserHistory.IS_SENT, KidsProvider.IS_SENT_YES);
            KidsApplication.getContext().getContentResolver().update(
                    KidsProvider.BrowserHistory.CONTENT_URI,
                    values,
                    KidsProvider.BrowserHistory.IS_SENT + "=\"" + KidsProvider.IS_SENT_NO +"\"",
                    null);
            
            return data;
        } catch (JSONException e) {
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
    				long id = obj.optLong(KidsProvider.BrowserHistory._ID);
    		        final ContentValues values = new ContentValues();
    		        values.put(KidsProvider.BrowserHistory.IS_SENT, KidsProvider.IS_SENT_NO);
    		        KidsApplication.getContext().getContentResolver().update(
    		        		KidsProvider.BrowserHistory.CONTENT_URI,
    		                values,
    		                KidsProvider.BrowserHistory._ID + "=\"" + id +"\"",
    		                null);
    			}
    		}
    	}
        String browserHistorySel = KidsProvider.BrowserHistory.IS_SENT
        		+ " = \"" + KidsProvider.IS_SENT_YES + "\"";
    	KidsApplication.getContext().getContentResolver().delete(
    			KidsProvider.BrowserHistory.CONTENT_URI,
    			browserHistorySel, null);
    }

}
