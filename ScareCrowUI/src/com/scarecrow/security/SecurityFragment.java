package com.scarecrow.security;


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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aes.pid.R;
import com.aes.pid.R.xml;
import com.scarecrow.activities.ScareCrowActivity;
import com.scarecrow.graph.SensorData;
import com.scarecrow.graph.SensorGraph;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SecurityFragment extends Fragment {

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	
	private ListView eventListView;
	
	private LinearLayout quadNW, quadN, quadNE, quadW, quadE, quadSW, quadS, quadSE;
	RelativeLayout quadH;
	private RelativeLayout icGarden1, icGarden2;
	ImageView icScarecrow,icScarecrowEye;
	EventAdapter eventAdapter;
	
	//Interface
	private ToggleButton securityEnable;
	private ImageButton btnPhoto,btnUpdate;
	private ToggleButton secSound,secLed,secPhoto;
	
	
	//Values
	int garden1Position = 0;
	int garden2Position = 0;
	int eyePosition = 0;
	
	private ProgressDialog progressDiag;

	private class TakePhoto extends AsyncTask<String, Void, String> {
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
		      
	    }
	  }
	
	private class DownloadEvents extends AsyncTask<String, Void, String> {
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
		      
	    	if (result.length()>5){
	    		progressDiag = new ProgressDialog(appContext);
			      progressDiag.setTitle("Downloading Events");
			      progressDiag.setMessage("Download in progress ...");
			      progressDiag.setProgressStyle(progressDiag.STYLE_HORIZONTAL);
			      progressDiag.setProgress(0);
			      progressDiag.setMax(100);
			      progressDiag.setCancelable(false);
			      progressDiag.show();
	    		
	    		ParseData parser = new ParseData();
	    		parser.setData(result);
	    		new Thread(parser).start();
	    	}
	    }
	  }
	
	public void updateEvents(){
		eventAdapter.clear();
		DownloadEvents task = new DownloadEvents();
		    task.execute(new String[] { ScareCrowActivity.URL+Integer.toHexString(ScareCrowActivity.HTTP_READ_EVENTS)+"00000000"});	
	}
	
	private class ParseData implements Runnable {
		 String dataString;
		 private class BackgroundTask extends AsyncTask<String, Void, Bitmap> {
			    protected void onPostExecute(Bitmap result) {
			    	eventAdapter.notifyDataSetChanged();
			    	}

				@Override
				protected Bitmap doInBackground(String... params) {
					return null;
				}
			}
		 BackgroundTask updateAdapter;
	 		public void setData(String data){
		 		dataString = data;
		 	}
	 		@Override
		 	   public void run() {
		  			  
		        try {
		        	JSONArray dataArray = new JSONArray(dataString);
		        	progressDiag.setMax(dataArray.length());
		        	for (int i=0;i<dataArray.length();i++){
		        			progressDiag.incrementProgressBy(1);
					JSONObject jsonObject = dataArray.getJSONObject(i);
					Event newEvent = new Event();
					newEvent.id = jsonObject.getInt("id");
					newEvent.time = jsonObject.getLong("time");
					newEvent.direction = (byte) jsonObject.getInt("direction");
					newEvent.url = jsonObject.getString("url");
					newEvent.type = jsonObject.getInt("type");
					eventAdapter.addEvent(newEvent);
			        }
				} catch (JSONException e) {

					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        progressDiag.dismiss();

		        updateAdapter = new BackgroundTask();
		        updateAdapter.execute("");
		 	   }
		 	};
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
	    	if (result.length()>5){
	        try {
				JSONObject jsonObject = new JSONObject(result);
				securityEnable.setChecked(jsonObject.getBoolean("SecEnable"));
				secSound.setChecked(jsonObject.getBoolean("SecSound"));
				secLed.setChecked(jsonObject.getBoolean("SecLed"));
				secPhoto.setChecked(jsonObject.getBoolean("SecPhoto"));
				garden1Position = jsonObject.getInt("g1Position");
				garden2Position = jsonObject.getInt("g2Position");
				eyePosition = jsonObject.getInt("eyePosition");
				setGardenPosition();
				progressDiag.dismiss();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	    	}
	    }
	  }
	
	 private final class MyTouchListener implements OnTouchListener {
		    public boolean onTouch(View view, MotionEvent motionEvent) {
		      if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
		        ClipData data = ClipData.newPlainText("", "");
		        DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
		        view.startDrag(data, shadowBuilder, view, 0);
		        view.setVisibility(View.INVISIBLE);
		        return true;
		      } else {
		        return false;
		      }
		    }
		  }

		  class MyDragListener implements OnDragListener {
		    Drawable enterShape = getResources().getDrawable(xml.quadrant_drop);
		    Drawable normalShape = getResources().getDrawable(xml.quadrant);

		    @Override
		    public boolean onDrag(View v, DragEvent event) {
		      int action = event.getAction();
		      switch (event.getAction()) {
		      case DragEvent.ACTION_DRAG_STARTED:
		        // do nothing
		        break;
		      case DragEvent.ACTION_DRAG_ENTERED:
		        v.setBackgroundDrawable(enterShape);
		        break;
		      case DragEvent.ACTION_DRAG_EXITED:
		        v.setBackgroundDrawable(normalShape);
		        break;
		      case DragEvent.ACTION_DROP:
		        // Dropped, reassign View to ViewGroup
		        View view = (View) event.getLocalState();
		        ViewGroup owner = (ViewGroup) view.getParent();
		        owner.removeView(view);
		        LinearLayout container = (LinearLayout) v;
		        container.addView(view);
		        view.setVisibility(View.VISIBLE);
		        byte direction = 0;
		        switch(container.getId()){
		        case R.id.quad_nw:
		        	direction = 1;
		        	break;
		        case R.id.quad_n:
		        	direction = 2;
		        	break;
		        case R.id.quad_ne:
		        	direction = 3;
		        	break;
		        case R.id.quad_w:
		        	direction = 8;
		        	break;
		        case R.id.quad_e:
		        	direction = 4;
		        	break;
		        case R.id.quad_sw:
		        	direction = 7;
		        	break;
		        case R.id.quad_s:
		        	direction = 6;
		        	break;
		        case R.id.quad_se:
		        	direction = 5;
		        	break;
		        }
		        switch(view.getId()){
		        case R.id.ic_garden1:
		        	garden1Position = direction;
		        	break;
		        case R.id.ic_garden2:
		        	garden2Position = direction;
		        	break;
		        case R.id.ic_scarecrow_eye:
		        	eyePosition = direction;
		        	icScarecrow.setPivotX(icScarecrow.getWidth()/2);
		        	icScarecrow.setPivotY(icScarecrow.getHeight()/2);
		        	icScarecrow.setRotation(direction*45);
		        	break;
		        }
		        break;
		      case DragEvent.ACTION_DRAG_ENDED:
		        v.setBackgroundDrawable(normalShape);
		      default:
		        break;
		      }
		      return true;
		    }
		  }
	
	public SecurityFragment() {
	}
	
	public void addView(int direction, View child){
		switch(direction){
		case 1:
			quadNW.addView(child);
			break;
			
		case 2:
			quadN.addView(child);
			break;
			
		case 3:
			quadNE.addView(child);
			break;
			
		case 4:
			quadE.addView(child);
			break;
			
		case 5:
			quadSE.addView(child);
			break;
			
		case 6:
			quadS.addView(child);
			break;
			
		case 7:
			quadSW.addView(child);
			break;
			
		case 8:
			quadW.addView(child);
			break;
			
		}
	}

	public void setGardenPosition() {
		quadNW.removeAllViews();
		quadN.removeAllViews();
		quadNE.removeAllViews();
		quadW.removeAllViews();
		quadE.removeAllViews();
		quadSW.removeAllViews();
		quadS.removeAllViews();
		quadSE.removeAllViews();
		
		addView(garden1Position,icGarden1);
		addView(garden2Position,icGarden2);
		addView(eyePosition,icScarecrowEye);
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
		
		
		
		
	}
	
	private Context appContext;
	@Override 
	public void onResume() {
		super.onResume();
		appContext = this.getActivity();
		progressDiag = new ProgressDialog(appContext);
	      progressDiag.setTitle("Loading Security Settings");
	      progressDiag.setMessage("Download in progress ...");
	      progressDiag.setProgressStyle(progressDiag.STYLE_SPINNER);
	      progressDiag.setCancelable(false);
	      progressDiag.show();
	      
	      btnUpdate = (ImageButton) this.getActivity().findViewById(R.id.btn_update_events);
	      btnUpdate.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
		    	updateEvents();
			}
	    	  
	      });
	      
		securityEnable = (ToggleButton) this.getActivity().findViewById(R.id.sw_security_enable);
		btnPhoto = (ImageButton) this.getActivity().findViewById(R.id.btn_take_photo);
		secSound = (ToggleButton) this.getActivity().findViewById(R.id.security_sound);
		secLed = (ToggleButton) this.getActivity().findViewById(R.id.security_led);
		secPhoto = (ToggleButton) this.getActivity().findViewById(R.id.security_picture);
		
		icGarden1 = (RelativeLayout) this.getActivity().findViewById(R.id.ic_garden1);
		icGarden1.setOnTouchListener(new MyTouchListener());
		icGarden2 =  (RelativeLayout) this.getActivity().findViewById(R.id.ic_garden2);
		icGarden2.setOnTouchListener(new MyTouchListener());
		
		icScarecrow = (ImageView) this.getActivity().findViewById(R.id.ic_scarecrow);
		
		icScarecrowEye = (ImageView)this.getActivity().findViewById(R.id.ic_scarecrow_eye);
		icScarecrowEye.setOnTouchListener(new MyTouchListener());
		
		quadNW = (LinearLayout) this.getActivity().findViewById(R.id.quad_nw);
		quadN = (LinearLayout) this.getActivity().findViewById(R.id.quad_n);
		quadNE = (LinearLayout) this.getActivity().findViewById(R.id.quad_ne);
		quadW = (LinearLayout) this.getActivity().findViewById(R.id.quad_w);
		quadH = (RelativeLayout) this.getActivity().findViewById(R.id.quad_home);
		quadE = (LinearLayout) this.getActivity().findViewById(R.id.quad_e);
		quadSW = (LinearLayout) this.getActivity().findViewById(R.id.quad_sw);
		quadS = (LinearLayout) this.getActivity().findViewById(R.id.quad_s);
		quadSE = (LinearLayout) this.getActivity().findViewById(R.id.quad_se);

		OnClickListener buttonControl = new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				switch(arg0.getId()){
				case R.id.btn_take_photo:
					TakePhoto task = new TakePhoto();
				    task.execute(new String[] { ScareCrowActivity.URL+Integer.toHexString(ScareCrowActivity.HTTP_TAKE_PHOTO)});	
					break;
				case R.id.security_led:
					break;
				case R.id.security_picture:
					break;
				case R.id.security_sound:
					break;
				}
				
			}
			
		};
				
		btnPhoto.setOnClickListener(buttonControl);
		
		quadNW.setOnDragListener(new MyDragListener());
		quadN.setOnDragListener(new MyDragListener());
		quadNE.setOnDragListener(new MyDragListener());
		quadW.setOnDragListener(new MyDragListener());
		quadE.setOnDragListener(new MyDragListener());
		quadSW.setOnDragListener(new MyDragListener());
		quadS.setOnDragListener(new MyDragListener());
		quadSE.setOnDragListener(new MyDragListener());
		
		eventListView = (ListView) this.getActivity().findViewById(R.id.event_list_view);
		    eventAdapter = new EventAdapter(this.getActivity());
		    eventListView.setAdapter(eventAdapter);
		    
		    updateContent();
	}
	
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_security,
				container, false);

		// Show the dummy content as text in a TextView.
//		if (mItem != null) {
//			((TextView) rootView.findViewById(R.id.item_detail))
//					.setText(mItem.content);
//		}

		return rootView;
	}

	public void updateContent() {
		DownloadWebPageTask task = new DownloadWebPageTask();
		    task.execute(new String[] { ScareCrowActivity.URL+Integer.toHexString(ScareCrowActivity.HTTP_SETTINGS_SECURITY)});
	}

	public void saveContent() {
		JSONObject settings = new JSONObject();
		
		try {
		settings.put("SecEnable",securityEnable.isChecked());
		settings.put("SecSound",secSound.isChecked());
		settings.put("SecLed",secLed.isChecked());
		settings.put("SecPhoto",secPhoto.isChecked());
		settings.put("g1Position", garden1Position);
		settings.put("g2Position", garden2Position);
		settings.put("eyePosition", eyePosition);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		postJson(settings,Integer.toHexString(ScareCrowActivity.HTTP_SETTINGS_SECURITY));
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
