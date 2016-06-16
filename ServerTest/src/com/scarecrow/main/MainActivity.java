package com.scarecrow.main;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
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
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.scarecrow.R;
import com.scarecrow.ui.LedActivity;
import com.scarecrow.ui.VaporActivity;

public class MainActivity extends Activity
{
	
	//new UI
	Button btnLed;
	Button btnVapor;
	Button btnOff;
	
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

	private WakeLock screenWakeLock;

	private static PhotoHandler pic;
	
	private static String TAG = "ELECTRICSCARE";
	
	public static SurfaceView photoView;
	private static Context context; 

	Messenger mService = null;
	/** Flag indicating whether we have called bind on the service. */
	boolean mIsBound;

	static class IncomingHandler extends Handler {
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	        case ScareService.MSG_TAKE_PIC:
				pic.takePicture(photoView,msg.arg1,msg.arg2);
				break;
	            default:
	                super.handleMessage(msg);
	        }
	    }
	}
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	 
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className,
	            IBinder service) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  We are communicating with our
	        // service through an IDL interface, so get a client-side
	        // representation of that from the raw service object.
	        mService = new Messenger(service);

	        // We want to monitor the service for as long as we are
	        // connected to it.
	        try {
	            Message msg = Message.obtain(null,
	            		ScareService.MSG_REGISTER_CLIENT);
	            msg.replyTo = mMessenger;
	            mService.send(msg);
	        } catch (RemoteException e) {
	            // In this case the service has crashed before we could even
	            // do anything with it; we can count on soon being
	            // disconnected (and then reconnected if it can be restarted)
	            // so there is no need to do anything here.
	        }
	    }
	    @Override
	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        mService = null;

	    }
	};

	void doBindService() {
	    // Establish a connection with the service.  We use an explicit
	    // class name because there is no reason to be able to let other
	    // applications replace our component.
	    bindService(new Intent(this, 
	            ScareService.class), mConnection, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	}

	void doUnbindService() {
	    if (mIsBound) {
	        // If we have received the service, and hence registered with
	        // it, then now is the time to unregister.
	        if (mService != null) {
	            try {
	                Message msg = Message.obtain(null,
	                		ScareService.MSG_UNREGISTER_CLIENT);
	                msg.replyTo = mMessenger;
	                mService.send(msg);
	            } catch (RemoteException e) {
	                // There is nothing special we need to do if the service
	                // has crashed.
	            }
	        }

	        // Detach our existing connection.
	        unbindService(mConnection);
	        mIsBound = false;
	    }
	}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_home);
       
     
        
    }

    // DON'T FORGET to stop the server
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        doUnbindService();
    }
    
    @Override
    protected void onPause() {
      
      super.onPause();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	photoView = (SurfaceView) this.findViewById(R.id.photo_view);
    	pic = new PhotoHandler(context);
    	// use this to start and trigger a service
//    	Intent i= new Intent(this, ScareService.class);
//    	// potentially add data to the intent
//    	i.putExtra("ScareServ", "Value to be used by the service");
//    	Log.d(TAG, "Start Service");
//    	this.startService(i);
    	context = this;
    	doBindService();

    	//Keep the display on
    	//getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);


    	PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
    	screenWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
    	                                "screenWakeLock");
    	screenWakeLock.acquire();
    	
    	OnClickListener btnListener = new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				switch(arg0.getId()){
				case R.id.home_btn_off:
					break;
				case R.id.home_btn_vaporizer:
					 Intent intent = new Intent(context, VaporActivity.class);
					 startActivity(intent);
					break;
				case R.id.home_btn_led:
					Intent ledAct = new Intent(context, LedActivity.class);
					 startActivity(ledAct);
					break;
				}
				
			}
    		
    	};
    	
    	btnVapor = (Button) findViewById(R.id.home_btn_vaporizer);
    	btnLed = (Button) findViewById(R.id.home_btn_led);
    	btnOff = (Button) findViewById(R.id.home_btn_off);
    	btnVapor.setOnClickListener(btnListener);
    	btnLed.setOnClickListener(btnListener);
    	btnOff.setOnClickListener(btnListener);
    	
    	
//        uiIP = ((TextView)findViewById(R.id.value_ip));
//        uiTemp = ((TextView)findViewById(R.id.value_temperature));
//        uiHumid = ((TextView)findViewById(R.id.value_humidity));
//         uiNumNodes = ((TextView)findViewById(R.id.value_numnodes));
//    	 uiNodeTemp = ((TextView)findViewById(R.id.value_node1temp));
//    	 uiNodeSoil = ((TextView)findViewById(R.id.value_node1soil));
//    	 uiNodeLight = ((TextView)findViewById(R.id.value_node1light));
//        
//        btnCamera = (Button) findViewById(R.id.captureFront);
//        btnCamera.setOnClickListener(new View.OnClickListener() {
//		    @Override
//	    	public void onClick(View v) {
//				pic.takePicture(photoView,0,0);
//		    }
//    });
//        
//        btnSolenoid = ((ToggleButton)findViewById(R.id.btn_solenoidToggle));
//        btnSolenoid.setOnClickListener(new View.OnClickListener() {
//		    @Override
//	    	public void onClick(View v) {
//		    	
//				 
//					
//				
//		    	//setSolenoid(1,btnSolenoid.isChecked());
//		    }
//    });
        
//        uiIP.setText(getLocalIpAddress());
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
    
   
    
	public void turnOffDisplay(){
		/** Turn off display */
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		params.screenBrightness = .01f;
		getWindow().setAttributes(params);

		/* Create Full screen black view to detect touches */
//		LayoutInflater inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//	    view = inflater.inflate(R.layout.activity_fullscreen, null);
//	    getWindow().addContentView(view, params);
	}

}
