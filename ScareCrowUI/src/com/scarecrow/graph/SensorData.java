package com.scarecrow.graph;

import java.util.Date;

public class SensorData {
	public int id;
	public long time;

	public double baseTemp;
	public double baseHumid;
	public byte baseLight;
	
	public byte[] nodeSoil = new byte[2];
	public byte[] nodeLight = new byte[2];
	public double[] nodeTemp = new double[2];
	
	public byte[] nodeBattery = new byte[2];
	
	public SensorData(int ident, long timeVal, double temp, double humid, byte light, byte soilSensor1, byte lightSensor1, double nodeTemp1, byte soilSensor2, byte lightSensor2, double nodeTemp2) {
		id = ident;
		time = timeVal;
		
		baseTemp = temp;
		baseHumid = humid;
		baseLight = light;
		
		nodeSoil[0] = soilSensor1;
		nodeSoil[1] = soilSensor2;
		nodeLight[0] = lightSensor1;
		nodeLight[1] = lightSensor2;
		nodeTemp[0] = nodeTemp1;
		nodeTemp[1] = nodeTemp2;
	}
	

	public SensorData() {
	}

	public double getTemp() {
		return baseTemp;
	}

	public double getHum() {
		return baseHumid;
	}

	public void setID(int key) {
		id = key;
	}
	
	public void setTime(Long timeVal) {
		time = timeVal;
	}

	public void setTemp(Double temp) {
		baseTemp = temp;
	}

	public void setHum(Double humid) {
		baseHumid = humid;
	}

	public int getID() {
		return id;
	}

	public Long getTime() {
		return time;
	}

}
