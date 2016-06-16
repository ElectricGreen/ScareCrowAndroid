package com.scarecrow.main;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.scarecrow.R;
import com.scarecrow.security.SecurityEvent;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

public class PhotoHandler implements PictureCallback {

  private final Context context;
  private int photoType = 0;
  private int photoNumber = 0;
  private Camera mCam;
  
Parameters param;

class PhotoRun implements Runnable{
	
	PhotoHandler handler;
	public void setHandler (PhotoHandler hand){
		this.handler = hand;
	}

	@Override
	public void run() {
		//Looper.prepare();
		Log.d("CAMERA", "Runable Started");
		try {
	        mCam = Camera.open();
	        Log.i("CAMERA", "Success");
	    } catch (RuntimeException e) {
	        Log.e("CAMERA", "Camera currently unavailable");
	        e.printStackTrace();
	    }
	    try {
	        param =  mCam.getParameters();
	        param.setPictureSize(1280, 960);
	        mCam.setParameters(param);
	        Log.i("CAMERA", "Success");
	    } catch (Exception e1) {
	        Log.e("CAMERA", "Parameter problem");
	        e1.printStackTrace();
	    }
	    try {
	        mCam.setPreviewDisplay(MainActivity.photoView.getHolder());
	        mCam.startPreview();
	        Thread.sleep(5000);
	        Log.i("CAMERA", "Success");
	    } catch (Exception e) {
	        Log.e("CAMERA", "Surface Problem");
	        e.printStackTrace();
	    }
	    try {
	    	//Handler picHand = new Handler();
//	        cam.takePicture(shutterCallback, new PhotoHandler(context,
//					PhotoHandler.PHOTO_USER, id), null);
	    	 mCam.takePicture(null, null, handler);
//	    	while(mCam != null){
//	    		Log.d("CAMERA", "waiting");
//	    		//Thread.sleep(2000);
//	    	}
	   }
     catch (Exception e)
     {
        System.out.println("Problem taking picture: " + e);
     }
		
	}
	
}
  public void takePicture(SurfaceView view, int type, int number)
  {
	    this.photoType = type;
	    this.photoNumber = number;
	    PhotoRun run = new PhotoRun();
	    run.setHandler(this);
	    run.run();
//	    new Thread(run).start();
	  
  }

  
  public PhotoHandler(Context context) {
    this.context = context;
  }

  @Override
  public void onPictureTaken(byte[] data, Camera camera) {
	  Log.d("CAMERA", "onpictaken");
    File pictureFileDir = getDir();

    if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

      Log.d("Photo", "Can't create directory to save image.");
      Toast.makeText(context, "Can't create directory to save image.",
          Toast.LENGTH_LONG).show();
      return;

    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
    String date = dateFormat.format(new Date());
    String photoFile = null;
    switch(photoType){
    case SecurityEvent.EVENT_SECURITY:
    	photoFile = "Photo_" + photoNumber + ".jpg";
    	break;
    case SecurityEvent.EVENT_WATER_ON:
    	photoFile = "Photo_" + photoNumber + "_ON.jpg";
    	break;
    case SecurityEvent.EVENT_WATER_OFF:
    	photoFile = "Photo_" + photoNumber + "_OFF.jpg";
    	break;
    case SecurityEvent.EVENT_USER:
    	photoFile = "Photo_" + photoNumber + ".jpg";
    	break;
    }

    String filename = pictureFileDir.getPath() + File.separator + photoFile;

    File pictureFile = new File(filename);

    try {
      FileOutputStream fos = new FileOutputStream(pictureFile);
      fos.write(data);
      fos.close();
      Log.d("CAMERA", "New Image saved:" + filename);
    } catch (Exception error) {
      Log.d("CAMERA", "File" + filename + "not saved: "
          + error.getMessage());
    }
    camera.release();
    camera = null;
  }
  private File getDir() {
    File sdDir = Environment
      .getExternalStorageDirectory();
    switch(photoType){
    case SecurityEvent.EVENT_SECURITY:
        return new File(sdDir, "www/Photo/Security");
    case SecurityEvent.EVENT_WATER_ON:
    case SecurityEvent.EVENT_WATER_OFF:
        return new File(sdDir, "www/Photo/Water");
    case SecurityEvent.EVENT_USER:
        return new File(sdDir, "www/Photo/User");
    }
    return new File(sdDir, "www/Photo/Other");
  }
} 