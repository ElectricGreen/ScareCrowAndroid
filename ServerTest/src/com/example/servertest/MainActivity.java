package com.example.servertest;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity
{


	
	private Camera camera;
	private int cameraId = 0;
	
	private TimerTask cameraTask; 	
	private Timer timer;


	
	 //UI
	TextView uiTemp;
	TextView uiHumid;
	TextView uiIP;
	
	TextView uiNumNodes;
	TextView uiNodeTemp;
	TextView uiNodeSoil;
	TextView uiNodeLight;

	 static ToggleButton btnSolenoid;
	 static Button btnCamera;
	
	public double temperatureValue;
	public double humidityValue;
	private boolean timeLapseOn = false;

	
	private static String TAG = "ELECTRICSCARE";
	

	
	 

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       
     // do we have a camera?
//        if (!getPackageManager()
//            .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
//          Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
//              .show();
//        } else {
//          cameraId = findFrontFacingCamera();
//            camera = Camera.open(0);
//        }
        
    }
    
    
    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
        	Log.d(TAG, "fries are done "+ i);
          CameraInfo info = new CameraInfo();
          Camera.getCameraInfo(i, info);
          if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
            Log.d("Camera", "Camera found");
            cameraId = i;
            break;
          }
        }
        return cameraId;
      }

    // DON'T FORGET to stop the server
    @Override
    public void onDestroy()
    {
        super.onDestroy();
//        if (server != null)
//            server.stop();
    }
    
    @Override
    protected void onPause() {
      if (camera != null) {
        camera.release();
        camera = null;
      }
      super.onPause();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	// use this to start and trigger a service
    	Intent i= new Intent(this, ScareService.class);
    	// potentially add data to the intent
    	i.putExtra("ScareServ", "Value to be used by the service");
    	this.startService(i);
    	
    	//Keep the display on
    	getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

        uiIP = ((TextView)findViewById(R.id.value_ip));
        uiTemp = ((TextView)findViewById(R.id.value_temperature));
        uiHumid = ((TextView)findViewById(R.id.value_humidity));
         uiNumNodes = ((TextView)findViewById(R.id.value_numnodes));
    	 uiNodeTemp = ((TextView)findViewById(R.id.value_node1temp));
    	 uiNodeSoil = ((TextView)findViewById(R.id.value_node1soil));
    	 uiNodeLight = ((TextView)findViewById(R.id.value_node1light));
        
        btnCamera = (Button) findViewById(R.id.captureFront);
        btnCamera.setOnClickListener(new View.OnClickListener() {
		    @Override
	    	public void onClick(View v) {
		    	camera.startPreview();
	            camera.setDisplayOrientation(180);

		    	camera.takePicture(null, null,
		    	        new PhotoHandler(getApplicationContext()));
		    	
//		    	if(timeLapseOn){
//		    		timeLapseOn = false;
//			    	btnCamera.setText("Turn On Time-Lapse");
//			    	timer.cancel();
//			    	timer = null;
//		    	}else{
//		    		timeLapseOn = true;
//			    	btnCamera.setText("Turn Off Time-Lapse");
//			    	if (timer == null) {
//						timer = new Timer("cameraTimer"); // Starts the DemoMode timertask
//						if (cameraTask == null) {
//							cameraTask = new TimerTask() {
//								@Override
//								public void run() {
//							    	camera.startPreview();
//						            camera.setDisplayOrientation(180);
//
//							    	camera.takePicture(null, null,
//							    	        new PhotoHandler(getApplicationContext()));
////							    	camera.stopPreview();
//								}
//							};
//						}
//
//						timer.schedule(cameraTask, 60000L, 1000L); // New values every 30
//																	// seconds
//					}
//		    	}
		    	
		    }
    });
        
        btnSolenoid = ((ToggleButton)findViewById(R.id.btn_solenoidToggle));
        btnSolenoid.setOnClickListener(new View.OnClickListener() {
		    @Override
	    	public void onClick(View v) {
		    	//setSolenoid(1,btnSolenoid.isChecked());
		    }
    });
        
        uiIP.setText(getLocalIpAddress());
    }
    
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
//        	String IP;
//        	  WifiManager wim= (WifiManager) getSystemService(WIFI_SERVICE);
//        	  List<WifiConfiguration> l =  wim.getConfiguredNetworks(); 
//        	  WifiConfiguration wc = l.get(0);
//        	  IP=Formatter.formatIpAddress(wim.getConnectionInfo().getIpAddress());
//        	  return IP;
        } catch (SocketException ex) {
//            Log.e(LOG_TAG, ex.toString());
        }
        return null;
    }
    
   
    
   

}
