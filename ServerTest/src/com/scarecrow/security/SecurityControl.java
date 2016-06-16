package com.scarecrow.security;

import java.util.Calendar;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;

import com.scarecrow.main.PhotoHandler;
import com.scarecrow.main.ScareService;
import com.scarecrow.usb.AlertPacket;

public class SecurityControl {

	public boolean securityEnabled;
	public boolean secSound;
	public boolean secLed;
	public boolean secPhoto;
	public static int garden1Position;
	public static int eyePosition;
	public static int garden2Position;
	
	public static int numberOfEvents = 0;
	private Context context;
	public static PhotoHandler pic;
	
	public SecurityControl(Context mainContext){
		context = mainContext;
		securityEnabled = false;
		secSound = false;
		secLed = false;
		secPhoto = false;
		garden1Position = 1;
		eyePosition = 2;
		garden2Position = 3;	
		numberOfEvents = 0;
	}
	
	public static void addEvent(AlertPacket alert, int eventType) {
		SecurityEvent event = new SecurityEvent();
		event.id = numberOfEvents;
		event.time = Calendar.getInstance().getTimeInMillis();
		event.direction = (byte) eyePosition;
		event.type = eventType;
		switch(eventType){
		case SecurityEvent.EVENT_SECURITY:
			event.url = "Photo/Security/Photo_" + numberOfEvents + ".jpg";
			break;
		case SecurityEvent.EVENT_USER:
			event.url = "Photo/User/Photo_" + numberOfEvents + ".jpg";
			break;
		case SecurityEvent.EVENT_WATER_ON:
			event.url = "Photo/Water/Photo_" + numberOfEvents +"_ON.jpg";
			break;
		case SecurityEvent.EVENT_WATER_OFF:
			event.url = "Photo/Water/Photo_" + numberOfEvents +"_OFF.jpg";
			break;
		}		
		numberOfEvents++;
		ScareService.eventDB.addEvent(event);
		ScareService.eventDB.writeDataLine(event);
		ScareService.sendToUI(ScareService.MSG_TAKE_PIC,eventType, event.id);
	}
}
