package com.scarecrow.watering;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.achartengine.GraphicalView;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aes.pid.R;
import com.aes.pid.R.xml;
import com.scarecrow.activities.ScareCrowActivity;
import com.scarecrow.graph.SensorGraph;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

public class WateringFragment extends Fragment {

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	
	private int gardenId = 0;
	
	private ToggleButton waterSunday,waterMonday,waterTuesday,waterWednesday,waterThursday,waterFriday,waterSaturday;
	
	private TimePicker startTime;
	
	private CheckBox adaptForecast,adaptSoil,adaptEnviron;
	
	private TextView displayDuration;
	private SeekBar seekDuration;

	private Button btnCancel;
	private Button btnSave;
	
	private RelativeLayout graphLayout;
	
	private GraphicalView graph = null;
	
	public void setGarden(int gardenId){
		this.gardenId = gardenId;
	}
	
	private ProgressDialog progressDiag;
	
	private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
	    @Override
	    protected String doInBackground(String... urls) {
	      String response = "";
	      for (String url : urls) {
	        DefaultHttpClient client = new DefaultHttpClient();
	        HttpGet httpGet = new HttpGet(url);
	        try {
	          HttpResponse execute = client.execute(httpGet);
	          InputStream content = execute.getEntity().getContent();

	          BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
	          String s = "";
	          while ((s = buffer.readLine()) != null) {
	            response += s;
	          }

	        } catch (Exception e) {
	          e.printStackTrace();
	        }
	      }
	      return response;
	    }

	    @Override
	    protected void onPostExecute(String result) {
	    	Log.d("Water frag", "REC WATER DATA");
	    	if (result.length()>5){
	        try {
	        	
				JSONObject jsonObject = new JSONObject(result);
				JSONArray waterDays = jsonObject.getJSONArray("WaterDays");
				waterSunday.setChecked(waterDays.getBoolean(0));
				waterMonday.setChecked(waterDays.getBoolean(1));
				waterTuesday.setChecked(waterDays.getBoolean(2));
				waterWednesday.setChecked(waterDays.getBoolean(3));
				waterThursday.setChecked(waterDays.getBoolean(4));
				waterFriday.setChecked(waterDays.getBoolean(5));
				waterSaturday.setChecked(waterDays.getBoolean(6));
				
				startTime.setCurrentHour(jsonObject.getInt("StartHour"));
				startTime.setCurrentMinute(jsonObject.getInt("StartMinute"));
				
				adaptForecast.setChecked(jsonObject.getBoolean("AdaptForecast"));
				adaptSoil.setChecked(jsonObject.getBoolean("AdaptSoil"));
				adaptEnviron.setChecked(jsonObject.getBoolean("AdaptEnviron"));
				
				seekDuration.setProgress(jsonObject.getInt("Duration"));
				displayDuration.setText(Integer.toString(jsonObject.getInt("Duration")));
				 progressDiag.dismiss();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	    	}
	    }
	  }
	
	public WateringFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		if (getArguments().containsKey(ARG_ITEM_ID)) {
//			// Load the dummy content specified by the fragment
//			// arguments. In a real-world scenario, use a Loader
//			// to load content from a content provider.
//			mItem = DummyContent.ITEM_MAP.get(getArguments().getString(
//					ARG_ITEM_ID));
//		}
		
		
		
		graph = null;
	}
	
