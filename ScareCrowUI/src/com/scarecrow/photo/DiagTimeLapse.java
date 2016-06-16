package com.scarecrow.photo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.aes.pid.R;
import com.scarecrow.activities.ScareCrowActivity;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class DiagTimeLapse extends DialogFragment {
		
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
//				securityEnable.setChecked(jsonObject.getBoolean("SecEnable"));
//				secSound.setChecked(jsonObject.getBoolean("SecSound"));
//				secLed.setChecked(jsonObject.getBoolean("SecLed"));
//				secPhoto.setChecked(jsonObject.getBoolean("SecPhoto"));
//				garden1Position = jsonObject.getInt("g1Position");
//				garden2Position = jsonObject.getInt("g2Position");
//				eyePosition = jsonObject.getInt("eyePosition");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	    	}
	    }
	  }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_timelapse, container);
        getDialog().setTitle("Time Lapse");
        
        //Get current timelapse settings
        DownloadWebPageTask task = new DownloadWebPageTask();
	    task.execute(new String[] { ScareCrowActivity.URL+Integer.toHexString(ScareCrowActivity.HTTP_SETTINGS_TIMELAPSE)});
	    
	    
	    
        return view;
    }
}
