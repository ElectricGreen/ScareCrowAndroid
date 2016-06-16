package com.scarecrow.ui;


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
import android.os.AsyncTask;
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
import android.os.SystemClock;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.scarecrow.R;
import com.scarecrow.main.ScareService;

public class VaporActivity extends Activity
{
		
	private static String TAG = "ELECTRICSCARE";
	
	private static Context context; 

	private Switch fanEnable;
	private Chronometer fanDuration;
	private ToggleButton fanPause;
	private LinearLayout offContainer, onContainer;
	private TextView speedValue;
	private SeekBar speedSet;
	private Button pauseBtn;
	
	private RadioGroup modeSelect;
	private TextView tempValue;
	
	Messenger mService = null;
	/** Flag indicating whether we have called bind on the service. */
	boolean mIsBound;

	static class IncomingHandler extends Handler {
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	        
	        }
	    }
	}
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	
	Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
	 	   @Override
	 	   public void run() {
	 		  DownloadUpdate task = new DownloadUpdate();
	 		    task.execute(new String[] { "http://192.168.1.92/info.json"});	
	 	      handler.postDelayed(this, 2000);
	 	   }
	 	};
	 	
private class DownloadUpdate extends AsyncTask<String, Void, String> {
		
	    @Override
	    protected String doInBackground(String... urls) {
	      String response = "";
	      for (String url : urls) {
	        DefaultHttpClient client = new DefaultHttpClient();
	        HttpGet httpGet = new HttpGet(url);
	        try {
	          HttpResponse execute = client.execute(httpGet);
	          InputStream content = execute.getEntity().getContent();

	          BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
	          String s = "";
	          while ((s = buffer.readLine()) != null) {
	            response += s;
	          }

	        } catch (Exception e) {
	          e.printStackTrace();
	        }
	      }
	      return response;
	      
	    }

	    @Override
	    protected void onPostExecute(String result) {
	    	if (result.length()>5){
	        try {
				JSONObject jsonObject = new JSONObject(result);
				JSONArray inputArray = jsonObject.getJSONArray("inputs");
				JSONArray setpointArray = jsonObject.getJSONArray("setpoints");
				
				tempValue.setText(Double.toString(inputArray.getJSONObject(0).getDouble("value")));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        
	    	}
	    }
	  }
	 
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
        setContentView(R.layout.screen_vaporizer);
        
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
    	context = this;
    	doBindService();
    	fanEnable = (Switch) findViewById(R.id.vapor_btn_fan_enable);
    	fanDuration = (Chronometer) findViewById(R.id.vapor_duration);
    	fanPause = (ToggleButton) findViewById(R.id.vapor_btn_pause);
    	offContainer = (LinearLayout) findViewById(R.id.vapor_container_off);
    	speedValue = (TextView) findViewById(R.id.vapor_display_fan_speed);
    	speedSet = (SeekBar) findViewById(R.id.vapor_value_fan_speed);
    	pauseBtn = (Button) findViewById(R.id.vapor_btn_pause);
    	modeSelect = (RadioGroup) findViewById(R.id.vapor_presets);
    	tempValue = (TextView) findViewById(R.id.mainTempValue);
    	tempValue.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				SetPointDialog diag = new SetPointDialog(context);
				diag.show();
			}
    		
    	});
    	speedSet.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

    		
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				speedValue.setText(Integer.toString(arg1));
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				setFanSpeed(seekBar.getProgress());
			}
    		
    	});
    	
    	modeSelect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch(checkedId){
				case R.id.vapor_preset_manual:
					speedSet.setEnabled(true);
					break;
				case R.id.vapor_preset_bag:
					speedSet.setEnabled(false);
					break;
				case R.id.vapor_preset_whip:
					speedSet.setEnabled(false);
					break;
				}
			}
    		
    	});
    	OnClickListener btnActions = new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				switch(arg0.getId()){
				case R.id.vapor_btn_pause:
					if (((ToggleButton)arg0).isChecked()){
					fanDuration.stop();
					setFanSpeed(0);
					}else{
						fanDuration.start();
						setFanSpeed(speedSet.getProgress());
					}
					break;
				}
				
			}
    		
    	};
    	
    	pauseBtn.setOnClickListener(btnActions);
    	
    	fanEnable.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (!arg1){//Off
					fanDuration.stop();
					setFanSpeed(0);
					speedSet.setEnabled(false);
					pauseBtn.setEnabled(false);
					pauseBtn.setPressed(false);
				}else{//On
					fanDuration.setBase(SystemClock.elapsedRealtime());
					fanDuration.start();
					setFanSpeed(speedSet.getProgress());
					speedSet.setEnabled(true);
					pauseBtn.setEnabled(true);
					pauseBtn.setPressed(false);
				}
				
			}
    		
    	});
    	
    	handler = new Handler();
	    handler.postDelayed(runnable, 100);
    }

	protected void setFanSpeed(int progress) {
		Message msg = Message.obtain(null,
        		ScareService.MSG_SET_FAN);
        msg.arg1 = progress;
        try {
			mService.send(msg);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
