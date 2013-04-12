package com.parent.management.monitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.parent.management.ManagementApplication;

public class NetworkLocationProvider {
    private static final String TAG = ManagementApplication.getApplicationTag() + "." +
            NetworkLocationProvider.class.getSimpleName();
    
    private Context mContext;
    private LocationListener mLocationListener = null;
    private HashMap<SCell, SItude> mLocationCache = new HashMap<SCell, SItude>();
    private SItude mLastItude = null;
    
    private long interval;
    private float mDistance;
    
    public class SCell{
        public int MCC;
        public int MNC;
        public int LAC;
        public int CID;
    }
    
    public class SItude{
        public Double latitude;
        public Double longitude;
    }
    
    public NetworkLocationProvider(Context context) {
        this.mContext = context;
    }
    
    public boolean isProviderEnabled() {
        return false;
    }
    
    void requestLocationUpdates(long minTime, float minDistance, LocationListener listener) {
        this.interval = minTime;
        this.mDistance = minDistance;
        this.mLocationListener = listener;
        start();
    }
    
    void start() {
        mListenThread.execute();
    }
    
    void stop() {
        
    }

    class NetworkPositionTask extends AsyncTask<Object, Object, Object> {

        @Override
        protected Object doInBackground(Object... arg0) {
            while(true) {
                try {
                    SCell cell;
                    cell = getCellInfo();
                    if(null != cell) {
                        SItude newItude = new SItude();
                        if(mLocationCache.containsKey(cell)) {
                            Log.i(TAG, "already have this cell info.");
                            newItude = mLocationCache.get(cell);
                        }
                        else {
                            newItude = getItude(cell);
                            mLocationCache.put(cell, newItude);
                        }
                        if(needUpdateLocation(newItude)) {
                            Log.i(TAG, "need update.");
                            Location newLocation = new Location(LocationManager.NETWORK_PROVIDER);
                            newLocation.setLatitude(newItude.latitude);
                            newLocation.setLongitude(newItude.longitude);
                            mLocationListener.onLocationChanged(newLocation);
                            mLastItude = newItude;
                        }
                    }
                    wait(interval);
                }
                catch (Exception e) {
                    Log.e(TAG, "error while get location: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    private NetworkPositionTask mListenThread = new NetworkPositionTask();
    
    boolean needUpdateLocation(SItude newItude) {
        double subLatitude = Math.abs(newItude.latitude - mLastItude.latitude);
        double subLongitude = Math.abs(newItude.longitude - mLastItude.longitude);
        double distence = Math.sqrt(Math.pow(subLatitude, 2) + Math.pow(subLongitude, 2));
        if (distence < mDistance) {
            return false;
        }
        return true;
    }
    
    private SCell getCellInfo() throws Exception {
        SCell cell = new SCell();
     
        TelephonyManager mTelNet = (TelephonyManager)
                mContext.getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation location = (GsmCellLocation) mTelNet.getCellLocation();
        if (location == null) {
            return null;
        }
     
        String operator = mTelNet.getNetworkOperator();
        int mcc = Integer.parseInt(operator.substring(0, 3));
        int mnc = Integer.parseInt(operator.substring(3));
        int cid = location.getCid();
        int lac = location.getLac();
     
        cell.MCC = mcc;
        cell.MNC = mnc;
        cell.LAC = lac;
        cell.CID = cid;
        Log.i(TAG, "Cell info, mcc=" + mcc + ", mnc=" + mnc + ", lac=" + lac + ", cid=" + cid);
     
        return cell;
    }
    
    private SItude getItude(SCell cell) throws Exception {
        SItude itude = new SItude();
     
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://www.google.com/loc/json");
        try {
            JSONObject holder = new JSONObject();
            holder.put("version", "1.1.0");
            holder.put("host", "maps.google.com");
            holder.put("address_language", "zh_CN");
            holder.put("request_address", true);
            holder.put("radio_type", "gsm");
            holder.put("carrier", "HTC");
     
            JSONObject tower = new JSONObject();
            tower.put("mobile_country_code", cell.MCC);
            tower.put("mobile_network_code", cell.MNC);
            tower.put("cell_id", cell.CID);
            tower.put("location_area_code", cell.LAC);
     
            JSONArray towerarray = new JSONArray();
            towerarray.put(tower);
            holder.put("cell_towers", towerarray);
     
            StringEntity query = new StringEntity(holder.toString());
            post.setEntity(query);
     
            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(entity.getContent()));
            StringBuffer strBuff = new StringBuffer();
            String result = null;
            while ((result = buffReader.readLine()) != null) {
                strBuff.append(result);
            }
     
            JSONObject json = new JSONObject(strBuff.toString());
            JSONObject subjosn = new JSONObject(json.getString("location"));
     
            itude.latitude = Double.parseDouble(subjosn.getString("latitude"));
            itude.longitude = Double.parseDouble(subjosn.getString("longitude"));
             
            Log.i(TAG, "new position, latitude=" + itude.latitude + ", longitude=" + itude.longitude);
             
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            throw new Exception("Get location error: " + e.getMessage());
        } finally{
            post.abort();
            client = null;
        }
         
        return itude;
    }
}
