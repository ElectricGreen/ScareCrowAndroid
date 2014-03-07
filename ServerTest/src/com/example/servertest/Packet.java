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
	
		public Packet(byte[] data){
			type = data[1];
			numNodes = data[2];
			soilSensors[0] = data[3];
			soilSensors[1] = data[4];
			lightSensors[0] = data[5];
			lightSensors[1] = data[6];
			temperature[0] = (((data[7]<<8)&0xFF)+(data[8]&0xFF))*.1;
			temperature[1] = (((data[9]<<8)&0xFF)+(data[10]&0xFF))*.1;	
			int temp = bytesToInt(data[11],data[12],data[13],data[14]);
			baseTemp = temp*.001;
			temp = bytesToInt(data[15],data[16],data[17],data[18]);
			baseHumid = temp*.001;
			baseLight = data[19];
		}
		public int bytesToInt(byte msb, byte byte1, byte byte2, byte lsb) {
			int temp = (msb & 0xFF);
			temp = ((temp << 8) & 0xFF00) + (byte1 & 0xFF);
			temp = ((temp << 8) & 0xFFFF00) + (byte2 & 0xFF);
			temp = ((temp << 8) & 0xFFFFFF00) + (lsb & 0xFF);
			return temp;
		}
}
