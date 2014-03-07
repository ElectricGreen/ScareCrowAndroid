package com.example.servertest;

public class Data {
	public int id;
	public double temperature;
	public double humidity;
	public byte[] soilSensors = new byte[2];
	public byte[] lightSensors = new byte[2];
	public double[] nodeTemp = new double[2];
	public Long time;

	public Data(int ident, Long timeVal, double temp, double humid, byte soilSensor1, byte lightSensor1, double nodeTemp1, byte soilSensor2, byte lightSensor2, double nodeTemp2) {
		id = ident;
		temperature = temp;
		humidity = humid;
		time = timeVal;
		soilSensors[0] = soilSensor1;
		soilSensors[1] = soilSensor2;
		lightSensors[0] = lightSensor1;
		lightSensors[1] = lightSensor2;
		nodeTemp[0] = nodeTemp1;
		nodeTemp[1] = nodeTemp2;
	}

	public Data() {
	}

	public double getTemp() {
		return temperature;
	}

	public double getHum() {
		return humidity;
	}

	public void setID(int key) {
		id = key;
	}
	
	public void setTime(Long timeVal) {
		time = timeVal;
	}

	public void setTemp(Double temp) {
		temperature = temp;
	}

	public void setHum(Double humid) {
		humidity = humid;
	}

	public int getID() {
		return id;
	}

	public Long getTime() {
		return time;
	}

}