	@Override 
	public void onResume() {
		super.onResume();
		progressDiag = new ProgressDialog(this.getActivity());
	      progressDiag.setTitle("Loading Security Settings");
	      progressDiag.setMessage("Download in progress ...");
	      progressDiag.setProgressStyle(progressDiag.STYLE_SPINNER);
	      progressDiag.setCancelable(false);
	      progressDiag.show();
		
		graphLayout = (RelativeLayout) this.getActivity().findViewById(R.id.soil_graph);
		if (graph==null){
		graph = SensorGraph.getGraph(SensorGraph.ZOOM_LEVEL_5DAY, new boolean[]{false,false,false,true,false,false,false,false, false});
		graphLayout.addView(graph);
		}
		
		btnCancel = (Button) this.getActivity().findViewById(R.id.btn_water_cancel);
		btnSave = (Button) this.getActivity().findViewById(R.id.btn_water_save);
		
		waterSunday = (ToggleButton) this.getActivity().findViewById(R.id.btn_days_sunday);
		waterMonday = (ToggleButton) this.getActivity().findViewById(R.id.btn_days_monday);
		waterTuesday = (ToggleButton) this.getActivity().findViewById(R.id.btn_days_tuesday);
		waterWednesday = (ToggleButton) this.getActivity().findViewById(R.id.btn_days_wednesday);
		waterThursday = (ToggleButton) this.getActivity().findViewById(R.id.btn_days_thursday);
		waterFriday = (ToggleButton) this.getActivity().findViewById(R.id.btn_days_friday);
		waterSaturday = (ToggleButton) this.getActivity().findViewById(R.id.btn_days_saturday);
		
		startTime = (TimePicker) this.getActivity().findViewById(R.id.timePicker);
		
		adaptForecast = (CheckBox) this.getActivity().findViewById(R.id.en_adaptive_forecast);
		adaptSoil = (CheckBox) this.getActivity().findViewById(R.id.en_adaptive_soil);
		adaptEnviron = (CheckBox) this.getActivity().findViewById(R.id.en_adaptive_environ);
		
		displayDuration = (TextView) this.getActivity().findViewById(R.id.display_water_duration);
		seekDuration = (SeekBar) this.getActivity().findViewById(R.id.value_water_duration);
		
		seekDuration.setOnSeekBarChangeListener(
                new OnSeekBarChangeListener() {
        @Override
      public void onProgressChanged(SeekBar seekBar, 
                                            int progresValue, boolean fromUser) {
        displayDuration.setText(Integer.toString(progresValue));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        // Do something here, 
                      //if you want to do anything at the start of
        // touching the seekbar
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        // Display the value in textview
      }
  });
	
		btnCancel.setOnClickListener(new OnClickListener() {
			 
			@Override
			public void onClick(View arg0) {
				//Switch to back to Home
				getFragmentManager().beginTransaction()
				.replace(R.id.main, ScareCrowActivity.fragHome).commit(); 
			}

		});
		
		btnSave.setOnClickListener(new OnClickListener() {
			 
			@Override
			public void onClick(View arg0) {
				//Save Data
				saveContent();

				//Switch to back to Home
				getFragmentManager().beginTransaction()
				.replace(R.id.main, ScareCrowActivity.fragHome).commit(); 
			}
			

		});
		
		
		//Load Inital Values
		updateContent();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_watering,
				container, false);

		// Show the dummy content as text in a TextView.
//		if (mItem != null) {
//			((TextView) rootView.findViewById(R.id.item_detail))
//					.setText(mItem.content);
//		}

		return rootView;
	}

	public void updateContent() {
		// TODO Auto-generated method stub
		DownloadWebPageTask task = new DownloadWebPageTask();
		switch(gardenId){
		case 1:
		    task.execute(new String[] { ScareCrowActivity.URL+Integer.toHexString(ScareCrowActivity.HTTP_SETTINGS_GARDEN1)});
			break;
		case 2:
		    task.execute(new String[] { ScareCrowActivity.URL+Integer.toHexString(ScareCrowActivity.HTTP_SETTINGS_GARDEN2)});
			break;
		}
	}

	public void saveContent() {
		JSONObject settings = new JSONObject();
		JSONArray waterDays = new JSONArray();
		try {
			waterDays.put(0, waterSunday.isChecked());
			waterDays.put(1, waterMonday.isChecked());
			waterDays.put(2, waterTuesday.isChecked());
			waterDays.put(3, waterWednesday.isChecked());
			waterDays.put(4, waterThursday.isChecked());
			waterDays.put(5, waterFriday.isChecked());
			waterDays.put(6, waterSaturday.isChecked());
			
			settings.put("WaterDays", waterDays);
			
			settings.put("StartHour", startTime.getCurrentHour());
			settings.put("StartMinute", startTime.getCurrentMinute());
			
			settings.put("AdaptForecast", adaptForecast.isChecked());
			settings.put("AdaptSoil", adaptSoil.isChecked());
			settings.put("AdaptEnviron", adaptEnviron.isChecked());
			
			settings.put("Duration", seekDuration.getProgress());
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		switch(gardenId){
		case 1:
			postJson(settings,Integer.toHexString(ScareCrowActivity.HTTP_SETTINGS_GARDEN1));
			break;
		case 2:
			postJson(settings,Integer.toHexString(ScareCrowActivity.HTTP_SETTINGS_GARDEN2));
			break;
		}
	}

	private class PostThread extends Thread{
		JSONObject object;
		String urlExt;
		
		public void setData(JSONObject object, String urlExt){
			this.object = object;
			this.urlExt = urlExt;
		}
		
		@Override
	     public void run() {
		try {		
			boolean trying = true;
			while(trying){
			URL url;
			url = new URL(ScareCrowActivity.URL+ urlExt);

		 HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
//		 httpCon.setDoOutput(true);
		 httpCon.setRequestMethod("POST");
	    httpCon.setRequestProperty("Content-Type", "application/json");
			
		 OutputStreamWriter out = new OutputStreamWriter(
		     httpCon.getOutputStream());
			out.write(object.toString());
		 out.close();

		 if (httpCon.getResponseCode() == 200){
			 trying = false;
		 }else{
			 Thread.sleep(100);
		 }
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	}
	
	public void postJson(JSONObject object, String urlExt){
		
		PostThread post = new PostThread();
		post.setData(object, urlExt);
		post.start();
	}

	
}
