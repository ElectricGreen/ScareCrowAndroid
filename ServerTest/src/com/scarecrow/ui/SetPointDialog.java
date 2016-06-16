package com.scarecrow.ui;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

import com.scarecrow.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

public class SetPointDialog extends Dialog implements android.view.View.OnClickListener {
	Context context;
	Button btnSet,btnCancel;

	public SetPointDialog(Context context) {
		super(context);
        // This is the layout XML file that describes your Dialog layout
        this.context = context;
	}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_set_temp);	
        
        Integer[] values = new Integer[121];
        for (int i=0;i<121;i++){
        	values[i] = i;
        }
        Integer[] decValues = new Integer[10];
        for (int i=0;i<10;i++){
        	decValues[i] = i;
        }
        
        btnSet = (Button) findViewById(R.id.vapor_diag_set);
        btnCancel = (Button) findViewById(R.id.vapor_diag_cancel);
        btnSet.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
       
        WheelView wheel = (WheelView) findViewById(R.id.diag_set_value);
		ArrayWheelAdapter<Integer> intAdapter = new ArrayWheelAdapter<Integer>(context, values);
		intAdapter.setTextSize(100);
		wheel.setViewAdapter(intAdapter);
		wheel.setVisibleItems(4);
		wheel.setBackgroundColor(00000000);
		wheel.setCurrentItem(0);
		
		WheelView decWheel = (WheelView) findViewById(R.id.diag_set_value_decimal);
		ArrayWheelAdapter<Integer> adapter = new ArrayWheelAdapter<Integer>(context, decValues);
		adapter.setTextSize(100);
		decWheel.setViewAdapter(adapter);
		decWheel.setVisibleItems(2);
		decWheel.setBackgroundColor(00000000);
		decWheel.setCurrentItem(0);
		this.setTitle("Set Point");
		getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.vapor_diag_cancel:
			dismiss();
			break;
		case R.id.vapor_diag_set:
			dismiss();
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
			url = new URL("192.168.1.91"+ urlExt);

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
