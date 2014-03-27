package com.example.servertest;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.servertest.NanoHTTPD.Method;
import com.example.servertest.NanoHTTPD.Response;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;

public class ScareService extends Service{
	 
public static final byte PCK_UPDATE = 0x5;
public static final byte PCK_CMD = 0x6;

public static final byte CMD_SOLENOID = 0x10;

public static final byte CMD_SET_RED = 0x50;
public static final byte CMD_SET_GREEN = 0x51;
public static final byte CMD_SET_BLUE = 0x52;
public static final byte CMD_SET_ALL = 0x53;

public static final byte HTTP_SOLENOID1_OFF = 0x11;
public static final byte HTTP_SOLENOID1_ON = 0x10;
public static final byte HTTP_SOLENOID2_OFF = 0x13;
public static final byte HTTP_SOLENOID2_ON = 0x12;
public static final byte HTTP_SOLENOID3_OFF = 0x15;
public static final byte HTTP_SOLENOID3_ON = 0x14;
public static final byte HTTP_SOLENOID4_OFF = 0x17;
public static final byte HTTP_SOLENOID4_ON = 0x16;
public static final byte HTTP_TAKE_PIC 	= 0x20;

public static final byte HTTP_SET_RED 	= 0x50;
public static final byte HTTP_SET_GREEN = 0x51;
public static final byte HTTP_SET_BLUE  = 0x52;
public static final byte HTTP_SET_ALL  = 0x53;

private static USBAccessoryManager accessoryManager; 

Packet currentPkt;

//Database
DataLogging db;
int dataCount = 0;

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

	@Override
	    public void onDestroy()
	    {
	        super.onDestroy();
	        if (server != null)
	            server.stop();
	    }
	
		  @Override
		  public int onStartCommand(Intent intent, int flags, int startId) {
		    //TODO do something useful
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
		        
		        accessoryManager.enable(this.getApplicationContext(), intent);

		    return Service.START_STICKY;
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
			  int parameter,parameter1,parameter2,command;
		    	if (uri.length()<4)
					return;

				char[] letters = uri.toCharArray();
				command = Character.getNumericValue(letters[1]) * 16;
				command += Character.getNumericValue(letters[2]);
				
				parameter =  Character.getNumericValue(letters[3]) * 16;
				parameter += Character.getNumericValue(letters[4]);

				if(uri.length()>8){

				parameter1 =  Character.getNumericValue(letters[5]) * 16;
				parameter += Character.getNumericValue(letters[6]);
				
				parameter2 =  Character.getNumericValue(letters[7]) * 16;
				parameter += Character.getNumericValue(letters[8]);
				}else{
					parameter1 = parameter2 = 0;
					
				}
				byte[] commandPacket;
				switch (command) {
				case HTTP_SOLENOID1_ON:
					Log.d(TAG, "solendoid on");
//					MainActivity.this.runOnUiThread(new Runnable() {
//						public void run() {
//							btnSolenoid.setChecked(true);
//						}
//					});
					setSolenoid(1, true);
					break;
				case HTTP_SOLENOID1_OFF:
					Log.d(TAG, "solendoid off");
//					MainActivity.this.runOnUiThread(new Runnable() {
//						public void run() {
//							btnSolenoid.setChecked(false);
//						}
//					});
					setSolenoid(1, false);
					break;
				case HTTP_TAKE_PIC:
//					camera.startPreview();
//		            camera.setDisplayOrientation(180);
//
//			    	camera.takePicture(null, null,
//			    	        new PhotoHandler(getApplicationContext()));
					break;
				case HTTP_SET_RED:
					commandPacket = new byte[4];
					commandPacket[0] = PCK_CMD;
					commandPacket[1] = CMD_SET_RED;
					commandPacket[2] = (byte) parameter;
					accessoryManager.write(commandPacket);
					break;
				case HTTP_SET_GREEN:
					commandPacket = new byte[4];
					commandPacket[0] = PCK_CMD;
					commandPacket[1] = CMD_SET_GREEN;
					commandPacket[2] = (byte) parameter;
					accessoryManager.write(commandPacket);
					break;
				case HTTP_SET_BLUE:
					commandPacket = new byte[4];
					commandPacket[0] = PCK_CMD;
					commandPacket[1] = CMD_SET_BLUE;
					commandPacket[2] = (byte) parameter;
					accessoryManager.write(commandPacket);
					break;
				case HTTP_SET_ALL:
					commandPacket = new byte[6];
					commandPacket[0] = PCK_CMD;
					commandPacket[1] = CMD_SET_ALL;
					commandPacket[2] = (byte) parameter;
					commandPacket[3] = (byte) parameter1;
					commandPacket[4] = (byte) parameter2;
					accessoryManager.write(commandPacket);
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
								currentPkt = new Packet(recMessage.data);
//								uiTemp.setText(String.format("%.2f", recPkt.baseTemp));
//								uiHumid.setText(String.format("%.2f", recPkt.baseHumid));
//								uiNumNodes.setText(Byte.toString(recPkt.numNodes));
//						    	 uiNodeTemp.setText(String.format("%.2f", recPkt.temperature[0]));
//						    	 uiNodeSoil.setText(Byte.toString(recPkt.soilSensors[0]));
//						    	 uiNodeLight.setText(Byte.toString(recPkt.lightSensors[0]));
								Date currentDate = new Date();
								Data newData = new Data(dataCount++,currentDate.getTime(),currentPkt.baseTemp,currentPkt.baseHumid,currentPkt.soilSensors[0],currentPkt.lightSensors[0],currentPkt.temperature[0],currentPkt.soilSensors[1],currentPkt.lightSensors[1],currentPkt.temperature[1]);
								db.addData(newData);		
								db.writeDataLine(newData);
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
		        	JSONObject object = new JSONObject();
		        	  try {
		        	    object.put("BaseTemp", currentPkt.baseTemp);
		        	    object.put("BaseHumidity",currentPkt.baseHumid);
		        	    object.put("Nodes", currentPkt.numNodes);
		        	    object.put("rValue", currentPkt.rValue);
		        	    object.put("gValue", currentPkt.gValue);
		        	    object.put("bValue", currentPkt.bValue);
		        	  } catch (JSONException e) {
		        	    e.printStackTrace();
		        	  }
		            String answer = object.toString();
		            
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
		  
		@Override
		public IBinder onBind(Intent intent) {
			// TODO Auto-generated method stub
			return null;
		}
}
