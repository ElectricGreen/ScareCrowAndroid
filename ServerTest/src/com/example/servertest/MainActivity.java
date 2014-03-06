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
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
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
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity
{
	public static final byte PCK_UPDATE = 0x5;
	public static final byte PCK_CMD = 0x6;

	public static final byte CMD_SOLENOID = 0x10;

	public static final byte HTTP_SOLENOID1_OFF = 0x11;
	public static final byte HTTP_SOLENOID1_ON = 0x10;
	public static final byte HTTP_SOLENOID2_OFF = 0x11;
	public static final byte HTTP_SOLENOID2_ON = 0x10;
	public static final byte HTTP_SOLENOID3_OFF = 0x11;
	public static final byte HTTP_SOLENOID3_ON = 0x10;
	public static final byte HTTP_SOLENOID4_OFF = 0x11;
	public static final byte HTTP_SOLENOID4_ON = 0x10;
	public static final byte HTTP_TAKE_PIC = 0x20;

	
	private Camera camera;
	private int cameraId = 0;
	
	private TimerTask cameraTask; 	
	private Timer timer;

	//Database
	DataLogging db;
	int dataCount = 0;
	
	 //UI
	TextView uiTemp;
	TextView uiHumid;
	TextView uiIP;
	 static ToggleButton btnSolenoid;
	 static Button btnCamera;
	
	public double temperatureValue;
	public double humidityValue;
	private boolean timeLapseOn = false;
	
	//Open Accessory
	private boolean deviceAttached = false;
	
	private int firmwareProtocol = 0;
	
	private static String TAG = "ELECTRICSCARE";
	
	private enum ErrorMessageCode {
		ERROR_OPEN_ACCESSORY_FRAMEWORK_MISSING,
		ERROR_FIRMWARE_PROTOCOL
	};
	
	private static USBAccessoryManager accessoryManager; 
	 
	//Web Server
    private WebServer server;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       
     // do we have a camera?
        if (!getPackageManager()
            .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
          Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
              .show();
        } else {
          cameraId = findFrontFacingCamera();
            camera = Camera.open(0);
        }
        
		db = new DataLogging(this);
        
        //WebServer
                	   server = new WebServer();
                       try {
                           server.start();
                       } catch(IOException ioe) {
                           Log.w("Httpd", "The server could not start.");
                       }
                       Log.w("Httpd", "Web server initialized.");
 
        //Open Accessory
        accessoryManager = new USBAccessoryManager(handler, 0);
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
        if (server != null)
            server.stop();
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
    	
    	//Keep the display on
    	getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

        accessoryManager.enable(this, getIntent());
        uiIP = ((TextView)findViewById(R.id.value_ip));
        uiTemp = ((TextView)findViewById(R.id.value_temperature));
        uiHumid = ((TextView)findViewById(R.id.value_humidity));
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
		    	setSolenoid(1,btnSolenoid.isChecked());
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
        } catch (SocketException ex) {
//            Log.e(LOG_TAG, ex.toString());
        }
        return null;
    }
    
    public static void setSolenoid(int solenoid, boolean state){
		byte[] commandPacket = new byte[4];
		commandPacket[0] = PCK_CMD;
		commandPacket[1] = CMD_SOLENOID;
		commandPacket[2] = (byte) solenoid;
		commandPacket[3] = (byte) (state?1:0);
		accessoryManager.write(commandPacket);
    }
    
    public void httpResponse(String uri){
    	if (uri.length()<4)
			return;

		char[] letters = uri.toCharArray();
		int command = Character.getNumericValue(letters[1]) * 1000;
		command += Character.getNumericValue(letters[2]) * 100;
		command += Character.getNumericValue(letters[3]) * 10;
		command += Character.getNumericValue(letters[4]);
		switch (command) {
		case HTTP_SOLENOID1_ON:
			Log.d(TAG, "solendoid on");
			MainActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					btnSolenoid.setChecked(true);
				}
			});
			setSolenoid(1, true);
			break;
		case HTTP_SOLENOID1_OFF:
			Log.d(TAG, "solendoid off");
			MainActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					btnSolenoid.setChecked(false);
				}
			});
			setSolenoid(1, false);
			break;
		case HTTP_TAKE_PIC:
			camera.startPreview();
            camera.setDisplayOrientation(180);

	    	camera.takePicture(null, null,
	    	        new PhotoHandler(getApplicationContext()));
			break;
		}
	}
    
    /** 
     * Handler for receiving messages from the USB Manager thread or
     *   the LED control modules
     */
    private Handler handler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		
    		if(accessoryManager.isConnected() == false) {
				return;
			}
    		
    		
    		Log.d("Handler", "what: " + msg.what);
    		
			switch(msg.what)
			{				
			case 0:
				USBAccessoryManagerMessage recMessage = (USBAccessoryManagerMessage)msg.obj;
				switch (recMessage.type) {
				case READ:
					if(accessoryManager.isConnected() == false) {
						return;
					}
					switch(recMessage.data[0]){
					
					case PCK_UPDATE:
						int temp = bytesToInt(recMessage.data[1],recMessage.data[2],recMessage.data[3],recMessage.data[4]);
						temperatureValue = temp*.001;
						temp = bytesToInt(recMessage.data[5],recMessage.data[6],recMessage.data[7],recMessage.data[8]);
						humidityValue = temp*.001;
						uiTemp.setText(Double.toString(temperatureValue));
						uiHumid.setText(Double.toString(humidityValue));
						Date currentDate = new Date();
						db.addData(new Data(dataCount++,currentDate.getTime(),temperatureValue,humidityValue));		
						db.exportCVS();
						break;
					
					}
					Log.d(TAG, "Rec Message:"+recMessage.data[0]+"|"+recMessage.data[1]);
					break;
					
				case CONNECTED:
					break;
				case READY:					
					Log.d(TAG, "BasicAccessoryDemo:Handler:READY");
					break;
				case DISCONNECTED:
					break;
				}				
				
   				break;
			default:
				break;
			}	//switch
    	} //handleMessage
    }; //handler
    
    
    
    
    
    // WebServer Temporary Class
    private class WebServer extends NanoHTTPD {

        public WebServer()
        {
            super(8090);
        }

        @Override
        public Response serve(String uri, Method method, 
                              Map<String, String> header,
                              Map<String, String> parameters,
                              Map<String, String> files) {
        	httpResponse(uri);
            String answer = "";
//            try {
//                // Open file from SD Card
//                File root = Environment.getExternalStorageDirectory();
//                FileReader index = new FileReader(root.getAbsolutePath() +
//                        "/www/index.html");
//                BufferedReader reader = new BufferedReader(index);
//                String line = "";
//                while ((line = reader.readLine()) != null) {
//                    answer += line;
//                }
//
//            } catch(IOException ioe) {
//                Log.w("Httpd", ioe.toString());
//            }
            

            return new NanoHTTPD.Response(answer);
        }
    }
    
    public int bytesToInt(byte msb, byte byte1, byte byte2, byte lsb) {
		int temp = (msb & 0xFF);
		temp = ((temp << 8) & 0xFF00) + (byte1 & 0xFF);
		temp = ((temp << 8) & 0xFFFF00) + (byte2 & 0xFF);
		temp = ((temp << 8) & 0xFFFFFF00) + (lsb & 0xFF);
		return temp;
	}
	public void intToBytes(byte[] data, int index, int value){
		data[index++] = (byte) ((value>>24)&0xFF);
		data[index++] = (byte) ((value>>16)&0xFF);
		data[index++] = (byte) ((value>>8)&0xFF);
		data[index++] = (byte) (value&0xFF);
		return;
	}

}
