package com.scarecrow.watering;

import java.util.Date;

import com.scarecrow.main.ScareService;
import com.scarecrow.security.SecurityControl;
import com.scarecrow.security.SecurityEvent;
import com.scarecrow.usb.AlertPacket;

public class WaterControl {

	public boolean[] waterDays = new boolean[7];
	public int hour;
	public int minute;
	
	public boolean adaptForecast, adaptSoil, adaptEnviron;
	
	public int seekDuration;
	
	public WaterControl(){
		for (int i=0;i<7;i++){
			waterDays[i] = false;
		}
		hour = 21;
		minute = 30;
		adaptForecast = false;
		adaptSoil = false;
		adaptEnviron = false;
		
		Runnable runWater = new Runnable(){

			@Override
			public void run() {
				int count = 0;
				boolean watering = false;
				while(true){
					try {

					Date test = new Date();
					if (watering){
						count++;
						if ((count>=seekDuration)){
							ScareService.setSolenoid(0, false);
							AlertPacket waterOff = new AlertPacket(0,SecurityEvent.EVENT_WATER_OFF,SecurityControl.garden1Position);
							SecurityControl.addEvent(waterOff, SecurityEvent.EVENT_WATER_OFF);
							watering =false;
							count = 0;
						}else{
							Thread.sleep(60000);
						}
					}else if (((test.getHours()==hour)&&(test.getMinutes()==minute)&&(waterDays[test.getDay()]))){
						//Correct Time Start watering
						ScareService.setSolenoid(0, true);
						AlertPacket waterOn = new AlertPacket(0,SecurityEvent.EVENT_WATER_ON,SecurityControl.garden1Position);
						SecurityControl.addEvent(waterOn, SecurityEvent.EVENT_WATER_ON);
						watering = true;
						count = 0;
						Thread.sleep(60000);
					}else{
							Thread.sleep(60000);
						
					}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		};
		new Thread(runWater).start();
	}
}
