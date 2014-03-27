package com.example.servertest;

public class Packet {
	public byte type;
	public byte numNodes;
	public byte[] soilSensors = new byte[2];
	public byte[] lightSensors = new byte[2];
	public double[] temperature = new double[2];
	public double baseTemp = 0;
	public double baseHumid = 0;
	public byte baseLight = 0;
	public byte rValue = 0;
	public byte gValue = 0;
	public byte bValue = 0;
	
		public Packet(byte[] data){
			int index = 0;
			type = data[index++];
			numNodes = data[index++];
			soilSensors[0] = data[index++];
			soilSensors[1] = data[index++];
			lightSensors[0] = data[index++];
			lightSensors[1] = data[index++];
			temperature[0] = (((data[index++]<<8)&0xFF)+(data[index++]&0xFF))*.1;
			temperature[1] = (((data[index++]<<8)&0xFF)+(data[index++]&0xFF))*.1;	
			int temp = bytesToInt(data[index++],data[index++],data[index++],data[index++]);
			baseTemp = temp*.001;
			temp = bytesToInt(data[index++],data[index++],data[index++],data[index++]);
			baseHumid = temp*.001;
			baseLight = data[index++];
			rValue = data[index++];
			gValue = data[index++];
			bValue = data[index++];
		}
		public int bytesToInt(byte lsb, byte byte2, byte byte1, byte msb) {
			int temp = (msb & 0xFF);
			temp = ((temp << 8) & 0xFF00) + (byte1 & 0xFF);
			temp = ((temp << 8) & 0xFFFF00) + (byte2 & 0xFF);
			temp = ((temp << 8) & 0xFFFFFF00) + (lsb & 0xFF);
			return temp;
		}
}
