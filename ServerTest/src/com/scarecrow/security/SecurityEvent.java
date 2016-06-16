package com.scarecrow.security;

public class SecurityEvent {
	public int id;
	public Long time;
	public byte direction;
	public String url;
	public int type;
	
	public static final int EVENT_USER 		= 0x10;
	public static final int EVENT_WATER_ON	= 0x21;
	public static final int EVENT_WATER_OFF	= 0x20;
	public static final int EVENT_SECURITY	= 0x30;
	

	public SecurityEvent(int ident, Long timeVal, byte direct, String url, int type ) {
		id = ident;
		time = timeVal;
		direction = direct;
		this.url = url;
		this.type = type;
	}

	public SecurityEvent() {
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
