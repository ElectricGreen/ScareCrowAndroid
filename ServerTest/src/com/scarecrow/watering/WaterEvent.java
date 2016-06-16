package com.scarecrow.watering;

public class WaterEvent {
	public int id;
	public Long time;
	byte direction;
	String url;
	
	public WaterEvent(int ident, Long timeVal, byte direct, String url) {
		id = ident;
		time = timeVal;
		direction = direct;
		this.url = url;
	}

	public WaterEvent() {
	}


	public void setID(int key) {
		id = key;
	}
	
	public void setTime(Long timeVal) {
		time = timeVal;
	}

	public int getID() {
		return id;
	}

	public Long getTime() {
		return time;
	}

}
