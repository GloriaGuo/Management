package com.parent.management.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.parent.management.ManagementApplication;
import com.parent.management.R;
import com.parent.management.jsonclient.JSONClientException;
import com.parent.management.jsonclient.JSONHttpClient;
import com.parent.management.receiver.ManagementReceiver;

public class MainActivity extends Activity {
	private static final String TAG = ManagementApplication.getApplicationTag() + "." +
            MainActivity.class.getSimpleName();
	
    EditText mAccountText = null;
    EditText mCheckCodeText = null;
    Button   mRegistButton = null;
    
    ProgressDialog mProgressDialog = null;
    
    Handler handler = new MyHandler();

    public BDLocationListener myListener = new MyLocationListener();
    private LocationClient mLocClient;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        setLocationOption();
        mLocClient.start();
        if (mLocClient != null && mLocClient.isStarted()) {
            Log.d(TAG, "locClient is requesting");
            mLocClient.requestLocation();
        }
        else { 
            Log.d(TAG, "locClient is null or not started");
        }
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		this.mAccountText = (EditText) findViewById(R.id.accountEditText);
        this.mCheckCodeText = (EditText) findViewById(R.id.checkCodeEditText);
        this.mRegistButton = (Button) findViewById(R.id.registButton);
        
        this.mRegistButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                final String account = MainActivity.this.mAccountText.getText().toString().trim();
                final String checkCode = MainActivity.this.mCheckCodeText.getText().toString().trim();
                
                if (account.equals("")) {
                    MainActivity.this.showAlert(
                            MainActivity.this.getResources().getString(R.string.alert_dialog_message_empty_account));
                } else if (checkCode.equals("")) {
                    MainActivity.this.showAlert(
                            MainActivity.this.getResources().getString(R.string.alert_dialog_message_empty_check_code));
                } else {
                
                    // do registration
                    new Thread(new Runnable() {
    
                        @Override
                        public void run() {
                            MainActivity.this.doRegistration(account, checkCode);
                        }
                        
                    }).start();
                            
                    MainActivity.this.mProgressDialog = ProgressDialog.show(
                            MainActivity.this, 
                            "", 
                            MainActivity.this.getResources().getString(R.string.progress_dialog_loading_message), 
                            true);
                }
            }
        });

	}

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.e(TAG, "onReceiveLocation");   
            if (location == null)
                return ;
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation){
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
            } 
     
            Log.e(TAG, sb.toString());            
        }

        @Override
        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null){
                return ;
            }
            StringBuffer sb = new StringBuffer(256);
            sb.append("Poi time : ");
            sb.append(poiLocation.getTime());
            sb.append("\nerror code : ");
            sb.append(poiLocation.getLocType());
            sb.append("\nlatitude : ");
            sb.append(poiLocation.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(poiLocation.getLongitude());
            sb.append("\nradius : ");
            sb.append(poiLocation.getRadius());
            if (poiLocation.getLocType() == BDLocation.TypeNetWorkLocation){
                sb.append("\naddr : ");
                sb.append(poiLocation.getAddrStr());
            } 
            if(poiLocation.hasPoi()){
                sb.append("\nPoi:");
                sb.append(poiLocation.getPoi());
            }else{              
                sb.append("noPoi information");
            }
            Log.v(TAG, sb.toString());            
        }
        
    }

    private void setLocationOption(){
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); 
        option.setCoorType("bd09ll");
        option.setServiceName("com.baidu.location.service_v2.9");
        option.setPoiExtraInfo(false);   
        option.setAddrType("all");
        option.setScanSpan(3000); 
        option.setPriority(LocationClientOption.NetWorkFirst);
//        option.setPriority(LocationClientOption.GpsFirst);
        option.setPoiNumber(10);
        option.disableCache(true);      
        mLocClient.setLocOption(option);
    }
    
	private void showGPSAlert() {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_dialog_message_enable_gps)
                .setCancelable(false)
                .setPositiveButton(R.string.alert_dialog_positive_button_enable_gps, 
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                        closeApp();
                    }
                })
                .setNegativeButton(R.string.alert_dialog_negative_button_enable_gps, 
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        closeApp();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
	}
	
	private void showAlert(String message) {
	    AlertDialog mErrorDialog = new AlertDialog.Builder(this)
	            .setTitle(R.string.error_dialog_title) 
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(true)
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(final DialogInterface dialog) {
                    }
                })
                .setPositiveButton(R.string.alert_dialog_positive_button, 
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                            }
                    
                }).show();
        // linkify dialog message
        final TextView msgView = 
            (TextView) mErrorDialog.findViewById(android.R.id.message);
        Linkify.addLinks(msgView, Linkify.ALL);
        msgView.setMovementMethod(new LinkMovementMethod());
        mErrorDialog.setOwnerActivity(this);
	}
	
	private void doRegistration(String account, String code) {
	    
	    JSONHttpClient client = new JSONHttpClient(this.getResources().getString(R.string.server_address));
        client.setConnectionTimeout(5000);
        client.setSoTimeout(5000);
        
        boolean result = false;
        try {
            result = client.doRegistion(account, code);
        } catch (JSONClientException e) {
        	Log.e(TAG, "Regist Failed: " + e.getMessage());
        }
        
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putBoolean("result", result);
        msg.setData(data);
        this.handler.sendMessage(msg);
	}
	
	private void closeApp () {
	    // hiden
        PackageManager p = getPackageManager();
        p.setComponentEnabledSetting(
                getComponentName(),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
	    //moveTaskToBack(true);
	    finish();
	}
	
	private class MyHandler extends Handler {
		public MyHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
        	Boolean result = msg.getData().getBoolean("result");
        	
        	Log.d(TAG, "Registration result: " + result);
        	ManagementApplication.getConfiguration().setIsRegisted(result);
        	
        	if (MainActivity.this.mProgressDialog != null) {
                MainActivity.this.mProgressDialog.dismiss();
            }
        	
        	if (result) {
                // Launch services
                ManagementApplication.getContext().sendBroadcast(
                        new Intent(ManagementApplication.getContext(), ManagementReceiver.class));
                
                // check the GPS settings
                LocationManager locationManager = (LocationManager) MainActivity.this.getSystemService(
                        Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                    !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    MainActivity.this.showGPSAlert();
                } else {
                    closeApp();
                }
            } else {
                MainActivity.this.showAlert(
                		MainActivity.this.getResources().getString(
                				R.string.alert_dialog_message_regist_failed));
            }
        }
	}
	
}
