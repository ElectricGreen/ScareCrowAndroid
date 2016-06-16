package com.scarecrow.watering;
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

public class WaterLogging extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	// Database Name
	private static final String DATABASE_NAME = "SensorDatabase.db";

	// Contacts table name
	private static final String TABLE_EVENTS = "events";

	// Contacts Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_TIME = "time";
	private static final String KEY_DIRECTION = "direction";
	private static final String KEY_URL = "url";
	public WaterLogging(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_EVENT_TABLE = "CREATE TABLE " + TABLE_EVENTS + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_TIME + " LONG NOT NULL," + KEY_DIRECTION + " INTEGER NOT NULL,"
				+ KEY_URL + " STRING NOT NULL" + ")";
		db.execSQL(CREATE_EVENT_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);

		// Create tables again
		onCreate(db);
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */
	public void eraseAll(){
		SQLiteDatabase db = this.getWritableDatabase();
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);

		// Create tables again
		onCreate(db);
		db.close();
	}
	// Adding new data entry
	public void addEvent(WaterEvent data) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_ID, data.id);
		values.put(KEY_TIME, data.getTime()); // Adds the time
		values.put(KEY_DIRECTION, data.direction); // Adds the temp
		values.put(KEY_URL, data.url); // Adds the humidity
		// Inserting Row
		db.insert(TABLE_EVENTS, null, values);
		
		db.close(); // Closing database connection
	}

	// Getting All Contacts
	public List<WaterEvent> getAllEntries() {
		List<WaterEvent> dataList = new ArrayList<WaterEvent>();
		// Select All Query
		String selectQuery = "SELECT * FROM " + TABLE_EVENTS;

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				WaterEvent data = new WaterEvent();
				data.setID(Integer.parseInt(cursor.getString(0)));
				data.setTime(Long.parseLong(cursor.getString(1)));
			    data.direction = (Byte.parseByte(cursor.getString(2)));
				data.url = cursor.getString(3);
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
		public List<WaterEvent> getEntries(Date start, Date end) {
			
			List<WaterEvent> dataList = new ArrayList<WaterEvent>();
			// Select All Query
			String selectQuery = "SELECT * FROM " + TABLE_EVENTS;

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
					WaterEvent data = new WaterEvent();
					data.setID(Integer.parseInt(cursor.getString(0)));
					data.setTime(Long.parseLong(cursor.getString(1)));
				    data.direction = (Byte.parseByte(cursor.getString(2)));
					data.url = cursor.getString(3);
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
		String countQuery = "SELECT * FROM " + TABLE_EVENTS;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		db.close(); // Closing database connection
		// return count
		return count;
	}
	
	public void writeDataLine(WaterEvent values){
		File exportDir = new File(Environment.getExternalStorageDirectory(), "www");

        if (!exportDir.exists())

        {

            exportDir.mkdirs();

        }

        File file = new File(exportDir, "eventData.csv");
        if(!file.exists()){
        	exportCVS();
        }
        
        FileWriter f;
        try {
         f = new FileWriter(file,true);
             f.write("\""+values.id+"\",\""+values.time+"\",\""+Byte.toString(values.direction)+"\",\""+values.url
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

            Cursor curCSV=db.rawQuery("SELECT * FROM " + TABLE_EVENTS,null);

            csvWrite.writeNext(curCSV.getColumnNames());

            while(curCSV.moveToNext())

            {

                String arrStr[] ={curCSV.getString(0),curCSV.getString(1),curCSV.getString(2),curCSV.getString(3),curCSV.getString(4)};

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