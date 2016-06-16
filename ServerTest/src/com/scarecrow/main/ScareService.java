package com.scarecrow.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import com.scarecrow.datalog.Data;
import com.scarecrow.datalog.DataLogging;
import com.scarecrow.main.NanoHTTPD.Method;
import com.scarecrow.main.NanoHTTPD.Response;
import com.scarecrow.security.SecurityControl;
import com.scarecrow.security.SecurityEvent;
import com.scarecrow.security.SecurityLogging;
import com.scarecrow.usb.AlertPacket;
import com.scarecrow.usb.UpdatePacket;
import com.scarecrow.usb.USBAccessoryManager;
import com.scarecrow.usb.USBAccessoryManagerMessage;
import com.scarecrow.watering.WaterControl;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;

public class ScareService extends Service{
	
/* PACKET TYPES */
public static final byte PCK_UPDATE 				= 0x5;
public static final byte PCK_CMD 					= 0x6;
public static final byte PCK_ALERT					= 0x7;

/* ARDUINO COMMANDS */
public static final byte CMD_SOLENOID 				= 0x10;
public static final byte CMD_MOVE_HEAD				= 0x20;
public static final byte CMD_SET_SECURITY			= 0x30;
public static final byte CMD_SET_FAN				= 0x40;


/* RGB LEDS CMDS */
public static final byte CMD_SET_RED 				= 0x50;
public static final byte CMD_SET_GREEN 				= 0x51;
public static final byte CMD_SET_BLUE 				= 0x52;
public static final byte CMD_SET_ALL 				= 0x53;

/* HTTP COMMANDS */
public static final byte HTTP_SOLENOID1_OFF 		= 0x11;
public static final byte HTTP_SOLENOID1_ON 			= 0x10;
public static final byte HTTP_SOLENOID2_OFF 		= 0x13;
public static final byte HTTP_SOLENOID2_ON 			= 0x12;
public static final byte HTTP_SOLENOID3_OFF 		= 0x15;
public static final byte HTTP_SOLENOID3_ON 			= 0x14;
public static final byte HTTP_SOLENOID4_OFF 		= 0x17;
public static final byte HTTP_SOLENOID4_ON		 	= 0x16;
public static final byte HTTP_TAKE_PIC 				= 0x20;

public static final byte HTTP_READ_DATA				= 0x30;
public static final byte HTTP_READ_EVENTS			= 0x40;

public static final byte HTTP_SET_RED 				= 0x50;
public static final byte HTTP_SET_GREEN 			= 0x51;
public static final byte HTTP_SET_BLUE  			= 0x52;
public static final byte HTTP_SET_ALL  				= 0x53;

public static final byte HTTP_SETTINGS_SECURITY		= 0x60;
public static final byte HTTP_SETTINGS_GARDEN1		= 0x61;
public static final byte HTTP_SETTINGS_GARDEN2		= 0x62;


public static SecurityControl securityManager;
public static WaterControl[] wateringManager = new WaterControl[2];

private Handler historicWeatherHandler;

private static USBAccessoryManager accessoryManager; 

private int dbCount = 0;

public static UpdatePacket currentPkt;
public static AlertPacket currentAlertPkt;

public static Camera camera;

//Databases
public static DataLogging dataDB;
public static SecurityLogging eventDB;

int userPhotoCount = 0;

int dataCount = 0;
long firstTime = 0;
int eventCount = 0;

//Web Server
private WebServer server;

//Open Accessory
private boolean deviceAttached = false;

private int firmwareProtocol = 0;
private static String TAG = "ScareService";

private enum ErrorMessageCode {
	ERROR_OPEN_ACCESSORY_FRAMEWORK_MISSING,
	ERROR_FIRMWARE_PROTOCOL
};

/** For showing and hiding our notification. */
NotificationManager mNM;
/** Keeps track of all current registered clients. */
static ArrayList<Messenger> mClients = new ArrayList<Messenger>();
/** Holds last value set by a client. */
int mValue = 0;

public static final int MSG_REGISTER_CLIENT = 1;

public static final int MSG_UNREGISTER_CLIENT = 2;

public static final int MSG_TAKE_PIC = 3;

public static final int MSG_SET_FAN = 4;


class IncomingHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_REGISTER_CLIENT:
                mClients.add(msg.replyTo);
                break;
            case MSG_UNREGISTER_CLIENT:
                mClients.remove(msg.replyTo);
                break;
            case MSG_SET_FAN:
            	byte[] commandPacket;
            	commandPacket = new byte[4];
				commandPacket[0] = PCK_CMD;
				commandPacket[1] = CMD_SET_FAN;
				commandPacket[2] = (byte) msg.arg1;
				accessoryManager.write(commandPacket);
            default:
                super.handleMessage(msg);
        }
    }
}

