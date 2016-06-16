package com.scarecrow.home;


import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import com.aes.pid.R;
import com.scarecrow.activities.ScareCrowActivity;
import com.scarecrow.graph.SensorGraph;
import com.scarecrow.watering.WateringFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeFragment extends Fragment {

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	private static TextView temperatureValue,humidValue,n1Temp,n2Temp,n1Soil,n2Soil,n1Light,n2Light,n1Battery,n2Battery;

	private ImageButton garden1Btn;
	private ImageButton garden2Btn;
	
	public HomeFragment() {
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
	
	@Override 
	public void onResume() {
		super.onResume();
		temperatureValue 	= 	(TextView) this.getActivity().findViewById(R.id.mainTempValue);	
		humidValue 		 	= 	(TextView) this.getActivity().findViewById(R.id.mainHumidValue);
		n1Temp				= 	(TextView) this.getActivity().findViewById(R.id.garden1TempValue);
		n2Temp				= 	(TextView) this.getActivity().findViewById(R.id.garden2TempValue);
		n1Soil				= 	(TextView) this.getActivity().findViewById(R.id.garden1SoilValue);
		n2Soil				= 	(TextView) this.getActivity().findViewById(R.id.garden2SoilValue);
		n1Light				= 	(TextView) this.getActivity().findViewById(R.id.garden1LightValue);
		n2Light				= 	(TextView) this.getActivity().findViewById(R.id.garden2LightValue);
		n1Battery			= 	(TextView) this.getActivity().findViewById(R.id.garden1BatteryValue);
		n2Battery			= 	(TextView) this.getActivity().findViewById(R.id.garden2BatteryValue);
		
		garden1Btn = (ImageButton) this.getActivity().findViewById(R.id.garden1Btn);
		garden2Btn = (ImageButton) this.getActivity().findViewById(R.id.garden2Btn);

		garden1Btn.setOnClickListener(new OnClickListener() {
 
			@Override
			public void onClick(View arg0) {
				ScareCrowActivity.fragWater.setGarden(1);
				getFragmentManager().beginTransaction()
				.replace(R.id.main, ScareCrowActivity.fragWater).commit(); 
			}
 
		});
		garden2Btn.setOnClickListener(new OnClickListener() {
			 
			@Override
			public void onClick(View arg0) {
				ScareCrowActivity.fragWater.setGarden(2);
				getFragmentManager().beginTransaction()
				.replace(R.id.main, ScareCrowActivity.fragWater).commit(); 
			}
 
		});
		updateUI();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_home,
				container, false);

		// Show the dummy content as text in a TextView.
//		if (mItem != null) {
//			((TextView) rootView.findViewById(R.id.item_detail))
//					.setText(mItem.content);
//		}

		return rootView;
	}

	public static void updateUI(){
		if (temperatureValue==null)
			return;

			temperatureValue.setText(String.format("%.1f", SensorGraph.newestData.baseTemp));

			humidValue.setText(String.format("%.1f", SensorGraph.newestData.baseHumid));
		
			n1Temp.setText(String.format("%.0f", SensorGraph.newestData.nodeTemp[0]));
			n2Temp.setText(String.format("%.0f", SensorGraph.newestData.nodeTemp[1]));
			n1Soil.setText(String.format("%d", SensorGraph.newestData.nodeSoil[0]));
			n2Soil.setText(String.format("%d", SensorGraph.newestData.nodeSoil[1]));
			n1Light.setText(String.format("%d", SensorGraph.newestData.nodeLight[0]));
			n2Light.setText(String.format("%d", SensorGraph.newestData.nodeLight[1]));
			n1Battery.setText(String.format("%d", SensorGraph.newestData.nodeBattery[0]));
			n2Battery.setText(String.format("%d", SensorGraph.newestData.nodeBattery[1]));
			
	}

	public static void ipDiag() {
		// TODO Auto-generated method stub
		
	}

	
}
