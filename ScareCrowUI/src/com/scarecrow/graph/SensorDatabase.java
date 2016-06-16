package com.scarecrow.graph;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.scarecrow.csv.CSVWriter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class SensorDatabase extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	// Database Name
	private static final String DATABASE_NAME = "SensorDatabase.db";

	// Contacts table name
	private static final String TABLE_SENSOR = "data";

	// Contacts Table Columns names
	private static final String KEY_ID = "ID";
	private static final String KEY_TIME = "Time";
	private static final String KEY_TEMP = "BaseTemp";
	private static final String KEY_HUM = "BaseHumid";
	private static final String KEY_LIGHT = "BaseLight";
	private static final String KEY_SOIL1 = "Node1Soil";
	private static final String KEY_SOIL2 = "Node2Soil";
	private static final String KEY_LIGHT1 = "Node1Light";
	private static final String KEY_LIGHT2 = "Node2Light";
	private static final String KEY_TEMP1 = "Node1Temp";
	private static final String KEY_TEMP2 = "Node2Temp";
	
	public SensorDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_SENSOR_TABLE = "CREATE TABLE " + TABLE_SENSOR + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_TIME + " LONG NOT NULL," + KEY_TEMP + " DOUBLE NOT NULL,"
				+ KEY_HUM + " DOUBLE NOT NULL," + KEY_LIGHT + " BYTE NOT NULL,"
				+ KEY_SOIL1 + " BYTE NOT NULL," + KEY_LIGHT1 + " BYTE NOT NULL," + KEY_TEMP1 + " DOUBLE NOT NULL,"
				+ KEY_SOIL2 + " BYTE NOT NULL," + KEY_LIGHT2 + " BYTE NOT NULL," + KEY_TEMP2 + " DOUBLE NOT NULL" + ")";
		db.execSQL(CREATE_SENSOR_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSOR);

		// Create tables again
		onCreate(db);
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */
	public void eraseAll(){
		SQLiteDatabase db = this.getWritableDatabase();
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSOR);

		// Create tables again
		onCreate(db);
		db.close();
	}
	// Adding new data entry
	public void addData(SensorData data) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_ID, data.getID());
		values.put(KEY_TIME, data.getTime()); // Adds the time
		values.put(KEY_TEMP, data.getTemp()); // Adds the temp
		values.put(KEY_HUM, data.getHum()); // Adds the humidity
		values.put(KEY_LIGHT, data.baseLight);
		values.put(KEY_SOIL1, data.nodeSoil[0]);
		values.put(KEY_LIGHT1, data.nodeLight[0]);
		values.put(KEY_TEMP1, data.nodeTemp[0]);
		values.put(KEY_SOIL2, data.nodeSoil[1]);
		values.put(KEY_LIGHT2, data.nodeLight[1]);
		values.put(KEY_TEMP2, data.nodeTemp[1]);
		
		// Inserting Row
		db.insert(TABLE_SENSOR, null, values);
		
		db.close(); // Closing database connection
	}

	// Getting single data entry
	SensorData getData(int id) {
//		if (id>=this.getDataCount())
//			return null;
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_SENSOR, new String[] { KEY_ID,
				KEY_TIME, KEY_TEMP, KEY_HUM, KEY_LIGHT,KEY_SOIL1,KEY_LIGHT1,KEY_TEMP2,KEY_SOIL2,KEY_LIGHT2,KEY_TEMP2 }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor.moveToFirst()) {
//		SensorData data = new SensorData(
//				Integer.parseInt(cursor.getString(0)),Long.parseLong(cursor.getString(1)),
//				Double.parseDouble(cursor.getString(2)), Double.parseDouble(cursor.getString(3)),
//				Byte.parseByte(cursor.getString(4)), Byte.parseByte(cursor.getString(5)),
//				Byte.parseByte(cursor.getString(6)), Double.parseDouble(cursor.getString(7)),
//				Byte.parseByte(cursor.getString(8)), Byte.parseByte(cursor.getString(9)),
//				Double.parseDouble(cursor.getString(10)));
		
		SensorData data = new SensorData();
		data.setID(Integer.parseInt(cursor.getString(0)));
		data.setTime(Long.parseLong(cursor.getString(1)));
		
	    data.setTemp(Double.parseDouble(cursor.getString(2)));
		data.setHum(Double.parseDouble(cursor.getString(3)));
		data.baseLight = Byte.parseByte(cursor.getString(4));
		
		data.nodeSoil[0] = Byte.parseByte(cursor.getString(5));
		data.nodeLight[0] = Byte.parseByte(cursor.getString(6));
		data.nodeTemp[0] = Double.parseDouble(cursor.getString(7));
		
		data.nodeSoil[1] = Byte.parseByte(cursor.getString(8));
		data.nodeLight[1] = Byte.parseByte(cursor.getString(9));
		data.nodeTemp[1] = Double.parseDouble(cursor.getString(10));
		
		cursor.close();
		db.close(); // Closing database connection
		// return data entry
		return data;
		}
		return null;
	}

	// Getting All Contacts
	public List<SensorData> getAllEntries() {
		List<SensorData> dataList = new ArrayList<SensorData>();
		// Select All Query
		String selectQuery = "SELECT * FROM " + TABLE_SENSOR;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				SensorData data = new SensorData();
				data.setID(Integer.parseInt(cursor.getString(0)));
				data.setTime(Long.parseLong(cursor.getString(1)));
				
			    data.setTemp(Double.parseDouble(cursor.getString(2)));
				data.setHum(Double.parseDouble(cursor.getString(3)));
				data.baseLight = Byte.parseByte(cursor.getString(4));
				
				data.nodeSoil[0] = Byte.parseByte(cursor.getString(5));
				data.nodeLight[0] = Byte.parseByte(cursor.getString(6));
				data.nodeTemp[0] = Double.parseDouble(cursor.getString(7));
				
				data.nodeSoil[1] = Byte.parseByte(cursor.getString(8));
				data.nodeLight[1] = Byte.parseByte(cursor.getString(9));
				data.nodeTemp[1] = Double.parseDouble(cursor.getString(10));
				// Adding data to list
				dataList.add(data);
			} while (cursor.moveToNext());
		}

		// return data list
		cursor.close();
		db.close(); // Closing database connection
		return dataList;
	}
	
	// Getting All Contacts
		public List<SensorData> getEntries(Date start, Date end) {
			
			List<SensorData> dataList = new ArrayList<SensorData>();
			// Select All Query
			String selectQuery = "SELECT * FROM " + TABLE_SENSOR;

			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery(selectQuery, null);

			// looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					if (Long.parseLong(cursor.getString(1))>=start.getTime()){
						if (Long.parseLong(cursor.getString(1))>=end.getTime()){
							// return data list
							cursor.close();
							db.close(); // Closing database connection
							return dataList;
						}
						SensorData data = new SensorData();
						data.setID(Integer.parseInt(cursor.getString(0)));
						data.setTime(Long.parseLong(cursor.getString(1)));
						
					    data.setTemp(Double.parseDouble(cursor.getString(2)));
						data.setHum(Double.parseDouble(cursor.getString(3)));
						data.baseLight = Byte.parseByte(cursor.getString(4));
						
						data.nodeSoil[0] = Byte.parseByte(cursor.getString(5));
						data.nodeLight[0] = Byte.parseByte(cursor.getString(6));
						data.nodeTemp[0] = Double.parseDouble(cursor.getString(7));
						
						data.nodeSoil[1] = Byte.parseByte(cursor.getString(8));
						data.nodeLight[1] = Byte.parseByte(cursor.getString(9));
						data.nodeTemp[1] = Double.parseDouble(cursor.getString(10));
					// Adding data to list
					dataList.add(data);
					}
				} while (cursor.moveToNext());
			}

			// return data list
			cursor.close();
			db.close(); // Closing database connection
			return dataList;
		}

	// Getting data Count
	public int getDataCount() {
		String countQuery = "SELECT * FROM " + TABLE_SENSOR;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		db.close(); // Closing database connection
		// return count
		return count;
	}
	
	public void writeDataLine(SensorData values){
		File exportDir = new File(Environment.getExternalStorageDirectory(), "www");

        if (!exportDir.exists())

        {

            exportDir.mkdirs();

        }

        File file = new File(exportDir, "sensorData.csv");
        if(!file.exists()){
        	exportCVS();
        }
        
        FileWriter f;
        try {
         f = new FileWriter(file,true);
             f.write("\""+values.id+"\",\""+values.time+"\",\""+Double.toString(values.baseTemp)+"\",\""+Double.toString(values.baseHumid)
            		 +"\",\""+Byte.toString(values.nodeSoil[0])+"\",\""+Byte.toString(values.nodeLight[0])+"\",\""+Double.toString(values.nodeTemp[0])
            		 +"\",\""+Byte.toString(values.nodeSoil[1])+"\",\""+Byte.toString(values.nodeLight[1])+"\",\""+Double.toString(values.nodeTemp[1])
            		 +"\"\n");
         f.flush();
         f.close();
        }catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean exportCVS(){
		
		SQLiteDatabase db = this.getReadableDatabase();

        File exportDir = new File(Environment.getExternalStorageDirectory(), "www");

        if (!exportDir.exists())

        {

            exportDir.mkdirs();

        }

        File file = new File(exportDir, "sensorData.csv");

        try

        {

            file.createNewFile();

            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));

            Cursor curCSV=db.rawQuery("SELECT * FROM " + TABLE_SENSOR,null);

            csvWrite.writeNext(curCSV.getColumnNames());

            while(curCSV.moveToNext())

            {

                String arrStr[] ={curCSV.getString(0),curCSV.getString(1),curCSV.getString(2),curCSV.getString(3),curCSV.getString(4),curCSV.getString(5),curCSV.getString(6),curCSV.getString(7),curCSV.getString(8),curCSV.getString(9)};

            /* curCSV.getString(3),curCSV.getString(4)};*/

                csvWrite.writeNext(arrStr);

            }

            csvWrite.close();

            curCSV.close();
            
            db.close(); 


            return true;

        }

        catch(SQLException sqlEx)

        {

            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);

            return false;

        }

        catch (IOException e)

        {

            Log.e("MainActivity", e.getMessage(), e);

            return false;

        }
    }
}