private void showNotification() {
    // In this sample, we'll use the same text for the ticker and the expanded notification
    CharSequence text = "Scare Service Running";

    // Set the icon, scrolling text and timestamp
    Notification notification = new Notification(R.drawable.ic_launcher, text,
            System.currentTimeMillis());

    // The PendingIntent to launch our activity if the user selects this notification
    PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
            new Intent(this, MainActivity.class), 0);

    // Set the info for the views that show in the notification panel.
    notification.setLatestEventInfo(this, text,
                   text, contentIntent);

    // Send the notification.
    // We use a string id because it is a unique number.  We use it later to cancel.
    mNM.notify(R.drawable.ic_launcher, notification);
}

/**
 * Target we publish for clients to send messages to IncomingHandler.
 */
final Messenger mMessenger = new Messenger(new IncomingHandler());

private class HistoryDataTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... urls) {
      String response = "";
      for (String url : urls) {
        DefaultHttpClient client = new DefaultHttpClient();
        Log.d(TAG, "Get historic");
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
        	double humMin=0, humMax=0,tempMin=0,tempMax=0,humMinTom=0,humMaxTom=0,tempMinTom=0,tempMaxTom=0;
        	
			JSONArray histData = new JSONArray(result);
			for (int i=0; i<histData.length(); i++){
				JSONObject day = histData.getJSONObject(i);
				JSONObject tomorrow = day;
				if (i!=histData.length()-1){
					tomorrow = histData.getJSONObject(i+1);
				}
				 humMin = day.getDouble("relHumMin");
				 humMax = day.getDouble("relHumMax");
				
				tempMin = day.getDouble("tempMin");
				tempMax = day.getDouble("tempMax");
				
				humMinTom = tomorrow.getDouble("relHumMin");
				humMaxTom = tomorrow.getDouble("relHumMax");
				
				tempMinTom = tomorrow.getDouble("tempMin");
				tempMaxTom = tomorrow.getDouble("tempMax");
				
				for (int j=0; j<720; j++){
					dataDB.addData(new Data(dataCount++,firstTime,
							tempMin+(tempMax-tempMin)*(j/720.0),humMin+(humMax-humMin)*(j/720.0),(byte)(100*(j/720.0)),
							(byte)(humMin+(humMax-humMin)*(j/750.0)),(byte)(100*(j/750.0)),tempMin+(tempMax-tempMin)*(j/750.0),
							(byte)(humMin+(humMax-humMin)*(j/760.0)),(byte)(100*(j/760.0)),tempMin+(tempMax-tempMin)*(j/760.0)));
					firstTime+=60000; //increment 60s
				}
				for (int j=0; j<720; j++){
					dataDB.addData(new Data(dataCount++,firstTime,
							tempMax-(tempMax-tempMinTom)*(j/720.0),humMax-(humMax-humMinTom)*(j/720.0),(byte)(100*(1-(j/720.0))),
							(byte)(humMax-(humMax-humMinTom)*(j/730.0)),(byte)(100*(1-(j/730.0))),tempMax-(tempMax-tempMinTom)*(j/730.0),
							(byte)(humMax-(humMax-humMinTom)*(j/740.0)),(byte)(100*(1-(j/740.0))),tempMax-(tempMax-tempMinTom)*(j/740.0)));
					firstTime+=60000; //increment 60s
				}
				Log.d(TAG, "Day Complete "+i);
			}
			while(firstTime<new Date().getTime()){
				for (int j=0; j<720; j++){
					dataDB.addData(new Data(dataCount++,firstTime,
							tempMin+(tempMax-tempMin)*(j/720.0),humMin+(humMax-humMin)*(j/720.0),(byte)(100*(j/720.0)),
							(byte)(humMin+(humMax-humMin)*(j/750.0)),(byte)(100*(j/750.0)),tempMin+(tempMax-tempMin)*(j/750.0),
							(byte)(humMin+(humMax-humMin)*(j/760.0)),(byte)(100*(j/760.0)),tempMin+(tempMax-tempMin)*(j/760.0)));
					firstTime+=60000; //increment 60s
				}
				for (int j=0; j<720; j++){
					dataDB.addData(new Data(dataCount++,firstTime,
							tempMax-(tempMax-tempMinTom)*(j/720.0),humMax-(humMax-humMinTom)*(j/720.0),(byte)(100*(1-(j/720.0))),
							(byte)(humMax-(humMax-humMinTom)*(j/730.0)),(byte)(100*(1-(j/730.0))),tempMax-(tempMax-tempMinTom)*(j/730.0),
							(byte)(humMax-(humMax-humMinTom)*(j/740.0)),(byte)(100*(1-(j/740.0))),tempMax-(tempMax-tempMinTom)*(j/740.0)));
					firstTime+=60000; //increment 60s
				}
			}
			Log.d(TAG, "Data Generation Complete");
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    	}
    }
  }




