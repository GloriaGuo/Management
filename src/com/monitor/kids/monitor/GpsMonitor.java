package com.monitor.kids.monitor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.monitor.kids.KidsApplication;
import com.monitor.kids.R;
import com.monitor.kids.db.KidsProvider;

public class GpsMonitor extends Monitor {
    private static final String TAG = KidsApplication.getApplicationTag() + "." +
            GpsMonitor.class.getSimpleName();
    Context mContext = null;
    private LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();
    
    private BDLocation mLastLocation = null;
    private boolean isInsertLastLocation = false;
    
    public final static String BD_LOCATION_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    public GpsMonitor(Context context) {
        super(context);
        mContext = KidsApplication.getContext();
        mLocClient = new LocationClient(mContext);
        mLocClient.registerLocationListener(myListener);
    }
    
    @Override
    public void startMonitoring() {
        setLocationOption();
        if (!mLocClient.isStarted()) {
            Log.d(TAG, "start locClient");
            mLocClient.start();
        }
        else {
            mLocClient.requestLocation();
//            mLocClient.requestOfflineLocation();
        }
        this.monitorStatus = true;
    }
    
    @Override
    public void stopMonitoring() {
        mLocClient.stop();
        this.monitorStatus = false;
    }

    private void setLocationOption(){
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(mContext.getResources().getBoolean(R.attr.location_bd_opetion_is_enable_gps)); 
        option.setCoorType(mContext.getResources().getString(R.string.location_bd_opetion_CoorType));
        option.setServiceName(mContext.getResources().getString(R.string.location_bd_opetion_ServiceName));
        option.setPoiExtraInfo(mContext.getResources().getBoolean(R.attr.location_bd_opetion_is_set_PoiExtraInfo));  
        option.setAddrType(mContext.getResources().getString(R.string.location_bd_opetion_AddrType));
//        if (mContext.getResources().getBoolean(R.attr.location_bd_opetion_is_gps_first)) {
//            option.setPriority(LocationClientOption.GpsFirst);
//        }
//        else {
//            option.setPriority(LocationClientOption.NetWorkFirst);
//        }
        option.setPriority(LocationClientOption.NetWorkFirst);
        option.setPoiNumber(mContext.getResources().getInteger(R.attr.location_bd_opetion_PoiNumber));
        option.disableCache(mContext.getResources().getBoolean(R.attr.location_bd_opetion_is_disableCache));      
        mLocClient.setLocOption(option);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null ||
                    (location != null && 
                    (location.getLatitude() - 0.00 < Double.MIN_VALUE) &&
                    (location.getLongitude() - 0.00 < Double.MIN_VALUE))) {
                return;
            }
            updateLocation(location);
        }

        @Override
        public void onReceivePoi(BDLocation poiLocation) {
        }
        
    }
    
    private final static double EARTH_RADIUS = 6378.137;
    private static double rad(double d)
    {
       return d * Math.PI / 180.0;
    }

    public static double getDistance(double lat1, double lng1, double lat2, double lng2)
    {
       double radLat1 = rad(lat1);
       double radLat2 = rad(lat2);
       double a = radLat1 - radLat2;
       double b = rad(lng1) - rad(lng2);

       double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
               Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
       s = s * EARTH_RADIUS;
       s = Math.round(s * 10000) / 10000;
       return s;
    }
    
    private boolean isNeedUpdateLocation(BDLocation newLocation) {
        if (null == newLocation) {
            Log.d(TAG, "Get empty location, ignore it...");
            return false;
        }
        if (null != mLastLocation && isBDLocationGeoEquals(mLastLocation, newLocation)) {
            Log.d(TAG, "Get same location, ignore it...");
            return false;
        }
        if (null != mLastLocation) {
            long timeThreshold = mContext.getResources()
                    .getInteger(R.attr.location_update_time_threshold);
            double distanceThreshold = Double.parseDouble(mContext.getResources()
                    .getString(R.string.location_update_distance_in_kilometer_threshold));
            long currentTime = getIntegerTimeFromString(newLocation.getTime());
            long lastTime = getIntegerTimeFromString(mLastLocation.getTime());
            double distance = getDistance(
                    mLastLocation.getLatitude(),
                    mLastLocation.getLongitude(),
                    newLocation.getLatitude(),
                    newLocation.getLongitude());
            if((currentTime - lastTime < timeThreshold)
                    && (distance - distanceThreshold > Double.MIN_VALUE)) {
                Log.d(TAG, "Get abnormal location, sub-time=" + (currentTime - lastTime)
                        + ", distance=" + distance
                        + ", ignore it...");
                return false;
            }
        }
        return true;
    }
    
    public boolean isBDLocationGeoEquals(BDLocation left, BDLocation right) {
        if (Math.abs(left.getAltitude() - right.getAltitude()) > Double.MIN_VALUE) {
            return false;
        }
        if (Math.abs(left.getLatitude() - right.getLatitude()) > Double.MIN_VALUE) {
            return false;
        }
        if (Math.abs(left.getLongitude() - right.getLongitude()) > Double.MIN_VALUE) {
            return false;
        }
        if (Math.abs(left.getRadius() - right.getRadius()) > Float.MIN_VALUE) {
            return false;
        }
        if (Math.abs(left.getSpeed() - right.getSpeed()) > Float.MIN_VALUE) {
            return false;
        }
        return true;
    }

    public long getIntegerTimeFromString(String timeStr) {
        SimpleDateFormat format = new SimpleDateFormat(BD_LOCATION_DATE_FORMAT);
        Date date = null;
        try {
            date = format.parse(timeStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }
    
    public void updateLocation(BDLocation location) {
        if (!isNeedUpdateLocation(location)) {
            return;
        }
        Log.d(TAG, "Get new location !");
        mLastLocation = location;
        isInsertLastLocation = false;
    }

    private void insertLocationToDB(BDLocation location) {
        double altitude = location.getAltitude();
        double latidude = location.getLatitude();
        double lontitude = location.getLongitude();
        float radius = location.getRadius();
        float speed = location.getSpeed();
        long time = getIntegerTimeFromString(location.getTime());

        final ContentValues values = new ContentValues();
        values.put(KidsProvider.Gps.ALTITUDE, altitude);
        values.put(KidsProvider.Gps.LATIDUDE, latidude);
        values.put(KidsProvider.Gps.LONGITUDE, lontitude);
        values.put(KidsProvider.Gps.RADIUS, radius);
        values.put(KidsProvider.Gps.SPEED, speed);
        values.put(KidsProvider.Gps.TIME, time);
        
        mContext.getContentResolver().insert(
                KidsProvider.Gps.CONTENT_URI, values);
        Log.d(TAG, "insert gps: altitude=" + altitude + ";latidude=" + latidude + ";lontitude=" + lontitude
                + ";radius=" + radius + ";speed=" + speed + ";time=" + time);
    }

    @Override
    public JSONArray extractDataForSend() {
        if (!isInsertLastLocation && null != mLastLocation) {
            insertLocationToDB(mLastLocation);
            isInsertLastLocation = true;
        }
        try {
            JSONArray data = new JSONArray();

            String[] GpsProj = new String[] {
            		KidsProvider.Gps._ID,
                    KidsProvider.Gps.ALTITUDE,
                    KidsProvider.Gps.LATIDUDE,
                    KidsProvider.Gps.LONGITUDE,
                    KidsProvider.Gps.RADIUS,
                    KidsProvider.Gps.SPEED,
                    KidsProvider.Gps.TIME};
            String GpsSel = KidsProvider.Gps.IS_SENT + " = \""
                    + KidsProvider.IS_SENT_NO + "\"";
            Cursor gpsCur = mContext.getContentResolver().query(
                    KidsProvider.Gps.CONTENT_URI,
                    GpsProj, GpsSel, null, null);

            if (gpsCur == null) {
                Log.e(TAG, "open gps table failed");
                return null;
            }
            if (gpsCur.moveToFirst() && gpsCur.getCount() > 0) {
                while (gpsCur.isAfterLast() == false) {
                	long id = gpsCur.getLong(gpsCur.getColumnIndex(KidsProvider.Gps._ID));
                    double alt = gpsCur.getDouble(
                            gpsCur.getColumnIndex(KidsProvider.Gps.ALTITUDE));
                    double lat = gpsCur.getDouble(
                            gpsCur.getColumnIndex(KidsProvider.Gps.LATIDUDE));
                    double lon = gpsCur.getDouble(
                            gpsCur.getColumnIndex(KidsProvider.Gps.LONGITUDE));
                    double rad = gpsCur.getFloat(
                            gpsCur.getColumnIndex(KidsProvider.Gps.RADIUS));
                    float spd = gpsCur.getFloat(
                            gpsCur.getColumnIndex(KidsProvider.Gps.SPEED));
                    long date = gpsCur.getLong(
                            gpsCur.getColumnIndex(KidsProvider.Gps.TIME));
                    JSONObject raw = new JSONObject();
                    raw.put(KidsProvider.Gps._ID, id);
                    raw.put(KidsProvider.Gps.ALTITUDE, alt);
                    raw.put(KidsProvider.Gps.LATIDUDE, lat);
                    raw.put(KidsProvider.Gps.LONGITUDE, lon);
                    raw.put(KidsProvider.Gps.RADIUS, rad);
                    raw.put(KidsProvider.Gps.SPEED, spd);
                    raw.put(KidsProvider.Gps.TIME, date);

                    data.put(raw);
                    gpsCur.moveToNext();
                }
            }
            if (null != gpsCur) {
                gpsCur.close();
            }
            
            Log.v(TAG, "data === " + data.toString());
            
            final ContentValues values = new ContentValues();
            values.put(KidsProvider.Gps.IS_SENT, KidsProvider.IS_SENT_YES);
            mContext.getContentResolver().update(
                    KidsProvider.Gps.CONTENT_URI,
                    values,
                    KidsProvider.Gps.IS_SENT + "=\"" + KidsProvider.IS_SENT_NO +"\"",
                    null);
            
            return data;
        } catch (JSONException e) {
            Log.v(TAG, "Json exception:" + e.getMessage());
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
    				long id = obj.optLong(KidsProvider.Gps._ID);
    		        final ContentValues values = new ContentValues();
    		        values.put(KidsProvider.Gps.IS_SENT, KidsProvider.IS_SENT_NO);
    		        mContext.getContentResolver().update(
    		        		KidsProvider.Gps.CONTENT_URI,
    		                values,
    		                KidsProvider.Gps._ID + "=\"" + id +"\"",
    		                null);
    			}
    		}
    	}
        String gpsSel = KidsProvider.Gps.IS_SENT
        		+ " = \"" + KidsProvider.IS_SENT_YES + "\"";
    	KidsApplication.getContext().getContentResolver().delete(
    			KidsProvider.Gps.CONTENT_URI,
    			gpsSel, null);
    }
   
}
