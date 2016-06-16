package com.scarecrow.usb;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class UpdatePacket {
	public byte type;
	public byte numNodes;
	public byte[] soilSensors = new byte[2];
	public byte[] lightSensors = new byte[2];
	public byte[] nodeBatteries = new byte[2];
	public double[] temperature = new double[2];
	
	public double baseTemp = 0;
	public double baseHumid = 0;
	public byte baseLight = 0;
	public byte baseDirection = 0;
	
	public byte rValue = 0;
	public byte gValue = 0;
	public byte bValue = 0;
	
		public UpdatePacket(byte[] data){
			int index = 0;
			type = data[index++];
			numNodes = data[index++];
			soilSensors[0] = data[index++];
			soilSensors[1] = data[index++];
			lightSensors[0] = data[index++];
			lightSensors[1] = data[index++];
			nodeBatteries[0] = data[index++];
			nodeBatteries[1] = data[index++];
			
			BigDecimal decimal;		
			
//			decimal = new BigDecimal((((data[index++]<<8)&0xFF00)+(data[index++]&0xFF))*.1);
//			decimal.setScale(2, RoundingMode.CEILING);
			int temp1 = bytesToInt(data[index++],data[index++],(byte)0,(byte)0);
			decimal = new BigDecimal(temp1*.1);
			decimal.setScale(2, RoundingMode.CEILING);
			temperature[0] = decimal.doubleValue();
			
//			decimal = new BigDecimal((((data[index++]<<8)&0xFF00)+(data[index++]&0xFF))*.1);
//			decimal.setScale(2, RoundingMode.CEILING);
			temp1 = bytesToInt(data[index++],data[index++],(byte)0,(byte)0);
			decimal = new BigDecimal(temp1*.1);
			decimal.setScale(2, RoundingMode.CEILING);
			temperature[1] = decimal.doubleValue();	
			
			int temp = bytesToInt(data[index++],data[index++],data[index++],data[index++]);
			decimal = new BigDecimal(temp*.001);
			decimal.setScale(2, RoundingMode.CEILING);
			baseTemp = decimal.doubleValue();
			
			temp = bytesToInt(data[index++],data[index++],data[index++],data[index++]);
			decimal = new BigDecimal(temp*.001);
			decimal.setScale(2, RoundingMode.CEILING);
			baseHumid = decimal.doubleValue();
			
			baseLight = data[index++];
			baseDirection = data[index++];
			
			rValue = data[index++];
			gValue = data[index++];
			bValue = data[index++];
		}
		public UpdatePacket() {
			// TODO Auto-generated constructor stub
		}
		public int bytesToInt(byte lsb, byte byte2, byte byte1, byte msb) {
			int temp = (msb & 0xFF);
			temp = ((temp << 8) & 0xFF00) + (byte1 & 0xFF);
			temp = ((temp << 8) & 0xFFFF00) + (byte2 & 0xFF);
			temp = ((temp << 8) & 0xFFFFFF00) + (lsb & 0xFF);
			return temp;
		}
}
