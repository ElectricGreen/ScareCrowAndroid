package com.scarecrow.activities;


import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.aes.pid.R;
import com.scarecrow.graph.GraphFragment;
import com.scarecrow.graph.SensorData;
import com.scarecrow.graph.SensorGraph;
import com.scarecrow.home.HomeFragment;
import com.scarecrow.pagenav.PageAdapter;
import com.scarecrow.photo.DiagTimeLapse;
import com.scarecrow.photo.PhotoFragment;
import com.scarecrow.security.SecurityFragment;
import com.scarecrow.watering.WateringFragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ScareCrowActivity extends FragmentActivity {

	
	public static String URL = "http://192.168.43.1:8090/";
	public static String WEB_URL = "http://192.168.43.1:8080/";
	
	public static final byte HTTP_TAKE_PHOTO			= 0x20;
	public static final byte HTTP_READ_DATA				= 0x30;
	public static final byte HTTP_READ_EVENTS			= 0x40;

	public static final byte HTTP_SET_RED 				= 0x50;
	public static final byte HTTP_SET_GREEN 			= 0x51;
	public static final byte HTTP_SET_BLUE  			= 0x52;
	public static final byte HTTP_SET_ALL  				= 0x53;

	public static final byte HTTP_SETTINGS_SECURITY		= 0x60;
	public static final byte HTTP_SETTINGS_GARDEN1		= 0x61;
	public static final byte HTTP_SETTINGS_GARDEN2		= 0x62;
	public static final byte HTTP_SETTINGS_TIMELAPSE	= 0x63;

	public static SensorGraph sensorManager;
	
	public static void updateUI(){
		switch(currentTab){
		case 0: //Home
			HomeFragment.updateUI();
			break;
		}
	}
	
	public static HomeFragment fragHome;
	public static GraphFragment fragAuto;
	public static SecurityFragment fragSecure;
	public static PhotoFragment fragPhoto;
	public static WateringFragment fragWater;
	
	DrawerLayout drawer = null;
	final String[] menuEntries = {"Home","Graph","Security","Photography"};

    private ActionBarDrawerToggle drawerToggle;
	
	public static View view;
	/* Graph Variables */
	public static Context context;	
	
	public static boolean readyToUpdate = false;
	
	/* Nav Drawer */
	ListView navList;
	PageAdapter adapter;
	ArrayAdapter<String> adapter1;
	
	public static Menu actionBar;
	public static byte currentTab = 0;
		
    Handler mHandler = new Handler();
    	
    private static final String TAG = "AesPid"; 

	  @Override
	  public boolean onCreateOptionsMenu(Menu menu) {
		  actionBar = menu;
	    return true;
	  }

	  
	  
	  @Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.graph_settings:
//	      GraphFragment.settingsDiag();
	      break;
	    case R.id.ip_settings:
	    	HomeFragment.ipDiag();
		      break;
	    case R.id.update_security:
	    	fragSecure.updateEvents();
	    	break;
	    case R.id.save_security:
	    	fragSecure.saveContent();
	    	break;
	    case R.id.btn_time_lapse:
	    	FragmentManager fm = getSupportFragmentManager();
	        DiagTimeLapse timeLapse = new DiagTimeLapse();
	        timeLapse.show(fm, "TimeLapse");
	    	break;
	    default:
	      break;
	    }
	    return true;
	  }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		fragHome = new HomeFragment();
		fragAuto = new GraphFragment();
		fragSecure = new SecurityFragment();
		fragPhoto = new PhotoFragment();
		fragWater = new WateringFragment();
		
		sensorManager = new SensorGraph(this,10,60);
		//SensorGraph.addData(new SensorData(1,new Date().getTime(),40.4,43.3,(byte)55,(byte)34,(byte)51,43.1,(byte)34,(byte)51,43.3));
		super.onCreate(savedInstanceState);
		context = this;
		
		setContentView(R.layout.activity_item_twopane);
		
		/* Drawer Setup */
		adapter = new PageAdapter(getActionBar().getThemedContext(),menuEntries);
        adapter1 = new ArrayAdapter<String>(getActionBar().getThemedContext(), android.R.layout.simple_list_item_1, menuEntries);

        drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        navList = (ListView) findViewById(R.id.drawer);
//        getActionBar().hide();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(false);
        getActionBar().setDisplayShowCustomEnabled(false);
        
//        getActionBar().setCustomView(R.layout.action_bar);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawer,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        ) {

        	
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {

            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {

            }
        };

        drawer.setDrawerListener(drawerToggle);

        navList.setAdapter(adapter);
        this.getSupportFragmentManager().beginTransaction().replace(R.id.main, fragHome).commit();
        navList.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int pos,long id){
                drawer.setDrawerListener( new DrawerLayout.SimpleDrawerListener(){
                	@Override
                    public void onDrawerOpened(View drawerView){
                		
                	}
                    @Override
                    public void onDrawerClosed(View drawerView){
                        super.onDrawerClosed(drawerView);
                        Log.d(TAG, Byte.toString(currentTab));
                        
                        if ((currentTab!=pos)&&(currentTab!=-1))
                        takeScreenShot(currentTab);
                        adapter.notifyDataSetChanged();
                        currentTab = (byte) pos;
                        
            			switch (currentTab) {
            			case 0:// Home
            				getSupportFragmentManager().beginTransaction()
            						.replace(R.id.main, fragHome).commit();
            				actionBar.clear();
            				getMenuInflater().inflate(R.menu.bar_graph, actionBar);
            				break;
            			case 1:// AutoTune
            				getSupportFragmentManager().beginTransaction()
            						.replace(R.id.main, fragAuto).commit();
            				actionBar.clear();
            			    getMenuInflater().inflate(R.menu.bar_graph, actionBar);
            				break;
            			case 2:// Security
            				getSupportFragmentManager().beginTransaction()
            						.replace(R.id.main, fragSecure).commit();
            				actionBar.clear();
            			    getMenuInflater().inflate(R.menu.security_menu, actionBar);
            				 break;
            			case 3://Photography
            				getSupportFragmentManager().beginTransaction()
    						.replace(R.id.main, fragPhoto).commit();
            				actionBar.clear();
            			}
                        
                    }
                });
                drawer.closeDrawer(navList);
            }
        });
        
	}
	

	public void takeScreenShot(byte page){
		View v1 = null;
		switch (currentTab) {
		case 0:// Home
			v1 = fragHome.getView();
			break;
		case 1:// Graph
			v1 = fragAuto.getView();
			break;
		case 2: //Security	
			v1 = fragSecure.getView();
			break;
		case 3: //Photo
			v1 = fragPhoto.getView();
		}
		
		if (v1==null)
			return;
		
	// image naming and path  to include sd card  appending name you choose for file
	String mPath = Environment.getExternalStorageDirectory().toString() + "/" + "page"+page;   

	// create bitmap screen capture
	Bitmap bitmap;
	
	
	
	v1.setDrawingCacheEnabled(true);
	bitmap = Bitmap.createScaledBitmap(v1.getDrawingCache(),512,276,true);
	v1.setDrawingCacheEnabled(false);

	OutputStream fout = null;
	File imageFile = new File(mPath);

	try {
	    fout = new FileOutputStream(imageFile);
	    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
	    fout.flush();
	    fout.close();

	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
		}

	@Override
    public void onResume(){
    	super.onResume();
    	
    	Log.d(TAG, "onResume");
		readyToUpdate = false;
		
       
    }
	
	@Override
    public void onPause(){
    	
    	super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
