package com.scarecrow.usb;

public class AlertPacket {
	public byte type;
	public byte alertType;
	public byte direction;
	
	public static final int ALERT_SECURITY = 0x01;
	public static final int ALERT_NODE = 0x02;
	public static final int ALERT_USER = 0x03;
	
	
		public AlertPacket(byte[] data){
			int index = 0;
			type = data[index++];
			alertType = data[index++];
			direction = data[index++];
		}
		public AlertPacket(int type, int alertType, int dir) {
			this.type = (byte) type;
			this.alertType = (byte) alertType;
			this.direction = (byte) dir;
		}
		public AlertPacket(){
			
		}
		public int bytesToInt(byte lsb, byte byte2, byte byte1, byte msb) {
			int temp = (msb & 0xFF);
			temp = ((temp << 8) & 0xFF00) + (byte1 & 0xFF);
			temp = ((temp << 8) & 0xFFFF00) + (byte2 & 0xFF);
			temp = ((temp << 8) & 0xFFFFFF00) + (lsb & 0xFF);
			return temp;
		}
}
