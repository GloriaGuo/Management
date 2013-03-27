package com.parent.management.jsonclient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.parent.management.ManagementApplication;

import android.util.Log;

public class JSONHttpClient {
    
    private static final String TAG = ManagementApplication.getApplicationTag()
            + "." + JSONHttpClient.class.getSimpleName();
    
    /*
     * HttpClient to issue the HTTP/POST request
     */
    private HttpClient httpClient;
    /*
     * Service URI
     */
    private String serviceUri;
    
    private String encoding = HTTP.UTF_8;
    
    private int soTimeout = 0, connectionTimeout = 0;
    
    // HTTP 1.0
    private static final ProtocolVersion PROTOCOL_VERSION = new ProtocolVersion("HTTP", 1, 0);
    
    // protocol
    private static final int MC_BASIC = 1;
    private static final int MC_CONFIG = 2;
    
    private static final int MT_BASIC_REG_REQ = 1;
    private static final int MT_BASIC_REG_RESP = 2;
    private static final int MT_BASIC_DATA_UPLOAD_REQ = 3;
    private static final int MT_BASIC_DATA_UPLOAD_RESP = 4;
    private static final int MT_CONFIG_GET_INTERVAL_REQ = 1;
    private static final int MT_CONFIG_GET_INTERVAL_RESP = 2;
    
    private static final int MS_SUCCESS = 0;
    
    public JSONHttpClient(HttpClient cleint, String uri){
        httpClient = cleint;
        serviceUri = uri;
    }
    
    public JSONHttpClient(String uri)
    {
        this(new DefaultHttpClient(), uri);
    }
    
    public void setEncoding(String encoding){
        this.encoding = encoding;
    }
    
    public void delEncoding(){
        this.encoding = "";
    }
    
    /**
     * Set the socket operation timeout
     * @param soTimeout timeout in milliseconds
     */
    public void setSoTimeout(int soTimeout)
    {
        this.soTimeout = soTimeout;
    }

    /**
     * Set the connection timeout
     * @param connectionTimeout timeout in milliseconds
     */
    public void setConnectionTimeout(int connectionTimeout)
    {
        this.connectionTimeout = connectionTimeout;
    }

    private JSONObject doJSONRequest(JSONObject jsonRequest) throws JSONClientException
    {
        // Create HTTP/POST request with a JSON entity containing the request
        HttpPost request = new HttpPost(serviceUri);
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, this.connectionTimeout);
        HttpConnectionParams.setSoTimeout(params, this.soTimeout);
        HttpProtocolParams.setVersion(params, PROTOCOL_VERSION);
        request.setParams(params);

        Log.d(TAG, "JSON Request: " + jsonRequest.toString());
        
        HttpEntity entity;
        
        try {
            if (encoding.length() > 0) {
                entity = new JSONEntity(jsonRequest, encoding);
            } else {
                entity = new JSONEntity(jsonRequest);
            }
        } catch (UnsupportedEncodingException e) {
            throw new JSONClientException("Unsupported encoding", e);
        }
        request.setEntity(entity);
        
        try {
            // Execute the request and try to decode the JSON Response
            long t = System.currentTimeMillis();
            HttpResponse response = httpClient.execute(request);
            
            t = System.currentTimeMillis() - t;
            String responseString = EntityUtils.toString(response.getEntity());
            
            responseString = responseString.trim();
            
            Log.d(TAG, "Response: " + responseString);
            
            return new JSONObject(responseString);
        }
        // Underlying errors are wrapped into a JSONRPCException instance
        catch (ClientProtocolException e) {
            throw new JSONClientException("HTTP error", e);
        } catch (IOException e) {
            throw new JSONClientException("IO error", e);
        } catch (JSONException e) {
            throw new JSONClientException("Invalid JSON response", e);
        }
    }
    
    protected static JSONArray getJSONArray(Object[] array){
        JSONArray arr = new JSONArray();
        for (Object item : array) {
            if(item.getClass().isArray()){
                arr.put(getJSONArray((Object[])item));
            }
            else {
                arr.put(item);
            }
        }
        return arr;
    }
    
    public JSONObject doUpload(Object[] params) throws JSONClientException
    {
        //Copy method arguments in a json array
        JSONArray jsonParams = new JSONArray();
        for (int i=0; i<params.length; i++)
        {
            if(params[i].getClass().isArray()){
                jsonParams.put(getJSONArray((Object[])params[i]));
            }
            jsonParams.put(params[i]);
        }
        
        //Create the json request object
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("pv", "1.0");
            jsonRequest.put("mc", MC_BASIC);
            jsonRequest.put("mt", MT_BASIC_DATA_UPLOAD_REQ);
            jsonRequest.put("seq", UUID.randomUUID().hashCode());
            jsonRequest.put("imei", ManagementApplication.getIMEI());
            jsonRequest.put("payload", jsonParams);
        } catch (JSONException e1) {
            throw new JSONClientException("Invalid JSON request", e1);
        }
        return doJSONRequest(jsonRequest);
    }
    
    public boolean doRegistion(String account, String code) throws JSONClientException
    {
        //Create the json request object
        JSONObject jsonRequest = new JSONObject();
        
        try {
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("ma", account);
            jsonParams.put("vc", code);
            jsonParams.put("ot", android.os.Build.MODEL);
            jsonParams.put("ov", android.os.Build.VERSION.RELEASE);
        
            jsonRequest.put("pv", "1.0");
            jsonRequest.put("mc", MC_BASIC);
            jsonRequest.put("mt", MT_BASIC_REG_REQ);
            int id = UUID.randomUUID().hashCode();
            jsonRequest.put("seq", id);
            jsonRequest.put("imei", ManagementApplication.getIMEI());
            jsonRequest.put("payload", jsonParams);
            
            JSONObject result = doJSONRequest(jsonRequest);
            
            if (result.getInt("mt") == MT_BASIC_REG_RESP && 
                result.getInt("seq") == id &&
                result.getInt("sc") == MS_SUCCESS) {
                return true;
            }
            return false;
        } catch (JSONException e1) {
            throw new JSONClientException("Invalid JSON request", e1);
        }
        
    }
    
    public int doConfiguration() throws JSONClientException 
    {
        //Create the json request object
        JSONObject jsonRequest = new JSONObject();
        
        try {
            jsonRequest.put("pv", "1.0");
            jsonRequest.put("mc", MC_CONFIG);
            jsonRequest.put("mt", MT_CONFIG_GET_INTERVAL_REQ);
            int id = UUID.randomUUID().hashCode();
            jsonRequest.put("seq", id);
            jsonRequest.put("imei", ManagementApplication.getIMEI());
            
            JSONObject result = doJSONRequest(jsonRequest);
            
            if (result.getInt("mt") == MT_CONFIG_GET_INTERVAL_RESP && 
                result.getInt("seq") == id &&
                result.getInt("sc") == MS_SUCCESS) {
                return result.getInt("interval");
            }
            return 1800000;
        } catch (JSONException e1) {
            throw new JSONClientException("Invalid JSON request", e1);
        }
        
    }
    
}