private Runnable runnable = new Runnable() {
	   @Override
	   public void run() {
		   HistoryDataTask task = new HistoryDataTask();
		    task.execute(new String[] { "https://api.weathersource.com/v1/b95c167449ad979afc69/history_by_postal_code.json?period=day&postal_code_eq=01002&limit=25&country_eq=US&timestamp_between=2014-04-05T00:00:00-05:00,2014-05-00T00:00:00-05:00&fields=tempMax,tempAvg,tempMin,relHumMax,relHumAvg,relHumMin,timestamp/"});	
	      //handler.postDelayed(this, 50000);
	   }
	};
	
	@Override
	    public void onDestroy()
	    {
	        super.onDestroy();
	        Log.d(TAG, "OnDestroy");
	        mNM.cancel(R.drawable.ic_launcher);

	        
	        if (server != null)
	            server.stop();
	    }
	
	 @Override
	    public void onCreate() {
		        Log.d(TAG, "OnCreate");
		        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

			  showNotification();
			  currentPkt = new UpdatePacket();
			  
			  securityManager = new SecurityControl(this);
			  wateringManager[0] = new WaterControl();
			  wateringManager[1] = new WaterControl();
			  
			  /* Initialize Values for testing */
//			  currentPkt.baseTemp = 75.5;
//			  currentPkt.baseHumid = 35.3;
//			  currentPkt.baseLight = 65;
//			  currentPkt.baseDirection = 2;
//			  
//			  currentPkt.temperature[0] = 0;
//			  currentPkt.temperature[1] = 0;
//			  
//			  currentPkt.lightSensors[0] = 0;
//			  currentPkt.lightSensors[1] = 0;
//			  
//			  currentPkt.nodeBatteries[0] = 0;
//			  currentPkt.nodeBatteries[1] = 0;
//			  
//			  currentPkt.soilSensors[0] = 0;
//			  currentPkt.soilSensors[1] = 0;
//			  
//			  currentPkt.numNodes = 0;
//			  
//			  currentPkt.rValue = 55;
//			  currentPkt.gValue = 55;
//			  currentPkt.bValue = 55;
			 
			  
			  Log.d(TAG, "onStartCommand");
			  eventDB = new SecurityLogging(this);
			  eventDB.eraseAll();
			  
			  dataDB = new DataLogging(this);
			  if (dataDB.getDataCount()==0){
//			  dataDB.eraseAll();
			    Calendar c = Calendar.getInstance();
				c.set(Calendar.MONTH, 3); //Starts at 0
				c.set(Calendar.DATE, 5);
				c.set(Calendar.YEAR, 2014);
				c.set(Calendar.HOUR, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.SECOND, 0);
			  
			  firstTime = c.getTime().getTime();
			    historicWeatherHandler = new Handler();
			    historicWeatherHandler.postDelayed(runnable, 100);
			  }
//			  HistoryDataTask task = new HistoryDataTask();
//			    task.execute(new String[] { "https://api.weathersource.com/v1/b95c167449ad979afc69/history_by_postal_code.json?"
//			    		+ "period=day&postal_code_eq=01002&limit=25&country_eq=US&"
//			    		+ "timestamp_between="+(currentDate.getYear()+1900)+"-"+currentDate.getMonth()+"-"+currentDate.getDay()+"T00:00:00-05:00,"
//			    				+(earlyDate.getYear()+1900)+"-"+earlyDate.getMonth()+"-"+earlyDate.getDay()+"T00:00:00-05:00&"
//			    		+ "fields=tempMax,tempMin,relHumMax,relHumMin"});
//			  task.execute("https://api.weathersource.com/v1/b95c167449ad979afc69/history_by_postal_code.json?period=day&postal_code_eq=01002&limit=25&country_eq=US&timestamp_between=2014-03-19T00:00:00-05:00,2014-04-12T00:00:00-05:00&fields=tempMax,tempAvg,tempMin,relHumMax,relHumAvg,relHumMin");

			  

			  
		        
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
		        
		        accessoryManager.enable(this.getApplicationContext());

//		    return Service.START_STICKY;
		  }
		  
		  public static void setSolenoid(int solenoid, boolean state){
				byte[] commandPacket = new byte[4];
				commandPacket[0] = PCK_CMD;
				commandPacket[1] = CMD_SOLENOID;
				commandPacket[2] = (byte) solenoid;
				commandPacket[3] = (byte) (state?1:0);
				if (accessoryManager!=null)
				accessoryManager.write(commandPacket);
		    }
		  
		  public String httpResponse(String uri){
			  
			  int[] parameters = new int[((uri.length()-1)/2)-1];
			  
				char[] letters = uri.toCharArray();

				int command;
				command = Character.getNumericValue(letters[1]) * 16;
				command += Character.getNumericValue(letters[2]);
				
				for (int i=0;i<parameters.length;i++){
				parameters[i] =  Character.getNumericValue(letters[3+(i*2)]) * 16;
				parameters[i] += Character.getNumericValue(letters[4+(i*2)]);
				}
				
				byte[] commandPacket;
				switch (command) {
				
				case HTTP_SETTINGS_SECURITY:
					JSONObject settings = new JSONObject();
					
					try {
					settings.put("SecEnable",securityManager.securityEnabled);
					settings.put("SecSound",securityManager.secSound);
					settings.put("SecLed",securityManager.secLed);
					settings.put("SecPhoto",securityManager.secPhoto);
					settings.put("g1Position", securityManager.garden1Position);
					settings.put("g2Position", securityManager.garden2Position);
					settings.put("eyePosition", securityManager.eyePosition);
					
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					return settings.toString();
					
				case HTTP_SETTINGS_GARDEN1:
					JSONObject g1Settings = new JSONObject();
					JSONArray waterDays = new JSONArray();
					try {
						for (int i=0;i<7;i++){
							waterDays.put(i, wateringManager[0].waterDays[i]);
						}
						
						g1Settings.put("WaterDays", waterDays);
						
						g1Settings.put("StartHour", wateringManager[0].hour);
						g1Settings.put("StartMinute", wateringManager[0].minute);
						
						g1Settings.put("AdaptForecast", wateringManager[0].adaptForecast);
						g1Settings.put("AdaptSoil", wateringManager[0].adaptSoil);
						g1Settings.put("AdaptEnviron", wateringManager[0].adaptEnviron);
						
						g1Settings.put("Duration", wateringManager[0].seekDuration);
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return g1Settings.toString();
					
				case HTTP_SETTINGS_GARDEN2:
					JSONObject g2Settings = new JSONObject();
					JSONArray water2Days = new JSONArray();
					try {
						for (int i=0;i<7;i++){
							water2Days.put(i, wateringManager[1].waterDays[i]);
						}
						
						g2Settings.put("WaterDays", water2Days);
						
						g2Settings.put("StartHour", wateringManager[1].hour);
						g2Settings.put("StartMinute", wateringManager[1].minute);
						
						g2Settings.put("AdaptForecast", wateringManager[1].adaptForecast);
						g2Settings.put("AdaptSoil", wateringManager[1].adaptSoil);
						g2Settings.put("AdaptEnviron", wateringManager[1].adaptEnviron);
						
						g2Settings.put("Duration", wateringManager[1].seekDuration);
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return g2Settings.toString();
					
				case HTTP_READ_DATA:
					long readFrom = parameters[0]<<24;
					readFrom += parameters[1]<<16;
					readFrom += parameters[2]<<8;
					readFrom += parameters[3];
					List<Data> selectedData = dataDB.getEntriesBatch(readFrom);
					JSONArray array = new JSONArray();
					for (int i=0; i<selectedData.size();i++){
						Data currentData = selectedData.get(i);
						JSONObject object = new JSONObject();
						try {
						object.put("id", currentData.id);
						object.put("time", currentData.time);
						object.put("baseTemp", currentData.baseTemp);
						object.put("baseHumid", currentData.baseHumid);
						object.put("baseLight", currentData.baseLight);
						object.put("n1Temp", currentData.nodeTemp[0]);
						object.put("n1Light", currentData.nodeLight[0]);
						object.put("n1Soil", currentData.nodeSoil[0]);
						object.put("n2Temp", currentData.nodeTemp[1]);
						object.put("n2Light", currentData.nodeLight[1]);
						object.put("n2Soil", currentData.nodeSoil[1]);
						array.put(object);
						
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (i>20000){
							selectedData = null;
							return array.toString();
						}
					}
					selectedData = null;
					return array.toString();
					
				case HTTP_READ_EVENTS:
					long readFrom1 = parameters[0]<<24;
					readFrom1 += parameters[1]<<16;
					readFrom1 += parameters[2]<<8;
					readFrom1 += parameters[3];
					List<SecurityEvent> selectedEvents = eventDB.getEntriesBatch(readFrom1);
					JSONArray arrayEvents = new JSONArray();
					for (int i=0; i<selectedEvents.size();i++){
						SecurityEvent currentData = selectedEvents.get(i);
						JSONObject object = new JSONObject();
						try {
							object.put("id", currentData.id);
						object.put("time", currentData.time);
						object.put("direction", currentData.direction);
						object.put("url", currentData.url);
						object.put("type", currentData.type);
						arrayEvents.put(object);
						
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					return arrayEvents.toString();

				case HTTP_SOLENOID1_ON:
					Log.d(TAG, "solendoid on");
//					MainActivity.this.runOnUiThread(new Runnable() {
//						public void run() {
//							btnSolenoid.setChecked(true);
//						}
//					});
					setSolenoid(0, true);
					break;
				case HTTP_SOLENOID1_OFF:
					Log.d(TAG, "solendoid off");
//					MainActivity.this.runOnUiThread(new Runnable() {
//						public void run() {
//							btnSolenoid.setChecked(false);
//						}
//					});
					setSolenoid(0, false);
					break;
				case HTTP_TAKE_PIC:
					commandPacket = new byte[4];
					commandPacket[0] = PCK_CMD;
					commandPacket[1] = CMD_MOVE_HEAD;
					commandPacket[2] = (byte) securityManager.eyePosition;
					accessoryManager.write(commandPacket);

					AlertPacket pic = new AlertPacket();
					pic.alertType = SecurityEvent.EVENT_USER;
					pic.direction = currentPkt.baseDirection;
					securityManager.addEvent(pic, SecurityEvent.EVENT_USER);
					return " ";
				case HTTP_SET_RED:
					commandPacket = new byte[4];
					commandPacket[0] = PCK_CMD;
					commandPacket[1] = CMD_SET_RED;
					commandPacket[2] = (byte) parameters[0];
					accessoryManager.write(commandPacket);
					break;
				case HTTP_SET_GREEN:
					commandPacket = new byte[4];
					commandPacket[0] = PCK_CMD;
					commandPacket[1] = CMD_SET_GREEN;
					commandPacket[2] = (byte) parameters[0];
					accessoryManager.write(commandPacket);
					break;
				case HTTP_SET_BLUE:
					commandPacket = new byte[4];
					commandPacket[0] = PCK_CMD;
					commandPacket[1] = CMD_SET_BLUE;
					commandPacket[2] = (byte) parameters[0];
					accessoryManager.write(commandPacket);
					break;
				case HTTP_SET_ALL:
					commandPacket = new byte[6];
					commandPacket[0] = PCK_CMD;
					commandPacket[1] = CMD_SET_ALL;
					commandPacket[2] = (byte) parameters[0];
					commandPacket[3] = (byte) parameters[1];
					commandPacket[4] = (byte) parameters[2];
					accessoryManager.write(commandPacket);
				}
				return "";
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
								currentPkt = new UpdatePacket(recMessage.data);
								
								if (dbCount == 6){ //
									Data newData = new Data(dataCount++,new Date().getTime(),currentPkt.baseTemp,currentPkt.baseHumid,currentPkt.baseLight,currentPkt.soilSensors[0],currentPkt.lightSensors[0],currentPkt.temperature[0],currentPkt.soilSensors[1],currentPkt.lightSensors[1],currentPkt.temperature[1]);
									dataDB.addData(newData);		
									dataDB.writeDataLine(newData);
									 
									writeToXively();
									dbCount = 0;
								}else{
									dbCount++;
								}
								break;
							case PCK_ALERT:
								currentAlertPkt = new AlertPacket(recMessage.data);
								securityManager.addEvent(currentAlertPkt, SecurityEvent.EVENT_SECURITY);
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
			
		    
		    
		    
		    public static void sendToUI(int what, int arg1, int arg2){
		    	for (int i=mClients.size()-1; i>=0; i--) {
                    try {
		    	 mClients.get(i).send(Message.obtain(null,
                         what, arg1, arg2));
                    } catch (RemoteException e) {
                        // The client is dead.  Remove it from the list;
                        // we are going through the list from back to front
                        // so this is safe to do inside the loop.
                        mClients.remove(i);
                    }
		    	}
		    }
		    
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
					String answer = null;

		        	switch(method){
					case DELETE:
						break;
					case GET:
			        	if (uri.length()<3){
			        		JSONObject object = new JSONObject();
				        	  try {
									object.put("baseTemp", currentPkt.baseTemp);
									object.put("baseHumid", currentPkt.baseHumid);
									object.put("baseLight", currentPkt.baseLight);
									object.put("n1Temp", currentPkt.temperature[0]);
									object.put("n1Light", currentPkt.lightSensors[0]);
									object.put("n1Soil", currentPkt.soilSensors[0]);
									object.put("n1Battery", currentPkt.nodeBatteries[0]);
									object.put("n2Temp", currentPkt.temperature[1]);
									object.put("n2Light", currentPkt.lightSensors[1]);
									object.put("n2Soil", currentPkt.soilSensors[1]);
									object.put("n2Battery", currentPkt.nodeBatteries[1]);
				        	  } catch (JSONException e) {
				        	    e.printStackTrace();
				        	  }
				          answer  = object.toString();
			        	}else{
			        	 answer = httpResponse(uri);
			        	}
						break;
					case HEAD:
						break;
					case OPTIONS:
						break;
					case POST:
						int command;
						char[] letters = uri.toCharArray();
						command = Character.getNumericValue(letters[1]) * 16;
						command += Character.getNumericValue(letters[2]);
						switch(command){
						case HTTP_SETTINGS_SECURITY:
							try {
								String param = parameters.toString();
								int index = param.indexOf("{\"g1Position\"");
								param = param.substring(index);
							JSONObject jsonObject = new JSONObject(param);
							securityManager.securityEnabled = jsonObject.getBoolean("SecEnable");
							securityManager.secSound = jsonObject.getBoolean("SecSound");
							securityManager.secLed = jsonObject.getBoolean("SecLed");
							securityManager.secPhoto = jsonObject.getBoolean("SecPhoto");
							securityManager.garden1Position = jsonObject.getInt("g1Position");
							securityManager.garden2Position = jsonObject.getInt("g2Position");
							securityManager.eyePosition = jsonObject.getInt("eyePosition");
							byte[] commandPacket = new byte[5];
							commandPacket[0] = PCK_CMD;
							commandPacket[1] = CMD_SET_SECURITY;
							commandPacket[2] = (byte) (securityManager.securityEnabled?1:0);
							commandPacket[3] = (byte) (securityManager.secSound?1:0);
							commandPacket[4] = (byte) (securityManager.secLed?1:0);
							accessoryManager.write(commandPacket);
							
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							break;
							
						case HTTP_SETTINGS_GARDEN1:
							try {
								String param = parameters.toString();
								int index = param.indexOf("{\"StartHour\"");
								param = param.substring(index);
							JSONObject jsonObject = new JSONObject(param);
							JSONArray waterDays = (JSONArray) jsonObject.get("WaterDays");
							
							for (int i=0; i<7; i++){
								wateringManager[0].waterDays[i] = waterDays.getBoolean(i);
							}
							
							wateringManager[0].hour = jsonObject.getInt("StartHour");
							wateringManager[0].minute = jsonObject.getInt("StartMinute");
							
							wateringManager[0].adaptForecast = jsonObject.getBoolean("AdaptForecast");
							wateringManager[0].adaptSoil = jsonObject.getBoolean("AdaptSoil");
							wateringManager[0].adaptEnviron = jsonObject.getBoolean("AdaptEnviron");
							
							wateringManager[0].seekDuration = jsonObject.getInt("Duration");
							
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
							
						case HTTP_SETTINGS_GARDEN2:
							try {
								String param = parameters.toString();
								int index = param.indexOf("{\"StartHour\"");
								param = param.substring(index);
								JSONObject jsonObject = new JSONObject(param);
								JSONArray waterDays = jsonObject.getJSONArray("WaterDays");
								
								for (int i=0; i<7; i++){
									wateringManager[1].waterDays[i] = waterDays.getBoolean(i);
								}
								
								wateringManager[1].hour = jsonObject.getInt("StartHour");
								wateringManager[1].minute = jsonObject.getInt("StartMinute");
								
								wateringManager[1].adaptForecast = jsonObject.getBoolean("AdaptForecast");
								wateringManager[1].adaptSoil = jsonObject.getBoolean("AdaptSoil");
								wateringManager[1].adaptEnviron = jsonObject.getBoolean("AdaptEnviron");
								
								wateringManager[1].seekDuration = jsonObject.getInt("Duration");
								
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							break;
						}
						break;
					case PUT:
						break;
					default:
						break;
		        	
		        	}
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
			protected void writeToXively() {
				 Thread thread = new Thread()
				 {
				     @Override
				     public void run() {
				        	 try{
				        		 JSONObject object = new JSONObject();
				        		 JSONObject tempJson = new JSONObject();
				        		 JSONObject humidJson = new JSONObject();
				        		 JSONArray array = new JSONArray();
								 try {

									 tempJson.put("id", "BaseTemp");
									 tempJson.put("current_value", currentPkt.baseTemp);
									 humidJson.put("id", "BaseHumidity");
									 humidJson.put("current_value", currentPkt.baseHumid);

									array.put(tempJson);
									array.put(humidJson);
									object.put("version","1.0.0");
									object.put("datastreams", array);
								 } catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
				        		 
				        	 URL url = new URL("https://api.xively.com/v2/feeds/1183483398.json");
							 HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
							 httpCon.setDoOutput(true);
							 httpCon.setRequestMethod("PUT");
							 httpCon.setRequestProperty("X-ApiKey", "lbiTSpMgKn9mahMOO00SUieSPV6Ry71zuZlP9x6dMGg4mDIU");
						    httpCon.setRequestProperty("Content-Type", "application/json");
								
							 OutputStreamWriter out = new OutputStreamWriter(
							     httpCon.getOutputStream());
								out.write(object.toString());
							 out.close();
							 Log.d("Xlively","response"+httpCon.getResponseCode());
							 } catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
				         
				     }
				 };

				 thread.start();
				
			}

			public void intToBytes(byte[] data, int index, int value){
				data[index++] = (byte) ((value>>24)&0xFF);
				data[index++] = (byte) ((value>>16)&0xFF);
				data[index++] = (byte) ((value>>8)&0xFF);
				data[index++] = (byte) (value&0xFF);
				return;
			}
		  
		@Override
		public IBinder onBind(Intent intent) {
		    return mMessenger.getBinder();
		}
}
