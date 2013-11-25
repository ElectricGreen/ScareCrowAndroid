package com.example.servertest;

public class Data {
	public int id;
	public double temperature;
	public double humidity;
	public Long time;

	public Data(int ident, Long timeVal, double temp, double humid) {
		id = ident;
		temperature = temp;
		humidity = humid;
		time = timeVal;
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
