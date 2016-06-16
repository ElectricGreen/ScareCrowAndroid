package com.scarecrow.graph;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.TimeChart;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.tools.PanListener;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.scarecrow.activities.ScareCrowActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class SensorGraph {
	/*
	 * Samples every 10s
	 * 6 Samples a minute
	 * 360 Samples an hour
	 * 8640 Samples a day
	 * 
	 * 6 Hour			= 2160 Max Samples
	 * 5 Day  - /24 	= 1800 Max Samples
	 * 10 Day - /48 	= 1800 Max Samples
	 * 30 Day - /96 	= 2700 Max Samples
	 */
	
	private static Context mainContext;
	private static XYMultipleSeriesRenderer renderer;
	private static SensorDatabase db;
	
	private static Handler handler;

	private static int[] currentIndex = new int[4];

	public static int getCurrentIndex(int i){
		return currentIndex[i];
	}
	private static int dataRate = 0;
	private static int recordRate = 0;
	
	public static SensorData newestData = new SensorData();
		
	//Indexes
	public static final int ZOOM_LEVEL_1DAY 	= 0;
	public static final int ZOOM_LEVEL_5DAY 	= 1;
	public static final int ZOOM_LEVEL_10DAY 	= 2;
	public static final int ZOOM_LEVEL_30DAY 	= 3;
	public static final int ZOOM_LEVEL_LIVE 	= 4;

	//Dividers
	public static final int ZOOM_VALUE_5DAY 	= 5;
	public static final int ZOOM_VALUE_10DAY 	= 10;
	public static final int ZOOM_VALUE_30DAY 	= 30;
	
	ProgressDialog progressDiag;

	private static TimeSeries[] baseTempSeries = new TimeSeries[5];
	private static TimeSeries[] baseHumidSeries = new TimeSeries[5];
	private static TimeSeries[] baseLightSeries = new TimeSeries[5];
	
	private static TimeSeries[] node1SoilSeries = new TimeSeries[5];
	private static TimeSeries[] node2SoilSeries= new TimeSeries[5];
	private static TimeSeries[] node1LightSeries = new TimeSeries[5];
	private static TimeSeries[] node2LightSeries = new TimeSeries[5];
	private static TimeSeries[] node1TempSeries = new TimeSeries[5];
	private static TimeSeries[] node2TempSeries = new TimeSeries[5];

	private static int currentZoom = 0;
	private static boolean[] currentSeries = new boolean[9];
	
	private static GraphicalView graphView;
	
	public static Lock networkLock;
	
	private class DownloadUpdate extends AsyncTask<String, Void, String> {
		
	    @Override
	    protected String doInBackground(String... urls) {
	      String response = "";
	      for (String url : urls) {
//	    	  networkLock.lock();
//	    	  try {
	    		   // access the resource protected by this lock
	    		  
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
//	    	  }finally {
//	    		  networkLock.unlock();
//	      		 }
	      }
	      return response;
	      
	    }

	    @Override
	    protected void onPostExecute(String result) {
	    	if (result.length()>5){
	        try {
				JSONObject jsonObject = new JSONObject(result);
				newestData = new SensorData(0,Calendar.getInstance().getTimeInMillis(),
						jsonObject.getDouble("baseTemp"),jsonObject.getDouble("baseHumid"),(byte)jsonObject.getInt("baseLight"),
						(byte)jsonObject.getInt("n1Soil"),(byte)jsonObject.getInt("n1Light"),jsonObject.getDouble("n1Temp"),
						(byte)jsonObject.getInt("n2Soil"),(byte)jsonObject.getInt("n2Light"),jsonObject.getDouble("n2Temp"));
				newestData.nodeBattery[0] = (byte)jsonObject.getInt("n1Battery");
				newestData.nodeBattery[1] = (byte)jsonObject.getInt("n2Battery");

				SensorGraph.addLiveData(newestData);		
				
				ScareCrowActivity.updateUI();
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        
	    	}
	    }
	  }
	
	private Runnable runnable = new Runnable() {
	 	   @Override
	 	   public void run() {
	 		  DownloadUpdate task = new DownloadUpdate();
	 		    task.execute(new String[] { ScareCrowActivity.URL});	
	 	      handler.postDelayed(this, dataRate*1000);
	 	   }
	 	};
	 	
	 private class ParseData implements Runnable {
		 String dataString;
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
					SensorGraph.addData(new SensorData(jsonObject.getInt("id"),jsonObject.getLong("time"),
							jsonObject.getDouble("baseTemp"),jsonObject.getDouble("baseHumid"),(byte)jsonObject.getInt("baseLight"),
							(byte)jsonObject.getInt("n1Soil"),(byte)jsonObject.getInt("n1Light"),jsonObject.getDouble("n1Temp"),
							(byte)jsonObject.getInt("n2Soil"),(byte)jsonObject.getInt("n2Light"),jsonObject.getDouble("n2Temp")),true);		
			        }
				} catch (JSONException e) {

					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        progressDiag.dismiss();
//		        this.notifyAll();
		 	   }
		 	};
		 	
	private class LoadDB implements Runnable {
			 		@Override
				 	   public void run() {		

			 			Looper.prepare();

			 			List<SensorData> data = db.getAllEntries();
			 
				 		  progressDiag.setMax(data.size());

						for(int i=0;i<data.size();i++){
							SensorGraph.addData(data.get(i),false);
							progressDiag.incrementProgressBy(1);
						}
						
						progressDiag.dismiss();
					      
						SensorData lastEntry = db.getData(db.getDataCount());
						DownloadArchiveData task = new DownloadArchiveData();
			 		    task.execute(new String[] { ScareCrowActivity.URL+Integer.toHexString(ScareCrowActivity.HTTP_READ_DATA)
			 		    		+String.format("%08X", lastEntry.id+1)});	
				 	}
		 	};
		 	
	private class DownloadArchiveData extends AsyncTask<String, Void, String> {
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
	    		progressDiag = new ProgressDialog(mainContext);
			      progressDiag.setTitle("Downloading Data");
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
	
	public SensorGraph(Context appContext, int rate, int recRate){
		mainContext = appContext;
		dataRate = rate;
		recordRate = recRate;
		
		db = new SensorDatabase(appContext);
//		db.eraseAll();
		
	      for (int i=0; i<5; i++){
				baseTempSeries[i] = new TimeSeries("BaseTemp:"+i);
				baseHumidSeries[i] = new TimeSeries("BaseHumid:"+i);
				baseLightSeries[i] = new TimeSeries("BaseLight:"+i);
				
				node1SoilSeries[i] = new TimeSeries("Node1Soil:"+i);

				node2SoilSeries[i] = new TimeSeries("Node2Soil:"+i);
				node1LightSeries[i] = new TimeSeries("Node1Light:"+i);

				node2LightSeries[i] = new TimeSeries("Node2Light:"+i);
				node1TempSeries[i] = new TimeSeries("Node1Temp:"+i);
				node2TempSeries[i] = new TimeSeries("Node2Temp:"+i);

			}
	      
		if (db.getDataCount()==0){
			Log.d("SensorGraph", "Download archive");
			DownloadArchiveData task = new DownloadArchiveData();
 		    task.execute(new String[] { ScareCrowActivity.URL+Integer.toHexString(ScareCrowActivity.HTTP_READ_DATA)+"00000000"});	
			
		}else{
			progressDiag = new ProgressDialog(mainContext);
		      progressDiag.setTitle("Loading Data");
		      progressDiag.setMessage("Loading from DB ...");
		      progressDiag.setProgressStyle(progressDiag.STYLE_HORIZONTAL);
		      progressDiag.setProgress(0);
		      progressDiag.setMax(100);
		      progressDiag.setCancelable(false);
		      progressDiag.show();
		      
			LoadDB startup = new LoadDB();
			new Thread(startup).start();
		}
		handler = new Handler();
	    handler.postDelayed(runnable, 500);

		
	}
	
	public static int getCurrentZoom(){
		return currentZoom;
	}
	
	public static boolean[] getCurrentSeries(){
		return currentSeries;
	}
	
	public static void addLiveData(SensorData data){
		baseTempSeries[ZOOM_LEVEL_LIVE].add(data.time,
				data.baseTemp);
		baseHumidSeries[ZOOM_LEVEL_LIVE].add(data.time,
				data.baseHumid);
		baseLightSeries[ZOOM_LEVEL_LIVE].add(data.time,
				data.baseLight);

		node1SoilSeries[ZOOM_LEVEL_LIVE].add(data.time,
				data.nodeSoil[0]);
		node2SoilSeries[ZOOM_LEVEL_LIVE].add(data.time,
				data.nodeSoil[1]);
		node1LightSeries[ZOOM_LEVEL_LIVE].add(data.time,
				data.nodeLight[0]);
		node2LightSeries[ZOOM_LEVEL_LIVE].add(data.time,
				data.nodeLight[1]);
		node1TempSeries[ZOOM_LEVEL_LIVE].add(data.time,
				data.nodeTemp[0]);
		node2TempSeries[ZOOM_LEVEL_LIVE].add(data.time,
				data.nodeTemp[1]);
	}
	
	public static void addData(SensorData data, boolean includeDB) {
		if (includeDB)
			db.addData(data);
		
		if (currentIndex[0] == currentIndex[1] * ZOOM_VALUE_5DAY) {
			if (currentIndex[0] == currentIndex[2] * ZOOM_VALUE_10DAY) {
				if (currentIndex[0] == currentIndex[3] * ZOOM_VALUE_30DAY) {
					// Highest Zoom Level
					baseTempSeries[ZOOM_LEVEL_30DAY].add(data.time,
							data.baseTemp);
					baseHumidSeries[ZOOM_LEVEL_30DAY].add(data.time,
							data.baseHumid);
					baseLightSeries[ZOOM_LEVEL_30DAY].add(data.time,
							data.baseLight);

					node1SoilSeries[ZOOM_LEVEL_30DAY].add(data.time,
							data.nodeSoil[0]);
					node2SoilSeries[ZOOM_LEVEL_30DAY].add(data.time,
							data.nodeSoil[1]);
					node1LightSeries[ZOOM_LEVEL_30DAY].add(data.time,
							data.nodeLight[0]);
					node2LightSeries[ZOOM_LEVEL_30DAY].add(data.time,
							data.nodeLight[1]);
					node1TempSeries[ZOOM_LEVEL_30DAY].add(data.time,
							data.nodeTemp[0]);
					node2TempSeries[ZOOM_LEVEL_30DAY].add(data.time,
							data.nodeTemp[1]);

					currentIndex[3]++;
				}

				// 10 Day Zoom Level
				baseTempSeries[ZOOM_LEVEL_10DAY].add(data.time, data.baseTemp);
				baseHumidSeries[ZOOM_LEVEL_10DAY]
						.add(data.time, data.baseHumid);
				baseLightSeries[ZOOM_LEVEL_10DAY]
						.add(data.time, data.baseLight);

				node1SoilSeries[ZOOM_LEVEL_10DAY].add(data.time,
						data.nodeSoil[0]);
				node2SoilSeries[ZOOM_LEVEL_10DAY].add(data.time,
						data.nodeSoil[1]);
				node1LightSeries[ZOOM_LEVEL_10DAY].add(data.time,
						data.nodeLight[0]);
				node2LightSeries[ZOOM_LEVEL_10DAY].add(data.time,
						data.nodeLight[1]);
				node1TempSeries[ZOOM_LEVEL_10DAY].add(data.time,
						data.nodeTemp[0]);
				node2TempSeries[ZOOM_LEVEL_10DAY].add(data.time,
						data.nodeTemp[1]);

				currentIndex[2]++;
			}
			// 5 Day Zoom Level
			baseTempSeries[ZOOM_LEVEL_5DAY].add(data.time, data.baseTemp);
			baseHumidSeries[ZOOM_LEVEL_5DAY].add(data.time, data.baseHumid);
			baseLightSeries[ZOOM_LEVEL_5DAY].add(data.time, data.baseLight);

			node1SoilSeries[ZOOM_LEVEL_5DAY].add(data.time, data.nodeSoil[0]);
			node2SoilSeries[ZOOM_LEVEL_5DAY].add(data.time, data.nodeSoil[1]);
			node1LightSeries[ZOOM_LEVEL_5DAY].add(data.time, data.nodeLight[0]);
			node2LightSeries[ZOOM_LEVEL_5DAY].add(data.time, data.nodeLight[1]);
			node1TempSeries[ZOOM_LEVEL_5DAY].add(data.time, data.nodeTemp[0]);
			node2TempSeries[ZOOM_LEVEL_5DAY].add(data.time, data.nodeTemp[1]);
			currentIndex[1]++;
		}

		baseTempSeries[ZOOM_LEVEL_1DAY].add(data.time, data.baseTemp);
		baseHumidSeries[ZOOM_LEVEL_1DAY].add(data.time, data.baseHumid);
		baseLightSeries[ZOOM_LEVEL_1DAY].add(data.time, data.baseLight);

		node1SoilSeries[ZOOM_LEVEL_1DAY].add(data.time, data.nodeSoil[0]);
		node2SoilSeries[ZOOM_LEVEL_1DAY].add(data.time, data.nodeSoil[1]);
		node1LightSeries[ZOOM_LEVEL_1DAY].add(data.time, data.nodeLight[0]);
		node2LightSeries[ZOOM_LEVEL_1DAY].add(data.time, data.nodeLight[1]);
		node1TempSeries[ZOOM_LEVEL_1DAY].add(data.time, data.nodeTemp[0]);
		node2TempSeries[ZOOM_LEVEL_1DAY].add(data.time, data.nodeTemp[1]);
		// Increment root index
		currentIndex[0]++;
	}
	
	public static GraphicalView getGraph(int zoom, boolean[] series){
		currentZoom = zoom;
		currentSeries = series;
		
		XYMultipleSeriesDataset dataSets = getDataset(zoom, series);
		
		renderer = new XYMultipleSeriesRenderer(dataSets.getSeriesCount());
		
//		renderer.setApplyBackgroundColor(true);
//		renderer.setBackgroundColor(Color.TRANSPARENT);
		renderer.setXLabelsColor(Color.WHITE);
		renderer.setShowGrid(true);
		renderer.setYLabelsAlign(Align.LEFT);
//		renderer.setChartTitle("Garden 1 Conditions");
		renderer.setXTitle("Date");
//		renderer.setYTitle("Soil Moisture");
		
		renderer.setLabelsTextSize(20);
		renderer.setAxisTitleTextSize(20);
		renderer.setLegendTextSize(20);
		renderer.setChartTitleTextSize(60);
		renderer.setXLabels(12);
		renderer.setYLabels(12);
		renderer.setMargins(new int[]{0,5,5,0});
		renderer.setMarginsColor(0x00FFFFFF);
		renderer.setShowLegend(false);
		
		
		XYSeriesRenderer[] lines = new XYSeriesRenderer[dataSets.getSeriesCount()];
		ArrayList<Integer> colors = getColors(series);
		
		for (int i=0;i<dataSets.getSeriesCount();i++){
			Log.d("SensorGraph", "Size of Series: "+ dataSets.getSeriesAt(0).getItemCount());
			lines[i] = new XYSeriesRenderer();
			lines[i].setColor(colors.get(i));
			lines[i].setLineWidth(5);
			renderer.addSeriesRenderer(lines[i]);
		}
		
		renderer.setZoomButtonsVisible(false);
		double[] panLimits = new double[]{
				-Double.MAX_VALUE
    			,Double.MAX_VALUE
    			, -30
    			,120
    	};
		renderer.setPanLimits(panLimits);
		
        Calendar c = Calendar.getInstance();
        renderer.setYAxisMax(120);
        renderer.setYAxisMin(-30);
        switch(zoom){
        case ZOOM_LEVEL_1DAY:
            renderer.setXAxisMin(c.getTimeInMillis()-24*60*60*1000);
            renderer.setXAxisMax(c.getTimeInMillis());
        	break;
        case ZOOM_LEVEL_5DAY:
        	renderer.setXAxisMin(c.getTimeInMillis()-24*60*60*5*1000);
            renderer.setXAxisMax(c.getTimeInMillis());
        	break;
        case ZOOM_LEVEL_10DAY:
        	renderer.setXAxisMin(c.getTimeInMillis()-24*60*60*10*1000);
            renderer.setXAxisMax(c.getTimeInMillis());
        	break;
        case ZOOM_LEVEL_30DAY:
        	long min = c.getTimeInMillis()-24*60*60*10*1000;
        	for (int i=0; i<2;i++){
        		min -=24*60*60*10*1000;
        	}
        	renderer.setXAxisMin(min);
            renderer.setXAxisMax(c.getTimeInMillis());
        	break;
        case ZOOM_LEVEL_LIVE:
            renderer.setXAxisMin(c.getTimeInMillis()-24*60*60*1000);
            renderer.setXAxisMax(c.getTimeInMillis());
        	break;
        }
		setZoomLimits(currentZoom, (renderer.getXAxisMax()+renderer.getXAxisMin())*.5);
		
        Log.d("SensorGraph", "Dataset size: "+dataSets.getSeriesCount());
		graphView = ChartFactory.getTimeChartView(mainContext, dataSets, 
				renderer,"MM:DD:YY H:mm:ss");
		
        graphView.addPanListener(new PanListener(){

			@Override
			public void panApplied() {
				setZoomLimits(currentZoom, (renderer.getXAxisMax()+renderer.getXAxisMin())*.5);
			}
        });
        
        return graphView;
	}
	
	private static ArrayList<Integer> getColors(boolean[] series) {
		ArrayList<Integer> colors = new ArrayList<Integer>();

		if (series[0]){
			colors.add(Color.RED);
		}
		
		if (series[1]){
			colors.add(Color.BLUE);
		}
		
		if (series[2]){
			colors.add(Color.YELLOW);
		}
		
		if (series[3]){
			colors.add(Color.CYAN);
		}
	
		if (series[4]){
			colors.add(Color.GREEN);
		}
		
		if (series[5]){
			colors.add(Color.DKGRAY);
		}
		
		if (series[6]){
			colors.add(Color.MAGENTA);
		}
		
		if (series[7]){
			colors.add(Color.WHITE);
		}
		
		if (series[8]){
			colors.add(Color.GRAY);
		}
		
		return colors;
	}

	private static void setZoomLimits(int zoom, double middleX) {
		double[] zoom1Limits = new double[]{
				middleX - 12*60*60*1000
    			,middleX + 12*60*60*1000
    			, -30
    			,120
    	};
		
		double[] zoom2Limits = new double[]{
				middleX - 12*60*60*5*1000,
				middleX + 12*60*60*5*1000,
    			-30,
    			120
    	};
		double[] zoom3Limits = new double[]{
				middleX - 432000000
    			,middleX + 432000000
    			, -30
    			,120
    	};
		double[] zoom4Limits = new double[]{
				middleX - 1296000000
    			,middleX + 1296000000
    			, -30
    			,120
    	};
		
        switch(zoom){
        case ZOOM_LEVEL_1DAY:
            renderer.setZoomLimits(zoom1Limits);
        	break;
        case ZOOM_LEVEL_5DAY:
        	renderer.setZoomLimits(zoom2Limits);
        	break;
        case ZOOM_LEVEL_10DAY:
        	renderer.setZoomLimits(zoom3Limits);
        	break;
        case ZOOM_LEVEL_30DAY:
        	renderer.setZoomLimits(zoom4Limits);
        	break;
        case ZOOM_LEVEL_LIVE:
        	renderer.setZoomLimits(zoom1Limits);
        	break;
        }
	}
 
	private static XYMultipleSeriesDataset getDataset(int zoom, boolean[] series) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		
		int count = 0;
		
		if (series[0]){
			dataset.addSeries(baseTempSeries[zoom]);
			count++;
		}
		
		if (series[1]){
			dataset.addSeries(baseHumidSeries[zoom]);
			count++;
		}
		
		if (series[2]){
			dataset.addSeries(baseLightSeries[zoom]);
			count++;
		}
		
		if (series[3]){
			dataset.addSeries(node1SoilSeries[zoom]);
			count++;
		}
	
		if (series[4]){
			dataset.addSeries(node1LightSeries[zoom]);
			count++;
		}
		
		if (series[5]){
			dataset.addSeries(node1TempSeries[zoom]);
			count++;
		}
		
		if (series[6]){
			dataset.addSeries(node2SoilSeries[zoom]);
			count++;
		}
		
		if (series[7]){
			dataset.addSeries(node2LightSeries[zoom]);
			count++;
		}
		
		if (series[8]){
			dataset.addSeries(node2TempSeries[zoom]);
			count++;
		}
		return dataset;
	}
}
