package com.scarecrow.graph;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.aes.pid.R;
import com.scarecrow.activities.ScareCrowActivity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

public class GraphFragment extends Fragment {

	private LinearLayout graphLayout;
	
	private GraphicalView graph = null;
	
	private ToggleButton btn1D,btn5D,btn10D,btn30D,btnLive;
	
	private CheckBox ckbBaseTemp,ckbBaseHumid,ckbBaseLight,ckbN1Soil,ckbN1Light, ckbN1Temp, ckbN2Soil, ckbN2Light, ckbN2Temp;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		graph = null;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_graph, container,
				false);

		return rootView;
	}

	public void onResume() {
		super.onResume();

		graphLayout = (LinearLayout) this.getActivity().findViewById(R.id.fullscreen_graph);
		if (graph==null){
		graph = SensorGraph.getGraph(SensorGraph.ZOOM_LEVEL_5DAY, new boolean[]{true,false,false,false,false,false,false,false, false});
		graphLayout.addView(graph);
		}
		
		ckbBaseTemp = (CheckBox) this.getActivity().findViewById(R.id.ckb_basetemp);
		ckbBaseHumid = (CheckBox) this.getActivity().findViewById(R.id.ckb_basehumid);
		ckbBaseLight = (CheckBox) this.getActivity().findViewById(R.id.ckb_baselight);
		ckbN1Soil = (CheckBox) this.getActivity().findViewById(R.id.ckb_n1soil);
		ckbN1Light = (CheckBox) this.getActivity().findViewById(R.id.ckb_n1light);
		ckbN1Temp = (CheckBox) this.getActivity().findViewById(R.id.ckb_n1temp);
		ckbN2Soil = (CheckBox) this.getActivity().findViewById(R.id.ckb_n2soil);
		ckbN2Light = (CheckBox) this.getActivity().findViewById(R.id.ckb_n2light);
		ckbN2Temp = (CheckBox) this.getActivity().findViewById(R.id.ckb_n2temp);
		
		ckbBaseTemp.setChecked(true);
		
		OnCheckedChangeListener seriesChecks = new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				
				graphLayout.removeView(graph);
				graph = SensorGraph.getGraph(SensorGraph.getCurrentZoom(), new boolean[]{
					ckbBaseTemp.isChecked(),ckbBaseHumid.isChecked(),ckbBaseLight.isChecked(),
					ckbN1Soil.isChecked(),ckbN1Light.isChecked(),ckbN1Temp.isChecked(),
					ckbN2Soil.isChecked(),ckbN2Light.isChecked(), ckbN2Temp.isChecked()});
				graphLayout.addView(graph);
					
				}
				
			
		};
		
		ckbBaseTemp.setOnCheckedChangeListener(seriesChecks);
		ckbBaseHumid.setOnCheckedChangeListener(seriesChecks);
		ckbBaseLight.setOnCheckedChangeListener(seriesChecks);
		ckbN1Soil.setOnCheckedChangeListener(seriesChecks);
		ckbN1Light.setOnCheckedChangeListener(seriesChecks);
		ckbN1Temp.setOnCheckedChangeListener(seriesChecks);
		ckbN2Soil.setOnCheckedChangeListener(seriesChecks);
		ckbN2Light.setOnCheckedChangeListener(seriesChecks);
		ckbN2Temp.setOnCheckedChangeListener(seriesChecks);
		
		btn1D = (ToggleButton) this.getActivity().findViewById(R.id.btn_zoom_1D);
		btn5D = (ToggleButton) this.getActivity().findViewById(R.id.btn_zoom_5D);
		btn10D = (ToggleButton) this.getActivity().findViewById(R.id.btn_zoom_10D);
		btn30D = (ToggleButton) this.getActivity().findViewById(R.id.btn_zoom_30D);
		btnLive = (ToggleButton) this.getActivity().findViewById(R.id.btn_zoom_live);

		btn5D.setChecked(true);
		
		OnCheckedChangeListener zoomListener = new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked){
					graphLayout.removeView(graph);
				btn1D.setChecked(false);
				btn1D.setClickable(true);
				btn5D.setChecked(false);
				btn5D.setClickable(true);

				btn10D.setChecked(false);
				btn10D.setClickable(true);

				btn30D.setChecked(false);
				btn30D.setClickable(true);

				btnLive.setChecked(false);
				btnLive.setClickable(true);

				
				
				switch(buttonView.getId()){
				case R.id.btn_zoom_1D:
						graph = SensorGraph.getGraph(SensorGraph.ZOOM_LEVEL_1DAY, SensorGraph.getCurrentSeries());
						btn1D.setChecked(true);
						btn1D.setClickable(false);
						break;
					
				case R.id.btn_zoom_5D:
					graph = SensorGraph.getGraph(SensorGraph.ZOOM_LEVEL_5DAY, SensorGraph.getCurrentSeries());
					btn5D.setChecked(true);
					btn5D.setClickable(false);
					break;
					
				case R.id.btn_zoom_10D:
					graph = SensorGraph.getGraph(SensorGraph.ZOOM_LEVEL_10DAY, SensorGraph.getCurrentSeries());
					btn10D.setChecked(true);
					btn10D.setClickable(false);
					break;
					
				case R.id.btn_zoom_30D:
					graph = SensorGraph.getGraph(SensorGraph.ZOOM_LEVEL_30DAY, SensorGraph.getCurrentSeries());
					btn30D.setChecked(true);
					btn30D.setClickable(false);
					break;
					
				case R.id.btn_zoom_live:
					graph = SensorGraph.getGraph(SensorGraph.ZOOM_LEVEL_LIVE, SensorGraph.getCurrentSeries());
					btnLive.setChecked(true);
					btnLive.setClickable(false);
					break;
					
				}
				graphLayout.addView(graph);
				
			}else{
//				switch(buttonView.getId()){
//				case R.id.btn_zoom_1D:
//						btn1D.setChecked(true);
//						break;
//					
//				case R.id.btn_zoom_5D:
//					btn5D.setChecked(true);
//					break;
//					
//				case R.id.btn_zoom_10D:
//					btn10D.setChecked(true);
//					break;
//					
//				case R.id.btn_zoom_30D:
//					btn30D.setChecked(true);
//					break;
//					
//				case R.id.btn_zoom_live:
//					btnLive.setChecked(true);
//					break;
//					
//				}
			}
			}
			
		};
		
		btn1D.setOnCheckedChangeListener(zoomListener);
		btn5D.setOnCheckedChangeListener(zoomListener);
		btn10D.setOnCheckedChangeListener(zoomListener);
		btn30D.setOnCheckedChangeListener(zoomListener);
		btnLive.setOnCheckedChangeListener(zoomListener);
	}

}